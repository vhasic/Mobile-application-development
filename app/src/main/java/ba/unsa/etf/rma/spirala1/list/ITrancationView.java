package ba.unsa.etf.rma.spirala1.list;

import java.util.ArrayList;

import ba.unsa.etf.rma.spirala1.data.Transaction;

public interface ITrancationView {
    void setTransactions(ArrayList<Transaction> movies);
    void notifyTransactionListDataSetChanged();
    void changeGlobalAmount(double amount);
    void changeLimit(double limit);
    void postaviDatum();
    void insertTransaction(Transaction transaction);
    void deleteTransaction(Transaction transaction);
    void editTransaction(Transaction stara, Transaction nova);
}
