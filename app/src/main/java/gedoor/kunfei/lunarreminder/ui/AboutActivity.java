package gedoor.kunfei.lunarreminder.ui;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gedoor.kunfei.lunarreminder.R;
import gedoor.kunfei.lunarreminder.help.InitTheme;


public class AboutActivity extends AppCompatActivity {
    @BindView(R.id.zfb)
    TextView zfb;
    @BindView(R.id.weXin)
    TextView weXin;
    @BindView(R.id.text_about_version)
    TextView textViewVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new InitTheme(this, true);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        setupActionBar();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(getString(R.string.pref_key_first_open), true)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.pref_key_first_open), false);
            editor.commit();
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
        } else if (id == R.id.action_score) {
            String mAddress = "market://details?id=" + getPackageName();
            Intent marketIntent = new Intent("android.intent.action.VIEW");
            marketIntent.setData(Uri.parse(mAddress ));
            startActivity(marketIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.zfb, R.id.weXin})
    public void onViewClicked(View view) {
        ClipboardManager clipboardManager = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        switch (view.getId()) {
            case R.id.zfb:
                Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();
                clipboardManager.setPrimaryClip(ClipData.newPlainText("支付宝", "gekunfei@qq.com"));
                break;
            case R.id.weXin:
                Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();
                clipboardManager.setPrimaryClip(ClipData.newPlainText("微信", "kunfei_ge"));
                break;
        }
    }
}
