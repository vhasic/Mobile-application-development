package ba.unsa.etf.rma.spirala1.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Datum {
    private Calendar date;
    private SimpleDateFormat format=new SimpleDateFormat("MMMM,yyyy", Locale.ENGLISH);

    public Datum() {
        date=new GregorianCalendar();
        date.setTime(Calendar.getInstance().getTime());
    }

    public Datum(Calendar c,SimpleDateFormat format){
        date=c;
        this.format=format;
    }

    public Datum(String s){
        try {
            format=new SimpleDateFormat("dd.MM.yyyy",Locale.ENGLISH);
            Date d = format.parse(s);
            date=new GregorianCalendar();
            date.setTime(d);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public Datum(Date d){
        date=new GregorianCalendar();
        date.setTime(d);
    }
    public Datum(Calendar c){
        date=c;
    }

    @Override
    public String toString() {
        String s=format.format(date.getTime());
        return s;
    }

    public void addMonth() {
        date.add(Calendar.MONTH, 1);
    }
    public void subMonth() {
        date.add(Calendar.MONTH, -1);
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public SimpleDateFormat getFormat() {
        return format;
    }

    public void setFormat(SimpleDateFormat format) {
        this.format = format;
    }
}
