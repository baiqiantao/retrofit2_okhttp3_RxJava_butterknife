package com.bqt.retrofit;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bqt.retrofit.bean.Contributor;
import com.bqt.retrofit.bean.Item;
import com.bqt.retrofit.bean.RetrofitBean;
import com.bqt.retrofit.bean.User;
import com.bqt.retrofit.progress.DownloadProgressHandler;
import com.bqt.retrofit.progress.ProgressHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.text.TextUtils.isEmpty;

public class MainActivity extends ListActivity {
	private TextView tv;
	private static final String baseUrl = "https://api.github.com/";
	private static final String mUserName = "square";//哪个公司【square】
	private static final String mRepo = "retrofit";//哪个项目【retrofit】
	private CompositeSubscription mSubscriptions = new CompositeSubscription();
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] array = {"1、简单完整演示retrofit的使用",
				"2、添加Gson转换器",
				"3、添加okHttp的日志拦截器Interceptor",
				"4、使用自己封装的API，演示@Headers",
				"5、演示同步请求",
				"6、演示@Query",
				"7、演示@QueryMap",
				"8、最简单、完整的retrofit+rxJava示例",
				"9、rxJava+retrofit增强",
				"10、演示文件下载",};
		tv = new TextView(this);// 将内容显示在TextView中
		tv.setTextColor(Color.BLUE);
		getListView().addFooterView(tv);
		setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>(Arrays.asList(array))));
	}
	
	@Override
	protected void onDestroy() {
		if (mSubscriptions != null) mSubscriptions.unsubscribe();
		super.onDestroy();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		switch (position + 1) {
			case 1://简单完整演示retrofit的使用
				requestGitHubContributorsSimple();
				break;
			case 2://添加Gson转换器
				requestGitHubContributorsByConverter();
				break;
			case 3://添加okHttp的日志拦截器Interceptor
				requestGitHubContributorsAddOkHttpLog();
				break;
			case 4://使用自己封装的API，演示@Headers
				requestGitHubContributorsAddHeader();
				break;
			case 5://演示同步请求
				requestGitHubContributorsBySync();
				break;
			case 6://演示@Query
				requestQueryRetrofitByGet(false);
				break;
			case 7://演示@QueryMap
				requestQueryRetrofitByGet(true);
				break;
			case 8://最简单、完整的retrofit+rxJava示例
				requestGitHubContributorsByRxJava();
				break;
			case 9://rxJava+retrofit增强
				requestGitHubContributorsWithFullUserInfo();
				break;
			case 10://演示文件下载
				retrofitDownload();
				break;
		}
	}
	
	/**
	 * 1、简单示例
	 */
	private void requestGitHubContributorsSimple() {
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(baseUrl)
				.build();
		GitHubApi repo = retrofit.create(GitHubApi.class);
		Call<ResponseBody> call = repo.contributorsBySimpleGetCall(mUserName, mRepo);
		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
				String result = null;
				try {
					result = response.body().string();
					if (result == null) return;
				} catch (IOException e) {
					e.printStackTrace();
				}
				tv.setText("GitHub上对项目的贡献-1：\n");
				ArrayList<Contributor> list = new Gson()
						.fromJson(result, new TypeToken<List<Contributor>>() {
						}.getType());
				if (list == null || list.size() == 0) return;
				for (Contributor contributor : list) {
					tv.append(contributor.login + "    " + contributor.contributions + "\n");
				}
			}
			
			@Override
			public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
				Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	/**
	 * 2、添加Gson转换器
	 */
	private void requestGitHubContributorsByConverter() {
		new Retrofit.Builder()
				.baseUrl(baseUrl)
				.addConverterFactory(GsonConverterFactory.create())//转换器
				.build()
				.create(GitHubApi.class)
				.contributorsByAddConverterGetCall(mUserName, mRepo)
				.enqueue(new Callback<List<Contributor>>() {
					@Override
					public void onResponse(@NonNull Call<List<Contributor>> call, @NonNull Response<List<Contributor>> response) {
						List<Contributor> list = response.body();
						tv.setText("GitHub上对项目的贡献-2：\n");
						if (list == null || list.size() == 0) return;
						for (Contributor contributor : list) {
							tv.append(contributor.login + "    " + contributor.contributions + "\n");
						}
					}
					
					@Override
					public void onFailure(@NonNull Call<List<Contributor>> call, @NonNull Throwable t) {
						Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
					}
				});
	}
	
	/**
	 * 3、添加okHttp的日志拦截器Interceptor
	 */
	private void requestGitHubContributorsAddOkHttpLog() {
		HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY);
		
		Retrofit retrofit = new Retrofit.Builder()
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.client(new OkHttpClient.Builder().addInterceptor(logInterceptor).build())
				.baseUrl(baseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		
		retrofit.create(GitHubApi.class)
				.contributorsByAddConverterGetCall(mUserName, mRepo)
				.enqueue(new Callback<List<Contributor>>() {
					@Override
					public void onResponse(@NonNull Call<List<Contributor>> call, @NonNull Response<List<com.bqt
							.retrofit.bean.Contributor>> response) {
						List<Contributor> list = response.body();
						tv.setText("GitHub上对项目的贡献-3：\n");
						if (list == null || list.size() == 0) return;
						for (Contributor contributor : list) {
							tv.append(contributor.login + "    " + contributor.contributions + "\n");
						}
					}
					
					@Override
					public void onFailure(@NonNull Call<List<Contributor>> call, @NonNull Throwable t) {
						Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
					}
				});
	}
	
	/**
	 * 4、使用自己封装的API，演示@Headers
	 */
	private void requestGitHubContributorsAddHeader() {
		createRetrofitService(GitHubApi.class)
				.contributorsAndAddHeader(mUserName, mRepo)
				.enqueue(new Callback<List<Contributor>>() {
					@Override
					public void onResponse(@NonNull Call<List<Contributor>> call, @NonNull Response<List<com.bqt
							.retrofit.bean.Contributor>> response) {
						List<Contributor> list = response.body();
						tv.setText("GitHub上对项目的贡献-4：\n");
						if (list == null || list.size() == 0) return;
						for (Contributor contributor : list) {
							tv.append(contributor.login + "    " + contributor.contributions + "\n");
						}
					}
					
					@Override
					public void onFailure(@NonNull Call<List<Contributor>> call, @NonNull Throwable t) {
					}
				});
	}
	
	/**
	 * 5、演示同步请求
	 */
	private void requestGitHubContributorsBySync() {
		final Call<List<Contributor>> call = createRetrofitService(GitHubApi.class)
				.contributorsByAddConverterGetCall(mUserName, mRepo);
		new Thread(() -> {
			try {
				Response<List<Contributor>> response = call.execute();//在子线程中请求网络
				final List<Contributor> list = response.body();
				runOnUiThread(() -> {
					tv.setText("GitHub上对项目的贡献-5：\n");
					for (Contributor contributor : list) {
						tv.append(contributor.login + "    " + contributor.contributions + "\n");
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}
	
	/**
	 * 6/7、演示@Query和@QueryMap
	 */
	private void requestQueryRetrofitByGet(boolean isQueryMap) {
		GitHubApi mGitHubService = createRetrofitService(GitHubApi.class);
		Call<RetrofitBean> call;
		if (!isQueryMap) call = mGitHubService.queryRetrofitByGetCall("retrofit", "2016-03-29", 1, 3);
		else {
			Map<String, String> queryMap = new HashMap<>();
			queryMap.put("q", "retrofit");
			queryMap.put("since", "2016-03-29");
			queryMap.put("page", "1");
			queryMap.put("per_page", "3");
			call = mGitHubService.queryRetrofitByGetCallMap(queryMap);
		}
		
		call.enqueue(new Callback<RetrofitBean>() {
			@Override
			public void onResponse(@NonNull Call<RetrofitBean> call, @NonNull Response<RetrofitBean> response) {
				RetrofitBean retrofitBean = response.body();
				if (retrofitBean == null) return;
				List<Item> list = retrofitBean.getItems();
				if (list == null || list.size() == 0) return;
				
				tv.setText(new SimpleDateFormat("yyyy.MM.dd HH:mm:ss SSS", Locale.getDefault()).format(new Date()));
				tv.append("\ntotal:" + retrofitBean.getTotalCount() + "\nincompleteResults:" + retrofitBean.getIncompleteResults());
				for (Item item : list) {
					tv.append("\n\n【name】" + item.name);
					tv.append("\n【full_name】" + item.full_name);
					tv.append("\n【 description】" + item.description);
				}
			}
			
			@Override
			public void onFailure(@NonNull Call<RetrofitBean> call, @NonNull Throwable t) {
			}
		});
	}
	
	/**
	 * 8、最简单、完整的retrofit+rxJava示例
	 */
	private void requestGitHubContributorsByRxJava() {
		createRetrofitService(GitHubApi.class)
				.contributorsByRxJava(mUserName, mRepo)//
				.subscribeOn(Schedulers.io())//
				.observeOn(AndroidSchedulers.mainThread())//
				.subscribe(new Observer<List<Contributor>>() {
					@Override
					public void onCompleted() {
						Toast.makeText(MainActivity.this, "完成", Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onError(Throwable e) {
						Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
					}
					
					@Override
					public void onNext(List<Contributor> list) {
						tv.setText("GitHub上对项目的贡献-8：\n");
						for (Contributor contributor : list) {
							tv.append(contributor.login + "    " + contributor.contributions + "\n");
						}
					}
				});
	}
	
	/**
	 * 9、rxJava+retrofit增强
	 */
	private void requestGitHubContributorsWithFullUserInfo() {
		tv.setText("");
		final GitHubApi mGitHubService = createRetrofitService(GitHubApi.class);
		
		Subscription subscription =
				mGitHubService.contributorsByRxJava(mUserName, mRepo)//
						.flatMap(new Func1<List<Contributor>, Observable<Contributor>>() {
							//变换：将事件序列中的对象或整个序列进行加工处理，转换成不同的事件或事件序列
							@Override
							public Observable<Contributor> call(List<Contributor>
									                                    contributors) {
								//1、使用传入的事件对象创建一个 Observable 对象；
								//2、并不发送这个 Observable，而是将它激活，于是它开始发送事件；
								//3、创建的 Observable 发送的事件，都被汇入同一个 Observable，而这个 Observable 负责将这些事件统一交给Subscriber 的回调方法
								return Observable.from(contributors);
							}
						})
						.flatMap(new Func1<Contributor, Observable<Pair<User, Contributor>>>() {
							@Override
							public Observable<Pair<User, Contributor>> call(com.bqt
									                                                .retrofit.bean.Contributor contributor) {
								Observable<User> userObservable = mGitHubService.userByRxJava(contributor.login)
										.filter(user -> !isEmpty(user.name) && !isEmpty(user.email));
								return Observable.zip(userObservable, Observable.just(contributor), Pair::new);
							}
						})
						.subscribeOn(Schedulers.newThread())//指定 subscribe() 发生在哪个线程，后台线程取数据，主线程显示
						.observeOn(AndroidSchedulers.mainThread())//指定 Subscriber 的回调发生在主线程
						.subscribe(new Observer<Pair<User, Contributor>>() {
							@Override
							public void onCompleted() {
								Toast.makeText(MainActivity.this, "完成", Toast.LENGTH_SHORT).show();
							}
							
							@Override
							public void onError(Throwable e) {
								Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
							}
							
							@Override
							public void onNext(Pair<User, Contributor> pair) {
								User user = pair.first;
								Contributor contributor = pair.second;
								tv.append("name：" + user.name + "\ncontributions：" + contributor.contributions + "\nEmail：" + user.email + "\n\n");
							}
						});
		mSubscriptions.add(subscription);
	}
	
	/**
	 * 10、演示文件下载
	 */
	public void retrofitDownload() {
		//监听下载进度
		final ProgressDialog dialog = new ProgressDialog(this);
		dialog.setProgressNumberFormat("%1d KB/%2d KB");
		dialog.setTitle("下载");
		dialog.setMessage("正在下载，请稍后...");
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setCancelable(false);
		dialog.show();
		
		ProgressHelper.setProgressHandler(new DownloadProgressHandler() {
			@Override
			protected void onProgress(long bytesRead, long contentLength, boolean done) {
				//在主线程中运行
				dialog.setMax((int) (contentLength / 1024));
				dialog.setProgress((int) (bytesRead / 1024));
				if (done) dialog.dismiss();
			}
		});
		
		Retrofit retrofit = new Retrofit.Builder()//
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())//
				.addConverterFactory(GsonConverterFactory.create())//
				.baseUrl("http://msoftdl.360.cn")
				.client(ProgressHelper.addProgress(null).build())
				.build();
		
		retrofit.create(GitHubApi.class).retrofitDownload()
				.enqueue(new Callback<ResponseBody>() {
					@Override
					public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
						try {
							InputStream is = response.body().byteStream();
							File file = new File(Environment.getExternalStorageDirectory(), "12345.apk");
							FileOutputStream fos = new FileOutputStream(file);
							BufferedInputStream bis = new BufferedInputStream(is);
							byte[] buffer = new byte[1024];
							int len;
							while ((len = bis.read(buffer)) != -1) {
								fos.write(buffer, 0, len);
								fos.flush();
							}
							fos.close();
							bis.close();
							is.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					
					@Override
					public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
					}
				});
	}
	
	public static <T> T createRetrofitService(final Class<T> service) {
		HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
		httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
		OkHttpClient.Builder builder = new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor);
		Retrofit retrofit = new Retrofit.Builder()//
				.client(ProgressHelper.addProgress(builder).build())//
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())//
				.addConverterFactory(GsonConverterFactory.create())//
				.baseUrl("https://api.github.com/")//
				.build();
		return retrofit.create(service);
	}
}