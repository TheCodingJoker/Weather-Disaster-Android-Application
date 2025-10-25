package com.mzansi.solutions.disasterdetectoralert.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mzansi.solutions.disasterdetectoralert.R;
import com.mzansi.solutions.disasterdetectoralert.models.Crop;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CropListAdapter extends RecyclerView.Adapter<CropListAdapter.ViewHolder> {

    private List<Crop> crops;
    private OnCropClickListener editListener;
    private OnCropClickListener deleteListener;

    public interface OnCropClickListener {
        void onCropClick(Crop crop);
    }

    public CropListAdapter(List<Crop> crops, OnCropClickListener editListener, OnCropClickListener deleteListener) {
        this.crops = crops;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    public void setCrops(List<Crop> crops) {
        this.crops = crops;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_crop_list, parent, false);
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

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCropName;
        TextView tvCropDetails;
        TextView tvStatus;
        ImageButton btnEdit;
        ImageButton btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCropName = itemView.findViewById(R.id.tvCropName);
            tvCropDetails = itemView.findViewById(R.id.tvCropDetails);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(Crop crop) {
            tvCropName.setText(crop.getName() + (crop.getVariety() != null && !crop.getVariety().isEmpty() ? " (" + crop.getVariety() + ")" : ""));

            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String details = "Planted: " + sdf.format(crop.getPlantingDate()) + "\n" +
                    "Days: " + crop.getDaysSincePlanting() + " • " +
                    "Progress: " + crop.getGrowthProgress() + "%";
            tvCropDetails.setText(details);

            tvStatus.setText(crop.getStatus().toUpperCase());

            // Set status color
            int color;
            switch (crop.getStatus().toLowerCase()) {
                case "ready":
                    color = itemView.getContext().getColor(R.color.secondary_green);
                    break;
                case "harvested":
                    color = itemView.getContext().getColor(R.color.text_secondary);
                    break;
                default:
                    color = itemView.getContext().getColor(R.color.primary);
            }
            tvStatus.setTextColor(color);

            btnEdit.setOnClickListener(v -> editListener.onCropClick(crop));
            btnDelete.setOnClickListener(v -> deleteListener.onCropClick(crop));
        }
    }
}



