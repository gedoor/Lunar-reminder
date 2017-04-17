package gedoor.kunfei.lunarreminder.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Stay
 *         在Application中统一捕获异常，保存到文件中下次再打开时上传
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    /**
     * CrashHandler实例
     */
    private static CrashHandler INSTANCE;
    /**
     * 系统默认的UncaughtException处理类
     */
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    /**
     * 程序的Context对象
     */
    private Context mContext;

    private Map<String, String> infos = new HashMap<>();
    private static boolean debug;

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }


    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    /**
     * 初始化,注册Context对象,
     * 获取系统默认的UncaughtException处理器,
     * 设置该CrashHandler为程序的默认处理器
     *
     * @param ctx
     */
    public void init(Context ctx, Boolean debug) {
        mContext = ctx;
        this.debug = debug;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {  //如果自己处理了异常，则不会弹出错误对话框，则需要手动退出app
            if (!(ex instanceof OutOfMemoryError)) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                }
                if (!debug) {
                    MobclickAgent.onKillProcess(mContext);
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(10);
                } else
                    mDefaultHandler.uncaughtException(thread, ex);
            }
        }
    }

    /**
     * 自定义错误处理,收集错误信息
     * 发送错误报告等操作均在此完成.
     * 开发者可以根据自己的情况来自定义异常处理逻辑
     *
     * @return true代表处理该异常，不再向上抛异常，
     * false代表不处理该异常(可以将该log信息存储起来)然后交给上层(这里就到了系统的异常处理)去处理，
     * 简单来说就是true不会弹出那个错误提示框，false就会弹出
     */
    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }
        if (ex instanceof OutOfMemoryError) {
            // OOM一般是解码Bitmap造成的，不影响程序继续运行
            return true;
        }
        if (debug) {
            ex.printStackTrace();
            Log.d("CrashHandler", "catched");
            return false;
        } else {
            //使用Toast来显示异常信息
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    //添加信息发送或本地保存
                    String error;
                    error = getMobileInfo();//获取手机信息
                    error = error + "\n" + getVersionInfo();//获取版本信息
                    error = error + "\n" + getErrorInfo(ex);//获取错误信息
                    ClipboardManager clipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setPrimaryClip(ClipData.newPlainText("error", error));
                    Toast.makeText(mContext, "程序出错,日志已复制到剪贴板", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

            }.start();
        }
        MobclickAgent.reportError(mContext, ex);
        return true;
    }

    /**
     * 获取异常信息
     * @param e
     * @return 异常信息
     */
    private String getErrorInfo(Throwable e) {
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        e.printStackTrace(pw);
        pw.flush();
        String error = writer.toString();
        return error;
    }

    /**
     * 获取设备信息
     */
    private String getMobileInfo() {
        StringBuffer sb = new StringBuffer();
        //通过反射获取设备信息
        try {
            Field[] fields = Build.class.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.e("UncaughtHandler", "has IllegalArgument error at method 'getMobileInfo()'");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e("UncaughtHandler", "has IllegalAccess error at method 'getMobileInfo()'");
        }
        return sb.toString();
    }

    /**
     * 获取手机
     */
    private String getVersionInfo() {
        try {
            PackageManager pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                return versionName + "-" + versionCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("UncaughtHandler", "has error at method 'getVersionInfo()'");
        }
        return "";
    }

}