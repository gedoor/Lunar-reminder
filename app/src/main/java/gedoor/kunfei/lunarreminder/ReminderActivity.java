package gedoor.kunfei.lunarreminder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import gedoor.kunfei.lunarreminder.Data.ChineseCalendar;
import gedoor.kunfei.lunarreminder.view.DialogGLC;

/**
 * Created by GKF on 2017/3/7.
 */

public class ReminderActivity extends AppCompatActivity {

    @BindView(R.id.swallday)
    Switch swallday;
    @BindView(R.id.vwchinesedate)
    TextView vwchinesedate;
    @BindView(R.id.vwtime)
    TextView vwtime;

    private DialogGLC mDialog;
    private ChineseCalendar cc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.reminder_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener((View view) -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        initReminder();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_reminder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            this.setResult(RESULT_OK);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.swallday, R.id.vwchinesedate, R.id.vwtime})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.swallday:
                break;
            case R.id.vwchinesedate:
                    selectDate();
                break;
            case R.id.vwtime:
                break;
        }
    }

    private void selectDate(){
        if(mDialog == null){
            mDialog = new DialogGLC(this);
        }
        if(mDialog.isShowing()){
            mDialog.dismiss();
        }else {
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(true);
            mDialog.show();
            mDialog.setDialogResult((String result) ->{
                vwchinesedate.setText(result);
            });
            mDialog.initCalendar(cc, false);
        }
    }

    private void initReminder() {
        cc = new ChineseCalendar(Calendar.getInstance());
        swallday.setChecked(true);
        vwchinesedate.setText(cc.getChinese(ChineseCalendar.CHINESE_MONTH) + cc.getChinese(ChineseCalendar.CHINESE_DATE));
    }
}
