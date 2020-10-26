package com.example.helloworld;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements FilterDialog.FilterDialogListener {
    static TextView tv;
    static Button b,dialog;
    static final String APIKEY="579b464db66ec23bdd0000019f6763ff8f8f48b46ac271169e884dd7";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        b=findViewById(R.id.button);
        tv=findViewById(R.id.textView);
        dialog=findViewById(R.id.button2);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FiltersData.reset();
                String sURL = "https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070?api-key="+APIKEY+"&format=json&offset=0&limit=25";
                showResults(sURL);

            }

        });
        dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv.setText("");
                FiltersData.reset();
                openFiltersDialog();
            }
        });
    }
    public void openFiltersDialog(){
        FilterDialog filterDialog=new FilterDialog();
        filterDialog.show(getSupportFragmentManager(),"filtersDialog");
    }
    public void filteredResult(){
        String sURL = "https://api.data.gov.in/resource/9ef84268-d588-465a-a308-a864a43d0070?api-key="+APIKEY+"&format=json&offset=0";
        if(FiltersData.limitNumber==0){sURL+="&limit=25";}else{sURL+="&limit="+Integer.toString(FiltersData.limitNumber);}
        if(!FiltersData.stateName.equals("All")){sURL+="&filters[state]="+FiltersData.stateName;}
        if(!FiltersData.districtName.equals("All")){sURL+="&filters[district]="+FiltersData.districtName;}
        if(!FiltersData.marketName.equals("All")){sURL+="&filters[market]="+FiltersData.marketName;}
        if(!FiltersData.sortParam.equals("None")){Commodity.flag=FiltersData.sortParam.toLowerCase();}
        if(!FiltersData.commodityName.equals("All")){sURL+="&filters[commodity]="+ FiltersData.commodityName;}

        Log.d("Ricky",sURL);
        showResults(sURL);
    }
    public void showResults(String sURL) {
        if(checkInternet(MainActivity.this)) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(sURL);
                        URLConnection request = url.openConnection();
                        request.connect();

                        JsonParser jp = new JsonParser();
                        JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
                        JsonObject rootobj = root.getAsJsonObject();
                        JsonArray records = rootobj.get("records").getAsJsonArray();
                        if (records.size() == 0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tv.setText(Html.fromHtml("<strong><font color='red'>No Results Found for Query At This Moment</font></strong>"));
                                }
                            });
                            return;

                        }
                        Commodity commodity[] = new Commodity[records.size()];
                        for (int i = 0; i < records.size(); i++) {
                            JsonObject jsonObject = records.get(i).getAsJsonObject();
                            //Log.d("Ricky",jsonObject.toString());
                            commodity[i] = new Commodity(
                                    jsonObject.get("timestamp").getAsString(),
                                    jsonObject.get("state").getAsString(),
                                    jsonObject.get("district").getAsString(),
                                    jsonObject.get("market").getAsString(),
                                    jsonObject.get("commodity").getAsString(),
                                    jsonObject.get("variety").getAsString(),
                                    jsonObject.get("arrival_date").getAsString(),
                                    Double.parseDouble(jsonObject.get("min_price").getAsString()),
                                    Double.parseDouble(jsonObject.get("max_price").getAsString()),
                                    Double.parseDouble(jsonObject.get("modal_price").getAsString())
                            );
                        }
                        if (!FiltersData.sortParam.equals("None")) {
                            Arrays.sort(commodity);
                        }
                        String fullTextDisplay = "";
                        for (Commodity c : commodity) {
                            fullTextDisplay += " <div >" +
                                    "  <strong><font color='grey'>State: " + c.state + "</font><font color='navy'> District: " + c.district + "</font><font color='red'> Variety: " + c.variety + "</font></strong><br/>" +
                                    "  <font color='purple'> Market: " + c.market + "</font><strong><font color='black' size='4sp'> Commodity: " + c.commodity + "</font></strong><br/>" +
                                    "  <font color='blue'> MinPrice: &#x20B9; " + Double.toString(c.min_price) + " </font><font color='blue'> MaxPrice: &#x20B9; " + Double.toString(c.max_price) + "</font>" +
                                    "  <font color='blue'> Modal: &#x20B9; " + Double.toString(c.modal_price) + "</font>" +
                                    "  <br/>" + "<font color='grey'>Arrival Date: " + c.arrival_date + "</font><br/>" +
                                    "  <span>...</span>" +
                                    "  </div>";
                        }
                        String tempDisplay = fullTextDisplay + "<div>End of results.</div>";
                        final String displayText = tempDisplay;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //String check[]=displayText.split("<div>");
                                //Log.d("Ricky",check[check.length-1]);
                                tv.setText(Html.fromHtml(displayText));
                            }
                        });

                        //Log.d("Ricky",Integer.toString(records.size()));


                    } catch (Exception e) {
                        // Log.d("Ricky",e.toString());
                    }
                }
            });
            thread.start();
        }
    }

    @Override
    public void applyFilters() {
        filteredResult();
    }

    public static boolean checkInternet(Context context) {
        NetworkInfo info = (NetworkInfo) ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if(info==null){
            Toast.makeText(context,"No Internet Connection.",Toast.LENGTH_LONG).show();
            b.setEnabled(false);
            dialog.setEnabled(false);
            tv.setText("Connect to Internet and Restart App");
            return false;
        }
        return true;
    }
}
class Commodity implements Comparable<Commodity>{
    String timestamp="",state="",district="",market="",commodity="",variety="",arrival_date="";
    double min_price=0,max_price=0,modal_price=0;
    Commodity(String timestamp,String state,String district,String market,String commodity,
              String variety,String arrival_date,Double min_price,Double max_price,Double modal_price)
    {
        this.timestamp=timestamp;
        this.state=state;
        this.district=district;
        this.market=market;
        this.commodity=commodity;
        this.variety=variety;
        this.arrival_date=arrival_date;
        this.min_price=min_price;
        this.max_price=max_price;
        this.modal_price=modal_price;
    }
    static String flag="price increasing";
    public int compareTo(Commodity obj){
        if(flag.equals("price increasing")){
            return Double.compare(this.modal_price,obj.modal_price);
        }else if(flag.equals("price decreasing")){
            return -1*Double.compare(this.modal_price,obj.modal_price);
        }
        else if(flag.equals("date increasing")){
            DateFormat dateFormat=new SimpleDateFormat("dd/mm/yyyy");
            Date date1=new Date(),date2=new Date();
            try {
                date1 = dateFormat.parse(this.arrival_date);
                date2 = dateFormat.parse(obj.arrival_date);
            }catch(Exception e){
              //  Log.d("Ricky","Error in Date Format");
            }
            return date1.compareTo(date2);
        }else{
            DateFormat dateFormat=new SimpleDateFormat("dd/mm/yyyy");
            Date date1=new Date(),date2=new Date();
            try {
                date1 = dateFormat.parse(this.arrival_date);
                date2 = dateFormat.parse(obj.arrival_date);
            }catch(Exception e){
              //  Log.d("Ricky","Error in Date Format");
            }
            return -1*date1.compareTo(date2);
        }
    }
}