package com.amlzq.android.push;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import android.widget.Toast;

import com.amlzq.android.ApplicationConfig;
import com.amlzq.android.log.Log;

import java.util.Locale;

import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.JPushMessage;

/**
 * Created by amlzq on 2018/6/6.
 * Updated by amlzq on 2019/5/21.
 * <p>
 * 极光推送集成库
 * <p>
 * from TagAliasOperatorHelper，
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
     * 务必在工程的Application类的 onCreate() 方法中注册推送服务
     */
    private void init(Context applicationContext) {
        Bundle bundle = getMetaDataBundle(applicationContext);
        Log.d("JPUSH_APPKEY: " + bundle.getString("JPUSH_APPKEY"));
        Log.d("JPUSH_CHANNEL: " + bundle.getString("JPUSH_CHANNEL"));
        JPushInterface.setDebugMode(ApplicationConfig.DEBUG); // 设置开启日志,发布时请关闭日志
        JPushInterface.init(applicationContext); // 初始化 JPush
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

    interface TAG_TYPE {
        // Channel list
        String SINA_WEIBO = "SINA_WEIBO";
    }

    // =============================================================================================
    // 标签/别名
    // =============================================================================================

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

    private SparseArray<Object> setActionCache = new SparseArray<Object>();

    public Object get(int sequence) {
        return setActionCache.get(sequence);
    }

    public Object remove(int sequence) {
        return setActionCache.get(sequence);
    }

    public void put(int sequence, Object tagAliasBean) {
        setActionCache.put(sequence, tagAliasBean);
    }

    public void handleAction(Context context, int sequence, String mobileNumber) {
        put(sequence, mobileNumber);
        Log.d("sequence:" + sequence + ",mobileNumber:" + mobileNumber);
        JPushInterface.setMobileNumber(context, sequence, mobileNumber);
    }

    /**
     * 处理设置tag
     */
    public void handleAction(Context context, int sequence, TagAliasBean tagAliasBean) {
        init(context);
        if (tagAliasBean == null) {
            Log.w("tagAliasBean was null");
            return;
        }
        put(sequence, tagAliasBean);
        if (tagAliasBean.isAliasAction) {
            switch (tagAliasBean.action) {
                case ACTION_GET:
                    JPushInterface.getAlias(context, sequence);
                    break;
                case ACTION_DELETE:
                    JPushInterface.deleteAlias(context, sequence);
                    break;
                case ACTION_SET:
                    JPushInterface.setAlias(context, sequence, tagAliasBean.alias);
                    break;
                default:
                    Log.w("unsupport alias action type");
                    return;
            }
        } else {
            switch (tagAliasBean.action) {
                case ACTION_ADD:
                    JPushInterface.addTags(context, sequence, tagAliasBean.tags);
                    break;
                case ACTION_SET:
                    JPushInterface.setTags(context, sequence, tagAliasBean.tags);
                    break;
                case ACTION_DELETE:
                    JPushInterface.deleteTags(context, sequence, tagAliasBean.tags);
                    break;
                case ACTION_CHECK:
                    //一次只能check一个tag
                    String tag = (String) tagAliasBean.tags.toArray()[0];
                    JPushInterface.checkTagBindState(context, sequence, tag);
                    break;
                case ACTION_GET:
                    JPushInterface.getAllTags(context, sequence);
                    break;
                case ACTION_CLEAN:
                    JPushInterface.cleanTags(context, sequence);
                    break;
                default:
                    Log.w("unsupport tag action type");
                    return;
            }
        }
    }

    // =============================================================================================
    // 处理消息
    // =============================================================================================

    private String getActionStr(int actionType) {
        switch (actionType) {
            case ACTION_ADD:
                return "add";
            case ACTION_SET:
                return "set";
            case ACTION_DELETE:
                return "delete";
            case ACTION_GET:
                return "get";
            case ACTION_CLEAN:
                return "clean";
            case ACTION_CHECK:
                return "check";
        }
        return "unkonw operation";
    }

    private String getRetryStr(boolean isAliasAction, int actionType, int errorCode) {
        String str = "Failed to %s %s due to %s. Try again after 60s.";
        str = String.format(Locale.ENGLISH, str, getActionStr(actionType), (isAliasAction ? "alias" : " tags"), (errorCode == 6002 ? "timeout" : "server too busy"));
        return str;
    }

    final Handler delaySendHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case DELAY_SEND_ACTION:
                    if (msg.obj != null && msg.obj instanceof TagAliasBean) {
                        Log.i("on delay time");
                        sequence++;
                        TagAliasBean tagAliasBean = (TagAliasBean) msg.obj;
                        setActionCache.put(sequence, tagAliasBean);
//                        if (context != null) {
//                            handleAction(context, sequence, tagAliasBean);
//                        } else {
                        Log.e("#unexcepted - context was null");
//                        }
                    } else {
                        Log.w("#unexcepted - msg obj was incorrect");
                    }
                    break;
                case DELAY_SET_MOBILE_NUMBER_ACTION:
                    if (msg.obj != null && msg.obj instanceof String) {
                        Log.i("retry set mobile number");
                        sequence++;
                        String mobileNumber = (String) msg.obj;
                        setActionCache.put(sequence, mobileNumber);
//                        if (context != null) {
//                            handleAction(context, sequence, mobileNumber);
//                        } else {
                        Log.e("#unexcepted - context was null");
//                        }
                    } else {
                        Log.w("#unexcepted - msg obj was incorrect");
                    }
                    break;
            }
            return true;
        }
    });

    public void onTagOperatorResult(Context context, JPushMessage jPushMessage) {
        int sequence = jPushMessage.getSequence();
        Log.d("action - onTagOperatorResult, sequence:" + sequence + ",tags:" + jPushMessage.getTags());
        Log.d("tags size:" + jPushMessage.getTags().size());
        init(context);
        //根据sequence从之前操作缓存中获取缓存记录
        TagAliasBean tagAliasBean = (TagAliasBean) setActionCache.get(sequence);
        if (tagAliasBean == null) {
            showToast("获取缓存记录失败", context);
            return;
        }
        if (jPushMessage.getErrorCode() == 0) {
            Log.i("action - modify tag Success,sequence:" + sequence);
            setActionCache.remove(sequence);
            String logs = getActionStr(tagAliasBean.action) + " tags success";
            Log.i(logs);
//            showToast(logs, context);
        } else {
            String logs = "Failed to " + getActionStr(tagAliasBean.action) + " tags";
            if (jPushMessage.getErrorCode() == 6018) {
                //tag数量超过限制,需要先清除一部分再add
                logs += ", tags is exceed limit need to clean";
            }
            logs += ", errorCode:" + jPushMessage.getErrorCode();
            Log.e(logs);
            if (!retryActionIfNeeded(context, jPushMessage.getErrorCode(), tagAliasBean)) {
                showToast(logs, context);
            }
        }
    }

    public void onCheckTagOperatorResult(Context context, JPushMessage jPushMessage) {
        int sequence = jPushMessage.getSequence();
        Log.i("action - onCheckTagOperatorResult, sequence:" + sequence + ",checktag:" + jPushMessage.getCheckTag());
        init(context);
        //根据sequence从之前操作缓存中获取缓存记录
        TagAliasBean tagAliasBean = (TagAliasBean) setActionCache.get(sequence);
        if (tagAliasBean == null) {
            showToast("获取缓存记录失败", context);
            return;
        }
        if (jPushMessage.getErrorCode() == 0) {
            Log.i("tagBean:" + tagAliasBean);
            setActionCache.remove(sequence);
            String logs = getActionStr(tagAliasBean.action) + " tag " + jPushMessage.getCheckTag() + " bind state success,state:" + jPushMessage.getTagCheckStateResult();
            Log.i(logs);
//            showToast(logs, context);
        } else {
            String logs = "Failed to " + getActionStr(tagAliasBean.action) + " tags, errorCode:" + jPushMessage.getErrorCode();
            Log.e(logs);
            if (!retryActionIfNeeded(context, jPushMessage.getErrorCode(), tagAliasBean)) {
                showToast(logs, context);
            }
        }
    }

    /**
     * 操作别名后的结果
     *
     * @param context
     * @param jPushMessage
     */
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        int sequence = jPushMessage.getSequence();
        Log.i("action - onAliasOperatorResult, sequence:" + sequence + ",alias:" + jPushMessage.getAlias());
        init(context);
        //根据sequence从之前操作缓存中获取缓存记录
        TagAliasBean tagAliasBean = (TagAliasBean) setActionCache.get(sequence);
        if (tagAliasBean == null) {
            showToast("获取缓存记录失败", context);
            return;
        }
        if (jPushMessage.getErrorCode() == 0) {
            Log.i("action - modify alias Success,sequence:" + sequence);
            setActionCache.remove(sequence);
            String logs = getActionStr(tagAliasBean.action) + " alias success";
            Log.i(logs);
//            showToast(logs, context);
        } else {
            String logs = "Failed to " + getActionStr(tagAliasBean.action) + " alias, errorCode:" + jPushMessage.getErrorCode();
            Log.e(logs);
            if (!retryActionIfNeeded(context, jPushMessage.getErrorCode(), tagAliasBean)) {
                showToast(logs, context);
            }
        }
    }

    //设置手机号码回调
    public void onMobileNumberOperatorResult(Context context, JPushMessage jPushMessage) {
        int sequence = jPushMessage.getSequence();
        Log.i("action - onMobileNumberOperatorResult, sequence:" + sequence + ",mobileNumber:" + jPushMessage.getMobileNumber());
        init(context);
        if (jPushMessage.getErrorCode() == 0) {
            Log.i("action - set mobile number Success,sequence:" + sequence);
            setActionCache.remove(sequence);
        } else {
            String logs = "Failed to set mobile number, errorCode:" + jPushMessage.getErrorCode();
            Log.e(logs);
            if (!retrySetMObileNumberActionIfNeeded(context, jPushMessage.getErrorCode(), jPushMessage.getMobileNumber())) {
                showToast(logs, context);
            }
        }
    }

    private boolean retryActionIfNeeded(Context context, int errorCode, TagAliasBean tagAliasBean) {
        if (!isConnected(context)) {
            Log.w("no network");
            return false;
        }
        //返回的错误码为6002 超时,6014 服务器繁忙,都建议延迟重试
        if (errorCode == 6002 || errorCode == 6014) {
            Log.d("need retry");
            if (tagAliasBean != null) {
                Message message = new Message();
                message.what = DELAY_SEND_ACTION;
                message.obj = tagAliasBean;
                delaySendHandler.sendMessageDelayed(message, 1000 * 60);
                String logs = getRetryStr(tagAliasBean.isAliasAction, tagAliasBean.action, errorCode);
                showToast(logs, context);
                return true;
            }
        }
        return false;
    }

    private boolean retrySetMObileNumberActionIfNeeded(Context context, int errorCode, String mobileNumber) {
        if (!isConnected(context)) {
            Log.w("no network");
            return false;
        }
        //返回的错误码为6002 超时,6024 服务器内部错误,建议稍后重试
        if (errorCode == 6002 || errorCode == 6024) {
            Log.d("need retry");
            Message message = new Message();
            message.what = DELAY_SET_MOBILE_NUMBER_ACTION;
            message.obj = mobileNumber;
            delaySendHandler.sendMessageDelayed(message, 1000 * 60);
            String str = "Failed to set mobile number due to %s. Try again after 60s.";
            str = String.format(Locale.ENGLISH, str, (errorCode == 6002 ? "timeout" : "server internal error”"));
            showToast(str, context);
            return true;
        }
        return false;

    }

    // =============================================================================================
    // 无法归类的方法
    // =============================================================================================

    public void showToast(final String toast, final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();
    }

    public boolean isConnected(Context context) {
        ConnectivityManager conn = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = conn.getActiveNetworkInfo();
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