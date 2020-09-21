package com.example.ai.dlibdemo.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;

/**
 * @author yeqing
 * @des
 * @date 2020/9/9 16:45
 */
public class AlarmUtil {

    private static AlarmManager am;

    private PendingIntent pendingIntent;

    private static NotificationManager notificationManager;
    //一次性闹钟

    public static void setAlarmOnce(final Context context){
        //获取闹钟管理器

        am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //获取通知管理器

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        //获取当前系统的时间

        Calendar calendar=Calendar.getInstance();

        //小时

        int hour=calendar.get(Calendar.HOUR_OF_DAY);

        //分钟

        int minute=calendar.get(Calendar.MINUTE);

        //1.弹出一个时间的对话框

        TimePickerDialog timePickerDialog=new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {

            @Override//2.获取到时间(选择的时针hourOfDay和分针minute

            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                //选择的时间

                Calendar c=Calendar.getInstance();

                c.set(Calendar.HOUR_OF_DAY,hourOfDay);

                c.set(Calendar.MINUTE,minute);

                //发送广播

                PendingIntent pendingIntent=PendingIntent.getBroadcast(context,0x101,new Intent("mlxx_alarm_notification.RING"),0);

                //3.设置闹钟 (RTC_WAKEUP唤醒屏幕)(getTimeInMillis14.48分的毫秒值)

                am.set(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pendingIntent);

            }

        },hour,minute,true);

        //弹框

        timePickerDialog.show();

    }

    //发送通知

    public static void sendNotify(Context context,View view){

        //实例化通知

        NotificationCompat.Builder builder=new NotificationCompat.Builder(context);

        //标题

        builder.setContentTitle("提示");

        //内容

        builder.setContentText("恭喜您，中奖啦！请先汇款到XXXXX-XXXXX-XXXXXX账号进行激活");

        //图标

        builder.setSmallIcon(android.R.drawable.star_big_off);

        //设置使用默认声音、震动、闪光灯

        builder.setDefaults(NotificationCompat.DEFAULT_ALL);

        Notification notification=builder.build();

//        //设置使用默认声音

//        builder.setDefaults(NotificationCompat.DEFAULT_SOUND);

//        //设置使用默认闪光灯

//        builder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);

//        //设置使用默认震动

//        builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);

        //发送通知

        notificationManager.notify(0x101,notification);

    }
}
