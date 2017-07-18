package gedoor.kunfei.lunarreminder.help;

import com.google.api.services.calendar.model.EventReminder;

import java.util.Locale;

/**
 * Created by GKF on 2017/4/10.
 * Reminder转化
 */

public class ReminderHelp {
    private String method;
    private int tqDay;
    private int txHour;
    private int txMinutesByHour;

    public ReminderHelp(EventReminder reminder) {
        this.method = reminder.getMethod();
        int tqMinutes = reminder.getMinutes();
        tqDay = tqMinutes%1440 == 0 ? tqMinutes/1440 : tqMinutes/1440 + 1;
        int txMinutes = tqMinutes%1440 == 0 ? 0 : 1440 - tqMinutes%1440;
        txHour = txMinutes/60;
        txMinutesByHour = txMinutes%60;
    }

    public ReminderHelp(String method, Double minutes ) {
        this.method = method;
        int tqMinutes = minutes.intValue();
        tqDay = tqMinutes%1440 == 0 ? tqMinutes/1440 : tqMinutes/1440 + 1;
        int txMinutes = tqMinutes%1440 == 0 ? 0 : 1440 - tqMinutes%1440;
        txHour = txMinutes/60;
        txMinutesByHour = txMinutes%60;
    }

    public String getTitle() {
        String txType = method.equals("email") ? "通过邮件" : "";
        return "提前" + tqDay + "天" + String.format(Locale.CHINA,"%d:%02d", txHour, txMinutesByHour) + txType;
    }

    public int getTqDay() {
        return tqDay;
    }

    public int getTxHour() {
        return txHour;
    }

    public int getTxMinutesByHour() {
        return txMinutesByHour;
    }

    public String getTime() {
        return String.format(Locale.CHINA, "%02d:%02d", txHour, txMinutesByHour);
    }
}
