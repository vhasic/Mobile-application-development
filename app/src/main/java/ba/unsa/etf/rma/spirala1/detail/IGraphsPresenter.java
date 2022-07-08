package ba.unsa.etf.rma.spirala1.detail;

import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;

import ba.unsa.etf.rma.spirala1.data.Account;
import ba.unsa.etf.rma.spirala1.data.Transaction;
import ba.unsa.etf.rma.spirala1.list.IRefresh;

public interface IGraphsPresenter extends IRefresh {
    ArrayList<Transaction> getTransactions();
    ArrayList<BarEntry> dajListuUkupnogStanja(int poljeDatuma);
    ArrayList<BarEntry> dajListuZarada(int poljeDatuma);
    ArrayList<BarEntry> dajListuPotrosnje(int poljeDatuma);
    void refreshTransactions();
    void refreshAccount(Account a);
}
