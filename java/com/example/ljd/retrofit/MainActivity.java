package com.example.ljd.retrofit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ljd.retrofit.download.DownloadActivity;
import com.example.ljd.retrofit.bean.Contributor;
import com.example.ljd.retrofit.bean.Item;
import com.example.ljd.retrofit.bean.Owner;
import com.example.ljd.retrofit.bean.RetrofitBean;
import com.example.ljd.retrofit.bean.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static android.text.TextUtils.isEmpty;

public class MainActivity extends FragmentActivity {
	@BindView(R.id.tv)
	TextView tv;

	private static final String baseUrl = "https://api.github.com/";
	private static final String mUserName = "square";
	private static final String mRepo = "retrofit";
	private CompositeSubscription mSubscriptions = new CompositeSubscription();

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
	}

	@Override
	protected void onDestroy() {
		if (mSubscriptions != null) mSubscriptions.unsubscribe();
		super.onDestroy();
	}

	@OnClick({R.id.btn_retrofit_simple_contributors, R.id.btn_retrofit_converter_contributors, R.id.btn_retrofit_sync_contributors, R.id
			.btn_add_okhttp_log_contributors, R.id.btn_add_header_contributors, R.id.btn_retrofit_get_query, R.id
			.btn_retrofit_get_query_map, R.id.btn_rxJava_retrofit_contributors, R.id.btn_rxJava_retrofit_contributors_with_user_info, R.id
			.btn_download_retrofit,})
	public void onClickButton(View v) {
		switch (v.getId()) {
			//简单演示retrofit的使用
			case R.id.btn_retrofit_simple_contributors:
				requestGitHubContributorsSimple();
				break;
			//添加转换器
			case R.id.btn_retrofit_converter_contributors:
				requestGitHubContributorsByConverter();
				break;
			//添加okHttp的Log信息
			case R.id.btn_add_okhttp_log_contributors:
				requestGitHubContributorsAddOkHttpLog();
				break;
			//添加请求头
			case R.id.btn_add_header_contributors:
				requestGitHubContributorsAddHeader();
				break;
			//同步请求
			case R.id.btn_retrofit_sync_contributors:
				requestGitHubContributorsBySync();
				break;
			//通过get请求，使用@Query
			case R.id.btn_retrofit_get_query:
				requestQueryRetrofitByGet(null);
				break;
			//通过get请求，使用@QueryMap
			case R.id.btn_retrofit_get_query_map:
				Map<String, String> queryMap = new HashMap<>();
				queryMap.put("q", "retrofit");
				queryMap.put("since", "2016-03-29");
				queryMap.put("page", "1");
				queryMap.put("per_page", "3");
				requestQueryRetrofitByGet(queryMap);
				break;
			//rxJava+retrofit
			case R.id.btn_rxJava_retrofit_contributors:
				requestGitHubContributorsByRxJava();
				break;
			//rxJava+retrofit
			case R.id.btn_rxJava_retrofit_contributors_with_user_info:
				requestGitHubContributorsWithFullUserInfo();
				break;
			//文件下载
			case R.id.btn_download_retrofit:
				startActivity(new Intent(this, DownloadActivity.class));
				break;
		}
	}

	/**
	 * 简单示例
	 */
	private void requestGitHubContributorsSimple() {
		Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).build();
		GitHubApi repo = retrofit.create(GitHubApi.class);
		Call<ResponseBody> call = repo.contributorsBySimpleGetCall(mUserName, mRepo);
		call.enqueue(new Callback<ResponseBody>() {
			@Override
			public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
				try {
					String result = response.body().string();
					tv.setText("GitHub上对项目的贡献-1：\n");
					ArrayList<Contributor> list = new Gson().fromJson(result, new TypeToken<List<Contributor>>() {}.getType());
					for (Contributor contributor : list) {
						tv.append(contributor.getLogin() + "    " + contributor.getContributions() + "\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Call<ResponseBody> call, Throwable t) {
				Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 转换器
	 */
	private void requestGitHubContributorsByConverter() {
		Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl).addConverterFactory(GsonConverterFactory.create()).build();
		GitHubApi repo = retrofit.create(GitHubApi.class);
		Call<List<Contributor>> call = repo.contributorsByAddConverterGetCall(mUserName, mRepo);
		call.enqueue(new Callback<List<Contributor>>() {
			@Override
			public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
				List<Contributor> list = response.body();
				tv.setText("GitHub上对项目的贡献-2：\n");
				for (Contributor contributor : list) {
					tv.append(contributor.getLogin() + "    " + contributor.getContributions() + "\n");
				}
			}

			@Override
			public void onFailure(Call<List<Contributor>> call, Throwable t) {
				Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 添加日志信息
	 */
	private void requestGitHubContributorsAddOkHttpLog() {
		HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
		httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
		OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build();
		Retrofit retrofit = new Retrofit.Builder().addCallAdapterFactory(RxJavaCallAdapterFactory.create()).client(okHttpClient).baseUrl
				(baseUrl).addConverterFactory(GsonConverterFactory.create()).build();
		GitHubApi repo = retrofit.create(GitHubApi.class);
		Call<List<Contributor>> call = repo.contributorsByAddConverterGetCall(mUserName, mRepo);
		call.enqueue(new Callback<List<Contributor>>() {
			@Override
			public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
				List<Contributor> list = response.body();
				tv.setText("GitHub上对项目的贡献-3：\n");
				for (Contributor contributor : list) {
					tv.append(contributor.getLogin() + "    " + contributor.getContributions() + "\n");
				}
			}

			@Override
			public void onFailure(Call<List<Contributor>> call, Throwable t) {
				Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 添加请求头
	 */
	private void requestGitHubContributorsAddHeader() {
		GitHubApi mGitHubService = GitHubService.createRetrofitService(GitHubApi.class);
		Call<List<Contributor>> call = mGitHubService.contributorsAndAddHeader(mUserName, mRepo);
		call.enqueue(new Callback<List<Contributor>>() {
			@Override
			public void onResponse(Call<List<Contributor>> call, Response<List<Contributor>> response) {
				List<Contributor> list = response.body();
				tv.setText("GitHub上对项目的贡献-4：\n");
				for (Contributor contributor : list) {
					tv.append(contributor.getLogin() + "    " + contributor.getContributions() + "\n");
				}
			}

			@Override
			public void onFailure(Call<List<Contributor>> call, Throwable t) {
			}
		});
	}

	/**
	 * 同步请求
	 */
	private void requestGitHubContributorsBySync() {
		GitHubApi mGitHubService = GitHubService.createRetrofitService(GitHubApi.class);
		final Call<List<Contributor>> call = mGitHubService.contributorsByAddConverterGetCall(mUserName, mRepo);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Response<List<Contributor>> response = call.execute();
					final List<Contributor> list = response.body();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							tv.setText("GitHub上对项目的贡献-5：\n");
							for (Contributor contributor : list) {
								tv.append(contributor.getLogin() + "    " + contributor.getContributions() + "\n");
							}
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * get请求
	 */
	private void requestQueryRetrofitByGet(Map<String, String> queryMap) {
		GitHubApi mGitHubService = GitHubService.createRetrofitService(GitHubApi.class);
		Call<RetrofitBean> call;
		if (queryMap == null || queryMap.size() == 0) call = mGitHubService.queryRetrofitByGetCall("retrofit", "2016-03-29", 1, 3);
		else call = mGitHubService.queryRetrofitByGetCallMap(queryMap);

		call.enqueue(new Callback<RetrofitBean>() {
			@Override
			public void onResponse(Call<RetrofitBean> call, Response<RetrofitBean> response) {
				RetrofitBean retrofit = response.body();
				List<Item> list = retrofit.getItems();
				if (list != null) {
					int index = new Random().nextInt(100);
					tv.setText(index + "－total:" + retrofit.getTotalCount() + "\nincompleteResults:" + retrofit.getIncompleteResults());
					for (Item item : list) {
						tv.append("\n\n【name】" + item.getName());
						tv.append("\n【full_name】" + item.getFull_name());
						tv.append("\n【 description】" + item.getDescription());
						Owner owner = item.getOwner();
						tv.append("\n【login】" + owner.getLogin());
						tv.append("\n【type】" + owner.getType());
					}
				}
			}

			@Override
			public void onFailure(Call<RetrofitBean> call, Throwable t) {
			}
		});
	}

	/**
	 * retrofit+rxJava
	 */
	private void requestGitHubContributorsByRxJava() {
		GitHubApi mGitHubService = GitHubService.createRetrofitService(GitHubApi.class);
		mSubscriptions.add(mGitHubService.contributorsByRxJava(mUserName, mRepo)//
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
						tv.setText("GitHub上对项目的贡献-6：\n");
						for (Contributor contributor : list) {
							tv.append(contributor.getLogin() + "    " + contributor.getContributions() + "\n");
						}
					}
				}));
	}

	/**
	 * retrofit+rxJava
	 */
	private void requestGitHubContributorsWithFullUserInfo() {
		tv.setText("");
		final GitHubApi mGitHubService = GitHubService.createRetrofitService(GitHubApi.class);
		mSubscriptions.add(mGitHubService.contributorsByRxJava(mUserName, mRepo)//
				//变换：将事件序列中的对象或整个序列进行加工处理，转换成不同的事件或事件序列
				.flatMap(new Func1<List<Contributor>, Observable<Contributor>>() {
					@Override
					public Observable<Contributor> call(List<Contributor> contributors) {
						//1. 使用传入的事件对象创建一个 Observable 对象；
						//2. 并不发送这个 Observable，而是将它激活，于是它开始发送事件；
						//3. 创建的 Observable 发送的事件，都被汇入同一个 Observable，而这个 Observable 负责将这些事件统一交给Subscriber 的回调方法
						return Observable.from(contributors);
					}
				})//
				.flatMap(new Func1<Contributor, Observable<Pair<User, Contributor>>>() {
					@Override
					public Observable<Pair<User, Contributor>> call(Contributor contributor) {
						Observable<User> userObservable = mGitHubService.userByRxJava(contributor.getLogin())//
								.filter(new Func1<User, Boolean>() {
									@Override
									public Boolean call(User user) {
										return !isEmpty(user.getName()) && !isEmpty(user.getEmail());
									}
								});

						return Observable.zip(userObservable, Observable.just(contributor), new Func2<User, Contributor, Pair<User,
								Contributor>>() {
							@Override
							public Pair<User, Contributor> call(User user, Contributor contributor) {
								return new Pair<>(user, contributor);
							}
						});
					}
				})//
				.subscribeOn(Schedulers.newThread())//指定 subscribe() 发生在哪个线程，『后台线程取数据，主线程显示』
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
						tv.append("name：" + user.getName() + "\ncontributions：" + contributor.getContributions() + "\nEmail：" + user.getEmail()+"\n\n");
					}
				}));
	}
}