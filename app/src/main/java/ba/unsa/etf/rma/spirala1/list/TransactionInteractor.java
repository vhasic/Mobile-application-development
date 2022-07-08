package ba.unsa.etf.rma.spirala1.list;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import ba.unsa.etf.rma.spirala1.data.Account;
import ba.unsa.etf.rma.spirala1.data.SharedViewModel;
import ba.unsa.etf.rma.spirala1.data.Transaction;
import ba.unsa.etf.rma.spirala1.data.Type;
import ba.unsa.etf.rma.spirala1.util.TransactionDBOpenHelper;


public class TransactionInteractor implements ITransactionInteractor, MojResultReceiver.Receiver {
    private Context context;
    private IRefresh presenter;
    private SharedViewModel viewModel=SharedViewModel.getInstance();

    private TransactionDBOpenHelper transactionDBOpenHelper;
    SQLiteDatabase database;
    private SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);

    private MojResultReceiver mReceiver;
    private ArrayList<Transaction> transactions=new ArrayList<>();
    private Account account=new Account();
    private boolean transakcijePromijenjene=true;
    private boolean promijenjenAccount=true;
    private boolean prethodnaKonekcija=true;

    public TransactionInteractor() {
    }

    //da bih pozvao servis moram imat contex
    public TransactionInteractor(Context context, IRefresh presenter) {
        this.context = context;
        this.presenter = presenter;
        transactionDBOpenHelper = new TransactionDBOpenHelper(context);
        //obrisiTrnsakcijeIzBaze();
        //dodajAccountUBazu(new Account(1000,1000,100));
    }


    private boolean checkInternetConnection() {
        ConnectivityManager cm =(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private ContentValues getTransactionContentValues(Transaction transaction) {
        String date=null,endDate=null,type=null;
        try{
            type=transaction.getType().toString();
            date=format.format(transaction.getDate().getTime());
            endDate=format.format(transaction.getEndDate().getTime());
        } catch (Exception ignored) {
        }

        ContentValues values = new ContentValues();
        values.put(TransactionDBOpenHelper.TRANSACTION_ID, transaction.getId());
        values.put(TransactionDBOpenHelper.TRANSACTION_TITLE, transaction.getTitle());
        values.put(TransactionDBOpenHelper.ITEM_DESCRIPTION, transaction.getItemDescription());
        values.put(TransactionDBOpenHelper.TRANSACTION_AMOUNT, transaction.getAmount());
        values.put(TransactionDBOpenHelper.TRANSACTION_INTERVAL, transaction.getTransactionInterval());
        values.put(TransactionDBOpenHelper.TRANSACTION_DATE, date);
        values.put(TransactionDBOpenHelper.TRANSACTION_ENDDATE, endDate);
        values.put(TransactionDBOpenHelper.TRANSACTION_TYPE, type);
        return values;
    }

    public ArrayList<Transaction> getTransactionsFromDatabase(String tabela) {
        ArrayList<Transaction> tranaskcijeIzBaze = new ArrayList<>();
        String query = "SELECT *"  + " FROM " + tabela;
        database = transactionDBOpenHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery(query,null);
        if(cursor.moveToFirst()) {
            do{
                int databaseIdPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_INTERNAL_ID);
                int webServisIdPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_ID);
                int titlePos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_TITLE);
                int itemDescriptionPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.ITEM_DESCRIPTION);
                int amountPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_AMOUNT);
                int datePos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_DATE);
                int endDatePos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_ENDDATE);
                int typePos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_TYPE);
                int intervalPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TRANSACTION_INTERVAL);

                int databaseId=cursor.getInt(databaseIdPos);
                int id=cursor.getInt(webServisIdPos);
                String dateString=cursor.getString(datePos);
                String endDateString=cursor.getString(endDatePos);
                String title=cursor.getString(titlePos);
                double amount=cursor.getDouble(amountPos);
                String itemDescription=cursor.getString(itemDescriptionPos);
                String typeString=cursor.getString(typePos);
                int interval=cursor.getInt(intervalPos);

                Calendar date = null, endDate=null;
                Type type=null;
                try {
                    Date d = format.parse(dateString);
                    date=new GregorianCalendar();
                    date.setTime(d);
                    type=Type.valueOf(typeString);
                    Date ed=format.parse(endDateString);
                    endDate=new GregorianCalendar();
                    endDate.setTime(ed);
                } catch (Exception ignored) {
                }

                Transaction t = new Transaction(databaseId,id,date,amount,title,type,itemDescription,interval,endDate);
                tranaskcijeIzBaze.add(t);
            }while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return tranaskcijeIzBaze;
    }

    private void zapisiOfflinePromjeneNaWebServis() {
        ArrayList<Transaction> transakcijeIzBaze=getTransactionsFromDatabase(TransactionDBOpenHelper.TRANSACTIONS_TABLE);
        for(Transaction t:transakcijeIzBaze){
            if(t.getId()==0){
                //treba je dodati na server
                Intent intent = new Intent(context, TransactionsIntentService.class);
                mReceiver = null;
                intent.putExtra("action", "insert");
                intent.putExtra("Transaction", t);
                intent.putExtra("receiver", mReceiver);
                context.startService(intent);
            }
            else{
                //treba je editovati na serveru
                Intent intent = new Intent(context, TransactionsIntentService.class);
                mReceiver = null;
                intent.putExtra("action", "editTransaction");
                intent.putExtra("newTransaction", t);
                intent.putExtra("oldTransaction", t);
                intent.putExtra("receiver", mReceiver);
                context.startService(intent);
            }
        }

        ArrayList<Transaction> obrisaneTransakcije=getTransactionsFromDatabase(TransactionDBOpenHelper.DELETED_TRANSACTIONS_TABLE);
        for(Transaction t:obrisaneTransakcije){
            if(t.getId()!=0) {
                Intent intent = new Intent(context, TransactionsIntentService.class);
                mReceiver = null;
                intent.putExtra("action", "delete");
                intent.putExtra("Transaction", t);
                intent.putExtra("receiver", mReceiver);
                context.startService(intent);
            }
        }

        account=getAccountFromDatabase();
        editAccount(account);
        //nakon što je zapisano na web servis izbriši sve iz baze
        obrisiTrnsakcijeIzBaze();
    }

    private void obrisiTrnsakcijeIzBaze() {
        database = transactionDBOpenHelper.getWritableDatabase();
        transactionDBOpenHelper.clearTransactionsTable(database);
        transactionDBOpenHelper.clearInsertedTransactionsTable(database);
        transactionDBOpenHelper.clearEditedTransactionsTable(database);
        transactionDBOpenHelper.clearDeletedTransactionsTable(database);
        database.close();
    }


    private ContentValues getAccountContentValues(Account account) {
        ContentValues values = new ContentValues();
        values.put(TransactionDBOpenHelper.BUDGET, account.getBudget());
        values.put(TransactionDBOpenHelper.TOTAL_lIMIT, account.getTotalLimit());
        values.put(TransactionDBOpenHelper.MONTH_lIMIT, account.getMonthLimit());
        return values;
    }


    @Override
    public ArrayList<Transaction> get() {
        if(prethodnaKonekcija==false && checkInternetConnection()){
            //pri bilo kojoj promjeni na list fragmentu offline promjene će biti zapisane na web servis
            Toast.makeText(context, "Zapisivanje offline promjena", Toast.LENGTH_SHORT).show();
            zapisiOfflinePromjeneNaWebServis();
        }
        if(prethodnaKonekcija!=checkInternetConnection()){
            prethodnaKonekcija=!prethodnaKonekcija;
            transakcijePromijenjene=true;
        }

        if(!checkInternetConnection()){
            transactions=getTransactionsFromDatabase(TransactionDBOpenHelper.TRANSACTIONS_TABLE);
        }
        else {
            //zbog efikasosti: samo prvi put dobavlja sa neta
            if (transakcijePromijenjene) {
                Intent intent = new Intent(context, TransactionsIntentService.class);
                mReceiver = new MojResultReceiver(new Handler());
                mReceiver.setReceiver(this);
                intent.putExtra("action", "getAll");
                intent.putExtra("receiver", mReceiver);
                context.startService(intent);
                transakcijePromijenjene = false;
            }
        }
        return transactions;
    }


    //ovo nije korišteno zbog neefikasnosti
    //ovdje se ne može raditi efikasnije, jer se uvijek mijenja zbog parametara
    @Override
    public ArrayList<Transaction> get(Type type, String sort, Date date) {
        Intent intent = new Intent(context, TransactionsIntentService.class);
        mReceiver = new MojResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        intent.putExtra("action", "getFiltered");
        intent.putExtra("type", type);
        intent.putExtra("sort", sort);
        intent.putExtra("date", date);
        intent.putExtra("receiver", mReceiver);
        context.startService(intent);
        transakcijePromijenjene=false;
        return transactions;
    }

    private void dodajTransakcijuUBazu(Transaction transaction, String tabela) {
        database = transactionDBOpenHelper.getWritableDatabase();
        ContentValues values = getTransactionContentValues(transaction);
        database.insertOrThrow(tabela, null, values);
        database.close();
    }


    //šalje podatke na web kao POST
    @Override
    public void insert(Transaction t) {
        if(checkInternetConnection()) {
            Intent intent = new Intent(context, TransactionsIntentService.class);
            mReceiver = new MojResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            intent.putExtra("action", "insert");
            intent.putExtra("Transaction", t);
            intent.putExtra("receiver", mReceiver);
            context.startService(intent);
        }
        else {
            dodajTransakcijuUBazu(t,TransactionDBOpenHelper.INSERTED_TRANSACTIONS_TABLE);
            dodajTransakcijuUBazu(t,TransactionDBOpenHelper.TRANSACTIONS_TABLE);
        }
        //pošto se promijenila lista transakcija dobavi je ponovo
        transakcijePromijenjene=true;
    }

    private void obrisiTransakcijuIzBaze(Transaction transaction, String tabela) {
        if(transaction.getDatabaseID()==0) return;
        database = transactionDBOpenHelper.getWritableDatabase();
        database.delete(tabela,"internalId="+transaction.getDatabaseID(),null);
        database.close();
    }

    //putem weba preko DELETE
    @Override
    public void delete(Transaction t) {
        if(checkInternetConnection()) {
            Intent intent = new Intent(context, TransactionsIntentService.class);
            mReceiver = new MojResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            intent.putExtra("action", "delete");
            intent.putExtra("Transaction", t);
            intent.putExtra("receiver", mReceiver);
            context.startService(intent);
        }
        else{
            dodajTransakcijuUBazu(t,TransactionDBOpenHelper.DELETED_TRANSACTIONS_TABLE);
            obrisiTransakcijuIzBaze(t,TransactionDBOpenHelper.TRANSACTIONS_TABLE); //
        }
        //pošto se promijenila lista transakcija dobavi je ponovo
        transakcijePromijenjene=true;
    }

    private void promijeniTransakcijuUBazi(Transaction stara, Transaction nova, String tabela) {
        nova.setId(stara.getId());
        nova.setDatabaseID(stara.getDatabaseID());
        if(stara.getDatabaseID()==0) dodajTransakcijuUBazu(nova,tabela);
        database = transactionDBOpenHelper.getWritableDatabase();
        ContentValues values = getTransactionContentValues(nova);
        database.update(tabela, values,"internalId="+ stara.getDatabaseID(),null);
        database.close();
    }

    @Override
    public void edit(Transaction stara, Transaction nova) {
        if(checkInternetConnection()) {
            Intent intent = new Intent(context, TransactionsIntentService.class);
            mReceiver = new MojResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            intent.putExtra("action", "editTransaction");
            intent.putExtra("newTransaction", nova);
            intent.putExtra("oldTransaction", stara);
            intent.putExtra("receiver", mReceiver);
            context.startService(intent);
        }
        else{
            dodajTransakcijuUBazu(nova,TransactionDBOpenHelper.EDITED_TRANSACTIONS_TABLE);
            promijeniTransakcijuUBazi(stara,nova,TransactionDBOpenHelper.TRANSACTIONS_TABLE);
        }
        //pošto se promijenila lista transakcija dobavi je ponovo
        transakcijePromijenjene=true;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case TransactionsIntentService.STATUS_RUNNING:
                //obavijest korisniku
                Toast.makeText(context, "Dobavljanje transakcija", Toast.LENGTH_SHORT).show();
                break;
            case TransactionsIntentService.STATUS_FINISHED:
                String akcija=resultData.getString("action");
                switch (akcija) {
                    case "get":
                        transactions = (ArrayList<Transaction>) resultData.getSerializable("transactions");
                        if(presenter!=null) presenter.refreshTransactions();
                        Toast.makeText(context, "Dobavljanje završeno", Toast.LENGTH_SHORT).show();
                        break;
                    case "insert":
                        Toast.makeText(context, "Transakcija dodana", Toast.LENGTH_SHORT).show();
                        if(presenter!=null) presenter.refreshTransactions();
                        break;
                    case "delete":
                        Toast.makeText(context, "Transakcija obrisana", Toast.LENGTH_SHORT).show();
                        if(presenter!=null) presenter.refreshTransactions();
                        break;
                    case "editTransaction":
                        Toast.makeText(context, "Transakcija promijenjena", Toast.LENGTH_SHORT).show();
                        if(presenter!=null) presenter.refreshTransactions();
                        break;
                    case "getAccount":
                    case "editAccount":
                        account = (Account) resultData.getSerializable("account");
                        promijeniAccountUBazi(account);
                        if(presenter!=null) presenter.refreshAccount(account);
                        break;
                }

                break;
            case TransactionsIntentService.STATUS_ERROR:
                /* Slucaj kada je doslo do greske */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                break;
        }
    }


    private Account getAccountFromDatabase() {
        String query = "SELECT *"  + " FROM " + TransactionDBOpenHelper.ACCOUNT_TABLE;
        database = transactionDBOpenHelper.getWritableDatabase();
        Cursor cursor = database.rawQuery(query,null);

        Account a = new Account();
        if(cursor.moveToFirst()) {
            int databaseIdPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.ACCOUNT_INTERNAL_ID);
            int budgetPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.BUDGET);
            int monthLimitPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.MONTH_lIMIT);
            int totalLimitPos = cursor.getColumnIndexOrThrow(TransactionDBOpenHelper.TOTAL_lIMIT);

            int databaseId=cursor.getInt(databaseIdPos);
            double budget=cursor.getDouble(budgetPos);
            double totalLimit=cursor.getDouble(totalLimitPos);
            double monthLimit=cursor.getDouble(monthLimitPos);

            a = new Account(databaseId,budget,totalLimit,monthLimit);
        }
        cursor.close();
        database.close();
        return a;
    }

    private void dodajAccountUBazu(Account account1) {
        database = transactionDBOpenHelper.getWritableDatabase();
        transactionDBOpenHelper.clearAccountsTable(database);
        ContentValues values = getAccountContentValues(account1);
        database.insert(TransactionDBOpenHelper.ACCOUNT_TABLE, null, values);
        Account a=getAccountFromDatabase();
        System.out.println(a);
        database.close();
    }

    private void promijeniAccountUBazi(Account account) {
        database = transactionDBOpenHelper.getWritableDatabase();
        ContentValues values = getAccountContentValues(account);
        //umjesto account.getDatabaseID() hardkodirano je 1 jer se u bazi nalazi samo ovaj jedan account
        database.update(TransactionDBOpenHelper.ACCOUNT_TABLE, values,"internalId="+ 1,null);
        database.close();
    }

    @Override
    public void editAccount(Account account) {
        if(checkInternetConnection()) {
            Intent intent = new Intent(context, TransactionsIntentService.class);
            mReceiver = new MojResultReceiver(new Handler());
            mReceiver.setReceiver(this);
            intent.putExtra("action", "editAccount");
            intent.putExtra("account", account);
            intent.putExtra("receiver", mReceiver);
            context.startService(intent);
        }
        else{
            this.account=account;
            promijeniAccountUBazi(account);
        }
        promijenjenAccount=true;
    }


    @Override
    public Account getAccount() {
        if(checkInternetConnection()) {
            if (promijenjenAccount) {
                Intent intent = new Intent(context, TransactionsIntentService.class);
                mReceiver = new MojResultReceiver(new Handler());
                mReceiver.setReceiver(this);
                intent.putExtra("action", "getAccount");
                intent.putExtra("receiver", mReceiver);
                context.startService(intent);
            }
        }
        else {
            account=getAccountFromDatabase();
            viewModel.getAccount().setValue(account);
        }
        promijenjenAccount=false;
        return account;
    }
}
