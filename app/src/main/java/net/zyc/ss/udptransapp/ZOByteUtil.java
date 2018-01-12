package net.zyc.ss.udptransapp;



import java.nio.ByteBuffer;

public class ZOByteUtil {
    /**
     * 获取无符号byte
     */
    public static int getUnsignedByte(byte var0) {
        int var1 = var0;
        if (var0 < 0) {
            var1 = var0 + 256;
        }
        return var1;
    }

    public static float bytesToFloat(byte[] var0) {
        return Float.intBitsToFloat(bytesToIntBits(var0));
    }

    public static float sBytes2Float(byte[] var0) {
        return Float.intBitsToFloat(sBytes2IntBits(var0));
    }

    public static int sBytes2IntBits(byte[] var0) {
        return (var0[0] & 255) << 24 | (var0[1] & 255) << 16 | (var0[2] & 255) << 8 | var0[3] & 255;
    }

    public static int bytesToIntBits(byte[] var0) {
        return (var0[3] & 255) << 24 | (var0[2] & 255) << 16 | (var0[1] & 255) << 8 | (var0[0] & 255);
    }


    //--------------发送数据转换----------------------
    public static byte[] short2Byte(short var0) {
        byte[] var1 = new byte[2];
        for (int var2 = 0; var2 < 2; ++var2) {
            var1[1 - var2] = (byte) (var0 >> 8 * (1 - var2));
        }
        return var1;
    }

    /**
     * long转换为高低位
     */
    public static byte[] toHL(long var0) {//000001529bd6ef03
        byte[] var1;
        (var1 = new byte[8])[0] = (byte) var0;
        var1[1] = (byte) (var0 >> 8);
        var1[2] = (byte) (var0 >> 16);
        var1[3] = (byte) (var0 >> 24);
        var1[4] = (byte) (var0 >> 32);
        var1[5] = (byte) (var0 >> 40);
        var1[6] = (byte) (var0 >> 48);
        var1[7] = (byte) (var0 >>> 56);
        return var1;
    }

    /**
     * short 转换为高低位
     */
    public static byte[] toHL(short var0) {
        byte[] var1;
        (var1 = new byte[2])[0] = (byte) var0;
        var1[1] = (byte) (var0 >>> 8);
        return var1;
    }

    /**
     * 字符串转换为协议对应的字节数组
     */
    public static byte[] str2Byte(String str) {
        byte[] byteStr = str.getBytes();
        ByteBuffer var2 = ByteBuffer.allocate(byteStr.length + 1);
        var2.put((byte) byteStr.length);
        var2.put(byteStr);
        return var2.array();
    }

//--------------接收数据转换-----------------------

    /**
     * 将两个字节转换为int
     *
     * @param var0 低位
     * @param var1 高位
     * @return int
     */
    public static int get2ByteToInt(byte var0, byte var1) {
        return var1 << 8 | getUnsignedByte(var0);
    }

    public static int getS2ByteToInt(byte heigh, byte low) {
        return heigh << 8 | getUnsignedByte(low);
    }

    /**
     * 将4个byte转为int, var1为起始位置
     */
    public static int get4ByteToInt(byte[] var0, int var1) {
        return var0[var1 + 3] << 24 | getUnsignedByte(var0[var1 + 2]) << 16 | getUnsignedByte(var0[var1 + 1]) << 8 | getUnsignedByte(var0[var1]);
    }


    public static int getS4ByteToInt(byte[] bytes) {
        return (bytes[0] & 0xff) << 24 | (bytes[1] & 0xff) << 16 | (bytes[2] & 0xff) << 8 | (bytes[3]) & 0xff;
    }

    /**
     * 将3个byte转为int, var1为起始位置
     */
    public static int get3ByteToInt(byte[] var0, int var1) {
        return getUnsignedByte(var0[var1 + 2]) << 16 | getUnsignedByte(var0[var1 + 1]) << 8 | getUnsignedByte(var0[var1]);
    }

    public static int bytes1ToIntBits(byte[] var0) {
        return (var0[0] & 255);
    }

    public static byte[] intBitsToBytes1(int var0) {
        byte[] var1 = new byte[]{(byte) (var0 & 255)};
        return var1;
    }

    public static byte[] sIntBitsToBytes4(int src) {
        byte[] result = new byte[4];
        result[0] = (byte) ((src >> 24) & 0xff);
        result[1] = (byte) ((src >> 16) & 0xff);
        result[2] = (byte) ((src >> 8) & 0xff);
        result[3] = (byte) (src & 0xff);
        return result;
    }

    public static byte[] bigIntBitsToBytes2(int src) {
        byte[] result = new byte[2];
        result[1] = (byte) ((src >> 8) & 0xff);
        result[0] = (byte) (src & 0xff);
        return result;
    }

    public static byte[] sIntBitsToBytes2(int src) {
        byte[] result = new byte[2];
        result[0] = (byte) ((src >> 8) & 0xff);
        result[1] = (byte) (src & 0xff);
        return result;
    }

    public static byte getCheckSum(byte[] var0) {
        if (null != var0 && var0.length > 0) {
            byte var1 = 0;

            for (int var2 = 0; var2 < var0.length - 1; ++var2) {
                int var3 = var1 + bytes1ToIntBits(new byte[]{var0[var2]});
                var1 = intBitsToBytes1(var3)[0];
            }

            return intBitsToBytes1(var1)[0];
        } else {
            return (byte) 0;
        }
    }

    public static boolean checkSumFailed(byte[] var0) {
        boolean checkSum = !(null != var0 && var0.length > 0) || getCheckSum(var0) != var0[var0.length - 1];
        if (checkSum) {
            ZOLogUtil.printHexArray_e("checkSumFail", var0);
        }
        return checkSum;
    }


    public static int bytes1ToInt(byte b) {
        return (b & 0xff);
    }

    public static byte intBitTobytes1(int i) {
        return (byte) (i & 0xff);
    }

    public static boolean isUsbDataHead(byte[] data) {
        // TODO: 2017/5/8   用到时打开
      /*  if (data[0] == Constant.USB_PROTOCAL_MAGIC_1 &&
                data[1] == Constant.USB_PROTOCAL_MAGIC_2 &&
                data[2] == Constant.USB_PROTOCAL_MAGIC_3 &&
                data[3] == Constant.USB_PROTOCAL_MAGIC_4) {
            return true;
        }*/
        return false;
    }

    public static byte[] copyBytes(byte[] var0, int var1, int var2) {
        byte[] var3 = new byte[var2];
        System.arraycopy(var0, var1, var3, 0, var2);
        return var3;
    }

    public static int bytesToIntBits(byte[] var0, int var1) {
        return var1 == 1 ? bytes1ToIntBits(var0) :
                (var1 == 2 ? bytes2ToIntBits(var0) :
                        (var1 == 3 ? bytes3ToIntBits(var0) :
                                (var1 == 4 ? bytesToIntBits(var0) : 0)));
    }

    public static int bytes2ToIntBits(byte[] var0) {
        return (var0[1] & 255) << 8 | (var0[0] & 255);
    }

    public static int bytes3ToIntBits(byte[] var0) {
        return (var0[2] & 255) << 16 | (var0[1] & 255) << 8 | (var0[0] & 255);
    }

    public static int bytes4ToIntBits(byte[] var0) {
        return (var0[3] & 255) << 24 | (var0[2] & 255) << 16 | (var0[1] & 255) << 8 | var0[0] & 255;
    }

    public static int bytes2ToIntBitsSigned(byte[] var0) {
        return var0[1] < 0 ? -65536 | (var0[1] & 255) << 8 | (var0[0] & 255) : (var0[1] & 255) << 8 | (var0[0] & 255);
    }

    public static int getOrder(byte[] data) {
        return get2ByteToInt(data[4], data[5]);
    }

    public static boolean checkContains(int order) {
        return true;
    }
}
