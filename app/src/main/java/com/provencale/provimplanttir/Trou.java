package com.provencale.provimplanttir;

import org.json.JSONObject;
import org.json.JSONException;

public class Trou implements Comparable<Trou> {
    // Position GPS (ie WGS 84)
    public Double latitude;
    public Double longitude;
    public Double altitude;
    public Long timeUtc;// GPS timestamp in UTC : Unix epoch time of this location fix
    public Integer numeroTrou; // Dans rangee
    public Integer numeroRangee; // Dans volee
    public String nomVolee;

    public Trou (String nomVolee, int numeroRangee, int numeroTrou,
                 double latitude, double longitude, double altitude, long timeUtc){
        this.nomVolee = nomVolee;
        this.numeroRangee = numeroRangee;
        this.numeroTrou = numeroTrou;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.timeUtc = timeUtc;
    }

    public Trou (JSONObject jsonObject){
        try {
            this.nomVolee = jsonObject.getString("nomVolee");;
            this.numeroRangee = jsonObject.getInt("numeroRangee");
            this.numeroTrou = jsonObject.getInt("numeroTrou");
            this.latitude = jsonObject.getDouble("latitude");
            this.longitude = jsonObject.getDouble("longitude");
            this.altitude = jsonObject.getDouble("altitude");
            if (jsonObject.isNull("timeUtc")){ // checks if timeUtc is present and if null
                this.timeUtc = 0L;
            }
            else{
                this.timeUtc = jsonObject.getLong("timeUtc");
            }


        } catch (JSONException e) {
            throw new RuntimeException(e);
            //            e.printStackTrace();
            //            return new JSONObject();
        }
    }

    public JSONObject toJson() {
        try {
            JSONObject jsonTrou = new JSONObject();
            jsonTrou.put("nomVolee",this.nomVolee);
            jsonTrou.put("numeroRangee",this.numeroRangee);
            jsonTrou.put("numeroTrou",this.numeroTrou);
            jsonTrou.put("latitude",this.latitude);
            jsonTrou.put("longitude",this.longitude);
            jsonTrou.put("altitude",this.altitude);
            jsonTrou.put("timeUtc",this.timeUtc);
            return jsonTrou;
        } catch (JSONException e) {
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

