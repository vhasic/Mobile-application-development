package ba.unsa.etf.rma.spirala1.data;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


public class SharedViewModel extends ViewModel {
    private MutableLiveData<Transaction> oldTransaction=new MutableLiveData<>();
    private MutableLiveData<Transaction> newTransaction=new MutableLiveData<>();
    private MutableLiveData<Account> account=new MutableLiveData<>();
    private MutableLiveData<String> action=new MutableLiveData<>();

    /*{
        account.setValue(new Account(3000,2000,700));
    }*/

    private static SharedViewModel instance;
    private SharedViewModel() {
    }

    public static SharedViewModel getInstance() {
        if (instance==null) instance=new SharedViewModel();
        return instance;
    }

    public MutableLiveData<Transaction> getOldTransaction() {
        return oldTransaction;
    }

    public void setOldTransaction(MutableLiveData<Transaction> oldTransaction) {
        this.oldTransaction = oldTransaction;
    }

    public MutableLiveData<Transaction> getNewTransaction() {
        return newTransaction;
    }

    public void setNewTransaction(MutableLiveData<Transaction> newTransaction) {
        this.newTransaction = newTransaction;
    }

    public MutableLiveData<Account> getAccount() {
        return account;
    }

    public void setAccount(MutableLiveData<Account> account) {
        this.account = account;
    }

    public MutableLiveData<String> getAction() {
        return action;
    }

    public void setAction(MutableLiveData<String> action) {
        this.action = action;
    }
}
