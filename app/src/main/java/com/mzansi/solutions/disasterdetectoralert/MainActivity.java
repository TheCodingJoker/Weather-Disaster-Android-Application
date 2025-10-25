package com.mzansi.solutions.disasterdetectoralert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private CardView cardCommunityMember;
    private CardView cardCommunityFarmer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        cardCommunityMember = findViewById(R.id.cardCommunityMember);
        cardCommunityFarmer = findViewById(R.id.cardCommunityFarmer);

        // Set click listeners
        cardCommunityMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRoleSelected("Community Member");
            }
        });

        cardCommunityFarmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRoleSelected("Community Farmer");
            }
        });
    }

    private void onRoleSelected(String role) {
        if ("Community Member".equals(role)) {
            // Navigate to location selection for community members
            Intent intent = new Intent(this, LocationActivity.class);
            intent.putExtra("is_farmer", false);
            startActivity(intent);
        } else if ("Community Farmer".equals(role)) {
            // Navigate to farmer login page
            Intent intent = new Intent(this, FarmerLoginActivity.class);
            startActivity(intent);
        }
    }
}