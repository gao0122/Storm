package com.storm.storm;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Storm on 5/4/16.
 *
 * @author Yuchao
 */
public class VIP implements Parcelable {

    /**
     * automatically called when this object need to be written parcelable
     */
    public static final Creator<VIP> CREATOR = new Creator<VIP>() {
        @Override
        public VIP createFromParcel(Parcel in) {
            return new VIP(in);
        }

        @Override
        public VIP[] newArray(int size) {
            return new VIP[size];
        }
    };

    private String name, phone;
    private int total, consumption;

    public VIP(String name, String phone, int total) {
        setName(name);
        setPhone(phone);
        setTotal(total);
        setConsumption(0);
    }

    protected VIP(Parcel in) {
        name = in.readString();
        phone = in.readString();
        total = in.readInt();
        consumption = in.readInt();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * get discount rate for this VIP
     *
     * @return a float number represents the discount rate
     */
    public float getDiscount(int s, float sf, int g, float gf, int p, float pf, float f) {
        return h(s) ? f : h(g) ? sf : h(p) ? gf : pf;
    }

    /**
     * helper function to judge if n is larger than total consumption
     *
     * @param n to be compared with total
     * @return true is n is larger
     */
    private boolean h(int n) {
        return total < n;
    }

    public int getConsumption() {
        return consumption;
    }

    public void setConsumption(int consumption) {
        this.consumption = consumption;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * write all information parcelable
     *
     * @param dest
     * @param flags
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeInt(total);
        dest.writeInt(consumption);
    }
}
