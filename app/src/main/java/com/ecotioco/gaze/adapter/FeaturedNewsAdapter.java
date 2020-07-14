package com.ecotioco.gaze.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ecotioco.gaze.R;
import com.ecotioco.gaze.data.Constant;
import com.ecotioco.gaze.model.NewsInfo;
import com.ecotioco.gaze.utils.Tools;
import com.balysv.materialripple.MaterialRippleLayout;

import java.util.List;

public class FeaturedNewsAdapter extends RecyclerView.Adapter<FeaturedNewsAdapter.ViewHolder> {

    private Context context;
    private List<NewsInfo> items;

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, NewsInfo obj);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public ImageView image;
        public MaterialRippleLayout parentLayout;

        public ViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            parentLayout = v.findViewById(R.id.lyt_parent);
        }
    }

    // constructor
    public FeaturedNewsAdapter(Context context, List<NewsInfo> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured_news, parent, false);
        vh = new FeaturedNewsAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final NewsInfo o = items.get(position);
        Tools.displayImageOriginal(context, holder.image, Constant.getURLimgNews(o.image));
        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, o);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public NewsInfo getItem(int pos) {
        return items.get(pos);
    }

    public void setItems(List<NewsInfo> items) {
        this.items = items;
        notifyDataSetChanged();
    }
}
