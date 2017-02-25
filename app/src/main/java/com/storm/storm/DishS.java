package com.storm.storm;

import java.io.Serializable;

/**
 * Created by Storm on 4/27/16.
 * Dish class for showing on the display
 *
 * @author Yuchao
 */
public class DishS implements Serializable {

    private int price;
    private String name, type, img, chef;

    public DishS(int price, String name, String type, String img, String chef) {
        setPrice(price);
        setName(name);
        setType(type);
        setImg(img);
        setChef(chef);
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getChef() {
        return chef;
    }

    public void setChef(String chef) {
        this.chef = chef;
    }
}
