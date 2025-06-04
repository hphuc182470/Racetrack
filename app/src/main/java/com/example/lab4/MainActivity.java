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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button btnStart;

    TextView txtNoti;

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

        SeekBar seekBar1 = findViewById(R.id.seekBar1);
        SeekBar seekBar2 = findViewById(R.id.seekBar2);
        SeekBar seekBar3 = findViewById(R.id.seekBar3);
        btnStart = findViewById(R.id.btnStart);
        txtNoti = (TextView) findViewById(R.id.noti);

        Random random = new Random();

        Handler handler = new Handler();
        Queue<SeekBar> queue = new LinkedList<>();
        Set<SeekBar> finishedSet = new HashSet<>();
        seekBar1.setTag("horse 1");
        seekBar2.setTag("horse 2");
        seekBar3.setTag("horse 3");

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (queue.size() >= 3) {
                    return;
                }

                int speed1 = random.nextInt(10) + 1;
                int speed2 = random.nextInt(10) + 1;
                int speed3 = random.nextInt(10) + 1;

                updateHorseProgress(seekBar1, speed1);
                updateHorseProgress(seekBar2, speed2);
                updateHorseProgress(seekBar3, speed3);

                handler.postDelayed(this, random.nextInt(300) + 200);
            }

            private void updateHorseProgress(SeekBar seekBar, int speed) {
                if (queue.contains(seekBar)) return;

                int current = seekBar.getProgress();
                int max = seekBar.getMax();
                int next = Math.min(current + speed, max);
                seekBar.setProgress(next);

                if (next >= max && !finishedSet.contains(seekBar)) {
                    finishedSet.add(seekBar);
                    queue.add(seekBar);

                    StringBuilder sb = new StringBuilder("Final result:\n");
                    int i = 1;
                    for (SeekBar s : queue) {
                        sb.append(i++).append(". ").append(s.getTag()).append("\n");
                    }
                    txtNoti.setText(sb.toString());
                }
            }
        };

        btnStart.setOnClickListener(v -> {
            seekBar1.setProgress(0);
            seekBar2.setProgress(0);
            seekBar3.setProgress(0);
            txtNoti.setText("");

            queue.clear();
            finishedSet.clear();

            handler.post(runnable);
        });
    }
}