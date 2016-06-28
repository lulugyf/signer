package tt.bmapsign;

/*通过下面的地址获取百度地图的坐标：
* http://api.map.baidu.com/lbsapi/getpoint/index.html?c=
* */

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import tt.bmapsign.util.Loc;
import tt.bmapsign.util.Util;


public class MainActivity extends Activity implements OnClickListener, Callback
{
	private static final String tag = "MainActivity";
	Spinner spin;
	private ArrayAdapter<String> arrAdpt;

	String notesID;
	String imei;
	TextView tvResult;
	TextView tvPos;
	TextView tvNid;
	Button btOn;
	Button btOff;
	
	SharedPreferences pref;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		spin = (Spinner)findViewById(R.id.spinner);

		tvResult = (TextView)findViewById(R.id.textView_result);
		tvPos = (TextView)findViewById(R.id.textViewPos);
		tvPos.setClickable(true);
		//tvPos.setOnClickListener(this);
		tvNid = (TextView)findViewById(R.id.textViewNID);
		tvNid.setClickable(true);
		tvNid.setOnClickListener(this);
		
		btOn = (Button)findViewById(R.id.button_signon);
		btOff = (Button)findViewById(R.id.button_signoff);

		pref = getSharedPreferences("mypref", 0);
		notesID = pref.getString("notesID", null);
		if(notesID != null){
			tvNid.setText("    NotesID: " + notesID);
			imei = pref.getString("imei", null);
			
		}

		setPosList();
	}

	private void setPosText() {
		tvPos.setText(
						"    longitude     : "+current_loc.longitude+"\n" +
						"    latitude   : "+current_loc.latitude+"\n");
	}

	private List<Loc> loc;
	private Loc current_loc;
	private void setPosList() {
		// 用于保存多个位置记录
		spin = (Spinner)findViewById(R.id.spinner);
		ArrayList<String> arr = new ArrayList<String>();

		loc = Util.getLocList();
		for(Loc l: loc){
			arr.add(l.name);
		}

		arrAdpt = new ArrayAdapter<String>(this.getBaseContext(),
				android.R.layout.simple_spinner_item, arr);
		arrAdpt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(arrAdpt);
		spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> view, View arg1,
									   int idx, long arg3) {
				current_loc = loc.get(idx);
				setPosText();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});
		spin.setSelection(pref.getInt("select", 0));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private void buttonEnable(boolean bl){
		btOn.setEnabled(bl);
		btOff.setEnabled(bl);
	}

	private String randNum(String num, Random r) {
		double x = Double.parseDouble(num);
		x += (r.nextInt(40) - 20) * 0.000001;
		String s = String.valueOf(x);
		if(s.length()-s.indexOf('.') > 6){
			s = s.substring(0, s.indexOf('.')+7);
		}
		return s;
	}
	private void sign(String flag) {
		if(notesID == null) {
			Toast.makeText(MainActivity.this, "input notesid first!!", Toast.LENGTH_SHORT).show();
			return;
		}
		// 加入随机数
		Random r = new Random();
		r.setSeed(System.currentTimeMillis());
		new DoSign(this).setCallback(this).execute(
				notesID, imei, flag,
				randNum(current_loc.latitude, r),
				randNum(current_loc.longitude, r));
		buttonEnable(false);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if(id == R.id.button_signon){
			sign("1");
		}else if(id == R.id.button_signoff){
			sign("2");
		}else if(id == R.id.textViewNID){
			inputNotesID((TextView)v);
		}
	}

	private void inputNotesID(final TextView tv){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);                 
		alert.setTitle("Input Box");  
		alert.setMessage("Enter your notesID:");

		 // Set an EditText view to get user input   
		final EditText input = new EditText(this); 
		if(notesID != null)
			input.setText(notesID);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
		    public void onClick(DialogInterface dialog, int whichButton) {  
		        notesID = input.getText().toString();
		        SharedPreferences pref = getSharedPreferences("mypref", 0);
		        Editor editor = pref.edit();
		        editor.putString("notesID", notesID);
		        
				imei = ((TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId(); //"phone"
				//String imsi = ((TelephonyManager)context.getSystemService("phone")).getSubscriberId();
				//String phoneno = ((TelephonyManager)context.getSystemService("phone")).getLine1Number();
				editor.putString("imei", imei);
		        editor.commit();
		        
		        tv.setText("    NotesID: "+notesID);                  
		       }  
		     });  

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) {
		            return;   
		        }
		    });
		alert.show();
	}

	private JSONObject getvalidjson(String s){
		JSONObject json = null;

		try{	
			json = new JSONObject(s);
			if (json.isNull("code"))
				return null;

			String code = json.getString("code");
			if(!"0".equals(code))
				return null;
			return json;
		}catch(Exception e){
			return null;
		}
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.menu_copyurl){
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("", "http://api.map.baidu.com/lbsapi/getpoint/index.html?c=");
			clipboard.setPrimaryClip(clip);
			Toast.makeText(MainActivity.this, "url text is put into clipboard(menu)!", Toast.LENGTH_SHORT).show();
		}else if(id == R.id.menu_openbrowser) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://api.map.baidu.com/lbsapi/getpoint/index.html?c="));
			startActivity(browserIntent);
		}else if (id == R.id.menu_addloc){
			addLoc();
		}else if (id == R.id.menu_delloc) {
			delLoc();
		}else if (id == R.id.menu_showip) {
			tvResult.setText(Util.showip());
		}else if(id == R.id.menu_showlog) {
			Intent intent = new Intent(this, LogviewActivity.class);
			startActivity(intent);
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void delLoc() {

		final int idx = spin.getSelectedItemPosition();
		if(idx == 0 && arrAdpt.getCount() == 1){
			Toast.makeText(MainActivity.this, "Can not delete all", Toast.LENGTH_SHORT).show();
			return;
		}


		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Are you sure");
		alert.setMessage("Remove current location, this can not be recover");
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				spin.setSelection(idx - 1);
				Loc l = loc.remove(idx);
				arrAdpt.remove(l.name);
				arrAdpt.notifyDataSetChanged();
				Util.saveLocList(loc);
			}
		});
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		alert.show();
	}

	private void addLoc() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Input New Location");
		alert.setMessage("Enter new Location:");

		LayoutInflater factory = LayoutInflater.from(this);
		final View v = factory.inflate(R.layout.layout_inputloc, null);
		alert.setView(v);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String name = ((EditText)v.findViewById(R.id.editName)).getText().toString();
				String longitude = ((EditText)v.findViewById(R.id.editLongitude)).getText().toString();
				String latitude = ((EditText)v.findViewById(R.id.editLatitude)).getText().toString();
				if("".equals(name) || "".equals(longitude) || "".equals(latitude)){
					Toast.makeText(MainActivity.this, "text must not empty!", Toast.LENGTH_SHORT).show();
					return;
				}
				loc.add(new Loc(name, longitude, latitude));
				arrAdpt.add(name);
				arrAdpt.notifyDataSetChanged();
				Util.saveLocList(loc);
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return;
			}
		});
		alert.show();
	}

	/*
         * (non-Javadoc)
         * @see net.map.akb.Callback#finishCall(java.lang.String)
         *
         * when doSign finish background work, this function will be invoked.
         */
	@Override
	public void finishCall(String ret,
			String notesid,
			String signFlag,
			String lat,
			String lng) {
		tvResult.setText(ret);
		buttonEnable(true);

		Editor editor = pref.edit();
		editor.putInt("select", spin.getSelectedItemPosition());
		editor.commit();
	}
}
