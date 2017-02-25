package com.storm.storm;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.Toast;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Storm on 5/3/16.
 */
public class ActivityManager extends Activity {

    private static final int TL = Toast.LENGTH_LONG;
    private static final int TS = Toast.LENGTH_SHORT;
    private static final String IMG_PATH = "http://wsq.gaoyuchao.com/imgs/";
    private static final String EDIT_MENU = "Edit menu";
    private static final String ALLOCATE = "Allocate chef";
    private static final String SET_DISCNT = "Set discount";
    private static final String VIEW_CMT = "View comments";
    private static final String NAME = "name";
    private static final String PHONE = "phone";
    private static final String DNAME = "dname";
    private static final String DTYPE = "dtype";
    private static final String DCHEF = "dchef";
    private static final String DPRICE = "dprice";
    private static final String CNAME = "cname";
    private static final String CPHONE = "cphone";

    private static boolean dishSelected, chefSelected;
    private static String name, phone, chefEd;
    private static InputStream in = null;
    private static ArrayList<String> listDishName;
    private static ArrayList<Integer> listDishPos;

    private static ImageView iv;
    private static EditText editName, editType, editPrice;
    private static EditText editAmount1, editAmount2, editAmount3, editDisc1, editDisc2, editDisc3, editDiscCC;
    private static Button btnCfmChef, btnResetChef, btnCfm1, btnCfm2, btnCfm3, btnCfmCC, btnCfm, btnDel;
    private static TabHost tabHost;
    private static ListView listViewDish, listViewChef, listViewEd, listViewEdit;
    private static List<Map<String, Object>> mListDish, mListChef, mListEd, mListEdit;
    private static SimpleAdapter adapterDish, adapterChef, adapterEd, adapterEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        // get dish list from database
        new Thread() {
            @Override
            public void run() {
                try {
                    mListEdit = mListDish = StormSQL.getDishList();
                    mListChef = StormSQL.getChefList();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        Bundle bundle = getIntent().getExtras();
        name = bundle.getString(NAME);
        phone = bundle.getString(PHONE);
        Toast.makeText(this, "Hello, Manager " + name + "! Your number is " + phone + ".", TL).show();
        listDishName = new ArrayList<>();
        listDishPos = new ArrayList<>();

        iv = (ImageView) findViewById(R.id.imageView_edit);
        listViewEdit = (ListView) findViewById(R.id.listView_edit);
        listViewEd = (ListView) findViewById(R.id.listView_ed_chef_manager);
        listViewChef = (ListView) findViewById(R.id.listView_dish_chef_manager);
        listViewDish = (ListView) findViewById(R.id.listView_dish_manager);
        btnCfmChef = (Button) findViewById(R.id.button_confirm_allocateChef);
        btnResetChef = (Button) findViewById(R.id.button_reset_allocateChef);
        btnCfm = (Button) findViewById(R.id.button_cfm_edit_manager);
        btnDel = (Button) findViewById(R.id.button_delete_edit_manager);
        btnCfm1 = (Button) findViewById(R.id.button_cfm_1);
        btnCfm2 = (Button) findViewById(R.id.button_cfm_2);
        btnCfm3 = (Button) findViewById(R.id.button_cfm_3);
        btnCfmCC = (Button) findViewById(R.id.button_cfm_cc);
        editAmount1 = (EditText) findViewById(R.id.editText_amount_1);
        editAmount2 = (EditText) findViewById(R.id.editText_amount_2);
        editAmount3 = (EditText) findViewById(R.id.editText_amount_3);
        editDisc1 = (EditText) findViewById(R.id.editText_discount_1);
        editDisc2 = (EditText) findViewById(R.id.editText_discount_2);
        editDisc3 = (EditText) findViewById(R.id.editText_discount_3);
        editDiscCC = (EditText) findViewById(R.id.editText_discount_cc);
        editAmount1.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        editAmount2.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        editAmount3.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        editName = (EditText) findViewById(R.id.edit_name);
        editType = (EditText) findViewById(R.id.edit_type);
        editPrice = (EditText) findViewById(R.id.edit_price);
        editName.setText("");
        editType.setText("");
        editPrice.setText("");
        editPrice.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

        // setup the tabhost view for multiple view pages
        tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec(EDIT_MENU).setContent(R.id.ChangeMenu).setIndicator(EDIT_MENU));
        tabHost.addTab(tabHost.newTabSpec(ALLOCATE).setContent(R.id.AllocateChef).setIndicator(ALLOCATE));
        tabHost.addTab(tabHost.newTabSpec(SET_DISCNT).setContent(R.id.SetDiscount).setIndicator(SET_DISCNT));
        tabHost.addTab(tabHost.newTabSpec(VIEW_CMT).setContent(R.id.ViewComment_manager).setIndicator(VIEW_CMT));
        tabHost.setCurrentTab(1);

        mListEd = new ArrayList<Map<String, Object>>();
        resetAllocateChef();
        while (mListChef == null) ;
        Map<String, Object> tMap = new HashMap<String, Object>();
        tMap.put("dname", "Name");
        tMap.put("dtype", "Type");
        tMap.put("dprice", "Price(￥)");
        if (!(mListEdit.get(0) != null && mListEdit.get(0).equals(tMap)))
            mListEdit.add(0, tMap);
        adapterEdit = new SimpleAdapter(this, mListEdit, R.layout.edit_item,
                new String[]{DNAME, DTYPE, DPRICE},
                new int[]{R.id.textView_edit_name_item, R.id.textView_edit_type_item, R.id.textView_edit_price_item});
        adapterDish = new SimpleAdapter(this, mListDish, R.layout.manager_chef_dish_list,
                new String[]{DNAME, DTYPE, DCHEF},
                new int[]{R.id.textChef_dish_name, R.id.textChef_dish_type, R.id.textChef_dish_chef});
        adapterChef = new SimpleAdapter(this, mListChef, R.layout.manager_chef_list_item,
                new String[]{CNAME, CPHONE},
                new int[]{R.id.textChef_name, R.id.textChef_phone});
        adapterEd = new SimpleAdapter(this, mListEd, R.layout.manager_chef_ed_list,
                new String[]{DNAME},
                new int[]{R.id.textViewEd});
        listViewEdit.setAdapter(adapterEdit);
        listViewEd.setAdapter(adapterEd);
        listViewDish.setAdapter(adapterDish);
        listViewChef.setAdapter(adapterChef);
        adapterChef.notifyDataSetChanged();
        adapterDish.notifyDataSetChanged();
        adapterEdit.notifyDataSetChanged();

        // click a item to delete
        listViewEd.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = mListDish.get(position);
                String dname = (String) map.get(DNAME);
                listDishPos.remove((Object) position);
                listDishName.remove(dname);
                mListEd.remove(position);
                adapterEd.notifyDataSetChanged();
            }
        });

        // click a item to delete
        listViewDish.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = mListDish.get(position);
                String dname = (String) map.get(DNAME);
                if (!listDishName.contains(dname)) {
                    listDishName.add(dname);
                    listDishPos.add(position);
                    dishSelected = true;
                    mListEd.add(map);
                    adapterEd.notifyDataSetChanged();
                }
            }
        });

        // click a item to delete
        listViewChef.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = mListChef.get(position);
                chefEd = (String) map.get(CPHONE);
                chefSelected = true;
            }
        });

        // confirm to update these information to chef table of database
        btnCfmChef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (chefSelected && dishSelected)
                    new Thread() {
                        @Override
                        public void run() {
                            final StringBuffer s = new StringBuffer();
                            try {
                                for (int i = 0; i < listDishPos.size(); i++) {
                                    int dishPos = listDishPos.get(i);
                                    StormSQL.updateDish(listDishName.get(i), chefEd);
                                    Map<String, Object> map = mListDish.get(dishPos);
                                    map.put(DCHEF, chefEd);
                                    mListDish.remove(dishPos);
                                    mListDish.add(dishPos, map);
                                }
                                s.append("Allocated successfully.");
                            } catch (SQLException e) {
                                e.printStackTrace();
                                s.append(e.getMessage());
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                                s.append(e.getMessage());
                            }
                            Handler handler = new Handler(v.getContext().getMainLooper()) {
                                @Override
                                public void handleMessage(Message msg) {
                                    super.handleMessage(msg);
                                    Toast.makeText(v.getContext(), s.toString(), TS).show();
                                    adapterDish.notifyDataSetChanged();
                                }
                            };
                            handler.sendMessage(handler.obtainMessage());
                        }
                    }.start();
                else if (dishSelected && !chefSelected)
                    Toast.makeText(v.getContext(), "Please select a chef.", TS).show();
                else if (chefSelected && !dishSelected)
                    Toast.makeText(v.getContext(), "Please select a dish.", TS).show();
                else
                    Toast.makeText(v.getContext(), "Please select chef and dish to allocate.", TS).show();
            }
        });

        btnResetChef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAllocateChef();
            }
        });

        // get VIP discount from database
        new Thread() {
            @Override
            public void run() {
                try {
                    final Map<String, List<Object>> map = StormSQL.getDiscount();
                    final int s = (int) map.get("silver").get(0);
                    final int g = (int) map.get("golden").get(0);
                    final int p = (int) map.get("platinum").get(0);
                    final float sf = (float) map.get("silver").get(1);
                    final float gf = (float) map.get("golden").get(1);
                    final float pf = (float) map.get("platinum").get(1);
                    final float f = (float) map.get("normal").get(1);
                    Handler handler = new Handler(getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            editAmount1.setText(String.valueOf(s));
                            editAmount2.setText(String.valueOf(g));
                            editAmount3.setText(String.valueOf(p));
                            editDisc1.setText(String.valueOf(sf));
                            editDisc2.setText(String.valueOf(gf));
                            editDisc3.setText(String.valueOf(pf));
                            editDiscCC.setText(String.valueOf(f));
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

        btnCfm1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        int amt = Integer.parseInt(editAmount1.getText().toString());
                        try {
                            float f = Float.parseFloat(editDisc1.getText().toString());
                            try {
                                StormSQL.updateDiscount("silver", amt, f);
                                toast("Updated silver successfully.", TS);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            toast("Invalid discount number.", TS);
                        }
                    }
                }.start();
            }
        });

        btnCfm2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        int amt = Integer.parseInt(editAmount2.getText().toString());
                        try {
                            float f = Float.parseFloat(editDisc2.getText().toString());
                            try {
                                StormSQL.updateDiscount("golden", amt, f);
                                toast("Updated golden successfully.", TS);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            toast("Invalid discount number.", TS);
                        }
                    }
                }.start();
            }
        });

        btnCfm3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        int amt = Integer.parseInt(editAmount3.getText().toString());
                        try {
                            float f = Float.parseFloat(editDisc3.getText().toString());
                            try {
                                StormSQL.updateDiscount("platinum", amt, f);
                                toast("Updated platinum successfully.", TS);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            toast("Invalid discount number.", TS);
                        }
                    }
                }.start();
            }
        });

        // confirm for daily discount
        btnCfmCC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            float f = Float.parseFloat(editDiscCC.getText().toString());
                            try {
                                StormSQL.updateDiscount("normal", 0, f);
                                toast("Updated normal successfully.", TS);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            toast("Invalid discount number.", TS);
                        }
                    }
                }.start();
            }
        });

        // click to choose the dish and set its information to the main UI
        final String[] dname = new String[]{null};
        listViewEdit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = mListEdit.get(position);
                dname[0] = (String) map.get(DNAME);
                final String type = (String) map.get(DTYPE);
                final int price = (int) map.get(DPRICE);
                editName.setText(dname[0]);
                editType.setText(type);
                editPrice.setText(String.valueOf(price));
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            final Bitmap bm = StormImg.getBm(getApplicationContext(),
                                    IMG_PATH + StormSQL.getDishImg(dname[0]));
                            Handler handler = new Handler(getMainLooper()) {
                                @Override
                                public void handleMessage(Message msg) {
                                    super.handleMessage(msg);
                                    iv.setImageBitmap(bm);
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
        });

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImgActivity();
            }
        });

        // confirm button of edit dishes page
        btnCfm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newName = editName.getText().toString();
                final String type = editType.getText().toString();
                final String priceStr = editPrice.getText().toString();
                int price;
                if (!"".equals(priceStr)) {
                    price = Integer.parseInt(priceStr);
                    if (dname[0] != null) {
                        if (!"".equals(newName) && !"".equals(type)) {
                            final int finalPrice = price;
                            new Thread() {
                                @Override
                                public void run() {
                                    try {
                                        StormSQL.updateDish(dname[0], newName, rawType(type), finalPrice);
                                        toast("Updated successfully.", TL);
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        } else
                            Toast.makeText(v.getContext(), "Please input something.", TS);
                    } else if ("".equals(newName) || "".equals(type)) {
                        Toast.makeText(v.getContext(), "Please input something.", TS);
                    } else {
                        final int finalPrice = price;
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    String jpg = newName + ".jpg";
                                    StormSQL.addDish(newName, finalPrice, rawType(type), jpg, "0");
                                    if (uploadImage(jpg))
                                        toast("Added " + newName + " successfully.", TL);
                                    else
                                        toast("Dish added but there is a server error which makes image uploading failed.", TL);
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    }
                } else
                    Toast.makeText(v.getContext(), "Please input something.", TS);
            }
        });

        // delete one dish
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dname[0] == null)
                    Toast.makeText(v.getContext(), "Please choose a dish to delete.", TS);
                else
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                StormSQL.deleteDish(dname[0]);
                                toast("Deleted successfully.", TL);
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
            }
        });

    }

    /**
     * transfer showing type string to raw type string for database
     *
     * @param type type string for showing on the UI
     * @return raw type string for database
     */
    private String rawType(String type) {
        switch (type) {
            case "Drink":
                return "drink";
            case "Hot dish":
                return "HotDish";
            case "Cold dish":
                return "coldDish";
            case "Daily":
                return "daily";
            case "Staple Food":
                return "StapleFood";
        }
        return "";
    }

    /**
     * using handler to make a toast which is not in the main UI thread
     *
     * @param s toast content
     * @param t toast showing time length
     */
    private void toast(final String s, final int t) {
        Handler handler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(getApplicationContext(), s, t).show();
            }
        };
        handler.sendMessage(handler.obtainMessage());
    }

    /**
     * reset user's input in allocating chefs page
     */
    private void resetAllocateChef() {
        chefSelected = dishSelected = false;
        listDishName.clear();
        listDishPos.clear();
        mListEd.clear();
        if (adapterEd != null)
            adapterEd.notifyDataSetChanged();
    }

    /**
     * select images from Android system(Gallery or SD card)
     */
    private void selectImgActivity() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    /**
     * upload one image to FTP sever
     *
     * @param imgName the name of image which is going to be uploaded
     * @return true if uploaded successfully
     */
    private boolean uploadImage(String imgName) {
        return StormImg.uploadFile(in, imgName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            final Uri uri = data.getData();
            ContentResolver cr = this.getContentResolver();
            try {
                in = cr.openInputStream(uri);
                iv.setImageBitmap(BitmapFactory.decodeStream(in));
            } catch (Exception e) {
                Log.e("My Error", "Not image");
                e.printStackTrace();
            }
        }
    }

}
