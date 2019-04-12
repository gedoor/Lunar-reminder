package gedoor.kunfei.lunarreminder.ui;


import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.help.DonateByAliPay;
import gedoor.kunfei.lunarreminder.help.InitTheme;

import static gedoor.kunfei.lunarreminder.App.qrCodeAliPay;


public class AboutActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;

    @BindView(R.id.main_toolbar)
    Toolbar toolBar;
    @BindView(R.id.zfb)
    TextView zfb;
    @BindView(R.id.text_about_version)
    TextView textViewVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new InitTheme(this, false);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        setSupportActionBar(toolBar);
        setupActionBar();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(getString(R.string.pref_key_first_open), true)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.pref_key_first_open), false);
            editor.apply();
        }
        textViewVersion.setText(sharedPreferences.getString("version", "0.0.0"));
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
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        } else if (id == R.id.action_Scoring) {
            String mAddress = "market://details?id=" + getPackageName();
            Intent marketIntent = new Intent("android.intent.action.VIEW");
            marketIntent.setData(Uri.parse(mAddress ));
            startActivity(marketIntent);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.pref_key_apk_score), true);
            editor.apply();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.zfb})
    public void onViewClicked(View view) {
        ClipboardManager clipboardManager = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        switch (view.getId()) {
            case R.id.zfb:
                DonateByAliPay.openAlipayPayPage(this, qrCodeAliPay);
                break;
        }
    }
}
