package com.mzansi.solutions.disasterdetectoralert.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mzansi.solutions.disasterdetectoralert.R;
import com.mzansi.solutions.disasterdetectoralert.models.DetailedAlert;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetailedAlertAdapter extends RecyclerView.Adapter<DetailedAlertAdapter.AlertViewHolder> {

    private List<DetailedAlert> alerts;

    public DetailedAlertAdapter(List<DetailedAlert> alerts) {
        this.alerts = alerts;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detailed_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        DetailedAlert alert = alerts.get(position);
        holder.bind(alert);
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAlertSeverity;
        private TextView tvAlertCategory;
        private TextView tvAlertTime;
        private TextView tvAlertTitle;
        private TextView tvAlertDescription;
        private TextView tvAffectedArea;
        private TextView tvDuration;
        private TextView tvIssuedBy;
        private TextView tvAlertStatus;
        private TextView tvInstructions;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlertSeverity = itemView.findViewById(R.id.tvAlertSeverity);
            tvAlertCategory = itemView.findViewById(R.id.tvAlertCategory);
            tvAlertTime = itemView.findViewById(R.id.tvAlertTime);
            tvAlertTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvAlertDescription = itemView.findViewById(R.id.tvAlertDescription);
            tvAffectedArea = itemView.findViewById(R.id.tvAffectedArea);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvIssuedBy = itemView.findViewById(R.id.tvIssuedBy);
            tvAlertStatus = itemView.findViewById(R.id.tvAlertStatus);
            tvInstructions = itemView.findViewById(R.id.tvInstructions);
        }

        public void bind(DetailedAlert alert) {
            tvAlertTitle.setText(alert.getTitle());
            tvAlertDescription.setText(alert.getDescription());
            tvAlertSeverity.setText(alert.getSeverity());
            tvAlertCategory.setText(alert.getCategory());
            tvAffectedArea.setText(alert.getAffectedArea());
            tvDuration.setText(alert.getDuration());
            tvIssuedBy.setText(alert.getIssuedBy());
            tvInstructions.setText(alert.getInstructions());
            tvAlertStatus.setText(alert.isActive() ? "ACTIVE" : "EXPIRED");

            // Format time
            long timeDiff = System.currentTimeMillis() - alert.getTimestamp();
            String timeText;
            if (timeDiff < 3600000) { // Less than 1 hour
                timeText = (timeDiff / 60000) + " minutes ago";
            } else if (timeDiff < 86400000) { // Less than 1 day
                timeText = (timeDiff / 3600000) + " hours ago";
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
                timeText = sdf.format(new Date(alert.getTimestamp()));
            }
            tvAlertTime.setText(timeText);

            // Set severity background color
            int severityColor;
            switch (alert.getSeverity().toUpperCase()) {
                case "HIGH":
                    severityColor = R.drawable.bg_severity_high;
                    break;
                case "MEDIUM":
                    severityColor = R.drawable.bg_severity_medium;
                    break;
                case "LOW":
                    severityColor = R.drawable.bg_severity_low;
                    break;
                default:
                    severityColor = R.drawable.bg_severity_high;
            }
            tvAlertSeverity.setBackgroundResource(severityColor);
        }
    }
}




