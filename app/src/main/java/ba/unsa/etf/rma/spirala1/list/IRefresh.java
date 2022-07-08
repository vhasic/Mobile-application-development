package ba.unsa.etf.rma.spirala1.list;

import ba.unsa.etf.rma.spirala1.data.Account;

public interface IRefresh {
    void refreshTransactions();
    void refreshAccount(Account a);
}
