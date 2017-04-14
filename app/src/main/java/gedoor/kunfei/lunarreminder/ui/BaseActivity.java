package gedoor.kunfei.lunarreminder.ui;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.data.FinalFields;
import gedoor.kunfei.lunarreminder.sync.InsertCalendar;
import pub.devrel.easypermissions.EasyPermissions;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;

/**
 * Created by GKF on 2017/4/12.
 */

public class BaseActivity extends AppCompatActivity {
    public static final int REQUEST_PERMS = 101;
    public static final int REQUEST_ACCOUNT_PICKER = 102;
    public static final int REQUEST_AUTHORIZATION = 103;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    public ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
    public boolean initFinish = false;
    public GoogleAccountCredential credential;
    public Calendar client;
    public String mGoogleAccount;
    String[] perms = {Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_PHONE_STATE};
    public boolean showAllEvents = false;

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    SharedPreferences.Editor editor = sharedPreferences.edit();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get permission
        if (EasyPermissions.hasPermissions(this, perms)) {
            init();
        } else {
            EasyPermissions.requestPermissions(this, "get permissions", REQUEST_PERMS, perms);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void init() {
        if (checkGooglePlayServicesAvailable()) {
            initGoogleAccount();
        } else {
            Toast.makeText(this, "检测不到Google服务,程序无法使用", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void initGoogleAccount() {
        //初始化Google账号
        mGoogleAccount = sharedPreferences.getString(getString(R.string.pref_key_google_account), null);
        credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(CalendarScopes.CALENDAR));
        credential.setSelectedAccountName(mGoogleAccount);
        if (credential.getSelectedAccountName() == null) {
            startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        } else {
            initFinish = true;
            initFinish();
        }
    }

    public void initFinish() {
        client = new Calendar.Builder(
                transport, jsonFactory, credential).setApplicationName("Google-LunarReminder")
                .build();
    }

    public void syncStart() {

    }
    public void syncFinish() {

    }
    public void eventListFinish() {

    }

    public void userRecoverable(UserRecoverableAuthIOException userRecoverableException) {
        startActivityForResult(userRecoverableException.getIntent(), REQUEST_AUTHORIZATION);
    }

    //插入农历提醒日历
    public void createGoogleCalender() {
        com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
        calendar.setSummary(FinalFields.CalendarName);

        new InsertCalendar(this, calendar).execute();
    }

    //检测google服务
    private boolean checkGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    public void afterPermissionGranted() {
        if (EasyPermissions.hasPermissions(this, perms)) {
            init();
        } else {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ACCOUNT_PICKER:
                    if (data != null && data.getExtras() != null) {
                        String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                        if (accountName != null) {
                            credential.setSelectedAccountName(accountName);
                            editor.putString(getString(R.string.pref_key_google_account), accountName);
                            editor.commit();
                            mGoogleAccount = accountName;
                            credential.setSelectedAccountName(mGoogleAccount);
                            initFinish = true;
                            initFinish();
                        } else {
                            Toast.makeText(this, "无法获取google用户,将退出", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                    break;
                case REQUEST_AUTHORIZATION:
                    initFinish = true;
                    initFinish();
                    break;
            }
        }
    }
}
