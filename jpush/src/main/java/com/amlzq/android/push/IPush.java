package com.amlzq.android.push;

import android.app.Activity;

/**
 * Created by amlzq on 2018/6/27.
 * 推送接口
 */

public interface IPush {

    // Prefs key
    /**
     * @amlzq move to utils lib
     */
    public static final String PREFS_PUSH_START_TIME = "push_start_time";
    public static final String PREFS_PUSH_END_TIME = "push_end_time";
    public static final String PREFS_PUSH_DAYS_PER_WEEK = "push_days_per_week";

    /**
     * 初始化SDK<br>
     * application.create()中执行<br>
     */
    void onApplicationCreate(String channel);

    void onApplicationCreate();

    /**
     * 在activity.onCreate()中调用
     *
     * @param activity 上下文
     */
    void onActivityCreate(final Activity activity);

    void init(final String channel);

    /**
     * 添加用户标签(tag),推送时按照标签来筛选
     */
    void addTag(String... tags);

    /**
     * 将之前添加的标签中的一个或多个删除。
     *
     * @param tags 用户标签
     */
    void deleteTag(String... tags);

    /**
     * 获取服务器端的所有标签
     */
    void getTag();

    /**
     * @param alias     设置用户别名（alias）
     * @param aliasType 别名类型
     */
    void addAlias(String alias, String aliasType);

    /**
     * @param alias     设置用户别名（alias）
     * @param aliasType 别名类型
     */
    void deleteAlias(String alias, String aliasType);

    /**
     * 关闭推送
     */
    void disable();

    /**
     * 关闭推送后，想要再次开启推送
     */
    void enable();

    // 应用内消息

    /**
     * 插屏消息
     *
     * @param activity
     * @param label
     */
    void showCardMessage(Activity activity, String label);

}
