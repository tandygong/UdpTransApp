package net.zyc.ss.udptransapp;

import android.util.Log;


public class ZOLogUtil {
    public static final boolean sDebug = true;
    public static String sLogTag = "syx";


    public static void v(String msg) {
        if (sDebug) {
            Log.v(sLogTag, msg);
        }
    }

    public static void v(String tag, String msg) {
        if (sDebug) {
            Log.v(tag, msg);
        }
    }

    public static void v(String msg, Throwable e) {
        if (sDebug) {
            Log.v(sLogTag, msg, e);
        }
    }

    public static void d(String msg) {
        if (sDebug) {
            Log.d(sLogTag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (sDebug) {
            Log.d(tag, msg);
        }
    }

    public static void d(String msg, Throwable e) {
        if (sDebug) {
            Log.d(sLogTag, msg, e);
        }
    }


    public static void i(String msg) {
        if (sDebug) {
            Log.i(sLogTag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (sDebug) {
            Log.i(tag, msg);
        }
    }

    public static void i(String msg, Throwable e) {
        if (sDebug) {
            Log.i(sLogTag, msg, e);
        }
    }

    public static void w(String msg) {
        if (sDebug) {
            Log.w(sLogTag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (sDebug) {
            Log.w(tag, msg);
        }
    }

    public static void w(String msg, Throwable e) {
        if (sDebug) {
            Log.w(sLogTag, msg, e);
        }
    }

    public static void e(String msg) {
        if (sDebug) {
            Log.e(sLogTag, msg);
        }
    }
    public static void e(String tag, String msg) {
        if (sDebug) {
            Log.e(tag, msg);
        }
    }

    public static void e(String msg, Throwable e) {
        if (sDebug) {
            Log.e(sLogTag, msg, e);
        } else {
            Log.e(sLogTag, "ZOLogUtil msg is NULL");
        }
    }

    public static String getHexArrayString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte aData : data) {
            String hexStr = Integer.toHexString(aData);
            //sb.append("0x");
            sb.append(hexStr.length() >= 2 ? hexStr.substring(hexStr.length() - 2) : hexStr);
            sb.append(",");
        }
        if (sb.length() > 0) {
            sb.insert(0, "0x[");
            sb.replace(sb.lastIndexOf(","), sb.length(), "]");
        }
        return sb.toString();
    }

    public static String getHexArrayString(int[] data) {
        StringBuilder sb = new StringBuilder();
        for (int aData : data) {
            String hexStr = Integer.toHexString(aData);
            // sb.append("0x");
            sb.append(hexStr.length() >= 2 ? hexStr.substring(hexStr.length() - 2) : hexStr);
            sb.append(",");
        }
        if (sb.length() > 0) {
            sb.insert(0, "0x[");
            sb.replace(sb.lastIndexOf(","), sb.length(), "]");
        }
        return sb.toString();
    }

    public static String getFloatArrayString(float[] data) {
        StringBuilder sb = new StringBuilder();
        for (float aData : data) {
            sb.append(aData);
            sb.append(",");
        }
        if (sb.length() > 0) {
            sb.insert(0, "[");
            sb.replace(sb.lastIndexOf(","), sb.length(), "]");
        }
        return sb.toString();
    }

    public static void printFloatArray_e(String tag, float[] data) {
        ZOLogUtil.e(tag, getFloatArrayString(data));
    }


    public static void printHexArray_e(String tag, byte[] data) {
        String logStr = getHexArrayString(data);
        if (logStr.length() > 2000) {
            int times = (logStr.length() - 1) / 2000 + 1;
            ZOLogUtil.e("此log的长度", logStr.length() + "");
            for (int i = 0; i < times; i++) {
                if (2000 * (i + 1) < logStr.length()) {
                    ZOLogUtil.e(tag, logStr.substring(2000 * i, 2000 * (i + 1)) + "接下面");
                } else {
                    ZOLogUtil.e(tag, logStr.substring(2000 * i, logStr.length()) + "完毕");
                }

            }

        } else {
            ZOLogUtil.e(tag, logStr);
        }
    }


    public static void printHexArray_i(String tag, byte[] data) {
        ZOLogUtil.i(tag, getHexArrayString(data));
    }

    public static void printHexArray_v(String s, byte[] data) {
        ZOLogUtil.v(s, getHexArrayString(data));
    }

    public static void printHexArray_d(String s, byte[] data) {
        ZOLogUtil.d(s, getHexArrayString(data));
    }


}
