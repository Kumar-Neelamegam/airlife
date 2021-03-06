package at.jku.mobilecomputing.airlife.Widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.work.WorkManager;

import at.jku.mobilecomputing.airlife.CoreModules.MainActivity;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.SharedPrefUtils;

/**
 * Implementation of App Widget functionality.
 */
public class ALWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        try {
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_aqi);
            String aqiString = SharedPrefUtils.getInstance(context).getLatestAQI();
            views.setTextViewText(R.id.widget_aqi_text, aqiString);
            String airQuality = "";
            int aqi = Integer.parseInt(aqiString);
            int colorId = -1;
            if (aqi >= 0 && aqi <= 50) {
                airQuality = context.getString(R.string.good);
                views.setImageViewResource(R.id.widget_background, R.drawable.widget_circle_good);
            } else if (aqi >= 51 && aqi <= 100) {
                airQuality = context.getString(R.string.moderate);
                views.setImageViewResource(R.id.widget_background, R.drawable.widget_circle_moderate);
            } else if (aqi >= 101 && aqi <= 150) {
                airQuality = context.getString(R.string.unhealthy);
                views.setImageViewResource(R.id.widget_background, R.drawable.widget_circle_unhealthysg);
            } else if (aqi >= 151 && aqi <= 200) {
                airQuality = context.getString(R.string.unhealthy);
                views.setImageViewResource(R.id.widget_background, R.drawable.widget_circle_unhealthy);
            } else if (aqi >= 201 && aqi <= 300) {
                airQuality = context.getString(R.string.very_unhealthy);
                views.setImageViewResource(R.id.widget_background, R.drawable.widget_circle_veryunhealthy);
            } else if (aqi >= 301) {
                airQuality = context.getString(R.string.hazardous);
                views.setImageViewResource(R.id.widget_background, R.drawable.widget_circle_harzardous);
            }
            views.setTextViewText(R.id.widget_air_quality_text, airQuality);
            views.setTextColor(R.id.widget_air_quality_text, context.getResources().getColor(R.color.white));
            views.setTextViewText(R.id.temperature_text_view, SharedPrefUtils.getInstance(context).getLatestTemp());
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            views.setOnClickPendingIntent(R.id.widget_background, pendingIntent);
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        WorkManager.getInstance().cancelAllWork();
    }
}

