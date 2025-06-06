package com.example.lab4;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

public class RaceResultDialog {
    public static void showResult(Context context, List<Integer> top3Images, int rewardCoins, List<String> horseName) {
        // Ensure we're on UI thread
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomAlertDialog);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_race_result, null);
        builder.setView(view);

        ImageView imgFirst = view.findViewById(R.id.imgFirst);
        ImageView imgSecond = view.findViewById(R.id.imgSecond);
        ImageView imgThird = view.findViewById(R.id.imgThird);
        TextView txtReward = view.findViewById(R.id.txtReward);
        Button btnClose = view.findViewById(R.id.btnClose);
        TextView txt1stName = view.findViewById(R.id.txtFirstName);
        TextView txt2ndName = view.findViewById(R.id.txtSecondName);
        TextView txt3rdName = view.findViewById(R.id.txtThirdName);

        // Set images safely
        if (top3Images.size() > 0) {
            imgFirst.setImageResource(top3Images.get(0));
            txt1stName.setText((horseName.get(0)));

        }
        if (top3Images.size() > 1) {
            imgSecond.setImageResource(top3Images.get(1));
            txt2ndName.setText(horseName.get(1));
        }
        if (top3Images.size() > 2) {
            imgThird.setImageResource(top3Images.get(2));
            txt3rdName.setText(horseName.get(2));
        }

        txtReward.setText("💰 You won: " + rewardCoins + " coins!");

        AlertDialog dialog = builder.create();
        dialog.show();

        btnClose.setOnClickListener(v -> dialog.dismiss());
    }
}