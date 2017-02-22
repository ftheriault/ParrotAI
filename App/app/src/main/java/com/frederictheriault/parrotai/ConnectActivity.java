package com.frederictheriault.parrotai;

import android.os.Bundle;

import com.parrot.arsdk.ARSDK;

public class ConnectActivity extends CommonActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ARSDK.loadSDKLibs();
    }

}

