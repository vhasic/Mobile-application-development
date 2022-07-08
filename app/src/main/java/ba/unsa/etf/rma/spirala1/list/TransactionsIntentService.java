package ba.unsa.etf.rma.spirala1.list;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import ba.unsa.etf.rma.spirala1.data.Account;
import ba.unsa.etf.rma.spirala1.data.Transaction;
import ba.unsa.etf.rma.spirala1.data.Type;


public class TransactionsIntentService extends IntentService {
    public static final int STATUS_RUNNING=0;
    public static final  int STATUS_FINISHED=1;
    public static final int STATUS_ERROR=2;
    private ResultReceiver receiver;
    private Bundle bundle = new Bundle();

    private ArrayList<Transaction> transactions=new ArrayList<>();
    private String id="2b2683f0-a2a6-4092-907a-177a307c4ead"; //nema drugog načina; Resources.getSystem().getString(R.string.api_id) baca izuzetak
    private SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
    private Type[] types= new Type[]{Type.ALL,Type.REGULARPAYMENT,Type.REGULARINCOME,Type.PURCHASE,Type.INDIVIDUALINCOME,Type.INDIVIDUALPAYMENT};

    public TransactionsIntentService() {
        super("TransactionsIntentService");
    }

    public TransactionsIntentService(String name) {
        super(name);
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        receiver = intent.getParcelableExtra("receiver");
        if(receiver!=null) {
            bundle = new Bundle();
            // Obavijest o početku rada
            receiver.send(STATUS_RUNNING, Bundle.EMPTY);
        }

        switch (intent.getStringExtra("action")) {
            case "getAll":
                getTransactions(null, null, null , null);
                break;
            case "getFiltered":
                Type type = (Type) intent.getSerializableExtra("type");
                int index = 0;
                if (type != null) index = Arrays.asList(types).indexOf(type);
                String indexString=null;
                if(index!=0) indexString=String.valueOf(index);

                String sort = intent.getStringExtra("sort");
                if(sort!=null) {
                    sort = sort.replaceAll("Price - ", "amount.");
                    sort = sort.replaceAll("Title - ", "title.");
                    sort = sort.replaceAll("Date - ", "date.");
                    sort = sort.replaceAll("Ascending", "asc");
                    sort = sort.replaceAll("Descending", "dsc");
                }

                String month=null;
                String year=null;
                Date date = (Date) intent.getSerializableExtra("date");
                if(date!=null) {
                    Calendar c = new GregorianCalendar();
                    c.setTime(date);
                    //da bih dobio ispravan mjesec moram dodati 1
                    c.add(Calendar.MONTH,1);
                    SimpleDateFormat format=new SimpleDateFormat("MM",Locale.ENGLISH);
                    month=format.format(c.getTime());
                    year=String.valueOf(c.get(Calendar.YEAR));
                }
                getTransactions(indexString, sort, month, year);
                break;
            case "insert":
                insertTransaction((Transaction) intent.getSerializableExtra("Transaction"));
                break;
            case "delete":
                deleteTransaction((Transaction) intent.getSerializableExtra("Transaction"));
                break;
            case "editTransaction":
                editTransaction((Transaction) intent.getSerializableExtra("oldTransaction"),
                        (Transaction) intent.getSerializableExtra("newTransaction"));
                break;
            case "editAccount":
                editAccount((Account) intent.getSerializableExtra("account"));
                break;
            case "getAccount":
                getAccount();
                break;
        }
    }

    private void deleteTransaction(Transaction transaction) {
        //ovo s id-om ne radi za dodane transakcije
        String url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com" + "/account/" + id + "/transactions/"+
                transaction.getId();

        URL url = null;
        HttpURLConnection httpURLConnection=null;
        try {
            url = new URL(url1);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestMethod("DELETE");
            //provjera
            System.out.println(httpURLConnection.getResponseCode());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        if(receiver!=null) {
            //Proslijedi rezultate nazad u pozivatelja
            bundle.putString("action", "delete");
            receiver.send(STATUS_FINISHED, bundle);
        }
    }

    //na stranici ne radi edit tipa
    private void editTransaction(Transaction oldTransaction,Transaction newTransaction) {
        String url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com" + "/account/" + id+
                "/transactions/"+oldTransaction.getId();
        URL url = null;
        try {
            url = new URL(url1);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();

            JSONObject jsonParam = getJsonObjectForTransaction(newTransaction);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(jsonParam.toString());
            os.flush();
            os.close();

            try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                //String pomocni=response.toString();
                //JSONObject jsonObject=new JSONObject(pomocni);
                //System.out.println(pomocni);
            }

            conn.disconnect();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void insertTransaction(Transaction transaction) {
        String url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com" + "/account/" + id + "/transactions";
        URL url;
        try {
            url = new URL(url1);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();

            JSONObject jsonParam = getJsonObjectForTransaction(transaction);

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(jsonParam.toString());
            os.flush();
            os.close();

            try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                String pomocni=response.toString();
                JSONObject jsonObject=new JSONObject(pomocni);
                Transaction t=getTransactionFromJSONObject(jsonObject);
                transaction.setId(t.getId());
                System.out.println(pomocni);
            }

            conn.disconnect();
        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
        }
        if(receiver!=null) {
            //Proslijedi rezultate nazad u pozivatelja
            bundle.putString("action", "insert");
            receiver.send(STATUS_FINISHED, bundle);
        }
    }

    private JSONObject getJsonObjectForTransaction(Transaction newTransaction) throws JSONException {
        String endDate = null;
        String date = null;
        String title = "";
        String itemDescription = null;
        String transactionInterval = null;
        String typeId = "";
        try {
            itemDescription = newTransaction.getItemDescription().equals("") ? null : newTransaction.getItemDescription();
            transactionInterval = newTransaction.getTransactionInterval() == 0 ? null : String.valueOf(newTransaction.getTransactionInterval());
            typeId = String.valueOf(Arrays.asList(types).indexOf(newTransaction.getType()));
            title = newTransaction.getTitle();
            date = format.format(newTransaction.getDate().getTime());
            endDate = format.format(newTransaction.getEndDate().getTime());
        } catch (Exception ignored) {
        }

        JSONObject jsonParam = new JSONObject();
        jsonParam.put("date", date);
        jsonParam.put("title", title);
        jsonParam.put("amount", newTransaction.getAmount());
        jsonParam.put("endDate", endDate);
        jsonParam.put("itemDescription", itemDescription);
        jsonParam.put("transactionInterval", transactionInterval);
        jsonParam.put("TransactionTypeId", typeId);

        return jsonParam;
    }

    private void getAccount() {
        String url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com" + "/account/" + id;
        Account account=new Account();
        HttpURLConnection httpURLConnection=null;
        try {
            URL url = new URL(url1);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
            String result = convertStreamToString(in);
            JSONObject jo = new JSONObject(result);
            account.setBudget(jo.getDouble("budget"));
            account.setTotalLimit(jo.getDouble("totalLimit"));
            account.setMonthLimit(jo.getDouble("monthLimit"));
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        if(receiver!=null) {
            /* Proslijedi rezultate nazad u pozivatelja */
            bundle.putString("action", "getAccount");
            bundle.putSerializable("account", account);
            receiver.send(STATUS_FINISHED, bundle);
        }
    }

    private void editAccount(Account account) {
        String url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com" + "/account/" + id;
        URL url = null;
        try {
            url = new URL(url1);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept","application/json");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("budget", account.getBudget());
            jsonParam.put("totalLimit", account.getTotalLimit());
            jsonParam.put("monthLimit", account.getMonthLimit());

            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(jsonParam.toString());
            os.flush();
            os.close();

            try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                //String pomocni=response.toString();
                //JSONObject jsonObject=new JSONObject(pomocni);
                //System.out.println(pomocni);
            }

            conn.disconnect();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        if(receiver!=null) {
            /* Proslijedi rezultate nazad u pozivatelja */
            bundle.putString("action", "editAccount");
            bundle.putSerializable("account", account);
            receiver.send(STATUS_FINISHED, bundle);
        }
    }

    private void getTransactions(String typeId, String sort, String month, String year) {
        int page=0;
        boolean prekid=false;
        while (!prekid) {
            /*String url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com" + "/account/" + id + "/transactions?page=" + page
                    +"&typeId="+typeId+"&sort="+sort+"&month="+month+"&year="+year;*/
            String url1 = "http://rma20-app-rmaws.apps.us-west-1.starter.openshift-online.com" + "/account/" + id + "/transactions/filter?page=" + page;
            if(typeId!=null) url1+="&typeId="+typeId;
            if(sort!=null) url1+="&sort="+sort;
            if(month!=null) url1+="&month="+month;
            if(year!=null) url1+="&year="+year;

            HttpURLConnection httpURLConnection=null;
            try {
                URL url = new URL(url1);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(httpURLConnection.getInputStream());
                String result = convertStreamToString(in);
                JSONObject jo = new JSONObject(result);
                JSONArray results = jo.getJSONArray("transactions");
                if(results.length()==0) prekid=true;
                for (int i = 0; i < results.length(); i++) {
                    JSONObject object = results.getJSONObject(i);
                    Transaction transaction = getTransactionFromJSONObject(object);
                    transactions.add(transaction);
                }
            } catch (ParseException | IOException | JSONException e) {
                e.printStackTrace();
            }
            finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }
            page++;
        }
        /* Proslijedi rezultate nazad u pozivatelja */
        bundle.putString("action","get");
        bundle.putSerializable("transactions",transactions);
        receiver.send(STATUS_FINISHED, bundle);
    }

    private Transaction getTransactionFromJSONObject(JSONObject object) throws ParseException, JSONException {
        Transaction transaction=new Transaction();
        int id=object.getInt("id");
        transaction.setId(id);
        Date d=format.parse(object.getString("date"));
        Calendar c=new GregorianCalendar();
        c.setTime(d);
        transaction.setDate(c);
        transaction.setTitle(object.getString("title"));
        transaction.setAmount(object.getDouble("amount"));
        String itemDescription=object.getString("itemDescription");
        transaction.setItemDescription(itemDescription.equals("null")? null:itemDescription);

        try {
            transaction.setTransactionInterval(Integer.parseInt(object.getString("transactionInterval")));
        }
        catch (NullPointerException | NumberFormatException e){
            transaction.setTransactionInterval(0);
        }

        try {
            d=format.parse(object.getString("endDate"));
            c=new GregorianCalendar();
            c.setTime(d);
            transaction.setEndDate(c);
        }
        catch (Exception e){
            transaction.setEndDate(null);
        }
        transaction.setType(types[object.getInt("TransactionTypeId")]);
        return transaction;
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        return sb.toString();
    }
}
