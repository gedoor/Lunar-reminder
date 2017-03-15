package gedoor.kunfei.lunarreminder;

import android.Manifest;
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
import android.provider.CalendarContract.Events;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import gedoor.kunfei.lunarreminder.CalendarProvider.initlunar;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;
import static gedoor.kunfei.lunarreminder.LunarReminderApplication.setingFile;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_REMINDER = 1;
    private static final int REQUEST_PERMS = 2;

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
                    Intent intent = new Intent(this, ReminderActivity.class);
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

    }

    public void initActivity() {
        if (accountName == null) {
            Map accounts = new initlunar().getCalendarsAccountNames();
            selectAccount(accounts);
        }

        long calenderID = new initlunar().getCalenderID(accountName);
        if (calenderID == 0) {
            String accountType = sharedPreferences.getString("accountType", "LOCAL");
            new initlunar().addCalender(accountName, accountType);
        }

        Uri uri = Events.CONTENT_URI;
        ContentResolver cr = mContext.getContentResolver();
        String selection = "(" + Events.CALENDAR_ID + " = ?)";
        String[] selectionArgs = new String[]{Long.toString(calenderID)};
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Cursor cursor = cr.query(uri, null, selection, selectionArgs, null);

        if (cursor != null) {
            Log.i("cth", cursor.getColumnIndex(Events.TITLE) + "");
        }

        ListAdapter listAdapter = new SimpleCursorAdapter(this, R.layout.main_reminder_item, cursor,
                new String[]{Events.TITLE},
                new int[]{R.id.ltitle},
                FLAG_REGISTER_CONTENT_OBSERVER);
        viewReminderList.setAdapter(listAdapter);

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
        builder.setTitle("请选择日历账户");
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

}
