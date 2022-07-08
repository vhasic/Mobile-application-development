package ba.unsa.etf.rma.spirala1.detail;

import android.content.Context;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;

import ba.unsa.etf.rma.spirala1.data.Account;
import ba.unsa.etf.rma.spirala1.data.Transaction;
import ba.unsa.etf.rma.spirala1.data.Type;
import ba.unsa.etf.rma.spirala1.list.ITransactionInteractor;
import ba.unsa.etf.rma.spirala1.list.TransactionInteractor;

public class GraphsPresenter implements IGraphsPresenter {
    private GraphsFragment view;
    private ITransactionInteractor interactor;
    private Context context;

    private ArrayList<Transaction> transactions;
    ArrayList<Transaction> zarade;
    ArrayList<Transaction> potrosnje;

    public GraphsPresenter(GraphsFragment view, Context context) {
        this.view       = view;
        this.interactor = new TransactionInteractor(context,this);
        this.context    = context;
        transactions=interactor.get();

        /*//test
        transactions=new ArrayList<Transaction>();
        transactions.add(new Transaction(new GregorianCalendar(2020, 4, 15),100.00,"Transakcija 1", Type.INDIVIDUALPAYMENT,
                null,0,null));
        transactions.add(new Transaction(new GregorianCalendar(2020, 1, 1),500.00,"Transakcija 2", Type.REGULARINCOME,
                null,31,new GregorianCalendar(2020, 6, 30)));
        transactions.add(new Transaction(new GregorianCalendar(2020, 4, 16),200,"Transakcija 3", Type.INDIVIDUALPAYMENT,
                null,0,null));
        transactions.add(new Transaction(new GregorianCalendar(2020, 2, 17),30,"Transakcija 4", Type.REGULARPAYMENT,
                null,60,new GregorianCalendar(2020, 6, 30)));*/
    }
    @Override
    public ArrayList<Transaction> getTransactions() {
        return transactions;
    }

    //Napomena: svuda sam za mjesece dodao +1 pri prikazu, jer se u biblioteci koristi notacija mjeseci od 0
    @Override
    public ArrayList<BarEntry> dajListuUkupnogStanja(final int poljeDatuma) {
        ArrayList<BarEntry> lista=new ArrayList<>();

        LinkedHashMap<Integer,Double> ukupno=dajUkupnoStanje(poljeDatuma);
        float suma=0;
        for (Map.Entry<Integer, Double> entry : ukupno.entrySet()) {
            suma+=entry.getValue().floatValue();
            int kljuc=entry.getKey();
            if(poljeDatuma==Calendar.MONTH) kljuc+=1;
            lista.add(new BarEntry(kljuc, suma));
        }
        return lista;
    }

    private LinkedHashMap<Integer, Double> dajUkupnoStanje(int poljeDatuma) {
        LinkedHashMap<Integer,Double> map=new LinkedHashMap<>();

        for(Transaction z:zarade){
            Double prolsaVrijednost=map.get(z.getDate().get(poljeDatuma));
            if(prolsaVrijednost==null) prolsaVrijednost=0.0;
            map.put(z.getDate().get(poljeDatuma),prolsaVrijednost+z.getAmount());
        }
        for(Transaction p:potrosnje){
            Double prolsaVrijednost=map.get(p.getDate().get(poljeDatuma));
            if(prolsaVrijednost==null) prolsaVrijednost=0.0;
            map.put(p.getDate().get(poljeDatuma),prolsaVrijednost-p.getAmount());
        }
        return map;
    }


    @Override
    public ArrayList<BarEntry> dajListuZarada(int poljeDatuma) {
        ArrayList<BarEntry> lista=new ArrayList<>();
        zarade=dajZarade(poljeDatuma);

        LinkedHashMap<Integer,Double> ukupno= dajIzracunatoStanje(poljeDatuma,zarade);
        for (Map.Entry<Integer, Double> entry : ukupno.entrySet()) {
            int kljuc=entry.getKey();
            if(poljeDatuma==Calendar.MONTH) kljuc+=1;
            lista.add(new BarEntry(kljuc, entry.getValue().floatValue()));
        }
        return lista;
    }

    private ArrayList<Transaction> dajZarade(int poljeDatuma) {
        ArrayList<Transaction> list=new ArrayList<>();
        for(Transaction t:transactions){
            if(t.getType().equals(Type.INDIVIDUALINCOME)) list.add(t);
            else if(t.getType().equals(Type.REGULARINCOME)){
                //pokupiti sve za regular transakcije
                int trenutnaGodina=Calendar.getInstance().get(Calendar.YEAR);
                Transaction nova=t.clone();
                while (provjeriDaLiJeDatumUOpsegu(nova.getDate().getTime(),t)){
                    //podaci se prikazuju samo za trenutnu godinu
                    if(nova.getDate().get(Calendar.YEAR)>trenutnaGodina) break;
                    else if(poljeDatuma==Calendar.YEAR || nova.getDate().get(Calendar.YEAR)==trenutnaGodina) {
                        list.add(nova);
                    }
                    Calendar c= (Calendar) nova.getDate().clone();
                    nova=nova.clone();
                    c.add(Calendar.DAY_OF_MONTH,nova.getTransactionInterval());
                    nova.setDate(c);
                }
            }
        }

        return list;
    }

    @Override
    public ArrayList<BarEntry> dajListuPotrosnje(int poljeDatuma) {
        ArrayList<BarEntry> lista=new ArrayList<>();
        potrosnje=dajPotrosnje(poljeDatuma);

        LinkedHashMap<Integer,Double> ukupno=dajIzracunatoStanje(poljeDatuma,potrosnje);
        for (Map.Entry<Integer, Double> entry : ukupno.entrySet()) {
            int kljuc=entry.getKey();
            if(poljeDatuma==Calendar.MONTH) kljuc+=1;
            lista.add(new BarEntry(kljuc, entry.getValue().floatValue()));
        }

        return lista;
    }

    private ArrayList<Transaction> dajPotrosnje(int poljeDatuma) {
        ArrayList<Transaction> list=new ArrayList<>();
        for(Transaction t:transactions){
            if(t.getType().equals(Type.INDIVIDUALPAYMENT) || t.getType().equals(Type.PURCHASE)) {
                if(t.getAmount()<0) t.setAmount(t.getAmount()*(-1));
                list.add(t);
            }
            else if(t.getType().equals(Type.REGULARPAYMENT)){
                //pokupiti sve za regular transakcije
                int trenutnaGodina=Calendar.getInstance().get(Calendar.YEAR);
                //posto sam ja zamislio da se transakcije koje su potrosnja pisu negativne, pa sam tako i radio one stvari vezane za prelazak limita,
                // a tek u drugoj spirali sam vidio da se na grafiku ocekuju sve pozitivne vrijednosti,
                // dodat cu samo da se negativne vrijednosti na graficima prikazuju kao pozitivne
                Transaction nova=t.clone();
                while (provjeriDaLiJeDatumUOpsegu(nova.getDate().getTime(),t)){
                    //podaci se prikazuju samo za trenutnu godinu
                    if(nova.getDate().get(Calendar.YEAR)>trenutnaGodina) break;
                    else if(poljeDatuma==Calendar.YEAR || nova.getDate().get(Calendar.YEAR)==trenutnaGodina) {
                        if (nova.getAmount() < 0) nova.setAmount(nova.getAmount() * (-1));
                        list.add(nova);
                    }
                    Calendar c= (Calendar) nova.getDate().clone();
                    nova=nova.clone();
                    c.add(Calendar.DAY_OF_MONTH,nova.getTransactionInterval());
                    nova.setDate(c);
                }
            }
        }
        return list;
    }

    private LinkedHashMap<Integer, Double> dajIzracunatoStanje(int poljeDatuma,ArrayList<Transaction> list) {
        LinkedHashMap<Integer,Double> map=new LinkedHashMap<>();

        for(Transaction z:list){
            Double prolsaVrijednost=map.get(z.getDate().get(poljeDatuma));
            if(prolsaVrijednost==null) prolsaVrijednost=0.0;
            map.put(z.getDate().get(poljeDatuma),prolsaVrijednost+z.getAmount());
        }
        return map;
    }


    private boolean provjeriDaLiJeDatumUOpsegu(Date date, Transaction t) {
        Calendar calendar1= Calendar.getInstance();
        Calendar calendar2= Calendar.getInstance();
        Calendar calendar3= Calendar.getInstance();

        calendar1.setTime(t.getDate().getTime());
        calendar1.set(Calendar.HOUR_OF_DAY, 0);
        calendar1.set(Calendar.MINUTE, 0);
        calendar1.set(Calendar.SECOND, 0);
        calendar1.set(Calendar.MILLISECOND, 0);

        calendar2.setTime(date);
        calendar2.set(Calendar.HOUR_OF_DAY, 0);
        calendar2.set(Calendar.MINUTE, 0);
        calendar2.set(Calendar.SECOND, 0);
        calendar2.set(Calendar.MILLISECOND, 0);

        calendar3.setTime(t.getEndDate().getTime());
        calendar3.set(Calendar.HOUR_OF_DAY, 0);
        calendar3.set(Calendar.MINUTE, 0);
        calendar3.set(Calendar.SECOND, 0);
        calendar3.set(Calendar.MILLISECOND, 0);

        return calendar2.compareTo(calendar1)>=0 && calendar2.compareTo(calendar3)<=0;
    }

    @Override
    public void refreshTransactions() {
        transactions=interactor.get();
        view.refreshGraphs();
    }

    @Override
    public void refreshAccount(Account a) {
    }
}
