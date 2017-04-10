package gedoor.kunfei.lunarreminder.UI;

import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import gedoor.kunfei.lunarreminder.Async.DeleteEvents;
import gedoor.kunfei.lunarreminder.Async.GetEvents;
import gedoor.kunfei.lunarreminder.Async.InsertCalendar;
import gedoor.kunfei.lunarreminder.Async.InsertEvents;
import gedoor.kunfei.lunarreminder.Async.LoadCalendars;
import gedoor.kunfei.lunarreminder.Async.UpdateEvents;
import gedoor.kunfei.lunarreminder.Data.FinalFields;
import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.UI.view.SimpleAdapterEvent;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarType;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.eventRepeat;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvent;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvents;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

@SuppressLint("WrongConstant")
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_REMINDER = 1;
    private static final int REQUEST_SETTINGS = 2;
    private static final int REQUEST_PERMS = 3;
    private static final int REQUEST_ACCOUNT_PICKER = 4;
    public static final int REQUEST_AUTHORIZATION = 5;
    public static final int REQUEST_ABOUT = 6;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    private String mGoogleAccount;
    private String mTimeZone;
    public GoogleAccountCredential credential;
    public Calendar client;
    public ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
    public boolean showAllEvents = false;
    public int numAsyncTasks = 0;
    private SimpleAdapterEvent adapter;

    String[] perms = {Manifest.permission.GET_ACCOUNTS};

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    SharedPreferences.Editor editor = sharedPreferences.edit();
    ;
    String accountName;

    @BindView(R.id.list_view_events)
    ListView listViewEvents;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab.setOnClickListener((View view) -> {
            googleEvent = null;
            Intent intent = new Intent(this, ReminderEditActivity.class);
            startActivityForResult(intent, REQUEST_REMINDER);
        });

        //get permission
        if (EasyPermissions.hasPermissions(this, perms)) {
            initActivity();
        } else {
            EasyPermissions.requestPermissions(this, "get permissions", REQUEST_PERMS, perms);
        }
        //列表点击
        listViewEvents.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String mId = list.get(position).get("id");
            if (mId.equals("")) {
                return;
            }
            Intent intent = new Intent(this, ReminderReadActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("position", Integer.parseInt(mId));
            bundle.putLong("id", position);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_REMINDER);
        });
        //列表长按
        listViewEvents.setOnItemLongClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String mId = list.get(position).get("id");
            if (mId.equals("")) {
                return true;
            }
            PopupMenu popupMenu = new PopupMenu(this, view);
            Menu menu = popupMenu.getMenu();
            menu.add(Menu.NONE, Menu.FIRST, 0, "修改");
            menu.add(Menu.NONE, Menu.FIRST + 1, 1, "删除");
            popupMenu.setOnMenuItemClickListener((MenuItem item) -> {
                switch (item.getItemId()) {
                    case Menu.FIRST:
                        Intent intent = new Intent(this, ReminderEditActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt("position", Integer.parseInt(mId));
                        bundle.putLong("id", position);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, REQUEST_REMINDER);
                        return true;
                    case Menu.FIRST + 1:
                        new DeleteEvents(this, calendarID, googleEvents.get(Integer.parseInt(mId))).execute();
                        return true;
                }
                return true;
            });
            popupMenu.show();
            return true;
        });
        //下拉刷新
        swipeRefresh.setOnRefreshListener(() -> {
            if (calendarType.equals(FinalFields.CalendarTypeGoogle)) {
                getGoogleEvents();
            }

        });

    }

    public void initActivity() {
        if (checkGooglePlayServicesAvailable()) {
            calendarType = FinalFields.CalendarTypeGoogle;
            editor.putString(FinalFields.PREF_CALENDAR_TYPE, calendarType);
            editor.commit();
            initGoogleAccount();
        } else {
            Toast.makeText(this, "检测不到Google服务,程序无法使用", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void initGoogleAccount() {
        //初始化Google账号
        mGoogleAccount = sharedPreferences.getString(getString(R.string.pref_key_google_account), null);
        mTimeZone = sharedPreferences.getString(FinalFields.PREF_GOOGLE_CALENDAR_TIMEZONE, null);
        credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccountName(mGoogleAccount);
        if (credential.getSelectedAccountName() == null) {
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        } else {
            swOnRefresh();
            loadGoogleCalendar();
        }
    }

    //载入事件
    public void loadGoogleCalendar() {
        Boolean cacheEvents = sharedPreferences.getBoolean(getString(R.string.pref_key_cache_events), true);
        client = new Calendar.Builder(
                transport, jsonFactory, credential).setApplicationName("Google-LunarReminder")
                .build();
        if (calendarID == null) {
            new LoadCalendars(this).execute();
//        } else if (listCache != null && cacheEvents) {
//            googleEvents = listCache;
//            new LoadEventsList(this).execute();
        } else {
            getGoogleEvents();
        }

    }

    //插入农历提醒日历
    public void createGoogleCalender() {
        com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
        calendar.setSummary(FinalFields.CalendarName);
        calendar.setTimeZone(mTimeZone);

        new InsertCalendar(this, calendar).execute();
    }

    public void getGoogleEvents() {
        new GetEvents(this).execute();
    }

    public String getAccountName() {
        return accountName;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
        editor.putString(FinalFields.PREF_GOOGLE_CALENDAR_TIMEZONE, timeZone);
        editor.commit();
    }

    //检测google服务
    private boolean checkGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 添加菜单
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_showAllEvents:
                showAllEvents = showAllEvents ? false : true;
                swOnRefresh();
                new GetEvents(this).execute();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivityForResult(intent, REQUEST_SETTINGS);
                return true;
            case R.id.action_about:
                Intent intent_about = new Intent(this, AboutActivity.class);
                this.startActivityForResult(intent_about, REQUEST_ABOUT);
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

    @AfterPermissionGranted(REQUEST_PERMS)
    private void methodRequiresPermission() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            initActivity();
        } else {
            finish();
        }
    }

    //刷新动画开始
    public void swOnRefresh() {
        swipeRefresh.setProgressViewOffset(false, 0, 52);
        swipeRefresh.setRefreshing(true);
    }

    //刷新动画停止
    public void swNoRefresh() {
        swipeRefresh.setRefreshing(false);
    }

    //刷新事件列表
    public void refreshView() {
        adapter = new SimpleAdapterEvent(this, list, R.layout.item_event,
                new String[]{"start", "summary"},
                new int[]{R.id.event_item_date, R.id.event_item_title});
        listViewEvents.setAdapter(adapter);
        swNoRefresh();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_REMINDER:
                    if (calendarType.equals(FinalFields.CalendarTypeGoogle)) {
                        swOnRefresh();
                        Bundle bundle = data.getExtras();
                        switch (bundle.getInt(FinalFields.OPERATION)) {
                            case FinalFields.OPERATION_INSERT:
                                new InsertEvents(this, calendarID, googleEvent, eventRepeat).execute();
                                break;
                            case FinalFields.OPERATION_UPDATE:
                                new UpdateEvents(this, calendarID, googleEvent, eventRepeat).execute();
                                break;
                            case FinalFields.OPERATION_DELETE:
                                new DeleteEvents(this, calendarID, googleEvent).execute();
                                break;
                        }

                    }
                    break;
                case REQUEST_ACCOUNT_PICKER:
                    if (data != null && data.getExtras() != null) {
                        String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                        if (accountName != null) {
                            credential.setSelectedAccountName(accountName);
                            editor.putString(getString(R.string.pref_key_google_account), accountName);
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
                    swOnRefresh();
                    new LoadCalendars(this).execute();
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

}
