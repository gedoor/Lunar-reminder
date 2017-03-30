package gedoor.kunfei.lunarreminder;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import gedoor.kunfei.lunarreminder.Async.GetEvents;
import gedoor.kunfei.lunarreminder.Async.InsertCalendar;
import gedoor.kunfei.lunarreminder.Async.LoadCalendars;
import gedoor.kunfei.lunarreminder.CalendarProvider.InitLunar;
import gedoor.kunfei.lunarreminder.util.ChineseCalendar;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER;
import static gedoor.kunfei.lunarreminder.Data.FinalFields.PREF_GOOGLE_ACCOUNT_NAME;
import static gedoor.kunfei.lunarreminder.Data.FinalFields.PREF_GOOGLE_CALENDAR_ID;
import static gedoor.kunfei.lunarreminder.Data.FinalFields.PREF_GOOGLE_CALENDAR_TIMEZONE;
import static gedoor.kunfei.lunarreminder.Data.FinalFields.caledarName;
import static gedoor.kunfei.lunarreminder.Data.FinalFields.setingFile;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

@SuppressLint("WrongConstant")
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_REMINDER = 1;
    private static final int REQUEST_SETTINGS = 2;
    private static final int REQUEST_PERMS = 3;
    private static final int REQUEST_ACCOUNT_PICKER = 4;
    public static final int REQUEST_AUTHORIZATION = 5;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    private String mGoogleAccount;
    private String mTimeZone;
    public GoogleAccountCredential credential;
    public com.google.api.services.calendar.Calendar client;
    public ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();;
    private SimpleAdapter adapter;
    int index;
    long st;
    long et;

    String[] perms = {Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR, Manifest.permission.GET_ACCOUNTS};

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String accountName;
    SimpleCursorAdapter listAdapter;
    Cursor cursor;

    @BindView(R.id.view_reminder_list)
    ListView viewReminderList;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;

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

        viewReminderList.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Intent intent = new Intent(this, ReminderReadActivity.class);
            Bundle bundle = new Bundle();
            bundle.putLong("id", id);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_REMINDER);
        });
        swipeRefresh.setOnRefreshListener(()->{
            refreshView();
        });
        swipeRefresh.setProgressViewOffset(false, 0, 52);
        swipeRefresh.setRefreshing(true);
    }

    public void initActivity() {
        if (checkGooglePlayServicesAvailable()) {
            initGoogleAccount();
        } else {
            loadLocalCalendar();
        }
    }

    public void initGoogleAccount() {
        //初始化Google账号
        mGoogleAccount = sharedPreferences.getString(PREF_GOOGLE_ACCOUNT_NAME, null);
        mTimeZone = sharedPreferences.getString(PREF_GOOGLE_CALENDAR_TIMEZONE, null);
        calendarID = sharedPreferences.getString(PREF_GOOGLE_CALENDAR_ID, null);
        credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccountName(mGoogleAccount);
        if (credential.getSelectedAccountName() == null) {
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        } else {
            loadGoogleCalendar();
        }
    }

    public void loadGoogleCalendar() {
        client = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential).setApplicationName("Google-LunarReminder")
                .build();
        if (calendarID == null) {
            new LoadCalendars(this).execute();
        } else {
            getGoogleEvents();
        }

    }

    public void loadLocalCalendar() {
        //初始化列表
        InitLunar initlunar = new InitLunar();
        if (accountName == null) {
            Map accounts = initlunar.getCalendarsAccountNames();
            selectAccount(accounts);
        }

        initlunar.getCalendarID(accountName);
        if (calendarID == null) {
            String accountType = sharedPreferences.getString("accountType", "LOCAL");
            initlunar.addCalendar(accountName, accountType);
        }
        getDtStEnd();
        getLocalEvents();
        listAdapter = new SimpleCursorAdapter(this,
                R.layout.item_reminder,
                cursor,
                new String[]{Events.DTSTART, Events.TITLE},
                new int[]{R.id.reminder_item_date, R.id.reminder_item_title},
                FLAG_REGISTER_CONTENT_OBSERVER);
        listAdapter.setViewBinder(viewBinder);
        viewReminderList.setAdapter(listAdapter);
    }

    private SimpleCursorAdapter.ViewBinder viewBinder = (View view, Cursor cursor, int columnIndex) -> {
        if (cursor.getColumnIndex(Events.DTSTART) == columnIndex) {    //duration为数据库中对应的属性列
            TextView textView = (TextView) view;
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

    public void createGoogleCalender() {
        com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
        calendar.setSummary(caledarName);
        calendar.setTimeZone(mTimeZone);
        new InsertCalendar(this, calendar).execute();
    }

    public void getGoogleEvents() {
        new GetEvents(this).execute();
    }

    public String getAccountName(){
        return accountName;
    }

    public void setCalenderID(String cid) {
        editor.putString(PREF_GOOGLE_CALENDAR_ID, cid);
        editor.commit();
        calendarID = cid;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
        editor.putString(PREF_GOOGLE_CALENDAR_TIMEZONE, timeZone);
        editor.commit();
    }

    public void getDtStEnd() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.MILLISECOND, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        int x = c.get(Calendar.YEAR)*10000+c.get(Calendar.MONTH)*100+c.get(Calendar.DAY_OF_MONTH);
        st = c.getTimeInMillis();
        ChineseCalendar cc = new ChineseCalendar(c);
        cc.add(ChineseCalendar.CHINESE_YEAR, 1);
        int y = (cc.get(Calendar.YEAR))*10000+cc.get(Calendar.MONTH)*100+cc.get(Calendar.DAY_OF_MONTH);
        et = cc.getTimeInMillis();
        if (x+10000 < y) {
            c.add(Calendar.YEAR,1);
            c.get(Calendar.YEAR);
            et = c.getTimeInMillis();
            cc = new ChineseCalendar(c);
            cc.add(ChineseCalendar.CHINESE_YEAR, -1);
            cc.get(Calendar.YEAR);
            st = cc.getTimeInMillis();
        }
    }

    public void getLocalEvents() {
        Uri uri = CalendarContract.Events.CONTENT_URI;
        ContentResolver cr = getContentResolver();
        String selection = new StringBuffer()
                .append(CalendarContract.Events.CALENDAR_ID)
                .append(" = ? and ")
                .append(CalendarContract.Events.DTSTART)
                .append(" < ? and ")
                .append(CalendarContract.Events.DTSTART)
                .append(" >= ? ")
                .toString();
        String[] selectionArgs = new String[]{String.valueOf(calendarID), String.valueOf(et), String.valueOf(st)};
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        cursor = cr.query(uri, null, selection, selectionArgs, CalendarContract.Events.DTSTART);
        return;
    }


    private boolean checkGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
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
            this.startActivityForResult(intent, REQUEST_SETTINGS);
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

    public void refreshView() {
        adapter = new SimpleAdapter(this, list, R.layout.item_reminder,
                new String[]{"start","summary"},
                new int[]{R.id.reminder_item_date, R.id.reminder_item_title});
        viewReminderList.setAdapter(adapter);
        swipeRefresh.setRefreshing(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_REMINDER:
                    cursor.close();
                    getLocalEvents();
                    listAdapter.swapCursor(cursor);
                    listAdapter.notifyDataSetChanged();
                    break;
                case REQUEST_ACCOUNT_PICKER:
                    if (data != null && data.getExtras() != null) {
                        String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                        if (accountName != null) {
                            credential.setSelectedAccountName(accountName);
                            editor.putString(PREF_GOOGLE_ACCOUNT_NAME, accountName);
                            editor.commit();
                            mGoogleAccount = accountName;
                            credential.setSelectedAccountName(mGoogleAccount);
                            loadGoogleCalendar();
                        } else {
                            finish();
                        }
                    }
                    break;
                case REQUEST_AUTHORIZATION:
                    new LoadCalendars(this).execute();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (cursor != null) {
            cursor.close();
        }
        super.onDestroy();
    }

}
