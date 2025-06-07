package com.example.lab4;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    // UI Elements for Race
    Button btnStartRace;
    TextView txtNoti;
    Handler handler;
    Random random;

    List<Horse> horses;
    Queue<Horse> queue;
    Set<Horse> finishedSet;

    // Auth UI Elements
    EditText etUsername, etPassword;
    Button btnLogin, btnRegister, btnLogout, btnLogoutRacing;
    Button btnPlaceBets, btnAddMoney, btnReset, btnTutorial;
    TextView tvCurrentUserStatus;
    LinearLayout authLayout, raceTrackLayout;

    AuthService authService;

    private static final LinkedHashMap<String, Double> HORSE_ODDS = new LinkedHashMap<>();

    static {
        HORSE_ODDS.put("Horse 1", 2.5);
        HORSE_ODDS.put("Horse 2", 3.0);
        HORSE_ODDS.put("Horse 3", 4.5);
        HORSE_ODDS.put("Horse 4", 5.0);
        HORSE_ODDS.put("Horse 5", 2.0);
        HORSE_ODDS.put("Horse 6", 3.5);
    }

    private ActivityResultLauncher<Intent> bettingActivityResultLauncher;
    private boolean isReturningFromBetting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Race UI components
        btnStartRace = findViewById(R.id.btnStart);
        txtNoti = findViewById(R.id.noti);

        // Initialize Auth UI components
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogout = findViewById(R.id.btnLogout);
        btnTutorial = findViewById(R.id.btnTutorial);
        btnLogoutRacing = findViewById(R.id.btnLogoutRacing);
        tvCurrentUserStatus = findViewById(R.id.tvCurrentUserStatus);
        authLayout = findViewById(R.id.authLayout);
        raceTrackLayout = findViewById(R.id.raceTrackLayout);

        // Initialize betting buttons
        btnPlaceBets = findViewById(R.id.btnGoToBetting);
        btnAddMoney = findViewById(R.id.btnAddMoney);
        btnReset = findViewById(R.id.btnReset);

        authService = new AuthService();

        bettingActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            HashMap<String, Integer> betsPlaced = (HashMap<String, Integer>) data.getSerializableExtra(BettingActivity.EXTRA_BETS_PLACED);
                            double totalBetAmount = data.getDoubleExtra(BettingActivity.EXTRA_TOTAL_BET_AMOUNT, 0.0);

                            for (Horse horse : horses) {
                                String simpleHorseName = horse.getName();
                                if (betsPlaced != null && betsPlaced.containsKey(simpleHorseName)) {
                                    horse.setChosen(true);
                                    horse.setMoneyBet(betsPlaced.get(simpleHorseName));
                                } else {
                                    horse.setChosen(false);
                                    horse.setMoneyBet(0);
                                }
                            }

                            User currentUser = authService.getCurrentUser();
                            if (currentUser != null) {
                                currentUser.setMoney(currentUser.getMoney() - totalBetAmount);
                                isReturningFromBetting = true;
                                updateUIBasedOnAuthState();
                            }
                            Toast.makeText(MainActivity.this, "Bets placed! Ready to race.", Toast.LENGTH_SHORT).show();

                            btnPlaceBets.setVisibility(View.VISIBLE);
                            raceTrackLayout.setVisibility(View.VISIBLE);
                            btnStartRace.setVisibility(View.VISIBLE);
                            btnAddMoney.setVisibility(View.VISIBLE);
                            btnReset.setVisibility(View.VISIBLE);
                            txtNoti.setVisibility(View.VISIBLE);
                            btnLogoutRacing.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Betting cancelled or failed.", Toast.LENGTH_SHORT).show();
                        updateUIBasedOnAuthState();
                    }
                }
        );

        // Initialize horses linked to SeekBars
        List<Integer> seekBarIds = Arrays.asList(
                R.id.seekBar1, R.id.seekBar2, R.id.seekBar3,
                R.id.seekBar4, R.id.seekBar5, R.id.seekBar6
        );

        horses = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Double> entry : HORSE_ODDS.entrySet()) {
            String horseName = entry.getKey();
            SeekBar seekBar = findViewById(seekBarIds.get(i));
            Horse horse = new Horse(horseName, seekBar);
            horses.add(horse);
            i++;
        }

        handler = new Handler();
        random = new Random();
        queue = new LinkedList<>();
        finishedSet = new HashSet<>();

        updateUIBasedOnAuthState();

        // Auth button listeners
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            if (authService.login(username, password)) {
                callClickyButSuccess(v);
                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                updateUIBasedOnAuthState();
            } else {
                callClicky(v);
                Toast.makeText(MainActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        btnReset.setOnClickListener(v -> {
            resetRace();
        });

        btnAddMoney.setOnClickListener(v -> {
            addMoney();
        });

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            if (authService.register(username, password, 100.0)) {
                callClickyButSuccess(v);
                Toast.makeText(MainActivity.this, "Registration Successful! Please login.", Toast.LENGTH_SHORT).show();
                etUsername.setText("");
                etPassword.setText("");
            } else {
                callClicky(v);
                Toast.makeText(MainActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> {
            authService.logout();
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            updateUIBasedOnAuthState();
        });

        btnLogoutRacing.setOnClickListener(v -> {
            logout(v);
            authService.logout();
            Toast.makeText(MainActivity.this, "Logged out", Toast.LENGTH_SHORT).show();
            updateUIBasedOnAuthState();
        });

        // Listener for the "Place Bets!" button
        btnPlaceBets.setOnClickListener(v -> {
            if (!authService.isUserLoggedIn()) {
                Toast.makeText(MainActivity.this, "Please log in to place bets.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if there are existing bets
            boolean hasExistingBets = false;
            for (Horse horse : horses) {
                if (horse.isChosen() && horse.getMoneyBet() > 0) {
                    hasExistingBets = true;
                    break;
                }
            }

            if (hasExistingBets) {
                showPreviousBets();
            } else {
                goToBettingActivity();
            }
        });

        // Listener for the "Start Race!" button
        btnStartRace.setOnClickListener(v -> {
            boolean betsExist = false;
            for (Horse horse : horses) {
                if (horse.isChosen() && horse.getMoneyBet() > 0) {
                    betsExist = true;
                    break;
                }
            }
            if (!betsExist) {
                Toast.makeText(MainActivity.this, "Please place your bets first by tapping 'Place Bets!'.", Toast.LENGTH_LONG).show();
                return;
            }

            startRace(v);
            for (Horse horse : horses) {
                horse.getSeekBar().setProgress(0);
            }
            txtNoti.setText("");
            queue.clear();
            finishedSet.clear();

            btnStartRace.setEnabled(false);
            btnPlaceBets.setEnabled(false);
            btnReset.setEnabled(false);
            btnLogoutRacing.setEnabled(false);

            btnPlaceBets.setVisibility(View.GONE);
            btnReset.setVisibility(View.GONE);
            btnLogoutRacing.setVisibility(View.GONE);

            handler.post(runnable);
        });

        btnTutorial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 Intent tutorialIntent = new Intent(MainActivity.this, TutorialActivity.class);
                 startActivity(tutorialIntent);
            }
        });
    }

    private void showPreviousBets() {
        if (!authService.isUserLoggedIn()) return;

        int totalBet = 0;
        StringBuilder sb = new StringBuilder("Current Bets:\n");
        boolean hasBets = false;

        for (Horse horse : horses) {
            if (horse.isChosen() && horse.getMoneyBet() > 0) {
                hasBets = true;
                sb.append(horse.getName())
                        .append(": $")
                        .append(horse.getMoneyBet())
                        .append(" (Odds: ")
                        .append(HORSE_ODDS.get(horse.getName()))
                        .append(")\n");
                totalBet += horse.getMoneyBet();
            }
        }
        User currentUser = authService.getCurrentUser();
        if (hasBets) {
            int finalTotalBet = totalBet;
            new AlertDialog.Builder(this)
                    .setTitle("Current Bets")
                    .setMessage(sb.toString())
                    .setPositiveButton("Keep Bets", (dialog, which) -> {
                    })
                    .setNeutralButton("Change Bets", (dialog, which) -> {
                        // Proceed to betting activity
                        currentUser.setMoney(currentUser.getMoney() + finalTotalBet);
                        goToBettingActivity();
                    })
                    .show();
        } else {
            goToBettingActivity();
        }
    }

    private void goToBettingActivity() {
        User currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User data not available. Please re-login.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser.getMoney() <= 0) {
            Toast.makeText(this, "You have no money to bet! Add money first.", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(MainActivity.this, BettingActivity.class);
        intent.putExtra(BettingActivity.EXTRA_CURRENT_USER_MONEY, currentUser.getMoney());
        bettingActivityResultLauncher.launch(intent);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            for (Horse horse : horses) {
                if (finishedSet.size() >= horses.size()) {
                    displayRaceResults();
                    updateUIBasedOnAuthState();
                    resetRace();
                    handler.removeCallbacks(this);

                    btnStartRace.setEnabled(true);
                    btnPlaceBets.setEnabled(true);
                    btnLogoutRacing.setEnabled(true);

                    btnPlaceBets.setVisibility(View.VISIBLE);
                    btnReset.setVisibility(View.VISIBLE);
                    btnLogoutRacing.setVisibility(View.VISIBLE);
                    return;
                }
                if (finishedSet.contains(horse))
                    continue;

                int speed = random.nextInt(10) + 1;
                horse.move(speed);

                if (horse.isFinished()) {
                    finishedSet.add(horse);
                    queue.add(horse);

                    StringBuilder sb = new StringBuilder("Race Progress:\n");
                    int i = 1;
                    for (Horse h : queue) {
                        sb.append(i++).append(". ").append(h.getName()).append("\n");
                    }
                    txtNoti.setText(sb.toString());
                }
            }
            if (finishedSet.size() < horses.size()) {
                handler.postDelayed(this, random.nextInt(300) + 200);
            }
        }
    };

    private void addMoney() {
        if (authService.isUserLoggedIn()) {
            User currentUser = authService.getCurrentUser();
            currentUser.setMoney(currentUser.getMoney() + 100);
            tvCurrentUserStatus.setText(currentUser.getUsername() + " - Money: $"
                    + String.format("%.2f", currentUser.getMoney()));
        }

        Toast.makeText(this, "$100 added to your account!", Toast.LENGTH_SHORT).show();
    }

    private void resetRace() {
        // Reset all horses
        for (Horse horse : horses) {
            horse.getSeekBar().setProgress(0);
            horse.setChosen(false);
            horse.setMoneyBet(0);
        }

        // Clear race state
        queue.clear();
        finishedSet.clear();
        handler.removeCallbacks(runnable);

        // Reset UI
        txtNoti.setText("Ready to race!");
        btnStartRace.setEnabled(true);
        btnPlaceBets.setEnabled(true);
        btnLogoutRacing.setEnabled(true);

        // Update user money display if logged in
        if (authService.isUserLoggedIn()) {
            User currentUser = authService.getCurrentUser();
            tvCurrentUserStatus.setText(currentUser.getUsername() + " - Money: $"
                    + String.format("%.2f", currentUser.getMoney()));
        }

        Toast.makeText(this, "Race reset - place new bets!", Toast.LENGTH_SHORT).show();
    }

    private void displayRaceResults() {
        runOnUiThread(() -> {
            if (queue.isEmpty()) {
                txtNoti.setText("Race finished, but no winner detected.");
                return;
            }

            List<Integer> top3Images = new ArrayList<>();
            List<Horse> finishedHorses = new ArrayList<>(queue);
            List<String> horseName = new ArrayList<>();


            for (int i = 0; i < Math.min(3, finishedHorses.size()); i++) {
                top3Images.add(getHorseImageResource(finishedHorses.get(i).getName()));
                horseName.add(finishedHorses.get(i).getName());
            }

            int payout = 0;
            if (!finishedHorses.isEmpty()) {
                if (finishedHorses.get(0).isChosen()) {
                    payout += (int) (finishedHorses.get(0).getMoneyBet() * 2);
                    User currentUser = authService.getCurrentUser();
                    currentUser.setMoney(currentUser.getMoney() + payout);
                }
                if (finishedHorses.get(1).isChosen()) {
                    payout += (int) (finishedHorses.get(1).getMoneyBet() * 1.5);
                    User currentUser = authService.getCurrentUser();
                    currentUser.setMoney(currentUser.getMoney() + payout);
                }
                if (finishedHorses.get(2).isChosen()) {
                    payout += (int) (finishedHorses.get(2).getMoneyBet() * 1.25);
                    User currentUser = authService.getCurrentUser();
                    currentUser.setMoney(currentUser.getMoney() + payout);
                }
            }

            if (!top3Images.isEmpty()) {
                RaceResultDialog.showResult(MainActivity.this, top3Images, payout, horseName);
            } else {
                Toast.makeText(this, "Race finished with no valid results", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getHorseImageResource(String horseName) {
        switch (horseName) {
            case "Horse 1":
                return R.drawable.horse_thumb;
            case "Horse 2":
                return R.drawable.horse_thumb;
            case "Horse 3":
                return R.drawable.horse_thumb;
            case "Horse 4":
                return R.drawable.horse_thumb;
            case "Horse 5":
                return R.drawable.horse_thumb;
            case "Horse 6":
                return R.drawable.horse_thumb;
            default:
                return R.drawable.horse_thumb;
        }
    }

    public void callClicky(View view) {
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.main_activity_sound_button);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        }
    }

    public void callClickyButSuccess(View view) {
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.sucessfully);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        }
    }

    public void logout(View view) {
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.logout);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        }
    }

    public void startRace(View view) {
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.some_horse_shii);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        }
    }

    private void updateUIBasedOnAuthState() {
        if (authService.isUserLoggedIn()) {
            User currentUser = authService.getCurrentUser();
            tvCurrentUserStatus.setText(currentUser.getUsername() + " - Money: $"
                    + String.format("%.2f", currentUser.getMoney()));
            authLayout.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);

            btnPlaceBets.setVisibility(View.VISIBLE);
            raceTrackLayout.setVisibility(View.VISIBLE);
            btnStartRace.setVisibility(View.VISIBLE);
            btnAddMoney.setVisibility(View.VISIBLE);
            btnReset.setVisibility(View.VISIBLE);
            txtNoti.setVisibility(View.VISIBLE);
            btnLogoutRacing.setVisibility(View.VISIBLE);
        } else {
            tvCurrentUserStatus.setText("Not logged in");
            authLayout.setVisibility(View.VISIBLE);
            etUsername.setText("");
            etPassword.setText("");
            btnLogout.setVisibility(View.GONE);

            btnPlaceBets.setVisibility(View.GONE);
            raceTrackLayout.setVisibility(View.GONE);
            btnStartRace.setVisibility(View.GONE);
            btnAddMoney.setVisibility(View.GONE);
            btnReset.setVisibility(View.GONE);
            txtNoti.setVisibility(View.GONE);
            btnLogoutRacing.setVisibility(View.GONE);

            for (Horse horse : horses) {
                horse.setChosen(false);
                horse.setMoneyBet(0);
            }
        }
    }

}