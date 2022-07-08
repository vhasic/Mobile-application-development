package ba.unsa.etf.rma.spirala1.list;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import ba.unsa.etf.rma.spirala1.R;
import ba.unsa.etf.rma.spirala1.data.Account;
import ba.unsa.etf.rma.spirala1.data.Datum;
import ba.unsa.etf.rma.spirala1.data.Transaction;
import ba.unsa.etf.rma.spirala1.data.Type;


public class TransactionListFragment extends Fragment implements ITrancationView{
    private ListView listaTransakcija;
    private Spinner spinnerFilter, spinnerSort;
    private Button addTransaction;
    private TextView globalAmount,limit;
    private Button right,left;
    private Button date;

    private TransactionAdapter adapter;
    private TransactionSpinerAdapter spinerAdapter;
    private Datum datum=new Datum();

    private static final int SECOND_ACTIVITY_REQUEST_CODE = 0;
    private ITransactionPresenter transactionPresenter;

    private int odabranaTransakcija=-1;

    private OnItemClick onItemClick;
    public interface OnItemClick {
        void onItemClicked(Transaction transaction, String action, Account account);
    }



    public ITransactionPresenter getPresenter() {
        if (transactionPresenter == null) {
            transactionPresenter = new TransactionPresenter(this, getActivity());
        }
        return transactionPresenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView= inflater.inflate(R.layout.fragment_transaction_list, container, false);
        //ovdje se tek može dobaviti context
        transactionPresenter=getPresenter();
        //
        adapter=new TransactionAdapter(getActivity(), R.layout.list_element, new ArrayList<Transaction>());

        listaTransakcija=fragmentView.findViewById(R.id.lista);
        spinnerFilter =fragmentView.findViewById(R.id.filter);
        spinnerSort =fragmentView.findViewById(R.id.sort);
        addTransaction=fragmentView.findViewById(R.id.addTransaction);
        globalAmount=fragmentView.findViewById(R.id.globalAmount);
        limit=fragmentView.findViewById(R.id.limit);
        right=fragmentView.findViewById(R.id.right);
        left=fragmentView.findViewById(R.id.left);
        date=fragmentView.findViewById(R.id.date);

        date.setEnabled(false);

        listaTransakcija.setAdapter(adapter);

        //povezivanje Global amount i limit sa accountom
        changeGlobalAmount(transactionPresenter.getAccount().getBudget());
        changeLimit(transactionPresenter.getAccount().getTotalLimit());


        postaviDatum();
        //transactionPresenter.filtrirajListu(Type.ALL,datum.getDate().getTime()); //promjena
        listaTransakcija.setOnItemClickListener(getOnItemClickListener());

        onItemClick= (OnItemClick) getActivity();

        //promjena datuma
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                datum.addMonth();
                postaviDatum();
                transactionPresenter.filtrirajListu((Type) spinnerFilter.getSelectedItem(),datum.getDate().getTime());
            }
        });
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                datum.subMonth();
                postaviDatum();
                transactionPresenter.filtrirajListu((Type) spinnerFilter.getSelectedItem(),datum.getDate().getTime());
            }
        });


        setSortSpinner();

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                transactionPresenter.sortirajListu((String) spinnerSort.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                System.out.println("nista nije izabrano");
            }

        });

        spinerAdapter=new TransactionSpinerAdapter(getActivity(), R.layout.spiner_element, Type.values());
        spinnerFilter.setAdapter(spinerAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                transactionPresenter.filtrirajListu((Type) spinnerFilter.getSelectedItem(),datum.getDate().getTime());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                System.out.println("nista nije izabrano");
            }

        });


        //dodavanje transakcije
        addTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                onItemClick.onItemClicked(null,"add",transactionPresenter.getAccount());
            }
        });

        return fragmentView;
    }



    private AdapterView.OnItemClickListener getOnItemClickListener() {
        //na klik se ovo izvrsava
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //ponovnim klikom vrsi se dodavanje transakcije, kako je trazeno u zadatku
                if(odabranaTransakcija!=position) {
                    odabranaTransakcija = position;
                    listaTransakcija.getChildAt(position).setBackgroundColor(Color.parseColor("#4CAF50"));
                    Transaction transaction = adapter.getTransaction(position);
                    onItemClick.onItemClicked(transaction, "edit/delete", transactionPresenter.getAccount());
                }
                else{
                    //nakon drugog klika se opet moze kliknuti isti item da bi se editovao
                    odabranaTransakcija=-1;
                    listaTransakcija.getChildAt(position).setBackgroundColor(Color.TRANSPARENT);
                    addTransaction.performClick();
                }
            }
        };
    }


    @Override
    public void postaviDatum() {
        date.setText(datum.toString());
    }

    private void setSortSpinner() {
        //spiner sort by
        String[] arraySpinner = new String[] {"Price - Ascending", "Price - Descending", "Title - Ascending", "Title - Descending",
                "Date - Ascending", "Date - Descending","Sort by"};
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item, arraySpinner){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount()-1; // you dont display last item. It is used as hint.
            }
        };
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);
        spinnerSort.setSelection(sortAdapter.getCount());
    }

    @Override
    public void setTransactions(ArrayList<Transaction> transactions) {
        adapter.setTransactions(transactions);
    }

    @Override
    public void notifyTransactionListDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void insertTransaction(Transaction transaction) {
        transactionPresenter.insertTransaction(transaction);
        transactionPresenter.filtrirajListu((Type) spinnerFilter.getSelectedItem(),datum.getDate().getTime());
    }

    @Override
    public void deleteTransaction(Transaction transaction) {
        transactionPresenter.deleteTransaction(transaction);
        transactionPresenter.filtrirajListu((Type) spinnerFilter.getSelectedItem(),datum.getDate().getTime());
    }

    @Override
    public void editTransaction(Transaction stara, Transaction nova) {
        transactionPresenter.editTransaction(stara,nova);
        transactionPresenter.filtrirajListu((Type) spinnerFilter.getSelectedItem(),datum.getDate().getTime());
    }

    @Override
    public void changeGlobalAmount(double amount) {
        globalAmount.setText("Global amount: "+amount);
    }
    @Override
    public void changeLimit(double l) {
        limit.setText("Limit: "+l);
    }
}
