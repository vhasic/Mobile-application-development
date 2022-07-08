package ba.unsa.etf.rma.spirala1.data;

import java.util.ArrayList;
import java.util.GregorianCalendar;

public class TransactionModel {
    public static ArrayList<Transaction> transactions = new ArrayList<Transaction>() {
        {
            //u javi su svi mjeseci manji za 1, tako je Januar 0,Februar 1...
            add(new Transaction(new GregorianCalendar(2020, 1, 10),100,"Transakcija 1", Type.INDIVIDUALINCOME,
                    null,0,null));
            add(new Transaction(new GregorianCalendar(2020, 1, 15),-150,"Transakcija 2", Type.REGULARPAYMENT,
                    "opis kupljenog proizvoda",30,new GregorianCalendar(2020, 11, 15)));
            add(new Transaction(new GregorianCalendar(2020, 1, 3),-50,"Transakcija 3", Type.PURCHASE,
                    "opis kupljenog proizvoda",0,null));
            add(new Transaction(new GregorianCalendar(2020, 4, 16),50,"Transakcija 7", Type.INDIVIDUALINCOME,
                    null,0,null));
            //plus onaj regular income i regular payment se ponistavaju, pa ne trebam brinuti o njima

            add(new Transaction(new GregorianCalendar(2020, 2, 20),-100,"Transakcija 4", Type.INDIVIDUALPAYMENT,
                    "opis kupljenog proizvoda",0,null));
            add(new Transaction(new GregorianCalendar(2020, 2, 1),150,"Transakcija 5", Type.REGULARINCOME,
                    null,30,new GregorianCalendar(2030, 1, 10)));
            add(new Transaction(new GregorianCalendar(2020, 2, 10),100,"Transakcija 6", Type.INDIVIDUALINCOME,
                    null,0,null));




            add(new Transaction(new GregorianCalendar(2020, 4, 2),200,"Transakcija 8", Type.INDIVIDUALINCOME,
                    null,0,null));
            add(new Transaction(new GregorianCalendar(2020, 4, 3),-200,"Transakcija 9", Type.PURCHASE,
                    "opis kupljenog proizvoda",0,null));

            add(new Transaction(new GregorianCalendar(2020, 5, 3),-400,"Transakcija 10", Type.PURCHASE,
                    "opis kupljenog proizvoda",0,null));
            add(new Transaction(new GregorianCalendar(2020, 5, 9),-200,"Transakcija 11", Type.PURCHASE,
                    "opis kupljenog proizvoda",0,null));
            add(new Transaction(new GregorianCalendar(2020, 5, 20),-100,"Transakcija 12", Type.PURCHASE,
                    "opis kupljenog proizvoda",0,null));
            add(new Transaction(new GregorianCalendar(2020, 5, 10),700,"Transakcija 13", Type.INDIVIDUALINCOME,
                    null,0,null));


            add(new Transaction(new GregorianCalendar(2020, 6, 15),300,"Transakcija 14", Type.INDIVIDUALINCOME,
                    null,0,null));
            add(new Transaction(new GregorianCalendar(2020, 6, 20),100,"Transakcija 15", Type.INDIVIDUALINCOME,
                    null,0,null));
            add(new Transaction(new GregorianCalendar(2020, 6, 20),-100,"Transakcija 16", Type.INDIVIDUALPAYMENT,
                    "opis kupljenog proizvoda",0,null));
            add(new Transaction(new GregorianCalendar(2020, 6, 20),-200,"Transakcija 17", Type.INDIVIDUALPAYMENT,
                    "opis kupljenog proizvoda",0,null));
            add(new Transaction(new GregorianCalendar(2020, 6, 15),-100,"Transakcija 18", Type.INDIVIDUALPAYMENT,
                    "opis kupljenog proizvoda",0,null));

            add(new Transaction(new GregorianCalendar(2020, 7, 20),-150,"Transakcija 19", Type.INDIVIDUALPAYMENT,
                    "opis kupljenog proizvoda",0,null));
            add(new Transaction(new GregorianCalendar(2020, 7, 10),150,"Transakcija 20", Type.INDIVIDUALINCOME,
                    null,0,null));

        }
    };
}
