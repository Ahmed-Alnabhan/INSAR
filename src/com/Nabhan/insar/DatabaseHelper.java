package com.Nabhan.insar;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {
    // database name
	public static final String DB_NAME = "insar.db";
	// database version name
	public static final int DB_VERSION = 14;
	private Context context;
	public DatabaseHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTable(R.raw.fingerprints, db);
		createTable(R.raw.rooms, db);
		createTable(R.raw.directions, db);
		createTable(R.raw.rssi, db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS rssi");
		db.execSQL("DROP TABLE IF EXISTS directions");
		db.execSQL("DROP TABLE IF EXISTS rooms");
		db.execSQL("DROP TABLE IF EXISTS fingerprints");
		onCreate(db);
		
	}
	
	// this method creates a table in the database
	private void createTable(int tableId, SQLiteDatabase db){
		Log.i("DBHelper","Executing SQL statements");
		try {
			InputStream is = context.getResources().openRawResource(tableId);
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(is, null);
			NodeList statements = doc.getElementsByTagName("query");
			for(int i = 0; i<statements.getLength();i++){
				String st = statements.item(i).getChildNodes().item(0).getNodeValue();
				db.execSQL(st);
			}
		} catch (Throwable t){
			Toast.makeText(context, t.toString(), Toast.LENGTH_LONG).show(); 
			Log.e("DbHelper", t.getMessage());
		}
	}

}
