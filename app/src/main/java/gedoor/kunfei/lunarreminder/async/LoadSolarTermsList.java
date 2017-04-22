package gedoor.kunfei.lunarreminder.async;

import android.annotation.SuppressLint;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import gedoor.kunfei.lunarreminder.ui.BaseActivity;
import gedoor.kunfei.lunarreminder.util.ACache;

/**
 * 载入节气
 */

public class LoadSolarTermsList extends CalendarAsyncTask {
    private ArrayList<HashMap<String, String>> list;

    public LoadSolarTermsList(BaseActivity activity) {
        super(activity);
    }

    @SuppressLint("WrongConstant")
    @Override
    protected void doInBackground() throws IOException {
        ACache mCache = ACache.get(activity);
        String str = mCache.getAsString("jq");
        Gson gson = new Gson();
        list = gson.fromJson(str, new TypeToken<ArrayList<HashMap<String, String>>>(){}.getType());

    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        activity.list.clear();
        activity.list.addAll(list);
        activity.eventListFinish();
    }
}
