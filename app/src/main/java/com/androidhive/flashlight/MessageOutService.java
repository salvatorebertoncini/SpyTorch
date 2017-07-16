package com.androidhive.flashlight;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by lucadalseno on 26/06/17.
 */

public class MessageOutService extends Service{


    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }




}
