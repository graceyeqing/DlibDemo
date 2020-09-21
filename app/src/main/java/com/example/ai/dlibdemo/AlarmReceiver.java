package com.example.ai.dlibdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * @author yeqing
 * @des 闹钟广播
 * @date 2020/9/9 16:41
 */
class AlarmReceiver extends BroadcastReceiver {

    @Override

    public void onReceive(Context context, Intent intent) {

        if("mlxx_alarm_notification.RING".equals(intent.getAction())){

            //跳转到Activity中

//            Intent intent2=new Intent(context,RingActivity.class);
//
//            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//            context.startActivity(intent2);
            Toast.makeText(context,"快开始了",Toast.LENGTH_SHORT).show();

        }

    }

}