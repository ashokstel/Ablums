package com.enqos.movies.networks;


import java.net.CookieManager;
import java.net.CookiePolicy;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Ashok on 30/08/16.
 */
public  class EnqosRestClient {


    public static String BASE_URL="https://api.themoviedb.org/3/";

    private static EnqosApiServices apiService=null;
    public  static String imagesPath="https://image.tmdb.org/t/p/w500";

     public static EnqosApiServices getClient(){
         if(apiService==null) {
             HttpLoggingInterceptor logging = new HttpLoggingInterceptor();

             logging.setLevel(HttpLoggingInterceptor.Level.BODY);
             OkHttpClient okClient = new OkHttpClient.Builder().addInterceptor(logging).build();
             CookieManager cookieManager = new CookieManager();
             cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
             okClient.cookieJar();
             Retrofit client = new Retrofit.Builder()
                     .baseUrl(BASE_URL)
                     .client(okClient)
                     .addCallAdapterFactory(RxJavaCallAdapterFactory.create())/*for rx java*/
                     .addConverterFactory(GsonConverterFactory.create())
                     .build();
             apiService = client.create(EnqosApiServices.class);
         }
             return  apiService;
     }

}
