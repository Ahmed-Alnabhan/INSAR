package com.Nabhan.insar;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.util.Log;

public class DBManipulator {
	SQLiteDatabase db;
    Cursor cursor;
    String query;
    int maxMatches = 0;
    String myFingerPrint = "";
    //List<ScanResult> myList;
    
    public DBManipulator(Context c){
    	db = (new DatabaseHelper(c).getWritableDatabase());
    	//this.myList = list;
    	//this.query = query1;
    }
    public String findCurrentLocation(List<ScanResult> myList){
        db.execSQL("CREATE TEMPORARY TABLE wifiScanResult (macAddress text,signal integer)");
        if(myList.size() > 0){  
	        for(ScanResult scanResult : myList){
	            // Getting the MAC address
	        	String mac = scanResult.BSSID;
	        	// Getting the signal 
	            int rssi = scanResult.level;
	            db.execSQL("INSERT INTO wifiScanResult (macAddress,signal) VALUES ('" + mac + "'," + rssi + ")");    
	        }
        }
		
		try {
			if (db != null){
			    cursor = db.rawQuery("select mytable.fp, mytable.average, mytable.counter from (select rssi.fp_id as fp, avg((rssi.rssiSignal - wifiScanResult.signal) * (rssi.rssiSignal - wifiScanResult.signal)) as average, count(*) As counter FROM rssi JOIN wifiScanResult ON rssi.mac = wifiScanResult.macAddress GROUP BY fp_id ) as mytable where counter > 8 group by fp ORDER BY 2",null);
				//cursor = db.rawQuery("SELECT fp_id, COUNT(*) FROM rssi WHERE (mac = '20:aa:4b:d3:db:d6' and (rssiSignal > -72 and rssiSignal < -64)) OR (mac = '08:86:3b:f1:da:18' and (rssiSignal > -63 and rssiSignal < -55)) group by fp_id",null);
				//cursor = db.rawQuery("select rssi.fp_id, count(*) As counter FROM rssi JOIN wifiScanResult ON rssi.mac = wifiScanResult.macAddress where ((rssi.rssiSignal - wifiScanResult.signal) * (rssi.rssiSignal - wifiScanResult.signal)<=4) group by fp_id ORDER BY 2 DESC LIMIT 1", null);
				//cursor = db.rawQuery("select rssi.fp_id, count(*) As counter FROM rssi JOIN wifiScanResult ON rssi.mac = wifiScanResult.macAddress WHERE ((wifiScanResult.signal < rssi.rssiSignal + 4) AND (wifiScanResult.signal > rssi.rssiSignal - 4)) GROUP BY fp_id ORDER BY 2 DESC LIMIT 1", null);

				//cursor = db.rawQuery(query1,null);  
			}
			int cursorSize = cursor.getCount();
			DatabaseUtils.dumpCursor(cursor);
			  if (cursorSize > 0){
				  cursor.moveToFirst();
				  String fp = cursor.getString(0);
				  myFingerPrint = fp;
				  /*while (cursor.moveToNext()){
					  String fp = cursor.getString(0);
					  int numOfAPs = cursor.getInt(1);
					  
					  if (numOfAPs > maxMatches ){
						  maxMatches = numOfAPs;
						  myFingerPrint = fp;
					  }
				  } // End of while
*/			  } // End of if
			  db.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	  
		  
	
    return myFingerPrint;
}
    
    public ArrayList<String> getRooms(){
 	   Cursor cur;
       cur = db.rawQuery("SELECT room_description FROM rooms;", null);
 	   ArrayList<String> rRooms = new ArrayList<String>();
 	   if(cur.getCount() >= 1){
 		   while(cur.moveToNext()){
 			   rRooms.add(cur.getString(0));
 		   }
 	   }
 	   cur.close();
 	   return rRooms;
    }
    
    
	public ArrayList<String> currentLocationInfo(String rp){
		Cursor cr;
    	ArrayList<String> info = new ArrayList<String>();
    	try {
			String query = "SELECT building, floor, fp_description FROM fingerprints WHERE (_id = " + rp + ");";
			cr = db.rawQuery(query, null);
			int cursorSize = cr.getCount();
			if (cursorSize > 0){
				cr.moveToFirst();
				info.add(cr.getString(0));
				info.add(Integer.toString(cr.getInt(1)));
				info.add(cr.getString(2));
			}
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("MYAPP", "exception", e);
		}
    	return info;
    }
	
	public float getDirectionInDegree(String fp, String dest){
		float degree = 0;
		Cursor cur;
		try {
			String query = "select degree from directions where room_id=(select room_no from rooms where room_description = '" + dest + "') and fp_id = '" + fp + "'";
			cur = db.rawQuery(query, null);
			int cursorSize = cur.getCount();
			if (cursorSize > 0){
				cur.moveToFirst();
				degree = cur.getFloat(0);
			}
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("MYAPP", "exception", e);
		}
		return degree;
	}
	
	public String getRoomNumber(String room){
		String roomNum = "";
		Cursor cur;
		try {
			String query = "select fp_id from rooms where room_description = '" + room + "'";
			cur = db.rawQuery(query, null);
			int cursorSize = cur.getCount();
			if (cursorSize > 0){
				cur.moveToFirst();
				roomNum = cur.getString(0);
			}
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("MYAPP", "exception", e);
		}
		return roomNum;
	}
} // End of the class
