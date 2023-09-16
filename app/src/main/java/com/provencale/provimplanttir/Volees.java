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
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

// File is stored in [Phone]\Mémoire de stockage interne\Android\data\com.provencale.provimplanttir\files\
public class Volees {
    public String FILENAME = "volees.geojson";
    private int JSONWRITESPACEEACHLEVEL = 2;
    private SortedSet<Trou> setTrous =  new TreeSet<Trou>() ; // the order is defined in Trou.compareTo

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

    public boolean contains(String nomVolee, int numeroRangee, int numeroTrou){
        Trou trou = new Trou(nomVolee, numeroRangee, numeroTrou,0., 0.,0.,new Date(0));
        return setTrous.contains(trou);
    }


    public void addtrou(String nomVolee, int numeroRangee, int numeroTrou,
                   double latitude, double longitude, double altitude, Date timeUtc){
        Log.d("Volees","addtrou");
        Trou trou = new Trou(nomVolee, numeroRangee, numeroTrou,latitude, longitude,altitude,timeUtc);
        setTrous.add(trou);

    }
    public boolean removeTrou(String nomVolee, int numeroRangee, int numeroTrou){
        Log.d("Volees","removeTrou");
        Trou trou = new Trou(nomVolee, numeroRangee, numeroTrou,0., 0.,0.,new Date(0));
        return setTrous.remove(trou);
    }
    public boolean removeTrou(Trou trou){
        Log.d("Volees","removeTrou");
        return setTrous.remove(trou);
    }

    public void removeALlTrou(){
        Log.d("Volees","deleteALlTrou");
        setTrous.clear();
    }

    public ArrayList<Trou> toArrayList() {
        return new ArrayList(this.setTrous);
    };

    public String toString() {
        // here we ant to create de geojson (FeatureCollection of Points with nomVolee, numeroRangee, numeroTrou, timeUtc as properties and also nomVolee+numeroRangeOn2digits+numeroTrouOn2Digit as optional name (for easier view in googleEarth)

        Log.d("Volees","toString");
        try {
            // Let s prepare the Points
            JSONArray jsonArrayFeatures = new JSONArray();
            for (Trou t : this.setTrous) {
                JSONObject jsonTrou = t.toJson();
                jsonArrayFeatures.put(jsonTrou);
            }

            // now lets create let s encapsulate the geoJSON
            JSONObject jsonbjectFull = new JSONObject();
            jsonbjectFull.put("type", "FeatureCollection");
            jsonbjectFull.put("features", jsonArrayFeatures);
            return jsonbjectFull.toString(JSONWRITESPACEEACHLEVEL);
        } catch (JSONException e) {
            Log.e("Volees","toString:JSONException");
            //e.printStackTrace();
            return "";
        }

    };


    public void fromString(String jsonString) throws JSONException {
        Log.d("Volees","fromString");
        if (jsonString.trim().isEmpty()) {
            setTrous.clear();
            return;
        }
        try {
            JSONObject jsonbjectFull = new JSONObject(jsonString);
            JSONArray jsonArrayFeatures = jsonbjectFull.getJSONArray("features");
            Log.d("Volees","fromString:length "+String.valueOf(jsonArrayFeatures.length()));

            setTrous.clear();
            for (int i = 0; i < jsonArrayFeatures.length(); i++) {
                Trou trou = new Trou(jsonArrayFeatures.getJSONObject(i));
                setTrous.add(trou);
            }
        } catch (JSONException e) {
            Log.e("Volees","fromString:JSONException");
            //e.printStackTrace();
            throw e;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //// File handling
    ////////////
    /// https://stackoverflow.com/questions/40168601/android-how-to-save-json-data-in-a-file-and-retrieve-it

    public File getFile(Context context) {
        File myExternalFile = new File(context.getExternalFilesDir(null), this.FILENAME); // Create a file object but do not create a real file if already exists
        return myExternalFile;
    }

    public void read(Context context) {
        Log.d("Volees","read");
        String jsonString = readFile(context);

        try {
            this.fromString(jsonString);
        } catch (JSONException e) {
            // the  file is malformed, let save it and create a new one to keep going
            Log.e("Volees","read:the file is malformed");
            boolean res = this.saveAndCleanFile(context,jsonString);

            if (!res){
                Log.e("Volees","read:Cannot save reset the file");
            }
            // let s continue as the file was empty
            try {
                this.fromString("");
            } catch (JSONException e2) {
                Log.e("Volees","read:Should not happen something is wrong");

            }
        }

    }
    private String readFile(Context context) {
        Log.d("Volees","readFile");
        try {
            File myExternalFile = getFile(context);
            FileInputStream fis = new FileInputStream(myExternalFile);
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
            File myExternalFile = getFile(context);
            FileOutputStream fos = new FileOutputStream(myExternalFile);
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
        File myExternalFile = getFile(context);
        try {
            boolean success_created = myExternalFile.createNewFile();
            return success_created;
        } catch (IOException ioException) {
            Log.e("Volees","create:ioException");
            return false;
        }
    }

    public boolean isFilePresent(Context context) {
        Log.d("Volees","isFilePresent");
        File myExternalFile = getFile(context);
        return myExternalFile.exists();
    }
    public boolean saveAndCleanFile(Context context,String stringPreviousFile) {
        // This is called when the base file is malformed : Let s keep a copy and clean the original
        Long tsLong = System.currentTimeMillis()/1000;
        String timestamp = tsLong.toString();


        try {
            File savFile = new File(context.getExternalFilesDir(null), "sav_"+timestamp+"_"+this.FILENAME);
            boolean success_created = savFile.createNewFile();
            if (!success_created){
                return false;
            }
            FileOutputStream fos = new FileOutputStream(savFile);
            if (stringPreviousFile != null) {
                fos.write(stringPreviousFile.getBytes());
            }
            fos.close();

        } catch (FileNotFoundException fileNotFound) {
            Log.e("Volees","saveAndCleanFile:fileNotFound");
            return false;
        } catch (IOException ioException) {
            Log.e("Volees","saveAndCleanFile:ioException");
            return false;
        }


        // now let s clean the original
        return writeFile(context, "");
    }


}
