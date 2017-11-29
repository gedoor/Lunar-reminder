package gedoor.kunfei.lunarreminder.help;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import gedoor.kunfei.lunarreminder.R;

/**
 * Created by GKF on 2017/7/24.
 * customize theme
 */

public class InitTheme {
    int themeRes;
    SharedPreferences sharedPreferences;

    public InitTheme(Context context, Boolean hasActionBar) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = sharedPreferences.getString(context.getString(R.string.pref_key_app_theme), "default_color");

        switch (theme) {
            case "default_color":
                if (hasActionBar) {
                    themeRes = R.style.AppTheme;
                    context.setTheme(R.style.AppTheme);
                } else {
                    themeRes = R.style.AppTheme;
                    context.setTheme(R.style.AppTheme_NoActionBar);
                }
                break;
            case "tangerine":
                if (hasActionBar) {
                    themeRes = R.style.AppTheme;
                    context.setTheme(R.style.AppTheme_tangerine);
                } else {
                    themeRes = R.style.AppTheme;
                    context.setTheme(R.style.AppTheme_tangerine_NoActionBar);
                }
                break;
            case "tomato":
                if (hasActionBar) {
                    themeRes = R.style.AppTheme;
                    context.setTheme(R.style.AppTheme_tomato);
                } else {
                    themeRes = R.style.AppTheme;
                    context.setTheme(R.style.AppTheme_tomato_NoActionBar);
                }
                break;
            case "banana":
                if (hasActionBar) {
                    themeRes = R.style.AppTheme;
                    context.setTheme(R.style.AppTheme_banana);
                } else {
                    themeRes = R.style.AppTheme;
                    context.setTheme(R.style.AppTheme_banana_NoActionBar);
                }
                break;
            case "peacock":
                if (hasActionBar) {
                    themeRes = R.style.AppTheme;
                    context.setTheme(R.style.AppTheme_peacock);
                } else {
                    themeRes = R.style.AppTheme;
                    context.setTheme(R.style.AppTheme_peacock_NoActionBar);
                }
                break;
        }

    }

    public int getThemeRes() {
        return themeRes;
    }
}
