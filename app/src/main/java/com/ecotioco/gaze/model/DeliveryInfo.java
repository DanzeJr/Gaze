package com.ecotioco.gaze.model;

import java.io.Serializable;
import java.util.Date;

public class DeliveryInfo implements Serializable {

    public long orderId = -1;
    public long userId = -1;
    public String fullName;
    public String phone;
    public String email;
    public String address;
    public int shippingOption = -1;
    public Date shipDate;
    public String comment;

    public DeliveryInfo() {
    }

    public DeliveryInfo(long orderId, long userId, String fullName, String phone, String email, String address, int shippingOption, Date shipDate, String comment) {
        this.orderId = orderId;
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.shippingOption = shippingOption;
        this.shipDate = shipDate;
        this.comment = comment;
    }

    public DeliveryInfo(long userId, String fullName, String phone, String email, String address, int shippingOption, Date shipDate, String comment) {
        this.userId = userId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.shippingOption = shippingOption;
        this.shipDate = shipDate;
        this.comment = comment;
    }

    public DeliveryInfo(String fullName, String phone, String email, String address, int shippingOption, Date shipDate, String comment) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.shippingOption = shippingOption;
        this.shipDate = shipDate;
        this.comment = comment;
    }
}
