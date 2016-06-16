package tt.bmapsign;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.TimeZone;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public class DoSign extends AsyncTask<String, String, String>{
	private static final String tag = "DoSign";
	
	private Context context;
	private Callback callback = null;
	//private String sendFlag; // "1":signOn  "2":signOff 
	private static final String urlsign = "http://im.on-con.com:9095/dataInterface/httpServer?action=signInOutInfoUpload";
	
	String corpID = "10000";
	//String notesID = null; //"guanyf";
	String ip = "255.255.255.255";
	//String imei = null;//"860404000864390";
	//String imsi = null; //"460003402641975";
	static String key = "3an7bgm3yjb554fp";
	
	//String longitude = "104.096952";
	//String latitude = "30.680610";
	
	public DoSign(Context context)
	{
		this.context = context;
	}
	
	public DoSign setCallback(Callback cl){
		this.callback = cl;
		return this;
	}
	
	private boolean checknet(){
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE); //"connectivity"
		NetworkInfo networkinfo =  cm.getActiveNetworkInfo(); //cm.getNetworkInfo(0);
		if(networkinfo != null){
			android.net.NetworkInfo.State state = networkinfo.getState();
			if (android.net.NetworkInfo.State.CONNECTED == state) {
				return true;
			}
		}
		/*
		networkinfo = cm.getNetworkInfo(1);
		if(networkinfo != null){
			android.net.NetworkInfo.State state = networkinfo.getState();
			if (android.net.NetworkInfo.State.CONNECTED == state)
				return true;
		} */
		return false;
	}
	
	
	private String genBody(String notesID, String imei, String signFlag,
			String latitude, String longitude)
	{	
		JSONObject jsonobject = new JSONObject();
		try
		{			
			jsonobject.put("corpID", corpID);//a1.b());
			jsonobject.put("notesID", notesID); //a1.g());
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
			String timestr = String.format("%d", calendar.getTimeInMillis());
			jsonobject.put("time", timestr);//a1.i());
			jsonobject.put("signFlag", signFlag);// "1": signOn  "2": signOff

			jsonobject.put("longitude", longitude);
			jsonobject.put("latitude", latitude);
			jsonobject.put("IP", URLEncoder.encode(ip, "UTF-8"));
			jsonobject.put("IMEI", imei);
			
			//String key = pref.getString("key", "3an7bgm3yjb554fp");
			String str = timestr + corpID + notesID;
			jsonobject.put("appmac", encmac(str));
		}
		catch (Exception ex){
			ex.printStackTrace();
			return "ERROR: json encode failed!";
		}

		Log.d(tag, jsonobject.toString());
		Log.d(tag, urlsign);
		return "requestJson=" + encmac(jsonobject.toString());
	}
	
	private String signFlag = null;
	private String notesID = null;
	private String imei = null;
	private String latitude = null;
	private String longitude = null;	
	@Override
	// arg = {notedID, imei, signFlag, latitude, longitude} 
	protected String doInBackground(String... arg) {
		if(arg.length > 4){
			notesID  = arg[0];
			imei = arg[1];
			signFlag = arg[2];
			latitude   = arg[3];
			longitude  = arg[4];
		}else{
			return "Not set sign type!";
		}

		
		if (!checknet())
			return "network not connected!";
		
		String body = genBody(notesID, imei, signFlag, latitude, longitude);
		if(body.startsWith("ERROR:"))
			return body;

		HttpPost httppost = new HttpPost(urlsign);
		httppost.setHeader("Content-Type", "application/x-www-form-urlencoded");
		DefaultHttpClient defaulthttpclient;

		try
		{
			httppost.setEntity(new StringEntity(body, "utf-8"));
		}
		catch (UnsupportedEncodingException ex)
		{
			Log.e("sign", ex.getMessage(), ex);
			return ex.toString();
		}
		defaulthttpclient = new DefaultHttpClient();

		try
		{
			HttpResponse ret = defaulthttpclient.execute(httppost);
			StatusLine sl = ret.getStatusLine();
			if (sl.getStatusCode() == 200){
				return EntityUtils.toString(ret.getEntity());
			}else{
				Log.e("NetError", EntityUtils.toString(ret.getEntity()));
				return String.format("HTTP Err: %d, %s", 
						sl.getStatusCode(), sl.getReasonPhrase());
			}
		}
		catch (Exception ex)
		{
			Log.e("sign", ex.getMessage(), ex);
			return ex.getMessage();
		}
	}
	
	private String getProp(String s, String pname){
		JSONObject json = null;

		try{	
			json = new JSONObject(s);
			if (json.isNull("code"))
				return null;

			String code = json.getString("code");
			if(!"0".equals(code))
				return null;
			return json.getString(pname);
		}catch(Exception e){
			return null;
		}
	}
	
	@Override
	protected void onPostExecute(String s)
	{

		if(callback != null){
			callback.finishCall(s, notesID, signFlag, latitude, longitude);
			return;
		}

//		String s1 = null;
//		String s2 = null;
//		JSONObject json = null;
//		try{
//			if (!s.startsWith("{"))
//				throw new Exception("Error Respond");
//
//			json = new JSONObject(s);
//			if (json.isNull("code"))
//				throw new Exception("Json Broken");
//
//			String corpID = json.getString("corpID");
//			String notesID = json.getString("notesID");
//			String signTime = json.getString("signTime");
//			String code = json.getString("code");
//			String officeLoc = json.getString("officeLoc");
//			String isLocValid = json.getString("isLocValid");
//			//String planSignTime = json.getString("planSignTime");
//
//			if (!code.equals("0"))
//				s1 = "Bad Location Or Signout without SignIn";
//			else
//				s1 = "OK";
//
//			if (s1.equals("OK"))
//			{
//				s2 = String.format("Success:%s @%s @%s @%s %s",
//						corpID,
//						notesID,
//						isLocValid,
//						signTime,
//						officeLoc
//						);
//			} else
//			{
//				s2 = String.format("Fail:%s", s1);
//			}
//		}catch(Exception ex){
//			s2 = String.format("Fail:%s", ex.toString());
//		}
//
//		NotificationManager notificationmanager;
//		Notification notification;
//		PendingIntent pendingintent;
//		notificationmanager =
//				(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE); //"notification"
//		notification = new Notification(R.drawable.ic_launcher,
//				"SignResult:", System.currentTimeMillis());
//		pendingintent = PendingIntent.getActivity(
//				context, 0, new Intent(context, MainActivity.class), 0);
//		notification.setLatestEventInfo(context, "SignResult:", s2, pendingintent);
//		notificationmanager.notify(R.string.app_name, notification);
		
	}
	
	
	public static String encmac(String str)
	{
		byte abyte0[] = null;
		try{
			SecretKeySpec secretkeyspec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
			Cipher cipher = Cipher.getInstance("AES");
			byte abyte2[] = str.getBytes("UTF-8");
			cipher.init(1, secretkeyspec);
			abyte0 = cipher.doFinal(abyte2);
		}catch(Exception ex){
			ex.printStackTrace();
		}

		String s3 = null;
		StringBuffer stringbuffer = new StringBuffer(2 * abyte0.length);
		int i = 0;
		do
		{
			if (i >= abyte0.length){
				s3 = stringbuffer.toString();
				break;
			}
			String s = Integer.toHexString(0xff & abyte0[i]);
			if (s.length() == 1)
				s = (new StringBuilder(String.valueOf('0'))).append(s).toString();
			stringbuffer.append(s.toUpperCase());
			i++;
		} while (true);

		return s3;
	}
	
}


/*

http://im.on-con.com:9095/dataInterface/httpServer?action=signInOutInfoUpload

{
"time":"1363239233652",
"signFlag":"1",
"notesID":"libinsc",
"IMEI":"860404000864390",
"appmac":"6974A8CE51CD1DE86D450D6587AAE935878D74DC1A856B33EAF69A1F0029E57B3A5D2E7675B3F17088773E19A3C785BB86122B540CA828A1754549B25F7C4CEC9D6DC06C9C1B238C084A52ADCFF61A06B2F31A0D9B6706A53A2B4A8BAD9BB389",
"IP":"255.255.255.255",
"longitude":"104.096945",
"latitude":"30.680602",
"corpID":"10000"
}


{"time":"1363250605755",
"signFlag":"1",
"notesID":"guanyf",
"IMEI":"860404000864390",
"appmac":"26E4E9901AB2BD579DDB534175E5C175CCCE4290843D3D95AB3612B27E834843",
"IP":"255.255.255.255",
"longitude":"104.096951",
"latitude":"30.680604",
"corpID":"10000"}

*/

