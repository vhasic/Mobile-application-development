package ba.unsa.etf.rma.spirala1.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import ba.unsa.etf.rma.spirala1.detail.TransactionDetailFragment;

public class ConnectivityBroadcastReceiver extends BroadcastReceiver {
    TransactionDetailFragment transactionDetailFragment;

    public ConnectivityBroadcastReceiver(TransactionDetailFragment transactionDetailFragment) {
        this.transactionDetailFragment=transactionDetailFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() == null) {
            transactionDetailFragment.setConnected(false);
        }
        else {
            transactionDetailFragment.setConnected(true);
        }
    }
}
