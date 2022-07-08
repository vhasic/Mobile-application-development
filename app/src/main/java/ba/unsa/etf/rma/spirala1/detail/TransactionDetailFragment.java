package ba.unsa.etf.rma.spirala1.detail;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ba.unsa.etf.rma.spirala1.R;
import ba.unsa.etf.rma.spirala1.data.Account;
import ba.unsa.etf.rma.spirala1.data.Datum;
import ba.unsa.etf.rma.spirala1.data.Transaction;
import ba.unsa.etf.rma.spirala1.data.Type;
import ba.unsa.etf.rma.spirala1.data.SharedViewModel;
import ba.unsa.etf.rma.spirala1.util.ConnectivityBroadcastReceiver;


public class TransactionDetailFragment extends Fragment {

    private EditText title;
    private EditText amount;
    private EditText date;
    private EditText type;
    private EditText itemDescription;
    private EditText transactionInterval;
    private EditText endDate;
    private Button save;
    private Button delete;
    private Transaction transaction;
    private Transaction novaTransakcija;
    private Account account;
    private boolean izmjena=false;
    private TextView rezimRada;

    private boolean connected;
    private ConnectivityBroadcastReceiver receiver = new ConnectivityBroadcastReceiver(this);
    private IntentFilter filter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");



    private OnSaveClick onSaveClick;
    public interface OnSaveClick {
        public void onSaveClicked(String action, Transaction oldTransaction, Transaction newTransaction ,Account account);
    }


    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
        postaviFieldRezimRada();
    }

    private void postaviFieldRezimRada() {
        if(!connected) {
            if(izmjena) rezimRada.setText("Offline izmjena");
            else rezimRada.setText("Offline dodavanje");
        }
        else rezimRada.setText("");
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(receiver);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);

        title = view.findViewById(R.id.title);
        amount = view.findViewById(R.id.amount);
        date = view.findViewById(R.id.date);
        type = view.findViewById(R.id.type);
        itemDescription = view.findViewById(R.id.itemDescription);
        transactionInterval = view.findViewById(R.id.transactionInterval);
        endDate = view.findViewById(R.id.endDate);
        save = view.findViewById(R.id.save);
        delete = view.findViewById(R.id.delete);

        rezimRada=view.findViewById(R.id.rezimRada);


        if (getArguments() != null && getArguments().containsKey("Transaction") && getArguments().containsKey("action")
                && getArguments().containsKey("account")) {

            onSaveClick= (OnSaveClick) getActivity();

            final String action = getArguments().getString("action");
            account = (Account) getArguments().getSerializable("account");
            if (action.equals("edit/delete")) {
                transaction = (Transaction) getArguments().getSerializable("Transaction");
                title.setText(transaction.getTitle());
                amount.setText(String.valueOf(transaction.getAmount()));
                //date.setText(new Datum(transaction.getDate()).toString());
                date.setText(new Datum(transaction.getDate(),new SimpleDateFormat("dd.MM.yyyy",Locale.ENGLISH)).toString());
                type.setText(transaction.getType().toString());
                itemDescription.setText(transaction.getItemDescription());
                transactionInterval.setText(String.valueOf(transaction.getTransactionInterval()));
                if (transaction.getEndDate() != null)
                    //endDate.setText(new Datum(transaction.getEndDate()).toString());
                    endDate.setText(new Datum(transaction.getEndDate(),new SimpleDateFormat("dd.MM.yyyy",Locale.ENGLISH)).toString());
                else endDate.setText("");
                izmjena = true;
                delete.setEnabled(true);
            } else {
                izmjena = false;
                delete.setEnabled(false);
            }

            postaviFieldRezimRada();

            postaviBoje();

        save.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                //format datuma je promijenjen u "dd.MM.yyyy", nakon sto je u drugoj spirali receno da trebaju i dani
                //tip mora biti jedan od navedenih tipova u zadatku
                //edit
                if(izmjena) {
                    novaTransakcija = getTransaction(new Transaction());

                    if(novaTransakcija.provjeriNetacnost()){
                        AlertDialog diaBox = pogresanUnos("Uneseni podaci nisu ispravni!");
                        diaBox.show();
                    }
                    else {
                        //kod izmjene treba provjerit stanje samo onda kada se mijenja cijena
                        //pri izmjeni nestaje stara vrijednost zato je: novaTransakcija.getAmount()-transaction.getAmount()
                        if(transaction.getAmount()!=novaTransakcija.getAmount() &&
                                account.provjeriStanje(novaTransakcija.getDate().get(Calendar.MONTH),novaTransakcija.getAmount()-transaction.getAmount())){
                            AlertDialog diaBox = potvrdiEdit();
                            diaBox.show();
                        }
                        else{
                            izmijeniTransakciju();
                            setTransparent();
                        }
                    }
                }
                else{
                    transaction=getTransaction(new Transaction());
                    if(transaction.provjeriNetacnost()){
                        AlertDialog diaBox = pogresanUnos("Uneseni podaci nisu ispravni!");
                        diaBox.show();
                    }
                    else {
                        if(account.provjeriStanje(transaction.getDate().get(Calendar.MONTH),transaction.getAmount())){
                            AlertDialog diaBox = potvrdiDodavanje();
                            diaBox.show();
                        }
                        else {
                            dodajTransakciju();
                        }

                    }
                }
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog diaBox = potvrdaBrisanja();
                diaBox.show();
            }
        });
        }

        return view;
    }



    private void izmijeniTransakciju() {
        account.promijeniStanje(novaTransakcija.getAmount()-transaction.getAmount());
        onSaveClick.onSaveClicked("edit",transaction,novaTransakcija,account);
    }

    private void dodajTransakciju() {
        account.promijeniStanje(transaction.getAmount());
        onSaveClick.onSaveClicked("add",null,transaction,account);
    }

    private Transaction getTransaction(Transaction novaTransakcija) {
        novaTransakcija.setTitle(title.getText().toString());
        novaTransakcija.setItemDescription(itemDescription.getText().toString());
        try {
            novaTransakcija.setAmount(Double.parseDouble(String.valueOf(amount.getText())));
        }
        catch (NullPointerException | NumberFormatException e){
            novaTransakcija.setAmount(0);
        }
        try {
            novaTransakcija.setTransactionInterval(Integer.parseInt(String.valueOf(transactionInterval.getText())));
        }
        catch (NullPointerException | NumberFormatException e){
            novaTransakcija.setTransactionInterval(0);
        }
        novaTransakcija.setDate(new Datum(date.getText().toString()).getDate());
        novaTransakcija.setEndDate(new Datum(endDate.getText().toString()).getDate());
        try {
            novaTransakcija.setType(Type.valueOf(type.getText().toString().toUpperCase()));
        }
        catch (IllegalArgumentException | NullPointerException e){
            novaTransakcija.setType(null);
        }
        return novaTransakcija;
    }

    private void postaviBoje() {
        title.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                title.setBackgroundColor(Color.parseColor("#4CAF50"));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        amount.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                amount.setBackgroundColor(Color.parseColor("#4CAF50"));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        date.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                date.setBackgroundColor(Color.parseColor("#4CAF50"));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        itemDescription.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                itemDescription.setBackgroundColor(Color.parseColor("#4CAF50"));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        transactionInterval.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                transactionInterval.setBackgroundColor(Color.parseColor("#4CAF50"));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        type.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                type.setBackgroundColor(Color.parseColor("#4CAF50"));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        endDate.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                endDate.setBackgroundColor(Color.parseColor("#4CAF50"));
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }

    private void setTransparent(){
        title.setBackgroundColor(Color.TRANSPARENT);
        amount.setBackgroundColor(Color.TRANSPARENT);
        date.setBackgroundColor(Color.TRANSPARENT);
        itemDescription.setBackgroundColor(Color.TRANSPARENT);
        transactionInterval.setBackgroundColor(Color.TRANSPARENT);
        type.setBackgroundColor(Color.TRANSPARENT);
        endDate.setBackgroundColor(Color.TRANSPARENT);
    }

    private AlertDialog potvrdaBrisanja()
    {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getContext())
                .setTitle("Delete")
                .setMessage("Do you want to Delete")

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        account.promijeniStanje(-1*transaction.getAmount());
                        onSaveClick.onSaveClicked("delete",transaction,null,account);
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        return myQuittingDialogBox;
    }
    private AlertDialog potvrdiDodavanje()
    {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getContext())
                .setTitle("Information")
                .setMessage("Prelazite zadani limit.\n Da li ste sigurni da želite napraviti izmjene")

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        dodajTransakciju();
                        dialog.dismiss();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .create();

        return myQuittingDialogBox;
    }
    private AlertDialog potvrdiEdit()
    {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getContext())
                .setTitle("Information")
                .setMessage("Prelazite zadani limit.\n Da li ste sigurni da želite napraviti izmjene")

                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        izmijeniTransakciju();
                        setTransparent();
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .create();

        return myQuittingDialogBox;
    }
    private AlertDialog pogresanUnos(String s)
    {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getContext())
                // set message, title, and icon
                .setTitle("Incorrect data")
                .setMessage(s)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }

                })
                .create();

        return myQuittingDialogBox;
    }
}
