package com.chaoli.myaaasettingkit;

import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class AAAPanel extends FrameLayout {

    private Context mContext;

    private LinearLayout mMainPanel;

    private RadioGroup rg_audioCap;

    private RadioGroup rg_audioQuality;

    private RadioGroup rg_volumeType;

    private SeekBar sb_aec_level_panel;
    private SeekBar sb_ans_level_panel;
    private SeekBar sb_agc_level_panel;

    private TextView tv_aec_level_panel;
    private TextView tv_ans_level_panel;
    private TextView tv_agc_level_panel;

    private SeekBar sb_voip_volume;
    private SeekBar sb_media_volume;

    private TextView tv_voip_volume_max;
    private TextView tv_voip_volume;
    private TextView tv_media_volume_max;
    private TextView tv_media_volume;

    private CheckBox ch_capRaw;
    private CheckBox ch_locPro;
    private CheckBox ch_remUser;
    private CheckBox ch_mixPlay;
    private CheckBox ch_mixAll;

    private RadioGroup rg_dumpformat;

    private TextView tv_close_panel;

    private AAASettingParam aaaSettingParam;

    private static final String TAG = AAAPanel.class.getSimpleName();

    public AAAPanel(@NonNull Context context) {
        super(context);
        initialize(context);
    }

    public AAAPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    private void initialize(Context context) {
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.aec_agc_ans_panel, this);
        initView();
        initData();
        refView();
        setListener();

    }

    public void refView() {
        //通话音量
        voipVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        voipVolumeCurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        //音乐音量
        mediaVolumeMax = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mediaVolumeCurrent = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        tv_voip_volume_max.setText("通话音量" + voipVolumeMax);
        tv_media_volume_max.setText("媒体音量" + mediaVolumeMax);
        tv_media_volume.setText(mediaVolumeCurrent + "");
        tv_voip_volume.setText(voipVolumeCurrent + "");
        sb_media_volume.setMax(mediaVolumeMax);
        sb_voip_volume.setMax(voipVolumeMax);
        sb_media_volume.setProgress(mediaVolumeCurrent);
        sb_voip_volume.setProgress(voipVolumeCurrent);
    }

    int voipVolumeMax;
    int voipVolumeCurrent;
    int mediaVolumeMax;
    int mediaVolumeCurrent;
    AudioManager mAudioManager;

    private void initData() {
        aaaSettingParam = AAASettingParam.getInstance();

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

    }

    private void refVolumeTypeAndAAA(int volumeType, int aec, int ans, int agc) {
        if (volumeType == 1) {
            rg_volumeType.check(R.id.rb_VOIP);
        } else if (volumeType == 2) {
            rg_volumeType.check(R.id.rb_mediaAndVOIP);
        } else if (volumeType == 3) {
            rg_volumeType.check(R.id.rb_media);
        }

        sb_aec_level_panel.setProgress(aec);
        sb_agc_level_panel.setProgress(agc);
        sb_ans_level_panel.setProgress(ans);
    }

    private void setListener() {
        rg_audioCap.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_sdkCapAudio) {
                    aaaSettingParam.setCusAudioMode(0);
                } else if (checkedId == R.id.rb_cusCapAudio) {
                    aaaSettingParam.setCusAudioMode(1);
                }
            }
        });
        rg_audioQuality.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_speech) {
                    aaaSettingParam.setAudioQuality(1);
                    refVolumeTypeAndAAA(1, 100, 120, 100);

                } else if (checkedId == R.id.rb_default) {
                    aaaSettingParam.setAudioQuality(2);
                    refVolumeTypeAndAAA(2, 100, 100, 100);

                } else if (checkedId == R.id.rb_music) {
                    aaaSettingParam.setAudioQuality(3);
                    refVolumeTypeAndAAA(3, 100, 30, 0);

                }
            }


        });
        rg_volumeType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_VOIP) {
                    aaaSettingParam.setVolumeType(2);

                } else if (checkedId == R.id.rb_mediaAndVOIP) {
                    aaaSettingParam.setVolumeType(0);

                } else if (checkedId == R.id.rb_media) {
                    aaaSettingParam.setVolumeType(1);

                }
            }
        });
        sb_aec_level_panel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int aec = calcAAA(progress);
                aaaSettingParam.setAec(aec);
                tv_aec_level_panel.setText(aec + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sb_agc_level_panel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int agc = calcAAA(progress);
                aaaSettingParam.setAgc(agc);
                tv_agc_level_panel.setText(agc + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sb_ans_level_panel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int ans = calcAAA(progress);
                aaaSettingParam.setAns(ans);
                tv_ans_level_panel.setText(ans + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sb_voip_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress==0){
                    progress+=1;
                }
                tv_voip_volume.setText(progress + "");
                mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, progress, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sb_media_volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                aaaSettingParam.setAns(progress);
                tv_media_volume.setText(progress + "");
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ch_capRaw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    aaaSettingParam.setDumpCapturedRawAudioFrameFlag(true);
                } else {
                    aaaSettingParam.setDumpCapturedRawAudioFrameFlag(false);
                }
            }
        });
        ch_locPro.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    aaaSettingParam.setDumpLocalProcessedAudioFrameFlag(true);
                } else {
                    aaaSettingParam.setDumpLocalProcessedAudioFrameFlag(false);
                }
            }
        });
        ch_remUser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    aaaSettingParam.setDumpRemoteUserAudioFrameFlag(true);
                } else {
                    aaaSettingParam.setDumpRemoteUserAudioFrameFlag(false);
                }
            }
        });
        ch_mixPlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    aaaSettingParam.setDumpMixedPlayAudioFrameFlag(true);
                } else {
                    aaaSettingParam.setDumpMixedPlayAudioFrameFlag(false);
                }
            }
        });
        ch_mixAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    aaaSettingParam.setDumpMixedAllAudioFrameFlag(true);
                } else {
                    aaaSettingParam.setDumpMixedAllAudioFrameFlag(false);
                }
            }
        });
        rg_dumpformat.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_dumpPCM) {
                    aaaSettingParam.setDumpAudioFormat(0);
                } else if (checkedId == R.id.rb_dumpWAV) {
                    aaaSettingParam.setDumpAudioFormat(1);
                }
            }
        });
    }

    private synchronized int calcAAA(int progress) {
        int a = -1;
        if (progress >= 0 && progress <= 10) {
            a = 0;
        } else if (progress > 10 && progress <= 40) {
            a = 30;
        } else if (progress > 40 && progress <= 70) {
            a = 60;
        } else if (progress > 70 && progress <= 110) {
            a = 100;
        } else {
            a = 120;
        }
        return a;
    }

    private void initView() {
        mMainPanel = findViewById(R.id.ll_aaa_panel);
        rg_audioCap = findViewById(R.id.rg_audioCap);
        rg_audioQuality = findViewById(R.id.rg_audioQuality);
        rg_volumeType = findViewById(R.id.rg_volumeType);
        sb_aec_level_panel = findViewById(R.id.sb_aec_level_panel);
        sb_ans_level_panel = findViewById(R.id.sb_ans_level_panel);
        sb_agc_level_panel = findViewById(R.id.sb_agc_level_panel);
        tv_aec_level_panel = findViewById(R.id.tv_aec_level_panel);
        tv_ans_level_panel = findViewById(R.id.tv_ans_level_panel);
        tv_agc_level_panel = findViewById(R.id.tv_agc_level_panel);
        sb_voip_volume = findViewById(R.id.sb_viop_volume);
        sb_media_volume = findViewById(R.id.sb_media_volume);
        tv_voip_volume_max = findViewById(R.id.tv_voip_volume_max);
        tv_voip_volume = findViewById(R.id.tv_voip_volume);
        tv_media_volume_max = findViewById(R.id.tv_media_volume_max);
        tv_media_volume = findViewById(R.id.tv_media_volume);
        ch_capRaw = findViewById(R.id.ch_capRaw);
        ch_locPro = findViewById(R.id.ch_locPro);
        ch_remUser = findViewById(R.id.ch_remUser);
        ch_mixPlay = findViewById(R.id.ch_mixPlay);
        ch_mixAll = findViewById(R.id.ch_mixAll);
        rg_dumpformat = findViewById(R.id.rg_dumpformat);
        tv_close_panel = findViewById(R.id.tv_close_panel);

        tv_close_panel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainPanel.setVisibility(View.GONE);
            }
        });

    }

    public void initPanelDefaultBackground() {
        mMainPanel.setBackground(getResources().getDrawable(R.drawable.audio_effect_setting_bg_gradient));
    }

    public void showAAAPanel() {
        mMainPanel.setVisibility(VISIBLE);
        rg_audioQuality.check(R.id.rb_speech);
    }

    public void hideAAAPanel() {
        mMainPanel.setVisibility(GONE);
    }
}
