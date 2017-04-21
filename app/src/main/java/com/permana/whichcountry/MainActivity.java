package com.permana.whichcountry;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    String baseUrl;
    ArrayList<String> countryUrls = new ArrayList<String>();
    ArrayList<String> countryNames = new ArrayList<String>();
    int chosenCountry = 0;
    ImageView imageView;
    final int totalAnswer = 4;
    int locationOfCorrectAnswer, incorrectAnswerLocation;
    String[] answers = new String[totalAnswer];
    Button button4,button1,button2,button3;
    TaskDownloader task;
    Random random;
    String message;
    Bitmap countryImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        baseUrl = "https://www.countries-ofthe-world.com/";
        random = new Random();
        task = new TaskDownloader();
        imageView = (ImageView) findViewById(R.id.imageView);
        button4 = (Button) findViewById(R.id.button4);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        downloadCountries();
        assignAnswers();
    }

    public void chooseCountry(View view){
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            message = "Correct!";
        } else {
            message = "Incorrect. The answer is " + countryNames.get(chosenCountry);
        }
        Toast.makeText(getApplicationContext(),message, Toast.LENGTH_SHORT).show();
        assignAnswers();
    }

    public void assignAnswers(){
        ImageDownloader imageDownloader = new ImageDownloader();
        chosenCountry = random.nextInt(countryUrls.size());
        try {
            countryImg = imageDownloader.execute(countryUrls.get(chosenCountry)).get();
            imageView.setImageBitmap(countryImg);
        } catch (Exception e) {
            e.printStackTrace();
        }

        locationOfCorrectAnswer = random.nextInt(totalAnswer);
        for (int i=0;i<totalAnswer;i++){
            if (i == locationOfCorrectAnswer) answers[i] = countryNames.get(chosenCountry);
            else {
                incorrectAnswerLocation = random.nextInt(countryUrls.size());
                while (incorrectAnswerLocation == chosenCountry){
                    incorrectAnswerLocation = random.nextInt(countryUrls.size());
                }
                answers[i] = countryNames.get(incorrectAnswerLocation);
            }
        }
        button1.setText(answers[0]);
        button2.setText(answers[1]);
        button3.setText(answers[2]);
        button4.setText(answers[3]);
    }


    public void downloadCountries(){
        String result;
        String[] resultSplit;
        try {
            result = task.execute("https://www.countries-ofthe-world.com/flags-of-the-world.html").get();
            resultSplit = result.split("<div id=\"leftcolumn\">");
            Pattern p = Pattern.compile("<img src=\"(.*?)\"");
            Matcher m = p.matcher(resultSplit[0]);
            while (m.find()){
                countryUrls.add(baseUrl + m.group(1));
            }
            p = Pattern.compile("alt=\"Flag of (.*?)\"");
            m = p.matcher(resultSplit[0]);
            while (m.find()){
                countryNames.add(m.group(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class ImageDownloader extends AsyncTask<String,Void,Bitmap>{
        @Override
        protected Bitmap doInBackground(String... urls) {

            Bitmap bitmap;
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();
                bitmap = BitmapFactory.decodeStream(in);
                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public class TaskDownloader extends AsyncTask<String,Integer,String>{
        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();
                while (data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
