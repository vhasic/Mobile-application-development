package ba.unsa.etf.rma.spirala1.detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Calendar;

import ba.unsa.etf.rma.spirala1.R;


public class GraphsFragment extends Fragment {
    private BarChart grafikZarade;
    private BarChart grafikPotrosnje;
    private BarChart grafikUkupnogStanja;
    private Spinner spinner;
    private ArrayAdapter<String> adapter;

    private String izbor;
    private int field;


    private IGraphsPresenter graphsPresenter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_graphs, container, false);
        graphsPresenter=new GraphsPresenter(this, getActivity());

        grafikZarade = view.findViewById(R.id.grafikZarde);
        grafikPotrosnje = view.findViewById(R.id.grafikPotrosnje);
        grafikUkupnogStanja = view.findViewById(R.id.grafikUkupnogStanja);


        podesiSpiner(view);
        izbor= (String) spinner.getSelectedItem();
        getDateConstant();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                izbor= (String) spinner.getSelectedItem();
                getDateConstant();

                nacrtajZaradu();
                nacrtajPotrosnju();
                nacrtajUkupnoStanje();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                System.out.println("nista nije izabrano");
            }

        });
        //ako se ovo zakomentarise onda je potreban trenutak dok se iscrta
        //da mi nebi dva puta iscrtavalo ovo je zakomentarisano
        /*nacrtajZaradu();
        nacrtajPotrosnju();
        nacrtajUkupnoStanje();*/

        return view;
    }

    private void nacrtajUkupnoStanje() {
        ArrayList<BarEntry> ukupnoStanje=graphsPresenter.dajListuUkupnogStanja(field);
        BarDataSet bardatasetUkupnoStanje = new BarDataSet(ukupnoStanje, "Ukupno stanje");
        bardatasetUkupnoStanje.setDrawValues(false);
        BarData dataUkupno = new BarData(bardatasetUkupnoStanje);
        postaviVrijednostiOsa(grafikUkupnogStanja);
        grafikUkupnogStanja.setData(dataUkupno);
        grafikUkupnogStanja.notifyDataSetChanged(); // let the chart know it's data changed
        grafikUkupnogStanja.invalidate(); // refresh
    }

    private void postaviVrijednostiOsa(BarChart grafik) {
        YAxis y=grafik.getAxisLeft();
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
//        y.setDrawGridLines(false);
        grafik.getAxisRight().setEnabled(false);
        XAxis x=grafik.getXAxis();
        x.setValueFormatter(new MyAxisValueFormatter());
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        if(field==Calendar.MONTH){
            x.setAxisMinimum(0);
            x.setAxisMaximum(12);
            x.setLabelCount(12);
        }
        else if(field==Calendar.DAY_OF_MONTH){
            x.setAxisMinimum(0);
            x.setAxisMaximum(32);
        }
        else if(field==Calendar.WEEK_OF_MONTH){
            x.setAxisMinimum(0);
            x.setAxisMaximum(5);
            x.setLabelCount(5);
        }
        else {
            x.setAxisMinimum(Calendar.getInstance().get(field));
            x.setAxisMaximum(Calendar.getInstance().get(field));
        }
        grafik.getDescription().setEnabled(false);
    }

    private void nacrtajPotrosnju() {
        ArrayList<BarEntry> potrosnja=graphsPresenter.dajListuPotrosnje(field);
        BarDataSet bardatasetPotrosnja = new BarDataSet(potrosnja, "Potrošnja");
        bardatasetPotrosnja.setDrawValues(false);
        BarData dataPotrosnja = new BarData(bardatasetPotrosnja);
        postaviVrijednostiOsa(grafikPotrosnje);
        grafikPotrosnje.setData(dataPotrosnja);
        grafikPotrosnje.notifyDataSetChanged(); // let the chart know it's data changed
        grafikPotrosnje.invalidate(); // refresh
    }

    private void nacrtajZaradu() {
        ArrayList<BarEntry> zarada=graphsPresenter.dajListuZarada(field);
        BarDataSet bardataset = new BarDataSet(zarada, "Zarada");
        bardataset.setDrawValues(false);
        BarData data = new BarData(bardataset);
        postaviVrijednostiOsa(grafikZarade);
        grafikZarade.setData(data);
        grafikZarade.notifyDataSetChanged(); // let the chart know it's data changed
        grafikZarade.invalidate(); // refresh
    }

    public void refreshGraphs(){
        nacrtajZaradu();
        nacrtajPotrosnju();
        nacrtajUkupnoStanje();
    }


    private void getDateConstant() {
        if(izbor.contains("dan")) field = Calendar.DAY_OF_MONTH;
        else if(izbor.contains("mjesec")) field =Calendar.MONTH;
        else if(izbor.contains("sedmica")) field=Calendar.WEEK_OF_MONTH;
        else field =Calendar.YEAR;
    }

    private void podesiSpiner(View view) {
        spinner =view.findViewById(R.id.vremenskeJedinice);
        String[] nizVrijednosti = new String[] {"Prikaži po danima","Prikaži po sedmicama","Prikaži po mjesecima","Prikaži po godinama"};
        adapter =new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,nizVrijednosti);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(2);
    }


}
