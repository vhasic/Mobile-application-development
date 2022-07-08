package ba.unsa.etf.rma.spirala1.list;

import java.util.ArrayList;
import java.util.Date;

import ba.unsa.etf.rma.spirala1.data.Account;
import ba.unsa.etf.rma.spirala1.data.Transaction;
import ba.unsa.etf.rma.spirala1.data.Type;

public interface ITransactionInteractor {
    ArrayList<Transaction> get();
    ArrayList<Transaction> get(Type type, String sort, Date date);
    void insert(Transaction t);
    void delete(Transaction t);
    void edit(Transaction stara, Transaction nova);
    Account getAccount();
    void editAccount(Account account);
    //
    //ArrayList<Transaction> getTransactionsFromDatabase();
}
