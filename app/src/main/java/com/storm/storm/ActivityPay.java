package com.storm.storm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by Storm on 5/3/16.
 */
public class ActivityPay extends Activity {

    private static final int TL = Toast.LENGTH_LONG;

    private static final String RMB = "￥";
    private static final String NAME = "name", CONSU = "consumption";
    private static final String PHONE = "phone", TOTAL = "total";

    private TextView textAmount;
    private Button btnBack;

    private VIP vip;
    private String name, phone;
    private int total, consu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Intent ti = getIntent();
        name = ti.getStringExtra(NAME);
        phone = ti.getStringExtra(PHONE);
        total = ti.getIntExtra(TOTAL, 0);
        consu = ti.getIntExtra(CONSU, -1);
        if (consu < 0)
            Toast.makeText(this, "Unexpected error happened, please check the network.", TL);
        else { // make payment and update the total amount to database
            textAmount = (TextView) findViewById(R.id.text_pay_amount_payment);
            btnBack = (Button) findViewById(R.id.button_back_payment);
            vip = new VIP(name, phone, total);

            new Thread() {
                @Override
                public void run() {
                    try {
                        Map<String, List<Object>> map = StormSQL.getDiscount();
                        vip.setConsumption(consu);
                        float pay = consu * vip.getDiscount(
                                (int) map.get("silver").get(0), (float) map.get("silver").get(1),
                                (int) map.get("golden").get(0), (float) map.get("golden").get(1),
                                (int) map.get("platinum").get(0), (float) map.get("platinum").get(1),
                                (float) map.get("normal").get(1)
                        );

                        DecimalFormat decimalFormat = new DecimalFormat(".00");
                        final String pays = decimalFormat.format(pay);
                        Handler handler = new Handler(getMainLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                textAmount.setText(RMB + pays + " ");
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

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
