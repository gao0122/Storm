package com.storm.storm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Storm on 5/3/16.
 *
 * @author Yuchao
 */
public class ActivityVIP extends Activity {

    private static final String
            DS_IMG = "dsImg",
            DS_PRICE = "dsPrice",
            DS_NAME = "dsName",
            DS_ = "ds";
    private final int TL = Toast.LENGTH_LONG;

    private String phone, vc; // vc is verification code
    private EditText phoneET, vcET, nameET;
    private Button back, confirm;
    private HashMap<String, ArrayList<String>> dishImg;
    private HashMap<String, ArrayList<String>> dishName;
    private HashMap<String, ArrayList<Integer>> dishPrice;
    private HashMap<String, DishS> dishes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vip);

        Bundle bundle = getIntent().getExtras();
        phone = (String) bundle.get("phone");
        vc = (String) bundle.get("vc");
        dishes = (HashMap<String, DishS>) bundle.get("ds");
        dishImg = (HashMap) bundle.get("dsImg");
        dishName = (HashMap) bundle.get("dsName");
        dishPrice = (HashMap) bundle.get("dsPrice");

        nameET = (EditText) findViewById(R.id.edit_name_vip);
        phoneET = (EditText) findViewById(R.id.edit_phone_vip);
        vcET = (EditText) findViewById(R.id.edit_vc);
        confirm = (Button) findViewById(R.id.button_confirm_vip);
        back = (Button) findViewById(R.id.button_back_vip);

        phoneET.setText(phone);
        phoneET.setFocusable(true);
        phoneET.setEnabled(false);
        nameET.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        vcET.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        // confirm to go to order dish activity
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (vc.equals(vcET.getText().toString())) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                if (StormSQL.addVIP(nameET.getText().toString(), phone))
                                    startAndFinish(v);
                                else
                                    ActivityMain.toast(v, "Unexpected error happened, please check your network.");
                            } catch (SQLException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                } else
                    Toast.makeText(v.getContext(), "Wrong verification code, please try again.", TL).show();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) v.getContext()).finish();
            }
        });
    }

    /**
     * start dish order activity and finish this activity
     *
     * @param v related view
     */
    private void startAndFinish(View v) {
        Intent i = new Intent(v.getContext(), ActivityDishOrder.class);
        Bundle bundle = new Bundle();
        bundle.putString("name", nameET.getText().toString());
        bundle.putString("phone", phone);
        bundle.putString("total", "0");
        bundle.putSerializable(DS_IMG, dishImg);
        bundle.putSerializable(DS_NAME, dishName);
        bundle.putSerializable(DS_PRICE, dishPrice);
        bundle.putSerializable(DS_, dishes);
        i.putExtras(bundle);
        startActivity(i);
        ((Activity) v.getContext()).finish();
    }
}
