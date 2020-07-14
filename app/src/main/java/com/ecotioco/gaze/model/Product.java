package com.ecotioco.gaze.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Product implements Serializable {

    public static int READY_STOCK = 0;
    public static int OUT_OF_STOCK = 1;
    public static int SUSPEND = 2;

    public Long id;
    public String name;
    public String color;
    public int size;
    public String image;
    public Double price;
    public Double discount;
    public Long stock;
    public String description;
    public int status;
    public Date createdDate;
    public Date lastUpdate;

    public List<Category> categories = new ArrayList<>();
    public List<ProductImage> productImages = new ArrayList<>();

}
