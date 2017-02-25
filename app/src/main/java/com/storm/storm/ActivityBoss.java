package com.storm.storm;

import android.app.TabActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Storm on 5/8/16.
 *
 * @author River
 */
public class ActivityBoss extends TabActivity {

    private LineChart mLineChart;

    private List<String> listYear = new ArrayList<String>();
    private List<String> listMonth = new ArrayList<String>();
    private List<String> listDay = new ArrayList<String>();
    private List<String> listType = new ArrayList<String>();
    private Spinner mySpinnerY;
    private Spinner mySpinnerM;
    private Spinner mySpinnerD;
    private Spinner mySpinnerT;
    private ArrayAdapter<String> adapterY;
    private ArrayAdapter<String> adapterM;
    private ArrayAdapter<String> adapterD;
    private ArrayAdapter<String> adapterT;

    private TextView yearText;
    private TextView monthText;
    private TextView dayText;
    private TextView typeText;

    private ArrayList<Integer> price = new ArrayList<Integer>();
    private List<Map<String, Object>> cmtList;
    private SimpleAdapter adapterCmt, adapterFavor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss);

        final TabHost tabHost = this.getTabHost();

        TabSpec DataAnalysis = tabHost.newTabSpec("Data Analysis").setIndicator("Data Analysis").setContent(R.id.dataAnalysis_boss);
        tabHost.addTab(DataAnalysis);

        TabSpec ViewComments = tabHost.newTabSpec("View Comments").setIndicator("View Comments").setContent(R.id.viewComment_boss);
        tabHost.addTab(ViewComments);

        TabSpec ViewFavorite = tabHost.newTabSpec("Favorite Dishes").setIndicator("Favorite Dishes").setContent(R.id.viewFavorite_boss);
        tabHost.addTab(ViewFavorite);


        ListView listView = (ListView) findViewById(R.id.listView4);
        cmtList = new ArrayList<>();
        adapterCmt = new SimpleAdapter(this, cmtList, R.layout.comment_item,
                new String[]{"cname", "type", "year", "month", "day", "content"},
                new int[]{
                        R.id.comment_name,
                        R.id.comment_type,
                        R.id.comment_year,
                        R.id.comment_month,
                        R.id.comment_day,
                        R.id.comment_content,
                });
        listView.setAdapter(adapterCmt);
        adapterCmt.notifyDataSetChanged();

        mLineChart = (LineChart) findViewById(R.id.lineChart);
        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        mLineChart.setDescription("Incomes");
        dataAnalysis(price);

        //year
        /* choice menu start */
        listYear.add("Year");
        listYear.add("2016");
        listYear.add("2015");
        listYear.add("2014");
        listYear.add("2013");
        yearText = (TextView) findViewById(R.id.year);
        mySpinnerY = (Spinner) findViewById(R.id.spinner_year);
        adapterY = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listYear);
        adapterY.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinnerY.setAdapter(adapterY);
        mySpinnerY.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                yearText.setText(adapterY.getItem(arg2));
                arg0.setVisibility(View.VISIBLE);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                yearText.setText("None");
                arg0.setVisibility(View.VISIBLE);
            }
        });
         /* choice menu end */

        //month
        listMonth.add("Month");
        for (int i = 1; i <= 12; i++) {
            String m = Integer.toString(i);
            listMonth.add(m);
        }
        monthText = (TextView) findViewById(R.id.month);
        mySpinnerM = (Spinner) findViewById(R.id.spinner_month);
        adapterM = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listMonth);
        adapterM.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinnerM.setAdapter(adapterM);
        mySpinnerM.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                monthText.setText(adapterM.getItem(arg2));
                arg0.setVisibility(View.VISIBLE);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                monthText.setText("None");
                arg0.setVisibility(View.VISIBLE);
            }
        });

        //Day
        listDay.add("Day");
        for (int i = 1; i <= 31; i++)
            listDay.add(Integer.toString(i));

        dayText = (TextView) findViewById(R.id.day);
        mySpinnerD = (Spinner) findViewById(R.id.spinner_day);
        adapterD = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listDay);
        adapterD.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinnerD.setAdapter(adapterD);
        mySpinnerD.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                dayText.setText(adapterD.getItem(arg2));
                arg0.setVisibility(View.VISIBLE);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                dayText.setText("None");
                arg0.setVisibility(View.VISIBLE);
            }
        });

        //type
        listType.add("Type");
        listType.add("Taste");
        listType.add("Service");
        listType.add("Environment");
        typeText = (TextView) findViewById(R.id.type);
        mySpinnerT = (Spinner) findViewById(R.id.spinner_type);
        adapterT = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listType);
        adapterT.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinnerT.setAdapter(adapterT);
        mySpinnerT.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                typeText.setText(adapterT.getItem(arg2));
                arg0.setVisibility(View.VISIBLE);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                typeText.setText("None");
                arg0.setVisibility(View.VISIBLE);
            }
        });

        Button check = (Button) findViewById(R.id.checkIncome);

        // checking data from database and showing them
        check.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                final boolean helper[] = {false};
                new Thread() {
                    public void run() {
                        try {
                            String year = yearText.getText().toString();
                            String month = monthText.getText().toString();
                            String day = dayText.getText().toString();
                            String type = typeText.getText().toString();
                            price = StormSQL.getIncome(year, month);

                            if (year.equals("Year") && month.equals("Month") && day.equals("Day") && !type.equals("Type")) {
                                cmtList.clear();
                                cmtList.addAll(StormSQL.getCommentListByType(type));
                                android.os.Handler handler = new android.os.Handler(getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        adapterCmt.notifyDataSetChanged();
                                    }
                                };
                                handler.sendMessage(handler.obtainMessage());
                            } else if (month.equals("Month") && day.equals("Day") && !year.equals("Year") && type.equals("Type")) {
                                cmtList.clear();
                                cmtList.addAll(StormSQL.getCommentListByYear(year));
                                android.os.Handler handler = new android.os.Handler(getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        adapterCmt.notifyDataSetChanged();
                                    }
                                };
                                handler.sendMessage(handler.obtainMessage());
                            } else if (month.equals("Month") && year.equals("Year") && !day.equals("Day") && type.equals("Type")) {
                                cmtList.clear();
                                cmtList.addAll(StormSQL.getCommentListByDay(day));
                                android.os.Handler handler = new android.os.Handler(getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        adapterCmt.notifyDataSetChanged();
                                    }
                                };
                                handler.sendMessage(handler.obtainMessage());
                            } else if (day.equals("Day") && !year.equals("Year") && !month.equals("Month") && type.equals("Type")) {
                                cmtList.clear();
                                cmtList.addAll(StormSQL.getCommentListByYearMonth(year, month));
                                android.os.Handler handler = new android.os.Handler(getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        adapterCmt.notifyDataSetChanged();
                                    }
                                };
                                handler.sendMessage(handler.obtainMessage());
                            } else if (day.equals("Day") && year.equals("Year") && !month.equals("Month") && type.equals("Type")) {
                                cmtList.clear();
                                cmtList.addAll(StormSQL.getCommentListByMonth(month));
                                android.os.Handler handler = new android.os.Handler(getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        adapterCmt.notifyDataSetChanged();
                                    }
                                };
                                handler.sendMessage(handler.obtainMessage());
                            } else if (year.equals("Year") && month.equals("Month") && day.equals("Day") && type.equals("Type")) {
                                cmtList.clear();
                                cmtList.addAll(StormSQL.getCommentAll());
                                android.os.Handler handler = new android.os.Handler(getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        adapterCmt.notifyDataSetChanged();
                                    }
                                };
                                handler.sendMessage(handler.obtainMessage());
                            } else {
                                cmtList.clear();
                                cmtList.addAll(StormSQL.getCommentList(year, month, day, type));
                                android.os.Handler handler = new android.os.Handler(getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        adapterCmt.notifyDataSetChanged();
                                    }
                                };
                                handler.sendMessage(handler.obtainMessage());
                            }

                            helper[0] = true;
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                while (helper[0] == false) ;

                if (price.size() == 31)
                    dataAnalysis(price);
            }
        });

        new Thread() {
            @Override
            public void run() {
                try {
                    final ListView listViewFavor = (ListView) findViewById(R.id.listView_favorite);
                    final List<Map<String, Object>> favoriteList = StormSQL.getFavorDishes();
                    Handler handler = new Handler(getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            adapterFavor = new SimpleAdapter(getApplicationContext(), favoriteList, R.layout.boss_favor_item,
                                    new String[]{"num", "name", "type", "amount"},
                                    new int[]{
                                            R.id.textView_boss_favor_num,
                                            R.id.textView_boss_favor_name,
                                            R.id.textView_boss_favor_type,
                                            R.id.textView_boss_favor_amount});
                            listViewFavor.setAdapter(adapterFavor);
                            adapterFavor.notifyDataSetChanged();
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

    // Analysis the income and showing them
    private void dataAnalysis(ArrayList income) {
        List<String> xValues = new ArrayList<String>();

        for (int i = 1; i <= 31; i++)
            xValues.add(i + "day");

        ArrayList<Entry> yValue = new ArrayList<>();
        if (income.size() > 0)
            for (int i = 0; i <= 30; i++) {
                int y = Integer.parseInt(income.get(i).toString());
                yValue.add(new Entry(y, i));
            }
        else
            for (int i = 0; i <= 30; i++)
                yValue.add(new Entry(0, i));

        //构建一个LineDataSet 代表一组Y轴数据
        LineDataSet dataSet = new LineDataSet(yValue, "Income");
        dataSet.setColor(Color.BLACK);
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        //将数据加入dataSets
        dataSets.add(dataSet);
        //构建一个LineData  将dataSets放入
        LineData lineData = new LineData(xValues, dataSet);
        mLineChart.clear();
        //将数据插入
        mLineChart.setData(lineData);
    }
}
