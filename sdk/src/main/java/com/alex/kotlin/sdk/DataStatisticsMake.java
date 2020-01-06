package com.alex.kotlin.sdk;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * @author Alex
 * @date 2020-01-03 10:15
 * @email 18238818283@sina.cn
 * @desc ...
 */
public class DataStatisticsMake {

    private static final String Tag = "com.alex.kotlin.sdk.DataStatisticsApi = ";
    private static List<Integer> ingoreActivities = new ArrayList<>();
    private static WeakReference<Activity> activityWeakReference;
    private static DataBaseHelper dataBaseHelper;
    private static final long TIME_SPACE = 30 * 1000;
    private static CountDownTimer sCountDownTimer;

    static void init(Application application) {
        dataBaseHelper = new DataBaseHelper(application.getBaseContext());
        sCountDownTimer = new CountDownTimer(TIME_SPACE, 10 * 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                if (activityWeakReference != null) {
                    traceEnd(activityWeakReference.get());
                }
            }
        };
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                dataBaseHelper.commitAppStartState(true);
                long time = System.currentTimeMillis() - dataBaseHelper.getLastPauseTime();
                System.out.println(Tag + time);
                System.out.println(Tag + dataBaseHelper.getAppEndEventState());
                if (time > TIME_SPACE && !dataBaseHelper.getAppEndEventState()) {
                    System.out.println(Tag + "add traceEnd()");
                    traceEnd(activity);
                }
                System.out.println(Tag + dataBaseHelper.getAppEndEventState());
                if (dataBaseHelper.getAppEndEventState()) {
                    System.out.println(Tag + "add traceStart()");
                    dataBaseHelper.commitAppEndState(false);
                    traceStart(activity);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                traceViewScreenStatisticsData(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                activityWeakReference = new WeakReference<>(activity);
                sCountDownTimer.start();
                dataBaseHelper.setOnPauseTime(System.currentTimeMillis());
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    static void registerActivityStateObserver(Application application) {
        application.getContentResolver().registerContentObserver(dataBaseHelper.getAPP_START_URI(), false,
                new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        if (dataBaseHelper.getAPP_START_URI().equals(uri)) {
                            sCountDownTimer.cancel();
                        }
                    }
                });
    }

    static void addIngoreActivity(Activity activity) {
        if (activity == null) {
            return;
        }
        ingoreActivities.add(activity.hashCode());
    }

    static void removeIngoreActivity(Activity activity) {
        if (activity == null) {
            return;
        }
        if (ingoreActivities.contains(activity.hashCode())) {
            ingoreActivities.remove(activity.hashCode());
        }
    }

    private static void traceEnd(Activity activity) {
        if (activity == null) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("$activity", activity.getClass().getCanonicalName());
            jsonObject.put("$title", getActivityTitle(activity));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataStatisticsApi.getInstance().traceStatisticsData("AppEnd", jsonObject);
        dataBaseHelper.commitAppEndState(true);
    }

    private static void traceStart(Activity activity) {
        if (activity == null) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("$activity", activity.getClass().getCanonicalName());
            jsonObject.put("$title", getActivityTitle(activity));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataStatisticsApi.getInstance().traceStatisticsData("AppStart", jsonObject);
    }


    private static void traceViewScreenStatisticsData(Activity activity) {
        if (activity == null) {
            return;
        }
        if (ingoreActivities.contains(activity.hashCode())) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("$activity", activity.getClass().getCanonicalName());
            jsonObject.put("$title", getActivityTitle(activity));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        DataStatisticsApi.getInstance().traceStatisticsData("AppScreen_view", jsonObject);
    }

    private static String getActivityTitle(Activity activity) {
        if (activity == null) {
            return null;
        }
        String titleActivity = activity.getTitle().toString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            String actionBarTitle = getActionOrSupportActionBarTitle(activity);
            if (!TextUtils.isEmpty(actionBarTitle)) {
                titleActivity = actionBarTitle;
            }
        }
        if (titleActivity == null) {
            PackageManager packageManager = activity.getPackageManager();
            try {
                ActivityInfo activityInfo = packageManager.getActivityInfo(activity.getComponentName(), 0);
                if (activityInfo != null) {
                    titleActivity = activityInfo.loadLabel(packageManager).toString();
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        return titleActivity;
    }

    private static String getActionOrSupportActionBarTitle(Activity activity) {
        ActionBar actionBar = activity.getActionBar();
        if (actionBar != null) {
            if (!TextUtils.isEmpty(actionBar.getTitle())) {
                return actionBar.getTitle().toString();
            }
        } else {
            if (activity instanceof AppCompatActivity) {
                AppCompatActivity appCompatActivity = (AppCompatActivity) activity;
                android.support.v7.app.ActionBar supportActionBar = appCompatActivity.getSupportActionBar();
                if (supportActionBar != null && !TextUtils.isEmpty(supportActionBar.getTitle())) {
                    return Objects.requireNonNull(supportActionBar.getTitle()).toString();
                }
            }
        }
        return null;
    }

    static void mergeJsonObject(JSONObject from, JSONObject to) {
        if (from == null && to == null) {
            return;
        }
        if (from == null) {
            return;
        }
        if (to == null) {
            return;
        }
        Iterator<String> iterator = from.keys();
        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                Object value = from.get(key);
                to.put(key, value);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private static String getViewId(View view) {
        if (view.getId() == View.NO_ID) {
            return null;
        }
        return view.getContext().getResources().getResourceEntryName(view.getId());
    }

    private static String getViewType(View view) {
        if (view == null) {
            return null;
        }
        String viewType = null;
        if (view instanceof CheckBox) {
            viewType = "CheckBox";
        } else if (view instanceof SwitchCompat) {
            viewType = "SwitchCompat";
        } else if (view instanceof RadioButton) {
            viewType = "RadioButton";
        } else if (view instanceof ToggleButton) {
            viewType = "ToggleButton";
        } else if (view instanceof Button) {
            viewType = "Button";
        } else if (view instanceof CheckedTextView) {
            viewType = "CheckedTextView";
        } else if (view instanceof TextView) {
            viewType = "TextView";
        } else if (view instanceof ImageButton) {
            viewType = "ImageButton";
        } else if (view instanceof ImageView) {
            viewType = "ImageView";
        } else if (view instanceof RatingBar) {
            viewType = "RatingBar";
        } else if (view instanceof SeekBar) {
            viewType = "SeekBar";
        }
        return viewType;
    }

    public static void trackViewOnClick(View view) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("$element_type", getViewType(view));
            jsonObject.put("$element_id", getViewId(view));
            jsonObject.put("$element_content", getElementContent(view));

            Activity activity = getActivityFromView(view);
            if (activity != null) {
                jsonObject.put("$activity", activity.getClass().getCanonicalName());
                jsonObject.put("$title", getActivityTitle(activity));
            }
            DataStatisticsApi.getInstance().traceStatisticsData("$AppClick", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Keep
    public static void trackViewOnClick(DialogInterface dialogInterface, int whichButton) {
        try {
            Dialog dialog = null;
            if (dialogInterface instanceof Dialog) {
                dialog = (Dialog) dialogInterface;
            }

            if (dialog == null) {
                return;
            }

            Context context = dialog.getContext();
            //将Context转成Activity
            Activity activity = getActivityFromContext(context);

            if (activity == null) {
                activity = dialog.getOwnerActivity();
            }

            JSONObject properties = new JSONObject();
            //$screen_name & $title
            if (activity != null) {
                properties.put("$activity", activity.getClass().getCanonicalName());
            }

            Button button = null;
            if (dialog instanceof android.app.AlertDialog) {
                button = ((android.app.AlertDialog) dialog).getButton(whichButton);
            } else if (dialog instanceof android.support.v7.app.AlertDialog) {
                button = ((android.support.v7.app.AlertDialog) dialog).getButton(whichButton);
            }

            if (button != null) {
                properties.put("$element_content", button.getText());
            }

            properties.put("$element_type", "Dialog");

            DataStatisticsApi.getInstance().traceStatisticsData("$AppClick", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Keep
    public static void trackViewOnClick(DialogInterface dialogInterface, int whichButton, boolean isChecked) {
        try {
            Dialog dialog = null;
            if (dialogInterface instanceof Dialog) {
                dialog = (Dialog) dialogInterface;
            }

            if (dialog == null) {
                return;
            }

            Context context = dialog.getContext();
            //将Context转成Activity
            Activity activity = getActivityFromContext(context);

            if (activity == null) {
                activity = dialog.getOwnerActivity();
            }

            JSONObject properties = new JSONObject();
            //$screen_name & $title
            if (activity != null) {
                properties.put("$activity", activity.getClass().getCanonicalName());
            }

            ListView listView = null;
            if (dialog instanceof android.app.AlertDialog) {
                listView = ((android.app.AlertDialog) dialog).getListView();
            } else if (dialog instanceof android.support.v7.app.AlertDialog) {
                listView = ((android.support.v7.app.AlertDialog) dialog).getListView();
            }

            if (listView != null) {
                ListAdapter listAdapter = listView.getAdapter();
                Object object = listAdapter.getItem(whichButton);
                if (object != null) {
                    if (object instanceof String) {
                        properties.put("$element_content", object);
                    }
                }
            }

            properties.put("isChecked", isChecked);
            properties.put("$element_type", "Dialog");

            DataStatisticsApi.getInstance().traceStatisticsData("$AppClick", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackViewOnClick(CompoundButton view, boolean isChecked) {
        try {
            Context context = view.getContext();
            if (context == null) {
                return;
            }

            JSONObject properties = new JSONObject();

            Activity activity = getActivityFromContext(context);

            try {
                String idString = context.getResources().getResourceEntryName(view.getId());
                if (!TextUtils.isEmpty(idString)) {
                    properties.put("$element_id", idString);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (activity != null) {
                properties.put("$activity", activity.getClass().getCanonicalName());
            }

            String viewText = null;
            String viewType;
            if (view instanceof CheckBox) {
                viewType = "CheckBox";
                CheckBox checkBox = (CheckBox) view;
                if (!TextUtils.isEmpty(checkBox.getText())) {
                    viewText = checkBox.getText().toString();
                }
            } else if (view instanceof SwitchCompat) {
                viewType = "SwitchCompat";
                SwitchCompat switchCompat = (SwitchCompat) view;
                if (!TextUtils.isEmpty(switchCompat.getTextOn())) {
                    viewText = switchCompat.getTextOn().toString();
                }
            } else if (view instanceof ToggleButton) {
                viewType = "ToggleButton";
                ToggleButton toggleButton = (ToggleButton) view;
                if (isChecked) {
                    if (!TextUtils.isEmpty(toggleButton.getTextOn())) {
                        viewText = toggleButton.getTextOn().toString();
                    }
                } else {
                    if (!TextUtils.isEmpty(toggleButton.getTextOff())) {
                        viewText = toggleButton.getTextOff().toString();
                    }
                }
            } else if (view instanceof RadioButton) {
                viewType = "RadioButton";
                RadioButton radioButton = (RadioButton) view;
                if (!TextUtils.isEmpty(radioButton.getText())) {
                    viewText = radioButton.getText().toString();
                }
            } else {
                viewType = view.getClass().getCanonicalName();
            }

            //Content
            if (!TextUtils.isEmpty(viewText)) {
                properties.put("$element_content", viewText);
            }

            if (!TextUtils.isEmpty(viewType)) {
                properties.put("$element_type", viewType);
            }

            properties.put("isChecked", isChecked);

            DataStatisticsApi.getInstance().traceStatisticsData("$AppClick", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Keep
    public static void trackExpandableListViewChildOnClick(ExpandableListView expandableListView, View view,
                                                           int groupPosition, int childPosition) {
        try {
            Context context = expandableListView.getContext();
            if (context == null) {
                return;
            }

            JSONObject properties = new JSONObject();
            Activity activity = getActivityFromContext(context);
            if (activity != null) {
                properties.put("$activity", activity.getClass().getCanonicalName());
            }

            if (childPosition != -1) {
                properties.put("$element_position", String.format(Locale.CHINA, "%d:%d", groupPosition, childPosition));
            } else {
                properties.put("$element_position", String.format(Locale.CHINA, "%d", groupPosition));
            }

            String idString = getViewId(expandableListView);
            if (!TextUtils.isEmpty(idString)) {
                properties.put("$element_id", idString);
            }

            properties.put("$element_type", "ExpandableListView");

            String viewText = null;
            if (view instanceof ViewGroup) {
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    viewText = traverseViewContent(stringBuilder, (ViewGroup) view);
                    if (!TextUtils.isEmpty(viewText)) {
                        viewText = viewText.substring(0, viewText.length() - 1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!TextUtils.isEmpty(viewText)) {
                properties.put("$element_content", viewText);
            }

            DataStatisticsApi.getInstance().traceStatisticsData("$AppClick", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Keep
    public static void trackViewOnClick(android.widget.AdapterView adapterView, android.view.View view, int position) {
        try {
            Context context = adapterView.getContext();
            if (context == null) {
                return;
            }

            JSONObject properties = new JSONObject();

            Activity activity = getActivityFromContext(context);
            String idString = getViewId(adapterView);
            if (!TextUtils.isEmpty(idString)) {
                properties.put("$element_id", idString);
            }

            if (activity != null) {
                properties.put("$activity", activity.getClass().getCanonicalName());
            }
            properties.put("$element_position", String.valueOf(position));

            if (adapterView instanceof Spinner) {
                properties.put("$element_type", "Spinner");
                Object item = adapterView.getItemAtPosition(position);
                if (item != null) {
                    if (item instanceof String) {
                        properties.put("$element_content", item);
                    }
                }
            } else {
                if (adapterView instanceof ListView) {
                    properties.put("$element_type", "ListView");
                } else if (adapterView instanceof GridView) {
                    properties.put("$element_type", "GridView");
                }

                String viewText = null;
                if (view instanceof ViewGroup) {
                    try {
                        StringBuilder stringBuilder = new StringBuilder();
                        viewText = traverseViewContent(stringBuilder, (ViewGroup) view);
                        if (!TextUtils.isEmpty(viewText)) {
                            viewText = viewText.substring(0, viewText.length() - 1);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    viewText = getElementContent(view);
                }
                //$element_content
                if (!TextUtils.isEmpty(viewText)) {
                    properties.put("$element_content", viewText);
                }
            }
            DataStatisticsApi.getInstance().traceStatisticsData("$AppClick", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * MenuItem 被点击，自动埋点
     *
     * @param object   Object
     * @param menuItem MenuItem
     */
    @Keep
    public static void trackViewOnClick(Object object, MenuItem menuItem) {
        try {
            Context context = null;
            if (object instanceof Context) {
                context = (Context) object;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("$element_type", "menuItem");

            jsonObject.put("$element_content", menuItem.getTitle());

            if (context != null) {
                String idString = null;
                try {
                    idString = context.getResources().getResourceEntryName(menuItem.getItemId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!TextUtils.isEmpty(idString)) {
                    jsonObject.put("$element_id", idString);
                }

                Activity activity = getActivityFromContext(context);
                if (activity != null) {
                    jsonObject.put("$activity", activity.getClass().getCanonicalName());
                }
            }
            DataStatisticsApi.getInstance().traceStatisticsData("$AppClick", jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Keep
    public static void trackTabHost(String tabName) {
        try {
            JSONObject properties = new JSONObject();
            properties.put("$element_type", "TabHost");
            properties.put("$element_content", tabName);
            DataStatisticsApi.getInstance().traceStatisticsData("$AppClick", properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取 View 所属 Activity
     *
     * @param view View
     * @return Activity
     */
    private static Activity getActivityFromView(View view) {
        Activity activity = null;
        if (view == null) {
            return null;
        }
        try {
            Context context = view.getContext();
            if (context != null) {
                if (context instanceof Activity) {
                    activity = (Activity) context;
                } else if (context instanceof ContextWrapper) {
                    while (!(context instanceof Activity) && context instanceof ContextWrapper) {
                        context = ((ContextWrapper) context).getBaseContext();
                    }
                    if (context instanceof Activity) {
                        activity = (Activity) context;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activity;
    }

    private static Activity getActivityFromContext(Context context) {
        Activity activity = null;
        try {
            if (context != null) {
                if (context instanceof Activity) {
                    activity = (Activity) context;
                } else if (context instanceof ContextWrapper) {
                    while (!(context instanceof Activity) && context instanceof ContextWrapper) {
                        context = ((ContextWrapper) context).getBaseContext();
                    }
                    if (context instanceof Activity) {
                        activity = (Activity) context;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activity;
    }

    /**
     * 获取 View 上显示的文本
     *
     * @param view View
     * @return String
     */
    private static String getElementContent(View view) {
        if (view == null) {
            return null;
        }
        CharSequence viewText = null;
        if (view instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) view;
            viewText = checkBox.getText();
        } else if (view instanceof SwitchCompat) {
            SwitchCompat switchCompat = (SwitchCompat) view;
            viewText = switchCompat.getTextOn();
        } else if (view instanceof RadioButton) {
            RadioButton radioButton = (RadioButton) view;
            viewText = radioButton.getText();
        } else if (view instanceof ToggleButton) {
            ToggleButton toggleButton = (ToggleButton) view;
            boolean isChecked = toggleButton.isChecked();
            if (isChecked) {
                viewText = toggleButton.getTextOn();
            } else {
                viewText = toggleButton.getTextOff();
            }
        } else if (view instanceof Button) {
            Button button = (Button) view;
            viewText = button.getText();
        } else if (view instanceof CheckedTextView) {
            CheckedTextView textView = (CheckedTextView) view;
            viewText = textView.getText();
        } else if (view instanceof TextView) {
            TextView textView = (TextView) view;
            viewText = textView.getText();
        } else if (view instanceof SeekBar) {
            SeekBar seekBar = (SeekBar) view;
            viewText = String.valueOf(seekBar.getProgress());
        } else if (view instanceof RatingBar) {
            RatingBar ratingBar = (RatingBar) view;
            viewText = String.valueOf(ratingBar.getRating());
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            if (!TextUtils.isEmpty(imageView.getContentDescription())) {
                viewText = imageView.getContentDescription().toString();
            }
        }
        if (viewText != null) {
            return viewText.toString();
        }
        return null;
    }

    private static String traverseViewContent(StringBuilder stringBuilder, ViewGroup root) {
        try {
            if (root == null) {
                return stringBuilder.toString();
            }

            final int childCount = root.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                final View child = root.getChildAt(i);

                if (child.getVisibility() != View.VISIBLE) {
                    continue;
                }

                if (child instanceof ViewGroup) {
                    traverseViewContent(stringBuilder, (ViewGroup) child);
                } else {
                    CharSequence viewText = null;
                    if (child instanceof CheckBox) {
                        CheckBox checkBox = (CheckBox) child;
                        viewText = checkBox.getText();
                    } else if (child instanceof SwitchCompat) {
                        SwitchCompat switchCompat = (SwitchCompat) child;
                        viewText = switchCompat.getTextOn();
                    } else if (child instanceof RadioButton) {
                        RadioButton radioButton = (RadioButton) child;
                        viewText = radioButton.getText();
                    } else if (child instanceof ToggleButton) {
                        ToggleButton toggleButton = (ToggleButton) child;
                        boolean isChecked = toggleButton.isChecked();
                        if (isChecked) {
                            viewText = toggleButton.getTextOn();
                        } else {
                            viewText = toggleButton.getTextOff();
                        }
                    } else if (child instanceof Button) {
                        Button button = (Button) child;
                        viewText = button.getText();
                    } else if (child instanceof CheckedTextView) {
                        CheckedTextView textView = (CheckedTextView) child;
                        viewText = textView.getText();
                    } else if (child instanceof TextView) {
                        TextView textView = (TextView) child;
                        viewText = textView.getText();
                    } else if (child instanceof ImageView) {
                        ImageView imageView = (ImageView) child;
                        if (!TextUtils.isEmpty(imageView.getContentDescription())) {
                            viewText = imageView.getContentDescription().toString();
                        }
                    }

                    if (!TextUtils.isEmpty(viewText)) {
                        stringBuilder.append(viewText.toString());
                        stringBuilder.append("-");
                    }
                }
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return stringBuilder.toString();
        }
    }
}
