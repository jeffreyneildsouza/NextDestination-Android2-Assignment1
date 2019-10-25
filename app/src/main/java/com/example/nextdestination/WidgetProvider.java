package com.example.nextdestination;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import static com.example.nextdestination.HomeActivity.SHARED_PREFS;
import static com.example.nextdestination.HomeActivity.KEY_BUTTON_TEXT;

public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, HomeActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
            String widget_text = prefs.getString(KEY_BUTTON_TEXT + appWidgetId, "");


            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
            //views.setCharSequence(R.id.widget_destination_TextView, "setText", widget_text);


            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

}
