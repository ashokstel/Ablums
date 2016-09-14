package com.enqos.movies.networks;

//import retrofit2.http.POST;
//import retrofit2.http.Query;
//import rx.Observable;

import com.enqos.movies.models.MoviesListPojo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Ashok on 30/08/16.
 */
public interface EnqosApiServices {
//    https://api.themoviedb.org/3/discover/movie?
    // api_key=f29e4356214210de149b4d32007cb04c&
    // primary_release_date.gte=2016-01-01&primary_release_date.lte=2016-03-31


    @GET("discover/movie")
    Call<MoviesListPojo> getMovies(@Query("api_key") String apikey,
                                     @Query("primary_release_date.gte")String strPrimaryRledate,
                                     @Query("primary_release_date.lte")String strlte);


}
