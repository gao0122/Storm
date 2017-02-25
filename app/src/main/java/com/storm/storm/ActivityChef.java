package com.storm.storm;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Storm on 5/3/16.
 *
 * @author Yuchao
 */
public class ActivityChef extends Activity {

    private final static String NAME = "name";
    private final static String PHONE = "phone";
    private final static String TID = "tid", ID = "id";
    private final static String DNAME = "dsName";
    private final static String AMOUNT = "amount";
    private final static String UNCOOK = "uncook";
    private final static String CKING = "cooking";
    private final static String FINISH = "finished";
    private static final String DS_ = "ds";

    private static Activity a;
    private static Firebase FB;
    private static ListView list_cooking, list_uncook;
    private static SimpleAdapter adapter_ing, adapter_un;
    private static List<Map<String, Object>> mListCook = null, mListUnCook = null;

    private String name, phone; // chef name and phone number
    private HashMap<String, DishS> dishes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chef);

        a = this;
        Firebase.setAndroidContext(a);
        FB = StormBase.CHEF_FB;

        Bundle bundle = getIntent().getExtras();
        name = bundle.getString(NAME);
        Log.e("Chef name", name);
        phone = bundle.getString(PHONE);
        dishes = (HashMap<String, DishS>) bundle.getSerializable(DS_);

        list_cooking = (ListView) findViewById(R.id.list_cooking);
        list_uncook = (ListView) findViewById(R.id.list_uncook);

        getListThread(false);
        while (mListCook == null) ;

        adapter_ing = new SimpleAdapter(this, mListCook, R.layout.chef_item,
                new String[]{TID, DNAME, AMOUNT},
                new int[]{R.id.dish_table_chef_item, R.id.dish_name_chef_item, R.id.dish_amount_chef_item});
        adapter_un = new SimpleAdapter(this, mListUnCook, R.layout.chef_item,
                new String[]{TID, DNAME, AMOUNT},
                new int[]{R.id.dish_table_chef_item, R.id.dish_name_chef_item, R.id.dish_amount_chef_item});
        list_cooking.setAdapter(adapter_ing);
        list_uncook.setAdapter(adapter_un);
        adapter_ing.notifyDataSetChanged();
        adapter_un.notifyDataSetChanged();

        // data change listener to listen if the dish list is edited by customer
        FB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                long value = (long) snapshot.getValue();
                if (value == 1) {
                    FB.setValue(0);
                    getListThread(true);
                    Toast.makeText(a, "Refresh in 1 second automatically.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
                String e = "The read failed, " + error.getMessage();
                Log.e("Firebase error", e);
                Toast.makeText(a, e, Toast.LENGTH_LONG).show();
            }
        });

        // click to make the state of that dish be cooking
        list_uncook.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Map<String, Object> map = mListUnCook.get(position);
                int t = (int) map.get(AMOUNT);
                mListUnCook.remove(position);
                if (t > 0) {
                    mListCook.add(map);
                    adapter_un.notifyDataSetChanged();
                    adapter_ing.notifyDataSetChanged();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                StormSQL.updateState((int) map.get(ID), CKING);
                                StormBase.TABLE1_FB.setValue(0);
                                StormBase.TABLE1_FB.setValue(1);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            }
        });

        // click to make the state of that dish be finishes
        list_cooking.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Map<String, Object> map = mListCook.get(position);
                int t = (int) map.get(AMOUNT);
                mListCook.remove(position);
                if (t > 0) {
                    adapter_ing.notifyDataSetChanged();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                StormSQL.updateState((int) map.get(ID), FINISH);
                                StormBase.TABLE1_FB.setValue(0);
                                StormBase.TABLE1_FB.setValue(1);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            }
        });
    }

    /**
     * get or update latest dish lists from database
     *
     * @param ntf true if mLists have been initialized
     */
    private void getListThread(final boolean ntf) {
        new Thread() {
            @Override
            public void run() {
                try {
                    if (ntf) {
                        mListUnCook.clear();
                        mListCook.clear();
                        mListUnCook.addAll(StormSQL.getTablesByChef(UNCOOK, dishes, phone));
                        mListCook.addAll(StormSQL.getTablesByChef(CKING, dishes, phone));
                        Handler handler = new Handler(a.getMainLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                adapter_ing.notifyDataSetChanged();
                                adapter_un.notifyDataSetChanged();
                            }
                        };
                        handler.sendMessage(handler.obtainMessage());
                    } else {
                        mListUnCook = StormSQL.getTablesByChef(UNCOOK, dishes, phone);
                        mListCook = StormSQL.getTablesByChef(CKING, dishes, phone);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
