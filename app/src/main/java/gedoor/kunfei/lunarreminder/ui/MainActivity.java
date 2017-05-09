package gedoor.kunfei.lunarreminder.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import gedoor.kunfei.lunarreminder.async.DeleteReminderEvents;
import gedoor.kunfei.lunarreminder.async.GetReminderEvents;
import gedoor.kunfei.lunarreminder.async.InsertReminderEvents;
import gedoor.kunfei.lunarreminder.async.InsertSolarTermsEvents;
import gedoor.kunfei.lunarreminder.async.LoadCalendars;
import gedoor.kunfei.lunarreminder.async.LoadReminderEventList;
import gedoor.kunfei.lunarreminder.async.LoadSolarTermsList;
import gedoor.kunfei.lunarreminder.async.UpdateReminderEvents;
import gedoor.kunfei.lunarreminder.data.FinalFields;
import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.data.GEvent;
import gedoor.kunfei.lunarreminder.ui.view.SimpleAdapterEvent;
import gedoor.kunfei.lunarreminder.util.ACache;
import pub.devrel.easypermissions.AfterPermissionGranted;

import static gedoor.kunfei.lunarreminder.App.eventRepeatNum;
import static gedoor.kunfei.lunarreminder.App.eventRepeatType;
import static gedoor.kunfei.lunarreminder.App.getEvents;
import static gedoor.kunfei.lunarreminder.App.googleEvent;
import static gedoor.kunfei.lunarreminder.App.listEvent;
import static gedoor.kunfei.lunarreminder.data.FinalFields.LunarRepeatId;

public class MainActivity extends BaseActivity {
    private static final int REQUEST_REMINDER = 1;
    private static final int REQUEST_SETTINGS = 2;
    public static final int REQUEST_ABOUT = 3;

    private SimpleAdapterEvent adapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private MenuItem menuShowAll;

    @BindView(R.id.list_view_events)
    ListView listViewEvents;
    @BindView(R.id.radioGroupDrawer)
    RadioGroup radioGroupDrawer;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.text_view_version)
    TextView textViewVersion;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setupActionBar();
        initDrawer();
        //悬浮按钮
        fab.setOnClickListener((View view) -> {
            googleEvent = null;
            Intent intent = new Intent(mContext, EventEditActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("position", -1);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_REMINDER);
        });

        adapter = new SimpleAdapterEvent(mContext, list, R.layout.item_event,
                new String[]{"start", "summary"},
                new int[]{R.id.event_item_date, R.id.event_item_title});
        listViewEvents.setAdapter(adapter);

        //列表点击
        listViewEvents.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String mId = list.get(position).get("id");
            if (mId.equals("") | mId.equals(getString(R.string.solar_terms_calendar_name))) {
                return;
            }
            Intent intent = new Intent(mContext, EventReadActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("position", Integer.parseInt(mId));
            bundle.putLong("id", position);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_REMINDER);
        });
        //列表长按
        listViewEvents.setOnItemLongClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String mId = list.get(position).get("id");
            if (mId.equals("") | mId.equals(getString(R.string.solar_terms_calendar_name))) {
                return true;
            }
            PopupMenu popupMenu = new PopupMenu(mContext, view);
            Menu menu = popupMenu.getMenu();
            getMenuInflater().inflate(R.menu.menu_event_list, menu);
            popupMenu.setOnMenuItemClickListener((MenuItem item) -> {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        Intent intent = new Intent(mContext, EventEditActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt("position", Integer.parseInt(mId));
                        bundle.putLong("id", position);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, REQUEST_REMINDER);
                        return true;
                    case R.id.action_delete:
                        swOnRefresh();
                        new DeleteReminderEvents(this, lunarReminderCalendarId, new GEvent(this, listEvent.get(Integer.parseInt(mId))).getLunarRepeatId()).execute();
                        return true;
                }
                return true;
            });
            popupMenu.show();
            return true;
        });
        //下拉刷新
        swipeRefresh.setOnRefreshListener(() -> {
            getCalendarId();
            switch (radioGroupDrawer.getCheckedRadioButtonId()) {
                case R.id.radioButtonReminder:
                    new GetReminderEvents(this, lunarReminderCalendarId).execute();
                    break;
                case R.id.radioButtonSolarTerms:
                    new InsertSolarTermsEvents(this, solarTermsCalendarId).execute();
                    break;
            }
        });
        //切换日历
        radioGroupDrawer.setOnCheckedChangeListener((group, checkedId) -> {
            drawer.closeDrawers();
            swOnRefresh();
            switch (checkedId) {
                case R.id.radioButtonReminder:
                    setTitle(R.string.app_name);
                    showReminderHelp(true);
                    loadReminderCalendar();
                    break;
                case R.id.radioButtonSolarTerms:
                    setTitle(R.string.solar_terms_24);
                    showReminderHelp(false);
                    loadSolarTerms();
                    break;
            }
        });
        textViewVersion.setText(getText(R.string.version) + sharedPreferences.getString("version", null));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // 这个必须要，没有的话进去的默认是个箭头。。正常应该是三横杠的
        mDrawerToggle.syncState();
        //初始化googleAccount
        initGoogleAccount();
    }

    @Override
    public void initFinish() {
        swOnRefresh();
        switch (radioGroupDrawer.getCheckedRadioButtonId()) {
            case R.id.radioButtonReminder:
                setTitle(R.string.app_name);
                loadReminderCalendar();
                break;
            case R.id.radioButtonSolarTerms:
                setTitle(R.string.solar_terms_24);
                loadSolarTerms();
                break;
        }
    }

    @Override
    public void syncSuccess() {

    }

    @Override
    public void syncError() {
        swNoRefresh();
    }

    @Override
    public void eventListFinish() {
        refreshView();
    }

    //载入提醒事件
    public void loadReminderCalendar() {
        Boolean isFirstOpen = sharedPreferences.getBoolean(getString(R.string.pref_key_first_open), true);
        if (sharedPreferences.getBoolean(getString(R.string.pref_key_cache_events), true) && !isFirstOpen) {
            getEvents(mContext);
        }
        getCalendarId();
        if (lunarReminderCalendarId == null) {
            new LoadCalendars(this, getString(R.string.lunar_reminder_calendar_name), getString(R.string.pref_key_lunar_reminder_calendar_id)).execute();
        } else if (listEvent != null) {
            new LoadReminderEventList(this).execute();
        } else {
            new GetReminderEvents(this, lunarReminderCalendarId).execute();
        }
        if (isFirstOpen) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }
    }

    //载入节气
    public void loadSolarTerms() {
        getCalendarId();
        if (solarTermsCalendarId == null) {
            new LoadCalendars(this, getString(R.string.solar_terms_calendar_name), getString(R.string.pref_key_solar_terms_calendar_id)).execute();
        } else {
            ACache mCache = ACache.get(this);
            if (mCache.isExist("jq", ACache.STRING)) {
                new LoadSolarTermsList(this).execute();
            } else {
                new InsertSolarTermsEvents(this, solarTermsCalendarId).execute();
            }
        }
    }
    //侧边栏初始化
    private void initDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerToggle.syncState();
        drawer.addDrawerListener(mDrawerToggle);
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setTitle(@StringRes int title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    private void showReminderHelp(Boolean show) {
        if (show) {
            fab.show();
            menuShowAll.setEnabled(true);
            menuShowAll.setVisible(true);
        } else {
            fab.hide();
            menuShowAll.setEnabled(false);
            menuShowAll.setVisible(false);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        menuShowAll = menu.getItem(0);
        return super.onPrepareOptionsMenu(menu);
    }
    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_showAllEvents:
                showAllEvents = !showAllEvents;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("showAllEvents", showAllEvents);
                editor.apply();
                swOnRefresh();
                getCalendarId();
                new GetReminderEvents(this, lunarReminderCalendarId).execute();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivityForResult(intent, REQUEST_SETTINGS);
                return true;
            case R.id.action_about:
                Intent intent_about = new Intent(this, AboutActivity.class);
                this.startActivityForResult(intent_about, REQUEST_ABOUT);
                return true;
            case android.R.id.home:
                if (drawer.isDrawerOpen(GravityCompat.START)
                        ) {
                    drawer.closeDrawers();
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        adapter.notifyDataSetChanged();
        swNoRefresh();
    }

    @Override
    public void userRecoverable() {
        swNoRefresh();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    @SuppressLint("WrongConstant")
    @AfterPermissionGranted(REQUEST_PERMS)
    private void methodRequiresPermission() {
        afterPermissionGranted();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_REMINDER:
                    swOnRefresh();
                    Bundle bundle = data.getExtras();
                    switch (bundle.getInt(FinalFields.OPERATION)) {
                        case FinalFields.OPERATION_INSERT:
                            new InsertReminderEvents(this, lunarReminderCalendarId, googleEvent, eventRepeatType, eventRepeatNum).execute();
                            break;
                        case FinalFields.OPERATION_UPDATE:
                            new UpdateReminderEvents(this, lunarReminderCalendarId, googleEvent, eventRepeatType, eventRepeatNum).execute();
                            break;
                        case FinalFields.OPERATION_DELETE:
                            new DeleteReminderEvents(this, lunarReminderCalendarId, googleEvent.getExtendedProperties().getPrivate().get(LunarRepeatId)).execute();
                            break;
                    }
                    break;

            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawers();
                return true;
            }
        }
        return super.onKeyDown(keyCode, keyEvent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
