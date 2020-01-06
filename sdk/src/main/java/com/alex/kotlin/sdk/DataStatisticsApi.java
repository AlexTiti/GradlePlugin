package com.alex.kotlin.sdk;

import android.app.Application;
import android.view.View;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;

/**
 * @author Alex
 * @date 2020-01-03 10:14
 * @email 18238818283@sina.cn
 * @desc ...
 */
public class DataStatisticsApi {

    private final String TAG = this.getClass().getCanonicalName();
    private static DataStatisticsApi mDataStatisticsApi;
    static final String VERSION = "1.0.0";
    private String mDeviceId;
    private Map<String, Object> mDeviceInfo;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    private DataStatisticsApi() {
    }

    private DataStatisticsApi(Application application) {
        mDeviceId = DeviceUtils.getDeviceId(application.getBaseContext());
        mDeviceInfo = DeviceUtils.getDeviceInfo(application.getBaseContext());
        DataStatisticsMake.init(application);
        DataStatisticsMake.registerActivityStateObserver(application);
    }

    public static DataStatisticsApi init(Application application) {
        if (mDataStatisticsApi == null) {
            synchronized (DataStatisticsApi.class) {
                if (mDataStatisticsApi == null) {
                    mDataStatisticsApi = new DataStatisticsApi(application);
                }
            }
        }
        return mDataStatisticsApi;
    }

    public static DataStatisticsApi getInstance() {
        return mDataStatisticsApi;
    }

    public void traceStatisticsData(String eventName, JSONObject jsonObject) {
        JSONObject jsonObjectSend = new JSONObject();
        try {
            jsonObjectSend.put("event", eventName);
            jsonObjectSend.put("device_id", mDeviceId);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonObjectProperties = new JSONObject(mDeviceInfo);
        if (jsonObject != null) {
            DataStatisticsMake.mergeJsonObject(jsonObject, jsonObjectProperties);
        }
        try {
            jsonObjectSend.put("properties", jsonObjectProperties);
            jsonObjectSend.put("time", System.currentTimeMillis());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println(TAG + jsonObjectSend);
    }

    public void traceViewOnClick(View view) {
        DataStatisticsMake.trackViewOnClick(view);
    }

    public static void staticTraceViewOnClick(View view) {
        DataStatisticsApi.getInstance().traceViewOnClick(view);
    }
}
