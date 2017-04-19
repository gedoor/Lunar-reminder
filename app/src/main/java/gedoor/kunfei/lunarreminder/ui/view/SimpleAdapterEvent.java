package gedoor.kunfei.lunarreminder.ui.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import gedoor.kunfei.lunarreminder.R;


/**
 * Created by GKF on 2017/4/2.
 * 自定义Adapter
 */

public class SimpleAdapterEvent extends SimpleAdapter {
    private Context mContext;
    private ArrayList<HashMap<String, String>> listItem;

    public SimpleAdapterEvent(Context context, ArrayList<HashMap<String, String>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.mContext = context;
        this.listItem = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        String mId = listItem.get(position).get("id");
        String bgColor = listItem.get(position).get("bgColor");
        TextView start = (TextView) view.findViewById(R.id.event_item_date);
        TextView title = (TextView) view.findViewById(R.id.event_item_title);
        if (mId.equals("")) {
            start.setTextSize(30);
            title.setBackground(mContext.getResources().getDrawable(R.color.colorTransparent));
            title.setTextColor(mContext.getResources().getColor(R.color.colorBlack));
            title.setTextSize(30);
        } else {
            start.setTextSize(16);
            title.setBackground(new ColorDrawable(Color.parseColor(bgColor)));
            title.setTextColor(mContext.getResources().getColor(R.color.colorWhite));
            title.setTextSize(16);
        }

        return view;
    }
}
