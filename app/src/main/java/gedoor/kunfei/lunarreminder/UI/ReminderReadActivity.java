package gedoor.kunfei.lunarreminder.UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventReminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import gedoor.kunfei.lunarreminder.Data.FinalFields;
import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;

import static gedoor.kunfei.lunarreminder.Data.FinalFields.LunarRepeatYear;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarType;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvent;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvents;

/**
 * Created by GKF on 2017/3/18.
 */

public class ReminderReadActivity extends AppCompatActivity {
    private static final int REQUEST_REMINDER = 1;

    ChineseCalendar cc = new ChineseCalendar();
    Event.Reminders reminders;
    List<EventReminder> listReminder;
    ArrayList<HashMap<String, String>> listReminderDis = new ArrayList<HashMap<String, String>>();
    long eventID;
    int position;
    String lunarRepeatNum;

    @BindView(R.id.vw_chinese_date)
    TextView vwChineseDate;
    @BindView(R.id.text_reminder_me)
    TextView textReminderMe;
    @BindView(R.id.vw_repeat)
    TextView vwRepeat;
    @BindView(R.id.list_vw_reminder)
    ListView listViewReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_read);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.reminder_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener((View view) -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
                    Intent intent = new Intent(this, ReminderEditActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putLong("id", eventID);
                    bundle.putInt("position", position);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, REQUEST_REMINDER);
                }
        );

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            eventID = bundle.getLong("id");
            position = bundle.getInt("position");
            if (calendarType.equals(FinalFields.CalendarTypeGoogle)) {
                InitGoogleEvent();
            }
        } else {
            finish();
        }

    }

    private void InitGoogleEvent() {
        googleEvent = googleEvents.get(position);
        textReminderMe.setText(googleEvent.getSummary());
        DateTime start = googleEvent.getStart().getDate();
        if (start == null) start = googleEvent.getStart().getDateTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            cc.setTime(dateFormat.parse(start.toStringRfc3339()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        vwChineseDate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));
        Event.ExtendedProperties properties = googleEvent.getExtendedProperties();
        lunarRepeatNum = properties.getPrivate().get(LunarRepeatYear);
        if (lunarRepeatNum == null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            lunarRepeatNum = preferences.getString(getString(R.string.pref_key_repeat_year), "12");
        }
        vwRepeat.setText(getString(R.string.repeat) + lunarRepeatNum + getString(R.string.year));
        reminders = googleEvent.getReminders();
        listReminder = reminders.getOverrides();
        refreshReminders();
    }

    private void refreshReminders() {
        //提醒
        listReminderDis.clear();
        if (listReminder != null) {
            for (EventReminder reminder : listReminder) {
                HashMap<String, String> listMap = new HashMap<String, String>();
                String txType = reminder.getMethod();
                int tqMinutes = reminder.getMinutes();
                int tqDay = tqMinutes%1440 == 0 ? tqMinutes/1440 : tqMinutes/1440 + 1;
                int txMinutes = tqMinutes%1440 == 0 ? 0 : 1440 - tqMinutes%1440;
                int txHour = txMinutes/60;
                int txMinutesByHour = txMinutes%60;
                String txTime = "提前" + tqDay + "天" + String.format(Locale.CHINA,"%2d:%02d", txHour, txMinutesByHour);
                listMap.put("txTitle", txTime);
                listReminderDis.add(listMap);
            }
        }
        SimpleAdapter adapter = new SimpleAdapter(this, listReminderDis, R.layout.item_reminder, new String[]{"txTitle"}, new  int[]{R.id.reminder_item_title});
        listViewReminder.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this  adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reminder_read, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            if (calendarType.equals(FinalFields.CalendarTypeGoogle)) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putInt(FinalFields.OPERATION, FinalFields.OPERATION_DELETE);
                intent.putExtras(bundle);
                this.setResult(RESULT_OK, intent);
                finish();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_REMINDER:
                    this.setResult(RESULT_OK, data);
                    finish();
            }
        }
    }
}
