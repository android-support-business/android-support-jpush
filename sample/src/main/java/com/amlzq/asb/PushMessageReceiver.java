package com.amlzq.asb;

import android.content.Context;

import com.amlzq.android.jpush.TagAliasOperatorHelper;
import com.amlzq.android.log.Log;

import cn.jpush.android.api.CmdMessage;
import cn.jpush.android.api.CustomMessage;
import cn.jpush.android.api.JPushMessage;
import cn.jpush.android.api.NotificationMessage;
import cn.jpush.android.service.JPushMessageReceiver;

/**
 * 推送通知接收器
 */
public class PushMessageReceiver extends JPushMessageReceiver {
    public static final String TAG = "PushMessageReceiver";

    @Override
    public void onMessage(Context context, CustomMessage message) {
        // 自定义消息到达之前
        Log.d(message.toString());
        processCustomMessage(context, message);
    }

    @Override
    public void onNotifyMessageArrived(Context context, NotificationMessage message) {
        // 消息到达
        Log.d(message.toString());
    }

    @Override
    public void onNotifyMessageOpened(Context context, NotificationMessage message) {
        // 消息打开事件
        Log.d(message.toString());
    }

    @Override
    public void onNotifyMessageDismiss(Context context, NotificationMessage message) {
        // 消息解除
        Log.d(message.toString());
    }

    @Override
    public void onRegister(Context context, String registrationId) {
        Log.d(registrationId);
    }

    @Override
    public void onConnected(Context context, boolean isConnected) {
        Log.d(isConnected);
    }

    @Override
    public void onCommandResult(Context context, CmdMessage message) {
        Log.d(message.toString());
    }

    @Override
    public void onTagOperatorResult(Context context, JPushMessage message) {
        TagAliasOperatorHelper.getInstance().onTagOperatorResult(context, message);
        super.onTagOperatorResult(context, message);
        Log.d(message.toString());
    }

    @Override
    public void onCheckTagOperatorResult(Context context, JPushMessage message) {
        TagAliasOperatorHelper.getInstance().onCheckTagOperatorResult(context, message);
        super.onCheckTagOperatorResult(context, message);
        Log.d(message.toString());
    }

    @Override
    public void onAliasOperatorResult(Context context, JPushMessage message) {
        TagAliasOperatorHelper.getInstance().onAliasOperatorResult(context, message);
        super.onAliasOperatorResult(context, message);
        Log.d(message.toString());
    }

    @Override
    public void onMobileNumberOperatorResult(Context context, JPushMessage message) {
        TagAliasOperatorHelper.getInstance().onMobileNumberOperatorResult(context, message);
        super.onMobileNumberOperatorResult(context, message);
        Log.d(message.toString());
    }

    // 处理自定义消息
    private void processCustomMessage(Context context, CustomMessage customMessage) {
        // 自定义消息使用场景举例：消息到达后，广播出去
    }

}
