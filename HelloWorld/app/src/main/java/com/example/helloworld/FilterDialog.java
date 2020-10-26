package com.example.helloworld;

import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.angmarch.views.NiceSpinner;
import org.angmarch.views.OnSpinnerItemSelectedListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class FilterDialog extends AppCompatDialogFragment {
    String stateName="All",districtName="All",marketName="All",sortParam="None",filename;
    NiceSpinner state_list,district_list,village_list,sorting_param;
    EditText commodityNameE;
    EditText limitNumberE;
    FilterDialogListener listener;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflator = getActivity().getLayoutInflater();
        View view = inflator.inflate(R.layout.filters_dialog,null);
        builder.setView(view).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setPositiveButton("SEARCH", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                    FiltersData.stateName=stateName;
                    FiltersData.districtName=districtName;
                    FiltersData.marketName=marketName;
                    FiltersData.sortParam=sortParam;
                    String temp=commodityNameE.getText().toString();
                    if(temp.equals("Commodity") || temp.equals("")){FiltersData.commodityName="All";}
                    else{FiltersData.commodityName=temp.substring(0,1).toUpperCase()+temp.substring(1);}
                    int temp2=0;
                    try {
                        temp2 = Integer.parseInt(limitNumberE.getText().toString());
                    }catch(Exception e){temp2=0;}
                    if(temp2==0){FiltersData.limitNumber=25;}
                    else{FiltersData.limitNumber=temp2;}
                    //Log.d("Ricky",Integer.toString(temp2));
                    Log.d("Ricky",districtName);
                    listener.applyFilters();
            }
        });
        commodityNameE = view.findViewById(R.id.commodityName);
        limitNumberE = view.findViewById(R.id.limitNumber);
        state_list = (NiceSpinner) view.findViewById(R.id.state_list);
        district_list = (NiceSpinner) view.findViewById(R.id.district_list);
        village_list = (NiceSpinner) view.findViewById(R.id.village_list);
        sorting_param = (NiceSpinner) view.findViewById(R.id.sorting_param);
        district_list.setEnabled(false);
        district_list.setBackgroundColor(Color.parseColor("#ef9a9a"));
        village_list.setEnabled(false);
        village_list.setBackgroundColor(Color.parseColor("#ef9a9a"));
        List<String> states = new LinkedList<>(Arrays.asList("Choose State","All", "Andaman and Nicobar","Andhra Pradesh","Arunanchal Pradesh","Assam","Bihar","Chandigarh","Chattisgarh",
                "Dadra and Nagar Haveli","Daman and Diu","Delhi","Goa","Gujarat","Haryana","Himachal Pradesh","Jammu and Kashmir","Jharkhand","Karanataka",
                "Kerala","Lakshadweep","Madhya Pradesh","Maharashtra","Meghalaya","Mizoram","Nagaland","Odisha","Puducherry","Punjab","Rajasthan","Sikkim",
                "Tamil Nadu","Tripura","Uttarakhand","West Bengal"));
        state_list.attachDataSource(states);
        List<String> districts = new LinkedList<>(Arrays.asList("Choose District","All"));
        district_list.attachDataSource(districts);
        List<String> villages = new LinkedList<>(Arrays.asList("Choose Village","All"));
        village_list.attachDataSource(villages);
        List<String> sorting = new LinkedList<>(Arrays.asList("Sort By","None","Price Increasing","Price Decreasing","Date Increasing","Date Decreasing"));
        sorting_param.attachDataSource(sorting);
        state_list.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                String chosen_state=String.valueOf(parent.getItemAtPosition(position));
                stateName=chosen_state;
                if(stateName.equals("All")){return;}
                String temp[]=chosen_state.split(" ");
                filename=temp[0].toLowerCase();
                //Log.d("Ricky",filename);
                int resId = getResources().getIdentifier(filename, "raw", getContext().getPackageName());
                district_list.setEnabled(true);
                district_list.setBackgroundColor(Color.parseColor("#ffffff"));
                //Log.d("Ricky",Integer.toString(resId));
                LinkedList<String> districtsList = readData(resId,1);
                districtsList.addFirst("All");
                districtsList.addFirst("Choose District");
                district_list.attachDataSource(districtsList);
            }
        });
        district_list.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                districtName=String.valueOf(parent.getItemAtPosition(position));
                FiltersData.districtName=districtName;
                if(districtName.equals("All")){return;}
                int resId = getResources().getIdentifier(filename,"raw",getContext().getPackageName());
                village_list.setEnabled(true);
                village_list.setBackgroundColor(Color.parseColor("#ffffff"));
                LinkedList<String> villagesList = readData(resId,2);
                villagesList.addFirst("All");
                villagesList.addFirst("Choose Village");
                village_list.attachDataSource(villagesList);
            }
        });
        village_list.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                marketName=String.valueOf(parent.getItemAtPosition(position));
            }
        });
        sorting_param.setOnSpinnerItemSelectedListener(new OnSpinnerItemSelectedListener() {
            @Override
            public void onItemSelected(NiceSpinner parent, View view, int position, long id) {
                sortParam=String.valueOf(parent.getItemAtPosition(position));
            }
        });
        return builder.create();
    }
    public LinkedList<String> readData(int resId,int dv) {
        InputStream is = getResources().openRawResource(resId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String line = "";
        LinkedList<String> dataList=new LinkedList<String>();
        try {
            if(dv==1){
            String temp="";
            reader.readLine();
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String columns[]=line.split(",");
                if(columns.length>3){
                if(columns[3].equals(temp)){}
                else{dataList.add(columns[3]);temp=columns[3];
                //Log.d("Ricky",columns[3]);
                }}}
            }
            else{
                reader.readLine();
                reader.readLine();
                while((line = reader.readLine())!=null){
                    String columns[]=line.split(",");
                    if(columns.length>=7 && columns[3].equals(districtName)){
                        dataList.add(columns[columns.length-1]);
                    }
                }

            }
        }
        catch(Exception e){
            //Log.d("Ricky",e.getMessage());
        }
        return dataList;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (FilterDialogListener) context;
        }catch(Exception e){e.printStackTrace();}

    }

    public interface FilterDialogListener {
        void applyFilters();
    }

}
