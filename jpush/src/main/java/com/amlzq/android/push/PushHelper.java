package com.amlzq.android.push;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.amlzq.android.ApplicationConfig;
import com.amlzq.android.content.ContextHolder;
import com.amlzq.android.jpush.JPushUtil;
import com.amlzq.android.jpush.TagAliasOperatorHelper;
import com.amlzq.android.jpush.TagAliasOperatorHelper.TagAliasBean;
import com.amlzq.android.log.Log;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import cn.jiguang.api.JCoreManager;
import cn.jpush.android.api.JPushInterface;

/**
 * Created by amlzq on 2018/6/6.
 * Updated by amlzq on 2019/7/10.
 * <p>
 * 推送服务助手之
 * 极光推送
 */

public class PushHelper {

    // 单例对象
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

    // =============================================================================================
    // 在生命周期方法中执行的方法
    // 如：初始化
    // =============================================================================================

    /**
     * application#create中执行
     */
    public void onApplicationCreate(Application application) {
        init(application.getApplicationContext());
    }

    // =============================================================================================
    // 推送服务生命周期
    // 初始化，关闭，重启
    // =============================================================================================

    /**
     * 必须在Application#onCreate中注册推送服务
     */
    private void init(Context applicationContext) {
        Bundle bundle = getMetaDataBundle(applicationContext);
        Log.d("JPUSH_APPKEY: " + bundle.getString("JPUSH_APPKEY"));
        Log.d("JPUSH_CHANNEL: " + bundle.getString("JPUSH_CHANNEL"));
        JPushInterface.setDebugMode(ApplicationConfig.DEBUG); // 设置开启日志,发布时请关闭日志
        JPushInterface.init(applicationContext); // 初始化 JPush

        // 打印极光推送初始化信息
        Log.d("JPush debug state:" + JCoreManager.getDebugMode());
    }

    /**
     * 关闭推送
     */
    public void disable(Context applicationContext) {
        JPushInterface.stopPush(applicationContext);
    }

    /**
     * 关闭推送后，想要再次开启推送
     */
    public void enable(Context applicationContext) {
        JPushInterface.resumePush(applicationContext);
    }

    /**
     * @return 是否被停止推送
     */
    public boolean isEnable(Context context) {
        return JPushInterface.isPushStopped(context);
    }

    /**
     * @return 推送后台的设备注册ID
     */
    public String getRegistrationId() {
        Context cxt = ContextHolder.getContext();
        return JPushInterface.getRegistrationID(cxt);
    }

    // =============================================================================================
    // 设置推送
    // 标签，别名，手机号，通知栏样式，接收时间
    // =============================================================================================

    /**
     * 设置允许接收通知时间
     */
    private void setPushTime(PushOptions options) {
        int startTime = options.startTime;
        int endTime = options.endTime;
        if (startTime > endTime) {
            Toast.makeText(ContextHolder.getContext(), "开始时间不能大于结束时间", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuffer daysSB = new StringBuffer();
        Set<Integer> days = new HashSet<Integer>();
        if (options.saturday) {
            days.add(0);
            daysSB.append("0,");
        }
        if (options.monday) {
            days.add(1);
            daysSB.append("1,");
        }
        if (options.tuesday) {
            days.add(2);
            daysSB.append("2,");
        }
        if (options.wednesday) {
            days.add(3);
            daysSB.append("3,");
        }
        if (options.thursday) {
            days.add(4);
            daysSB.append("4,");
        }
        if (options.friday) {
            days.add(5);
            daysSB.append("5,");
        }
        if (options.saturday) {
            days.add(6);
            daysSB.append("6,");
        }

        //调用JPush api设置Push时间
        JPushInterface.setPushTime(ContextHolder.getContext(), days, startTime, endTime);

        SharedPreferences preferences = ContextHolder.getContext().
                getSharedPreferences(ApplicationConfig.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(IPush.PREFS_PUSH_DAYS_PER_WEEK, daysSB.toString());
        editor.putInt(IPush.PREFS_PUSH_START_TIME, startTime);
        editor.putInt(IPush.PREFS_PUSH_END_TIME, endTime);
        editor.commit();
        Toast.makeText(ContextHolder.getContext(), "设置成功", Toast.LENGTH_SHORT).show();
    }

    // =============================================================================================
    // 标签
    // =============================================================================================

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
    }

    /**
     * 将之前添加的标签中的一个或多个删除。
     *
     * @param tags 用户标签
     */
    public void deleteTag(String... tags) {
        Context cxt = ContextHolder.getContext();
    }

    // =============================================================================================
    // 别名
    // =============================================================================================

    /**
     * 设置用户别名,每个用户只能指定一个别名
     * 在用户登录后设置别名为userId
     */
    public void addAlias(String alias) {
        Context context = ContextHolder.getContext();
        if (TextUtils.isEmpty(alias)) {
            Toast.makeText(context, "Alias is empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!JPushUtil.isValidTagAndAlias(alias)) {
            Toast.makeText(context, "Invalid format", Toast.LENGTH_SHORT).show();
            return;
        }
        TagAliasBean aliasBean = new TagAliasBean();
        aliasBean.action = TagAliasOperatorHelper.ACTION_SET;
        TagAliasOperatorHelper.sequence++;
        aliasBean.alias = alias;
        aliasBean.isAliasAction = true;
        TagAliasOperatorHelper.getInstance().handleAction(context, TagAliasOperatorHelper.sequence, aliasBean);
    }

    public void checkAlias() {
        Context context = ContextHolder.getContext();
        TagAliasBean aliasBean = new TagAliasBean();
        aliasBean.action = TagAliasOperatorHelper.ACTION_GET;
        TagAliasOperatorHelper.sequence++;
        aliasBean.isAliasAction = true;
        TagAliasOperatorHelper.getInstance().handleAction(context, TagAliasOperatorHelper.sequence, aliasBean);
    }

    /**
     * 在用户登出后删除别名
     */
    public void deleteAlias() {
        Context context = ContextHolder.getContext();
        TagAliasBean aliasBean = new TagAliasBean();
        aliasBean.action = TagAliasOperatorHelper.ACTION_DELETE;
        TagAliasOperatorHelper.sequence++;
        aliasBean.isAliasAction = true;
        TagAliasOperatorHelper.getInstance().handleAction(context, TagAliasOperatorHelper.sequence, aliasBean);
    }

    // =============================================================================================
    // 无法归类的方法
    // =============================================================================================

    @Deprecated
    public void showToast_(final String toast, final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
    }

    /**
     * @amlzq move to utils lib
     */
    public boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    public Bundle getMetaDataBundle(Context context) {
        Bundle bundle = null;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            bundle = appInfo.metaData;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return bundle;
    }

}