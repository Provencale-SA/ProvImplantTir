package com.provencale.provimplanttir;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Trou implements Comparable<Trou> {
    ////////   /!\ Position in GPS standard (ie WGS 84)/////////////////////////////////////////////
    public Double latitude;
    public Double longitude;
    public Double altitude;
    public Date timeUtc;// GPS timestamp in UTC : Unix epoch time of this location fix
    public Integer numeroTrou; // Dans rangee
    public Integer numeroRangee; // Dans volee
    public String nomVolee;

    public Trou (String nomVolee, int numeroRangee, int numeroTrou,
                 double latitude, double longitude, double altitude, Date timeUtc){
        this.nomVolee = nomVolee;
        this.numeroRangee = numeroRangee;
        this.numeroTrou = numeroTrou;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.timeUtc = timeUtc;
    }

    private static Date parseJsontoDate(String text) throws JSONException {
        try {
            if (text.length() == 19) { // assume no timezone : GMT
                text = text + "GMT-00:00";
            } else {
                if (text.endsWith("Z")) {
                    text = text.substring(0, text.length() - 1) + "GMT-00:00";
                } else{
                    int inset = 6;// assume +00:00
                    String s0 = text.substring(0, text.length() - inset);
                    String s1 = text.substring(text.length() - inset, text.length());
                    text = s0 + "GMT" + s1;
                }
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
            return dateFormat.parse(text);
        } catch (ParseException e ){
            Log.e("Trou","parseJsontoDate:ParseException:"+e.toString());
            throw new JSONException("Invalid timeutc format");
        }
    }

    private static String parseDateToJson(Date timeutc) {
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );
        TimeZone tz = TimeZone.getTimeZone( "UTC" );
        dateFormat.setTimeZone(tz);
        return dateFormat.format( timeutc );
    }

    public Trou (JSONObject jsonObject) throws JSONException {
        try {
            JSONObject jsonTrouProperties = jsonObject.getJSONObject("properties");
            this.nomVolee = jsonTrouProperties.getString("nomVolee");;
            this.numeroRangee = jsonTrouProperties.getInt("numeroRangee");
            this.numeroTrou = jsonTrouProperties.getInt("numeroTrou");
            if (jsonTrouProperties.isNull("timeUtc")){ // checks if timeUtc is present and if null
                this.timeUtc = new java.util.Date(0L);
            }
            else{
                this.timeUtc = parseJsontoDate(jsonTrouProperties.getString("timeUtc"));
            }

            JSONObject jsonTrouGeo = jsonObject.getJSONObject("geometry");
            JSONArray jsonArrayCoord = jsonTrouGeo.getJSONArray("coordinates");
            this.longitude = jsonArrayCoord.getDouble(0); // Longitude then latitude then altitude in geoJson Position
            this.latitude = jsonArrayCoord.getDouble(1);
            this.altitude = jsonArrayCoord.getDouble(2);

        } catch (JSONException e) {
            Log.e("Trou","Trou:JSONException:"+e.toString());
            throw e;
            //            e.printStackTrace();
            //            return new JSONObject();
        }
    }

    public JSONObject toJson() {
        // Let s write it as a Feature in a GeoJSON
        try {
            JSONObject jsonTrou = new JSONObject();
            jsonTrou.put("type","Feature");

            JSONObject jsonTrouProperties = new JSONObject();
            jsonTrouProperties.put("name",String.format("%s%02d%02d", this.nomVolee,this.numeroRangee,this.numeroTrou));//nomVolee+numeroRangeeOnDigits+numeroTrouOn2Digits //NOT TAKEN INTO ACCOUNT WHEN READING THE FILE
            jsonTrouProperties.put("nomVolee",this.nomVolee);
            jsonTrouProperties.put("numeroRangee",this.numeroRangee);
            jsonTrouProperties.put("numeroTrou",this.numeroTrou);
            jsonTrouProperties.put("timeUtc",parseDateToJson(this.timeUtc));
            jsonTrou.put("properties",jsonTrouProperties);


            JSONObject jsonTrouGeo = new JSONObject();
            jsonTrouGeo.put("type","Point");

            JSONArray jsonArrayCoord = new JSONArray();// Longitude then latitude then altitude in geoJson Position
            jsonArrayCoord.put(this.longitude);
            jsonArrayCoord.put(this.latitude);
            jsonArrayCoord.put(this.altitude); // can also be put in Properties (but makes it redundant)
            jsonTrouGeo.put("coordinates",jsonArrayCoord);

            jsonTrou.put("geometry",jsonTrouGeo);

            return jsonTrou;
        } catch (JSONException e) {
            Log.e("Trou","toJson:JSONException:"+e.toString());
            throw new RuntimeException(e);
            //            e.printStackTrace();
            //            return new JSONObject();
        }
    }

    public String toString() {
        return this.toJson().toString();
    }

    // needed by Comparable : reverse Order for nomVolee, numeroRangee, numeroTrou, timeUtc
    @Override public int compareTo(Trou t) {
        if (this.nomVolee.compareTo(t.nomVolee) != 0) {
            return -this.nomVolee.compareTo(t.nomVolee);
        }
        else{
            if (this.numeroRangee.compareTo(t.numeroRangee) != 0) {
                return -this.numeroRangee.compareTo(t.numeroRangee);
            }
            else{
                if (this.numeroTrou.compareTo(t.numeroTrou) != 0) {
                    return -this.numeroTrou.compareTo(t.numeroTrou);
                }
                else{
                    if (this.timeUtc.compareTo(t.timeUtc) != 0) {
                        return -this.timeUtc.compareTo(t.timeUtc);
                    }
                    else{
                        if (this.latitude.compareTo(t.latitude) != 0) {
                            return this.latitude.compareTo(t.latitude);
                        }
                        else{
                            if (this.longitude.compareTo(t.longitude) != 0) {
                                return this.longitude.compareTo(t.longitude);
                            }
                            else{
                                return this.altitude.compareTo(t.altitude);
                            }
                        }
                    }
                }
            }
        }
    }
};

