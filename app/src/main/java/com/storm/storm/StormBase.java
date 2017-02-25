package com.storm.storm;

import com.firebase.client.Firebase;

/**
 * Created by Storm on 5/4/16.
 * Util class for Firebase part
 *
 * @author Yuchao
 */
public abstract class StormBase {

    private static final String ROOT = "http://uicstorm.firebaseio.com/";
    public static final Firebase FB = new Firebase(ROOT);
    private static final String CHEF = "chef";
    public static final Firebase CHEF_FB = FB.child(CHEF);
    private static final String TABLE = "table1";
    public static final Firebase TABLE1_FB = FB.child(TABLE);

}
