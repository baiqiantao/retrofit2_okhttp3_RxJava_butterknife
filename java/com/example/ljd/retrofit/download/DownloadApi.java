package com.example.ljd.retrofit.download;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;

public interface DownloadApi {
	@GET("/mobilesafe/shouji360/360safesis/360MobileSafe_6.2.3.1060.apk")
	Call<ResponseBody> retrofitDownload();
}