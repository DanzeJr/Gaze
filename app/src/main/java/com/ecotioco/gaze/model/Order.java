package com.ecotioco.gaze.model;

import android.content.Context;

import com.ecotioco.gaze.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {

    public Long id;
    public String code;
    public int status;
    public long userId = -1;
    public String fullName;
    public String address;
    public String email;
    public int shippingOption;
    public Date shipDate;
    public double shippingFee;
    public String phone;
    public String comment;
    public Date createdDate;
    public List<Cart> cartList = new ArrayList<>();

    public Order() {
    }

    public Order(DeliveryInfo deliveryInfo, List<Cart> cartList) {
        this.userId = deliveryInfo.userId;
        this.fullName = deliveryInfo.fullName;
        this.address = deliveryInfo.address;
        this.email = deliveryInfo.email;
        this.phone = deliveryInfo.phone;
        this.shippingOption = deliveryInfo.shippingOption;
        this.shipDate = deliveryInfo.shipDate;
        this.comment = deliveryInfo.comment;
        this.cartList = cartList;
    }

    public Order(User user, int shippingOption, Date shipDate, String comment, List<Cart> cartList) {
        this.userId = user.id;
        this.fullName = user.fullName;
        this.address = user.address;
        this.email = user.email;
        this.phone = user.phone;
        this.shippingOption = shippingOption;
        this.shipDate = shipDate;
        this.comment = comment;
        this.cartList = cartList;
    }

    public Order(String fullName, String address, String email, String phone, int shippingOption, Date shipDate, String comment, List<Cart> cartList) {
        this.userId = userId;
        this.fullName = fullName;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.shippingOption = shippingOption;
        this.shipDate = shipDate;
        this.comment = comment;
        this.cartList = cartList;
    }

    public double getTotal(boolean includeShippingFee) {
        double total = 0;
        for (int i = 0; i < cartList.size(); i++) {
            Cart cart = cartList.get(i);
            total += cart.product.price * cart.quantity;
        }

        if (includeShippingFee) {
            total += shippingFee;
        }

        return total;
    }

    public int getAmount() {
        int amount = 0;
        for (int i = 0; i < cartList.size(); i++) {
            Cart cart = cartList.get(i);
            amount += cart.quantity;
        }

        return amount;
    }

    public DeliveryInfo getDeliveryInfo() {
        return new DeliveryInfo(id, userId, fullName, phone, email, address, shippingOption, shipDate, comment);
    }

    public static String getStatus(Context context, int status) {
        switch (status) {
            case 0: {
                return context.getString(R.string.order_status_submitted);
            }
            case 1: {
                return context.getString(R.string.order_status_delivering);
            }
            case 2: {
                return context.getString(R.string.order_status_completed);
            }
            case 3: {
                return context.getString(R.string.order_status_cancelled);
            }
        }

        return status + "";
    }

    public static String getShippingOption(Context context, int option) {
        if (option == 1) {
            return context.getString(R.string.shipping_option_standard);
        } else if (option == 2) {
            return context.getString(R.string.shipping_option_premium);
        }

        return option + "";
    }

}



