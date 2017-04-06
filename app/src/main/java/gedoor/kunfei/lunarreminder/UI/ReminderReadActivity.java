package gedoor.kunfei.lunarreminder.UI;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.api.client.util.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import gedoor.kunfei.lunarreminder.Data.FinalFields;
import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarType;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvent;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvents;

/**
 * Created by GKF on 2017/3/18.
 */

public class ReminderReadActivity extends AppCompatActivity {
    private static final int REQUEST_REMINDER = 1;

    @BindView(R.id.vwchinesedate)
    TextView vwchinesedate;
    @BindView(R.id.text_reminder_me)
    TextView textReminderMe;

    ChineseCalendar cc = new ChineseCalendar();
    long eventID;
    int position;

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
        googleEvent = googleEvents.getItems().get(position);
        textReminderMe.setText(googleEvent.getSummary());
        DateTime start = googleEvent.getStart().getDate();
        if (start==null) start = googleEvent.getStart().getDateTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            cc.setTime(dateFormat.parse(start.toStringRfc3339()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        vwchinesedate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));
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