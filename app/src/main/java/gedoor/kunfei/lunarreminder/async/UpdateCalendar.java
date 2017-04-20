package gedoor.kunfei.lunarreminder.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.IOException;
import java.util.Collections;

import gedoor.kunfei.lunarreminder.R;

/**
 * 更新日历
 */

public class UpdateCalendar extends AsyncTask<Void, Integer, Boolean> {
    private Context mContext;
    private ProgressDialog mDialog;
    private String calendarId;
    private final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    private final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    public UpdateCalendar(Context context, ProgressDialog dialog, String calendarId) {
        mContext = context;
        mDialog = dialog;
        this.calendarId = calendarId;
    }

    @Override
    protected final Boolean doInBackground(Void... ignored) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        int calendarColor = sharedPreferences.getInt(mContext.getString(R.string.pref_key_calendar_color),0);
        String mGoogleAccount = sharedPreferences.getString(mContext.getString(R.string.pref_key_google_account), null);
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccountName(mGoogleAccount);
        Calendar client = new Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Google-LunarReminder")
                .build();
        try {
            CalendarListEntry calendarListEntry = client.calendarList().get(calendarId).execute();
            calendarListEntry.setBackgroundColor(String.format("#%06X", 0xFFFFFF & calendarColor));
            client.calendarList().update(calendarListEntry.getId(), calendarListEntry).setColorRgbFormat(true).execute();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        mDialog.dismiss();
        if (!success) {
            Toast.makeText(mContext, "更新失败", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, "更新成功", Toast.LENGTH_LONG).show();
        }
    }
}
