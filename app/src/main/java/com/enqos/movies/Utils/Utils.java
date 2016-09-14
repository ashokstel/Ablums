package com.enqos.movies.Utils;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

/**
 * @author Ashok
 */

public  class Utils {


    public static boolean checkforNetworkconnection(Context context) {
        ConnectivityManager connmgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = connmgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifinetwork = connmgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifinetwork.isConnected() && wifinetwork.isAvailable() || mobile != null && mobile.isConnected() && mobile.isAvailable();
    }
    public static Snackbar getSnackbar(View view, String message) {

        Snackbar snackbar = Snackbar
                .make(view, message, Snackbar.LENGTH_LONG);
        View view1 = snackbar.getView();
        TextView tv = (TextView) view1.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
        return snackbar;
    }

}
