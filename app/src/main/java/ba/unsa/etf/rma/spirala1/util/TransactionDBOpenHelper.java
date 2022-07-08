package ba.unsa.etf.rma.spirala1.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TransactionDBOpenHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "SpiralaDataBase.db";
    public static final int DATABASE_VERSION = 1;


    public TransactionDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public TransactionDBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    public static final String TRANSACTIONS_TABLE = "transactions";
    public static final String TRANSACTION_ID = "id";   //TRANSACTION_ID je 0 ako se dodaje, inače ima id sa web servisa
    public static final String TRANSACTION_INTERNAL_ID = "internalId";
    public static final String TRANSACTION_TITLE = "title";
    public static final String ITEM_DESCRIPTION = "itemDescription";
    public static final String TRANSACTION_AMOUNT = "amount";
    public static final String TRANSACTION_DATE = "date";
    public static final String TRANSACTION_ENDDATE = "endDate";
    public static final String TRANSACTION_TYPE = "type";
    public static final String TRANSACTION_INTERVAL = "transactionInterval";
    private static final String TRANSACTION_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + TRANSACTIONS_TABLE + " ("  + TRANSACTION_INTERNAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TRANSACTION_ID + " INTEGER, "
                    + TRANSACTION_TITLE + " TEXT NOT NULL, "
                    + ITEM_DESCRIPTION + " TEXT, "
                    + TRANSACTION_AMOUNT + " REAL, "
                    + TRANSACTION_DATE + " TEXT, "
                    + TRANSACTION_ENDDATE + " TEXT, "
                    + TRANSACTION_TYPE + " TEXT, "
                    + TRANSACTION_INTERVAL + " INTEGER);";

    private static final String DROP_TRANSACTIONS_TABLE = "DROP TABLE IF EXISTS " + TRANSACTIONS_TABLE;

    public static final String DELETED_TRANSACTIONS_TABLE = "deletedTransactions";
    private static final String DELETED_TRANSACTION_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + DELETED_TRANSACTIONS_TABLE + " ("  + TRANSACTION_INTERNAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TRANSACTION_ID + " INTEGER, "
                    + TRANSACTION_TITLE + " TEXT NOT NULL, "
                    + ITEM_DESCRIPTION + " TEXT, "
                    + TRANSACTION_AMOUNT + " REAL, "
                    + TRANSACTION_DATE + " TEXT, "
                    + TRANSACTION_ENDDATE + " TEXT, "
                    + TRANSACTION_TYPE + " TEXT, "
                    + TRANSACTION_INTERVAL + " INTEGER);";

    private static final String DROP_DELETED_TRANSACTIONS_TABLE = "DROP TABLE IF EXISTS " + DELETED_TRANSACTIONS_TABLE;

    public static final String INSERTED_TRANSACTIONS_TABLE = "insertedTransactions";
    private static final String INSERTED_TRANSACTION_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + INSERTED_TRANSACTIONS_TABLE + " ("  + TRANSACTION_INTERNAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TRANSACTION_ID + " INTEGER, "
                    + TRANSACTION_TITLE + " TEXT NOT NULL, "
                    + ITEM_DESCRIPTION + " TEXT, "
                    + TRANSACTION_AMOUNT + " REAL, "
                    + TRANSACTION_DATE + " TEXT, "
                    + TRANSACTION_ENDDATE + " TEXT, "
                    + TRANSACTION_TYPE + " TEXT, "
                    + TRANSACTION_INTERVAL + " INTEGER);";

    private static final String DROP_INSERTED_TRANSACTIONS_TABLE = "DROP TABLE IF EXISTS " + INSERTED_TRANSACTIONS_TABLE;

    public static final String EDITED_TRANSACTIONS_TABLE = "editedTransactions";
    private static final String EDITED_TRANSACTION_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + EDITED_TRANSACTIONS_TABLE + " ("  + TRANSACTION_INTERNAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + TRANSACTION_ID + " INTEGER, "
                    + TRANSACTION_TITLE + " TEXT NOT NULL, "
                    + ITEM_DESCRIPTION + " TEXT, "
                    + TRANSACTION_AMOUNT + " REAL, "
                    + TRANSACTION_DATE + " TEXT, "
                    + TRANSACTION_ENDDATE + " TEXT, "
                    + TRANSACTION_TYPE + " TEXT, "
                    + TRANSACTION_INTERVAL + " INTEGER);";

    private static final String DROP_EDITED_TRANSACTIONS_TABLE = "DROP TABLE IF EXISTS " + EDITED_TRANSACTIONS_TABLE;


    public static final String ACCOUNT_TABLE = "accounts";
    //public static final String ACCOUNT_ID = "id";
    public static final String ACCOUNT_INTERNAL_ID = "internalId";
    public static final String BUDGET = "budget";
    public static final String TOTAL_lIMIT = "totalLimit";
    public static final String MONTH_lIMIT = "monthLimit";


    private static final String ACCOUNT_TABLE_CREATE =
            "CREATE TABLE IF NOT EXISTS " + ACCOUNT_TABLE + " ("  + ACCOUNT_INTERNAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    //+ ACCOUNT_ID + " TEXT UNIQUE, "
                    + BUDGET + " REAL, "
                    + TOTAL_lIMIT + " REAL, "
                    + MONTH_lIMIT + " REAL);";

    private static final String DROP_ACCOUNT_TABLE = "DROP TABLE IF EXISTS " + ACCOUNT_TABLE;


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TRANSACTION_TABLE_CREATE);
        db.execSQL(ACCOUNT_TABLE_CREATE);
        db.execSQL(INSERTED_TRANSACTION_TABLE_CREATE);
        db.execSQL(EDITED_TRANSACTION_TABLE_CREATE);
        db.execSQL(DELETED_TRANSACTION_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TRANSACTIONS_TABLE);
        db.execSQL(DROP_ACCOUNT_TABLE);
        db.execSQL(DROP_INSERTED_TRANSACTIONS_TABLE);
        db.execSQL(DROP_EDITED_TRANSACTIONS_TABLE);
        db.execSQL(DROP_DELETED_TRANSACTIONS_TABLE);
        onCreate(db);
    }

    public void clearTransactionsTable(SQLiteDatabase db){
        db.execSQL(DROP_TRANSACTIONS_TABLE);
        db.execSQL(TRANSACTION_TABLE_CREATE);
    }

    public void clearAccountsTable(SQLiteDatabase db){
        db.execSQL(DROP_ACCOUNT_TABLE);
        db.execSQL(ACCOUNT_TABLE_CREATE);
    }

    public void clearDeletedTransactionsTable(SQLiteDatabase db){
        db.execSQL(DROP_DELETED_TRANSACTIONS_TABLE);
        db.execSQL(DELETED_TRANSACTION_TABLE_CREATE);
    }
    public void clearInsertedTransactionsTable(SQLiteDatabase db){
        db.execSQL(DROP_INSERTED_TRANSACTIONS_TABLE);
        db.execSQL(INSERTED_TRANSACTION_TABLE_CREATE);
    }
    public void clearEditedTransactionsTable(SQLiteDatabase db){
        db.execSQL(DROP_EDITED_TRANSACTIONS_TABLE);
        db.execSQL(EDITED_TRANSACTION_TABLE_CREATE);
    }
}
