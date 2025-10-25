package com.mzansi.solutions.disasterdetectoralert.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mzansi.solutions.disasterdetectoralert.R;
import com.mzansi.solutions.disasterdetectoralert.models.ForecastDay;
import com.mzansi.solutions.disasterdetectoralert.models.ClothingSuggestion;

import java.util.ArrayList;
import java.util.List;

public class ForecastDayAdapter extends RecyclerView.Adapter<ForecastDayAdapter.ForecastViewHolder> {
    private List<ForecastDay> forecastDays;
    private List<ClothingSuggestion> clothingSuggestions;
    private OnClothingGenerateListener clothingGenerateListener;

    public interface OnClothingGenerateListener {
        void onGenerateClothing(int position, ForecastDay forecastDay);
    }

    public ForecastDayAdapter() {
        this.forecastDays = new ArrayList<>();
        this.clothingSuggestions = new ArrayList<>();
    }

    public void setForecastDays(List<ForecastDay> forecastDays) {
        this.forecastDays = forecastDays;
        notifyDataSetChanged();
    }

    public void setClothingSuggestions(List<ClothingSuggestion> clothingSuggestions) {
        this.clothingSuggestions = clothingSuggestions;
        notifyDataSetChanged();
    }

    public void addClothingSuggestion(int position, ClothingSuggestion suggestion) {
        if (position < clothingSuggestions.size()) {
            clothingSuggestions.set(position, suggestion);
        } else {
            clothingSuggestions.add(suggestion);
        }
        notifyItemChanged(position);
    }

    public void setClothingGenerateListener(OnClothingGenerateListener listener) {
        this.clothingGenerateListener = listener;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast_day, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastDay forecastDay = forecastDays.get(position);
        holder.bind(forecastDay, position);
    }

    @Override
    public int getItemCount() {
        return forecastDays.size();
    }

    public class ForecastViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDay, tvDate, tvMaxTemp, tvMinTemp, tvWeatherDescription;
        private TextView tvPrecipitation, tvWindSpeed, tvHumidity, tvClothingSuggestion;
        private ImageView ivWeatherIcon;
        private LinearLayout layoutClothingSuggestion, layoutClothingLoading;
        private ProgressBar progressBar;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvMaxTemp = itemView.findViewById(R.id.tvMaxTemp);
            tvMinTemp = itemView.findViewById(R.id.tvMinTemp);
            tvWeatherDescription = itemView.findViewById(R.id.tvWeatherDescription);
            tvPrecipitation = itemView.findViewById(R.id.tvPrecipitation);
            tvWindSpeed = itemView.findViewById(R.id.tvWindSpeed);
            tvHumidity = itemView.findViewById(R.id.tvHumidity);
            tvClothingSuggestion = itemView.findViewById(R.id.tvClothingSuggestion);
            ivWeatherIcon = itemView.findViewById(R.id.ivWeatherIcon);
            layoutClothingSuggestion = itemView.findViewById(R.id.layoutClothingSuggestion);
            layoutClothingLoading = itemView.findViewById(R.id.layoutClothingLoading);
            progressBar = itemView.findViewById(R.id.progressBar);
        }

        public void bind(ForecastDay forecastDay, int position) {
            // Set basic weather information
            tvDay.setText(forecastDay.getDayOfWeek());
            tvDate.setText(forecastDay.getFormattedDate());
            tvMaxTemp.setText(String.format("%.0f°C", forecastDay.getMaxTemp()));
            tvMinTemp.setText(String.format("%.0f°C", forecastDay.getMinTemp()));
            tvWeatherDescription.setText(forecastDay.getWeather().getDescription());

            // Set precipitation probability
            int precipProb = (int) forecastDay.getPrecipitationProbability();
            tvPrecipitation.setText(itemView.getContext().getString(
                    R.string.precipitation_chance, precipProb));

            // Set wind speed
            tvWindSpeed.setText(itemView.getContext().getString(
                    R.string.wind_speed_format, forecastDay.getWindSpeed()));

            // Set humidity
            tvHumidity.setText(itemView.getContext().getString(
                    R.string.humidity_format, forecastDay.getHumidity()));

            // Load weather icon
            loadWeatherIcon(forecastDay.getWeather().getIconUrl());

            // Handle clothing suggestion
            ClothingSuggestion suggestion = getClothingSuggestion(position);
            if (suggestion != null) {
                showClothingSuggestion(suggestion);
            } else {
                hideClothingSuggestion();
            }

            // Set up generate button
            itemView.findViewById(R.id.btnGenerateClothing).setOnClickListener(v -> {
                if (clothingGenerateListener != null) {
                    clothingGenerateListener.onGenerateClothing(position, forecastDay);
                }
            });
        }

        private ClothingSuggestion getClothingSuggestion(int position) {
            if (position < clothingSuggestions.size()) {
                return clothingSuggestions.get(position);
            }
            return null;
        }

        private void showClothingSuggestion(ClothingSuggestion suggestion) {
            layoutClothingSuggestion.setVisibility(View.VISIBLE);
            tvClothingSuggestion.setText(suggestion.getSuggestion());
            layoutClothingLoading.setVisibility(View.GONE);
        }

        private void hideClothingSuggestion() {
            layoutClothingSuggestion.setVisibility(View.GONE);
            layoutClothingLoading.setVisibility(View.GONE);
        }

        public void showClothingLoading() {
            layoutClothingLoading.setVisibility(View.VISIBLE);
            layoutClothingSuggestion.setVisibility(View.GONE);
        }

        public void hideClothingLoading() {
            layoutClothingLoading.setVisibility(View.GONE);
        }

        private void loadWeatherIcon(String iconUrl) {
            Glide.with(itemView.getContext())
                    .load(iconUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(ivWeatherIcon);
        }
    }
}
