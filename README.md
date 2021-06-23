# TRTC3AResearch
TRTC腾讯实时音视频，音频3A调用示例

### 说明
代码主要演示TRTC腾讯实时音视频，音频3A调用示例

### 接口
```
    private void set3A(int aec, int ans, int agc) {
        //callExperimentalAPI接口，无回调
        int aecFlag = aec == 0 ? 0 : 1;
        int ansFlag = ans == 0 ? 0 : 1;
        int agcFlag = agc == 0 ? 0 : 1;

        String aecJson = "{\"api\":\"enableAudioAEC\",\"params\":{\"enable\":" + aecFlag + ", \"level\":" + aec + "}}";
        mTRTCCloud.callExperimentalAPI(aecJson);
        String ansJson = "{\"api\":\"enableAudioANS\",\"params\":{\"enable\":" + ansFlag + ", \"level\":" + ans + "}}";
        mTRTCCloud.callExperimentalAPI(ansJson);
        String agcJson = "{\"api\":\"enableAudioAGC\",\"params\":{\"enable\":" + agcFlag + ", \"level\":" + agc + "}}";
        mTRTCCloud.callExperimentalAPI(agcJson);
    }
 ```


### demo截图

![S10622-143151622](https://user-images.githubusercontent.com/49272458/122881316-9cc53d00-d36d-11eb-8442-d522076ab848.jpg)


