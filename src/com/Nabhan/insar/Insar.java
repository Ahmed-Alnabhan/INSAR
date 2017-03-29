package com.Nabhan.insar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class Insar extends Activity implements SensorListener {
      // ----------- Declarations --------------//
	  // CAMERA staff//
	  SurfaceView surfaceView;
	  SurfaceHolder surfaceHolder;
	  Camera cam;
	  boolean isInPreview = false;
	  
	  Context context;  
	  String myQuery;
	  ArrayList<String> roomsList;
	  WifiManager wifiMan;
	  private WifiBCReceiver wifiBCReceiver;  
	  TextView mainText;
	  Button btnScnner;
	  IntentFilter intFil;// = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
	  List<ScanResult> wifiAPsList;
	  StringBuilder stringBldr = new StringBuilder();
	  WifiTimerTask myTimerTask;
	  Timer timer;
	  boolean isIntentReged = false;
	  
	  ArrayAdapter<String> roomsAdapter;
	  Spinner spnrRoomsList;
	  String roomSelected;
	  // First part of query
	  String queryPart1;
	  // Second Part of query
	  String queryPart2;
	  // Third part of query
	  String queryPart3;
	  // Flag of the first record in the Cursor
	  boolean isFirst = true;
	  // The range of measured signal is + or - 4
	  //private static final int SIGNAL_RANGE = 3;
	  // Maximum number of matches with the rssis in DB
	  int maxMatches = 0;
	  // Returned Fingerprint
	  String myFingerPrint = "aaa";
	  //Cursor cursor;
	  String result;
	  TextView chooseDestination;
	  TextView selectedDest;
	  TextView aps;
	  TextView building;
	  TextView floor;
	  TextView currLocation;
	  TextView currentLoc;
	  TextView currBuilding;
	  TextView currFloor;
	  Button roomsMenu;
	  Intent myIntent;
	  Handler handler;
	  ArrowDirection ad;
	  SensorManager sensorManager;
	  static final int sensor = SensorManager.SENSOR_ORIENTATION;
	  String[] roomsArray;
	  // read arrow heading 
	  TextView heading;
	  float directionDeg = 0;
	  //user Enters angle in degrees
	  EditText angleDegrees;
	  int angle;
	
	  /* (non-Javadoc)
		 * @see android.app.Activity#onStart()
		 */
		@Override
		protected void onStart() {
			super.onStart();
		}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// ---- Preventing Screen from going off
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		// **********Setting up the camera***************//
		surfaceView = (SurfaceView) findViewById(R.id.camPreview);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(myCallback);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		ad = new ArrowDirection(this);
		ad = (ArrowDirection)findViewById(R.id.arrowDrawing);
		ad.setVisibility(View.GONE);
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		intFil = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		DBManipulator rooms = new DBManipulator(this);
		roomsList = new ArrayList<String>();
		roomsList = rooms.getRooms();
		// Setting the first and the third parts of the query
		queryPart1 = "SELECT fp_id, COUNT(*) FROM rssi WHERE ";
		queryPart3 = "GROUP BY fp_id;";
	    // Setting the timer
		timer = new Timer();
		aps = (TextView) findViewById(R.id.aps);
		mainText = (TextView) findViewById(R.id.mainText);
		building = (TextView) findViewById(R.id.buildingNo);
		floor = (TextView) findViewById(R.id.floorNo);		
		currLocation = (TextView) findViewById(R.id.loc);
		chooseDestination = (TextView) findViewById(R.id.chooseDest);
		selectedDest = (TextView) findViewById(R.id.theDestination);
		currentLoc = (TextView) findViewById(R.id.currentLocation);
		currBuilding = (TextView) findViewById(R.id.building);
		currFloor = (TextView) findViewById(R.id.floor);
		heading = (TextView) findViewById(R.id.bearing);

	    // the edtitText of entered angles	 	
		angleDegrees = (EditText) findViewById(R.id.degrees);
		
		// ---- Hide TextViews when the app runs ----//
		chooseDestination.setVisibility(View.GONE);
		currFloor.setVisibility(View.GONE);
		currBuilding.setVisibility(View.GONE);
		currentLoc.setVisibility(View.GONE);
		btnScnner = (Button) findViewById(R.id.btnGo);
		// ---- Disable the start navigation button when the app runs ----//
		btnScnner.setEnabled(false);
		btnScnner.setOnClickListener(new View.OnClickListener() {
	
		public void onClick(View v) {
			if(wifiMan.isWifiEnabled() == false){
			    wifiMan.setWifiEnabled(true);
				
			}
			// Start scanning for wifi networks when click GO!!! button
			//wifiMan.startScan();
			myTimerTask = new WifiTimerTask();
			timer.schedule(myTimerTask, 1000, 3000);
			try {
				wifiBCReceiver.myAlarm(getApplicationContext());
				//wifiMan.startScan();
				mainText.setText("\nStarting Scan...\n");
				currentLoc.setVisibility(View.VISIBLE);
			    currBuilding.setVisibility(View.VISIBLE);
			    currFloor.setVisibility(View.VISIBLE);
                //ad.setVisibility(View.VISIBLE);
			    angle = Integer.parseInt(angleDegrees.getText().toString());
				//registerReceiver(wifiBCReceiver, intFil);
				isIntentReged = true;
				//mainText.setText("Starting Scan");
			} catch (Exception e) {
				Log.i("e","Exception");
				e.printStackTrace();
			}
		}
		});
		
		// ---- Creating a dropdown list of rooms (destinations)----//
		// Converting arrayList to an array
		roomsArray = new String[roomsList.size()];
		roomsArray = roomsList.toArray(roomsArray);
		roomsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, roomsList);
		roomsMenu = (Button) findViewById(R.id.btnMenu);
		roomsMenu.setOnClickListener(new View.OnClickListener() {
		// This code was taken from http://stackoverflow.com/questions/867518/how-to-make-an-android-spinner-with-initial-text-select-one
			public void onClick(View w) {
			  new AlertDialog.Builder(Insar.this)
			  .setTitle("Choose a destination...")
			  .setAdapter(roomsAdapter, new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog, int which) {
                    roomSelected = roomsArray[which];
                    // ---- Show the destination label when the user chooses a destination from the list
                    chooseDestination.setVisibility(View.VISIBLE);
                    btnScnner.setEnabled(true);
                    selectedDest.setText(roomSelected);
                    //Toast.makeText(getBaseContext(), "You selected: " + roomSelected, Toast.LENGTH_SHORT).show();

			      dialog.dismiss();
			    }
			  }).create().show();
			}
		});
		//spnrRoomsList = new ArrayList<String>();
		/*spnrRoomsList = (Spinner) findViewById(R.id.spnrRooms);
		
		roomsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,roomsList);
		//spnrRoomsList.setPromptId(R.string.spnr_default);
		
		spnrRoomsList.setAdapter(roomsAdapter);
		roomsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// TEST - Print out the selected item
		spnrRoomsList.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> adptrView, View view,
					int location, long arg3) {
					roomSelected = adptrView.getItemAtPosition(location).toString();
					Toast.makeText(getBaseContext(), "You selected: " + roomSelected, Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});*/
		
		
		aps.setMovementMethod(new ScrollingMovementMethod());
		//mainText.setMovementMethod(new ScrollingMovementMethod());
		wifiMan = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiBCReceiver = new WifiBCReceiver();		
		myTimerTask = new WifiTimerTask();
		registerReceiver(wifiBCReceiver, intFil);
		//isIntentReged = true;
		//if(wifiMan.isWifiEnabled() == false){
	    //wifiMan.setWifiEnabled(true);
		//wifiMan.startScan();
		//mainText.setText("\nStarting Scan...\n");
	//}
		   
} // onCreate
	public void onSensorChanged(int sensor, float[] values) {
	    if (sensor != Insar.sensor){
	      return;
	    }
	    float direction = (float) values[0];
	    heading.setText("Heading: " + Float.toString(direction));
	    ad.findOreintation(directionDeg - direction);
	  }
	SurfaceHolder.Callback myCallback = new SurfaceHolder.Callback() {
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				cam.setPreviewDisplay(surfaceHolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			Camera.Parameters params = cam.getParameters();
			Camera.Size camSize = previewSize(params, width, height);
			if (camSize!=null) {
				params.setPreviewSize(camSize.width, camSize.height);
				cam.setParameters(params);
				cam.startPreview();
				isInPreview=true;
				}
		}

		private Size previewSize(Parameters params, int width,
				int height) {
			Camera.Size preview = null;
			for (Camera.Size cameraSize : params.getSupportedPreviewSizes()){
				if (cameraSize.width<=width && cameraSize.height<=height) {
					if (preview==null) {
						preview=cameraSize;
					}
					else {
					int previewFrame= (preview.width*preview.height);
					int newFrame= (cameraSize.width*cameraSize.height);
					if (previewFrame < newFrame) {
						preview=cameraSize;
					}
					}
					}
			}
			return preview;
		}
	};
	/* (non-Javadoc)
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		//wifiMan.startScan();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}


	@Override
protected void onPause() {
	if (isInPreview == true){   
	   cam.stopPreview();
	}
	cam.release();
	cam = null;
	isInPreview = false;
    if(isIntentReged == true) {
	    unregisterReceiver(wifiBCReceiver);
	    isIntentReged = false;
    }
    sensorManager.unregisterListener(this, sensor);
    super.onPause();
  }
	@Override
  protected void onResume() {
	super.onResume();
	cam = Camera.open();
	if(isIntentReged == false){
	  registerReceiver(wifiBCReceiver, intFil);
	  isIntentReged = true;
    }
	sensorManager.registerListener(this, sensor);
  }
  
 
 /* protected void onStop() {
	  unregisterReceiver(wifiBCReceiver);		    
	  super.onStop();
  }
*/
  class WifiBCReceiver extends BroadcastReceiver {
	    public void onReceive(Context c, Intent intent) {
	    //isFirst = true;
	    stringBldr = new StringBuilder();
	    //wifiMan.startScan();
	    wifiAPsList = wifiMan.getScanResults();
	    stringBldr.append("APs: " + wifiAPsList.size() + "\n\n");
		  /*if(wifiAPsList.size() > 0){  
	        for(ScanResult scanResult : wifiAPsList){
	            // Getting the MAC address
	        	String macAddress = scanResult.BSSID;
	        	// Getting the signal 
	            int rssi = scanResult.level;
	            int maxRSSI = rssi + SIGNAL_RANGE;
	            int minRSSI = rssi - SIGNAL_RANGE;
	            if (isFirst == true){
	            	//queryPart2 = "(mac = '" + macAddress + "' and (rssiSignal > '" + minRSSI + "' and rssiSignal < '" + maxRSSI + "'))";
	            	queryPart2 = " (mac = '" + macAddress + "' and ((rssiSignal - " + rssi + ") * (rssiSignal - " + rssi + ")<=4 )) ";
	            	isFirst = false;
	            } else {
	            	queryPart2 += " OR (mac = '" + macAddress + "' and ((rssiSignal - " + rssi + ") * (rssiSignal - " + rssi + ")<=4 )) ";
	            	//queryPart2 += " OR (mac = '" + macAddress + "' and (rssiSignal > '" + minRSSI + "' and rssiSignal < '" + maxRSSI + "'))";
	            }
	            
	            stringBldr.append("MAC: " + scanResult.BSSID + "\n");
		        stringBldr.append("SSID: " + scanResult.SSID + "\n");
		        stringBldr.append("RSSI: " + scanResult.level + " dB\n");
		        stringBldr.append("\n");
        
		  } // end of loop of ScanResult
	        myQuery = queryPart1 + queryPart2 + queryPart3;
	        FindCurrentLocation fcl = new FindCurrentLocation(c);
	       
	        result = fcl.findCurrentLocation(myQuery);
	         
		 
			  } // End of if ScanResult list is not empty
*/		 
	     DBManipulator fcl = new DBManipulator(c);
	       
         result = fcl.findCurrentLocation(wifiAPsList);
        
	     aps.setText(stringBldr); 
	     mainText.setText(result);
	     ad.setVisibility(View.VISIBLE);	     
	     DBManipulator inf = new DBManipulator(c);
	     if (result == ""){
	    	 currLocation.setText("Calculating....");
	     } else{
	    	 ArrayList<String> info = inf.currentLocationInfo(result);
		     building.setText(info.get(0).toString());
		     floor.setText(info.get(1).toString());
		     currLocation.setText(info.get(2).toString());
	     }
	     
	     // ---- get the orientation of the arrow from database ----//
	     DBManipulator OrientationInDegree = new DBManipulator(c);
	     if (result != ""){
	        directionDeg = OrientationInDegree.getDirectionInDegree(result, roomSelected);
	     }
	     
	     DBManipulator destination = new DBManipulator(c);
	     String destRoom = destination.getRoomNumber(roomSelected);
	     if (result.equals(destRoom)){
	    	 Toast.makeText(c, "You are front of " + roomSelected,Toast.LENGTH_LONG).show();
	 		    // Vibrate the phone
	 		    Vibrator vibrator = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
	 		    vibrator.vibrate(2000);
	     }
	    } // End of onReceive()
	    
	    public void myAlarm(Context context){
	    	Intent myIntent = new Intent(context,WifiBCReceiver.class);
	    	//Calendar calendar = Calendar.getInstance();
			PendingIntent myPIntent = PendingIntent.getBroadcast(context, 0, myIntent, 0);			
			AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),3000, myPIntent);
	    }
	  } // End of WifiBCReceiver


@Override
public void onAccuracyChanged(int arg0, int arg1) {
	// TODO Auto-generated method stub
	
}



class WifiTimerTask extends TimerTask {

	@Override
	public void run() {
		 //wifiMan.setWifiEnabled(true);
		 wifiMan.startScan();
	} // End of run()
	
} // End of WifiTimerTask



}
