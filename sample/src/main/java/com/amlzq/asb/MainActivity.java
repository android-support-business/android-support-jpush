package com.amlzq.asb;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView mTVInfo;

    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 11:

                    break;
                default:
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTVInfo = findViewById(R.id.tv_info);
    }

    @Override
    protected void onDestroy() {
        // 清除以该Handler为target的所有Message（包括Callback）
        // Remove all Runnable and Message.
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

}
