package com.enqos.movies;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.enqos.movies.models.MoviesListPojo;
import com.enqos.movies.models.Result;
import com.enqos.movies.networks.EnqosRestClient;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

/**
 * Created by Ashok on 30/08/16.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.MyViewHolder> {

    private Context mContext;
    private MoviesListPojo albumList;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, count,tvRating,tvDes;
        public ImageView thumbnail;
        public CardView cv;
        RatingBar rtBar;
        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            count = (TextView) view.findViewById(R.id.tv_release);
            tvDes=(TextView)view.findViewById(R.id.tv_des);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            cv=(CardView)view.findViewById(R.id.card_view);
            rtBar=(RatingBar)view.findViewById(R.id.ratingBar);
            tvRating=(TextView)view.findViewById(R.id.tv_rating);
        }
    }


    public AlbumsAdapter(Context mContext, MoviesListPojo albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
       final Result album = albumList.getResults().get(position);
        holder.title.setText(album.getOriginalTitle());
        holder.count.setText("Date :"+album.getReleaseDate());
        holder.tvDes.setText(album.getOverview());
        holder.tvRating.setText(""+album.getVoteAverage());
        // loading album cover using Glide library
        Picasso.with(mContext).load(EnqosRestClient.imagesPath+album.getPosterPath()).fit().centerCrop()
                .into(holder.thumbnail);
        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentList=new Intent(mContext,MovieDetailsActivity.class);
                intentList.putExtra("myObject", new Gson().toJson(album));
                mContext.startActivity(intentList);
            }
        });
    }


    @Override
    public int getItemCount() {
        return albumList.getResults().size();
    }
}
