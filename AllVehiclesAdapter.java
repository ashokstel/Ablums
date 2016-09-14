package com.group10.tml.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.group10.tml.R;
import com.group10.tml.api.APIS;
import com.group10.tml.models.LiveTrackModel;
import com.group10.tml.models.Result;
import com.tml.flexiableadapterlibrary.FlexibleAdapter;
import com.tml.flexiableadapterlibrary.anim.FastScroller;
import com.tml.flexiableadapterlibrary.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by group10 on 11/12/15.
 */
public class AllVehiclesAdapter extends FlexibleAdapter<AllVehiclesAdapter.ViewHolder, Result>
        implements FastScroller.BubbleTextGetter {

    private static final String TAG = AllVehiclesAdapter.class.getSimpleName();

    public interface OnItemClickListener {
        /**
         * Delegate the click event to the listener and check if selection MULTI enabled.<br/>
         * If yes, call toggleActivation.
         *
         * @param position
         * @return true if MULTI selection is enabled, false for SINGLE selection
         */
        boolean onListItemClick(int position);

        /**
         * This always calls toggleActivation after listener event is consumed.
         *
         * @param position
         */
        void setOnAlertStatusOnClickListener(int position);
    }

    public Context mContext;
    private static final int
            EXAMPLE_VIEW_TYPE = 1,
            ROW_VIEW_TYPE = 1;

    private LayoutInflater mInflater;
    private OnItemClickListener mClickListener;
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    Date date = null;
    String strformatedTime = null, strFilterValue;
    Date datenow;
    SimpleDateFormat mFormatter1 = new SimpleDateFormat(
            "yyyy-MM-dd");

    //Selection fields
    private boolean
            mUserLearnedSelection = false,
            mLastItemInActionMode = false,
            mSelectAll = false;
    private LiveTrackModel livetrackingAdapter;

    public AllVehiclesAdapter(Activity activity, String listId, LiveTrackModel livetracking, String strFilterType) {
        super(activity);
        this.mContext = (Context) activity;
        this.mClickListener = (OnItemClickListener) activity;
        livetrackingAdapter = livetracking;
        strFilterValue = strFilterType;
        updateDataSetAsync(listId);
        datenow=new Date();
    }

    /**
     * Param in this example is not used.
     *
     * @param param A custom parameter to filter the DataSet
     */
    public void updateDataSet(String param) {
        //Fill mItems with your custom list
        this.mItems = createExampleItems();
    }
    public Result getNewExampleItem(int i) {
        Result item = livetrackingAdapter.getResults().get(i);
        return item;
    }
    public List<Result> createExampleItems() {
        List<Result> items = new ArrayList<Result>();
        for (int i = 0; i < livetrackingAdapter.getResults().size(); i++) {
            Result item = getNewExampleItem(i);
            if (!hasSearchText() || (hasSearchText() && filterObject(item, getSearchText())))
                items.add(item);
        }
        return items;
    }
    @Override
    public void setMode(int mode) {
        super.setMode(mode);
        if (mode == MODE_SINGLE) mLastItemInActionMode = true;
    }
    @Override
    public void selectAll() {
        mSelectAll = true;
        super.selectAll(EXAMPLE_VIEW_TYPE);
    }
    @Override
    public int getItemViewType(int position) {
//        return (position == 0 && !mUserLearnedSelection && !hasSearchText() ? EXAMPLE_VIEW_TYPE : ROW_VIEW_TYPE);
        return ROW_VIEW_TYPE;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }
        return new ViewHolder(
                mInflater.inflate(R.layout.adapter_allvehicles_items, parent, false),
                this);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Result item = getItem(position);

        holder.tvVehicleName.setTypeface(com.group10.tml.utils.Utils.roboticbold_Typeface(mContext));
        holder.tvDepoName.setTypeface(com.group10.tml.utils.Utils.roboticregular_Typeface(mContext));
        holder.tvLoc.setTypeface(com.group10.tml.utils.Utils.roboticregular_Typeface(mContext));
        holder.tvUpdated.setTypeface(com.group10.tml.utils.Utils.roboticregular_Typeface(mContext));
        if (strFilterValue.equals("offline"))
        {
            holder.itemView.setActivated(isSelected(position));
            //In case of searchText matches with Title or with an Item's field
            // this will be highlighted
            if (hasSearchText()) {
                setHighlightText(holder.tvVehicleName, item.getVehName(), mSearchText);
            } else {
                holder.tvVehicleName.setText(item.getVehName());
            }
            holder.ivVehicleStatus.setImageResource(R.drawable.busnotworking);
        }
        else
        {
            if (item.getVehParamValues().size() > 1)
            {
                //When user scrolls this bind the correct selection status
                holder.itemView.setActivated(isSelected(position));
                //In case of searchText matches with Title or with an Item's field
                // this will be highlighted
                if (hasSearchText()) {
                    setHighlightText(holder.tvVehicleName, item.getVehName(), mSearchText);
                } else {
                    holder.tvVehicleName.setText(item.getVehName());
                }
//            Log.e(TAG, "device data time" + item.getVehParamValues().get(0).getDeviceDateTime());
                try {
                    date = (Date) formatter.parse(item.getVehParamValues().get(1).getDeviceDateTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                strformatedTime = com.group10.tml.utils.Utils.getTimeInStringFormat(formatter.getCalendar());
                long MAX_DURATION = MILLISECONDS.convert(3, MINUTES);
                long duration = new Date().getTime() - date.getTime();
                holder.tvLoc.setText(item.getVehParamValues().get(0).getValue());
                holder.tvDepoName.setText("Depo Name :"+ APIS.strDepoName);
                try {
                    Date df = new Date(item.getVehParamValues().get(1).getDeviceDateMillis());
                    Date pickerDate=mFormatter1.parse(mFormatter1.format(df));;
                    Date currentDate=mFormatter1.parse(mFormatter1.format(new Date()));
                    if(com.group10.tml.utils.Utils.compareTo(pickerDate,currentDate)==0){
                        String  vv = new SimpleDateFormat("HH:mm:ss").format(df);
                      holder.  tvUpdated .setText("Today at"+" "+ vv) ;

                    }else{
                        String vv =new SimpleDateFormat("MMM dd, yyyy HH:mm:ss").format(df);
                      holder.  tvUpdated.setText(vv);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
//                Log.e(TAG, "speed of vehicle is" + "======" + "++++++" + item.getVehParamValues().get(0).getValue());
                if (Integer.valueOf(item.getVehParamValues().get(2).getValue()) == 1) {
                    holder.ivVehicleStatus.setImageResource(R.drawable.candisconnected);
                } else if (strformatedTime.contains("hour") || strformatedTime.contains("day") || strformatedTime.contains("year")) {
                    holder.ivVehicleStatus.setImageResource(R.drawable.busnotworking);
                } else if (Integer.valueOf(item.getVehParamValues().get(1).getValue()) > 3 && duration < MAX_DURATION) {
                    holder.ivVehicleStatus.setImageResource(R.drawable.busss);
                } else {
                    holder.ivVehicleStatus.setImageResource(R.drawable.bushalt);
                }
            }
        }
    }

    @Override
    public String getTextToShowInBubble(int position) {
        return getItem(position).getVehName().substring(5); //This is an example
    }
    private void setHighlightText(TextView textView, String text, String searchText) {
        Spannable spanText = Spannable.Factory.getInstance().newSpannable(text);
        int i = text.toLowerCase(Locale.getDefault()).indexOf(searchText);
        if (i != -1) {
            spanText.setSpan(new ForegroundColorSpan(Utils.getColorAccent(mContext)), i,
                    i + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spanText.setSpan(new StyleSpan(Typeface.BOLD), i,
                    i + searchText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(spanText, TextView.BufferType.SPANNABLE);
        } else {
            textView.setText(text, TextView.BufferType.NORMAL);
        }
    }
    @Override
    protected boolean filterObject(Result myObject, String constraint) {
        String valueText = myObject.getVehName();
        //Filter on Title
        if (valueText != null && valueText.toLowerCase().contains(constraint)) {
            return true;
        }
        return false;
    }
    /**
     * Used for UserLearnsSelection.
     * Must be the base class of extension for Adapter Class.
     */
    static class SimpleViewHolder extends RecyclerView.ViewHolder {
        TextView tvVehicleName,tvLoc,tvUpdated,tvDepoName;
        ImageView ivVehicleStatus;

        AllVehiclesAdapter mAdapter;
        CardView cvDashboard;
        SimpleViewHolder(View view) {
            super(view);
        }
    }
    /**
     * Provide a reference to the views for each data item.
     * Complex data labels may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder.
     */
    static final class ViewHolder extends SimpleViewHolder implements View.OnClickListener {
        ViewHolder(View view, final AllVehiclesAdapter adapter) {
            super(view);
            this.mAdapter = adapter;
            this.tvVehicleName = (TextView) view.findViewById(R.id.tv_vehiclename);
            this.ivVehicleStatus = (ImageView) view.findViewById(R.id.ic_vehiclestatus);
            this.tvLoc=(TextView)view.findViewById(R.id.tv_loc_dashboard);
            this.tvDepoName=(TextView)view.findViewById(R.id.tv_deponame_dashboard);
            this.tvUpdated=(TextView)view.findViewById(R.id.tv_updatedtime_dashboard);
            this.cvDashboard=(CardView)view.findViewById(R.id.card_dashboard);

            this.cvDashboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapter.mClickListener.onListItemClick(getAdapterPosition());
                }
            });


        }
        @Override
        public void onClick(View view) {
            mAdapter.mClickListener.onListItemClick(getAdapterPosition());
        }
    }
}
