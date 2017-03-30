package gedoor.kunfei.lunarreminder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gedoor.kunfei.lunarreminder.CalendarProvider.LunarEvents;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.view.DialogGLC;
import gedoor.kunfei.lunarreminder.view.DialogTime;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

/**
 * Created by GKF on 2017/3/7.
 */
@SuppressLint("WrongConstant")
public class ReminderEditActivity extends AppCompatActivity {

    @BindView(R.id.vwchinesedate)
    TextView vwchinesedate;
    @BindView(R.id.text_reminder_me)
    EditText textReminderMe;

    private DialogGLC mDialog;
    private ChineseCalendar cc;
    int cyear;
    long id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_edit);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.reminder_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener((View view) -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            id = bundle.getLong("id");
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
                cc = new ChineseCalendar(c);
                cyear = cc.get(ChineseCalendar.CHINESE_YEAR);
                vwchinesedate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));

            }
        } else {
            initReminder();
        }

    }

    public void saveEvent() {
        String title = textReminderMe.getText().toString();
        if (title.isEmpty()) {
            Snackbar.make(textReminderMe, "提醒内容不能为空", Snackbar.LENGTH_LONG)
            .show();
            return;
        }
        cc.set(ChineseCalendar.CHINESE_YEAR, cyear);
        cc.get(Calendar.YEAR);
        long dtStart = cc.getTimeInMillis();
        cc.add(Calendar.DATE,1);
        cc.get(Calendar.DATE);
        long dtEnd = cc.getTimeInMillis();

        if (id == 0) {
            new LunarEvents().addEvent(title, dtStart, dtEnd);
        } else {
            new LunarEvents().updateEvent(id, title, dtStart, dtEnd);
        }
        this.setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reminder_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveEvent();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @OnClick({R.id.vwchinesedate})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vwchinesedate:
                selectDate();
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

    private void initReminder() {
        cc = new ChineseCalendar(Calendar.getInstance());
        cc.set(Calendar.HOUR_OF_DAY, 0);
        cc.set(Calendar.MINUTE, 0);
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND,0);
        cyear = cc.get(Calendar.YEAR);
        vwchinesedate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                .setTitle("退出")
                .setMessage("是否保存")
                .setPositiveButton("是", (DialogInterface di, int which)->{
                    saveEvent();
                })
                .setNegativeButton("否", (DialogInterface di, int which)->{
                    finish();
                })
                .show();
        }
        return false;
    }
}
