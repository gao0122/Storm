package com.storm.storm;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Storm on 5/1/2016.
 * MySQL util class
 *
 * @author Yuchao
 */
public abstract class StormSQL {

    private static final String s = "jdbc:mysql://rds651pb32rr4mtq03y6.mysql.rds.aliyuncs.com:3306/r013vpj938?user=yuchao&password=";

    /**
     * close these parameters
     *
     * @param resultSet
     * @param connect
     * @param preparedStatement
     * @throws SQLException
     */
    private static void close(ResultSet resultSet, Connection connect, PreparedStatement preparedStatement) throws SQLException {
        if (resultSet != null)
            resultSet.close();
        if (connect != null)
            connect.close();
        if (preparedStatement != null)
            preparedStatement.close();
    }

    /**
     * transfer raw type to displayed type string
     *
     * @param type raw type string
     * @return displayed type string
     */
    private static String toType(String type) {
        switch (type) {
            case "daily":
                return "Daily";
            case "HotDish":
                return "Hot dish";
            case "coldDish":
                return "Cold dish";
            case "drink":
                return "Drink";
            case "StapleFood":
                return "Staple food";
        }
        return null;
    }

    /**
     * get staff information by its phone number
     *
     * @param phone phone string
     * @return map information
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static HashMap<String, String> getStaff(String phone) throws ClassNotFoundException, SQLException {
        HashMap<String, String> info = new HashMap<String, String>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `staffs` WHERE `phone`='" + phone + "'");
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            info.put("id", resultSet.getString("id"));
            info.put("name", resultSet.getString("name"));
            info.put("phone", resultSet.getString("phone"));
            info.put("type", resultSet.getString("type"));
            info.put("pwd", resultSet.getString("pwd"));
        }
        close(resultSet, connect, preparedStatement);
        return info;
    }

    public static boolean addStaff(String name, String phone, String type, String pwd)
            throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("INSERT INTO `staffs`(`name`, `phone`, `type`, `pwd`) VALUES ('"
                + name + "', '" + phone + "', '" + type + "', '" + pwd + "')");
        preparedStatement.execute();
        close(null, connect, preparedStatement);
        return true;
    }

    public static boolean updateStaff(String phone, String newPhone, String name, String pwd)
            throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("UPDATE `staffs` SET `phone`='" + newPhone
                + "', `name`='" + name + "', `pwd`='" + pwd + "' WHERE `phone`='" + phone + "'");
        preparedStatement.execute();
        close(null, connect, preparedStatement);
        return true;
    }

    public static ArrayList<DishS> getDishes() throws ClassNotFoundException, SQLException {
        ArrayList<DishS> dishes = new ArrayList<DishS>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `dishes`");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next())
            dishes.add(new DishS(resultSet.getInt("price"), resultSet.getString("name"),
                    resultSet.getString("type"), resultSet.getString("img"), resultSet.getString("chef")));
        close(resultSet, connect, preparedStatement);
        return dishes;
    }

    public static String getDishImg(String name) throws ClassNotFoundException, SQLException {
        String img = "storm_default.png";
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT img FROM `dishes` WHERE `name`='" + name + "'");
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next())
            img = resultSet.getString("img");
        close(resultSet, connect, preparedStatement);
        return img;
    }

    public static boolean addDish(String name, int price, String type, String img, String chef) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("INSERT INTO `dishes`(`name`, `price`, `type`, `img`, `chef`) VALUES ('"
                + name + "', '" + price + "', '" + type + "', '" + img + "', '" + chef + "')");
        preparedStatement.execute();
        close(null, connect, preparedStatement);
        return true;
    }

    public static void deleteDish(String name) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("DELETE FROM `dishes` WHERE `name`='" + name + "'");
        preparedStatement.execute();
        close(null, connect, preparedStatement);
    }

    public static void updateDish(String name, String newName, String type, int price)
            throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("UPDATE `dishes` SET `price`='" + price
                + "', `name`='" + newName + "', `type`='" + type + "' WHERE `name`='" + name + "'");
        preparedStatement.execute();
        close(null, connect, preparedStatement);
    }

    public static void updateDish(String name, String chef) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("UPDATE `dishes` SET `chef`='" + chef + "' WHERE `name`='" + name + "'");
        preparedStatement.execute();
        close(null, connect, preparedStatement);
    }

    public static boolean addCon(String name, int price, int amount, int table, String type,
                                 int year, int month, int day, Time time, String cname, String phone)
            throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("INSERT INTO `consumption`(`name`, "
                + "`price`, `amount`, `table`, `type`, `year`, `month`, `day`, `time`, `cName`, `phone`) VALUES ('"
                + name + "', '" + price + "', '" + amount + "', '" + table + "', '" + type + "', '"
                + year + "', '" + month + "', '" + day + "', '" + time + "', '" + cname + "', '" + phone + "')");
        preparedStatement.execute();
        close(null, connect, preparedStatement);
        return true;
    }

    public static boolean addVIP(String name, String phone)
            throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("INSERT INTO `vips`(`name`, "
                + "`phone`, `total`) VALUES ('" + name + "', '" + phone + "', '" + 0 + "')");
        preparedStatement.execute();
        close(null, connect, preparedStatement);
        return true;
    }

    public static ArrayList<String> getVIP(String phone) throws ClassNotFoundException, SQLException {
        ArrayList<String> list = new ArrayList<String>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `vips` WHERE `phone`='" + phone + "'");
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            list.add(resultSet.getString("name"));
            list.add(resultSet.getInt("total") + "");
        }
        close(resultSet, connect, preparedStatement);
        return list;
    }

    public static boolean updateVIPTotal(String phone, int tmp)
            throws SQLException, ClassNotFoundException {
        if (tmp <= 0)
            return false;
        ArrayList<String> list = getVIP(phone);
        int total = list.size() == 2 ? (Integer.parseInt(list.get(1)) + tmp) : 0;
        if (total == 0)
            return false;
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("UPDATE `vips` SET `total`='"
                + total + "' WHERE `phone`='" + phone + "'");
        preparedStatement.execute();
        close(null, connect, preparedStatement);
        return true;
    }

    public static void updateTable(ArrayList<DishO> dishes) throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        connect.prepareStatement("DELETE FROM `tables`").execute();
        PreparedStatement preparedStatement = null;
        for (int i = 0; i < dishes.size(); i++) {
            DishO ds = dishes.get(i);
            Log.e("updateTable", ds.getName());
            preparedStatement = connect.prepareStatement("INSERT INTO `tables`(`table_id`, "
                    + "`name`, `amount`, `type`, `price`, `state`, `phone`, `cName`) VALUES ('" + ds.getId() + "', '"
                    + ds.getName() + "', '" + ds.getAmount() + "', '" + ds.getType() + "', '" + ds.getPrice() + "', '"
                    + ds.getState() + "', '" + ds.getPhone() + "', '" + ds.getcName() + "')");
            preparedStatement.execute();
        }
        close(null, connect, preparedStatement);
    }

    public static List<Map<String, Object>> getTablesByChef(String state, HashMap<String, DishS> dishes, String chef)
            throws ClassNotFoundException, SQLException {
        final String DNAME = "dsName", AMOUNT = "amount", TID = "tid", ID = "id";
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `tables` WHERE `state`='" + state + "'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            String nm = resultSet.getString("name");
            Log.e("cook", nm);
            if (dishes.get(nm) != null && chef.equals(dishes.get(nm).getChef())) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(DNAME, nm);
                map.put(ID, resultSet.getInt("id"));
                map.put(TID, "T" + resultSet.getInt("table_id"));
                map.put(AMOUNT, resultSet.getInt("amount"));
                Log.e("table dish", nm + "\t" + resultSet.getInt("amount"));
                list.add(map);
            }
        }
        close(resultSet, connect, preparedStatement);
        return list;
    }

    /**
     * update state of dishes
     *
     * @param id    table dish id
     * @param state state to be updated
     * @return the amount after updating state
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static void updateState(int id, String state) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("UPDATE `tables` SET `state`='"
                + state + "' WHERE `id`='" + id + "'");
        preparedStatement.execute();
        close(null, connect, preparedStatement);
    }

    public static ArrayList<DishO> getTablesById(int tid) throws ClassNotFoundException, SQLException {
        ArrayList<DishO> list = new ArrayList<DishO>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `tables` WHERE `table_id`='" + tid + "'");
        ResultSet resultSet = preparedStatement.executeQuery();
        String name, state, type, phone, cName;
        int amount, price;
        while (resultSet.next()) {
            state = resultSet.getString("state");
            name = resultSet.getString("name");
            type = resultSet.getString("type");
            phone = resultSet.getString("phone");
            cName = resultSet.getString("cName");
            amount = resultSet.getInt("amount");
            price = resultSet.getInt("price");
            DishO o = new DishO(tid, name, amount, state, price, type, phone, cName);
            list.add(o);
        }
        close(resultSet, connect, preparedStatement);
        return list;
    }

    public static List<Map<String, Object>> getDishList() throws ClassNotFoundException, SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `dishes`");
        ResultSet resultSet = preparedStatement.executeQuery();
        String name, type, chef;
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            name = resultSet.getString("name");
            type = resultSet.getString("type");
            chef = resultSet.getString("chef");
            map.put("dname", name);
            map.put("dtype", toType(type));
            map.put("dchef", chef);
            map.put("dprice", resultSet.getInt("price"));
            list.add(map);
        }
        close(resultSet, connect, preparedStatement);
        return list;
    }

    public static List<Map<String, Object>> getChefList() throws ClassNotFoundException, SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `staffs` WHERE `type` = 'chef'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put("cname", resultSet.getString("name"));
            map.put("cphone", resultSet.getString("phone"));
            list.add(map);
        }
        close(resultSet, connect, preparedStatement);
        return list;
    }

    public static Map<String, List<Object>> getDiscount() throws ClassNotFoundException, SQLException {
        Map<String, List<Object>> map = new HashMap<>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `discount`");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            List<Object> list = new ArrayList<>();
            list.add(resultSet.getInt("amount"));
            list.add(resultSet.getFloat("discount"));
            map.put(resultSet.getString("type"), list);
        }
        close(resultSet, connect, preparedStatement);
        return map;
    }

    public static void updateDiscount(String type, int amt, float f) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("UPDATE `discount` SET `discount`='"
                + f + "', `amount`='" + amt + "' WHERE `type`='" + type + "'");
        preparedStatement.execute();
        close(null, connect, preparedStatement);
    }

    public static boolean addComments(String name, String phone, int year, int month, int day, String type, String content) throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("INSERT INTO `comments`(`name`, `phone`, `year`,`month`,`day`, `type`, `content`) VALUES ('"
                + name + "', '" + phone + "', '" + year + "', '" + month + "','" + day + "','" + type + "', '" + content + "')");
        preparedStatement.execute();
        close(null, connect, preparedStatement);
        return true;
    }

    public static ArrayList getIncome(String year, String month) throws ClassNotFoundException, SQLException {
        ArrayList priceDay = new ArrayList<>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        for (int i = 1; i <= 31; i++) {
            int price = 0;
            preparedStatement = connect.prepareStatement("SELECT * FROM `consumption` WHERE `year`='"
                    + year + "' and `month`='" + month + "' and `day`='" + i + "'");
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next())
                price = price + resultSet.getInt("price");
            priceDay.add(price);
        }
        close(resultSet, connect, preparedStatement);
        return priceDay;
    }

    public static List<Map<String, Object>> getCommentList(String year, String month, String day, String type) throws ClassNotFoundException, SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `comments` WHERE `year`='"
                + year + "' and `month`='" + month + "' and `day`='" + day + "' and `type`='" + type + "'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put("cname", resultSet.getString("name"));
            map.put("type", resultSet.getString("type"));
            map.put("year", String.valueOf(resultSet.getInt("year")));
            map.put("month", String.valueOf(resultSet.getInt("month")));
            map.put("day", String.valueOf(resultSet.getInt("day")));
            map.put("content", resultSet.getString("content"));
            list.add(map);
        }
        close(resultSet, connect, preparedStatement);
        return list;
    }

    public static List<Map<String, Object>> getCommentListByYear(String year) throws ClassNotFoundException, SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `comments` WHERE `year`='" + year + "'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put("cname", resultSet.getString("name"));
            map.put("type", resultSet.getString("type"));
            map.put("year", String.valueOf(resultSet.getInt("year")));
            map.put("month", String.valueOf(resultSet.getInt("month")));
            map.put("day", String.valueOf(resultSet.getInt("day")));
            map.put("content", resultSet.getString("content"));
            list.add(map);
        }
        close(resultSet, connect, preparedStatement);
        return list;
    }

    public static List<Map<String, Object>> getCommentListByYearMonth(String year, String month) throws ClassNotFoundException, SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `comments` WHERE `year`='"
                + year + "' and `month`='" + month + "'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put("cname", resultSet.getString("name"));
            map.put("type", resultSet.getString("type"));
            map.put("year", String.valueOf(resultSet.getInt("year")));
            map.put("month", String.valueOf(resultSet.getInt("month")));
            map.put("day", String.valueOf(resultSet.getInt("day")));
            map.put("content", resultSet.getString("content"));
            list.add(map);
        }
        close(resultSet, connect, preparedStatement);
        return list;
    }

    public static List<Map<String, Object>> getCommentListByMonth(String month) throws ClassNotFoundException, SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `comments` WHERE  `month`='" + month + "'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put("cname", resultSet.getString("name"));
            map.put("type", resultSet.getString("type"));
            map.put("year", String.valueOf(resultSet.getInt("year")));
            map.put("month", String.valueOf(resultSet.getInt("month")));
            map.put("day", String.valueOf(resultSet.getInt("day")));
            map.put("content", resultSet.getString("content"));
            list.add(map);
        }
        close(resultSet, connect, preparedStatement);
        return list;
    }

    public static List<Map<String, Object>> getCommentListByType(String type) throws ClassNotFoundException, SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `comments` WHERE `type`='" + type + "'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put("cname", resultSet.getString("name"));
            map.put("type", resultSet.getString("type"));
            map.put("year", String.valueOf(resultSet.getInt("year")));
            map.put("month", String.valueOf(resultSet.getInt("month")));
            map.put("day", String.valueOf(resultSet.getInt("day")));
            map.put("content", resultSet.getString("content"));
            list.add(map);
        }
        close(resultSet, connect, preparedStatement);
        return list;
    }

    public static List<Map<String, Object>> getCommentListByDay(String day) throws ClassNotFoundException, SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `comments` WHERE `day`='" + day + "'");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put("cname", resultSet.getString("name"));
            map.put("type", resultSet.getString("type"));
            map.put("year", String.valueOf(resultSet.getInt("year")));
            map.put("month", String.valueOf(resultSet.getInt("month")));
            map.put("day", String.valueOf(resultSet.getInt("day")));
            map.put("content", resultSet.getString("content"));
            list.add(map);
        }
        close(resultSet, connect, preparedStatement);
        return list;
    }

    public static List<Map<String, Object>> getCommentAll() throws ClassNotFoundException, SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `comments`");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            Map<String, Object> map = new HashMap<>();
            map.put("cname", resultSet.getString("name"));
            map.put("type", resultSet.getString("type"));
            map.put("year", String.valueOf(resultSet.getInt("year")));
            map.put("month", String.valueOf(resultSet.getInt("month")));
            map.put("day", String.valueOf(resultSet.getInt("day")));
            map.put("content", resultSet.getString("content"));
            list.add(map);
        }
        close(resultSet, connect, preparedStatement);
        return list;
    }

    /**
     * get favourite dishes from database
     *
     * @return Favourite dishes list map
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static List<Map<String, Object>> getFavorDishes() throws ClassNotFoundException, SQLException {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        HashMap<String, Integer> dishes = new HashMap<String, Integer>();
        HashMap<String, String> types = new HashMap<String, String>();
        Integer amount;
        String name, type;
        Class.forName("com.mysql.jdbc.Driver");
        Connection connect = DriverManager.getConnection(s);
        PreparedStatement preparedStatement = connect.prepareStatement("SELECT * FROM `consumption`");
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            type = resultSet.getString("type");
            amount = resultSet.getInt("amount");
            name = resultSet.getString("name");
            if (dishes.containsKey(name))
                dishes.put(name, amount + dishes.get(name));
            else
                dishes.put(name, amount);
            if (!types.containsKey(name))
                types.put(name, type);
        }
        ValueComparator vc = new ValueComparator(dishes);
        TreeMap<String, Integer> sorted = new TreeMap<String, Integer>(vc);
        sorted.putAll(dishes);
        Iterator<String> itr = sorted.keySet().iterator();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("num", " No.");
        map.put("name", "Dish name");
        map.put("type", "Type");
        map.put("amount", "Sold amount ");
        list.add(map);
        for (int i = 1; itr.hasNext(); i++) {
            name = itr.next();
            Log.e(i + "", name);
            amount = dishes.get(name);
            map = new HashMap<String, Object>();
            map.put("num", "  " + i);
            map.put("name", name);
            map.put("type", toType(types.get(name)));
            map.put("amount", amount + "          ");
            list.add(map);
        }
        close(resultSet, connect, preparedStatement);
        return list;
    }

    private static class ValueComparator implements Comparator<String> {
        Map<String, Integer> map;

        public ValueComparator(Map<String, Integer> map) {
            this.map = map;
        }

        @Override
        public int compare(String lhs, String rhs) {
            if (map.get(lhs) >= map.get(rhs))
                return -1;
            else
                return 1;
        }
    }

}