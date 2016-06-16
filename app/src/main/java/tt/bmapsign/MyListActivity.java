package tt.bmapsign;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MyListActivity extends ListActivity {

String[] DayOfWeek = {"Sunday", "Monday", "Tuesday",
  "Wednesday", "Thursday", "Friday", "Saturday"
};

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       //setContentView(R.layout.main);
       setListAdapter(new ArrayAdapter<String>(this,
         android.R.layout.simple_list_item_1, DayOfWeek));
   }

@Override
protected void onListItemClick(ListView l, View v, int position, long id) {
 // TODO Auto-generated method stub
 //super.onListItemClick(l, v, position, id);
 String selection = l.getItemAtPosition(position).toString();
 Toast.makeText(this, selection, Toast.LENGTH_LONG).show();
}
}