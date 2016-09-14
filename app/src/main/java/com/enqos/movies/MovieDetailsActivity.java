package com.enqos.movies;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.enqos.movies.models.Result;
import com.enqos.movies.networks.EnqosRestClient;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

/**
 * Created by group10 on 30/8/16.
 */
public class MovieDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView tvTitle=(TextView)findViewById(R.id.title) ;
        TextView tvDes=(TextView)findViewById(R.id.count);
        String jsonMyObject=null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            jsonMyObject = extras.getString("myObject");
        }
        Result myResObj = new Gson().fromJson(jsonMyObject, Result.class);
        initCollapsingToolbar(myResObj.getTitle());
        if(myResObj!=null) {
            getSupportActionBar().setTitle(myResObj.getTitle());
            tvTitle.setText(myResObj.getOriginalTitle());
            tvDes.setText(myResObj.getOverview());
            try {
                Picasso.with(this).load(EnqosRestClient.imagesPath + myResObj.getBackdropPath()).fit().centerCrop()
                        .into((ImageView) findViewById(R.id.backdrop));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Initializing collapsing toolbar
     * Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar(final String strtitle) {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(strtitle);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(strtitle);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(strtitle);
                    isShow = false;
                }
            }
        });
    }
}
