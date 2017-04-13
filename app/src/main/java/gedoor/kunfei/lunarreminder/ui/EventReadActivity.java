package gedoor.kunfei.lunarreminder.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import gedoor.kunfei.lunarreminder.data.FinalFields;
import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.ui.help.ReminderUtil;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;

import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatYear;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvent;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvents;

/**
 * Created by GKF on 2017/3/18.
 */

public class EventReadActivity extends BaseActivity {
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
    @BindView(R.id.reminder_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_read);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener((View view) -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
                    Intent intent = new Intent(this, EventEditActivity.class);
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
            InitGoogleEvent();
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
                listMap.put("txTitle", new ReminderUtil(reminder).getTitle());
                listReminderDis.add(listMap);
            }
        } else {
            HashMap<String, String> listMap = new HashMap<String, String>();
            listMap.put("txTitle", getString(R.string.defaultReminder));
            listReminderDis.add(listMap);
        }
        SimpleAdapter adapter = new SimpleAdapter(this, listReminderDis, R.layout.item_reminder, new String[]{"txTitle"}, new int[]{R.id.reminder_item_title});
        listViewReminder.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this  adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event_read, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            bundle.putInt(FinalFields.OPERATION, FinalFields.OPERATION_DELETE);
            intent.putExtras(bundle);
            this.setResult(RESULT_OK, intent);
            finish();
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
