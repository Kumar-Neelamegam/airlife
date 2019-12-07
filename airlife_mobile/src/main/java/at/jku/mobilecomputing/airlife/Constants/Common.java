package at.jku.mobilecomputing.airlife.Constants;

import android.content.Context;

import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.SharedPrefUtils;

public class Common {

    public static void setUpTheme(SharedPrefUtils sharedPrefUtils, Context ctx) {
        sharedPrefUtils = SharedPrefUtils.getInstance(ctx);
        if (sharedPrefUtils.getAppInstallTime() == 0)
            sharedPrefUtils.setAppInstallTime(System.currentTimeMillis());
        if (sharedPrefUtils.isDarkMode()) ctx.setTheme(R.style.AppTheme_Dark);
        else ctx.setTheme(R.style.AppTheme_Light);
    }


}
