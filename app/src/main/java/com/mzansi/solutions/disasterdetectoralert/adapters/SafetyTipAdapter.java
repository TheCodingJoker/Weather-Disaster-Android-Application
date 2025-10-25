package com.mzansi.solutions.disasterdetectoralert.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mzansi.solutions.disasterdetectoralert.R;
import com.mzansi.solutions.disasterdetectoralert.models.SafetyTip;

import java.util.List;

public class SafetyTipAdapter extends RecyclerView.Adapter<SafetyTipAdapter.TipViewHolder> {

    private List<SafetyTip> tips;

    public SafetyTipAdapter(List<SafetyTip> tips) {
        this.tips = tips;
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_safety_tip, parent, false);
        return new TipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        SafetyTip tip = tips.get(position);
        holder.bind(tip);
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    static class TipViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTipTitle;
        private TextView tvTipDescription;

        public TipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTipTitle = itemView.findViewById(R.id.tvTipTitle);
            tvTipDescription = itemView.findViewById(R.id.tvTipDescription);
        }

        public void bind(SafetyTip tip) {
            tvTipTitle.setText(tip.getTitle());
            tvTipDescription.setText(tip.getDescription());
        }
    }
}




