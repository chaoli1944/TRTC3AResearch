# TRTC3AResearch
TRTC腾讯实时音视频，音频3A调用示例

### 说明
代码主要演示TRTC腾讯实时音视频，音频3A调用示例。

- demo中演示了自定义采集音频，需要pcm文件，请自行获取。
- 包文件夹下，有一个pcm音乐文件，提供测试使用，可以把它放到手机本地sd下audioResearch/PCM路径下。
- pcm文件名称：bei_yi_wang_de_shi_guang.pcm  、 44100hz 、 双声道 、 16位宽，无损音质。

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

### dumpSDKAudioFrame
勾选后就会把sdk回调的pcm数据，存到本地文件，存储路径是sd卡/audioResearch/

<table>
  <tr>
    <th width="200px" style="text-align:center">API</th>
    <th width="260px" style="text-align:center">描述</th>
  </tr>
  <tr>
    <td style="text-align:center">onCapturedRawAudioFrame</td>
    <td style="text-align:center">本地麦克风采集到的音频数据回调。</td>
  </tr>
  <tr>
    <td style="text-align:center">onLocalProcessedAudioFrame</td>
    <td style="text-align:center">本地采集并经过音频模块前处理后的音频数据回调。</td>
  </tr>
  <tr>
    <td style="text-align:center">onRemoteUserAudioFrame</td>
    <td style="text-align:center">混音前的每一路远程用户的音频数据，即混音前的各路原始数据。例如，对某一路音频进行文字转换时，您必须使用该路音频的原始数据。</td>
  </tr>
  <tr>
    <td style="text-align:center">onMixedPlayAudioFrame</td>
    <td style="text-align:center">各路音频数据混合后送入喇叭播放的音频数据。</td>
  </tr>
  <tr>
    <td style="text-align:center">onMixedAllAudioFrame</td>
    <td style="text-align:center">SDK所有音频数据混合后的数据回调（包括采集音频数据和所有播放音频数据）。</td>
  </tr>
</table>



