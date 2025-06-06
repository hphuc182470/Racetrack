package com.example.lab4;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class BettingActivity extends AppCompatActivity {

    // UI elements
    private TextView totalBetText;
    private Button placeBetButton;
    private Map<String, CheckBox> horseCheckBoxes;
    private Map<String, EditText> horseBetAmounts;

    // Data
    private double currentUserMoney;
    private double currentTotalBet;

    // Keys for Intent data to pass between Activities
    public static final String EXTRA_CURRENT_USER_MONEY = "extra_current_user_money";
    public static final String EXTRA_BETS_PLACED = "extra_bets_placed";
    public static final String EXTRA_TOTAL_BET_AMOUNT = "extra_total_bet_amount";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_betting);

        currentUserMoney = getIntent().getDoubleExtra(EXTRA_CURRENT_USER_MONEY, 0.0);
        if (currentUserMoney <= 0) {
            Toast.makeText(this, "You have no money to bet!", Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        totalBetText = findViewById(R.id.totalBetText);
        placeBetButton = findViewById(R.id.placeBetButton);

        horseCheckBoxes = new HashMap<>();
        horseBetAmounts = new HashMap<>();

        Map<String, Integer> checkBoxIds = new HashMap<>();
        checkBoxIds.put("Horse 1", R.id.horse1_check);
        checkBoxIds.put("Horse 2", R.id.horse2_check);
        checkBoxIds.put("Horse 3", R.id.horse3_check);
        checkBoxIds.put("Horse 4", R.id.horse4_check);
        checkBoxIds.put("Horse 5", R.id.horse5_check);
        checkBoxIds.put("Horse 6", R.id.horse6_check);

        Map<String, Integer> amountIds = new HashMap<>();
        amountIds.put("Horse 1", R.id.horse1_amount);
        amountIds.put("Horse 2", R.id.horse2_amount);
        amountIds.put("Horse 3", R.id.horse3_amount);
        amountIds.put("Horse 4", R.id.horse4_amount);
        amountIds.put("Horse 5", R.id.horse5_amount);
        amountIds.put("Horse 6", R.id.horse6_amount);

        for (int i = 1; i <= 6; i++) {
            String horseName = "Horse " + i;

            CheckBox checkBox = findViewById(checkBoxIds.get(horseName));
            EditText betAmount = findViewById(amountIds.get(horseName));

            if (checkBox != null && betAmount != null) {
                horseCheckBoxes.put(horseName, checkBox);
                horseBetAmounts.put(horseName, betAmount);

                betAmount.setEnabled(false);

                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    betAmount.setEnabled(isChecked);
                    if (!isChecked) {
                        betAmount.setText("0");
                    }
                    updateTotalBet();
                });

                betAmount.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override public void afterTextChanged(Editable s) {
                        updateTotalBet();
                    }
                });
            }
        }

        updateTotalBet();

        placeBetButton.setOnClickListener(v -> placeBets());
    }

    private void updateTotalBet() {
        currentTotalBet = 0.0;
        for (Map.Entry<String, CheckBox> entry : horseCheckBoxes.entrySet()) {
            String horseName = entry.getKey();
            CheckBox checkBox = entry.getValue();
            EditText betAmountEt = horseBetAmounts.get(horseName);

            if (checkBox.isChecked() && betAmountEt != null) {
                String betStr = betAmountEt.getText().toString().trim();
                if (!betStr.isEmpty()) {
                    try {
                        currentTotalBet += Double.parseDouble(betStr);
                    } catch (NumberFormatException e) {
                        // Ignore invalid input
                    }
                }
            }
        }
        totalBetText.setText("Total bet: $" + String.format("%.2f", currentTotalBet));
    }

    private void placeBets() {
        HashMap<String, Integer> betsPlaced = new HashMap<>();

        int chosenHorseCount = 0;
        for (Map.Entry<String, CheckBox> entry : horseCheckBoxes.entrySet()) {
            String horseName = entry.getKey();
            CheckBox checkBox = entry.getValue();
            EditText betAmountEt = horseBetAmounts.get(horseName);

            if (checkBox.isChecked()) {
                chosenHorseCount++;
                String betStr = betAmountEt.getText().toString().trim();
                if (betStr.isEmpty() || Double.parseDouble(betStr) <= 0) {
                    Toast.makeText(this, "Please enter a valid bet amount for selected horses.", Toast.LENGTH_SHORT).show();
                    return;
                }
                int bet = (int) Double.parseDouble(betStr);
                betsPlaced.put(horseName, bet);
            }
        }

        if (chosenHorseCount == 0) {
            Toast.makeText(this, "Please select at least one horse to bet on.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentTotalBet > currentUserMoney) {
            Toast.makeText(this, "Insufficient funds! You have $" + String.format("%.2f", currentUserMoney), Toast.LENGTH_LONG).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_BETS_PLACED, betsPlaced);
        resultIntent.putExtra(EXTRA_TOTAL_BET_AMOUNT, currentTotalBet);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
