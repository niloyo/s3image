package dependency.nilmadhab.com.s3upload;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Time;
import java.util.Iterator;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    String key = "AKIAJXB4JP3SDZT4YQ4A";
    String secret = "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";

    BasicAWSCredentials credentials;


    AmazonS3Client s3;

    TransferUtility transferUtility;

    ProgressBar pb;
    Button btn_upload;
    TextView _status;

    ImageView imageView, imageView2;


    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pb = (ProgressBar) findViewById(R.id.progressBar);
        btn_upload = (Button) findViewById(R.id.btn_upload);
        _status = (TextView) findViewById(R.id.txt_progress);
        imageView = (ImageView) findViewById(R.id.image);

        imageView2 = (ImageView) findViewById(R.id.image2);


        requestForSpecificPermission();

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                credentials = new BasicAWSCredentials(key, secret);
                s3 = new AmazonS3Client(credentials);
                //s3.setEndpoint("http://localhost:9444/s3");
                //s3.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).disableChunkedEncoding().build());
                //s3.setS3ClientOptions(S3ClientOptions.builder().setPathStyleAccess(true).d);
                transferUtility = new TransferUtility(s3, MainActivity.this);

                String path = Environment.getExternalStorageDirectory().getPath();
                //String myJpgPath = path + "/Download/images.jpg";
                String myJpgPath = path + "/Download/images.jpg";

                Bitmap myBitmap = BitmapFactory.decodeFile(myJpgPath);

                imageView.setImageBitmap(myBitmap);

                Log.d("Nilmadhab", myJpgPath);

                //String path = "abc.png" ;


                File file = new File(myJpgPath);

                File file1 = new File(myJpgPath);

                if (!file.exists()) {
                    Toast.makeText(MainActivity.this, "File Not Found!", Toast.LENGTH_SHORT).show();
                    return;
                }


                Log.d("Nilmadhab", file.getAbsolutePath() + " " + file.getName());

                //Log.d("Nimadhab", file.);

                //InputStream is=file.getInputStream();
                //s3client.putObject(new PutObjectRequest(bucketName, keyName,is,new ObjectMetadata()));
                String output = new Random().nextInt() + file.getName();

                final String s3Name = "https://s3.amazonaws.com/bucketforflask/"+output;

                final TransferObserver observer = transferUtility.upload(
                        "bucketforflask",
                        output,
                        file
                );



                observer.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {

                        if (state.COMPLETED.equals(observer.getState())) {

                            Toast.makeText(MainActivity.this, "File Upload Complete", Toast.LENGTH_SHORT).show();
                            new LongOperation().execute(s3Name);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {


                        long _bytesCurrent = bytesCurrent;
                        long _bytesTotal = bytesTotal;

                        float percentage = ((float) _bytesCurrent / (float) _bytesTotal * 100);
                        Log.d("percentage", "" + percentage);
                        pb.setProgress((int) percentage);
                        _status.setText(percentage + "%");
                    }

                    @Override
                    public void onError(int id, Exception ex) {

                        Toast.makeText(MainActivity.this, "" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        public DownloadImageTask(ImageView bmImage) {
            imageView = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            imageView2.setImageBitmap(result);
        }
    }


    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            try {

                URL url = new URL("http://107.21.9.12/upload");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("myfile", params[0]);
                Log.e("params", postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);


                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode = conn.getResponseCode();

                if (false || responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while ((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                } else {
                    return new String("false : " + responseCode);
                }
            } catch (Exception e) {
                return new String("Exception: " + e.getMessage());
            }

        }

        public String getPostDataString(JSONObject params) throws Exception {

            StringBuilder result = new StringBuilder();
            boolean first = true;

            Iterator<String> itr = params.keys();

            while(itr.hasNext()){

                String key= itr.next();
                Object value = params.get(key);

                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));
                //result = new StringBuilder(value.toString());



            }
            return result.toString();
        }

        public String getoutputString(JSONObject params) throws Exception {

            StringBuilder result = new StringBuilder();
            boolean first = true;

            Iterator<String> itr = params.keys();

            while(itr.hasNext()){

                String key= itr.next();
                Object value = params.get(key);

                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(key, "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(value.toString(), "UTF-8"));
                //result = new StringBuilder(value.toString());
                return value.toString();


            }
            return result.toString();
        }


    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            String url1 = getoutputString(jsonObject);
            new DownloadImageTask(imageView).execute(url1);
            URL url = null;
            try {
                url = new URL(url1);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("Nilmadhab", result);

        //String url1 = value.toString();



    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}
}
