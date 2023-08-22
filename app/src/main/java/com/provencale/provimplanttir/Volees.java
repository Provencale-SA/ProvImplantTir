package com.provencale.provimplanttir;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class Volees {
    public String FILENAME = "storage.json";
    private SortedSet<Trou> setTrous =  new TreeSet<Trou>() ;

    //Creator
    public Volees(Context context){
        Log.d("Volees","Creator Volees");
        boolean isFilePresent = this.isFilePresent(context);
        if(isFilePresent) {
            Log.d("Volees","File found");
            this.read(context);
            //do the json parsing here and do the rest of functionality of app
        } else {
            boolean isFileCreated = this.create(context);
            Log.d("Volees","File created");
            if(isFileCreated) {
                Log.d("Volees","File successfully created");
                //this.volees = new Volees();
            } else {
                Log.e("Volees","File cannot be created");
                //show error or try again.
            }
        }
    }

    public void addtrou(String nomVolee, int numeroRangee, int numeroTrou,
                   double latitude, double longitude, double altitude){
        Log.d("Volees","addtrou");
        Trou trou = new Trou(nomVolee, numeroRangee, numeroTrou,latitude, longitude,  altitude);
        setTrous.add(trou);

    }

    public boolean removeTrou(Trou trou){
        Log.d("Volees","removeTrou");
        return setTrous.remove(trou);
    }


    public ArrayList<Trou> toArrayList() {
        return new ArrayList(this.setTrous);
    };

    public String toString() {
        Log.d("Volees","toString");
        try {
            JSONArray jsonArray = new JSONArray();
            for (Trou t : this.setTrous) {
                JSONObject jsonTrou = t.toJson();
                jsonArray.put(jsonTrou);
            }

            JSONObject volees = new JSONObject();
            volees.put("volees", jsonArray);
            return volees.toString();
        } catch (JSONException e) {
            Log.e("Volees","toString:JSONException");
            //e.printStackTrace();
            return "";
        }

    };


    public void fromString(String jsonString) {
        Log.d("Volees","fromString");
        try {
            JSONObject jsnobject = new JSONObject(jsonString);
            JSONArray jsonArray = jsnobject.getJSONArray("volees");
            Log.d("Volees","fromString:length"+String.valueOf(jsonArray.length()));

            setTrous.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                Trou trou = new Trou(jsonArray.getJSONObject(i));
                setTrous.add(trou);
            }
        } catch (JSONException e) {
            Log.e("Volees","fromString:JSONException");
            //e.printStackTrace();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //// File handling
    ////////////
    /// https://stackoverflow.com/questions/40168601/android-how-to-save-json-data-in-a-file-and-retrieve-it

    public void read(Context context) {
        Log.d("Volees","read");
        this.fromString(readFile(context));
    }
    private String readFile(Context context) {
        Log.d("Volees","readFile");
        try {
            FileInputStream fis = context.openFileInput(this.FILENAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            Log.d("Volees","readFile:Trous:"+sb.toString());
            return sb.toString();
        } catch (FileNotFoundException fileNotFound) {
            Log.d("Volees","readFile:fileNotFound");
            return null;
        } catch (IOException ioException) {
            Log.d("Volees","readFile:ioException");
            return null;
        }
    }

    public boolean write(Context context) {
        Log.d("Volees","write");
        String jsonString = this.toString();
        return this.writeFile(context, jsonString);
    }
    //chekc json  https://stackoverflow.com/questions/62474129/create-write-edit-json-file-in-android-studio
    private boolean writeFile(Context context, String jsonString) {
        Log.d("Volees","writeFile");
        try {
            FileOutputStream fos = context.openFileOutput(this.FILENAME, Context.MODE_PRIVATE);
            if (jsonString != null) {
                fos.write(jsonString.getBytes());
            }
            fos.close();
            return true;
        } catch (FileNotFoundException fileNotFound) {
            Log.e("Volees","writeFile:fileNotFound");
            return false;
        } catch (IOException ioException) {
            Log.e("Volees","writeFile:ioException");
            return false;
        }
    }


    public boolean create(Context context) {
        Log.d("Volees","Create");
        return writeFile(context, "");
    }

    public boolean isFilePresent(Context context) {
        Log.d("Volees","isFilePresent");
        String path = context.getFilesDir().getAbsolutePath() + "/" + this.FILENAME;
        File file = new File(path);
        return file.exists();
    }


}
