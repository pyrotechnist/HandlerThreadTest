package com.longyuan.handlerthreadtest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;


import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity   implements MyWorkerThread.Callback {

    private static boolean isVisible;
    public static final int LEFT_SIDE = 0;
    public static final int RIGHT_SIDE = 1;
    private LinearLayout mLeftSideLayout;
    private LinearLayout mRightSideLayout;
    private MyWorkerThread mWorkerThread;

    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        isVisible = true;
        mLeftSideLayout = (LinearLayout) findViewById(R.id.leftSideLayout);
        mRightSideLayout = (LinearLayout) findViewById(R.id.rightSideLayout);
        mImageView = (ImageView) findViewById(R.id.image);

        String[] urls = new String[]{"https://developer.android.com/design/media/principles_delight.png",
                "https://developer.android.com/design/media/principles_real_objects.png",
                "https://developer.android.com/design/media/principles_make_it_mine.png",
                "https://developer.android.com/design/media/principles_get_to_know_me.png"};


        String[] urls2 = new String[]{"principles_delight.png",
                "principles_real_objects.png",
                "principles_make_it_mine.png",
                "principles_get_to_know_me.png"};


        // solution 1  ： HandlerThread
      /*  mWorkerThread = new MyWorkerThread(new Handler(), this);
        mWorkerThread.start();
        mWorkerThread.prepareHandler();
        Random random = new Random();
        for (String url : urls){
            mWorkerThread.queueTask(url, random.nextInt(2), new ImageView(this));
        }*/

        //Solution 2 : rx + retrofit

        final DownLoadImageAPI downloadService = createService(DownLoadImageAPI.class, "https://developer.android.com/design/media/");

        Observable.from(urls2)
                .flatMap(new Func1<String, Observable<Response<ResponseBody>>>() {
            @Override
            public Observable<Response<ResponseBody>> call(String s) {
                return downloadService.downloadFileByUrlRx(s);
            }
        }).flatMap(processResponse())
                .subscribeOn(Schedulers.io())
                //.interval(1, TimeUnit.SECONDS)
                //.delay(2, TimeUnit.SECONDS, Schedulers.trampoline())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(handleResult());

    }

    @Override
    protected void onPause() {
        isVisible = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
       // mWorkerThread.quit();
        super.onDestroy();
    }



    @Override
    public void onImageDownloaded(ImageView imageView, Bitmap bitmap, int side) {
        imageView.setImageBitmap(bitmap);
        if (isVisible && side == LEFT_SIDE){
            mLeftSideLayout.addView(imageView);
        } else if (isVisible && side == RIGHT_SIDE){
            mRightSideLayout.addView(imageView);
        }


    }

    public <T> T createService(Class<T> serviceClass, String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(new OkHttpClient.Builder().build())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build();
        return retrofit.create(serviceClass);
    }

    private Func1<Response<ResponseBody>, Observable<Bitmap>> processResponse() {
        return new Func1<Response<ResponseBody>, Observable<Bitmap>>() {
            @Override
            public Observable<Bitmap> call(Response<ResponseBody> responseBodyResponse) {
                try {
                    Bitmap bitmap = BitmapFactory
                            .decodeStream((InputStream) responseBodyResponse.body().byteStream());
                    return Observable.just(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    private Observer<Bitmap> handleResult() {
        return new Observer<Bitmap>() {
            @Override
            public void onCompleted() {
                Log.d(TAG, "onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Log.d(TAG, "Error " + e.getMessage());
            }

            @Override
            public void onNext(Bitmap file) {
               // Log.d(TAG, "File downloaded to " + file.getAbsolutePath());
                mImageView.setImageBitmap(file);
            }
        };
    }
}
