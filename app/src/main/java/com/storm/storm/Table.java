package com.storm.storm;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Storm on 5/5/16.
 * Local database to store dish list using SQLite3
 *
 * @author Yuchao
 */
public class Table extends SQLiteOpenHelper {

    private static final String TABLE_UN = "Storm";
    private static final String TABLE_ING = "Storming";
    private static final String TABLE_ED = "Stormed";
    private static final String TABLE_ID = "TID";
    private static final String DNAME = "dname";
    private static final String AMONT = "amount";
    private static final String PRICE = "price";
    private static final String TYPEE = "type";
    private static final String CNAME = "cname";
    private static final String PHONE = "phone";
    private static final String STATE = "state";
    private static final String SENT_ = "sent";
    private static final String UNCOOK = "uncook";
    private static final String CKING = "cooking";
    private static final String FINISH = "finished";
    private static final String TBLID = "table_id";

    private VIP vip;

    public Table(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // un-cooking dish table
        db.execSQL("CREATE TABLE " + TABLE_UN + "("
                + DNAME + " TEXT PRIMARY KEY, "
                + AMONT + " INTEGER, "
                + STATE + " TEXT, "
                + PRICE + " INTEGER, "
                + TYPEE + " TEXT, "
                + CNAME + " TEXT, "
                + PHONE + " TEXT, "
                + SENT_ + " INTEGER)");

        // cooking dish table
        db.execSQL("CREATE TABLE " + TABLE_ING + "("
                + DNAME + " TEXT PRIMARY KEY, "
                + AMONT + " INTEGER, "
                + STATE + " TEXT, "
                + PRICE + " INTEGER, "
                + TYPEE + " TEXT, "
                + CNAME + " TEXT, "
                + PHONE + " TEXT)");

        // finished dish table
        db.execSQL("CREATE TABLE " + TABLE_ED + "("
                + DNAME + " TEXT PRIMARY KEY, "
                + AMONT + " INTEGER, "
                + STATE + " TEXT, "
                + PRICE + " INTEGER, "
                + TYPEE + " TEXT, "
                + CNAME + " TEXT, "
                + PHONE + " TEXT)");

        // table id table
        db.execSQL("CREATE TABLE " + TABLE_ID + "(" + TBLID + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // do nothing here
    }

    /**
     * add or update dish
     *
     * @param name   dish name string
     * @param amount dish amount
     * @param price  dish price
     * @param type   dish type, there are daily dish, cold dish, hot dish, staple food and drinks
     * @param phone  customer's phone number
     * @param cName  customer's name
     * @return total dish amount after adding a dish/dishes
     */
    public int addDish(String name, int amount, int price, String type, String phone, String cName) {
        int amt = amount;
        SQLiteDatabase wdb = getWritableDatabase();
        ContentValues values = new ContentValues();
        Cursor cursor = wdb.rawQuery("SELECT " + AMONT + " FROM " + TABLE_UN
                + " WHERE " + DNAME + "='" + name + "'", null);
        if (cursor.moveToFirst()) {
            amt += cursor.getInt(0);
            if (amt < 1) {
                wdb.delete(TABLE_UN, DNAME + "='" + name + "'", null);
                return amt;
            } else {
                values.put(AMONT, amt);
                wdb.update(TABLE_UN, values, DNAME + "='" + name + "'", null);
            }
        } else {
            if (type != null) {
                values.put(DNAME, name);
                values.put(AMONT, amount);
                values.put(STATE, UNCOOK);
                values.put(PRICE, price);
                values.put(TYPEE, type);
                values.put(CNAME, cName);
                values.put(PHONE, phone);
                values.put(SENT_, 0);
                wdb.insert(TABLE_UN, null, values);
            } else
                amt = -2;
        }
        cursor.close();
        wdb.close();
        return amt;
    }

    /**
     *
     */
    public void setAllSent() {
        SQLiteDatabase wdb = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SENT_, 1);
        wdb.update(TABLE_UN, values, SENT_ + "='0'", null);
        wdb.close();
    }

    public ArrayList<Map<String, Object>> getOrderedNames() {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor cursor = rdb.rawQuery("SELECT * FROM " + TABLE_UN
                + " WHERE " + STATE + "='" + UNCOOK + "'", null);
        while (cursor.moveToNext()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(DNAME, cursor.getString(0));
            map.put(AMONT, cursor.getInt(1));
            list.add(map);
        }
        cursor.close();
        rdb.close();
        return list;
    }

    public boolean allFinished() {
        boolean b = true;
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor cursor = rdb.rawQuery("SELECT * FROM " + TABLE_UN, null);
        if (cursor.moveToFirst())
            b = false;
        else {
            cursor = rdb.rawQuery("SELECT * FROM " + TABLE_ING, null);
            if (cursor.moveToFirst())
                b = false;
        }
        cursor.close();
        rdb.close();
        return b;
    }

    public void removeUn() {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.delete(TABLE_UN, "", null);
        wdb.close();
    }

    public void clear() {
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.delete(TABLE_UN, "", null);
        wdb.delete(TABLE_ING, "", null);
        wdb.delete(TABLE_ED, "", null);
        wdb.close();
    }

    public ArrayList<Map<String, Object>> setOrdered(ArrayList<DishO> ordered) {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SQLiteDatabase wdb = getWritableDatabase();
        wdb.delete(TABLE_UN, "", null);
        wdb.delete(TABLE_ING, "", null);
        wdb.delete(TABLE_ED, "", null);
        for (int i = 0; i < ordered.size(); i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            ContentValues values = new ContentValues();
            DishO o = ordered.get(i);
            String dn = o.getName();
            String state = o.getState();
            int amt = o.getAmount();
            values.put(DNAME, dn);
            values.put(AMONT, amt);
            values.put(STATE, state);
            values.put(PRICE, o.getPrice());
            values.put(TYPEE, o.getType());
            values.put(CNAME, o.getcName());
            values.put(PHONE, o.getPhone());
            if (UNCOOK.equals(state)) {
                values.put(SENT_, 1);
                wdb.insert(TABLE_UN, null, values);
                map.put(DNAME, dn);
                map.put(AMONT, amt);
                list.add(map);
            } else if (CKING.equals(state)) {
                Cursor cursor = wdb.rawQuery("SELECT " + AMONT + " FROM " + TABLE_ING + " WHERE " + DNAME + "='" + dn + "'", null);
                if (cursor.moveToFirst()) {
                    values.put(AMONT, amt + cursor.getInt(0));
                    wdb.update(TABLE_ING, values, DNAME + "='" + dn + "'", null);
                } else
                    wdb.insert(TABLE_ING, null, values);
                cursor.close();
            } else if (FINISH.equals(state)) {
                Cursor cursor = wdb.rawQuery("SELECT " + AMONT + " FROM " + TABLE_ED + " WHERE " + DNAME + "='" + dn + "'", null);
                if (cursor.moveToFirst()) {
                    values.put(AMONT, amt + cursor.getInt(0));
                    wdb.update(TABLE_ED, values, DNAME + "='" + dn + "'", null);
                } else
                    wdb.insert(TABLE_ED, null, values);
                cursor.close();
            }
        }
        wdb.close();
        return list;
    }

    public List<Map<String, Object>> getOrderedUncook() {
        List<Map<String, Object>> ordered = new ArrayList<Map<String, Object>>();
        readTable(TABLE_UN, ordered);
        return ordered;
    }

    public List<Map<String, Object>> getOrderedList() {
        List<Map<String, Object>> ordered = new ArrayList<Map<String, Object>>();
        readTable(TABLE_UN, ordered);
        readTable(TABLE_ING, ordered);
        readTable(TABLE_ED, ordered);
        return ordered;
    }

    private void readTable(String tName, List<Map<String, Object>> ordered) {
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor cursor = rdb.rawQuery("SELECT * FROM " + tName, null);
        Map<String, Object> map;
        while (cursor.moveToNext()) {
            map = new HashMap<String, Object>();
            String name = cursor.getString(0);
            int amount = cursor.getInt(1);
            String state = cursor.getString(2);
            int price = cursor.getInt(3);
            String type = cursor.getString(4);
            Log.e("allOrdered", name + " " + amount + " " + state);
            map.put(DNAME, name);
            map.put(AMONT, amount);
            map.put(PRICE, "￥" + price);
            map.put(TYPEE, toType(type));
            map.put(STATE, state);
            ordered.add(map);
        }
        cursor.close();
        rdb.close();
    }

    private String toType(String type) {
        switch (type) {
            case "daily":
                return "Daily";
            case "HotDish":
                return "Hot dish";
            case "coldDish":
                return "Cold dish";
            case "drink":
                return "Drink";
            case "StapleFood":
                return "Staple food";
        }
        return null;
    }

    public ArrayList<DishO> getOrdered() {
        ArrayList<DishO> ordered = new ArrayList<DishO>();
        readTable(TABLE_UN, ordered);
        readTable(TABLE_ING, ordered);
        readTable(TABLE_ED, ordered);
        return ordered;
    }

    private void readTable(String tName, ArrayList<DishO> ordered) {
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor cursor = rdb.rawQuery("SELECT * FROM " + tName, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            int amount = cursor.getInt(1);
            String state = cursor.getString(2);
            int price = cursor.getInt(3);
            String type = cursor.getString(4);
            String cname = cursor.getString(5);
            String phone = cursor.getString(6);
            Log.e("allOrdered", name + " " + amount + " " + state);
            DishO o = new DishO(1, name, amount, state, price, type, phone, cname); // TODO table id
            ordered.add(o);
        }
        cursor.close();
        rdb.close();
    }

    public VIP getVip() {
        return vip;
    }

    public void setVip(VIP vip) {
        this.vip = vip;
    }

    public int getTotal() {
        int total = 0;
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor cursor = rdb.rawQuery("SELECT * FROM " + TABLE_UN, null);
        while (cursor.moveToNext())
            total += cursor.getInt(3) * cursor.getInt(1);
        cursor = rdb.rawQuery("SELECT * FROM " + TABLE_ING, null);
        while (cursor.moveToNext())
            total += cursor.getInt(3) * cursor.getInt(1);
        cursor = rdb.rawQuery("SELECT * FROM " + TABLE_ED, null);
        while (cursor.moveToNext())
            total += cursor.getInt(3) * cursor.getInt(1);
        cursor.close();
        rdb.close();
        return total;
    }

    public int getId() {
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor cursor = rdb.rawQuery("SELECT id FROM " + TABLE_ID, null);
        if (cursor.moveToFirst())
            return cursor.getInt(0);
        return 0;
    }

    public void setId(int id) {
        SQLiteDatabase wdb = getWritableDatabase();
        Cursor cursor = wdb.rawQuery("SELECT id FROM " + TABLE_ID, null);
        ContentValues values = new ContentValues();
        values.put(TBLID, id);
        if (cursor.moveToFirst())
            wdb.update(TABLE_ID, values, TBLID + "='" + cursor.getInt(0), null);
        else
            wdb.insert(TABLE_ID, null, values);
        cursor.close();
        wdb.close();
    }
}
