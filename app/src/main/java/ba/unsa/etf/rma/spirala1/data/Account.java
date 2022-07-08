package ba.unsa.etf.rma.spirala1.data;

import java.io.Serializable;

public class Account implements Serializable {
    private int databaseID;
    private double budget, totalLimit, monthLimit;

    private double trenutnoStanjePotrosnjeUkupno =0;
    private double[] trenutnoStanjePotrosnjeUMjesecu=new double[12];

    public Account() {
    }

    public Account(double budget, double totalLimit, double monthLimit) {
        this.budget = budget;
        this.totalLimit = totalLimit;
        this.monthLimit = monthLimit;
    }

    public Account(int databaseID, double budget, double totalLimit, double monthLimit) {
        this.databaseID = databaseID;
        this.budget = budget;
        this.totalLimit = totalLimit;
        this.monthLimit = monthLimit;
    }

    //metode
    public void promijeniStanje(int mjesec,double vrijednost){
        budget+=vrijednost;
        if(vrijednost<0) {//dobitak ne racunam, trazi se samo potrosnja
            vrijednost *= -1;
            trenutnoStanjePotrosnjeUMjesecu[mjesec] += vrijednost;
            trenutnoStanjePotrosnjeUkupno += vrijednost;
        }
    }
    public void promijeniStanje(double vrijednost) {
        budget += vrijednost;
    }
    public void promijeniMjesecnoStanje(int mjesec, double vrijednost){
        if(vrijednost<0) {//dobitak ne racunam, trazi se samo potrosnja
            vrijednost *= -1;
            trenutnoStanjePotrosnjeUMjesecu[mjesec] += vrijednost;
        }
    }
    public void promijeniGlobalnoStanje(double vrijednost){
        budget+=vrijednost;
        if(vrijednost<0) {//dobitak ne racunam, trazi se samo potrosnja
            vrijednost *= -1;
            trenutnoStanjePotrosnjeUkupno+= vrijednost;
        }
    }

    //vraca true ako je doslo do prekoracenja
    public boolean provjeriStanje(int mjesec,double vrijednost){
        //zbog potrosnje treba uzeti samo negativnu vrijednost, koju negiranu treba dodati
        if(vrijednost>0) return false; //dobitak ne racunam, trazi se samo potrosnja
        vrijednost*=-1;
        return trenutnoStanjePotrosnjeUMjesecu[mjesec] +vrijednost>monthLimit || trenutnoStanjePotrosnjeUkupno+vrijednost>totalLimit;
    }


    public void resetujMjesecnoStanje(int mjesec){
        trenutnoStanjePotrosnjeUMjesecu[mjesec]=0;
    }


    //geteri i seteri
    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public double getTotalLimit() {
        return totalLimit;
    }

    public void setTotalLimit(double totalLimit) {
        this.totalLimit = totalLimit;
    }

    public double getMonthLimit() {
        return monthLimit;
    }

    public void setMonthLimit(double monthLimit) {
        this.monthLimit = monthLimit;
    }


    public int getDatabaseID() {
        return databaseID;
    }

    public void setDatabaseID(int databaseID) {
        this.databaseID = databaseID;
    }
}
