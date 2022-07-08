package ba.unsa.etf.rma.spirala1.list;

import java.util.Date;

import ba.unsa.etf.rma.spirala1.data.Account;
import ba.unsa.etf.rma.spirala1.data.Type;
import ba.unsa.etf.rma.spirala1.data.Transaction;

public interface ITransactionPresenter extends IRefresh {
    void refreshTransactions();
    void refreshAccount(Account a);
    void sortirajListu(String selectedItem);
    void filtrirajListu(Type type, Date date);
    Account getAccount();
    void setAccount(Account account);
    void insertTransaction(Transaction transaction);
    void deleteTransaction(Transaction transaction);
    void editTransaction(Transaction stara, Transaction nova);
}
