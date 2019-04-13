package gedoor.kunfei.lunarreminder.ui.activity;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.util.PermissionUtils;

/**
 * Created by GKF on 2017/4/12.
 * 农历初始化
 */

public abstract class  BaseActivity extends AppCompatActivity {
    public static final int REQUEST_PERMS = 101;
    public static final int REQUEST_ACCOUNT_PICKER = 102;
    public static final int REQUEST_AUTHORIZATION = 103;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = new AndroidJsonFactory();
    final String[] perms = {Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_PHONE_STATE};

    public Context mContext;
    public SharedPreferences sharedPreferences;
    public String lunarReminderCalendarId;
    public String solarTermsCalendarId;
    public ArrayList<HashMap<String, String>> list = new ArrayList<>();
    public GoogleAccountCredential credential;
    public Calendar client;
    public String mGoogleAccount;
    public boolean showAllEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        showAllEvents = sharedPreferences.getBoolean("showAllEvents", false);
        getCalendarId();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void initGoogleAccount() {
        PermissionUtils.checkMorePermissions(this, perms, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                //检查google服务
                if (!checkGooglePlayServicesAvailable()) {
                    Toast.makeText(mContext, R.string.no_google_services, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                //初始化Google账号
                mGoogleAccount = mGoogleAccount == null ? sharedPreferences.getString(getString(R.string.pref_key_google_account), null) : mGoogleAccount;
                credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(CalendarScopes.CALENDAR));
                credential.setSelectedAccountName(mGoogleAccount);
                if (credential.getSelectedAccountName() == null) {
                    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                    return;
                }
                client = new Calendar.Builder(transport, jsonFactory, credential)
                        .setApplicationName("Google-LunarReminder")
                        .build();
                initFinish();
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                Toast.makeText(BaseActivity.this, "请给于电话和账户访问权限", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAlreadyTurnedDownAndNoAsk(String... permission) {
                PermissionUtils.requestMorePermissions(BaseActivity.this, permission, REQUEST_PERMS);
            }
        });
    }

    public void initFinish() {

    }
    public void syncStart() {

    }
    public void syncSuccess() {

    }
    public void syncError() {

    }
    public void eventListFinish() {

    }
    public void userRecoverable() {

    }

    public void loadReminderCalendar() {

    }

    public void loadSolarTerms() {

    }

    public void getCalendarId() {
        lunarReminderCalendarId = sharedPreferences.getString(getString(R.string.pref_key_lunar_reminder_calendar_id), null);
        solarTermsCalendarId = sharedPreferences.getString(getString(R.string.pref_key_solar_terms_calendar_id), null);
    }
    //检测google服务
    private boolean checkGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(mContext);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.checkMorePermissions(this, perms, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                //检查google服务
                if (!checkGooglePlayServicesAvailable()) {
                    Toast.makeText(mContext, R.string.no_google_services, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                //初始化Google账号
                mGoogleAccount = mGoogleAccount == null ? sharedPreferences.getString(getString(R.string.pref_key_google_account), null) : mGoogleAccount;
                credential = GoogleAccountCredential.usingOAuth2(mContext, Collections.singleton(CalendarScopes.CALENDAR));
                credential.setSelectedAccountName(mGoogleAccount);
                if (credential.getSelectedAccountName() == null) {
                    startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                    return;
                }
                client = new Calendar.Builder(transport, jsonFactory, credential)
                        .setApplicationName("Google-LunarReminder")
                        .build();
                initFinish();
            }

            @Override
            public void onUserHasAlreadyTurnedDown(String... permission) {
                PermissionUtils.requestMorePermissions(BaseActivity.this, permission, REQUEST_PERMS);
            }

            @Override
            public void onAlreadyTurnedDownAndNoAsk(String... permission) {
                Toast.makeText(mContext, "没有权限,程序无法使用", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ACCOUNT_PICKER:
                    if (data != null && data.getExtras() != null) {
                        String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                        if (accountName != null) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            credential.setSelectedAccountName(accountName);
                            editor.putString(getString(R.string.pref_key_google_account), accountName);
                            editor.apply();
                            mGoogleAccount = accountName;
                            credential.setSelectedAccountName(mGoogleAccount);
                            initGoogleAccount();
                        } else {
                            Toast.makeText(mContext, "无法获取google用户,将退出", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                    break;
                case REQUEST_AUTHORIZATION:
                    initGoogleAccount();
                    break;
            }
        }
    }
}
