package com.tencent.rtc;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.chaoli.myaaasettingkit.AAAPanel;
import com.chaoli.myaaasettingkit.AAASettingParam;
import com.tencent.liteav.debug.Constant;
import com.tencent.rtc.R;
import com.tencent.rtmp.TXLiveBase;
import com.tencent.trtc.TRTCCloudDef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.tencent.rtc.R.*;

/**
 * RTC视频通话的入口页面（可以设置房间id和用户id）
 *
 * RTC视频通话是基于房间来实现的，通话的双方要进入一个相同的房间id才能进行视频通话
 */
public class RTCEntranceActivity extends AppCompatActivity {

    private EditText mInputUserId;
    private EditText mInputRoomId;
    private AAAPanel aaaPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_rtc_entrance);
        getSupportActionBar().hide();

        String sdkVersionStr = TXLiveBase.getSDKVersionStr();

        TextView tvServion = (TextView) findViewById(id.tv_version);
        ImageView iv_aaa_set = (ImageView) findViewById(id.iv_aaa_set);
        tvServion.setText(sdkVersionStr);

        aaaPanel = findViewById(R.id.anchor_aaa_panel);
        aaaPanel.initPanelDefaultBackground();
        AAASettingParam aaaSettingParam = AAASettingParam.getInstance();

        iv_aaa_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aaaPanel.setVisibility(View.VISIBLE);
                aaaPanel.showAAAPanel();
                aaaPanel.refView();
            }
        });

        mInputUserId = findViewById(id.et_input_username);
        mInputRoomId = findViewById(id.et_input_room_id);
        findViewById(id.bt_enter_room).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startEnterRoom(); // 开始进房
            }
        });
        findViewById(id.rtc_entrance_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideInput(); // 点击非EditText输入区域，隐藏键盘
            }
        });
        findViewById(id.entrance_ic_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();  // 返回结束
            }
        });
        mInputRoomId.setText("95279");
        String time = String.valueOf(System.currentTimeMillis());
        String userId = time.substring(time.length() - 8);
        mInputUserId.setText(userId);

        checkPermission();
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(RTCEntranceActivity.this,
                        permissions.toArray(new String[0]),
                        0x1000);
                return false;
            }
        }
        return true;
    }

    private void startEnterRoom() {

        if (TextUtils.isEmpty(mInputUserId.getText().toString().trim())
                || TextUtils.isEmpty(mInputRoomId.getText().toString().trim())) {
            Toast.makeText(RTCEntranceActivity.this, getString(string.rtc_room_input_error_tip), Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent(RTCEntranceActivity.this, RTCActivity.class);
        intent.putExtra(Constant.ROOM_ID, mInputRoomId.getText().toString().trim());
        intent.putExtra(Constant.USER_ID, mInputUserId.getText().toString().trim());
        startActivity(intent);
    }



    /**
     * 隐藏键盘
     */
    protected void hideInput() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View v = getWindow().peekDecorView();
        if (null != v) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

}
