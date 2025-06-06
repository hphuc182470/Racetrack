package com.example.lab4;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

// Imports giữ nguyên

public class MainActivity extends AppCompatActivity {

    Button btnStart;
    TextView txtNoti;
    Handler handler;
    Random random;

    List<Horse> horses;
    Queue<Horse> queue;
    Set<Horse> finishedSet;

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

        btnStart = findViewById(R.id.btnStart);
        txtNoti = findViewById(R.id.noti);

        horses = new ArrayList<>();
        horses.add(new Horse("Horse 1", findViewById(R.id.seekBar1)));
        horses.add(new Horse("Horse 2", findViewById(R.id.seekBar2)));
        horses.add(new Horse("Horse 3", findViewById(R.id.seekBar3)));
        horses.add(new Horse("Horse 4", findViewById(R.id.seekBar4)));
        horses.add(new Horse("Horse 5", findViewById(R.id.seekBar5)));
        horses.add(new Horse("Horse 6", findViewById(R.id.seekBar6)));

        handler = new Handler();
        random = new Random();
        queue = new LinkedList<>();
        finishedSet = new HashSet<>();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (queue.size() >= horses.size()) {
                    handler.removeCallbacks(this);
                    return;
                }

                for (Horse horse : horses) {
                    if (finishedSet.contains(horse)) continue;

                    int speed = random.nextInt(10) + 1;
                    horse.move(speed);

                    if (horse.isFinished()) {
                        finishedSet.add(horse);
                        queue.add(horse);

                        StringBuilder sb = new StringBuilder("Final result:\n");
                        int i = 1;
                        for (Horse h : queue) {
                            sb.append(i++).append(". ").append(h.getName()).append("\n");
                        }
                        txtNoti.setText(sb.toString());
                    }
                }

                handler.postDelayed(this, random.nextInt(300) + 200);
            }
        };

        btnStart.setOnClickListener(v -> {
            for (Horse horse : horses) {
                horse.getSeekBar().setProgress(0);
            }
            txtNoti.setText("");
            queue.clear();
            finishedSet.clear();

            handler.post(runnable);
        });
    }
}
