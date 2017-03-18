package gedoor.kunfei.lunarreminder;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gedoor.kunfei.lunarreminder.Data.ChineseCalendar;
import gedoor.kunfei.lunarreminder.view.DialogGLC;
import gedoor.kunfei.lunarreminder.view.DialogTime;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

/**
 * Created by GKF on 2017/3/7.
 */

public class ReminderEditActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.swallday)
    Switch swallday;
    @BindView(R.id.vwchinesedate)
    TextView vwchinesedate;
    @BindView(R.id.vwtime)
    TextView vwtime;
    @BindView(R.id.text_reminder_me)
    EditText textReminderMe;

    private DialogGLC mDialog;
    private ChineseCalendar cc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_edit);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.reminder_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener((View view) -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        swallday.setOnCheckedChangeListener(this);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            long id = bundle.getLong("id");
            Uri uri = CalendarContract.Events.CONTENT_URI;
            ContentResolver cr = mContext.getContentResolver();
            String[] selectcol = new String[]{CalendarContract.Events._ID, CalendarContract.Events._COUNT, CalendarContract.Events.TITLE};
            String selection = "(" + CalendarContract.Events._ID + " = ?)";
            String[] selectionArgs = new String[]{Long.toString(id)};
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Cursor cursor = cr.query(uri, null, selection, selectionArgs, null);
            while (cursor.moveToNext()) {
                textReminderMe.setText(cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE)));
                String std = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.DTSTART));
                Date dt = new Date(Long.parseLong(std));
                Calendar c = Calendar.getInstance();
                c.setTime(dt);
                ChineseCalendar cc = new ChineseCalendar(c);
                vwchinesedate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));
                String allday = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.ALL_DAY));
                if (allday.contains("0")) {
                    swallday.setChecked(false);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("k:mm");
                    vwtime.setText(dateFormat.format(c.getTime()));
                } else {
                    swallday.setChecked(true);
                    vwtime.setVisibility(View.INVISIBLE);
                    vwtime.setText("7:00");
                }

            }
        } else {
            initReminder();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            this.setResult(RESULT_OK);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.swallday:
                if (isChecked) {
                    vwtime.setVisibility(View.INVISIBLE);
                } else {
                    vwtime.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @OnClick({R.id.vwchinesedate, R.id.vwtime})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vwchinesedate:
                selectDate();
                break;
            case R.id.vwtime:
                selectTime(vwtime);
                break;
        }
    }

    public interface DialogListener {
        public void getCalendar(ChineseCalendar cc);
    }

    private void selectDate() {
        mDialog = new DialogGLC(this, ((ChineseCalendar cc) -> {
            this.cc = cc;
            vwchinesedate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));
        }));

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        } else {
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(true);
            mDialog.show();
            mDialog.initCalendar(cc, false);
        }
    }

    public void selectTime(View v) {
        DialogFragment newFragment = new DialogTime();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    private void initReminder() {
        cc = new ChineseCalendar(Calendar.getInstance());
        swallday.setChecked(true);
        vwtime.setVisibility(View.INVISIBLE);
        vwtime.setText("7:00");
        vwchinesedate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));
    }
}
