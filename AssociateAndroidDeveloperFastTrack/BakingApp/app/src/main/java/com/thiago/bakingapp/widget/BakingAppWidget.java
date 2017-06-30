package com.thiago.bakingapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.thiago.bakingapp.R;
import com.thiago.bakingapp.activities.MainActivity;
import com.thiago.bakingapp.activities.RecipeDetailsActivity;
import com.thiago.bakingapp.bean.Ingredient;
import com.thiago.bakingapp.bean.Recipe;
import com.thiago.bakingapp.utils.Constants;

/**
 * Implementation of App Widget functionality.
 */
public class BakingAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Recipe recipe) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.baking_app_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        Intent intent = null;
        if (recipe == null) {
            // call MainActivity
            intent = new Intent(context, MainActivity.class);
        } else {
            // call DetailsActivity
            intent = new Intent(context, RecipeDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_RECIPE_SELECTED, recipe);

            views.setViewVisibility(R.id.appwidget_recipe_title, View.VISIBLE);
            views.setTextViewText(R.id.appwidget_recipe_title, recipe.getName());

            StringBuilder builder = new StringBuilder();
            for (Ingredient ingredient : recipe.getIngredients()) {
                builder.append(ingredient.getQuantity()).append(" ").append(ingredient.getMeasure())
                        .append(" ").append(ingredient.getIngredient()).append("\n");
            }
            views.setTextViewText(R.id.appwidget_text, builder.toString());
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.appwidget_image_button, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                       Recipe recipe, int[] ids) {
        for (Integer id : ids) {
            updateAppWidget(context, appWidgetManager, id, recipe);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (Integer id : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id, null);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

