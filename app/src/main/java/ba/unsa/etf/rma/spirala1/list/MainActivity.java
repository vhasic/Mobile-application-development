package ba.unsa.etf.rma.spirala1.list;

import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import ba.unsa.etf.rma.spirala1.R;
import ba.unsa.etf.rma.spirala1.data.Account;
import ba.unsa.etf.rma.spirala1.data.SharedViewModel;
import ba.unsa.etf.rma.spirala1.data.Transaction;
import ba.unsa.etf.rma.spirala1.detail.BudgetFragment;
import ba.unsa.etf.rma.spirala1.detail.GraphsFragment;
import ba.unsa.etf.rma.spirala1.detail.TransactionDetailFragment;

public class MainActivity extends AppCompatActivity implements TransactionListFragment.OnItemClick ,TransactionDetailFragment.OnSaveClick ,
BudgetFragment.OnBudgetChanged{

    private boolean landscapeMode;
    private SharedViewModel viewModel=SharedViewModel.getInstance();
    FragmentManager fragmentManager = getSupportFragmentManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout details = findViewById(R.id.transaction_detail);
        if (details != null) {
            landscapeMode = true;
            TransactionDetailFragment transactionDetailFragment = (TransactionDetailFragment) fragmentManager.findFragmentById(R.id.transaction_detail);
            if (transactionDetailFragment ==null) {
                transactionDetailFragment = new TransactionDetailFragment();
                fragmentManager.beginTransaction().replace(R.id.transaction_detail, transactionDetailFragment).commit();
            }


        } else {
            landscapeMode = false;
        }
        Fragment listFragment =  fragmentManager.findFragmentById(R.id.transaction_list);
        if (listFragment==null){
            listFragment = new TransactionListFragment();
            fragmentManager.beginTransaction().replace(R.id.transaction_list,listFragment).commit();
        }
        else{
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        if(!landscapeMode) {
            ScrollView scrollView = findViewById(R.id.transaction_list);
            scrollView.setOnTouchListener(new OnSwipeTouchListener(this) {
                @Override
                public void onSwipeLeft() {
                    super.onSwipeLeft();
                    Fragment fragment =  fragmentManager.findFragmentById(R.id.transaction_list);
                    if(fragment instanceof TransactionListFragment) fragmentManager.beginTransaction().replace(R.id.transaction_list,new BudgetFragment()).addToBackStack(null).commit();
                    else if(fragment instanceof BudgetFragment) fragmentManager.beginTransaction().replace(R.id.transaction_list,new GraphsFragment()).addToBackStack(null).commit();
                    else if(fragment instanceof GraphsFragment) {
                        onBackPressed();
                        try {
                            TransactionListFragment transactionListFragment;
                            transactionListFragment = (TransactionListFragment) fragmentManager.findFragmentById(R.id.transaction_list);
                        }
                        catch (Exception e){
                            //ako prethodni fragment nije transactionListFragment onda idi jos jedan korak nazad
                            onBackPressed();
                        }
                    }
                }
                @Override
                public void onSwipeRight() {
                    super.onSwipeRight();
                    Fragment fragment =  fragmentManager.findFragmentById(R.id.transaction_list);
                    if(fragment instanceof TransactionListFragment) fragmentManager.beginTransaction().replace(R.id.transaction_list,new GraphsFragment()).addToBackStack(null).commit();
                    if(fragment instanceof BudgetFragment) onBackPressed();
                    else if(fragment instanceof GraphsFragment) {
                        onBackPressed();
                    }
                }
            });
        }

    }



    @Override
    public void onItemClicked(Transaction transaction, String action, Account account) {
        Bundle arguments = new Bundle();
        arguments.putSerializable("Transaction",transaction);
        arguments.putString("action",action);
        arguments.putSerializable("account",account);


        TransactionDetailFragment detailFragment = new TransactionDetailFragment();
        detailFragment.setArguments(arguments);
        if (landscapeMode){
            getSupportFragmentManager().beginTransaction().replace(R.id.transaction_detail, detailFragment).commit();
        }
        else{
            getSupportFragmentManager().beginTransaction().replace(R.id.transaction_list,detailFragment).addToBackStack(null).commit();
        }
    }

    @Override
    public void onSaveClicked(String action, Transaction oldTransaction, Transaction newTransaction, Account account) {
        TransactionListFragment transactionListFragment;
        transactionListFragment = getTransactionListFragment();

        if(action.equals("delete")){
            transactionListFragment.deleteTransaction(oldTransaction);
        }
        else if(action.equals("edit")){
            transactionListFragment.editTransaction(oldTransaction,newTransaction);
        }
        else if(action.equals("add")){
            transactionListFragment.insertTransaction(newTransaction);
        }


        if (landscapeMode){
            //stavi novi prazni detail fragment ako je landscape
            fragmentManager.beginTransaction().replace(R.id.transaction_detail, new TransactionDetailFragment()).commit();
        }
        else{
            //stavi list fragment ako je u portrait
            fragmentManager.beginTransaction().replace(R.id.transaction_list,transactionListFragment).addToBackStack(null).commit();
        }

        //prilikom promjene transakcija mijenja se i account
        onBudgetChanged(account);
    }

    private TransactionListFragment getTransactionListFragment() {
        TransactionListFragment transactionListFragment;
        if (landscapeMode) {
            transactionListFragment = (TransactionListFragment) fragmentManager.findFragmentById(R.id.transaction_list);
        } else {
            onBackPressed();
            transactionListFragment = (TransactionListFragment) fragmentManager.findFragmentById(R.id.transaction_list);
        }
        return transactionListFragment;
    }

    @Override
    public void onBudgetChanged(Account account) {
        viewModel.getAccount().setValue(account);

        TransactionListFragment transactionListFragment = getTransactionListFragment();
        transactionListFragment.getPresenter().setAccount(viewModel.getAccount().getValue());
    }
}
