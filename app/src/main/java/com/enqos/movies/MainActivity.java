package com.enqos.movies;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import com.enqos.movies.Utils.Utils;
import com.enqos.movies.models.MoviesListPojo;
import com.enqos.movies.networks.EnqosRestClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AlbumsAdapter adapter;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        if(Utils.checkforNetworkconnection(this)){
            pDialog=new ProgressDialog(this);
            pDialog = new ProgressDialog(this);
            pDialog.setCancelable(false);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.setMessage("Loading ...");
            pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

            pDialog.show();
            final Call<MoviesListPojo> moviesListPojoCall= EnqosRestClient.getClient().getMovies("f29e4356214210de149b4d32007cb04c","2016-01-01","2016-03-31");
            moviesListPojoCall.enqueue(new Callback<MoviesListPojo>() {
                @Override
                public void onResponse(Call<MoviesListPojo> call, Response<MoviesListPojo> response) {
                    if(pDialog!=null) {
                        pDialog.dismiss();
                    }
                    MoviesListPojo moviesListPojo=(MoviesListPojo)response.body();
                    adapter = new AlbumsAdapter(MainActivity.this, moviesListPojo);
                    RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(MainActivity.this, 1);
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.addItemDecoration(new GridSpacingItemDecoration(1, dpToPx(10), true));
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    recyclerView.setAdapter(adapter);
                }

                @Override
                public void onFailure(Call<MoviesListPojo> call, Throwable t) {
                    if(pDialog!=null) {
                        pDialog.dismiss();
                    }
                    Utils.getSnackbar(findViewById(R.id.main_content),getResources().getString(R.string.str_server));

                }
            });
        }else {
            Utils.getSnackbar(findViewById(R.id.main_content),getResources().getString(R.string.str_network));
        }

    }



    /**
     * RecyclerView item decoration - give equal margin around grid item
     */
    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
