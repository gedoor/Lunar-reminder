package gedoor.kunfei.lunarreminder.UI;

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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gedoor.kunfei.lunarreminder.Data.FinalFields;
import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.UI.view.DialogGLC;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarType;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvent;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvents;
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

    DialogGLC mDialog;
    ChineseCalendar cc = new ChineseCalendar();
    int cyear;
    long id;
    int position;

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
            position = bundle.getInt("position");
            if (calendarType.equals(FinalFields.CalendarTypeGoogle)) {
                googleEvent = googleEvents.getItems().get(position);
                initGoogleEvent();
            }
        } else {
            if (calendarType.equals(FinalFields.CalendarTypeGoogle)) {
                googleEvent = new Event();
            }
            initEvent();
        }

    }

    private void initGoogleEvent() {
        textReminderMe.setText(googleEvent.getSummary());
        textReminderMe.setSelection(googleEvent.getSummary().length());
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

    private void initEvent() {
        cc = new ChineseCalendar(Calendar.getInstance());
        cc.set(Calendar.HOUR_OF_DAY, 0);
        cc.set(Calendar.MINUTE, 0);
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND,0);
        cyear = cc.get(Calendar.YEAR);
        vwchinesedate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));
    }

    private void saveEvent() {
        String title = textReminderMe.getText().toString();
        if (title.isEmpty()) {
            Snackbar.make(textReminderMe, "提醒内容不能为空", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
        if (calendarType.equals(FinalFields.CalendarTypeGoogle)) {
            saveGoogleEvent();
        }
    }

    private void saveGoogleEvent() {
        googleEvent.setSummary(textReminderMe.getText().toString());
        googleEvent.setStart(new EventTimeUtil(cc).getEventStartDT());
        googleEvent.setEnd(new EventTimeUtil(cc).getEventEndDT());
        googleEvent.setDescription(textReminderMe.getText().toString() + "(农历)");
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        int operation = googleEvent.getId() == null ? FinalFields.OPERATION_INSERT : FinalFields.OPERATION_UPDATE;
        bundle.putInt(FinalFields.OPERATION,operation);
        intent.putExtras(bundle);
        this.setResult(RESULT_OK, intent);
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
