package com.androidhive.flashlight;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lucadalseno on 02/10/17.
 */

public class GetInfo {

    private int status = 0;
    private String ssid;
    private TelephonyManager telephonyManager;
    private Intent batteryStatus;
    private ConnectivityManager connectivityManager;
    private final WifiManager wifiManager;
    private NetworkInfo networkInfo;
    private AccountManager accountManager;
    private int chargePlug;
    private int scale;
    private int ip;


    public GetInfo(Context mContext) {

        //To get access of all the information about the telephony services
        telephonyManager = (TelephonyManager)mContext.getSystemService(mContext.TELEPHONY_SERVICE);

        //Battery Info
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = mContext.registerReceiver(null, ifilter);

        //Class that answers queries about the state of network connectivity
        connectivityManager = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        //Returns connection status information about a particular Network (WIFI in this case)
        networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        //This class provides the primary API for managing all aspects of Wi-Fi connectivity.
        wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        //This class provides access to a centralized registry of the user's online accounts
        accountManager = AccountManager.get(mContext);

        chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    }

    //Returns the phone number
    public String getPhoneNumber(){
        return telephonyManager.getLine1Number();
    }
    //Returns the IMEI of the phone
    public String getImei(){
        return telephonyManager.getDeviceId();
    }
    //Returns the Operator name
    public String getNetworkOperatorName(){
        return telephonyManager.getNetworkOperatorName();
    }
    //Country of the Sim
    public String getSimCountryIso(){
        return telephonyManager.getSimCountryIso();
    }
    //Returns the MCC+MNC (mobile country code + mobile network code) of the provider of the SIM.
    public String getSimOperator(){
        return telephonyManager.getSimOperator();
    }
    //Returns the name of the connected WIFI
    public String getSsid(){
		if (networkInfo.isConnected()) {
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty((connectionInfo.getSSID()))) {
                 ssid = connectionInfo.getSSID();
            }
        }
        return ssid;
    }
    //Returns the IP address
    public int getIP(){
        if (networkInfo.isConnected()) {
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty((connectionInfo.getSSID()))) {
                ip =   connectionInfo.getIpAddress();
            }
        }
        return ip;
    }
    //battery status
    public int getStatus(){
       return  batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
    }
    // is charging??
    public boolean getIsCharging(){
       return  status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
    }
    // Is USB charging?
    public boolean isUsbCharge(){
        return chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
    }
    //Is AC charging?
    public boolean isAcCharge(){
        return chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
    }
    //Return the level of the battery
    public int getLevel(){
        return batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    }
    //Return the % of battery
    public float getBatteryPct(){
        return  getLevel() / (float)scale;
    }
    //Returns the username of the phone's owner
    public String getUsername(){
        Account[] accounts = accountManager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();

        for (Account account : accounts) {
            // account.name as an email address only for certain account.type values.
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");
            if (parts.length > 1)
                return parts[0];
        }
        return "Sconosciuto";

    }

    //Some more general info about the device
    public String getSDK(){
        return Build.VERSION.SDK;
    }

    public String getDevice(){
        return Build.DEVICE;
    }

    public String getManufacturer(){
        return Build.MANUFACTURER;
    }

    public String getModel(){
        return Build.MODEL;
    }
    public String getProduct(){
        return Build.PRODUCT;
    }
    public String getBuildUser(){
        return Build.USER;
    }

    public String getBrand(){
        return Build.BRAND;
    }

    public String getFingerprint(){
        return Build.FINGERPRINT;
    }

    public String getHardware(){
        return Build.HARDWARE;
    }
}
