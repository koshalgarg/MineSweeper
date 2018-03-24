package com.koshalgarg.minesweeper;


import com.google.gson.annotations.SerializedName;

class Grid {


    private int open;
    private int bomb;
    private int number;
    private int flagged;

    public int getOpen() {
        return open;
    }

    public void setOpen(int open) {
        this.open = open;
    }

    int getBomb() {
        return bomb;
    }

    public void setBomb(int bomb) {
        this.bomb = bomb;
    }

    int getNumber() {
        return number;
    }


    Grid(int open, int bomb, int number, int flagged) {
        this.open = open;
        this.bomb = bomb;
        this.number = number;
        this.flagged = flagged;
    }

    Grid() {
    }

    void setNumber(int number) {
        this.number = number;
    }

    public int getFlagged() {
        return flagged;
    }

    public void setFlagged(int flagged) {
        this.flagged = flagged;
    }
}
