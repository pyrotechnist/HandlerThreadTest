package com.longyuan.handlerthreadtest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Random;

public class MainActivity extends Activity   implements MyWorkerThread.Callback {

    private static boolean isVisible;
    public static final int LEFT_SIDE = 0;
    public static final int RIGHT_SIDE = 1;
    private LinearLayout mLeftSideLayout;
    private LinearLayout mRightSideLayout;
    private MyWorkerThread mWorkerThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        isVisible = true;
        mLeftSideLayout = (LinearLayout) findViewById(R.id.leftSideLayout);
        mRightSideLayout = (LinearLayout) findViewById(R.id.rightSideLayout);
        String[] urls = new String[]{"http://developer.android.com/design/media/principles_delight.png",
                "http://developer.android.com/design/media/principles_real_objects.png",
                "http://developer.android.com/design/media/principles_make_it_mine.png",
                "http://developer.android.com/design/media/principles_get_to_know_me.png"};
        mWorkerThread = new MyWorkerThread(new Handler(), this);
        mWorkerThread.start();
        mWorkerThread.prepareHandler();
        Random random = new Random();
        for (String url : urls){
            mWorkerThread.queueTask(url, random.nextInt(2), new ImageView(this));
        }


    }

    @Override
    protected void onPause() {
        isVisible = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWorkerThread.quit();
        super.onDestroy();
    }



    @Override
    public void onImageDownloaded(ImageView imageView, Bitmap bitmap, int side) {


    }
}
