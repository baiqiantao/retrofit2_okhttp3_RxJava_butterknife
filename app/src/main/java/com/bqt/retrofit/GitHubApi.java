package com.bqt.retrofit;

import com.bqt.retrofit.bean.Contributor;
import com.bqt.retrofit.bean.RetrofitBean;
import com.bqt.retrofit.bean.User;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rx.Observable;

public interface GitHubApi {
	
	@GET("repos/{owner}/{repo}/contributors")
	Call<ResponseBody> contributorsBySimpleGetCall(@Path("owner") String owner, @Path("repo") String repo);
	
	@GET("repos/{owner}/{repo}/contributors")
	Call<List<Contributor>> contributorsByAddConverterGetCall(@Path("owner") String owner, @Path("repo") String repo);
	
	@Headers({"Accept: application/vnd.github.v3.full+json", "User-Agent: RetrofitBean-Sample-App", "name:ljd"})
	@GET("repos/{owner}/{repo}/contributors")
	Call<List<Contributor>> contributorsAndAddHeader(@Path("owner") String owner, @Path("repo") String repo);
	
	@GET("search/repositories")
	Call<RetrofitBean> queryRetrofitByGetCall(@Query("q") String owner, @Query("since") String time, @Query("page") int page, @Query("per_page") int per_Page);
	
	@GET("search/repositories")
	Call<RetrofitBean> queryRetrofitByGetCallMap(@QueryMap Map<String, String> map);
	
	@GET("repos/{owner}/{repo}/contributors")
	Observable<List<Contributor>> contributorsByRxJava(@Path("owner") String owner, @Path("repo") String repo);
	
	@GET("users/{user}")
	Observable<User> userByRxJava(@Path("user") String user);
	
	@GET("/mobilesafe/shouji360/360safesis/360MobileSafe_6.2.3.1060.apk")
	Call<ResponseBody> retrofitDownload();
}