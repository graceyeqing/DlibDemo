package com.example.ai.dlibdemo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ai.dlibdemo.databinding.ActivityFaceChangeBinding;
import com.example.ai.dlibdemo.utils.AlarmUtil;
import com.example.ai.dlibdemo.utils.BitmapUtil;
import com.example.ai.dlibdemo.utils.CalendarReminderUtils;
import com.tzutalin.dlib.Constants;
import com.tzutalin.dlib.FaceDet;
import com.tzutalin.dlib.VisionDetRet;
import com.yanzhenjie.album.Action;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Inflater;

import static com.example.ai.dlibdemo.MainActivity.getOrientationFromJPEGFile;

/**
 * @author yeqing
 * @des
 * @date 2020/9/2 11:16
 */
public class FaceChangeActivity extends AppCompatActivity {
    static {
        System.loadLibrary("native-lib");
    }
    //加速检测
    private boolean isFast;
    FaceDet faceDet;
    List<VisionDetRet> results;
    List<Point> points;

    List<VisionDetRet> results2;
    List<Point> points2;

    private String mPicFile;
    private String mPicFile2;

    //图片大小
    private int height = 1800;
    private int width = 1200;

    //加速检测时 图片的缩放倍数
    private static int SCALE = 5;
    private int scale = 1;

    //人脸框
    int rectLeft;
    int rectTop;
    int rectRight;
    int rectBottom;

    private ActivityFaceChangeBinding binding;

    public static final BitmapFactory.Options OPTION_RGBA8888 = new BitmapFactory.Options();
    public static final BitmapFactory.Options OPTION_A8 = new BitmapFactory.Options();
    static {
        // Android's Bitmap.Config.ARGB_8888 is misleading, its memory layout is RGBA, as shown in
        // JNI's macro ANDROID_BITMAP_FORMAT_RGBA_8888, and getPixel() returns ARGB format.
        OPTION_RGBA8888.inPreferredConfig = Bitmap.Config.ARGB_8888;
        OPTION_RGBA8888.inDither = false;
        OPTION_RGBA8888.inMutable = true;
        OPTION_RGBA8888.inPremultiplied = false;

        OPTION_A8.inPreferredConfig = Bitmap.Config.ALPHA_8;
        OPTION_A8.inDither = false;
        OPTION_A8.inMutable = true;
        OPTION_A8.inPremultiplied = false;
    }

    //日历相关uri
    private static String CALENDER_URL = "content://com.android.calendar/calendars";
    private static String CALENDER_EVENT_URL = "content://com.android.calendar/events";
    private static String CALENDER_REMINDER_URL = "content://com.android.calendar/reminders";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.inflate(LayoutInflater.from(this),R.layout.activity_face_change,null,false);
        setContentView(binding.getRoot());

        initFaceDet();

        binding.ivSource1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(binding.ivSource1,true);
            }
        });
        binding.ivSource2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage(binding.ivSource2,false);
            }
        });

        //关键点检测
        binding.btnDetection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(mPicFile)){
                    Bitmap bitmap1 = decodeFile(mPicFile,OPTION_RGBA8888);
                    detection(bitmap1,binding.ivSource1,true);
                }

                if(!TextUtils.isEmpty(mPicFile)){
                    Bitmap bitmap2 = decodeFile(mPicFile2,OPTION_RGBA8888);
                    detection(bitmap2,binding.ivSource2,false);
                }

            }
        });

        //人脸变换
        binding.btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(mPicFile) && !TextUtils.isEmpty(mPicFile)){
                    Bitmap bitmap1 = decodeFile(mPicFile,OPTION_RGBA8888);
                    Bitmap bitmap2 = decodeFile(mPicFile2,OPTION_RGBA8888);
                    int[] pointsArray1 = new int[points.size() * 2];

                    for (int i = 0; i < points.size(); i++) {
                        pointsArray1[2 * i] = points.get(i).x * scale;
                        pointsArray1[2 * i + 1] = points.get(i).y * scale;
                    }

                    int[] pointsArray2 = new int[points2.size() * 2];

                    for (int i = 0; i < points2.size(); i++) {
                        pointsArray2[2 * i] = points2.get(i).x * scale;
                        pointsArray2[2 * i + 1] = points2.get(i).y * scale;
                    }

                    Bitmap bitmap = faceExchange(bitmap1,bitmap2,pointsArray1,pointsArray2);
                    binding.image.setImageBitmap(bitmap);
                }

            }
        });

        //添加日历提醒
        binding.btnRemindMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CalendarReminderUtils.addCalendarEvent(FaceChangeActivity.this,"美丽修行 黛珂保湿精华试用即将开始","吃了饭再去",System.currentTimeMillis()+15*1000,0);
                CalendarReminderUtils.addCalendarEvent(FaceChangeActivity.this,"美丽修行 雅诗兰黛试用申请即将开始","吃了饭再去",System.currentTimeMillis()+15*1000,0);
            }
        });

        //添加闹钟提醒
//        AlarmUtil.setAlarmOnce(FaceChangeActivity.this);
        binding.btnRemindMe2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                intent.putExtra(AlarmClock.EXTRA_HOUR, 17);
                intent.putExtra(AlarmClock.EXTRA_MINUTES, 16);
                intent.putExtra(AlarmClock.EXTRA_MESSAGE, "hello world");
                intent.putExtra(AlarmClock.EXTRA_VIBRATE, true);
                intent.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }


            }
        });

    }

    //初始化人脸检测模型
    private void initFaceDet() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                faceDet = new FaceDet(Constants.getFaceShapeModelPath());
            }
        }).start();
    }

    //检测特征点
    private void detection(Bitmap bitmap, ImageView imageView,boolean isFirst) {
        if (faceDet == null) {
            Toast.makeText(this, "模型还未初始化好", Toast.LENGTH_SHORT).show();
            return;
        }
        Long start = System.currentTimeMillis();
        if (bitmap != null) {
            if(isFirst){
                results = faceDet.detect(bitmap);
                for (final VisionDetRet ret : results) {
                    points = ret.getFaceLandmarks();
//                    rectLeft = ret.getLeft() * scale;
//                    rectTop = ret.getTop() * scale;
//                    rectRight = ret.getRight() * scale;
//                    rectBottom = ret.getBottom() * scale;
                }
                drawKeyPoint(bitmap,imageView,points);
            }else{
                results2 = faceDet.detect(bitmap);
                for (final VisionDetRet ret : results2) {
                    points2 = ret.getFaceLandmarks();
//                    rectLeft = ret.getLeft() * scale;
//                    rectTop = ret.getTop() * scale;
//                    rectRight = ret.getRight() * scale;
//                    rectBottom = ret.getBottom() * scale;
                }
                drawKeyPoint(bitmap,imageView,points2);
            }

        }
        Long end = System.currentTimeMillis();
        if (points.isEmpty()) {
            Toast.makeText(this, "检测失败", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(FaceChangeActivity.this, "检测特征点完成 耗时" + (end - start) + "ms", Toast.LENGTH_LONG)
                .show();
        Log.i("taoran", "检测特征点完成  耗时" + (end - start) + "ms");



    }

    private void drawKeyPoint(Bitmap bitmap, ImageView imageView,List<Point> facePoints){
        //画出关键点
        if (facePoints.isEmpty()) {
            Toast.makeText(this, "还未检测特征点", Toast.LENGTH_SHORT).show();
            return;
        }
        if(bitmap != null){
            Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(mutableBitmap);
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            for (int i = 0; i < facePoints.size(); i++)
            {
                //将68个特征点画到图片上
                canvas.drawCircle(facePoints.get(i).x,facePoints.get(i).y, 4, paint);
            }
            imageView.setImageBitmap(mutableBitmap);
        }

    }

    private void selectImage(final ImageView imageView, final boolean isFirst) {
        Album.image(this)
                .singleChoice() // Multi-Mode, Single-Mode: singleChoice().
                .columnCount(4) // The number of columns in the page list.
                .camera(true) // Whether the camera appears in the Item.
                .onResult(new Action<ArrayList<AlbumFile>>() {
                    @Override
                    public void onAction(@NonNull ArrayList<AlbumFile> result) {

                        if (result != null && result.size() > 0) {
                            if(isFirst){
                                mPicFile = result.get(0).getPath();
                            }else{
                                mPicFile2 = result.get(0).getPath();
                            }
                            String mPicFilePath = result.get(0).getPath();
                            Bitmap bitmap = BitmapUtil.compress(mPicFilePath);
                            imageView.setImageBitmap(bitmap);
                        }
                    }
                })
                .onCancel(new Action<String>() {
                    @Override
                    public void onAction(@NonNull String result) {
                        // The user canceled the operation.
                    }
                })
                .start();
    }

    public static Bitmap decodeFile(@NonNull String pathName, @Nullable BitmapFactory.Options
            opts) {
        Bitmap bitmap = BitmapFactory.decodeFile(pathName, opts);

        final String lowerPathName = pathName.toLowerCase();
        boolean isJPEG = lowerPathName.endsWith(".jpg") || lowerPathName.endsWith(".jpeg");
        if (isJPEG) {
            Matrix matrix = getOrientationFromJPEGFile(pathName);
            Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap
                    .getHeight(), matrix, true);

            bitmap.recycle();
            return rotated;
        }

        return bitmap;
    }

    /**
     * 人脸交换本地方法
     */
    public native Bitmap faceExchange(Bitmap source1, Bitmap source2,
                                    int[] points1, int[] points2);

}
