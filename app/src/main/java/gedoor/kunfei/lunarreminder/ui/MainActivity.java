package gedoor.kunfei.lunarreminder.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import gedoor.kunfei.lunarreminder.async.DeleteEvents;
import gedoor.kunfei.lunarreminder.async.GetCalendar;
import gedoor.kunfei.lunarreminder.async.GetLunarReminderEvents;
import gedoor.kunfei.lunarreminder.async.InsertEvents;
import gedoor.kunfei.lunarreminder.async.InsertSolarTermsEvents;
import gedoor.kunfei.lunarreminder.async.LoadCalendars;
import gedoor.kunfei.lunarreminder.async.LoadSolarTermsList;
import gedoor.kunfei.lunarreminder.async.UpdateEvents;
import gedoor.kunfei.lunarreminder.data.FinalFields;
import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.ui.view.SimpleAdapterEvent;
import gedoor.kunfei.lunarreminder.util.ACache;
import pub.devrel.easypermissions.AfterPermissionGranted;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.eventRepeat;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvent;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvents;

public class MainActivity extends BaseActivity {
    private static final int REQUEST_REMINDER = 1;
    private static final int REQUEST_SETTINGS = 2;
    public static final int REQUEST_ABOUT = 3;

    private SharedPreferences sharedPreferences;
    private SimpleAdapterEvent adapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayList<String> listDrawer = new ArrayList<>();
    private String lunarReminderCalendarId;
    private String solarTermsCalendarId;
    private Boolean isFirstOpen;
    private int drawerSelected;

    @BindView(R.id.list_view_events)
    ListView listViewEvents;
    @BindView(R.id.listViewDrawer)
    ListView listViewDrawer;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.drawer)
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isFirstOpen = sharedPreferences.getBoolean(getString(R.string.pref_key_first_open), true);

        setupActionBar();
        initDrawer();
        //悬浮按钮
        fab.setOnClickListener((View view) -> {
            googleEvent = null;
            Intent intent = new Intent(this, EventEditActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt("position", -1);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_REMINDER);
        });

        adapter = new SimpleAdapterEvent(this, list, R.layout.item_event,
                new String[]{"start", "summary"},
                new int[]{R.id.event_item_date, R.id.event_item_title});
        listViewEvents.setAdapter(adapter);

        //列表点击
        listViewEvents.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String mId = list.get(position).get("id");
            if (mId.equals("")) {
                return;
            }
            Intent intent = new Intent(this, EventReadActivity.class);
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
                        Intent intent = new Intent(this, EventEditActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putInt("position", Integer.parseInt(mId));
                        bundle.putLong("id", position);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, REQUEST_REMINDER);
                        return true;
                    case Menu.FIRST + 1:
                        swOnRefresh();
                        new DeleteEvents(this, lunarReminderCalendarId, googleEvents.get(Integer.parseInt(mId))).execute();
                        return true;
                }
                return true;
            });
            popupMenu.show();
            return true;
        });
        //下拉刷新
        swipeRefresh.setOnRefreshListener(() -> {
            if (drawerSelected == 1) {
                solarTermsCalendarId = sharedPreferences.getString(getString(R.string.pref_key_solar_terms_calendar_id), null);
                new InsertSolarTermsEvents(this, solarTermsCalendarId).execute();
            } else {
                lunarReminderCalendarId = sharedPreferences.getString(getString(R.string.pref_key_lunar_reminder_calendar_id), null);
                new GetCalendar(this, lunarReminderCalendarId).execute();
                new GetLunarReminderEvents(this, lunarReminderCalendarId).execute();
            }
        });
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
        loadReminderCalendar();
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
        lunarReminderCalendarId = sharedPreferences.getString(getString(R.string.pref_key_lunar_reminder_calendar_id), null);
        if (lunarReminderCalendarId == null) {
            new LoadCalendars(this, getString(R.string.lunar_reminder_calendar_name), getString(R.string.pref_key_lunar_reminder_calendar_id)).execute();
//        } else if (listCache != null && cacheEvents) {
//            googleEvents = listCache;
//            new LoadEventsList(this).execute();
        } else {
            lunarReminderCalendarId = sharedPreferences.getString(getString(R.string.pref_key_lunar_reminder_calendar_id), null);
            new GetCalendar(this, lunarReminderCalendarId).execute();
            new GetLunarReminderEvents(this, lunarReminderCalendarId).execute();
        }
        if (isFirstOpen) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        }
    }

    //载入节气
    public void loadSolarTerms() {
        solarTermsCalendarId = sharedPreferences.getString(getString(R.string.pref_key_solar_terms_calendar_id), null);
        if (solarTermsCalendarId == null) {
            swOnRefresh();
            new LoadCalendars(this, getString(R.string.solar_terms_calendar_name), getString(R.string.pref_key_solar_terms_calendar_id)).execute();
        } else {
            swOnRefresh();
            ACache mCache = ACache.get(this);
            if (mCache.isExist("jq", ACache.STRING)) {
                new LoadSolarTermsList(this).execute();
            } else {
                new LoadCalendars(this, getString(R.string.solar_terms_calendar_name), getString(R.string.pref_key_solar_terms_calendar_id)).execute();
            }
        }
    }
    //侧边栏初始化
    private void initDrawer() {
        listViewDrawer.setSelector(R.color.colorLightGrey);
        mDrawerToggle = new ActionBarDrawerToggle(this, drawer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawerToggle.syncState();
        drawer.addDrawerListener(mDrawerToggle);
        listDrawer.add("农历提醒");
        listDrawer.add("廿四节气");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_expandable_list_item_1,
                listDrawer);
        listViewDrawer.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listViewDrawer.post(new Runnable() {
            @Override
            public void run() {
                listViewDrawer.setItemChecked(0, true);;
                listViewDrawer.performItemClick(listViewDrawer.getSelectedView(), 0, 0);
            }
        });
        listViewDrawer.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 1) {
                loadSolarTerms();
            }
            drawerSelected = position;
            drawer.closeDrawers();
        });
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    // 添加菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    //菜单
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_showAllEvents:
                showAllEvents = !showAllEvents;
                swOnRefresh();
                new GetLunarReminderEvents(this, lunarReminderCalendarId).execute();
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
                            new InsertEvents(this, lunarReminderCalendarId, googleEvent, eventRepeat).execute();
                            break;
                        case FinalFields.OPERATION_UPDATE:
                            new UpdateEvents(this, lunarReminderCalendarId, googleEvent, eventRepeat).execute();
                            break;
                        case FinalFields.OPERATION_DELETE:
                            new DeleteEvents(this, lunarReminderCalendarId, googleEvent).execute();
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
