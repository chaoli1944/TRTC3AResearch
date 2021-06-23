package com.chaoli.myaaasettingkit;

public class AAASettingParam {
    private int cusAudioMode;
    private int audioQuality;
    private int volumeType;
    private int aec;
    private int agc;
    private int ans;
    private int systemVOIPVolume;
    private int systemMediaVolume;
    private boolean dumpCapturedRawAudioFrameFlag;
    private boolean dumpLocalProcessedAudioFrameFlag;
    private boolean dumpRemoteUserAudioFrameFlag;
    private boolean dumpMixedPlayAudioFrameFlag;
    private boolean dumpMixedAllAudioFrameFlag;
    private int dumpAudioFormat;

    private static AAASettingParam instance;

    public static AAASettingParam getInstance() {
        if(instance == null){
            instance = new AAASettingParam();
        }
        return instance;
    }

    private AAASettingParam () {
        cusAudioMode =0;
        audioQuality =0;
        volumeType =2;
        aec =100;
        ans =120;
        agc =100;
        systemVOIPVolume =0;
        systemMediaVolume =0;
        dumpCapturedRawAudioFrameFlag =false;
        dumpLocalProcessedAudioFrameFlag =false;
        dumpRemoteUserAudioFrameFlag =false;
        dumpMixedPlayAudioFrameFlag =false;
        dumpMixedAllAudioFrameFlag =false;
        dumpAudioFormat =0;
    }

    public int getDumpAudioFormat() {
        return dumpAudioFormat;
    }

    public void setDumpAudioFormat(int dumpAudioFormat) {
        this.dumpAudioFormat = dumpAudioFormat;
    }

    public boolean isDumpMixedAllAudioFrameFlag() {
        return dumpMixedAllAudioFrameFlag;
    }

    public void setDumpMixedAllAudioFrameFlag(boolean dumpMixedAllAudioFrameFlag) {
        this.dumpMixedAllAudioFrameFlag = dumpMixedAllAudioFrameFlag;
    }

    public boolean isDumpMixedPlayAudioFrameFlag() {
        return dumpMixedPlayAudioFrameFlag;
    }

    public void setDumpMixedPlayAudioFrameFlag(boolean dumpMixedPlayAudioFrameFlag) {
        this.dumpMixedPlayAudioFrameFlag = dumpMixedPlayAudioFrameFlag;
    }

    public boolean isDumpRemoteUserAudioFrameFlag() {
        return dumpRemoteUserAudioFrameFlag;
    }

    public void setDumpRemoteUserAudioFrameFlag(boolean dumpRemoteUserAudioFrameFlag) {
        this.dumpRemoteUserAudioFrameFlag = dumpRemoteUserAudioFrameFlag;
    }

    public boolean isDumpLocalProcessedAudioFrameFlag() {
        return dumpLocalProcessedAudioFrameFlag;
    }

    public void setDumpLocalProcessedAudioFrameFlag(boolean dumpLocalProcessedAudioFrameFlag) {
        this.dumpLocalProcessedAudioFrameFlag = dumpLocalProcessedAudioFrameFlag;
    }

    public boolean isDumpCapturedRawAudioFrameFlag() {
        return dumpCapturedRawAudioFrameFlag;
    }

    public void setDumpCapturedRawAudioFrameFlag(boolean dumpCapturedRawAudioFrameFlag) {
        this.dumpCapturedRawAudioFrameFlag = dumpCapturedRawAudioFrameFlag;
    }

    public int getSystemMediaVolume() {
        return systemMediaVolume;
    }

    public void setSystemMediaVolume(int systemMediaVolume) {
        this.systemMediaVolume = systemMediaVolume;
    }

    public int getSystemVOIPVolume() {
        return systemVOIPVolume;
    }

    public void setSystemVOIPVolume(int systemVOIPVolume) {
        this.systemVOIPVolume = systemVOIPVolume;
    }

    public int getAns() {
        return ans;
    }

    public void setAns(int ans) {
        this.ans = ans;
    }

    public int getAgc() {
        return agc;
    }

    public void setAgc(int agc) {
        this.agc = agc;
    }

    public int getAec() {
        return aec;
    }

    public void setAec(int aec) {
        this.aec = aec;
    }

    public int getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(int volumeType) {
        this.volumeType = volumeType;
    }

    public int getAudioQuality() {
        return audioQuality;
    }

    public void setAudioQuality(int audioQuality) {
        this.audioQuality = audioQuality;
    }

    public int getCusAudioMode() {
        return cusAudioMode;
    }

    public void setCusAudioMode(int cusAudioMode) {
        this.cusAudioMode = cusAudioMode;
    }
}
