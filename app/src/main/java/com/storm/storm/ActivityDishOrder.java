package com.storm.storm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Storm on 5/2/16.
 *
 * @author Yuchao
 */
public class ActivityDishOrder extends AppCompatActivity {

    private static final int TL = Toast.LENGTH_LONG;
    private static final int TS = Toast.LENGTH_SHORT;
    private static final int DISH_WIDTH = 5;
    private static final int DISH_HEIGHT = 3;

    private static final String COOKING_ = "The dish has already been cooking";
    private static final String DAILY = "Dailys", DAILY_ = "daily";
    private static final String COLDD = "Cold Dish", COLD_ = "coldDish";
    private static final String HOTDS = "Hot Dish", HOT_ = "HotDish";
    private static final String STAPL = "Staple Food", STAPLE_ = "StapleFood";
    private static final String DRINK = "Drink", DRINK_ = "drink";
    private static final String DNAME = "dname", STATE = "state", AMOUNT = "amount";
    private static final String RMB = " ￥", PAY = "Pay";
    private static final String NAME = "name", CONSU = "consumption";
    private static final String PHONE = "phone", TOTAL = "total";

    private static Firebase FB; // data change real-time listener
    private static int tid = 1; // table id
    private static Context a;
    private static VIP vip;
    private static Table table;
    private static boolean listChanged = false;
    private static HashMap<String, ArrayList<String>> dishImg;
    private static HashMap<String, ArrayList<String>> dishName;
    private static HashMap<String, ArrayList<Integer>> dishPrice;
    private static HashMap<String, DishS> dishes;
    private static Button clearAll, moreBtn, cmtBtn, payBtn, cfmBtn;
    private static ListView listView;
    private static SimpleAdapter adapter;
    private static List<Map<String, Object>> mList;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderdish);

        a = this;
        Firebase.setAndroidContext(a);
        FB = StormBase.TABLE1_FB;
        FB.setValue(0);

        clearAll = (Button) findViewById(R.id.button_clearAll);
        moreBtn = (Button) findViewById(R.id.button_list_orderdish);
        cfmBtn = (Button) findViewById(R.id.button_confirm_orderdish);
        cmtBtn = (Button) findViewById(R.id.button_comment_orderdish);
        payBtn = (Button) findViewById(R.id.button_pay_orderdish);
        listView = (ListView) findViewById(R.id.list_dishorder);

        Bundle bundle = getIntent().getExtras();
        dishes = (HashMap<String, DishS>) bundle.get("ds");
        dishImg = (HashMap) bundle.get("dsImg");
        dishName = (HashMap) bundle.get("dsName");
        dishPrice = (HashMap) bundle.get("dsPrice");
        vip = new VIP(bundle.getString(NAME),
                bundle.getString(PHONE),
                Integer.parseInt(bundle.getString(TOTAL)));
        bundle.clear();

        final StringBuffer WELCOME = new StringBuffer("Welcome");
        WELCOME.append("0".equals(vip.getPhone()) ? "!" : ", " + vip.getName() + "!");
        Toast.makeText(this, WELCOME.toString(), TL).show();

        table = new Table(this, "storm.db", null, 1);
        table.setVip(vip);

        updateTotal();
        mList = table.getOrderedNames();
        adapter = new SimpleAdapter(this, mList, R.layout.dish_list_item_dishorder,
                new String[]{DNAME, AMOUNT},
                new int[]{R.id.dish_name_list_item, R.id.dish_amount_list_item});

        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        // click to delete that dish from local database
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String, Object> map = mList.get(position);
                String dsName = (String) map.get(DNAME);
                int t;
                if (dsName != null) {
                    if ((t = (int) map.get(AMOUNT) - 1) >= 0) {
                        if (table.addDish(dsName, -1, 0, null, null, null) < 0) {
                            Toast.makeText(view.getContext(), COOKING_, TL).show();
                        } else {
                            listChanged = true;
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
        });

        // long click to edit the amount of that dish and update to local database
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                final Map<String, Object> map = mList.get(position);
                final int amount = (int) map.get(AMOUNT);
                final String name = (String) map.get(DNAME);
                if (name != null) {
                    final String title = "Input the amount of " + name + " you want: ";
                    final EditText et = new EditText(view.getContext());
                    et.setInputType(InputType.TYPE_CLASS_NUMBER);
                    et.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
                    et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                    // make a dialog for customer to make choices
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
                                            listChanged = true;
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
                                    String dsName = (String) map.get(DNAME);
                                    int t = (int) map.get(AMOUNT);
                                    if (table.addDish(dsName, -t, 0, null, null, null) < 0)
                                        Toast.makeText(a, COOKING_, TL);
                                    else {
                                        mList.remove(position);
                                        updateTotal();
                                        adapter.notifyDataSetChanged();
                                        listChanged = true;
                                    }
                                    dialog.dismiss();
                                }
                            }).show();
                } else
                    Toast.makeText(view.getContext(), COOKING_, TL).show();
                return true;
            }
        });

        // clear all the dishes in the local database
        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(v.getContext()).setTitle("Warning")
                        .setMessage("Are you sure to click all ordered dishes?").setPositiveButton("sure",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mList.clear();
                                adapter.notifyDataSetChanged();
                                table.removeUn();
                                updateTotal();
                                dialog.dismiss();
                            }
                        }).setNegativeButton("nope", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });

        // confirm to send the dish list to chef
        cfmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // only the list is edited can we send it to chef
                if (listChanged) {
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
                                    }
                                };
                                handler.sendMessage(handler.obtainMessage());
                                listChanged = false;
                            } catch (SQLException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }

                    }.start();
                } else
                    Toast.makeText(v.getContext(), "Nothing changed.", TS).show();
            }
        });

        // to check more information of ordered dish list
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(a, ActivityList.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("vip", vip);
                i.putExtras(bundle);
                startActivity(i);
            }
        });

        // click to write a comment
        cmtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), ActivityComment.class);
                i.putExtra("name", vip.getName());
                i.putExtra("phone", vip.getPhone());
                startActivity(i);
            }
        });

        // click to pay and only all dishes are finished can we make payments
        payBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (table.allFinished()) {
                    new Thread() {
                        @Override
                        public void run() {
                            Calendar calendar = Calendar.getInstance();
                            try {
                                for (DishO o : table.getOrdered())
                                    StormSQL.addCon(o.getName(), o.getPrice(), o.getAmount(), tid, o.getType(),
                                            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                            calendar.get(Calendar.DAY_OF_MONTH), new Time(new Date().getTime()),
                                            o.getcName(), o.getPhone());
                                StormSQL.updateVIPTotal(vip.getPhone(), vip.getConsumption());
                                table.clear();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                    Intent i = new Intent(a, ActivityPay.class);
                    i.putExtra(NAME, vip.getName());
                    i.putExtra(PHONE, vip.getPhone());
                    i.putExtra(TOTAL, vip.getTotal());
                    i.putExtra(CONSU, vip.getConsumption());
                    startActivity(i);
                } else
                    Toast.makeText(a, "Please pay until all dishes are finished.", TL).show();
            }
        });

        // data change listener to listen if the dish state is changed by chef or not,
        // if so we get the latest dish list from database
        FB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long value = (long) dataSnapshot.getValue();
                if (value == 1) {
                    FB.setValue(0);
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                mList.clear();
                                mList.addAll(table.setOrdered(StormSQL.getTablesById(tid)));
                                Handler handler = new Handler(a.getMainLooper()) {
                                    @Override
                                    public void handleMessage(android.os.Message msg) {
                                        super.handleMessage(msg);
                                        adapter.notifyDataSetChanged();
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

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                String e = "The read failed, " + firebaseError.getMessage();
                Log.e("Firebase error", e);
                Toast.makeText(a, e, Toast.LENGTH_LONG).show();
            }
        });

    }

    /**
     * this is make sure to show the latest dish list all the time
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            Log.e("resumed", mList.size() + "");
            mList.clear();
            mList.addAll(table.getOrderedUncook());
            adapter.notifyDataSetChanged();
        }
        updateTotal();
    }

    /**
     * update total money of this consumption
     */
    private void updateTotal() {
        int total = table.getTotal();
        vip.setConsumption(total);
        if (total > 0)
            payBtn.setText(PAY + RMB + total);
        else if (total == 0)
            payBtn.setText(PAY);
    }

    /**
     * A placeholder fragment containing dishes view
     */
    public static class DishesFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this fragment.
         */
        private static final String PAGE = "page";
        private TextView[][] names = new TextView[DISH_HEIGHT][DISH_WIDTH];
        private SubsamplingScaleImageView[][] imgs = new SubsamplingScaleImageView[DISH_HEIGHT][DISH_WIDTH];

        public DishesFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        public static DishesFragment newInstance(int sectionNumber) {
            DishesFragment fragment = new DishesFragment();
            Bundle args = new Bundle();
            args.putInt(PAGE, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.dishes, container, false);
            setImgNameView(rootView);
            setFragmentViews();
            return rootView;
        }

        /**
         * setup fragment views
         */
        private void setFragmentViews() {
            final int page = Integer.valueOf(getArguments().get(PAGE).toString());
            switch (page) {
                case 1:
                    setDishInfo(dishName, dishPrice, dishImg, DAILY_);
                    break;
                case 2:
                    setDishInfo(dishName, dishPrice, dishImg, COLD_);
                    break;
                case 3:
                    setDishInfo(dishName, dishPrice, dishImg, HOT_);
                    break;
                case 4:
                    setDishInfo(dishName, dishPrice, dishImg, STAPLE_);
                    break;
                case 5:
                    setDishInfo(dishName, dishPrice, dishImg, DRINK_);
                    break;
            }
        }

        /**
         * set dish information and show them on main UI
         *
         * @param dishNames  dish name map list differed by type
         * @param dishPrices dish price map list differed by type
         * @param dishImgs   dish image name string map list differed by type
         * @param type       dish type string
         */
        private void setDishInfo(final HashMap<String, ArrayList<String>> dishNames,
                                 final HashMap<String, ArrayList<Integer>> dishPrices,
                                 final HashMap<String, ArrayList<String>> dishImgs,
                                 final String type) {
            ArrayList<Integer> dishPrice = dishPrices.get(type);
            ArrayList<String> dishName = dishNames.get(type);
            final ArrayList<String> dishImg = dishImgs.get(type);
            for (int i = 0; i < dishName.size() && i < 15; i++) {
                final int finalI = i;
                final Bitmap[] bm = {null};
                final int m = i / DISH_WIDTH, n = i % DISH_WIDTH;
                final int dsPrice = dishPrice.get(i);
                final String dsName = dishName.get(i);
                final SubsamplingScaleImageView iv = imgs[m][n];
                TextView tv = names[m][n];
                tv.setTextColor(Color.DKGRAY);
                tv.setTextSize(12f);
                tv.setText(dsName + RMB + dsPrice);
                new Thread() {
                    @Override
                    public void run() {
                        bm[0] = StormImg.getBm(a, dishImg.get(finalI));
                        Handler handler = new Handler(a.getMainLooper()) {
                            @Override
                            public void handleMessage(android.os.Message msg) {
                                super.handleMessage(msg);
                                if (bm[0] != null)
                                    iv.setImage(ImageSource.bitmap(bm[0]));
                            }
                        };
                        handler.sendMessage(handler.obtainMessage());
                    }
                }.start();

                View.OnClickListener listener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addDishToList(dsName, 1, dsPrice, type);
                    }
                };
                View.OnLongClickListener ll = new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        String title = "Input the amount of " + dsName + " you want: ";
                        final EditText et = new EditText(v.getContext());
                        et.setInputType(InputType.TYPE_CLASS_NUMBER);
                        et.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
                        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
                        new AlertDialog.Builder(v.getContext()).setTitle(title).setView(et).setPositiveButton("okay",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (et.getText().length() > 0) {
                                            int amt = Integer.valueOf(et.getText().toString());
                                            if (amt > 0)
                                                addDishToList(dsName, amt, dsPrice, type);
                                            dialog.dismiss();
                                        }
                                    }
                                }).setNegativeButton("cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                        return true;
                    }
                };
                // click to add dishes to the dish list
                tv.setOnClickListener(listener);
                iv.setOnClickListener(listener);
                tv.setOnLongClickListener(ll);
                iv.setOnLongClickListener(ll);
            }
        }

        /**
         * add specific amount dish to the dish list
         *
         * @param dsName  dish name string
         * @param amt     amount number
         * @param dsPrice dish price
         * @param type    type string
         */
        private void addDishToList(String dsName, int amt, int dsPrice, String type) {
            int n, total;
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(DNAME, dsName);
            if ((n = table.addDish(dsName, amt, dsPrice, type,
                    vip.getPhone(), vip.getName())) > amt) {
                Map<String, Object> tm = new HashMap<String, Object>();
                tm.put(DNAME, dsName);
                tm.put(AMOUNT, n - amt);
                mList.remove(tm);
                map.put(AMOUNT, n);
                mList.add(map);
            } else {
                map.put(AMOUNT, amt);
                mList.add(map);
            }
            total = vip.getConsumption() + amt * dishes.get(dsName).getPrice();
            vip.setConsumption(total);
            payBtn.setText(PAY + RMB + String.valueOf(total));
            adapter.notifyDataSetChanged();
            listChanged = true;
        }

        private void setImgNameView(View rootView) {
            names[0][0] = (TextView) rootView.findViewById(R.id.name00);
            names[0][1] = (TextView) rootView.findViewById(R.id.name01);
            names[0][2] = (TextView) rootView.findViewById(R.id.name02);
            names[0][3] = (TextView) rootView.findViewById(R.id.name03);
            names[0][4] = (TextView) rootView.findViewById(R.id.name04);
            names[1][0] = (TextView) rootView.findViewById(R.id.name10);
            names[1][1] = (TextView) rootView.findViewById(R.id.name11);
            names[1][2] = (TextView) rootView.findViewById(R.id.name12);
            names[1][3] = (TextView) rootView.findViewById(R.id.name13);
            names[1][4] = (TextView) rootView.findViewById(R.id.name14);
            names[2][0] = (TextView) rootView.findViewById(R.id.name20);
            names[2][1] = (TextView) rootView.findViewById(R.id.name21);
            names[2][2] = (TextView) rootView.findViewById(R.id.name22);
            names[2][3] = (TextView) rootView.findViewById(R.id.name23);
            names[2][4] = (TextView) rootView.findViewById(R.id.name24);

            imgs[0][0] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img00);
            imgs[0][1] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img01);
            imgs[0][2] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img02);
            imgs[0][3] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img03);
            imgs[0][4] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img04);
            imgs[1][0] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img10);
            imgs[1][1] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img11);
            imgs[1][2] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img12);
            imgs[1][3] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img13);
            imgs[1][4] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img14);
            imgs[2][0] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img20);
            imgs[2][1] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img21);
            imgs[2][2] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img22);
            imgs[2][3] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img23);
            imgs[2][4] = (SubsamplingScaleImageView) rootView.findViewById(R.id.img24);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a DishesFragment (defined as a static inner class below).
            return DishesFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 5; // Show 5 total pages.
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return DAILY;
                case 1:
                    return COLDD;
                case 2:
                    return HOTDS;
                case 3:
                    return STAPL;
                case 4:
                    return DRINK;
            }
            return null;
        }
    }
}
