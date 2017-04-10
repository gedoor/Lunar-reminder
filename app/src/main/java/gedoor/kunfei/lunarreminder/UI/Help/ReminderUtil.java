package gedoor.kunfei.lunarreminder.UI.Help;

import com.google.api.services.calendar.model.EventReminder;

import java.util.Locale;

/**
 * Created by GKF on 2017/4/10.
 */

public class ReminderUtil {
    EventReminder reminder;
    int tqDay;
    int txHour;
    int txMinutesByHour;

    public ReminderUtil(EventReminder reminder) {
        this.reminder = reminder;
        int tqMinutes = reminder.getMinutes();
        tqDay = tqMinutes%1440 == 0 ? tqMinutes/1440 : tqMinutes/1440 + 1;
        int txMinutes = tqMinutes%1440 == 0 ? 0 : 1440 - tqMinutes%1440;
        txHour = txMinutes/60;
        txMinutesByHour = txMinutes%60;
    }

    public String getTitle() {
        String txType = reminder.getMethod().equals("mail") ? "通过邮件" : "";
        String txTitle = "提前" + tqDay + "天" + String.format(Locale.CHINA,"%d:%02d", txHour, txMinutesByHour) + txType;
        return txTitle;
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
}
