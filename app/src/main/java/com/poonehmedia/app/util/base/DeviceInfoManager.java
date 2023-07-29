package com.poonehmedia.app.util.base;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.gson.JsonObject;
import com.najva.sdk.NajvaClient;
import com.poonehmedia.app.BuildConfig;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

import static android.content.Context.ACTIVITY_SERVICE;

@Singleton
public class DeviceInfoManager {

    private final String TAG = getClass().getSimpleName();
    private final Context context;

    @Inject
    public DeviceInfoManager(@ApplicationContext Context context) {
        this.context = context;
    }

    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        return formatSize(totalBlocks * blockSize);
    }

    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }

        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }

        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    @SuppressLint("HardwareIds")
    public String getUniquePsuedoID(int type) {
        String macaddress = "";
        String androidId = "";
        if (type == 0 || type == 1) {
            WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = null;
            if (manager != null) {
                info = manager.getConnectionInfo();
            }
            if (info != null) {
                macaddress = info.getMacAddress();
            }
        }

        if (type == 0 || type == 2)
            androidId = android.provider.Settings.Secure.getString(context.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);

        if (type == 1)
            return macaddress;

        if (type == 2)
            return androidId;

        String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10)
                + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10)
                + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10) + androidId + macaddress;

        return md5(m_szDevIDShort);
    }

    public String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                StringBuilder h = new StringBuilder(Integer.toHexString(0xFF & aMessageDigest));
                while (h.length() < 2)
                    h.insert(0, "0");
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public long getTotalRAM() {
        return getMemoryInfo(MemoryType.TOTAL);
    }

    private long getMemoryInfo(MemoryType type) {
        try {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            activityManager.getMemoryInfo(mi);
            long memory = 0;

            if (type == MemoryType.TOTAL)
                memory = mi.totalMem;
            else if (type == MemoryType.AVAILABLE)
                memory = mi.availMem;

            return memory / 0x100000L;
        } catch (Exception a) {
            Log.e("memoryInfo", "COULD NOT GET READ RAM INFO");
        }
        return 0;
    }

    /**
     * @return String because all methods that are calling this info will eventually need to stringify it
     * so JsonObject is used and then toString()
     */
    public String getDeviceFeatures() {
        TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        JsonObject temp = new JsonObject();

        if (tMgr != null) {
            temp.addProperty("simoperator", tMgr.getSimOperator());
            temp.addProperty("operatorname", tMgr.getSimOperatorName());
        }
        temp.addProperty("totalmemory", getTotalRAM());
        temp.addProperty("totalstorage", getTotalInternalMemorySize());
        temp.addProperty("deviceid", getUniquePsuedoID(0));
        temp.addProperty("wifimacaddress", getUniquePsuedoID(1));
        temp.addProperty("androidid", getUniquePsuedoID(2));
        temp.addProperty("androidversion", Build.VERSION.RELEASE);
        temp.addProperty("manufacture", Build.MANUFACTURER);
        temp.addProperty("brand", Build.BRAND);
        temp.addProperty("product", Build.PRODUCT);
        temp.addProperty("model", Build.MODEL);
        temp.addProperty("width", metrics.widthPixels);
        temp.addProperty("height", metrics.heightPixels);
        temp.addProperty("density", (int) (metrics.density * 160f));
        temp.addProperty("packagename", getPackageName());
        temp.addProperty("packageversion", getAppVersion());

        if (getNajvaApiKey() != null)
            temp.addProperty("notiftoken", NajvaClient.getInstance().getSubscribedToken());

        return temp.toString();
    }


    /**
     * @return String Application Id set in build.gradle
     */
    public String getPackageName() {
        return BuildConfig.APPLICATION_ID;
    }

    /**
     * @return String Application Version Name
     */
    public String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    /**
     * @return String Najva Api Key used to send notifications to specific devices.
     */
    public String getNajvaApiKey() {
        String najvaKey = null;
        Bundle metadata = null;
        try {
            metadata = context.getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (metadata != null) {
            najvaKey = (String) metadata.get("com.najva.sdk.metadata.API_KEY");
        }
        return najvaKey;
    }

    private enum MemoryType {TOTAL, AVAILABLE}
}