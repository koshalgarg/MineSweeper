package com.koshalgarg.minesweeper;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener {

    LinearLayout ll;


    LinearLayout[] llRows;
    FrameLayout[][] llGrids;

    int[] numberImages;

    MSGame game;
    MediaPlayer mediaClick, mediaBlast;
    TextView tvBomb, tvTime;
    Timer timer;
    private AdView mAdView;
    private RewardedVideoAd mRewardedVideoAd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ll = (LinearLayout) findViewById(R.id.ll);

        initialize();

        newGame();



    }

    private void newGame() {

        if(!mRewardedVideoAd.isLoaded())
            loadRewardedVideoAd();

        game = new MSGame(16, 10, 15);
        tvBomb.setText("Bomb: " + game.getBombsLeft());
        tvTime.setText(setTime(game.getTime()));

        if(llRows!=null)
        {
            for(int i=0;i<game.getRows();i++){
                llRows[i].removeAllViews();
            }
        }



        timer = new Timer();
        timer.schedule(new  updateTime(), 0,1000);


        llRows = new LinearLayout[game.getRows()];
        llGrids = new FrameLayout[game.getRows()][game.getCols()];

        for (int i = 0; i < game.getRows(); i++) {
            llRows[i] = (LinearLayout) getLayoutInflater().inflate(R.layout.row, null);

            for (int j = 0; j < game.getCols(); j++) {
                llGrids[i][j] = (FrameLayout) getLayoutInflater().inflate(R.layout.grid, null);
                llGrids[i][j].setTag(new Tag(i, j));

                updateView(i, j);

                final int finalI = i;
                final int finalJ = j;
                llGrids[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                       // Toast.makeText(MainActivity.this, ""+((10*finalI)+finalJ), Toast.LENGTH_SHORT).show();

                        if (game.getGridsStatus()[finalI][finalJ].getOpen() == 0 && game.getGridsStatus()[finalI][finalJ].getFlagged() == 0) {
                            if (game.getGridsStatus()[finalI][finalJ].getBomb() == 1) {

                                if(mRewardedVideoAd.isLoaded() && game.getRewarded()==0){
                                    showAlertDialog();
                                }else {
                                    gameOver();
                                }

                            } else {

                                mediaClick.start();
                                click(finalI, finalJ);
                            }
                        }
                    }
                });

                llGrids[i][j].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {

                        if (game.getGridsStatus()[finalI][finalJ].getOpen() == 0) {
                            int f = game.getGridsStatus()[finalI][finalJ].getFlagged();
                            if(f==1){
                                game.setBombsLeft(game.getBombsLeft()+1);
                                game.getGridsStatus()[finalI][finalJ].setFlagged(0);
                            }
                            else{
                                game.setBombsLeft(game.getBombsLeft()-1);
                                game.getGridsStatus()[finalI][finalJ].setFlagged(1);

                            }
                            tvBomb.setText("Bomb: "+game.getBombsLeft());


                            updateView(finalI, finalJ);
                        }
                        return true;
                    }
                });

                llRows[i].addView(llGrids[i][j]);
            }
            ll.addView(llRows[i]);
        }

    }

    private void showAlertDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want continue?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        mRewardedVideoAd.show();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        gameOver();
                    }
                });

        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        alert.setTitle("Save Me !!");
        alert.show();

    }

    private void initialize() {

        MobileAds.initialize(this, "ca-app-pub-6690454024464967~5280800709");
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);

        numberImages = new int[9];
        numberImages[0] = R.drawable.open0;
        numberImages[1] = R.drawable.open1;
        numberImages[2] = R.drawable.open2;
        numberImages[3] = R.drawable.open3;
        numberImages[4] = R.drawable.open4;
        numberImages[5] = R.drawable.open5;
        numberImages[6] = R.drawable.open6;
        numberImages[7] = R.drawable.open7;
        numberImages[8] = R.drawable.open8;

        mediaClick = MediaPlayer.create(this, R.raw.click_x);
        mediaBlast = MediaPlayer.create(this, R.raw.explosion);

        tvBomb = (TextView) findViewById(R.id.tvBomb);
        tvTime = (TextView) findViewById(R.id.tvTime);
    }

    private void click(int i, int j) {
        game.getGridsStatus()[i][j].setOpen(1);
        int x[] = {0, 0, 1, 1, 1, -1, -1, -1};
        int y[] = {1, -1, 0, 1, -1, 0, 1, -1};

        if (game.getGridsStatus()[i][j].getNumber() == 0) {
            for (int k = 0; k < 8; k++) {
                int p = i + x[k];
                int q = j + y[k];

                if (p >= 0 && p < game.getRows() && q >= 0 && q < game.getCols()) {
                    if (game.getGridsStatus()[p][q].getOpen() == 0 && game.getGridsStatus()[p][q].getBomb() != 1) {
                        click(p, q);
                    }
                }
            }
        }
        updateView(i, j);


    }

    private void gameOver() {


        mediaBlast.start();

        timer.cancel();

        Grid[][] gridstatus = game.getGridsStatus();

        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getCols(); j++) {
                gridstatus[i][j].setOpen(1);
                updateView(i, j);
            }
        }
    }

    private void updateView(int i, int j) {


        Grid grid = game.getGridsStatus()[i][j];

        FrameLayout llgrid = llGrids[i][j];

        ImageView base = (ImageView) llgrid.findViewById(R.id.base);
        ImageView top = (ImageView) llgrid.findViewById(R.id.top);

        base.setVisibility(ImageView.VISIBLE);
        top.setVisibility(ImageView.GONE);
        base.setImageResource(R.drawable.blank_tile);


        if (grid.getOpen() == 0) {
            if (grid.getFlagged() == 1) {
                top.setVisibility(View.VISIBLE);
                top.setImageResource(R.drawable.flag);
            } else {
                top.setVisibility(View.GONE);
            }

        } else {
            int num = grid.getNumber();
            if (num == -1){

                if(grid.getFlagged()==1)
                    base.setImageResource(R.drawable.bombrevealed);
                else
                    base.setImageResource(R.drawable.bombdeath);

            }
            else
                base.setImageResource(numberImages[num]);
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        Toast.makeText(this, "Loaded", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        if(game.getRewarded()==0){
            gameOver();
            Toast.makeText(this, "Closed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        game.setRewarded(1);
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        //gameOver();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
      //  Toast.makeText(this, "Failed to load Video", Toast.LENGTH_SHORT).show();
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoCompleted() {

        //Toast.makeText(this, "Completed", Toast.LENGTH_SHORT).show();

    }

    class updateTime extends TimerTask {
        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    game.setTime(game.getTime()+1);
                    tvTime.setText(setTime(game.getTime()));
                }
            });


        }
    }

    private String setTime(int time) {
        String str="Time: ";

        if(time<60){
            String s="";
            if(time<10)
                s="0"+time;
            str+=s;
        }
        else if(time<3600){
            String m= String.valueOf(time/60);
            String s= String.valueOf(time%60);

            if(m.length()==1)
                m="0"+m;
            if(s.length()==1)
                s="0"+s;

            str=str+m+":"+s;
        }
        else{
            int h=time/3600;
            time%=3600;
            String m= String.valueOf(time/60);
            String s= String.valueOf(time%60);

            if(m.length()==1)
                m="0"+m;
            if(s.length()==1)
                s="0"+s;


            str=str+h+":"+m+":"+s;
        }
        return str;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.restart:
                newGame();
                return true;
            case R.id.save:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

/*
    @Override
    protected void onResume() {

        if(timer!=null)
            timer.schedule(new updateTime(),1000);

        super.onResume();
    }

    @Override
    protected void onPause() {

        timer.cancel();

        super.onPause();
    }*/

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());
    }
}
