package com.storm.storm;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.util.Random;

/**
 * Created by Storm on 4/30/16.
 * Message sending util class
 *
 * @author Yuchao
 */
public class StormMsg implements Runnable {

    private final String MSG = "Welcome to Storm Restaurant, your verification code is: ";
    private String phone, rdCode;
    private boolean check;
    private int valid;

    /**
     * constructor StormMsg
     *
     * @param check true if we are checking a phone number if is valid
     * @param phone phone number string
     */
    public StormMsg(boolean check, String phone) {
        this.phone = phone;
        this.check = check;
        setValid(-100);
        setRdCode(null);
    }

    /**
     * generate a random verification code string
     *
     * @return the verification code string
     */
    static String rdCode() {
        Random random = new Random();
        StringBuffer s = new StringBuffer("");
        for (int i = 0; i < 6; i++)
            s.append(random.nextInt(9));
        return s.toString();
    }

    @Override
    public void run() {
        String txt = "";
        if (!check)
            txt = MSG + (rdCode = rdCode());

        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod("http://gbk.sms.webchinese.cn");
        post.addRequestHeader("Content-Type", "application/x-www-form-urlencoded;charset=gbk");
        NameValuePair[] data = {
                new NameValuePair("Uid", "yuchao"),
                new NameValuePair("Key", ""),
                new NameValuePair("smsMob", phone),
                new NameValuePair("smsText", txt)
        };
        post.setRequestBody(data);

        try {
            client.executeMethod(post);
        } catch (IOException e) {
            e.printStackTrace();
            valid = 0;
        }

        try {
            valid = Integer.parseInt(new String(post.getResponseBodyAsString().getBytes("gbk")));
        } catch (IOException e) {
            e.printStackTrace();
            valid = 0;
        }

        post.releaseConnection();
    }

    public int getValid() {
        return valid;
    }

    public void setValid(int valid) {
        this.valid = valid;
    }

    public String getRdCode() {
        return rdCode;
    }

    public void setRdCode(String rdCode) {
        this.rdCode = rdCode;
    }
}
