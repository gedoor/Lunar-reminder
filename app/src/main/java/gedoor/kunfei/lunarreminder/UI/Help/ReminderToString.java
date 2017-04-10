package gedoor.kunfei.lunarreminder.UI.Help;

import com.google.api.services.calendar.model.EventReminder;

import java.util.Locale;

/**
 * Created by GKF on 2017/4/10.
 */

public class ReminderToString {
    EventReminder reminder;

    public ReminderToString(EventReminder reminder) {
        this.reminder = reminder;
    }

    public String getTitle() {
        String txType = reminder.getMethod().equals("mail") ? "通过邮件" : "";
        int tqMinutes = reminder.getMinutes();
        int tqDay = tqMinutes%1440 == 0 ? tqMinutes/1440 : tqMinutes/1440 + 1;
        int txMinutes = tqMinutes%1440 == 0 ? 0 : 1440 - tqMinutes%1440;
        int txHour = txMinutes/60;
        int txMinutesByHour = txMinutes%60;
        String txTitle = "提前" + tqDay + "天" + String.format(Locale.CHINA,"%2d:%02d", txHour, txMinutesByHour) + txType;

        return txTitle;
    }
}
