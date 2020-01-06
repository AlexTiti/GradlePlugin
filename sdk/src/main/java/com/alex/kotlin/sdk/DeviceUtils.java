package com.alex.kotlin.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex
 * @date 2020-01-03 11:02
 * @email 18238818283@sina.cn
 * @desc ...
 */
public class DeviceUtils {

    private static final String UNKNOWN = "UNKNOWN";

    @SuppressLint("HardwareIds")
    static String getDeviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    static Map<String, Object> getDeviceInfo(Context context) {
        final HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("$lib", "Android");
        hashMap.put("$lib_version", DataStatisticsApi.VERSION);
        hashMap.put("$os", "Android");
        hashMap.put("$os_version", Build.VERSION.RELEASE == null ? UNKNOWN : Build.VERSION.RELEASE);
        hashMap.put("$manufacturer", Build.MANUFACTURER == null ? UNKNOWN : Build.MANUFACTURER);
        hashMap.put("$model", Build.MODEL == null ? UNKNOWN : Build.MODEL.trim());

        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            hashMap.put("$app_version", packageInfo.versionName);
            int labelRes = packageInfo.applicationInfo.labelRes;
            hashMap.put("$app_name", context.getString(labelRes));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        hashMap.put("$screen_width", displayMetrics.widthPixels);
        hashMap.put("$screen_height", displayMetrics.heightPixels);
        hashMap.put("$screen_densityDpi", displayMetrics.densityDpi);
        return Collections.unmodifiableMap(hashMap);
    }

}
