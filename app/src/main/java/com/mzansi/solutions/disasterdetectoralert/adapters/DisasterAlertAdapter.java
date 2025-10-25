package com.mzansi.solutions.disasterdetectoralert.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mzansi.solutions.disasterdetectoralert.R;
import com.mzansi.solutions.disasterdetectoralert.models.DisasterAlert;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DisasterAlertAdapter extends RecyclerView.Adapter<DisasterAlertAdapter.AlertViewHolder> {

    private List<DisasterAlert> alerts;

    public DisasterAlertAdapter(List<DisasterAlert> alerts) {
        this.alerts = alerts;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_disaster_alert, parent, false);
        return new AlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        DisasterAlert alert = alerts.get(position);
        holder.bind(alert);
    }

    @Override
    public int getItemCount() {
        return alerts.size();
    }

    static class AlertViewHolder extends RecyclerView.ViewHolder {
        private TextView tvAlertTitle;
        private TextView tvAlertDescription;
        private TextView tvAlertTime;
        private TextView tvAlertSeverity;

        public AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAlertTitle = itemView.findViewById(R.id.tvAlertTitle);
            tvAlertDescription = itemView.findViewById(R.id.tvAlertDescription);
            tvAlertTime = itemView.findViewById(R.id.tvAlertTime);
            tvAlertSeverity = itemView.findViewById(R.id.tvAlertSeverity);
        }

        public void bind(DisasterAlert alert) {
            tvAlertTitle.setText(alert.getTitle());
            tvAlertDescription.setText(alert.getDescription());
            tvAlertSeverity.setText(alert.getSeverity());

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
        }
    }
}




