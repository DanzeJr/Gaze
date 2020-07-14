package com.ecotioco.gaze.model;

import java.util.Date;

public class Cart {

    public long id;
    public long orderId = -1L;
    public long productId;
    public Product product;
    public int quantity;
    public Date createdDate;

    public Cart() {
    }

    public Cart(long productId, int quantity, Date createdDate) {
        this.productId = productId;
        this.quantity = quantity;
        this.createdDate = createdDate;
    }

    public Cart(Product product, int quantity, Date createdDate) {
        this.productId = product.id;
        this.product = product;
        this.quantity = quantity;
        this.createdDate = createdDate;
    }
}
