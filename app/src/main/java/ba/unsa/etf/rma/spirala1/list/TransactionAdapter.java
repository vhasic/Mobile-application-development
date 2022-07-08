package ba.unsa.etf.rma.spirala1.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import ba.unsa.etf.rma.spirala1.R;
import ba.unsa.etf.rma.spirala1.data.Type;
import ba.unsa.etf.rma.spirala1.data.Transaction;

public class TransactionAdapter extends ArrayAdapter<Transaction> {
    int resource;
    public TextView titleView;
    public TextView iznosView;
    public ImageView imageView;

    public TransactionAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Transaction> objects) {
        super(context, resource, objects);
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LinearLayout newView;
        if (convertView == null) {
            newView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater li;
            li = (LayoutInflater)getContext().
                    getSystemService(inflater);
            li.inflate(resource, newView, true);
        } else {
            newView = (LinearLayout)convertView;
        }

        Transaction transaction = getItem(position);

        titleView = newView.findViewById(R.id.title);
        iznosView = newView.findViewById(R.id.iznos);
        imageView = newView.findViewById(R.id.icon);
        titleView.setText(transaction.getTitle());
        iznosView.setText(Double.toString(transaction.getAmount()));

        Type type = transaction.getType();
        if(type.equals(Type.INDIVIDUALINCOME)) imageView.setImageResource(R.drawable.individual_income);
        else if(type.equals(Type.INDIVIDUALPAYMENT)) imageView.setImageResource(R.drawable.individual_payment);
        else if(type.equals(Type.PURCHASE)) imageView.setImageResource(R.drawable.purchase);
        else if(type.equals(Type.REGULARINCOME)) imageView.setImageResource(R.drawable.regular_income);
        else imageView.setImageResource(R.drawable.regular_payment);

        return newView;
    }
    public void setTransactions(ArrayList<Transaction> transactions) {
        this.clear();
        this.addAll(transactions);
    }

    public Transaction getTransaction(int position) {
        return getItem(position);
    }
}
