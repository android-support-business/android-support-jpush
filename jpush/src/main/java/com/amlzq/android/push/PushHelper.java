package com.amlzq.android.push;

import android.app.Activity;
import android.content.Context;

import com.amlzq.android.content.ContextHolder;

import java.util.LinkedHashSet;
import java.util.Set;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by amlzq on 2018/6/6.
 * 极光推送集成库
 */

public class PushHelper {

    /**
     * @ReleaseCheck
     */
    public static boolean DEBUG = false;

    private static volatile PushHelper instance = null;

    public static PushHelper getInstance() {
        if (null == instance) {
            synchronized (PushHelper.class) {
                if (null == instance) {
                    instance = new PushHelper();
                }
            }
        }
        return instance;
    }

    /**
     * @hide
     */
    private PushHelper() {
    }

    private final String JPUSH_CHANNEL = "JPUSH_CHANNEL";

    /**
     * 初始化SDK<br>
     * application.create()中执行<br>
     */
    public void onApplicationCreate(String channel) {
        init(channel);
    }

    /**
     * 在activity.onCreate()中调用
     *
     * @param activity 上下文
     */
    public void onActivityCreate(final Activity activity) {

    }

    /**
     * at application or main activity init
     *
     * @param channel 渠道标识
     *                务必在工程的Application类的 onCreate() 方法中注册推送服务
     */
    private void init(String channel) {
        Context cxt = ContextHolder.getContext();
        JPushInterface.setDebugMode(DEBUG); // 设置开启日志,发布时请关闭日志
        JPushInterface.init(cxt); // 初始化 JPush
    }

    public static int sequence = 1;
    /**
     * 增加
     */
    public static final int ACTION_ADD = 1;
    /**
     * 覆盖
     */
    public static final int ACTION_SET = 2;
    /**
     * 删除部分
     */
    public static final int ACTION_DELETE = 3;
    /**
     * 删除所有
     */
    public static final int ACTION_CLEAN = 4;
    /**
     * 查询
     */
    public static final int ACTION_GET = 5;

    public static final int ACTION_CHECK = 6;

    public static final int DELAY_SEND_ACTION = 1;

    public static final int DELAY_SET_MOBILE_NUMBER_ACTION = 2;

    /**
     * 添加用户标签,推送时按照标签来筛选
     * 一般设置为用户群属性
     */
    public void addTag(String... tags) {
        Context cxt = ContextHolder.getContext();
        Set<String> tagSet = new LinkedHashSet<String>();
        for (String tag : tags) {
            tagSet.add(tag);
        }
        tagSet = JPushInterface.filterValidTags(tagSet);
        if (tagSet.isEmpty()) return;
        JPushInterface.addTags(cxt, sequence + ACTION_ADD, tagSet);
        JPushInterface.setTags(cxt, sequence + ACTION_SET, tagSet);
    }

    /**
     * 将之前添加的标签中的一个或多个删除。
     *
     * @param tags 用户标签
     */
    public void deleteTag(String... tags) {
        Context cxt = ContextHolder.getContext();
        JPushInterface.deleteAlias(cxt, sequence + ACTION_DELETE);
    }

    interface TAG_TYPE {
        // Channel list
        String SINA_WEIBO = "SINA_WEIBO";
    }

    /**
     * 设置用户别名,每个用户只能指定一个别名
     * 一般设置别名为userid
     */
    public void addAlias(String... alias) {
        Context cxt = ContextHolder.getContext();
        // 调用 JPush 接口来设置别名。
        JPushInterface.setAlias(cxt, sequence + ACTION_ADD, alias[0]);
    }

    /**
     * @return 推送后台的设备注册ID
     */
    public String getRegistrationId() {
        Context cxt = ContextHolder.getContext();
        return JPushInterface.getRegistrationID(cxt);
    }

    /**
     * 关闭推送
     */
    public void disable() {
        Context cxt = ContextHolder.getContext();
        JPushInterface.stopPush(cxt);
    }

    /**
     * 关闭推送后，想要再次开启推送
     */
    public void enable() {
        Context cxt = ContextHolder.getContext();
        JPushInterface.resumePush(cxt);
    }

    /**
     * @return 是否被停止推送
     */
    public boolean isEnable() {
        Context cxt = ContextHolder.getContext();
        return JPushInterface.isPushStopped(cxt);
    }

}