package com.chaoli.trtc3aresearch;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudListener;

import static com.tencent.trtc.TRTCCloud.setLogListener;


public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //集成bugly
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
        strategy.setAppVersion(TXLiveBase.getSDKVersionStr());
        CrashReport.initCrashReport(getApplicationContext(), "6e3737e3a0", true, strategy);


    }
}




