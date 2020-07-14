package com.ecotioco.gaze.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.ecotioco.gaze.R;
import com.ecotioco.gaze.model.Order;
import com.ecotioco.gaze.utils.Tools;

import java.util.List;


public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private Context ctx;
    private List<Order> items;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View view, Order obj);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView code;
        public TextView orderDate;
        public TextView price;
        public TextView amount;
        public ImageView statusIcon;
        public TextView status;
        public MaterialRippleLayout parentLayout;

        public ViewHolder(View v) {
            super(v);
            code = v.findViewById(R.id.code);
            orderDate = v.findViewById(R.id.order_date);
            amount = v.findViewById(R.id.amount);
            price = v.findViewById(R.id.price);
            statusIcon = v.findViewById(R.id.status_icon);
            status = v.findViewById(R.id.status);
            parentLayout = v.findViewById(R.id.lyt_parent);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.listener = onItemClickListener;
    }

    public OrderAdapter(Context ctx, List<Order> items) {
        this.ctx = ctx;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Order order = items.get(position);
        holder.code.setText(order.code);
        holder.price.setText(Tools.getFormattedPrice(order.getTotal(true), ctx));
        holder.amount.setText("(" + order.getAmount() + " " + (order.getAmount() == 1 ? ctx.getString(R.string.item) : ctx.getString(R.string.items)) + ")");
        holder.statusIcon.setImageResource(getStatusImage(order.status));
        ImageViewCompat.setImageTintList(holder.statusIcon, getColorStateList(order.status));
        holder.status.setText(Order.getStatus(ctx, order.status));
        holder.orderDate.setText(Tools.getFormattedDate(order.createdDate));
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (listener != null) {
                    listener.onItemClick(v, order);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<Order> getItems() {
        return items;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(List<Order> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    private ColorStateList getColorStateList(int status) {
        if (status == 0) {
            return ContextCompat.getColorStateList(ctx, android.R.color.holo_blue_dark);
        }
        if (status == 1) {
            return ContextCompat.getColorStateList(ctx, android.R.color.holo_orange_dark);
        }
        if (status == 2) {
            return ContextCompat.getColorStateList(ctx, android.R.color.holo_green_dark);
        }

        return ContextCompat.getColorStateList(ctx, android.R.color.holo_red_dark);
    }

    private int getStatusImage(int status) {
        if (status == 0) {
            return R.drawable.ic_submitted;
        }
        if (status == 1) {
            return R.drawable.ic_delivering;
        }
        if (status == 2) {
            return R.drawable.icons_checked;
        }
        return R.drawable.ic_clear;
    }
}