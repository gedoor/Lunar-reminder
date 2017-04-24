package gedoor.kunfei.lunarreminder.async;

import com.google.api.services.calendar.model.ColorDefinition;
import com.google.api.services.calendar.model.Colors;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import gedoor.kunfei.lunarreminder.ui.BaseActivity;
import gedoor.kunfei.lunarreminder.util.ACache;

/**
 * 获取Event颜色ID和颜色
 */

public class GetEventColors extends CalendarAsyncTask {

    public GetEventColors(BaseActivity activity) {
        super(activity);
    }

    protected void doInBackground() throws IOException {
        ACache mCache = ACache.get(activity);
        if (mCache.isExist("eventColors", ACache.STRING)) {
            HashMap<String, String> hp = new HashMap<>();
            Colors colors = client.colors().get().setFields("items(event)").execute();
            for (Map.Entry<String, ColorDefinition> color : colors.getEvent().entrySet()) {
                hp.put(color.getValue().getBackground(), color.getKey());
            }
            Gson gson = new Gson();
            String eventColors = gson.toJson(hp);
            mCache.put("eventColors", eventColors);
        }
    }

}
