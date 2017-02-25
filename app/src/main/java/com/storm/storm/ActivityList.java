package com.storm.storm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by Storm on 5/3/16.
 */
public class ActivityList extends Activity {

    private static final int TL = Toast.LENGTH_LONG;
    private static final String COOKING_ = "The dish has already been cooking";
    private static final String TOTAL_ = "Total: ￥";
    private static final String VIP = "vip", TABLE = "storm.db";
    private static final String DNAME = "dname";
    private static final String PRICE = "price";
    private static final String STATE = "state";
    private static final String TYPE = "type";
    private static final String AMOUNT = "amount";
    private static final String UNCOOK = "uncook";

    private static VIP vip;
    private static Table table;
    private static Firebase FB;
    private static Context a;

    private static TextView txtTotal;
    private static Button btnOK;
    private static ListView listView;
    private static SimpleAdapter adapter;
    private static List<Map<String, Object>> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        a = this;
        Firebase.setAndroidContext(a);
        FB = StormBase.TABLE1_FB;
        FB.setValue(0);

        Bundle bundle = getIntent().getExtras();
        vip = bundle.getParcelable(VIP);
        table = new Table(this, TABLE, null, 1);
        table.setVip(vip);

        txtTotal = (TextView) findViewById(R.id.textView_totalAmount);
        btnOK = (Button) findViewById(R.id.button_ok_list);
        listView = (ListView) findViewById(R.id.listView_list);

        updateTotal();

        mList = table.getOrderedList();
        adapter = new SimpleAdapter(this, mList, R.layout.list_item,
                new String[]{DNAME, STATE, TYPE, PRICE, AMOUNT},
                new int[]{
                        R.id.dish_name_list_item,
                        R.id.dish_state_list_item,
                        R.id.dish_type_list_item,
                        R.id.dish_price_list_item,
                        R.id.dish_amount_list_item
                });
        listView.setAdapter(adapter);

        // click to delete one dish
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = mList.get(position);
                String state = (String) map.get(STATE);
                if (UNCOOK.equals(state)) {
                    String dsName = (String) map.get(DNAME);
                    int t;
                    if (dsName != null) {
                        if ((t = (int) map.get(AMOUNT) - 1) >= 0) {
                            if (table.addDish(dsName, -1, 0, null, null, null) < 0) {
                                Toast.makeText(view.getContext(), COOKING_, TL).show();
                            } else {
                                if (t > 0)
                                    mList.get(position).put(AMOUNT, t);
                                else if (t == 0)
                                    mList.remove(position);
                                updateTotal();
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        });

        // long click to edit the dish amount
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final Map<String, Object> map = mList.get(position);
                String state = (String) map.get(STATE);
                if (UNCOOK.equals(state)) {
                    final int amount = (int) map.get(AMOUNT);
                    final String name = (String) map.get(DNAME);
                    final String title = "Input the amount of " + name + " you want: ";
                    final EditText et = new EditText(view.getContext());

                    // customer can only input positive number here
                    et.setInputType(InputType.TYPE_CLASS_NUMBER);
                    et.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
                    et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                    new AlertDialog.Builder(view.getContext()).setTitle(title).setView(et).setPositiveButton("okay",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (et.getText().length() > 0) {
                                        int amt = Integer.valueOf(et.getText().toString());
                                        int t = amt - amount;
                                        String dsName = (String) map.get(DNAME);
                                        if (table.addDish(dsName, t, 0, null, null, null) < 0) {
                                            Toast.makeText(a, COOKING_, TL);
                                        } else {
                                            if (amt > 0)
                                                mList.get(position).put(AMOUNT, amt);
                                            else
                                                mList.remove(position);
                                            updateTotal();
                                            adapter.notifyDataSetChanged();
                                        }
                                        dialog.dismiss();
                                    }
                                }
                            }).setNegativeButton("cancel",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).setNeutralButton("delete it",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int t = (int) map.get(AMOUNT);
                                    String dsName = (String) map.get(DNAME);
                                    if (table.addDish(dsName, -t, 0, null, null, null) < 0)
                                        Toast.makeText(a, COOKING_, TL);
                                    else {
                                        mList.remove(position);
                                        updateTotal();
                                        adapter.notifyDataSetChanged();
                                    }
                                    dialog.dismiss();
                                }
                            }).show();
                }
                return true;
            }
        });

        // ok to confirm the edition and update them to database
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            StormSQL.updateTable(table.getOrdered());
                            table.setAllSent();
                            StormBase.CHEF_FB.setValue(0);
                            StormBase.CHEF_FB.setValue(1);
                            Handler handler = new Handler(v.getContext().getMainLooper()) {
                                @Override
                                public void handleMessage(android.os.Message msg) {
                                    super.handleMessage(msg);
                                    Toast.makeText(v.getContext(), "Sent successfully.", TL).show();
                                    finish();
                                }
                            };
                            handler.sendMessage(handler.obtainMessage());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }

    /**
     * start a new thread to update total amount of this consumption
     */
    private void updateTotal() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Map<String, List<Object>> map = StormSQL.getDiscount();
                    final int total = table.getTotal();
                    vip.setConsumption(total);
                    float pay = total * vip.getDiscount(
                            (int) map.get("silver").get(0), (float) map.get("silver").get(1),
                            (int) map.get("golden").get(0), (float) map.get("golden").get(1),
                            (int) map.get("platinum").get(0), (float) map.get("platinum").get(1),
                            (float) map.get("normal").get(1)
                    );
                    DecimalFormat decimalFormat = new DecimalFormat(".00");
                    final String pays = decimalFormat.format(pay);
                    Handler handler = new Handler(a.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            StringBuffer s = new StringBuffer(TOTAL_ + pays);
                            if (!vip.getPhone().equals("0"))
                                s.append("\n(before discount is ￥" + total + ")");
                            if (total == 0)
                                s = new StringBuffer("0");
                            txtTotal.setText(s);
                        }
                    };
                    handler.sendMessage(handler.obtainMessage());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
