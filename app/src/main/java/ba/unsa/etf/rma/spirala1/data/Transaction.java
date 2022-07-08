package ba.unsa.etf.rma.spirala1.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class Transaction implements Serializable, Cloneable {
    //dodano
    private int databaseID;
    private int id;
    //
    private Calendar date; //u formatu "dd.MM.yyyy"
    private Calendar endDate;
    private double amount=0;
    private String title="", itemDescription="";
    private Type type;
    private int transactionInterval=0;


    public Transaction() {
        this.date = new GregorianCalendar();
        this.endDate = new GregorianCalendar();
    }


    public Transaction(Calendar date, double amount, String title, Type type, String itemDescription, int transactionInterval, Calendar endDate) {
        this.date = date;
        this.endDate = endDate;
        this.amount = amount;
        this.title = title;
        this.itemDescription = itemDescription;
        this.type = type;
        this.transactionInterval = transactionInterval;
    }

    public Transaction(int databaseID, int id, Calendar date, double amount, String title, Type type, String itemDescription, int transactionInterval, Calendar endDate) {
        this.databaseID=databaseID;
        this.id=id;
        this.date = date;
        this.endDate = endDate;
        this.amount = amount;
        this.title = title;
        this.itemDescription = itemDescription;
        this.type = type;
        this.transactionInterval = transactionInterval;
    }


    //provjere
    public boolean provjeriNetacnost(){
        return !checkTitle() || !checkItemDescription() || !checkTransactionInterval() || !checkEndDate() || date == null || !isDateBeforeEndDate();
    }

    private boolean isDateBeforeEndDate(){
        if(date!=null && endDate!=null) return date.before(endDate);
        return true;
    }

    private boolean checkTitle(){
        if(title==null) return false;
        return title.length()>3 && title.length()<15;
    }
    private boolean checkItemDescription(){
        if(type==null) return false;
        if(type.equals(Type.REGULARINCOME) || type.equals(Type.INDIVIDUALINCOME)){
            return itemDescription == null || itemDescription.equals("");
        }
        return true;
    }
    private boolean checkTransactionInterval(){
        if(type==null) return false;
        if(type.equals(Type.REGULARINCOME) || type.equals(Type.REGULARPAYMENT)){
            return transactionInterval != 0;
        }
        return true;
    }
    private boolean checkEndDate(){
        if(type==null) return false;
        if(type.equals(Type.REGULARINCOME) || type.equals(Type.REGULARPAYMENT)){
            return endDate != null;
        }
        return true;
    }



    //getters and setters
    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getTransactionInterval() {
        return transactionInterval;
    }

    public void setTransactionInterval(int transactionInterval) {
        this.transactionInterval = transactionInterval;
    }

    //dodano

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDatabaseID() {
        return databaseID;
    }

    public void setDatabaseID(int databaseID) {
        this.databaseID = databaseID;
    }

    //

    @Override
    public boolean equals(@Nullable Object o) {
        Transaction t= (Transaction) o;
        boolean pomocna;
        pomocna=title.equals(t.title);
        pomocna=pomocna && amount==t.amount;
        pomocna=pomocna && date.equals(t.date);
        if(endDate==null && t.endDate!=null || endDate!=null && t.endDate==null) return false;
        if(endDate!=null && t.endDate!=null)pomocna=pomocna && endDate.equals(t.endDate);
        pomocna=pomocna && transactionInterval==t.transactionInterval;
        pomocna=pomocna && type.equals(t.type);
        if(itemDescription==null && t.itemDescription!=null || itemDescription!=null && t.itemDescription==null) return false;
        if(itemDescription!=null && t.itemDescription!=null) pomocna=pomocna && itemDescription.equals(t.itemDescription);
        return pomocna;
    }

    @NonNull
    @Override
    public Transaction clone()  {
        Transaction t = null;
        try {
            t = (Transaction) super.clone();
        } catch (CloneNotSupportedException ignored) {
            t=new Transaction();
        }
        t.date = (Calendar) this.date.clone();
        return t;
    }
}
