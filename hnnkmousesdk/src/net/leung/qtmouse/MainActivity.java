package net.leung.qtmouse;
import com.iflytek.VoiceWakeuperHelper;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import net.leung.qtmouse.MouseAccessibilityService;
import org.greenrobot.eventbus.EventBus;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.graphics.drawable.AnimationDrawable;
import android.os.CountDownTimer;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;
import android.Manifest;
import android.content.Intent;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tencent.bugly.beta.Beta;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.PicassoEngine;
import com.zhihu.matisse.filter.Filter;
import android.view.Window;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import net.leung.qtmouse.JniEvent;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import net.leung.qtmouse.FloatWindowManager;

import android.media.AudioManager;
import android.media.MediaPlayer;
import com.ryan.socketwebrtc.WebRTCClient;
import  net.leung.qtmouse.LoadingDialog;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.faceDemo.activity.faceMainActivity;
import com.faceDemo.activity.ClassifierActivity;
import com.faceDemo.activity.ActivityFaceList;
public class MainActivity extends org.qtproject.qt5.android.bindings.QtActivity  implements SensorEventListener  {


    private MyReceiver receiver;
    private static final int REQUEST_CODE_CHOOSE = 23;
        public boolean isAnimationFinish= false;
           private AudioManager audioManager;
           private SensorManager sensorManager = null;
           private boolean mRegister = false;
           private Sensor sensor = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoveApplication.getInstance().initActivity(this);
          EventBus.getDefault().register(this);
              audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
      //  requestWindowFeature(Window.FEATURE_NO_TITLE);
       getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                           WindowManager.LayoutParams.FLAG_FULLSCREEN );

                                           FrameLayout.LayoutParams params = new FrameLayout.LayoutParams
                                                     (FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                                             //设置顶部,左边布局
                                             params.gravity= Gravity.TOP|Gravity.LEFT;
                                           final  ImageView iv=new ImageView(this);
                                             addContentView(iv, params);
                                             final AnimationDrawable animationDrawable = new AnimationDrawable();
                                             animationDrawable.setOneShot(false);
                                             for (int i = 1; i <= 31; i++) {
                                                 int id = getResources().getIdentifier("loading_" + i, "drawable", getPackageName());
                                                 Drawable drawable = getResources().getDrawable(id);
                                                 animationDrawable.addFrame(drawable, 100);
                                             }
                                             iv.setBackground(animationDrawable);
                                                animationDrawable.start();
                                                CountDownTimer timer = new CountDownTimer(3100, 1000) {
                                                        public void onTick(long millisUntilFinished) {
                                                        }
                                                        public void onFinish() {
                                                            animationDrawable.stop();
                                                          iv.setVisibility(View.GONE);
                                                                  ((ViewGroup)iv.getParent()).removeView(iv);
                                                                  isAnimationFinish=true;
                                                     // FloatWindowManager.getInstance().applyOrShowFloatWindowResume(MainActivity.this);
                                                        }
                                                    };
                                                    timer.start();


                                                    receiver = new MyReceiver();
                                                                    IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

                                                                    registerReceiver(receiver, homeFilter);

           toggleSpeaker(true);
           new Thread(new Runnable() {
                     @Override
                     public void run() {
                         System.out.println(Thread.currentThread().getId());

                         while (true){
                             try {
                                 Thread.sleep(1000);
                             } catch (InterruptedException e) {
                                 e.printStackTrace();
                             }
                             runOnUiThread(new Runnable() {
                                 @Override
                                 public void run() {
                                     //UI操作
                                      if (isSoftShowing()==false&&mVoiceWakeuperHelper!=null){

                                          mVoiceWakeuperHelper.startListening();
                                      }
                                     //
                                 }
                             });

                         }
                     }
                 }).start();
//           sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//           sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
//           mRegister = sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);

    }
public boolean toggleSpeaker(boolean enable) {
    if (audioManager != null) {
        if (enable) {
            audioManager.setSpeakerphoneOn(true);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    AudioManager.STREAM_VOICE_CALL);
        } else {
            audioManager.setSpeakerphoneOn(false);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.STREAM_VOICE_CALL);
        }

        return true;
    }
    return false;

}
    //返回键按下时进入后台运行
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
      moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

public static void JniSelectPhoto() {

    Handler handler = new Handler(LoveApplication.getInstance().getMainLooper());
    handler.post(new Runnable() {
        @Override
        public void run() {
        ((MainActivity)LoveApplication.getInstance().getMainActivity()).select_photo();
        }
    });

 }

public void select_photo() {
    RxPermissions rxPermissions = new RxPermissions(this);
    rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe(aBoolean -> {
                if (aBoolean) {
                        Matisse.from(MainActivity.this)
                                .choose(MimeType.ofImage())
                                .theme(R.style.Matisse_Dracula)
                                .countable(false)
                                .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                                .maxSelectable(1)
                                .originalEnable(true)
                                .maxOriginalSize(10)
                                .imageEngine(new PicassoEngine())
                                .forResult(REQUEST_CODE_CHOOSE);

                } else {
                    Toast.makeText(MainActivity.this, "无权限", Toast.LENGTH_LONG).show();
                }
            }, Throwable::printStackTrace);
}
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
        //  Matisse.obtainResult(data)+
        //  mAdapter.setData(Matisse.obtainResult(data), Matisse.obtainPathResult(data));
      //    Log.e("OnActivityResult state", String.valueOf(Matisse.obtainOriginalState(data)));
     //     Log.e("OnActivityResult ",Matisse.obtainPathResult(data).get(0).toString());
               onSelectPhotoPath(Matisse.obtainPathResult(data).get(0).toString());
    }

}
@Override
protected void onDestroy() {
//    DaemonEnv.startServiceMayBind(WatchDogService.class);
//    LogUtils.i("Keep","SinglePixelActivity onDestroy");
MouseAccessibilityService.onStartClicked(0);
    super.onDestroy();

    EventBus.getDefault().unregister(this);
         sensorManager.unregisterListener(this);

}


private class MyReceiver extends BroadcastReceiver {

                private final String SYSTEM_DIALOG_REASON_KEY = "reason";
                private final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
                private final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

                @Override
                public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

                                if (reason == null)
                                        return;
                                // Home键
                                if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {

                                }

                                // 最近任务列表键
                                if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                                         moveTaskToBack(false);
                                     //   Toast.makeText(getApplicationContext(), "按了最近任务列表", Toast.LENGTH_SHORT).show();
                                }
                        }
                }
        }

public static void ResetMouse() {
onSelectReset();
}

public static native void   onSelectPhotoPath(String path);
public static native void   onSelectReset();
public static native void    onVoicePaste();
public static void VoicePaste() {
    if (isBackground==false){
       // onVoicePaste();

    }
 onVoicePaste();
}
static   boolean  isBackground=false;
boolean  isRunRoot=false;
@Override
protected void onResume() {
    super.onResume();
    if (isAnimationFinish==false&&isRunRoot==false){
                 isRunRoot=true;
                 execRootCmd("adb  shell");
                 execRootCmd("settings put secure enabled_accessibility_services net.leung.qtmouse/net.leung.qtmouse.MouseAccessibilityService");
                 execRootCmd("settings put secure accessibility_enabled 1");

             }
         if(FloatWindowManager.getInstance().checkAndApplyPermission(this)==false){
             execRootCmd("adb  shell");
             execRootCmd("settings put secure enabled_accessibility_services net.leung.qtmouse/net.leung.qtmouse.MouseAccessibilityService");
             execRootCmd("settings put secure accessibility_enabled 1");
             }
     if(isAnimationFinish){
         FloatWindowManager.getInstance().applyOrShowFloatWindowResume(MainActivity.this);
         }

    isBackground=false;
}

@Override
protected void onStop() {
    super.onStop();
    isBackground=true;
}
@Subscribe(threadMode = ThreadMode.MAIN)
public void onMouseMove(JniEvent event) {

    switch (event.eventType) {
        case JniEvent.ON_VOICE_PASTE:

            break;
            case JniEvent.ON_RESET_MOUSE:
            ResetMouse();
            break;
            case JniEvent.ON_WINDOW_CHANGE:

            break;
            case JniEvent.SOFTINPUT_SHOW:
               mVoiceWakeuperHelper.stopListening();

               //Log.d( "voicewake", "SOFTINPUT_SHOW: startListening start" );
           break;
           case JniEvent.SOFTINPUT_CAN_CLOSE:
               mVoiceWakeuperHelper.startListening();
             //  Log.d( "voicewake", "SOFTINPUT_CAN_CLOSE: startListening stop" );
           break;
        default:
            break;
    }
}
public static  void setCallServer(int  ip1,int ip2,int ip3,int ip4 ){
     String  perfix=ip1+"."+ip2+"."+ip3+"."+ip4;
     WebRTCClient.getInstance().asClient((MainActivity) LoveApplication.getInstance().getMainActivity(),perfix);
 }
public static  void startInitWake( ){
    Handler handler = new Handler(LoveApplication.getInstance().getMainLooper());
    handler.post(new Runnable() {
        @Override
        public void run() {
        ((MainActivity)LoveApplication.getInstance().getMainActivity()).initWake();
        }
    });
 }
public static  void closeDeviceDialog( ){
    Handler handler = new Handler(LoveApplication.getInstance().getMainLooper());
    handler.post(new Runnable() {
        @Override
        public void run() {
     LoadingDialog.closeDialog();
            }
        });
    }
public static  void showDeviceDialog( ){
    Handler handler = new Handler(LoveApplication.getInstance().getMainLooper());
    handler.post(new Runnable() {
        @Override
        public void run() {
             LoadingDialog.createDialog(((MainActivity)LoveApplication.getInstance().getMainActivity()),30);

            }
        });
    }
public static  void showCheckUpdate( ){
    Handler handler = new Handler(LoveApplication.getInstance().getMainLooper());
    handler.post(new Runnable() {
        @Override
        public void run() {

            Beta.checkUpgrade();

            }
        });
    }
   VoiceWakeuperHelper mVoiceWakeuperHelper=null;
private void initWake() {
     // Toast.makeText(MainActivity.this,"initWake", Toast.LENGTH_SHORT).show();
        mVoiceWakeuperHelper = new VoiceWakeuperHelper();
       mVoiceWakeuperHelper.initWake(LoveApplication.getInstance(), new VoiceWakeuperHelper.IReceivedEvent() {
           @Override
           public void onEvent(final int id) {
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                          // Log.d("TAG", "onResult: VoiceWakeuperHelper reset");
                          ResetMouse();
                     // Toast.makeText(MainActivity.this, id+"", Toast.LENGTH_SHORT).show();
                   }
               });
               //

           }
       });
   }
  public static int getIsSoftShowing(int i,int j) {
      boolean result= ((MainActivity)LoveApplication.getInstance().getMainActivity()).isSoftShowing();
      return result?0:1;
      }
public boolean isSoftShowing() {
    //获取当屏幕内容的高度
    int screenHeight = this.getWindow().getDecorView().getHeight();
    //获取View可见区域的bottom
    Rect rect = new Rect();
    //DecorView即为activity的顶级view
   this.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
    //考虑到虚拟导航栏的情况（虚拟导航栏情况下：screenHeight = rect.bottom + 虚拟导航栏高度）
    //选取screenHeight*2/3进行判断
    //  Log.e("test", "isSoftShowing: "+(screenHeight*2/3 > rect.bottom) );
    return screenHeight*2/3 > rect.bottom;
}
  Toast toast=null;
public void showTest(String  number){
    if(toast!=null){
      toast.cancel();
      toast=null;
        }
      toast=    Toast.makeText(this, number+"", Toast.LENGTH_SHORT);
      toast.show();
    }
public static  void callToast(String  number ){
    Handler handler = new Handler(LoveApplication.getInstance().getMainLooper());
    handler.post(new Runnable() {
        @Override
        public void run() {
            if(  number.contains("startchair")){
                        ((MainActivity)LoveApplication.getInstance().getMainActivity()).finishFace();
                    ((MainActivity)LoveApplication.getInstance().getMainActivity()).startFace();
                       return ;
                }
            if(  number.contains("finishchair")){

                ((MainActivity)LoveApplication.getInstance().getMainActivity()).finishFace();
                return ;
                }
      MainActivity activity=    (MainActivity)LoveApplication.getInstance().getMainActivity();
      if(activity!=null){
                activity.showTest(number);
          }
      }
        });
    }
@Override
  public void onSensorChanged(SensorEvent event) {
      float x=event.values[0];
      float y=event.values[1];
      float z=event.values[2];
      // angleSensorChanged(x,y,z);
//   /     tvAzimuth.setText("Azimuth 方位角: " + event.values[0] + "\n(0 - 359) 0=北, 90=东, 180=南, 270=西");
//        tvPitch.setText("Pitch 倾斜角: " + event.values[1] + "\n(-180 to 180)");
//        tvRoll.setText("Roll 旋转角: " + event.values[2] + "\n(-90 to 90)");
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int i) {

  }


/**
  * 执行命令并且输出结果
  */
 public static String execRootCmd(String cmd) {
     String result = "";
     DataOutputStream dos = null;
     DataInputStream dis = null;

     try {
         Process p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
         dos = new DataOutputStream(p.getOutputStream());
         dis = new DataInputStream(p.getInputStream());


         dos.writeBytes(cmd + "\n");
         dos.flush();
         dos.writeBytes("exit\n");
         dos.flush();
         String line = null;
         while ((line = dis.readLine()) != null) {
             Log.d("result", line);
             result += line;
         }
         p.waitFor();
     } catch (Exception e) {
         e.printStackTrace();
     } finally {
         if (dos != null) {
             try {
                 dos.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         if (dis != null) {
             try {
                 dis.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
     return result;
 }
public   void startFace(){
       ActivityFaceList.getInstance().FinishActivity();
       Intent intent = new Intent(this, ClassifierActivity.class);
       intent.putExtra("visible",false);
       startActivity(intent);
       //moveTaskToBack(false);

    }
public   void finishFace(){
    ActivityFaceList.getInstance().FinishActivity();

    }
 // public static native   void  angleSensorChanged(float x,float y,float z);
}
