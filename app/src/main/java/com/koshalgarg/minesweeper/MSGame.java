package com.koshalgarg.minesweeper;


import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Random;

class MSGame {

  private  int rows,cols,noOfBombs;
  private  int[][] bombsPosition;
  private  Grid[][] gridsStatus;
  private  int time=0;
  private  int bombsLeft=0;
    int rewarded=0;

    int moves=0;

    public int getMoves() {
        return moves;
    }

    public void setMoves(int moves) {
        this.moves = moves;
    }

    private int gameOver=0;


    public int getGameOver() {
        return gameOver;
    }

    public void setGameOver(int gameOver) {
        this.gameOver = gameOver;
    }

    public int getRewarded() {
        return rewarded;
    }

    public void setRewarded(int rewarded) {
        this.rewarded = rewarded;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getBombsLeft() {
        return bombsLeft;
    }

    public void setBombsLeft(int bombsLeft) {
        this.bombsLeft = bombsLeft;
    }

    MSGame(int m, int n, int b) {


        rewarded=0;
        rows=m;
        cols=n;
        noOfBombs=b;
        bombsLeft=b;
        bombsPosition=new int[noOfBombs][2];
        gridsStatus=new Grid[rows][cols];

        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++)
            {
                gridsStatus[i][j]=new Grid(0,0,0,0);
            }
        }

        bombsPosition = placeBombs(m,n,b,-1,-1);


        for(int i=0;i<b;i++){
            gridsStatus[bombsPosition[i][0]][bombsPosition[i][1]]=new Grid(0,1,-1,0);
        }


        int x[]={0,0,1,1,1,-1,-1,-1};
        int y[]={1,-1,0,1,-1,0,1,-1};

        for(int i=0;i<m;i++){

            for(int j=0;j<n;j++){

                if(gridsStatus[i][j].getBomb()==1)
                    continue;

                gridsStatus[i][j]=new Grid(0,0,0,0);


                int num=0;

                for(int k=0;k<8;k++){
                    int p=i+x[k];
                    int q=j+y[k];

                    if(p>=0 && p<m && q>=0 && q<n){
                        num+=gridsStatus[p][q].getBomb();
                    }
                }

                gridsStatus[i][j].setNumber(num);

            }
        }

       // Log.i("hg","gh");
    }


    MSGame(int m, int n, int b,int bob_i,int bomb_j) {


        rewarded=0;
        rows=m;
        cols=n;
        noOfBombs=b;
        bombsLeft=b;
        bombsPosition=new int[noOfBombs][2];
        gridsStatus=new Grid[rows][cols];

        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++)
            {
                gridsStatus[i][j]=new Grid(0,0,0,0);
            }
        }

        bombsPosition = placeBombs(m,n,b,bob_i,bomb_j);


        for(int i=0;i<b;i++){
            gridsStatus[bombsPosition[i][0]][bombsPosition[i][1]]=new Grid(0,1,-1,0);
        }


        int x[]={0,0,1,1,1,-1,-1,-1};
        int y[]={1,-1,0,1,-1,0,1,-1};

        for(int i=0;i<m;i++){

            for(int j=0;j<n;j++){

                if(gridsStatus[i][j].getBomb()==1)
                    continue;

                gridsStatus[i][j]=new Grid(0,0,0,0);


                int num=0;

                for(int k=0;k<8;k++){
                    int p=i+x[k];
                    int q=j+y[k];

                    if(p>=0 && p<m && q>=0 && q<n){
                        num+=gridsStatus[p][q].getBomb();
                    }
                }

                gridsStatus[i][j].setNumber(num);

            }
        }

        // Log.i("hg","gh");
    }


    private int[][] placeBombs(int m, int n, int b,int bomb_i,int bomb_j) {

        int[][] bombs=new int[b][2];
        int total=m*n;

        int[] placed=new int[total];

        if(bomb_i>=0 && bomb_j>=0)
        {
            placed[n*bomb_i+bomb_j]=1;
        }


        Random rand=new Random();

        for(int i=0;i<b;i++){
            int a=  Math.abs(rand.nextInt());
            a%=total;

            while (placed[a]==1){
                a++;
                if(a>=total)
                    a=0;
            }

            placed[a]=1;

            bombs[i][0]=a/n;
            bombs[i][1]=a%n;


           // Log.i("a",a+" "+ a/n+ " "+a%n);
        }
        return bombs;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public int getNoOfBombs() {
        return noOfBombs;
    }

    public void setNoOfBombs(int noOfBombs) {
        this.noOfBombs = noOfBombs;
    }

    public int[][] getBombsPosition() {
        return bombsPosition;
    }

    public void setBombsPosition(int[][] bombsPosition) {
        this.bombsPosition = bombsPosition;
    }

    public Grid[][] getGridsStatus() {
        return gridsStatus;
    }

    public void setGridsStatus(Grid[][] gridsStatus) {
        this.gridsStatus = gridsStatus;
    }
}
