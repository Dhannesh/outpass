package com.example.dhanesh.outpass2;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class MyWorker extends Worker {

    private String coerid;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public MyWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        pref = getApplicationContext().getSharedPreferences(MainActivity.myPref, 0);
    }

    private boolean doLogin(String coerid) {
        String login_url = "http://coerians.in/outpass/apis/getDetails.php";
        Log.i("codings",coerid+"");
        String result = "";
        try {
            URL url = new URL(login_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            Log.i("Worker","doLogin");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("coerid", "UTF-8") + "=" + URLEncoder.encode(coerid, "UTF-8");
            bufferedWriter.write(post_data);
            bufferedWriter.flush();
            bufferedWriter.close();
            outputStream.close();
            int status = httpURLConnection.getResponseCode();
            if (status == 404) {
                Log.i("Worker","404");
                return false;
            }
            Log.i("Worker","reading");
            InputStream inputStream = httpURLConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));

            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            bufferedReader.close();
            Log.i("Worker",result);
            inputStream.close();
            httpURLConnection.disconnect();
            //result = result.substring(7);
            JSONObject obj = new JSONObject(result);
            JSONArray arr = obj.getJSONArray("output");

            String fullname = arr.getJSONObject(0).getString("fullname");
            String roomno = arr.getJSONObject(0).getString("roomno");
            String mobileno = arr.getJSONObject(0).getString("mobileno");
            Log.i("Worker",mobileno);
            editor = pref.edit();
            editor.putString("fullname", fullname);
            editor.putString("roomno", roomno);
            editor.putString("mobileno", mobileno);
            editor.putString("coerid", coerid);
            editor.apply();
// loadImageBitmap(getApplicationContext(),coerid+".jpg");
            String weburl = "http://coerians.in/outpass/images/"+coerid + ".jpg";
            String imgName = coerid + ".jpg";
            //sUrl = weburl + coerid + ".jpg";
            Bitmap bitmap = null;
            Log.i("Img Start","from here");
            inputStream = new URL(weburl).openStream();   // Download Image from URL
            bitmap = BitmapFactory.decodeStream(inputStream);       // Decode Bitmap
            inputStream.close();
            saveImage(getApplicationContext(), bitmap, imgName);
            Log.i("dd1", weburl);
            return true;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    @NonNull
    @Override
    public Result doWork() {
        coerid=pref.getString("coerid","");
        if(doLogin(coerid)){
            return Result.SUCCESS;
        }
        return Result.FAILURE;

    }
    private void saveImage(Context context, Bitmap b, String imageName) {
        FileOutputStream foStream;
        try {
            foStream = context.openFileOutput(imageName, Context.MODE_PRIVATE);
            b.compress(Bitmap.CompressFormat.PNG, 100, foStream);
            foStream.close();
        } catch (Exception e) {
            Log.d("saveImage", "Exception 2, Something went wrong!");
            e.printStackTrace();
        }
    }
}
