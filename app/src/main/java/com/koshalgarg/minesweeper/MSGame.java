package com.koshalgarg.minesweeper;


import android.util.Log;

import java.util.HashMap;
import java.util.Random;

class MSGame {

  private  int rows,cols,noOfBombs;
  private  int[][] bombsPosition;
  private  Grid[][] gridsStatus;
  private  int time=0;
  private  int bombsLeft=0;

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


        rows=m;
        cols=n;
        noOfBombs=b;
        bombsLeft=b;
        bombsPosition=new int[noOfBombs][2];
        gridsStatus=new Grid[rows][cols];

        for(int i=0;i<m;i++){
            for(int j=0;j<n;j++)
            {
                gridsStatus[i][j]=new Grid();
            }
        }

        bombsPosition = placeBombs(m,n,b);


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

        Log.i("hg","gh");
    }

    private int[][] placeBombs(int m, int n, int b) {
        int[][] bombs=new int[b][2];
        int total=m*n;

        int[] placed=new int[total];

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

            bombs[i][0]=a/m;
            bombs[i][1]=a%n;
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
