package com.group10.tml.activities;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.group10.tml.R;
import com.group10.tml.adapters.AllVehiclesAdapter;
import com.group10.tml.adapters.CustomSpinnerAdapter;
import com.group10.tml.dbhelepers.MySectionListDb;
import com.group10.tml.models.LiveTrackModel;
import com.group10.tml.models.Result;
import com.group10.tml.notifications.NotificationActivity;
import com.group10.tml.notifications.NotificationAdapter;
import com.group10.tml.services.LiveTrackingService;
import com.group10.tml.sharedprefrences.SharedPreferencesClass;
import com.group10.tml.utils.Utils;
import com.tml.flexiableadapterlibrary.FlexibleAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        FlexibleAdapter.OnUpdateListener, AllVehiclesAdapter.OnItemClickListener, LiveTrackingService.liveTrackcallback {

    //Defining Variables
    public static String TAG = MainActivity.class.getSimpleName();
    public static String strVehicleId = "1", strVehName = "";
    private NavigationView navigationView;
    private AllVehiclesAdapter mAdapter;
    private DrawerLayout drawerLayout;
    private Intent service;
//    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ScheduledExecutorService scheduledExecutorServiceActivity;
    private RecyclerView recyclerView;
    private Menu muDetails;
    private View snackbarLayout;
    private boolean boolSearch, isDrawerOpned, isSpinner;
    private LiveTrackModel liveTrackModel;
    private Date date = null;
    private SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    private String strformatedTime = null;
    private MySectionListDb db = null;

    // filters for vehiclestatus enum
    enum Filter {
        All,
        Moving,
        Halted, Notworking, Disconnected
    }

    private Filter currentFilter = Filter.All;
    ArrayList<String> al = new ArrayList<>();
    ArrayList<Integer> alCount = new ArrayList<>();
    private Spinner spinner;
    private Toolbar toolbar;
    static Activity activityInstance = null;
    public  int intAllVehCount=0,intMovingVehCount=0,intHaltedVehCount=0,intNotworingVehcount=0,intDisconnectVehcount=0;
    CustomSpinnerAdapter customSpinnerAdapter=null;
    public  static String SAVED_LAYOUT_MANAGER="key";
    private Parcelable layoutManagerSavedState;


    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            layoutManagerSavedState = ((Bundle) state).getParcelable(SAVED_LAYOUT_MANAGER);
        }
        super.onRestoreInstanceState((Bundle) state);
    }




    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SAVED_LAYOUT_MANAGER, recyclerView.getLayoutManager().onSaveInstanceState());
        return bundle;
    }
    public void setItems() {
        mAdapter.createExampleItems();
        restoreLayoutManagerPosition();
    }
    private void restoreLayoutManagerPosition() {
        if (layoutManagerSavedState != null) {
            recyclerView.getLayoutManager().onRestoreInstanceState(layoutManagerSavedState);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityInstance = this;
        al.clear();

        for (int i = 0; i < Filter.values().length; i++) {
            al.add(Filter.values()[i].toString());
            alCount.add(i);
        }
        customSpinnerAdapter=new CustomSpinnerAdapter(MainActivity.this, al,alCount);
//        Getting all the view from activity_main.xml
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
//       serviceIntent initialization
        service = new Intent(getApplicationContext(), LiveTrackingService.class);
//        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        snackbarLayout = (findViewById(R.id.homelayout));
        spinner = (Spinner) findViewById(R.id.spinner_toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);

//        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);
//        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
////                     refresh the data
//                        if (Utils.checkforNetworkconnection(MainActivity.this)) {
//                            mSwipeRefreshLayout.setRefreshing(true);
//                            mAdapter = new AllVehiclesAdapter(MainActivity.this, "example parameter for List1", liveTrackModel, "online");
//                            recyclerView.setAdapter(mAdapter);
//                            mAdapter.notifyDataSetChanged();
//                        } else {
//                            setRecylerViewData();
//                        }
//                        mSwipeRefreshLayout.setRefreshing(false);
//                    }
//                }, 3000);
//            }
//        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, final View view, final int position, long id) {

                alCount.clear();intAllVehCount=0;intMovingVehCount=0;intHaltedVehCount=0;intDisconnectVehcount=0;
                intNotworingVehcount=0;

                currentFilter = Filter.values()[position];
                isSpinner = true;
//              getVehiclesFilterLiveTrackModel() this method returns the LiveTrackModel data
                LiveTrackModel l = getVehiclesFilterLiveTrackModel(liveTrackModel, currentFilter.toString());
//                toolbar.setTitle("" + l.getResults().size());
                for (int i = 0; i < Filter.values().length; i++) {
                    if(i==0){
                        alCount.add(intAllVehCount);
                    }else if(i==1){
                        alCount.add(intMovingVehCount);
                    }else if(i==2){
                        alCount.add(intHaltedVehCount);
                    }else if(i==3){
                        alCount.add(intNotworingVehcount);
                    }else if(i==4){
                        alCount.add(intDisconnectVehcount);
                    }
                }
                customSpinnerAdapter.notifyDataSetChanged();

                try {
                    final View v = spinner.getRootView();
                    LinearLayout ll = (LinearLayout) v.findViewById(R.id.ll_spinner_row);
                    TextView tvTitle = (TextView) v.findViewById(R.id.tv_spinneritem);
                    ll.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    tvTitle.setTextColor(Color.parseColor("#ffffff"));
                }catch (Exception e){e.printStackTrace();}
                Parcelable state= recyclerView.getLayoutManager().onSaveInstanceState();
                recyclerView.getLayoutManager().onRestoreInstanceState(state);

                mAdapter = new AllVehiclesAdapter(MainActivity.this, "example parameter for List1", l, currentFilter.toString());
                recyclerView.setAdapter(mAdapter);
                setItems();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //Setting Navigation View  Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();
                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    case R.id.menu_mapview:
                        if (Utils.checkforNetworkconnection(MainActivity.this)) {
                            if(scheduledExecutorServiceActivity!=null){
                                scheduledExecutorServiceActivity.shutdownNow();
                            }
                            stopService(service);
                            startActivity(new Intent(MainActivity.this, AllVehiclesMapViewActivity.class));
                        } else {
                            Utils.getSnackbar(findViewById(R.id.homelayout), "There is no internet");
                        }
//                        Toast.makeText(getApplicationContext(), "map Selected", Toast.LENGTH_SHORT).show();
                        return true;
                    // For rest of the options we just show a toast on click
                    case R.id.menu_charts:
                        startActivity(new Intent(MainActivity.this,ChartActivity.class));
                        Toast.makeText(getApplicationContext(), "charts Selected", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.menu_reports:
                        startActivity(new Intent(MainActivity.this,ReportsActivity.class));
//                        Toast.makeText(getApplicationContext(), "reports Selected", Toast.LENGTH_SHORT).show();
                        return true;
//                    case R.id.menu_abtg10:
//                        Toast.makeText(getApplicationContext(), "Group10 Selected", Toast.LENGTH_SHORT).show();
//                        return true;
//                    case R.id.menu_feedback:
//                        Toast.makeText(getApplicationContext(), "Feedback Selected", Toast.LENGTH_SHORT).show();
//                        return true;
//                    case R.id.menu_logout:
//                        Toast.makeText(getApplicationContext(), "Logout Selected", Toast.LENGTH_SHORT).show();
//                        return true;
//                    case R.id.menu_share:
//                        Toast.makeText(getApplicationContext(), "share Selected", Toast.LENGTH_SHORT).show();
//                        return true;
//                    case R.id.menu_abtapp:
//                        Toast.makeText(getApplicationContext(), "tml Selected", Toast.LENGTH_SHORT).show();
//                        return true;
                    default:
                        Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                        return true;

                }
            }
        });
        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                isDrawerOpned = false;
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                isDrawerOpned = true;
                super.onDrawerOpened(drawerView);
            }
        };
        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        menu inflate here home
        getMenuInflater().inflate(R.menu.home, menu);
        muDetails = menu;
//        This method used for search of the vehicles
        initSearchView(menu);
        return true;
    }

    private void initSearchView(final Menu menu) {
        //Associate searchable configuration with the SearchView
        Log.d(TAG, "onCreateOptionsMenu setup SearchView!");
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) MenuItemCompat
                .getActionView(menu.findItem(R.id.action_search));
        searchView.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        searchView.setQueryHint(getString(R.string.action_search));
        searchView.setMaxWidth(searchView.getMaxWidth());
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.findItem(R.id.action_msg).setVisible(false);
                spinner.setVisibility(View.GONE);
                boolSearch = true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                menu.findItem(R.id.action_msg).setVisible(true);
                spinner.setVisibility(View.VISIBLE);
                return false;
            }
        });
    }

    @Override
    public void onLoadComplete() {
//		mProgressBar.setVisibility(View.INVISIBLE);
//		updateEmptyView();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        //Has searchText?
        if (!NotificationAdapter.hasSearchText()) {
            searchView.setIconified(true);// This also clears the text in SearchView widget
        } else {
            searchView.setQuery(NotificationAdapter.getSearchText(), false);
            searchView.setIconified(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!NotificationAdapter.hasSearchText()
                || !NotificationAdapter.getSearchText().equalsIgnoreCase(newText)) {
            NotificationAdapter.setSearchText(newText);
            mAdapter.updateDataSetAsync(newText);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return onQueryTextChange(query);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_msg) {
            if (Utils.checkforNetworkconnection(MainActivity.this)) {
                startActivity(new Intent(this, NotificationActivity.class));
            } else {
                Utils.getSnackbar(findViewById(R.id.homelayout), "There is no internet");
            }
            return true;
        } else if (id == R.id.action_map) {
            if (Utils.checkforNetworkconnection(MainActivity.this)) {
                if(scheduledExecutorServiceActivity!=null){
                    scheduledExecutorServiceActivity.shutdownNow();
                }
                stopService(service);
                startActivity(new Intent(this, AllVehiclesMapViewActivity.class));
            } else {
                Utils.getSnackbar(findViewById(R.id.homelayout), "There is no internet");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //If ActionMode is active, back key closes it
        if (boolSearch) {
            muDetails.clear();
            invalidateOptionsMenu();
            boolSearch = false;
            NotificationAdapter.setSearchText("");
            mAdapter.updateDataSetAsync("");
            return;
        } else if (isDrawerOpned) {
            drawerLayout.closeDrawers();
            return;
        }
        //Close the App
        super.onBackPressed();
    }

    private class FilterAdapter extends ArrayAdapter<Filter> {
        public FilterAdapter(Context context) {

            super(context, android.R.layout.simple_spinner_item, Filter.values());
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }
    }
    //    this method is used for getting static instance
    public static Activity getInstance() {
        return activityInstance;
    }
    @Override
    protected void onStart() {
        super.onStart();
//        start service here
        startService(service);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        initial stops the scheduledExecutorservice and then to stop service
        if (scheduledExecutorServiceActivity != null) {
            scheduledExecutorServiceActivity.shutdownNow();
        }
        stopService(service);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //        initial stops the scheduledExecutorservice and then to stop service
        if (scheduledExecutorServiceActivity != null) {
            scheduledExecutorServiceActivity.shutdownNow();
        }
        stopService(service);
    }

    //    Get the data from offline
    private void setRecylerViewData() {

        List<Result> liveTrackModelObject = new ArrayList<Result>();
        liveTrackModelObject.clear();
        LiveTrackModel liveTrackStatus = new LiveTrackModel();
        if (db == null) {
            db = new MySectionListDb((MainActivity.this));
        }
        db.open();
        Cursor curVehicleList = db.getAllVehicleList();
        if (curVehicleList.getCount() > 0) {
            while (curVehicleList.moveToNext()) {
                Result result = new Result();
                result.setVehId(curVehicleList.getString(0));
                result.setVehName(curVehicleList.getString(1));
                liveTrackModelObject.add(result);
            }
            liveTrackStatus.setResults(liveTrackModelObject);
        }
        db.close();
        if (recyclerView != null) {
            mAdapter = new AllVehiclesAdapter(MainActivity.this, "example parameter for List1", liveTrackStatus, "offline");
            recyclerView.setAdapter(mAdapter);
        }
    }

    //    This method for filters the vehicles(Moving,Halted,Notworking,Disconnected)
    private LiveTrackModel getVehiclesFilterLiveTrackModel(final LiveTrackModel live, String strVehicleFilters) {
        List<Result> liveTrackModelObject = new ArrayList<Result>();
        liveTrackModelObject.clear();
        LiveTrackModel liveTrackStatus = new LiveTrackModel();


        if (strVehicleFilters.contains("All")) {
            for (int i = 0; i < live.getResults().size(); i++) {

                if (live.getResults().get(i).getVehParamValues().size() > 1) {
                    intAllVehCount=intAllVehCount+1;
                    try {
                        date = (Date) formatter.parse(live.getResults().get(i).getVehParamValues().get(1).getDeviceDateTime());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    strformatedTime = com.group10.tml.utils.Utils.getTimeInStringFormat(formatter.getCalendar());
                    long MAX_DURATION = MILLISECONDS.convert(3, MINUTES);
                    long duration = new Date().getTime() - date.getTime();

                    if (Integer.valueOf(live.getResults().get(i).getVehParamValues().get(2).getValue()) == 1) {
                        intDisconnectVehcount=intDisconnectVehcount+1;
                    }else if (strformatedTime.contains("hour") || strformatedTime.contains("day") || strformatedTime.contains("year")) {
                        intNotworingVehcount=intNotworingVehcount+1;

                    } else if ((Integer.valueOf(live.getResults().get(i).getVehParamValues().get(1).getValue()) > 3 && duration < MAX_DURATION)) {
                      intMovingVehCount=intMovingVehCount+1;
                    } else {
                       intHaltedVehCount=intHaltedVehCount+1;
                    }
                }
            }
            return live;
        } else {
            if (strVehicleFilters.contains("Disconnected")) {
                for (int i = 0; i < live.getResults().size(); i++) {

                    if (live.getResults().get(i).getVehParamValues().size() > 1) {
                        intAllVehCount=intAllVehCount+1;
                        try {
                            date = (Date) formatter.parse(live.getResults().get(i).getVehParamValues().get(1).getDeviceDateTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        strformatedTime = com.group10.tml.utils.Utils.getTimeInStringFormat(formatter.getCalendar());
                        long MAX_DURATION = MILLISECONDS.convert(3, MINUTES);
                        long duration = new Date().getTime() - date.getTime();
                        if (Integer.valueOf(live.getResults().get(i).getVehParamValues().get(2).getValue()) == 1) {
                            Result result = new Result();
                            result.setVehId(live.getResults().get(i).getVehId());
                            result.setVehName(live.getResults().get(i).getVehName());
                            result.setVehUpdatedTime(live.getResults().get(i).getVehUpdatedTime());
                            result.setVehParamValues(live.getResults().get(i).getVehParamValues());
                            liveTrackModelObject.add(result);
                            intDisconnectVehcount=intDisconnectVehcount+1;
                        } else if (strformatedTime.contains("hour") || strformatedTime.contains("day") || strformatedTime.contains("year")) {
                            intNotworingVehcount=intNotworingVehcount+1;
                        } else if ((Integer.valueOf(live.getResults().get(i).getVehParamValues().get(1).getValue()) > 3 && duration < MAX_DURATION)) {
                            intMovingVehCount=intMovingVehCount+1;
                        } else {
                            intHaltedVehCount=intHaltedVehCount+1;
                        }
                    }
                }
                liveTrackStatus.setResults(liveTrackModelObject);
            } else if (strVehicleFilters.contains("Notworking")) {
                for (int i = 0; i < live.getResults().size(); i++) {

                    if (live.getResults().get(i).getVehParamValues().size() > 1) {
                        intAllVehCount=intAllVehCount+1;
                        try {
                            date = (Date) formatter.parse(live.getResults().get(i).getVehParamValues().get(1).getDeviceDateTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        strformatedTime = com.group10.tml.utils.Utils.getTimeInStringFormat(formatter.getCalendar());
                        long MAX_DURATION = MILLISECONDS.convert(3, MINUTES);
                        long duration = new Date().getTime() - date.getTime();
                        if (Integer.valueOf(live.getResults().get(i).getVehParamValues().get(2).getValue()) == 1) {
                            intDisconnectVehcount=intDisconnectVehcount+1;
                        } else if (strformatedTime.contains("hour") || strformatedTime.contains("day") || strformatedTime.contains("year")) {
                            Result result = new Result();
                            result.setVehId(live.getResults().get(i).getVehId());
                            result.setVehName(live.getResults().get(i).getVehName());
                            result.setVehUpdatedTime(live.getResults().get(i).getVehUpdatedTime());
                            result.setVehParamValues(live.getResults().get(i).getVehParamValues());
                            liveTrackModelObject.add(result);
                            intNotworingVehcount=intNotworingVehcount+1;
                        } else if ((Integer.valueOf(live.getResults().get(i).getVehParamValues().get(1).getValue()) > 3 && duration < MAX_DURATION)) {
                            intMovingVehCount=intMovingVehCount+1;
                        } else {
                            intHaltedVehCount=intHaltedVehCount+1;
                        }
                    }
                }
                liveTrackStatus.setResults(liveTrackModelObject);
            } else if (strVehicleFilters.contains("Moving")) {
                for (int i = 0; i < live.getResults().size(); i++) {

                    if (live.getResults().get(i).getVehParamValues().size() > 1) {
                        intAllVehCount=intAllVehCount+1;
                        try {
                            date = (Date) formatter.parse(live.getResults().get(i).getVehParamValues().get(1).getDeviceDateTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        strformatedTime = com.group10.tml.utils.Utils.getTimeInStringFormat(formatter.getCalendar());
                        long MAX_DURATION = MILLISECONDS.convert(3, MINUTES);
                        long duration = new Date().getTime() - date.getTime();
                        if (Integer.valueOf(live.getResults().get(i).getVehParamValues().get(2).getValue()) == 1) {
                            intDisconnectVehcount=intDisconnectVehcount+1;
                        } else if (strformatedTime.contains("hour") || strformatedTime.contains("day") || strformatedTime.contains("year")) {
                            intNotworingVehcount=intNotworingVehcount+1;

                        } else if ((Integer.valueOf(live.getResults().get(i).getVehParamValues().get(1).getValue()) > 3 && duration < MAX_DURATION)) {
                            Result result = new Result();
                            result.setVehId(live.getResults().get(i).getVehId());
                            result.setVehName(live.getResults().get(i).getVehName());
                            result.setVehUpdatedTime(live.getResults().get(i).getVehUpdatedTime());
                            result.setVehParamValues(live.getResults().get(i).getVehParamValues());
                            liveTrackModelObject.add(result);
                            intMovingVehCount=intMovingVehCount+1;
                        } else {
                            intHaltedVehCount=intHaltedVehCount+1;
                        }
                    }
                }
                liveTrackStatus.setResults(liveTrackModelObject);
            } else if (strVehicleFilters.contains("Halted")) {
                for (int i = 0; i < live.getResults().size(); i++) {

                    if (live.getResults().get(i).getVehParamValues().size() > 1) {
                        intAllVehCount=intAllVehCount+1;
                        try {
                            date = (Date) formatter.parse(live.getResults().get(i).getVehParamValues().get(1).getDeviceDateTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        strformatedTime = com.group10.tml.utils.Utils.getTimeInStringFormat(formatter.getCalendar());
                        long MAX_DURATION = MILLISECONDS.convert(3, MINUTES);
                        long duration = new Date().getTime() - date.getTime();
                        if (Integer.valueOf(live.getResults().get(i).getVehParamValues().get(2).getValue()) == 1) {
                            intDisconnectVehcount=intDisconnectVehcount+1;
                        } else if (strformatedTime.contains("hour") || strformatedTime.contains("day") || strformatedTime.contains("year")) {
                            intNotworingVehcount=intNotworingVehcount+1;
                        } else if ((Integer.valueOf(live.getResults().get(i).getVehParamValues().get(1).getValue()) > 3 && duration < MAX_DURATION)) {
                            intMovingVehCount=intMovingVehCount+1;
                        } else {
                            Result result = new Result();
                            result.setVehId(live.getResults().get(i).getVehId());
                            result.setVehName(live.getResults().get(i).getVehName());
                            result.setVehUpdatedTime(live.getResults().get(i).getVehUpdatedTime());
                            result.setVehParamValues(live.getResults().get(i).getVehParamValues());
                            liveTrackModelObject.add(result);
                            intHaltedVehCount=intHaltedVehCount+1;
                        }
                    }
                }
                liveTrackStatus.setResults(liveTrackModelObject);
            }
            return liveTrackStatus;
        }
    }

    @Override
    public boolean onListItemClick(int position) {
        strVehicleId = mAdapter.getItem(position).getVehId();
        strVehName = mAdapter.getItem(position).getVehName();
        Log.e(TAG, "strvehicleid" + mAdapter.getItem(position).getVehId() + "name==" + mAdapter.getItem(position).getVehName());

        if (Utils.checkforNetworkconnection(MainActivity.this)) {
            startActivity(new Intent(this, VehicleDetailsActivity.class));
        } else {
            Utils.getSnackbar(findViewById(R.id.homelayout), "There is no internet.Please switch on internet.");
        }
        return true;
    }

    @Override
    public void setOnAlertStatusOnClickListener(int position) {
        strVehicleId = mAdapter.getItem(position).getVehId();
        strVehName = mAdapter.getItem(position).getVehName();
        if (Utils.checkforNetworkconnection(MainActivity.this)) {
            startActivity(new Intent(this, NotificationActivity.class));
        } else {
            Utils.getSnackbar(findViewById(R.id.homelayout), "There is no internet.Please switch on internet.");
        }
    }

    @Override
    public void onLiveTrackUpdation(LiveTrackModel livetracking, ScheduledExecutorService scheduleLiveTrack) {

        liveTrackModel = livetracking;
        scheduledExecutorServiceActivity = scheduleLiveTrack;
//        if (!isSpinner) {
            spinner.setAdapter(customSpinnerAdapter);
//        }
        if (currentFilter.toString().contains("All")) {
            spinner.setSelection(0);
        } else if (currentFilter.toString().contains("Moving")) {
            spinner.setSelection(1);
        } else if (currentFilter.toString().contains("Halted")) {
            spinner.setSelection(2);
        } else if (currentFilter.toString().contains("Notworking")) {
            spinner.setSelection(3);
        } else {
            spinner.setSelection(4);
        }
    }

    @Override
    public void onNetworkStatusChecked(String responseCode) {

        if (responseCode.contains("Session")) {
            if (db == null) {
                db = new MySectionListDb(MainActivity.this);
            }
            Utils.sessionExpiredForLoginAndDashBoardService(db, snackbarLayout, SharedPreferencesClass.getUname(), SharedPreferencesClass.getPwd());
        } else if (responseCode.contains("There is no internet")) {
            setRecylerViewData();
        } else {
            Utils.getSnackbar(snackbarLayout, responseCode);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {
            Toast.makeText(this, "You pressed the home button!", Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}