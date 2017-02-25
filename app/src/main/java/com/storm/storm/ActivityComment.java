package com.storm.storm;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Storm on 5/6/16.
 *
 * @author River
 */
public class ActivityComment extends AppCompatActivity {

    private List<String> list = new ArrayList<String>();
    private TextView myTextView, textViewName;
    private Spinner mySpinner;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        String name, phone;
        name = getIntent().getStringExtra("name");
        phone = getIntent().getStringExtra("phone");
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH) + 1;
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final String sDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
        final TextView type = (TextView) findViewById(R.id.textView_logo_comments);
        final EditText comment = (EditText) findViewById(R.id.editText2);
        TextView data = (TextView) findViewById(R.id.textView_dateDisplay_comments_customer);
        data.setText(sDate);

        list.add("Taste");
        list.add("Service");
        list.add("Environment");
        textViewName = (TextView) findViewById(R.id.textView_nameDisplay_comments_customer);
        textViewName.setText(name);
        myTextView = (TextView) findViewById(R.id.textView_logo_comments);
        mySpinner = (Spinner) findViewById(R.id.spinner_commentType);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(adapter);
        mySpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                myTextView.setText(adapter.getItem(arg2));
                arg0.setVisibility(View.VISIBLE);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
                myTextView.setText("None");
                arg0.setVisibility(View.VISIBLE);
            }
        });

        Button submit = (Button) findViewById(R.id.button_submit);
        final String finalName = name;
        final String finalPhone = phone;
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            String sType = type.getText().toString();
                            String sContent = comment.getText().toString();
                            if (sContent.length() > 0) {
                                StormSQL.addComments(finalName, finalPhone, year, month, day, sType, sContent);
                                toast("Commented successfully.", Toast.LENGTH_LONG);
                            } else
                                toast("Please input something.", Toast.LENGTH_SHORT);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });

        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * using handler to make a toast which is not in the main UI thread,
     * only in this activity, if the length is long then finish this activity
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
                if (t == Toast.LENGTH_LONG)
                    finish();
            }
        };
        handler.sendMessage(handler.obtainMessage());
    }

}
