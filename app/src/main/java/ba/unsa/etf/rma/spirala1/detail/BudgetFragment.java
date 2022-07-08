package ba.unsa.etf.rma.spirala1.detail;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import ba.unsa.etf.rma.spirala1.R;
import ba.unsa.etf.rma.spirala1.data.Account;
import ba.unsa.etf.rma.spirala1.data.SharedViewModel;
import ba.unsa.etf.rma.spirala1.list.OnSwipeTouchListener;

public class BudgetFragment extends Fragment {
    private EditText budget;
    private EditText totalLimit;
    private EditText monthLimit;
    private Button save;

    private Account account;

    private OnBudgetChanged onItemClick;

    public BudgetFragment() {
        this.account=SharedViewModel.getInstance().getAccount().getValue();
    }

    public interface OnBudgetChanged {
        public void onBudgetChanged(Account account);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView= inflater.inflate(R.layout.fragment_budget, container, false);

        budget=fragmentView.findViewById(R.id.budget);
        //budzet se ne moze mijenjati
        budget.setEnabled(false);
        totalLimit=fragmentView.findViewById(R.id.totalLimit);
        monthLimit=fragmentView.findViewById(R.id.monthLimit);
        save=fragmentView.findViewById(R.id.save);

        if(account!=null){
            budget.setText(String.valueOf(account.getBudget()));
            totalLimit.setText(String.valueOf(account.getTotalLimit()));
            monthLimit.setText(String.valueOf(account.getMonthLimit()));
        }

        onItemClick=(OnBudgetChanged) getActivity();

        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Account newAccount=new Account();
                try {
                    newAccount.setBudget(account.getBudget());
                    newAccount.setTotalLimit(Double.parseDouble(String.valueOf(totalLimit.getText())));
                    newAccount.setMonthLimit(Double.parseDouble(String.valueOf(monthLimit.getText())));
                }
                catch (NullPointerException | NumberFormatException e){
                    System.out.println("Pogresan unos");
                }
                onItemClick.onBudgetChanged(newAccount);
            }
        });

        return fragmentView;
    }
}
