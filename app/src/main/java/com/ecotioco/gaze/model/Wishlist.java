package com.ecotioco.gaze.model;

import java.io.Serializable;
import java.util.Date;

public class Wishlist implements Serializable {

    public Long productId;
    public String name;
    public String image;
    public Date createdDate = new Date();

    public Wishlist() {
    }

    public Wishlist(Long productId, String name, String image, Date createdDate) {
        this.productId = productId;
        this.name = name;
        this.image = image;
        this.createdDate = createdDate;
    }
}
