package gedoor.kunfei.lunarreminder.ui.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import gedoor.kunfei.lunarreminder.R;


/**
 * Created by GKF on 2017/4/2.
 * 自定义Adapter
 */

public class SimpleAdapterEvent extends SimpleAdapter {
    private Context mContext;
    private ArrayList<HashMap<String, String>> listItem;
    private Drawable calendarColor;

    public SimpleAdapterEvent(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.mContext = context;
        this.listItem = data;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int color = sharedPreferences.getInt(context.getString(R.string.pref_key_calendar_color), 0);
        this.calendarColor = color == 0 ?
                context.getResources().getDrawable(R.color.colorLunar) : new ColorDrawable(color);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        String mId = listItem.get(position).get("id");
        TextView start = (TextView) view.findViewById(R.id.event_item_date);
        TextView title = (TextView) view.findViewById(R.id.event_item_title);
        if (mId == "") {
            start.setTextSize(30);
            title.setBackground(mContext.getResources().getDrawable(R.color.colorTransparent));
            title.setTextColor(mContext.getResources().getColor(R.color.colorBlack));
            title.setTextSize(30);
        } else {
            start.setTextSize(16);
            title.setBackground(calendarColor);
            title.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
            title.setTextSize(16);
        }

        return view;
    }
}
