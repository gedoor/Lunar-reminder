package gedoor.kunfei.lunarreminder.ui;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.data.FinalFields;
import gedoor.kunfei.lunarreminder.async.InsertCalendar;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by GKF on 2017/4/12.
 * 农历初始化
 */

public abstract class BaseActivity extends AppCompatActivity {
    public static final int REQUEST_PERMS = 101;
    public static final int REQUEST_ACCOUNT_PICKER = 102;
    public static final int REQUEST_AUTHORIZATION = 103;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();
    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    public ArrayList<HashMap<String, String>> list = new ArrayList<>();
    public GoogleAccountCredential credential;
    public Calendar client;
    public String mGoogleAccount;
    String[] perms = {Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_PHONE_STATE};
    public boolean showAllEvents = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    public void initGoogleAccount() {
        //get permission
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "get permissions", REQUEST_PERMS, perms);
            return;
        }
        //检查google服务
        if (!checkGooglePlayServicesAvailable()) {
            Toast.makeText(this, "检测不到Google服务,程序无法使用", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //初始化Google账号
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mGoogleAccount = mGoogleAccount == null ? sharedPreferences.getString(getString(R.string.pref_key_google_account), null) : mGoogleAccount;
        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
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
            initGoogleAccount();
        } else {
            Toast.makeText(this, "没有权限,程序无法使用", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            credential.setSelectedAccountName(accountName);
                            editor.putString(getString(R.string.pref_key_google_account), accountName);
                            editor.apply();
                            mGoogleAccount = accountName;
                            credential.setSelectedAccountName(mGoogleAccount);
                            initGoogleAccount();
                        } else {
                            Toast.makeText(this, "无法获取google用户,将退出", Toast.LENGTH_LONG).show();
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
