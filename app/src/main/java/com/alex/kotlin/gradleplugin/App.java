package com.alex.kotlin.gradleplugin;

import android.app.Application;
import com.alex.kotlin.sdk.DataStatisticsApi;

/**
 * @author Alex
 * @date 2020-01-03 10:08
 * @email 18238818283@sina.cn
 * @desc ...
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DataStatisticsApi.init(App.this);
    }
}
