package com.example.travelbuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ThingsToDoCategoryAdapter extends RecyclerView.Adapter<ThingsToDoCategoryAdapter.CategoryViewHolder> {

    private List<String> categories;
    private OnCategoryClickListener onCategoryClickListener;


    public ThingsToDoCategoryAdapter(List<String> categories, OnCategoryClickListener onCategoryClickListener) {
        this.categories = categories;
        this.onCategoryClickListener = onCategoryClickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_category, parent, false);
        return new CategoryViewHolder(itemView, onCategoryClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String categoryName = categories.get(position);
        holder.categoryName.setText(categoryName);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView categoryName;
        OnCategoryClickListener onCategoryClickListener;

        public CategoryViewHolder(@NonNull View itemView, OnCategoryClickListener onCategoryClickListener) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            this.onCategoryClickListener = onCategoryClickListener;
            itemView.setOnClickListener(this);
        }



        @Override
        public void onClick(View v) {
            onCategoryClickListener.onCategoryClick(getAdapterPosition());
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(int position);
    }
}