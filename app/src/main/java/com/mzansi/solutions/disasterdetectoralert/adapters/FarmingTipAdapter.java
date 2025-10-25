package com.mzansi.solutions.disasterdetectoralert.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mzansi.solutions.disasterdetectoralert.R;
import com.mzansi.solutions.disasterdetectoralert.models.FarmingTip;

import java.util.ArrayList;
import java.util.List;

public class FarmingTipAdapter extends RecyclerView.Adapter<FarmingTipAdapter.ViewHolder> {
    
    private List<FarmingTip> tips;
    
    public FarmingTipAdapter() {
        this.tips = new ArrayList<>();
    }
    
    public void setTips(List<FarmingTip> tips) {
        this.tips = tips;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_farming_tip, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FarmingTip tip = tips.get(position);
        holder.bind(tip);
    }
    
    @Override
    public int getItemCount() {
        return tips.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDescription;
        TextView tvIcon;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTipTitle);
            tvDescription = itemView.findViewById(R.id.tvTipDescription);
            tvIcon = itemView.findViewById(R.id.tvTipIcon);
        }
        
        public void bind(FarmingTip tip) {
            tvTitle.setText(tip.getTitle());
            tvDescription.setText(tip.getDescription());
            tvIcon.setText(tip.getIcon());
        }
    }
}



