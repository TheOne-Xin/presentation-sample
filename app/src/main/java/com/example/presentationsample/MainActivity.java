package com.example.presentationsample;

import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity_log";
    private MyPresentation myPresentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();

        findViewById(R.id.show_presentation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPresentation();
            }
        });

        findViewById(R.id.close_presentation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closePresentation();
            }
        });
        try {
            Log.d(TAG, "1");
            test1();
            Log.d(TAG, "2");
        } catch (Exception e) {
            Log.d(TAG, "test1 failed:" + e.toString());
        }
    }

    private void test1(){
        Log.d(TAG,"3");
        MyPresentation testPresentation = null;
        testPresentation.dismiss();
        Log.d(TAG,"4");
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //SYSTEM_ALERT_WINDOW权限申请
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 100);
            } else {
                Log.d(TAG, "Permission gained.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    //已获得权限
                    Toast.makeText(MainActivity.this, "Permission gained.", Toast.LENGTH_SHORT).show();
                } else {
                    //未获取到权限
                    Toast.makeText(MainActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    //显示副屏
    private void showPresentation() {
        //方式1
        MediaRouter mediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
        MediaRouter.RouteInfo route = mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO);
        if (route != null) {
            Display presentationDisplay = route.getPresentationDisplay();
            if (presentationDisplay != null) {
                myPresentation = new MyPresentation(MainActivity.this, presentationDisplay);
                myPresentation.show();
            }
        } else {
            Toast.makeText(MainActivity.this, "不支持分屏", Toast.LENGTH_SHORT).show();
        }

        //方式2
/*        DisplayManager mDisplayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = mDisplayManager.getDisplays();
        if (displays.length > 1) {
            //displays[0] 主屏，displays[1] 副屏
            myPresentation = new MyPresentation(MainActivity.this, displays[1]);
            myPresentation.show();
        } else {
            Toast.makeText(MainActivity.this, "不支持分屏", Toast.LENGTH_SHORT).show();
        }*/
    }

    //关闭副屏
    private void closePresentation() {
        if (myPresentation != null) {
            myPresentation.dismiss();
            myPresentation = null;
        }
    }
}
