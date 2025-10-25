package com.mzansi.solutions.disasterdetectoralert.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mzansi.solutions.disasterdetectoralert.R;
import com.mzansi.solutions.disasterdetectoralert.models.Crop;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ActiveCropAdapter extends RecyclerView.Adapter<ActiveCropAdapter.ViewHolder> {
    
    private List<Crop> crops;
    
    public ActiveCropAdapter() {
        this.crops = new ArrayList<>();
    }
    
    public void setCrops(List<Crop> crops) {
        this.crops = crops;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_active_crop, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Crop crop = crops.get(position);
        holder.bind(crop);
    }
    
    @Override
    public int getItemCount() {
        return crops.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCropName;
        TextView tvCropVariety;
        TextView tvDaysProgress;
        TextView tvHarvestDate;
        ProgressBar progressBar;
        
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCropName = itemView.findViewById(R.id.tvCropName);
            tvCropVariety = itemView.findViewById(R.id.tvCropVariety);
            tvDaysProgress = itemView.findViewById(R.id.tvDaysProgress);
            tvHarvestDate = itemView.findViewById(R.id.tvHarvestDate);
            progressBar = itemView.findViewById(R.id.progressGrowth);
        }
        
        public void bind(Crop crop) {
            tvCropName.setText(crop.getName());
            tvCropVariety.setText(crop.getVariety());
            
            int daysSincePlanting = crop.getDaysSincePlanting();
            int growthProgress = crop.getGrowthProgress();
            tvDaysProgress.setText(daysSincePlanting + " days planted • " + growthProgress + "% grown");
            
            int daysUntilHarvest = crop.getDaysUntilHarvest();
            if (daysUntilHarvest > 0) {
                tvHarvestDate.setText("Harvest in " + daysUntilHarvest + " days");
            } else if (daysUntilHarvest == 0) {
                tvHarvestDate.setText("Ready to harvest!");
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                tvHarvestDate.setText("Est. " + sdf.format(crop.getExpectedHarvestDate()));
            }
            
            progressBar.setProgress(Math.min(growthProgress, 100));
        }
    }
}




