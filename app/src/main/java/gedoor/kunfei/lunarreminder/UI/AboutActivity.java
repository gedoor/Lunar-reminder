package gedoor.kunfei.lunarreminder.UI;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gedoor.kunfei.lunarreminder.R;

import static gedoor.kunfei.lunarreminder.LunarReminderApplication.mContext;


public class AboutActivity extends AppCompatActivity {
    ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);

    @BindView(R.id.zfb)
    TextView zfb;
    @BindView(R.id.weXin)
    TextView weXin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);

        setupActionBar();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @OnClick({R.id.zfb, R.id.weXin})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.zfb:
                Toast.makeText(mContext, "复制成功", Toast.LENGTH_SHORT).show();
                clipboardManager.setPrimaryClip(ClipData.newPlainText("支付宝", "gekunfei@qq.com"));
                break;
            case R.id.weXin:
                Toast.makeText(mContext, "复制成功", Toast.LENGTH_SHORT).show();
                clipboardManager.setPrimaryClip(ClipData.newPlainText("微信", "kunfei_ge"));
                break;
        }
    }
}
