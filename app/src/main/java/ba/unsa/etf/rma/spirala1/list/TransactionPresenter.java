package ba.unsa.etf.rma.spirala1.list;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import ba.unsa.etf.rma.spirala1.data.Account;
import ba.unsa.etf.rma.spirala1.data.SharedViewModel;
import ba.unsa.etf.rma.spirala1.data.Transaction;
import ba.unsa.etf.rma.spirala1.data.Type;


public class TransactionPresenter implements ITransactionPresenter{
    private ITrancationView view;
    private ITransactionInteractor interactor;
    private Context context;

    private ArrayList<Transaction> transakcije=new ArrayList<>();
    SharedViewModel viewModel=SharedViewModel.getInstance();
    private Account account;

    private String trenutniNacinSortiranja;
    private Type trenutniTipFiltriranja=Type.ALL;
    private Date trenutniDatumFiltriranja=Calendar.getInstance().getTime();

    public TransactionPresenter(ITrancationView view, Context context) {
        this.view       = view;
        this.interactor = new TransactionInteractor(context,this);
        this.context    = context;
        //posto je account jedinstven, dobavit ću ga odmah na početku
        account=interactor.getAccount();
    }
    //vezano za account
    private void izracunajMjesecnuPotrosnju(Date date){
        Calendar c=Calendar.getInstance();
        c.setTime(date);
        int mjesec=c.get(Calendar.MONTH);
        account.resetujMjesecnoStanje(mjesec);
        for(Transaction t:transakcije){
            account.promijeniMjesecnoStanje(mjesec,t.getAmount());
        }
    }
    public void izracunajGlobalnuPotrosnju(){
        for(Transaction t:interactor.get()){
            account.promijeniGlobalnoStanje(t.getAmount());
        }
    }


    @Override
    public void refreshTransactions() {
        //transakcije=interactor.get();
        //izracunajGlobalnuPotrosnju();
        filtrirajListu(trenutniTipFiltriranja,trenutniDatumFiltriranja);
        view.setTransactions(transakcije);
        view.notifyTransactionListDataSetChanged();
    }

    //ovo uraditi pomoću neta, da ne gubim bodove ako sam već uradio metodu
    //ako hoću raditi sve preko neta u interaktoru treba izbrisati ono što je dodano radi efikasnosti,
    // i pri dodavanju regular transakcija treba ih refreshovati
    @Override
    public void filtrirajListu(Type type, Date date) {
        trenutniDatumFiltriranja=date;
        trenutniTipFiltriranja=type;

        transakcije=filtrirajPoDatumu(interactor.get(),date);
        dodajRegularTransakcije(type,date);
        transakcije=filtrirajPoTipu(transakcije,type);
        izracunajMjesecnuPotrosnju(date);
        //sortiraj je po zadanom nacinu
        if(trenutniNacinSortiranja!=null) sortirajListu(trenutniNacinSortiranja);

        /*transakcije=interactor.get(type,trenutniNacinSortiranja,date);
        //dodajRegularTransakcije(type, date);
        izracunajMjesecnuPotrosnju(date);*/

        view.changeGlobalAmount(account.getBudget());
        view.setTransactions(transakcije);
        view.notifyTransactionListDataSetChanged();
    }

    private void dodajRegularTransakcije(Type type, Date date) {
        //ovo ovako ne valja zbog kašnjenja; morao bi i za ovo uvesti refresh
        /*ArrayList<Transaction> regularTransakcije=interactor.get(Type.REGULARPAYMENT,null,null);
        regularTransakcije.addAll(interactor.get(Type.REGULARINCOME,null,null));*/
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(date);

        for (Transaction t : interactor.get()) {
            if (t.getType().equals(Type.REGULARPAYMENT) || t.getType().equals(Type.REGULARINCOME)){
                Transaction nova=t.clone();
                do {
                    if(nova.getDate().compareTo(nova.getEndDate())>=0 || nova.getDate().compareTo(calendar)>=0) break;
                    Calendar c= (Calendar) nova.getDate().clone();
                    c.add(Calendar.DAY_OF_MONTH,nova.getTransactionInterval());
                    nova.setDate(c);
                    if(provjeriDaLiJeDatumUOpsegu(nova,t) && provjeriJednakostDatuma(date,nova.getDate().getTime())) transakcije.add(t);
                }while (provjeriDaLiJeDatumUOpsegu(nova,t));
            }
        }
    }

    private boolean provjeriJednakostDatuma(Date date1, Date date2){
        Calendar calendar1= Calendar.getInstance();
        Calendar calendar2= Calendar.getInstance();
        postaviDatum(calendar1,date1);
        postaviDatum(calendar2,date2);
        return calendar1.compareTo(calendar2)==0;
    }

    private boolean provjeriDaLiJeDatumUOpsegu(Transaction nova, Transaction t) {
        Calendar calendar1= Calendar.getInstance();
        Calendar calendar2= Calendar.getInstance();
        Calendar calendar3= Calendar.getInstance();

        postaviDatum(calendar1, t.getDate().getTime());
        postaviDatum(calendar2, nova.getDate().getTime());
        postaviDatum(calendar3, t.getEndDate().getTime());

        return calendar2.compareTo(calendar1)>=0 && calendar2.compareTo(calendar3)<=0;
    }

    private void postaviDatum(Calendar c1, Date date) {
        c1.setTime(date);
        c1.set(Calendar.HOUR_OF_DAY, 0);
        c1.set(Calendar.MINUTE, 0);
        c1.set(Calendar.SECOND, 0);
        c1.set(Calendar.MILLISECOND, 0);
        c1.set(Calendar.DAY_OF_MONTH, 1);
    }


    private ArrayList<Transaction> filtrirajPoTipu(ArrayList<Transaction> transakcije, Type type) {
        if(type.equals(Type.ALL)) return transakcije; //ne treba ih filtrirati po tipu
        ArrayList<Transaction> novaLista=new ArrayList<>();
        for (Transaction t : transakcije) {
            if (t.getType().equals(type)) novaLista.add(t);
        }
        return novaLista;
    }

    private ArrayList<Transaction> filtrirajPoDatumu(ArrayList<Transaction> transactions, Date date) {
        ArrayList<Transaction> novaLista=new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int godina=calendar.get(Calendar.YEAR);
        int mjesec=calendar.get(Calendar.MONTH);
        for (Transaction t : transactions) {
            Calendar c=Calendar.getInstance();
            c.setTime(t.getDate().getTime());
            int g=c.get(Calendar.YEAR);
            int m=c.get(Calendar.MONTH);
            if (godina==g && mjesec==m) novaLista.add(t);
        }
        return novaLista;
    }


    @Override
    public void sortirajListu(String selectedItem) {
        trenutniNacinSortiranja=selectedItem;
        if(selectedItem.equals("Price - Ascending")){
            Collections.sort(transakcije, new Comparator<Transaction>() {
                public int compare(Transaction o1, Transaction o2) {
                    return Double.compare(o1.getAmount(),o2.getAmount());
                }
            });
        }
        else if(selectedItem.equals("Price - Descending")){
            Collections.sort(transakcije, new Comparator<Transaction>() {
                public int compare(Transaction o1, Transaction o2) {
                    return Double.compare(o2.getAmount(),o1.getAmount());
                }
            });
        }
        else if(selectedItem.equals("Title - Ascending")){
            Collections.sort(transakcije, new Comparator<Transaction>() {
                public int compare(Transaction o1, Transaction o2) {
                    if (o1.getTitle() == null || o2.getTitle() == null)
                        return 0;
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            });
        }
        else if(selectedItem.equals("Title - Descending")){
            Collections.sort(transakcije, new Comparator<Transaction>() {
                public int compare(Transaction o1, Transaction o2) {
                    if (o1.getTitle() == null || o2.getTitle() == null)
                        return 0;
                    return o2.getTitle().compareTo(o1.getTitle());
                }
            });
        }
        else if(selectedItem.equals("Date - Ascending")){
            Collections.sort(transakcije, new Comparator<Transaction>() {
                public int compare(Transaction o1, Transaction o2) {
                    if (o1.getDate() == null || o2.getDate() == null)
                        return 0;
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
        }
        else if(selectedItem.equals("Date - Descending")){
            Collections.sort(transakcije, new Comparator<Transaction>() {
                public int compare(Transaction o1, Transaction o2) {
                    if (o1.getDate() == null || o2.getDate() == null)
                        return 0;
                    return o2.getDate().compareTo(o1.getDate());
                }
            });
        }
        else trenutniNacinSortiranja=null;

        view.setTransactions(transakcije);
        view.notifyTransactionListDataSetChanged();
    }

    public Account getAccount() {
        return account;
    }

    @Override
    public void refreshAccount(Account a) {
        account=a;
        //zbog budgetChanged fragmenta moram promijenit u viewModelu
        viewModel.getAccount().setValue(account);

        view.changeGlobalAmount(account.getBudget());
        view.changeLimit(account.getTotalLimit());
    }

    //Svaka promjena accouna ide preko prezentera, a on je povezan preko interactora na web
    public void setAccount(Account account) {
        this.account = account;
        //ažuriranje accounta na webu
        interactor.editAccount(account);
        view.changeGlobalAmount(account.getBudget());
        view.changeLimit(account.getTotalLimit());
    }

    @Override
    public void insertTransaction(Transaction transaction) {
        interactor.insert(transaction);
    }

    @Override
    public void deleteTransaction(Transaction transaction) {
        interactor.delete(transaction);
    }

    @Override
    public void editTransaction(Transaction stara, Transaction nova) {
        interactor.edit(stara,nova);
    }
}

