package com.example.lab4;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class RaceResultDialog {
    public static void showResult(Context context, List<Integer> top3Images, int rewardCoins) {
        if (top3Images == null || top3Images.size() < 3) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(context); // optional custom style
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_race_result, null);
        builder.setView(view);

        // Get references to views
        ImageView imgFirst = view.findViewById(R.id.imgFirst);
        ImageView imgSecond = view.findViewById(R.id.imgSecond);
        ImageView imgThird = view.findViewById(R.id.imgThird);
        TextView txtReward = view.findViewById(R.id.txtReward);
        Button btnClose = view.findViewById(R.id.btnClose);

        // Set images for top 3
        imgFirst.setImageResource(top3Images.get(0));
        imgSecond.setImageResource(top3Images.get(1));
        imgThird.setImageResource(top3Images.get(2));

        // Set reward text
        txtReward.setText("💰 Bạn được thưởng: " + rewardCoins + " coins");

        // Create and show dialog
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false); // disable outside touch dismiss (optional)
        dialog.show();

        // Close button
        btnClose.setOnClickListener(v -> dialog.dismiss());
    }
}
