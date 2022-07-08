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

import ba.unsa.etf.rma.spirala1.R;
import ba.unsa.etf.rma.spirala1.data.Type;

public class TransactionSpinerAdapter extends ArrayAdapter<Type> {
    int resource;
    public TextView titleView;
    public ImageView imageView;

    public TransactionSpinerAdapter(@NonNull Context context, int resource, @NonNull Type[] objects) {
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

        Type type = getItem(position);

        titleView = newView.findViewById(R.id.title);
        imageView = newView.findViewById(R.id.icon);


        if(type.equals(Type.INDIVIDUALINCOME)) {
            imageView.setImageResource(R.drawable.individual_income);
        }
        else if(type.equals(Type.INDIVIDUALPAYMENT)) {
            imageView.setImageResource(R.drawable.individual_payment);
        }
        else if(type.equals(Type.PURCHASE)) {
            imageView.setImageResource(R.drawable.purchase);
        }
        else if(type.equals(Type.REGULARINCOME)) {
            imageView.setImageResource(R.drawable.regular_income);
        }
        else if(type.equals(Type.REGULARPAYMENT)){
            imageView.setImageResource(R.drawable.regular_payment);
        }
        titleView.setText(type.toString());


        return newView;
    }
    // It gets a View that displays in the drop down popup the data at the specified position
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }
}
