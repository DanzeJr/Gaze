package com.ecotioco.gaze.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.ecotioco.gaze.R;
import com.ecotioco.gaze.data.Constant;
import com.ecotioco.gaze.model.Cart;
import com.ecotioco.gaze.utils.Tools;

import java.util.List;


public class ShoppingCartAdapter extends RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder> {

    private Context ctx;
    private List<Cart> items;
    private Boolean is_cart = true;

    private OnItemClickListener onItemClickListener;
    private OnItemClickListener removeItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Cart obj);
    }

    public void setOnRemoveItemClick(OnItemClickListener listener) {
        this.removeItemClickListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView details;
        public TextView amount;
        public TextView price;
        public ImageView image;
        public ImageView removeItemBtn;
        public RelativeLayout lyt_image;

        public ViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            details = v.findViewById(R.id.details);
            amount = v.findViewById(R.id.amount);
            price = v.findViewById(R.id.price);
            image = v.findViewById(R.id.image);
            removeItemBtn = v.findViewById(R.id.bt_remove_cart_item);
            lyt_image = v.findViewById(R.id.lyt_image);
        }
    }

    public ShoppingCartAdapter(Context ctx, boolean is_cart, List<Cart> items) {
        this.ctx = ctx;
        this.items = items;
        this.is_cart = is_cart;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shopping_cart, parent, false);
        vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ViewHolder vItem = (ViewHolder) holder;
        final Cart c = items.get(position);
        vItem.title.setText(c.product.name);
        vItem.details.setText(ctx.getResources().getString(R.string.color) + ": " + c.product.color
                + " | " + ctx.getResources().getString(R.string.size) + ": " + c.product.size);
        vItem.price.setText(Tools.getFormattedPrice(c.product.price, ctx));
        vItem.amount.setText("x" + c.quantity);
        Tools.displayImageThumbnail(ctx, vItem.image, Constant.getURLimgProduct(c.product.image), 0.5f);

        vItem.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, c);
                }
            }
        });
        vItem.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, c);
                }
            }
        });
        vItem.price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, c);
                }
            }
        });
        vItem.removeItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (removeItemClickListener != null) {
                    removeItemClickListener.onItemClick(v, c);
                }
            }
        });

        if (is_cart) {
            vItem.title.setEnabled(true);
            vItem.details.setEnabled(true);
            vItem.price.setEnabled(true);
            vItem.removeItemBtn.setVisibility(View.VISIBLE);
        } else {
            vItem.title.setEnabled(false);
            vItem.details.setEnabled(false);
            vItem.price.setEnabled(false);
            vItem.removeItemBtn.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<Cart> getItems() {
        return items;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setItems(List<Cart> items) {
        this.items = items;
        notifyDataSetChanged();
    }

}