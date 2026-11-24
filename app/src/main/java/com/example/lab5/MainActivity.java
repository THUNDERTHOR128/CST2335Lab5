package com.example.lab5;

import android.os.Bundle;
import android.os.AsyncTask;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private ImageView catImage;
    private ProgressBar progressBar;
    private CatImages catTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        catImage = findViewById(R.id.catImage);
        progressBar = findViewById(R.id.progressBar);

        catTask = new CatImages();
        catTask.execute();
    }

    private class CatImages extends AsyncTask<String, Integer, String> {

        private Bitmap bitmap;

        @Override
        protected String doInBackground(String... strings) {

            while (!isCancelled()) {

                try {
                    // Request a random cat from the API
                    URL apiUrl = new URL(
                            "https://cataas.com/cat?json=true");

                    HttpURLConnection connection =
                            (HttpURLConnection) apiUrl.openConnection();

                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(10000);

                    String response;

                    try (Scanner scanner =
                                 new Scanner(connection.getInputStream())) {

                        scanner.useDelimiter("\\A");
                        response = scanner.next();
                    } finally {
                        connection.disconnect();
                    }

                    JSONObject json = new JSONObject(response);

                    String id = json.has("id")
                            ? json.getString("id")
                            : json.getString("_id");

                    String imageUrl = json.optString(
                            "url", "https://cataas.com/cat/" + id);

                    if (imageUrl.startsWith("/")) {
                        imageUrl = "https://cataas.com" + imageUrl;
                    }

                    File imageFile = new File(
                            getFilesDir(), id + ".jpg");

                    // Check whether the image already exists
                    if (imageFile.exists()) {

                        bitmap = BitmapFactory.decodeFile(
                                imageFile.getAbsolutePath());

                        Log.d("Lab6", "Loaded local image: " + id);

                    } else {

                        bitmap = null;
                    }

                    // Download the image if necessary
                    if (bitmap == null) {

                        URL url = new URL(imageUrl);

                        HttpURLConnection imageConnection =
                                (HttpURLConnection) url.openConnection();

                        imageConnection.setConnectTimeout(10000);
                        imageConnection.setReadTimeout(10000);

                        try (InputStream input =
                                     imageConnection.getInputStream()) {

                            bitmap = BitmapFactory.decodeStream(input);

                        } finally {
                            imageConnection.disconnect();
                        }

                        if (bitmap == null) {
                            throw new Exception("Invalid image");
                        }

                        // Save the downloaded image locally
                        try (FileOutputStream output =
                                     new FileOutputStream(imageFile)) {

                            bitmap.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    100,
                                    output);
                        }

                        Log.d("Lab6", "Downloaded image: " + id);
                    }

                    // Display the new picture
                    publishProgress(-1);

                    // Update progress bar for 5 seconds
                    for (int i = 0; i <= 100 && !isCancelled(); i++) {

                        publishProgress(i);
                        Thread.sleep(50);
                    }

                } catch (Exception e) {

                    Log.e("Lab6", "Error loading cat image", e);

                    if (isCancelled()) {
                        break;
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }

            return "Finished";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {

            if (values[0] == -1) {

                catImage.setImageBitmap(bitmap);
                progressBar.setProgress(0);

            } else {

                progressBar.setProgress(values[0]);
            }
        }
    }

    @Override
    protected void onDestroy() {

        if (catTask != null) {
            catTask.cancel(true);
        }

        super.onDestroy();
    }
}
