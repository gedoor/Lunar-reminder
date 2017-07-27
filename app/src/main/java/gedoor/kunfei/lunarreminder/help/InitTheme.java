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
    SharedPreferences sharedPreferences;

    public InitTheme(Context context, Boolean hasActionBar) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = sharedPreferences.getString(context.getString(R.string.pref_key_app_theme), "default_color");
        if (theme.equals("default_color")) {
            return;
        }

        switch (theme) {
            case "tangerine":
                if (hasActionBar) {
                    context.setTheme(R.style.AppTheme_tangerine);
                } else {
                    context.setTheme(R.style.AppTheme_tangerine_NoActionBar);
                }
                break;
            case "tomato":
                if (hasActionBar) {
                    context.setTheme(R.style.AppTheme_tomato);
                } else {
                    context.setTheme(R.style.AppTheme_tomato_NoActionBar);
                }
                break;
            case "banana":
                if (hasActionBar) {
                    context.setTheme(R.style.AppTheme_banana);
                } else {
                    context.setTheme(R.style.AppTheme_banana_NoActionBar);
                }
                break;
            case "peacock":
                if (hasActionBar) {
                    context.setTheme(R.style.AppTheme_peacock);
                } else {
                    context.setTheme(R.style.AppTheme_peacock_NoActionBar);
                }
                break;
        }

    }
}
