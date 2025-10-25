package com.mzansi.solutions.disasterdetectoralert.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mzansi.solutions.disasterdetectoralert.R;
import com.mzansi.solutions.disasterdetectoralert.models.ForecastDay;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherForecastAdapter extends RecyclerView.Adapter<WeatherForecastAdapter.ForecastViewHolder> {

    private List<ForecastDay> forecastData;

    public WeatherForecastAdapter(List<ForecastDay> forecastData) {
        this.forecastData = forecastData;
    }

    public void setForecastDays(List<ForecastDay> forecastData) {
        this.forecastData = forecastData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_weather_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastDay forecastDay = forecastData.get(position);
        holder.bind(forecastDay, position);
    }

    @Override
    public int getItemCount() {
        return forecastData.size();
    }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        private TextView tvForecastDay;
        private TextView tvForecastDate;
        private ImageView ivForecastIcon;
        private TextView tvForecastHigh;
        private TextView tvForecastLow;
        private TextView tvForecastDescription;
        private TextView tvForecastPrecipitation;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvForecastDay = itemView.findViewById(R.id.tvForecastDay);
            tvForecastDate = itemView.findViewById(R.id.tvForecastDate);
            ivForecastIcon = itemView.findViewById(R.id.ivForecastIcon);
            tvForecastHigh = itemView.findViewById(R.id.tvForecastHigh);
            tvForecastLow = itemView.findViewById(R.id.tvForecastLow);
            tvForecastDescription = itemView.findViewById(R.id.tvForecastDescription);
            tvForecastPrecipitation = itemView.findViewById(R.id.tvForecastPrecipitation);
        }

        public void bind(ForecastDay forecastDay, int position) {
            // Set day name
            String dayName;
            if (position == 0) {
                dayName = "Tomorrow";
            } else if (position == 1) {
                dayName = "Day After";
            } else {
                dayName = "In " + (position + 1) + " days";
            }
            tvForecastDay.setText(dayName);

            // Set date
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                Date date = inputFormat.parse(forecastDay.getDatetime());
                tvForecastDate.setText(outputFormat.format(date));
            } catch (Exception e) {
                tvForecastDate.setText(forecastDay.getDatetime());
            }

            // Set temperature (using actual temperature data from ForecastDay)
            tvForecastHigh.setText(String.format("%.0f°C", forecastDay.getMaxTemp()));
            tvForecastLow.setText(String.format("%.0f°C", forecastDay.getMinTemp()));

            // Set description
            tvForecastDescription.setText(forecastDay.getWeather().getDescription());

            // Set precipitation probability
            tvForecastPrecipitation.setText(String.format("Precip: %.0f%%", forecastDay.getPrecipitationProbability()));

            // Load weather icon from Weatherbit API
            loadWeatherIcon(forecastDay.getWeather().getIconUrl());
        }

        private void loadWeatherIcon(String iconUrl) {
            Glide.with(itemView.getContext())
                    .load(iconUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivForecastIcon);
        }
    }
}
