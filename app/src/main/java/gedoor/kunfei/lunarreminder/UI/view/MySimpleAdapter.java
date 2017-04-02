package gedoor.kunfei.lunarreminder.UI.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import gedoor.kunfei.lunarreminder.R;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

/**
 * Created by GKF on 2017/4/2.
 */

public class MySimpleAdapter extends SimpleAdapter {
    ArrayList<HashMap<String, String>> listitem;

    public MySimpleAdapter(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.listitem = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        String mId = listitem.get(position).get("id");
        if (mId == "") {
            TextView title = (TextView) view.findViewById(R.id.reminder_item_title);
            title.setBackground(mContext.getResources().getDrawable(R.color.colorTransparent));
            title.setTextColor(mContext.getResources().getColor(R.color.black));
            title.setTextSize(30);
        }

        return view;
    }
}
