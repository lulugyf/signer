package tt.bmapsign;

import android.os.Bundle;
import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import tt.bmapsign.util.FileLogger;
import tt.bmapsign.util.Util;

public class LogviewActivity extends Activity {

    //private ListView lv;
    private TextView tv;
    private TextView tv_log;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logview);

        String[] lines = FileLogger.tail(10);

//        lv = (ListView)findViewById(R.id.lv_logview);
//
//        ArrayAdapter<String> ad = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1, lines);
//        lv.setAdapter(ad);

        tv = (TextView)findViewById(R.id.tv_logpath);
        tv.setText(FileLogger.getPath());

        tv_log = (TextView)findViewById(R.id.tv_log);
        tv_log.setText(Util.join(lines, "\r\n\r\n"));
    }

}
