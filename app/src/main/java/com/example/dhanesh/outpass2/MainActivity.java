package com.example.dhanesh.outpass2;

import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
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
import java.util.List;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private ConstraintLayout logLayout;
    private Button loginButton;
    private EditText COERIDEditText;
    OneTimeWorkRequest request;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    public static final String myPref = "myPref";
    private String fullname,mobileno,roomno;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logLayout = findViewById(R.id.logLayout);
        loginButton = findViewById(R.id.buttonLogin);
        COERIDEditText = findViewById(R.id.editTextCOERID);
        request = new OneTimeWorkRequest.Builder(MyWorker.class).build();


        pref = getApplicationContext().getSharedPreferences(myPref, 0);
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait While Loading...");
        progress.setCancelable(false);
        if (pref.contains("coerid")) {
            logLayout.setVisibility(View.GONE);
           fullname= pref.getString("fullname", null);
           roomno= pref.getString("roomno", null);
            mobileno= pref.getString("mobileno", null);
            Log.i("fname:",fullname);
            String coerid= pref.getString("coerid", null);
            Log.i("coerid:",coerid);
           setNavBar();

        }
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String coerid = COERIDEditText.getText().toString();
                if (coerid.length()!=8) {
                    Toast.makeText(MainActivity.this, "Invalid COERID, Please Try Again!", Toast.LENGTH_SHORT).show();
                } else {
                    editor = pref.edit();
                    editor.putString("coerid", coerid);
                    editor.apply();
                    WorkManager.getInstance().enqueue(request);
                }
            }
        });

        if (request != null) {
            WorkManager.getInstance().getWorkInfoByIdLiveData(request.getId())
                    .observe(this, new Observer<WorkInfo>() {
                        @Override
                        public void onChanged(@Nullable WorkInfo workInfo) {
                            if (workInfo != null) {
                                if (workInfo.getState().isFinished()) {
                                  String status = workInfo.getState().name();
                                    if (status.equals("SUCCEEDED")) {
                                       progress.dismiss();
                                       setNavBar();
                                        logLayout.setVisibility(View.INVISIBLE);
                                    } else {
                                        progress.setMessage("Invalid COERID");
                                        progress.dismiss();
                                    }
                                } else {
                                    progress.show();

                                }
                            }
                                          }
                    });
        }else{
            Toast.makeText(this, "null request", Toast.LENGTH_SHORT).show();
        }
        //
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container2, new LoginFragment()).commit();
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setCheckedItem(R.id.nav_outpass);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new OutpassFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_outpass);
        }

    }

    public void setNavBar(){
        String coerid= pref.getString("coerid","");
        NavigationView navigationView = findViewById(R.id.nav_view);
        View hView = navigationView.getHeaderView(0);
        TextView tvName = hView.findViewById(R.id.nav_uname);
        TextView tvDetails = hView.findViewById(R.id.nav_details);
        ImageView imageView = hView.findViewById(R.id.sPic);
        imageView.setImageBitmap(loadImageBitmap(getApplicationContext(),coerid+".jpg"));
        tvName.setText(fullname);
        tvDetails.setText(roomno+" "+mobileno);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nav_outpass:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new OutpassFragment()).commit();
                break;
            case R.id.nav_history:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HistoryFragment()).commit();
                break;
            case R.id.nav_approval:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ApprovalsFragment()).commit();
                break;
            case R.id.nav_feedback:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FeedbackFragment()).commit();
                break;
            case R.id.nav_logout:

                if(pref.edit().clear().commit())
                    Toast.makeText(this, "See You Later!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Not Logout!", Toast.LENGTH_SHORT).show();

                finish();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public Bitmap loadImageBitmap(Context context, String imageName) {
        Bitmap bitmap = null;
        FileInputStream fiStream;
        try {
            fiStream = context.openFileInput(imageName);
            bitmap = BitmapFactory.decodeStream(fiStream);
            fiStream.close();
        } catch (Exception e) {
            Log.d("saveImage", "Exception 3, Something went wrong!");
            e.printStackTrace();
        }
        return bitmap;
    }
}
