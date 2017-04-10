package gedoor.kunfei.lunarreminder.UI;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventReminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gedoor.kunfei.lunarreminder.Data.FinalFields;
import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.UI.Help.ReminderUtil;
import gedoor.kunfei.lunarreminder.UI.view.DialogGLC;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;

import static gedoor.kunfei.lunarreminder.Data.FinalFields.LunarRepeatYear;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.eventRepeat;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvent;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvents;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

/**
 * Created by GKF on 2017/3/7.
 */
@SuppressLint("WrongConstant")
public class EventEditActivity extends AppCompatActivity {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    DialogGLC mDialog;
    ChineseCalendar cc = new ChineseCalendar();
    Event.Reminders reminders;
    List<EventReminder> listReminder = new ArrayList<>();
    ArrayList<HashMap<String, String>> listReminderDis = new ArrayList<HashMap<String, String>>();
    int cYear;
    long id;
    int position;
    String lunarRepeatNum;

    @BindView(R.id.vw_chinese_date)
    TextView vwChineseDate;
    @BindView(R.id.text_reminder_me)
    EditText textReminderMe;
    @BindView(R.id.vw_repeat)
    TextView vwRepeat;
    @BindView(R.id.list_vw_reminder)
    ListView listViewReminder;
    @BindView(R.id.reminder_toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        toolbar.setNavigationOnClickListener((View view) -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            id = bundle.getLong("id");
            position = bundle.getInt("position");
            googleEvent = googleEvents.get(position);
            initGoogleEvent();
        } else {
            googleEvent = new Event();
            initEvent();
        }

        listViewReminder.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            editReminder(position);
        });

    }

    private void initGoogleEvent() {
        textReminderMe.setText(googleEvent.getSummary());
        textReminderMe.setSelection(googleEvent.getSummary().length());
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
                HashMap<String, String> listMap = new HashMap<>();
                listMap.put("txTitle", new ReminderUtil(reminder).getTitle());
                listReminderDis.add(listMap);
            }
        }
        HashMap<String, String> listMap = new HashMap<String, String>();
        listMap.put("txTitle", getString(R.string.create_reminder));
        listReminderDis.add(listMap);
        SimpleAdapter adapter = new SimpleAdapter(this, listReminderDis, R.layout.item_reminder, new String[]{"txTitle"}, new int[]{R.id.reminder_item_title});
        listViewReminder.setAdapter(adapter);
    }

    private void editReminder(int position) {
        int checkedItem = 1;
        int[] reminderMinutes = new int[]{0, 900, 900, 9540, 9540};
        String[] reminderMethod = new String[]{"", "popup", "email", "popup", "email"};
        String[] reminderTitle = new String[]{getString(R.string.reminder0), getString(R.string.reminder1), getString(R.string.reminder2),
                getString(R.string.reminder3), getString(R.string.reminder4), getString(R.string.customize)};
        boolean isCreateReminder = listReminderDis.get(position).get("txTitle").equals(getString(R.string.create_reminder));
        if (!isCreateReminder) {
            EventReminder reminder = listReminder.get(position);
            if (reminder.getMinutes() == reminderMinutes[1]) {
                if (reminder.getMethod().equals("email")) {
                    checkedItem = 2;
                }
            } else if (reminder.getMinutes() == reminderMinutes[3]) {
                if (reminder.getMethod().equals("email")) {
                    checkedItem = 4;
                } else {
                    checkedItem = 3;
                }
            } else {
                checkedItem = 5;
                reminderTitle = new String[]{getString(R.string.reminder0), getString(R.string.reminder1), getString(R.string.reminder2),
                        getString(R.string.reminder3),new ReminderUtil(reminder).getTitle() + getString(R.string.reminder4)};
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setSingleChoiceItems(reminderTitle, checkedItem, (DialogInterface dialog, int which) -> {
            switch (which) {
                case 0:
                    if (!isCreateReminder) {
                        listReminder.remove(position);
                        refreshReminders();
                    }
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                    if (isCreateReminder) {
                        EventReminder reminder = new EventReminder();
                        reminder.setMinutes(reminderMinutes[which]);
                        reminder.setMethod(reminderMethod[which]);
                        listReminder.add(reminder);
                    } else {
                        EventReminder reminder= listReminder.get(position);
                        reminder.setMinutes(reminderMinutes[which]);
                        reminder.setMethod(reminderMethod[which]);
                    }
                    refreshReminders();
                    break;
                case 5:
                    break;
            }
            dialog.dismiss();
        });
        builder.show();
    }

    private void initEvent() {
        cc = new ChineseCalendar(Calendar.getInstance());
        cc.set(Calendar.HOUR_OF_DAY, 0);
        cc.set(Calendar.MINUTE, 0);
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        cYear = cc.get(Calendar.YEAR);
        vwChineseDate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));
        lunarRepeatNum = preferences.getString(getString(R.string.pref_key_repeat_year), "12");
        vwRepeat.setText(getString(R.string.repeat) + lunarRepeatNum + getString(R.string.year));
        refreshReminders();
    }

    private void saveEvent() {
        String title = textReminderMe.getText().toString();
        if (title.isEmpty()) {
            Snackbar.make(textReminderMe, "提醒内容不能为空", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
        saveGoogleEvent();
    }

    private void saveGoogleEvent() {
        eventRepeat = Integer.parseInt(lunarRepeatNum);
        googleEvent.setSummary(textReminderMe.getText().toString());
        googleEvent.setStart(new EventTimeUtil(cc).getEventStartDT());
        googleEvent.setEnd(new EventTimeUtil(cc).getEventEndDT());
        googleEvent.setDescription(textReminderMe.getText().toString() + "(农历)");
        if (listReminder.size() > 0) {
            reminders.setUseDefault(false);
            reminders.setOverrides(listReminder);

        } else {
            reminders.setUseDefault(true);
        }
        googleEvent.setReminders(reminders);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        int operation = googleEvent.getId() == null ? FinalFields.OPERATION_INSERT : FinalFields.OPERATION_UPDATE;
        bundle.putInt(FinalFields.OPERATION, operation);
        intent.putExtras(bundle);
        this.setResult(RESULT_OK, intent);
        finish();
    }

    //菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_event_edit, menu);
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

    //单击事件
    @OnClick({R.id.vw_chinese_date, R.id.vw_repeat})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vw_chinese_date:
                selectDate();
                break;
            case R.id.vw_repeat:
                selectRepeatYear();
                break;
        }
    }

    private void selectRepeatYear() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择重复年数");
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_repeat_year, null);
        NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.number_picker_repeat_year);
        numberPicker.setMaxValue(36);
        numberPicker.setMinValue(1);
        numberPicker.setValue(Integer.parseInt(lunarRepeatNum));
        builder.setView(view);
        builder.setPositiveButton("确定", (DialogInterface dialog, int which) -> {
            lunarRepeatNum = String.valueOf(numberPicker.getValue());
            vwRepeat.setText(getString(R.string.repeat) + lunarRepeatNum + getString(R.string.year));
        });
        builder.setNegativeButton("取消", (DialogInterface dialog, int which) -> {
        });
        builder.create();
        builder.show();
    }

    public interface DialogListener {
        void getCalendar(ChineseCalendar cc);
    }

    private void selectDate() {
        mDialog = new DialogGLC(this, ((ChineseCalendar cc) -> {
            this.cc = cc;
            vwChineseDate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));
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
                    .setPositiveButton("是", (DialogInterface dialogInterface, int which) -> {
                        saveEvent();
                    })
                    .setNegativeButton("否", (DialogInterface dialogInterface, int which) -> {
                        finish();
                    })
                    .show();
        }
        return false;
    }
}
