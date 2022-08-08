package com.example.presentationsample;

import android.app.AlertDialog;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class MyPresentation extends Presentation {
    private Context context;
    private Display display;

    public MyPresentation(Context outerContext, Display display) {
        super(outerContext, display);
        this.context = outerContext;
        this.display = display;
        if (getWindow() != null) {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            }else{
                getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_presentation);

        initView();
    }

    private void initView() {
        findViewById(R.id.show_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("提示")
                        .setMessage("副屏触发的对话框")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(), "主屏对话框被点击", Toast.LENGTH_SHORT).show();

                            }
                        }).create().show();
            }
        });
        findViewById(R.id.dismiss).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
