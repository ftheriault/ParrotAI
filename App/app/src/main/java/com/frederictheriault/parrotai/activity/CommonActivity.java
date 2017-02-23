package com.frederictheriault.parrotai.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class CommonActivity extends AppCompatActivity {
    public static int activitiesNum = 0;

    @Override
    protected void onStop() {
        super.onStop();
        activitiesNum--;

        if(activitiesNum == 0){
            // user is not in application anymore
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        activitiesNum++;
    }

}
