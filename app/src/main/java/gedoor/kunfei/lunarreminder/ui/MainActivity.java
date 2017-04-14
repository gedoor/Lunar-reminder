package gedoor.kunfei.lunarreminder.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import gedoor.kunfei.lunarreminder.sync.DeleteEvents;
import gedoor.kunfei.lunarreminder.sync.GetEvents;
import gedoor.kunfei.lunarreminder.sync.InsertCalendar;
import gedoor.kunfei.lunarreminder.sync.InsertEvents;
import gedoor.kunfei.lunarreminder.sync.LoadCalendars;
import gedoor.kunfei.lunarreminder.sync.UpdateEvents;
import gedoor.kunfei.lunarreminder.data.FinalFields;
import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.ui.view.SimpleAdapterEvent;
import pub.devrel.easypermissions.AfterPermissionGranted;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.calendarID;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.eventRepeat;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvent;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.googleEvents;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

@SuppressLint("WrongConstant")
public class MainActivity extends BaseActivity {
    private static final int REQUEST_REMINDER = 1;
    private static final int REQUEST_SETTINGS = 2;
    public static final int REQUEST_ABOUT = 3;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    private SimpleAdapterEvent adapter;

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

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
            Intent intent = new Intent(this, EventEditActivity.class);
            startActivityForResult(intent, REQUEST_REMINDER);
        });

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
            new GetEvents(this).execute();
        });
        initFinish();
    }

    @Override
    public void initFinish() {
        if (swipeRefresh == null || !initFinish) {
            return;
        }
        swOnRefresh();
        loadGoogleCalendar();
    }

    @Override
    public void syncFinish() {
        refreshView();
    }

    //载入事件
    public void loadGoogleCalendar() {
        Boolean isFirstOpen = sharedPreferences.getBoolean(getString(R.string.pref_key_first_open), true);
        client = new Calendar.Builder(
                transport, jsonFactory, credential).setApplicationName("Google-LunarReminder")
                .build();
        if (calendarID == null) {
            new LoadCalendars(this).execute();
//        } else if (listCache != null && cacheEvents) {
//            googleEvents = listCache;
//            new LoadEventsList(this).execute();
        } else {
            new GetEvents(this).execute();
        }
        if (isFirstOpen) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
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
    public void userRecoverable(UserRecoverableAuthIOException userRecoverableException) {
        super.userRecoverable(userRecoverableException);
        swNoRefresh();
    }

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
                            new InsertEvents(this, calendarID, googleEvent, eventRepeat).execute();
                            break;
                        case FinalFields.OPERATION_UPDATE:
                            new UpdateEvents(this, calendarID, googleEvent, eventRepeat).execute();
                            break;
                        case FinalFields.OPERATION_DELETE:
                            new DeleteEvents(this, calendarID, googleEvent).execute();
                            break;
                    }
                    break;

            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
