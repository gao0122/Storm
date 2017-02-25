package com.storm.storm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Storm on 4/27/16.
 *
 * @author Yuchao
 */
public class ActivityMain extends Activity {

    private static final int TL = Toast.LENGTH_LONG;
    private static final String PHONE_EE = "Please input your phone number before being a VIP.";
    private static final String PHONE_E0 = "Please input a valid number.";
    private static final String PHONE_E1 = "There is no such a phone number.";
    private static final String PHONE_E4 = "The format of your number is wrong.";
    private static final String PHONE_E2 = "This phone number is disabled.";
    private static final String PHONE_EXIST = "The number is already a VIP number, just click \"Order\".";
    private static final String CHEF = "Chef", BOSS = "Boss", MNG = "Manager", WTRS = "Waitress";
    private static final String PHONE = "phone", NAME = "name", TOTAL = "total", VC = "vc";
    private static final String DS_IMG = "dsImg", DS_PRICE = "dsPrice", DS_NAME = "dsName", DS_ = "ds";
    final private String[] helper = {null};
    private int times = 4; // staffs can only have 4 chances to input password
    private boolean clickedOrder, clickedVIP, clickedLogo; // true if user clicked a view
    private boolean valid; // true if it is a valid phone number
    private String num; // the number which is valid after checking
    private EditText phone;
    private Button order, vip;
    private ImageView logo;
    private HashMap<String, ArrayList<String>> dishImg = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<String>> dishName = new HashMap<String, ArrayList<String>>();
    private HashMap<String, ArrayList<Integer>> dishPrice = new HashMap<String, ArrayList<Integer>>();
    private HashMap<String, DishS> dishes = new HashMap<String, DishS>();

    /**
     * using handler to make a long toast which is not in the main UI thread
     *
     * @param v view parameter to get context
     * @param s toast content
     */
    public static void toast(final View v, final String s) {
        Handler handler = new Handler(v.getContext().getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                Toast.makeText(v.getContext(), s, TL).show();
            }
        };
        handler.sendMessage(handler.obtainMessage());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context a = this;
        sqlThread();

        clickedOrder = clickedVIP = false;

        vip = (Button) findViewById(R.id.buttonVIP);
        order = (Button) findViewById(R.id.buttonOrder);
        phone = (EditText) findViewById(R.id.edit_phone_main);
        logo = (ImageView) findViewById(R.id.logo_main);

        phone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        phone.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

        // go to order dish activity
        order.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!clickedOrder) {
                    final Intent i = new Intent(v.getContext(), ActivityDishOrder.class);
                    if (valid) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    ArrayList<String> list = StormSQL.getVIP(num);
                                    if (list.size() == 2) {
                                        Bundle bundle = new Bundle();
                                        bundle.putString(PHONE, num);
                                        bundle.putString(NAME, list.get(0));
                                        bundle.putString(TOTAL, list.get(1));
                                        while (helper[0] == null) ;
                                        bundle.putSerializable(DS_IMG, dishImg);
                                        bundle.putSerializable(DS_NAME, dishName);
                                        bundle.putSerializable(DS_PRICE, dishPrice);
                                        bundle.putSerializable(DS_, dishes);
                                        i.putExtras(bundle);
                                        startActivity(i);
                                    } else
                                        dialog(v, i, valid);
                                } catch (java.sql.SQLException e) {
                                    e.printStackTrace();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    } else // invalid input
                        dialog(v, i, valid);
                    clickedOrder = true; // after resumed automatically set to false
                }
            }
        });

        // click to apply for VIP, only valid phone can go to next activity
        vip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!clickedVIP) {
                    if (valid)
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    ArrayList<String> list = StormSQL.getVIP(num);
                                    if (list.size() == 0) {// if the number is not a VIP, goto apply
                                        clickedVIP = true; // after it resumed automatically set to false
                                        applyVIP();
                                    } else
                                        toast(v, PHONE_EXIST);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    else if (phone.getText().length() == 0) // no input
                        Toast.makeText(v.getContext(), PHONE_EE, TL).show();
                    else // input is not valid
                        Toast.makeText(v.getContext(), PHONE_E0, TL).show();
                }
            }
        });

        // dynamically check if input number is a valid phone
        phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                valid = false;
                num = s.toString();
                if (s.length() == 11) {
                    int x = checkNum();
                    if (x == -1)
                        Toast.makeText(a, PHONE_E1, TL).show();
                    else if (x == -4)
                        Toast.makeText(a, PHONE_E4, TL).show();
                    else if (x == -11)
                        Toast.makeText(a, PHONE_E2, TL).show();
                    else if (x == -42)
                        valid = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // login method for staffs and only staffs' phone number can get response
        logo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                if (!clickedLogo && valid) {
                    new Thread() {
                        @Override
                        public void run() {
                            clickedLogo = true;
                            HashMap<String, String> map = null;
                            try {
                                map = StormSQL.getStaff(num);
                                if (map.size() == 0) {
                                    clickedLogo = false;
                                    return;
                                }
                            } catch (ClassNotFoundException e) {
                                clickedLogo = false;
                                e.printStackTrace();
                            } catch (SQLException e) {
                                clickedLogo = false;
                                e.printStackTrace();
                            }
                            if (map != null && !map.get("type").equals(WTRS))
                                askPwd(v, map);
                        }
                    }.start();
                }
                return true;
            }
        });
    }

    /**
     * reset click boolean variable with false
     */
    @Override
    protected void onResume() {
        super.onResume();
        clickedOrder = clickedVIP = clickedLogo = false;
    }

    /**
     * check if the input phone number is a valid phone number
     *
     * @return a integer valid code
     */
    private int checkNum() {
        StormMsg msg = new StormMsg(true, num); // false for send a message, true for check if number is valid
        new Thread(msg).start();
        while (msg.getValid() == -100) ;
        return msg.getValid();
    }

    /**
     * send a verification message to the phone of this customer
     *
     * @return the random verification code string
     */
    private String sendMsg() {
        StormMsg msg = new StormMsg(false, num); // false for send a message, true for check if number is valid
        new Thread(msg).start();
        while (msg.getRdCode() == null) ;
        return msg.getRdCode();

    }

    /**
     * apply for VIP and start that activity
     */
    private void applyVIP() {
        Bundle bundle = new Bundle();
        Intent i = new Intent(this, ActivityVIP.class);
        bundle.putString(VC, sendMsg());
        bundle.putString(PHONE, num);
        bundle.putSerializable(DS_IMG, dishImg);
        bundle.putSerializable(DS_NAME, dishName);
        bundle.putSerializable(DS_PRICE, dishPrice);
        bundle.putSerializable(DS_, dishes);
        i.putExtras(bundle);
        startActivity(i);
    }

    /***
     * make a dialog to customer to make more decisions by handler
     *
     * @param v     the View clicked or processed by user
     * @param i     the Intent which is going to be started if user chose to just go to that activity
     * @param input true if user's input is valid, the same as "valid"
     */
    private void dialog(final View v, final Intent i, final boolean input) {
        Handler handler = new Handler(v.getContext().getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);
                String t = "Your number is " + num + " which is not VIP here, only VIP can accumulate credits and get discounts";
                String s = input ? t : "You are ordering as a casual customer which will not get VIP discounts";
                String neutral = input ? "Change number!" : "Input number and be VIP!";
                builder.setMessage(s);
                builder.setTitle("Attention!");
                if (input)
                    builder.setPositiveButton("Be VIP!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clickedOrder = false;
                            dialog.dismiss();
                            applyVIP();
                        }
                    });
                builder.setNegativeButton("Just go to order!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clickedOrder = false;
                        dialog.dismiss();
                        Bundle bundle = new Bundle();
                        bundle.putString(PHONE, "0");
                        bundle.putString(NAME, "Casual Customer");
                        bundle.putString(TOTAL, "0");
                        while (helper[0] == null) ;
                        bundle.putSerializable(DS_IMG, dishImg);
                        bundle.putSerializable(DS_NAME, dishName);
                        bundle.putSerializable(DS_PRICE, dishPrice);
                        bundle.putSerializable(DS_, dishes);
                        i.putExtras(bundle);
                        startActivity(i);
                    }
                });
                builder.setNeutralButton(neutral, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clickedOrder = false;
                        dialog.dismiss();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        clickedOrder = false;
                    }
                });
                builder.create().show();
            }
        };
        handler.sendMessage(handler.obtainMessage());
    }

    /**
     * make a dialog to ask staff to input passwords
     *
     * @param v   view parameter
     * @param map staffs information map
     */
    private void askPwd(final View v, final HashMap<String, String> map) {
        Handler handler = new Handler(v.getContext().getMainLooper()) {
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityMain.this);
                final EditText pwdTxt = new EditText(ActivityMain.this);
                pwdTxt.setTransformationMethod(PasswordTransformationMethod.getInstance());
                builder.setTitle("Please input your password");
                builder.setView(pwdTxt);
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clickedLogo = false;
                        if (times > 0 && map.get("pwd").equals(pwdTxt.getText().toString())) {
                            start(v, map.get("type"), map.get("phone"), map.get("name"));
                            dialog.dismiss();
                        } else if (times == 1) {
                            toast(v, "Sorry, please try again after 30 minutes.");
                            dialog.dismiss();
                        } else
                            toast(v, "Wrong password, you still can try " + --times + " times.");
                    }
                });
                builder.setNegativeButton("Oh no, I don't know anything", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clickedLogo = false;
                        dialog.dismiss();
                    }
                });
                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        clickedLogo = false;
                    }
                });
                builder.show();
            }
        };
        handler.sendMessage(handler.obtainMessage());
    }

    /**
     * start staff activities differed by different type of staffs
     *
     * @param v     view parameter
     * @param type  staff type string
     * @param phone staff phone number
     * @param name  staff name
     */
    private void start(View v, String type, String phone, String name) {
        Intent i = null;
        Bundle bundle = new Bundle();
        bundle.putString(NAME, name);
        bundle.putString(PHONE, phone);
        while (helper[0] == null) ;
        bundle.putSerializable(DS_, dishes);
        switch (type) {
            case CHEF:
                i = new Intent(v.getContext(), ActivityChef.class);
                break;
            case BOSS:
                i = new Intent(v.getContext(), ActivityBoss.class);
                break;
            case MNG:
                i = new Intent(v.getContext(), ActivityManager.class);
                break;
        }
        if (i != null) {
            i.putExtras(bundle);
            startActivity(i);
        } else
            toast(v, "Unexpected error happened, please check your network.");
    }

    /**
     * get dishes information from database
     * the helper value is to identify if this thread is finished or not
     */
    private void sqlThread() {
        new Thread() {
            @Override
            public void run() {
                final String IMG_PATH = "http://wsq.gaoyuchao.com/imgs/";

                final String DAILY_ = "daily";
                final String COLD_ = "coldDish";
                final String HOT_ = "HotDish";
                final String STAPLE_ = "StapleFood";
                final String DRINK_ = "drink";
                dishName.put(DAILY_, new ArrayList<String>());
                dishName.put(COLD_, new ArrayList<String>());
                dishName.put(HOT_, new ArrayList<String>());
                dishName.put(STAPLE_, new ArrayList<String>());
                dishName.put(DRINK_, new ArrayList<String>());
                dishPrice.put(DAILY_, new ArrayList<Integer>());
                dishPrice.put(COLD_, new ArrayList<Integer>());
                dishPrice.put(HOT_, new ArrayList<Integer>());
                dishPrice.put(STAPLE_, new ArrayList<Integer>());
                dishPrice.put(DRINK_, new ArrayList<Integer>());
                dishImg.put(DAILY_, new ArrayList<String>());
                dishImg.put(COLD_, new ArrayList<String>());
                dishImg.put(HOT_, new ArrayList<String>());
                dishImg.put(STAPLE_, new ArrayList<String>());
                dishImg.put(DRINK_, new ArrayList<String>());

                try {
                    ArrayList<DishS> dishList = StormSQL.getDishes();
                    for (int i = 0; i < dishList.size(); i++) {
                        DishS ds = dishList.get(i);
                        String name = ds.getName();
                        String type = ds.getType();
                        dishes.put(name, ds);
                        dishName.get(type).add(name);
                        dishPrice.get(type).add(ds.getPrice());
                        dishImg.get(type).add(IMG_PATH + ds.getImg());
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                helper[0] = "";
            }
        }.start();
    }
}
