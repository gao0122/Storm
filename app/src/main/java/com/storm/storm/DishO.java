package com.storm.storm;

import java.io.Serializable;

/**
 * Created by Storm on 4/27/16.
 * Dish class for dishes ordered by customers
 *
 * @author Yuchao
 */
public class DishO implements Serializable {
    private int amount, price, id;
    private String name, type, phone, cName, state;

    public DishO(int id, String name, int amount, String state,
                 int price, String type, String phone, String cName) {
        setId(id);
        setName(name);
        setAmount(amount);
        setPrice(price);
        setType(type);
        setPhone(phone);
        setcName(cName);
        setState(state);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
