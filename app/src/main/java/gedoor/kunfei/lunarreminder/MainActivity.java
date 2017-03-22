package gedoor.kunfei.lunarreminder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import gedoor.kunfei.lunarreminder.CalendarProvider.initLunar;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER;
import static gedoor.kunfei.lunarreminder.Data.FinalFields.setingFile;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_REMINDER = 1;
    private static final int REQUEST_SETTINGS = 2;
    private static final int REQUEST_PERMS = 3;

    int index;

    String[] perms = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String accountName;

    @BindView(R.id.view_reminder_list)
    ListView viewReminderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener((View view) -> {
                    Intent intent = new Intent(this, ReminderEditActivity.class);
                    startActivityForResult(intent, REQUEST_REMINDER);
                }
        );

        sharedPreferences = this.getSharedPreferences(setingFile, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        accountName = sharedPreferences.getString("accountName", null);

        //get permission
        if (EasyPermissions.hasPermissions(this, perms)) {
            initActivity();
        } else {
            EasyPermissions.requestPermissions(this, "get permissions", REQUEST_PERMS, perms);
        }

        viewReminderList.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) ->{
            Intent intent = new Intent(this, ReminderReadActivity.class);
            Bundle bundle = new Bundle();
            bundle.putLong("id", id);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_REMINDER);
        });

    }

    public void initActivity() {
        //初始化列表
        initLunar initlunar = new initLunar();
        if (accountName == null) {
            Map accounts = initlunar.getCalendarsAccountNames();
            selectAccount(accounts);
        }

        long calenderID = initlunar.getCalendarID(accountName);
        if (calenderID == 0) {
            String accountType = sharedPreferences.getString("accountType", "LOCAL");
            new initLunar().addCalendar(accountName, accountType);
        }

        Cursor cursor = initlunar.getEvents();
        SimpleCursorAdapter listAdapter = new SimpleCursorAdapter(this,
                R.layout.item_reminder,
                cursor,
                new String[]{Events.DTSTART, Events.TITLE},
                new int[]{R.id.reminder_item_date, R.id.reminder_item_title},
                FLAG_REGISTER_CONTENT_OBSERVER);
        listAdapter.setViewBinder(viewBinder);
        viewReminderList.setAdapter(listAdapter);
    }

    private SimpleCursorAdapter.ViewBinder viewBinder = (View view, Cursor cursor, int columnIndex)-> {
            if(cursor.getColumnIndex(Events.DTSTART)==columnIndex){    //duration为数据库中对应的属性列
                TextView textView=(TextView)view;
                String std = cursor.getString(columnIndex);
                Date dt = new Date(Long.parseLong(std));
                Calendar c = Calendar.getInstance();
                c.setTime(dt);
                ChineseCalendar cc = new ChineseCalendar(c);
                textView.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + "\n" + cc.getChinese(ChineseCalendar.CHINESE_DATE));  //显示
                return true;
            }
            return false;
    };

    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            this.startActivityForResult(intent,REQUEST_SETTINGS);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void selectAccount(Map accounts) {
        Set set = accounts.keySet();
        String[] accountNames = new String[set.size()];
        set.toArray(accountNames);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.selectCalendarAaccount);
        builder.setSingleChoiceItems(accountNames, 0, (DialogInterface dialog, int which) -> {
            index = which;
        });
        builder.setPositiveButton("确定", (DialogInterface dialog, int which) -> {
            accountName = accountNames[index];
            editor.putString("accountName", accountNames[index]);
            editor.putString("accountType", accounts.get(accountName).toString());
            editor.commit();
        });
        builder.setNegativeButton("取消", (DialogInterface dialog, int which) -> {
            this.finish();
        });
        builder.show();
    }

    @AfterPermissionGranted(REQUEST_PERMS)
    private void methodRequiresTwoPermission() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            initActivity();
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }


}
