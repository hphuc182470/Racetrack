package com.example.lab4;

import android.widget.SeekBar;

public class Horse {
    private final String name;
    private final SeekBar seekBar;
    private boolean isChosen;
    private int moneyBet;

    public Horse(String name, SeekBar seekBar) {
        this.name = name;
        this.seekBar = seekBar;
        this.seekBar.setTag(name);
        this.isChosen = false;
        this.moneyBet = 0;
    }

    public String getName() {
        return name;
    }

    public SeekBar getSeekBar() {
        return seekBar;
    }

    public boolean isFinished() {
        return seekBar.getProgress() >= seekBar.getMax();
    }

    public void move(int speed) {
        int current = seekBar.getProgress();
        int max = seekBar.getMax();
        seekBar.setProgress(Math.min(current + speed, max));
    }

    public boolean isChosen() {
        return isChosen;
    }

    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    public int getMoneyBet() {
        return moneyBet;
    }

    public void setMoneyBet(int moneyBet) {
        this.moneyBet = moneyBet;
    }
}
