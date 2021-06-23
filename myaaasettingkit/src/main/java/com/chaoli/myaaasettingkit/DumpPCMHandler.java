package com.chaoli.myaaasettingkit;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

public class DumpPCMHandler extends Handler {

    String folder = "";
    int sampleRate = 16000;
    int channel = 1;

    public DumpPCMHandler(@NonNull Looper looper) {
        super(looper);
    }

    public DumpPCMHandler(@NonNull Looper looper, @Nullable Callback callback) {
        super(looper, callback);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 1:
                HandleEnty he = (HandleEnty) msg.obj;
                byte[] data = he.data;
                String filePCMName = he.filePCMName;
                String folder = he.folder;
                FileUtils.writeFileToSDCard(data, folder, "capRaw_" + filePCMName, true, false, sampleRate, channel);
                Log.i("chaoli", "handleMessage: 通信1:");
                break;
            case 2:
                HandleEnty he2 = (HandleEnty) msg.obj;
                byte[] data2 = he2.data;
                String filePCMName2 = he2.filePCMName;
                String folder2 = he2.folder;
                FileUtils.writeFileToSDCard(data2, folder2, "localP_" + filePCMName2, true, false, sampleRate, channel);
                Log.i("chaoli", "handleMessage: 通信2");
                break;
            case 3:
                HandleEnty he3 = (HandleEnty) msg.obj;
                byte[] data3 = he3.data;
                String userId = he3.userId;
                String enterTime = he3.enterTime;
                String filePCMName3 = he3.filePCMName;
                String folder3 = he3.folder;
                FileUtils.writeFileToSDCard(data3, folder3 + File.separator + userId + "_" + enterTime, "remote_" + filePCMName3, true, false, 48000, 2);
                Log.i("chaoli", "handleMessage: 通信3");
                break;
            case 4:
                HandleEnty he4 = (HandleEnty) msg.obj;
                byte[] data4 = he4.data;
                String filePCMName4 = he4.filePCMName;
                String folder4 = he4.folder;
                FileUtils.writeFileToSDCard(data4, folder4, "mixPlay_" + filePCMName4, true, false, sampleRate, channel);
                Log.i("chaoli", "handleMessage: 通信4");
                break;
            case 5:
                HandleEnty he5 = (HandleEnty) msg.obj;
                byte[] data5 = he5.data;
                String filePCMName5 = he5.filePCMName;
                String folder5 = he5.folder;
                FileUtils.writeFileToSDCard(data5, folder5, "mixAll_" + filePCMName5, true, false, 48000, 1);
                Log.i("chaoli", "handleMessage: 通信5");
                break;
        }
    }

    public static class HandleEnty {
        private byte[] data;
        private String userId;
        private String enterTime;
        private String filePCMName;
        private String folder;

        public HandleEnty(byte[] buffer, String str, String enterTime,String filePCMName,String folder) {
            this.data = buffer;
            this.userId = str;
            this.enterTime = enterTime;
            this.filePCMName = filePCMName;
            this.folder = folder;
        }
    }
}
