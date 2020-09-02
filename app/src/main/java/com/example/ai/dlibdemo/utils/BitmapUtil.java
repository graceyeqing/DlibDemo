package com.example.ai.dlibdemo.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;

/**
 * @author yeqing
 * @des
 * @date 2020/7/10 10:48
 */
public class BitmapUtil {

    /**
     *质量压缩
     * 质量压缩不会减少图片的像素，它是在保持像素的前提下改变图片的位深及透明度，来达到压缩图片的目的，图片的长，宽，像素都不会改变，那么bitmap所占内存大小是不会变的。
     * 我们可以看到有个参数：quality，可以调节你压缩的比例，但是还要注意一点就是，质量压缩堆png格式这种图片没有作用，因为png是无损压缩。
     * 图片的质量压缩，会改变图片在磁盘中的大小（File文件的大小），不会改变图片在加载时，在内存的大小。
     * @param bitmap
     * @return
     */
    public static Bitmap compressQuality(Bitmap bitmap) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bytes = bos.toByteArray();
        Bitmap bit = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bit;

    }


    /**
     * 采样率压缩,通过调节其inSampleSize参数，比如调节为2，宽高会为原来的1/2，内存变回原来的1/4.
     * @param picPath
     * @return
     */
    public static Bitmap compressSampling(String  picPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        Bitmap bitmap = BitmapFactory.decodeFile(picPath,options);
        return bitmap;
    }

    public static Bitmap compress(String  picPath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(picPath,options);
        return bitmap;
    }


    /**
     *缩放法压缩
     * @param bitmap
     * @return
     */
    public static Bitmap compressMatrix(Bitmap bitmap) {

        Matrix matrix = new Matrix();
        matrix.setScale(0.5f, 0.5f);
        Bitmap bit = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return bit;

    }


    /**
     *RGB_565压缩
     * @param picPath
     * @return
     */
    public static Bitmap compressRGB565(String  picPath) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(picPath,options);
        return bitmap;

    }

    /**
     * 指定大小压缩
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap compressScaleBitmap(Bitmap bitmap,int width,int height) {
        Bitmap bit = Bitmap.createScaledBitmap(bitmap, 600, 900, true);
        return bit;
    }

}
