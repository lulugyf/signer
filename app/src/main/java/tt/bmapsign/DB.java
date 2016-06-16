package tt.bmapsign;


import java.util.Enumeration;
import java.util.Properties;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DB extends SQLiteOpenHelper{
	static final String TAG = "DB";
	static final String DB_NAME = "mycfg.db";
	static final int DB_VERSION = 1;
	
	//private Context context;
	private SQLiteDatabase db;

	// Constructor	
	public DB(Context context){
		super(context, DB_NAME, null, DB_VERSION);
		db = this.getWritableDatabase();
		// db = getReadableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = null;
		
		sql = "create table if not exists cfgparam(catalog text, pname text, pvalue text)";
		db.execSQL(sql);

		sql = "create index if not exists idx1_cfgparam on cfgparam(catalog)";
		db.execSQL(sql);

		sql = "create table if not exists map_pos(" +
				"pid long primary key, " +
				"latitude long, longitude long, office_loc text," +
				"last_acc long)";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { //
		// Typically do ALTER TABLE statements, but...we're just in development,
		// so:
		//db.execSQL("drop table if exists " + TABLE); // drops the old database
		//db.execSQL("drop table if exists cfgparam");
		//Log.d(TAG, "onUpdated");
		onCreate(db); // run onCreate to get new database
	}
	
	public Properties getParams(String catalog){
		Cursor cur = db.query("cfgparam",
				new String[]{"pname", "pvalue"},
				"catalog=?", new String[]{catalog}, null, null, null);
		if(cur.getCount() == 0)
			return null;
		Properties prop = new Properties();
		while(cur.moveToNext()){
			prop.setProperty(cur.getString(0), cur.getString(1));
		}
		cur.close();
		return prop;
	}

	public int setParams(String catalog, Properties prop)
	{
		int rc = 0;
		db.delete("cfgparam", "catalog=?", new String[]{catalog});
		for(Enumeration<Object> enu=prop.keys(); enu.hasMoreElements();){
			String k = (String)enu.nextElement();
			String v = prop.getProperty(k);
			ContentValues cv = new ContentValues();
			cv.put("catalog", catalog);
			cv.put("pname",  k);
			cv.put("pvalue", v);
			if(db.insert("cfgparam", null, cv) != -1L)
				rc ++;
		}
		return rc;
	}
	public boolean setParam(String pname, String pvalue){
		ContentValues cv = new ContentValues();
		cv.put("pvalue", pvalue);
		boolean r;
		int ret = db.update("cfgparam", cv, "catalog=? and pname=?",
				new String[]{"main", pname});
		if(ret == 0){
			cv = new ContentValues();
			cv.put("catalog", "main");
			cv.put("pname",  pname);
			cv.put("pvalue", pvalue);
			r = db.insert("cfgparam", null, cv) != -1L;
		}else{
			r = true;
		}

		if(db.inTransaction())
			db.endTransaction();
		return r;
	}
	public String getParam(String pname){
		Cursor cur = db.query("cfgparam",
				new String[]{"pvalue"},
				"catalog=? and pname=?", new String[]{"main", pname}, null, null, null);
		String ret = null;
		if(cur.moveToNext()){
			ret = cur.getString(0);
		}
		cur.close();
		if(db.inTransaction())
			db.endTransaction();

		return ret;
	}
}
