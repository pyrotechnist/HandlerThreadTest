package com.longyuan.handlerthreadtest;

import com.squareup.okhttp.ResponseBody;


import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by LONGYUAN on 2017/9/25.
 */

public interface DownLoadImageAPI {

    // Retrofit 2 GET request for rxjava
    @Streaming
    @GET
    Observable<Response<ResponseBody>> downloadFileByUrlRx(@Url String fileUrl);
}
