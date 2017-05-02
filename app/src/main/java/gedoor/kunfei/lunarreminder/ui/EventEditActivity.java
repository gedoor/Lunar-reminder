package gedoor.kunfei.lunarreminder.ui;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventReminder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gedoor.kunfei.lunarreminder.App;
import gedoor.kunfei.lunarreminder.data.FinalFields;
import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.data.GEvent;
import gedoor.kunfei.lunarreminder.data.Properties;
import gedoor.kunfei.lunarreminder.help.ReminderHelp;
import gedoor.kunfei.lunarreminder.async.InsertReminderEvents;
import gedoor.kunfei.lunarreminder.ui.view.DialogGLC;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import gedoor.kunfei.lunarreminder.util.EventTimeUtil;
import pub.devrel.easypermissions.AfterPermissionGranted;

import static gedoor.kunfei.lunarreminder.App.listEvent;
import static gedoor.kunfei.lunarreminder.App.eventRepeatType;
import static gedoor.kunfei.lunarreminder.App.eventRepeatNum;
import static gedoor.kunfei.lunarreminder.App.googleEvent;

/**
 * 编辑创建事件
 */
@SuppressLint("WrongConstant")
public class EventEditActivity extends BaseActivity {
    DialogGLC mDialog;
    ChineseCalendar cc = new ChineseCalendar();
    Event.Reminders reminders;
    List<EventReminder> listReminder = new ArrayList<>();
    ArrayList<HashMap<String, String>> listReminderDis = new ArrayList<>();
    static int[] reminderMinutes = new int[]{0, 900, 900, 9540, 9540};
    static String[] reminderMethod = new String[]{"", "popup", "email", "popup", "email"};
    boolean isCreateEvent;
    boolean isShortcut;

    int cYear;
    int position;
    String lunarRepeatType;
    String lunarRepeatNum;

    @BindView(R.id.tvw_chinese_date)
    TextView textChineseDate;
    @BindView(R.id.text_reminder_me)
    EditText textReminderMe;
    @BindView(R.id.tvw_repeat_type)
    TextView textRepeatType;
    @BindView(R.id.tvw_repeat_num)
    TextView textRepeatNum;
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
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener((View view) -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        googleEvent = new Event();
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            isShortcut = false;
            position = bundle.getInt("position");
            if (position == -1) {
                isCreateEvent = true;
                initEvent();
            } else {
                isCreateEvent = false;
                initGoogleEvent();
            }
        } else {
            isShortcut = true;
            isCreateEvent = true;
            initEvent();
        }

        listViewReminder.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> selectReminder(position));

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //初始化googleAccount
        initGoogleAccount();
    }

    //新建事件
    @SuppressLint("SetTextI18n")
    private void initEvent() {
        cc = new ChineseCalendar(Calendar.getInstance());
        cc.set(Calendar.HOUR_OF_DAY, 0);
        cc.set(Calendar.MINUTE, 0);
        cc.set(Calendar.SECOND, 0);
        cc.set(Calendar.MILLISECOND, 0);
        cYear = cc.get(Calendar.YEAR);
        textChineseDate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));
        lunarRepeatType = "year";
        lunarRepeatNum = sharedPreferences.getString(getString(R.string.pref_key_repeat_year), "12");
        textRepeatNum.setText(lunarRepeatNum);
        int defaultReminder = Integer.parseInt(sharedPreferences.getString(getString(R.string.pref_key_default_reminder), "0"));
        if (defaultReminder != 0) {
            EventReminder reminder = new EventReminder();
            reminder.setMinutes(reminderMinutes[defaultReminder]);
            reminder.setMethod(reminderMethod[defaultReminder]);
            listReminder.add(reminder);
        }
        refreshReminders();
    }
    //载入事件
    @SuppressLint("SetTextI18n")
    private void initGoogleEvent() {
        if (!App.getEvents(mContext)) {
            Toast.makeText(mContext, "获取缓存事件出错, 请下拉强制刷新事件", Toast.LENGTH_LONG).show();
            finish();
        }
        GEvent gEvent = new GEvent(this, listEvent.get(position));
        googleEvent.setExtendedProperties(new Properties(gEvent.getLunarRepeatId(), gEvent.getLunarRepeatNum()).getProperties());
        googleEvent.setId(gEvent.getId());
        textReminderMe.setText(gEvent.getSummary());
        textReminderMe.setSelection(gEvent.getSummary().length());
        cc.setTime(gEvent.getStart());
        textChineseDate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));
        lunarRepeatType = gEvent.getLunarRepeatType();
        lunarRepeatNum = gEvent.getLunarRepeatNum();
        textRepeatType.setText(lunarRepeatType.equals("year") ? getString(R.string.year): getString(R.string.month));
        textRepeatNum.setText(lunarRepeatNum);
        ArrayList<LinkedHashMap<String, Object>> listReminders = gEvent.getReminders();
        if (listReminders != null) {
            for (LinkedHashMap<String, Object> rd : listReminders) {
                EventReminder reminder = new EventReminder();
                Double minutes = (Double) rd.get("minutes");
                reminder.setMinutes(minutes.intValue());
                reminder.setMethod((String) rd.get("method"));
                listReminder.add(reminder);
            }
        }
        refreshReminders();
    }

    private void refreshReminders() {
        //提醒
        listReminderDis.clear();
        for (EventReminder reminder : listReminder) {
            HashMap<String, String> listMap = new HashMap<>();
            listMap.put("txTitle", new ReminderHelp(reminder).getTitle());
            listReminderDis.add(listMap);
        }
        HashMap<String, String> listMap = new HashMap<>();
        listMap.put("txTitle", getString(R.string.create_reminder));
        listReminderDis.add(listMap);
        SimpleAdapter adapter = new SimpleAdapter(this, listReminderDis, R.layout.item_reminder, new String[]{"txTitle"}, new int[]{R.id.reminder_item_title});
        listViewReminder.setAdapter(adapter);
    }

    private void selectReminder(int position) {
        int checkedItem = 1;
        String[] reminderTitle = new String[]{getString(R.string.reminder0), getString(R.string.reminder1), getString(R.string.reminder2),
                getString(R.string.reminder3), getString(R.string.reminder4), getString(R.string.reminder_customize)};
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
                reminderTitle[5] = getString(R.string.reminder_customize) + " - " + new ReminderHelp(reminder).getTitle();
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
                        EventReminder reminder = listReminder.get(position);
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

    private void saveEvent() {
        String title = textReminderMe.getText().toString();
        if (title.isEmpty()) {
            Snackbar.make(textReminderMe, "提醒内容不能为空", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
        saveGoogleEvent();
    }
    //保存事件
    private void saveGoogleEvent() {
        eventRepeatType = lunarRepeatType;
        eventRepeatNum = Integer.parseInt(lunarRepeatNum);
        googleEvent.setSummary(textReminderMe.getText().toString());
        googleEvent.setStart(new EventTimeUtil(cc).getEventStartDT());
        googleEvent.setEnd(new EventTimeUtil(cc).getEventEndDT());
        googleEvent.setDescription(textReminderMe.getText().toString() + "(农历)");
        reminders = new Event.Reminders();
        if (listReminder.size() > 0) {
            reminders.setUseDefault(false);
            reminders.setOverrides(listReminder);
        } else {
            reminders.setUseDefault(true);
            reminders.setOverrides(null);
        }
        googleEvent.setReminders(reminders);
        if (isShortcut) {
            getCalendarId();
            new InsertReminderEvents(this, lunarReminderCalendarId, googleEvent, Integer.parseInt(lunarRepeatNum)).execute();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            int operation = googleEvent.getId() == null ? FinalFields.OPERATION_INSERT : FinalFields.OPERATION_UPDATE;
            bundle.putInt(FinalFields.OPERATION, operation);
            intent.putExtras(bundle);
            this.setResult(RESULT_OK, intent);
            finish();
        }

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
    @OnClick({R.id.vw_chinese_date, R.id.vw_repeat_num, R.id.vw_repeat_type})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.vw_chinese_date:
                selectDate();
                break;
            case R.id.vw_repeat_num:
                selectRepeatYear();
                break;
            case R.id.vw_repeat_type:
                selectRepeatType();
                break;
        }
    }

    //选择重复方式
    private void selectRepeatType() {
        String[] repeatType = new String[]{"year", "month"};
        String[] repeatTypeStr = new String[]{getString(R.string.year), getString(R.string.month)};
        int checkedItem = lunarRepeatType.equals(repeatType[1]) ? 1 : 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_repeat_type);
        builder.setSingleChoiceItems(repeatTypeStr, checkedItem, (dialog, which) -> {
            textRepeatType.setText(repeatTypeStr[which]);
            lunarRepeatType = repeatType[which];
            dialog.dismiss();
        });
        builder.create();
        builder.show();
    }
    //选择重复次数
    @SuppressLint("SetTextI18n")
    private void selectRepeatYear() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_repeat_num);
        @SuppressLint("InflateParams") View view = LayoutInflater.from(this).inflate(R.layout.dialog_repeat_num, null);
        NumberPicker numberPicker = (NumberPicker) view.findViewById(R.id.number_picker_repeat_year);
        numberPicker.setMaxValue(36);
        numberPicker.setMinValue(1);
        numberPicker.setValue(Integer.parseInt(lunarRepeatNum));
        builder.setView(view);
        builder.setPositiveButton(R.string.ok, (DialogInterface dialog, int which) -> {
            lunarRepeatNum = String.valueOf(numberPicker.getValue());
            textRepeatNum.setText(lunarRepeatNum);
        });
        builder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int which) -> {
        });
        builder.create();
        builder.show();
    }

    public interface DialogListener {
        void getCalendar(ChineseCalendar cc);
    }

    @SuppressLint("SetTextI18n")
    private void selectDate() {
        mDialog = new DialogGLC(this, ((ChineseCalendar cc) -> {
            this.cc = cc;
            textChineseDate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));
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

    @AfterPermissionGranted(REQUEST_PERMS)
    private void methodRequiresPermission() {
        afterPermissionGranted();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.exit))
                    .setMessage(getString(R.string.exit_event_description))
                    .setPositiveButton("是", (DialogInterface dialogInterface, int which) -> {})
                    .setNegativeButton("否", (DialogInterface dialogInterface, int which) -> finish())
                    .show();
            return true;
        }
        return super.onKeyDown(keyCode, keyEvent);
    }
}
