package com.tencent.rtc;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.carlos.voiceline.mylibrary.VoiceLineView;
import com.chaoli.myaaasettingkit.AAASettingParam;
import com.chaoli.myaaasettingkit.DumpPCMHandler;
import com.chaoli.myaaasettingkit.FileUtils;
import com.tencent.liteav.TXLiteAVCode;
import com.tencent.liteav.audiosettingkit.AudioEffectPanel;
import com.tencent.liteav.beauty.TXBeautyManager;
import com.tencent.liteav.debug.Constant;
import com.tencent.liteav.debug.GenerateTestUserSig;
import com.tencent.rtc.R;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tencent.trtc.TRTCCloudDef.TRTCRoleAnchor;
import static com.tencent.trtc.TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL;
import static com.tencent.trtc.TRTCCloudDef.TRTC_GSENSOR_MODE_DISABLE;
import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT;

/**
 * RTC视频通话的主页面
 * <p>
 * 包含如下简单功能：
 * - 进入/退出视频通话房间
 * - 切换前置/后置摄像头
 * - 打开/关闭摄像头
 * - 打开/关闭麦克风
 * - 显示房间内其他用户的视频画面（当前示例最多可显示6个其他用户的视频画面）
 */
public class RTCActivity extends AppCompatActivity implements View.OnClickListener, TRTCCloudListener.TRTCAudioFrameListener {

    private static final String TAG = "RTCActivity";
    private static final int REQ_PERMISSION_CODE = 0x1000;

    private TextView mTitleText;                 //【控件】页面Title
    private TXCloudVideoView mLocalPreviewView;          //【控件】本地画面View
    private ImageView mBackButton;                //【控件】返回上一级页面
    private Button mMuteVideo;                 //【控件】是否停止推送本地的视频数据
    private Button mMuteAudio;                 //【控件】开启、关闭本地声音采集并上行
    private Button mSwitchCamera;              //【控件】切换摄像头
    private Button trtc_btn_3A;              //【控件】切换摄像头
    private Button mLogInfo;                   //【控件】开启、关闭日志显示
    private LinearLayout mVideoMutedTipsView;        //【控件】关闭视频时，显示默认头像

    private TRTCCloud mTRTCCloud;                 // SDK 核心类
    private boolean mIsFrontCamera = true;      // 默认摄像头前置
    private List<String> mRemoteUidList;             // 远端用户Id列表
    private List<TXCloudVideoView> mRemoteViewList;            // 远端画面列表
    private int mGrantedCount = 0;          // 权限个数计数，获取Android系统权限
    private int mUserCount = 0;             // 房间通话人数个数
    private int mLogLevel = 0;              // 日志等级
    private String mRoomId;                    // 房间Id
    private String mUserId;                    // 用户Id
    private AudioEffectPanel mPanelAudioControl;     // 音效控制面板
    private int AEC = 0;
    private int AGC = 0;
    private int ANS = 0;
    private int sampleRate = 16000;
    private int channel = 1;
    private int capAudioType = 0;

    private DumpPCMHandler dumpPCMHandler;

    private String folder;
    private String filePCMName;

    private String enterRoomTime;

    private HandlerThread mHandlerThread;


    private AudioTrack audioTrack;
    private String pushPCMPath;
    private Thread playPCMAudioThread;
    private Thread pushCustomAudioThread;

    private AAASettingParam aaaSettingParam;

    private boolean isPCMFileExist;
    private boolean isStop;
    private boolean showVoiceView;

    private VoiceLineView voiceLineView;
    private VoiceLineView voiceLineView2;

    private TextView tv_remotevolume_sb;
    private TextView tv_localvolume_sb;

    private StringBuffer sbLocalVolume;
    private StringBuffer sbRemoteVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtc);
        getSupportActionBar().hide();
        handleIntent();
        initView();
        initData();
        enterRoom();
    }

    private void initData() {

        aaaSettingParam = AAASettingParam.getInstance();
        AEC = aaaSettingParam.getAec();
        ANS = aaaSettingParam.getAns();
        AGC = aaaSettingParam.getAgc();

        SimpleDateFormat df = new SimpleDateFormat("MM-dd-HH-mm-ss");//设置日期格式
        enterRoomTime = df.format(new Date());// new Date()为获取当前系统时间
        folder = "audioResearch" + File.separator + enterRoomTime + "_" + mRoomId + "_" + mUserId;
        filePCMName = enterRoomTime + "_AEC" + AEC + "_AGC" + AGC + "_ANS" + ANS + ".pcm";

        if (aaaSettingParam.isDumpMixedAllAudioFrameFlag() ||
                aaaSettingParam.isDumpMixedPlayAudioFrameFlag() ||
                aaaSettingParam.isDumpRemoteUserAudioFrameFlag() ||
                aaaSettingParam.isDumpLocalProcessedAudioFrameFlag() ||
                aaaSettingParam.isDumpCapturedRawAudioFrameFlag()) {
            mHandlerThread = new HandlerThread("handlerThread");
            mHandlerThread.start();
            dumpPCMHandler = new DumpPCMHandler(mHandlerThread.getLooper());
        }


        pushPCMPath = Environment.getExternalStorageDirectory()
                + File.separator + "audioResearch" + File.separator + "PCM" + File.separator + "bei_yi_wang_de_shi_guang.pcm";

        isPCMFileExist = FileUtils.checkPCMFile(pushPCMPath);
        isStop = false;
        showVoiceView = false;

        sbLocalVolume = new StringBuffer();
        sbRemoteVolume = new StringBuffer();
        sbLocalVolume.append("sampleRate:16kHZ  channel:1  16bit");
        sbLocalVolume.append("\r\n");
        sbLocalVolume.append(mUserId + " volume:000");
        sbRemoteVolume.append("sampleRate:48kHZ  channel:2  16bit");
        sbRemoteVolume.append("\r\n");
        sbRemoteVolume.append("totalVolume:000");
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (null != intent) {
            if (intent.getStringExtra(Constant.USER_ID) != null) {
                mUserId = intent.getStringExtra(Constant.USER_ID);
            }
            if (intent.getStringExtra(Constant.ROOM_ID) != null) {
                mRoomId = intent.getStringExtra(Constant.ROOM_ID);
            }
        }
    }

    private void initView() {
        mTitleText = findViewById(R.id.trtc_tv_room_number);
        mBackButton = findViewById(R.id.trtc_ic_back);
        mLocalPreviewView = findViewById(R.id.trtc_tc_cloud_view_main);
        mMuteVideo = findViewById(R.id.trtc_btn_mute_video);
        mMuteAudio = findViewById(R.id.trtc_btn_mute_audio);
        mSwitchCamera = findViewById(R.id.trtc_btn_switch_camera);
        trtc_btn_3A = findViewById(R.id.trtc_btn_3A);
        mLogInfo = findViewById(R.id.trtc_btn_log_info);
        tv_remotevolume_sb = findViewById(R.id.tv_remotevolume_sb);
        tv_localvolume_sb = findViewById(R.id.tv_localvolume_sb);

        voiceLineView = findViewById(R.id.voicLine);
        voiceLineView.l2r(false);
        voiceLineView2 = findViewById(R.id.voicLine2);
        voiceLineView2.l2r(true);


        mVideoMutedTipsView = findViewById(R.id.ll_trtc_mute_video_default);

        if (!TextUtils.isEmpty(mRoomId)) {
            mTitleText.setText(mRoomId);
        }
        mBackButton.setOnClickListener(this);
        mMuteVideo.setOnClickListener(this);
        mMuteAudio.setOnClickListener(this);
        mSwitchCamera.setOnClickListener(this);
        trtc_btn_3A.setOnClickListener(this);
        mLogInfo.setOnClickListener(this);

        mRemoteUidList = new ArrayList<>();
        mRemoteViewList = new ArrayList<>();
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_1));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_2));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_3));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_4));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_5));
        mRemoteViewList.add((TXCloudVideoView) findViewById(R.id.trtc_tc_cloud_view_6));

    }

    private void enterRoom() {

        mTRTCCloud = TRTCCloud.sharedInstance(getApplicationContext());
        mTRTCCloud.setListener(new TRTCCloudImplListener(RTCActivity.this));
        mTRTCCloud.setGSensorMode(TRTC_GSENSOR_MODE_DISABLE);
        mTRTCCloud.enableAudioVolumeEvaluation(100);
        set3A(AEC, ANS, AGC);

        if (aaaSettingParam.getCusAudioMode() == 1) {
            mTRTCCloud.enableCustomAudioCapture(true);
        }

        TRTCCloudDef.TRTCAudioFrameCallbackFormat format = new TRTCCloudDef.TRTCAudioFrameCallbackFormat();
        format.channel = channel;
        format.sampleRate = sampleRate;
        format.samplesPerCall = channel * sampleRate / 1000;
        mTRTCCloud.setCapturedRawAudioFrameCallbackFormat(format);
        mTRTCCloud.setLocalProcessedAudioFrameCallbackFormat(format);
        mTRTCCloud.setMixedPlayAudioFrameCallbackFormat(format);

        mTRTCCloud.setAudioFrameListener(this);

        // 初始化配置 SDK 参数
        TRTCCloudDef.TRTCParams trtcParams = new TRTCCloudDef.TRTCParams();
        trtcParams.sdkAppId = GenerateTestUserSig.SDKAPPID;
        trtcParams.userId = mUserId;
        trtcParams.roomId = Integer.parseInt(mRoomId);
        // userSig是进入房间的用户签名，相当于密码（这里生成的是测试签名，正确做法需要业务服务器来生成，然后下发给客户端）
        trtcParams.userSig = GenerateTestUserSig.genTestUserSig(trtcParams.userId);
        trtcParams.role = TRTCRoleAnchor;

        // 进入通话
        mTRTCCloud.enterRoom(trtcParams, TRTC_APP_SCENE_VIDEOCALL);

        mTRTCCloud.setSystemVolumeType(aaaSettingParam.getVolumeType());

        if (aaaSettingParam.getCusAudioMode() == 0) {
            mTRTCCloud.startLocalAudio(aaaSettingParam.getAudioQuality());
        } else if (aaaSettingParam.getCusAudioMode() == 1) {
            if (isPCMFileExist) {
                playPCMAudio();
                pushCustomAudio();
            } else {
                Toast.makeText(this, "无PCM音频文件，请在手机sd卡/audioResearch/PCM路径下放入pcm文件", Toast.LENGTH_LONG).show();
            }
        }

        // 开启本地画面采集并上行
        mTRTCCloud.startLocalPreview(mIsFrontCamera, mLocalPreviewView);

        /**
         * 设置默认美颜效果（美颜效果：自然，美颜级别：5, 美白级别：1）
         * 美颜风格.三种美颜风格：0 ：光滑  1：自然  2：朦胧
         * 视频通话场景推荐使用“自然”美颜效果
         */
        TXBeautyManager beautyManager = mTRTCCloud.getBeautyManager();
        beautyManager.setBeautyStyle(Constant.BEAUTY_STYLE_NATURE);
        beautyManager.setBeautyLevel(5);
        beautyManager.setWhitenessLevel(1);

        TRTCCloudDef.TRTCVideoEncParam encParam = new TRTCCloudDef.TRTCVideoEncParam();
        encParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360;
        encParam.videoFps = Constant.VIDEO_FPS;
        encParam.videoBitrate = Constant.RTC_VIDEO_BITRATE;
        encParam.videoResolutionMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT;
        mTRTCCloud.setVideoEncoderParam(encParam);

        //AudioEffectPanel
        mPanelAudioControl = (AudioEffectPanel) findViewById(R.id.anchor_audio_panel);
        mPanelAudioControl.setAudioEffectManager(mTRTCCloud.getAudioEffectManager());
        mPanelAudioControl.init3A(AEC, ANS, AGC);
        mPanelAudioControl.initPanelDefaultBackground();
        mPanelAudioControl.setOnAudioEffectPanelHideListener(new AudioEffectPanel.OnAudioEffectPanelHideListener() {
            @Override
            public void onClosePanel(int aec, int ans, int agc) {
                mPanelAudioControl.setVisibility(View.GONE);
                set3A(aec, ans, agc);
                showDebugView(showVoiceView, false);

            }

            @Override
            public void onBGMChange(boolean startFlag) {
            }
        });
    }

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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        exitRoom();
        if (mPanelAudioControl != null) {
            mPanelAudioControl.reset();
            mPanelAudioControl.unInit();
            mPanelAudioControl = null;
        }
        if (playPCMAudioThread != null) {
            isStop = true;
            if (audioTrack != null) {
                audioTrack.stop();
            }
            playPCMAudioThread = null;
            pushCustomAudioThread = null;
        }


    }

    /**
     * 离开通话
     */
    private void exitRoom() {
        mTRTCCloud.stopLocalAudio();
        mTRTCCloud.stopLocalPreview();
        mTRTCCloud.exitRoom();
        //销毁 trtc 实例
        if (mTRTCCloud != null) {
            mTRTCCloud.setListener(null);
        }
        mTRTCCloud = null;
        TRTCCloud.destroySharedInstance();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.trtc_ic_back) {
            finish();
        } else if (id == R.id.trtc_btn_mute_video) {
            muteVideo();
        } else if (id == R.id.trtc_btn_mute_audio) {
            muteAudio();
        } else if (id == R.id.trtc_btn_switch_camera) {
            switchCamera();
        } else if (id == R.id.trtc_btn_3A) {
            showAudioEffectPannel();
        } else if (id == R.id.trtc_btn_log_info) {
            showDebugView(showVoiceView, true);
        }
    }

    private void showAudioEffectPannel() {

        if (mPanelAudioControl.isShown()) {
            mPanelAudioControl.setVisibility(View.GONE);
            mPanelAudioControl.hideAudioPanel();

        } else {
            mPanelAudioControl.setVisibility(View.VISIBLE);
            mPanelAudioControl.showAudioPanel();
            showDebugView(showVoiceView, false);

        }
    }


    private void muteVideo() {
        boolean isSelected = mMuteVideo.isSelected();
        if (!isSelected) {
            mTRTCCloud.stopLocalPreview();
            mMuteVideo.setBackground(getResources().getDrawable(R.mipmap.rtc_camera_off));
            mVideoMutedTipsView.setVisibility(View.VISIBLE);
        } else {
            mTRTCCloud.startLocalPreview(mIsFrontCamera, mLocalPreviewView);
            mMuteVideo.setBackground(getResources().getDrawable(R.mipmap.rtc_camera_on));
            mVideoMutedTipsView.setVisibility(View.GONE);
        }
        mMuteVideo.setSelected(!isSelected);
    }

    private void muteAudio() {
        boolean isSelected = mMuteAudio.isSelected();
        if (!isSelected) {
            mTRTCCloud.stopLocalAudio();
            mMuteAudio.setBackground(getResources().getDrawable(R.mipmap.rtc_mic_off));
        } else {
            mTRTCCloud.startLocalAudio();
            mMuteAudio.setBackground(getResources().getDrawable(R.mipmap.rtc_mic_on));
        }
        mMuteAudio.setSelected(!isSelected);
    }

    private void switchCamera() {
        mTRTCCloud.switchCamera();
        boolean isSelected = mSwitchCamera.isSelected();
        mIsFrontCamera = !isSelected;
        mSwitchCamera.setSelected(!isSelected);
    }

    private void showDebugView(boolean showVoiceView, boolean changeFlag) {
//        mLogLevel = (mLogLevel + 1) % 3;
//        mTRTCCloud.showDebugView(mLogLevel);
        showVoiceView = changeFlag ? !showVoiceView : showVoiceView;

        tv_localvolume_sb.setVisibility(showVoiceView ? View.VISIBLE : View.GONE);
        voiceLineView.setVisibility(showVoiceView ? View.VISIBLE : View.GONE);
        voiceLineView.refreshRect(showVoiceView);

        tv_remotevolume_sb.setVisibility(showVoiceView && mRemoteUidList.size() > 0 ? View.VISIBLE : View.GONE);
        voiceLineView2.setVisibility(showVoiceView && mRemoteUidList.size() > 0 ? View.VISIBLE : View.GONE);
        voiceLineView2.refreshRect(showVoiceView);
        this.showVoiceView = showVoiceView;
    }

    private void refreshVolume(int volume, int totalVolume) {
        voiceLineView.setVolume(volume);
        sbLocalVolume.delete(sbLocalVolume.length() - 3, sbLocalVolume.length());
        sbLocalVolume.append(String.format("%03d", volume));
        tv_localvolume_sb.setText(sbLocalVolume);

        voiceLineView2.setVolume(totalVolume);
        sbRemoteVolume.delete(sbRemoteVolume.length() - 3, sbRemoteVolume.length());
        sbRemoteVolume.append(String.format("%03d", totalVolume));
        tv_remotevolume_sb.setText(sbRemoteVolume);

    }

    //本地麦克风采集到的音频数据回调。
    @Override
    public void onCapturedRawAudioFrame(TRTCCloudDef.TRTCAudioFrame trtcAudioFrame) {
        if (!aaaSettingParam.isDumpCapturedRawAudioFrameFlag()) return;
        byte[] data = trtcAudioFrame.data;
        Message msg = Message.obtain();
        DumpPCMHandler.HandleEnty he = new DumpPCMHandler.HandleEnty(data, "noOne", enterRoomTime, filePCMName, folder);
        msg.what = 1;
        msg.obj = he;
        dumpPCMHandler.sendMessage(msg);
    }

    //本地采集并经过音频模块前处理后的音频数据回调。
    @Override
    public void onLocalProcessedAudioFrame(TRTCCloudDef.TRTCAudioFrame trtcAudioFrame) {

        if (!aaaSettingParam.isDumpLocalProcessedAudioFrameFlag()) return;
        byte[] data = trtcAudioFrame.data;
        Message msg = Message.obtain();
        DumpPCMHandler.HandleEnty he = new DumpPCMHandler.HandleEnty(data, "noOne", enterRoomTime, filePCMName, folder);
        msg.what = 2;
        msg.obj = he;
        dumpPCMHandler.sendMessage(msg);
    }

    //混音前的每一路远程用户的音频数据，
    // 即混音前的各路原始数据。例如，对某一路音频进行文字转换时，您必须使用该路音频的原始数据。
    @Override
    public void onRemoteUserAudioFrame(TRTCCloudDef.TRTCAudioFrame trtcAudioFrame, String userid) {
        if (!aaaSettingParam.isDumpRemoteUserAudioFrameFlag()) return;
        byte[] data = trtcAudioFrame.data;
        Message msg = Message.obtain();
        SharedPreferences sp = getSharedPreferences("info", MODE_PRIVATE);
        String time = sp.getString(userid, "1970");
        DumpPCMHandler.HandleEnty he = new DumpPCMHandler.HandleEnty(data, userid, time, filePCMName, folder);
        msg.what = 3;
        msg.obj = he;
        dumpPCMHandler.sendMessage(msg);
        Log.i("chaoli", "onRemoteUserAudioFrame: userid=" + userid);
    }


    //各路音频数据混合后送入喇叭播放的音频数据
    @Override
    public void onMixedPlayAudioFrame(TRTCCloudDef.TRTCAudioFrame trtcAudioFrame) {
        if (!aaaSettingParam.isDumpMixedPlayAudioFrameFlag()) return;
        byte[] data = trtcAudioFrame.data;
        Message msg = Message.obtain();
        DumpPCMHandler.HandleEnty he = new DumpPCMHandler.HandleEnty(data, "noOne", enterRoomTime, filePCMName, folder);
        msg.what = 4;
        msg.obj = he;
        dumpPCMHandler.sendMessage(msg);
    }


    //SDK所有音频数据混合后的数据回调（包括采集音频数据和所有播放音频数据）。
    @Override
    public void onMixedAllAudioFrame(TRTCCloudDef.TRTCAudioFrame trtcAudioFrame) {
        if (!aaaSettingParam.isDumpMixedAllAudioFrameFlag()) return;
        byte[] data = trtcAudioFrame.data;
        Message msg = Message.obtain();
        DumpPCMHandler.HandleEnty he = new DumpPCMHandler.HandleEnty(data, "noOne", enterRoomTime, filePCMName, folder);
        msg.what = 5;
        msg.obj = he;
        dumpPCMHandler.sendMessage(msg);
    }


    private void playPCMAudio() {
        playPCMAudioThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int bufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                Log.i("chaoli", "run: bufferSize=" + bufferSize);
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
                FileInputStream fis = null;
                try {
                    audioTrack.play();
                    fis = new FileInputStream(pushPCMPath);
                    byte[] buffer = new byte[bufferSize];
                    int len = 0;
                    while ((len = fis.read(buffer)) != -1 && !isStop) {
                        audioTrack.write(buffer, 0, len);
                    }

                } catch (Exception e) {
                    Log.e("chaoli", "playPCMRecord: e : " + e);
                } finally {
                    isStop = false;
                    if (audioTrack != null) {
                        audioTrack.stop();
                        audioTrack = null;
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        playPCMAudioThread.start();
    }


    public void pushCustomAudio() {
        pushCustomAudioThread = new Thread(new Runnable() {
            @Override
            public void run() {

                FileInputStream fis = null;
//                byte[] buffer = new byte[1280];
                byte[] buffer = new byte[3528];

                try {
                    fis = new FileInputStream(pushPCMPath);
                    while (fis.read(buffer) != -1 && !isStop) {
                        long l1 = System.currentTimeMillis();
                        TRTCCloudDef.TRTCAudioFrame frame = new TRTCCloudDef.TRTCAudioFrame();
                        frame.data = buffer;
                        frame.timestamp = 0;
//                        frame.sampleRate = 16000;
                        frame.sampleRate = 44100;
                        frame.channel = 2;
                        mTRTCCloud.sendCustomAudioData(frame);
                        long l2 = System.currentTimeMillis();
                        long i = l2 - l1;
//                        Log.i("chaoli", "run: l1=" + l1 + ";l2=" + l2 + "; i =" + i);
                        if (i < 20) {
                            Thread.sleep(20 - i);
                        }
                    }
                } catch (Exception e) {

                } finally {
                    isStop = false;
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        });
        pushCustomAudioThread.start();
    }


    private class TRTCCloudImplListener extends TRTCCloudListener {

        private WeakReference<RTCActivity> mContext;

        public TRTCCloudImplListener(RTCActivity activity) {
            super();
            mContext = new WeakReference<>(activity);
        }

        @Override
        public void onEnterRoom(long l) {
            super.onEnterRoom(l);
            if (capAudioType == 1) {
                playPCMAudio();
                pushCustomAudio();
            }
        }

        @Override
        public void onRemoteUserEnterRoom(String s) {
            super.onRemoteUserEnterRoom(s);
            SimpleDateFormat df = new SimpleDateFormat("MM-dd-HH-mm-ss");//设置日期格式
            String remoteEnterTime = df.format(new Date());// new Date()为获取当前系统时间
            SharedPreferences sp = getSharedPreferences("info", MODE_PRIVATE);
            SharedPreferences.Editor edit = sp.edit();
            edit.putString(s, remoteEnterTime);
            edit.commit();
        }


        @Override
        public void onUserVideoAvailable(String userId, boolean available) {

            int index = mRemoteUidList.indexOf(userId);
            if (available) {
                if (index != -1) { //如果mRemoteUidList有，就不重复添加
                    return;
                }
                mRemoteUidList.add(userId);
                refreshRemoteVideoViews();
            } else {
                if (index == -1) { //如果mRemoteUidList没有，说明已关闭画面
                    return;
                }
                /// 关闭用户userId的视频画面
                mTRTCCloud.stopRemoteView(userId);
                mRemoteUidList.remove(index);
                refreshRemoteVideoViews();
            }

        }

        @Override
        public void onUserVoiceVolume(ArrayList<TRTCCloudDef.TRTCVolumeInfo> arrayList, int totalVolume) {
            super.onUserVoiceVolume(arrayList, totalVolume);

            if (!showVoiceView) return;

            for (int i = 0; i < arrayList.size(); i++) {
                TRTCCloudDef.TRTCVolumeInfo trtcVolumeInfo = arrayList.get(i);
                if (trtcVolumeInfo.userId == mUserId) {
                    int volume = trtcVolumeInfo.volume;
                    refreshVolume(volume, totalVolume);

                }
            }


        }

        private void refreshRemoteVideoViews() {
            for (int i = 0; i < mRemoteViewList.size(); i++) {
                if (i < mRemoteUidList.size()) {
                    String remoteUid = mRemoteUidList.get(i);
                    mRemoteViewList.get(i).setVisibility(View.VISIBLE);
                    // 开始显示用户userId的视频画面
                    mTRTCCloud.startRemoteView(remoteUid, mRemoteViewList.get(i));
                } else {
                    mRemoteViewList.get(i).setVisibility(View.GONE);
                }
            }
            showDebugView(showVoiceView, false);
        }

        // 错误通知监听，错误通知意味着 SDK 不能继续运行
        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
            Log.d(TAG, "sdk callback onError");
            RTCActivity activity = mContext.get();
            if (activity != null) {
                Toast.makeText(activity, "onError: " + errMsg + "[" + errCode + "]", Toast.LENGTH_SHORT).show();
                if (errCode == TXLiteAVCode.ERR_ROOM_ENTER_FAIL) {
                    activity.exitRoom();
                }
            }
        }
    }


}
