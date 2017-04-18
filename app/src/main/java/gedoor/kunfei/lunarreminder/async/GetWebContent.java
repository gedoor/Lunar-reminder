package gedoor.kunfei.lunarreminder.async;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gedoor.kunfei.lunarreminder.ui.BaseActivity;
import gedoor.kunfei.lunarreminder.util.ACache;

import static gedoor.kunfei.lunarreminder.data.FinalFields.solarTermsF;
import static gedoor.kunfei.lunarreminder.data.FinalFields.solarTermsJ;

/**
 * Created by GKF on 2017/4/18.
 * 获取节气
 */

public class GetWebContent extends CalendarAsyncTask {

    public GetWebContent(BaseActivity activity) {
        super(activity);
    }

    @Override
    protected void doInBackground() throws IOException {
        String urlStr = "http://data.weather.gov.hk/gts/time/calendar/text/T2017c.txt";
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        int code = connection.getResponseCode();
        try {
            if (HttpURLConnection.HTTP_OK == code) {
                connection.connect();
                InputStream is = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, "Big5"));
                String line = null;
                ArrayList<HashMap<String, String>> list = new ArrayList<>();
                while ((line = bufferedReader.readLine()) != null) {
                    for (int i=0; i<24; i++) {
                        if (line.contains(solarTermsF[i])) {
                            String dt[] = line.split(" ");
                            HashMap<String, String> hp = new HashMap<>();
                            hp.put("dt", dt[0]);
                            hp.put("jq", solarTermsJ[i]);
                            list.add(hp);
                        }
                    }
                }
                is.close();
                Gson gson = new Gson();
                String str = gson.toJson(list);
                ACache mCache = ACache.get(activity);
                mCache.put("jq", str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }

    }

}
