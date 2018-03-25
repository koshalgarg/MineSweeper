package com.koshalgarg.minesweeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.gson.Gson;

import java.sql.Time;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener {

    LinearLayout ll;


    LinearLayout[] llRows;
    FrameLayout[][] llGrids;

    int[] numberImages;

    MSGame game;
    MediaPlayer mediaClick, mediaBlast, mediaWin;
    TextView tvBomb, tvTime;
    Timer timer;
    private AdView mAdView;
    private RewardedVideoAd mRewardedVideoAd;
    Button obtnOpenAll;

    SharedPreferences sharedPreferencse;
    SharedPreferences.Editor edit;


    int rows, cols, bombs;
    long timeStamp;
    int clicked_i,clicked_j;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ll = (LinearLayout) findViewById(R.id.ll);

        initialize();
        checkForUpdate();
        if (sharedPreferencse.getString("gameOn", "").equals("1")) {
            String g = sharedPreferencse.getString("game", " ");
            game = new Gson().fromJson(g, MSGame.class);
            
            if (game.getGameOver() == 0 && game.getMoves()>0) {

                newGame();
            } else {
                game = new MSGame(rows, cols, bombs);
                newGame();
            }
        } else {
            game = new MSGame(rows, cols, bombs);
            newGame();
        }
    }

    private void checkForUpdate() {


        long last_time_update_dialog_shown = Long.parseLong(sharedPreferencse.getString("last_time_update_dialog_shown", "0"));
        final long cur = System.currentTimeMillis();

        if (cur - last_time_update_dialog_shown >= 86400000) {

            if(sharedPreferencse.getString("update_available","0").equals("1")){

                if(sharedPreferencse.getString("show_update_dialog","1").equals("1")){
                    showUpdateDialog(sharedPreferencse.getString("url",""));
                }
                edit.putString("last_time_update_dialog_shown", String.valueOf(cur));
                edit.putString("update_available","0");
                edit.apply();
                //Log.i("abc","show");
                //Toast.makeText(this, "show", //Toast.LENGTH_SHORT).show();
            }
            else {

                final String ver = sharedPreferencse.getString("version", "1");
                final String last_update_version=sharedPreferencse.getString("last_update_version", "1");

                final FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
                config.fetch(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            config.activateFetched();
                            String nver = config.getString("version");
                            if (!ver.equals(nver)) {

                                if(last_update_version.equals(nver)){

                                    edit.putString("update_available","1");
                                    edit.putString("url",config.getString("url"));
                                    //Log.i("abc","1");
                                    //Toast.makeText(MainActivity.this, "1", //Toast.LENGTH_SHORT).show();


                                }
                                else{
                                    edit.putString("last_update_version",nver);
                                    edit.putString("update_available","1");
                                    edit.putString("show_update_dialog","1");
                                    edit.putString("url",config.getString("url"));
                                    //Log.i("abc","2");
                                    //Toast.makeText(MainActivity.this, "2", //Toast.LENGTH_SHORT).show();
                                }
                                edit.apply();
                            }
                        }
                    }
                });

            }
        }
    }

    private void showUpdateDialog(final String url) {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.updates_layout, null);

        Button update = (Button) alertLayout.findViewById(R.id.update);
        Button later = (Button) alertLayout.findViewById(R.id.later);

        final CheckBox check= (CheckBox) alertLayout.findViewById(R.id.check);
        TextView check_tv= (TextView) alertLayout.findViewById(R.id.check_text);

        check_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(check.isChecked())
                    check.setChecked(false);
                else
                    check.setChecked(true);
            }
        });


        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertLayout);
        alert.setCancelable(true);
        final AlertDialog dialog = alert.create();
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(check.isChecked()){
                    edit.putString("show_update_dialog","0");
                }
                else{
                    edit.putString("show_update_dialog","1");
                }
                edit.apply();
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
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
        mediaWin = MediaPlayer.create(this, R.raw.explosion); //TODO applause

        tvBomb = (TextView) findViewById(R.id.tvBomb);
        tvTime = (TextView) findViewById(R.id.tvTime);
        obtnOpenAll = (Button) findViewById(R.id.open);

        obtnOpenAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (game.getBombsLeft() != 0)
                    return;

                openAll();

            }
        });

        timer = new Timer();
        sharedPreferencse = getSharedPreferences("myPref",
                Context.MODE_PRIVATE);
        edit = sharedPreferencse.edit();

        rows = Integer.parseInt(sharedPreferencse.getString("rows", "13"));
        cols = Integer.parseInt(sharedPreferencse.getString("cols", "10"));
        bombs = Integer.parseInt(sharedPreferencse.getString("bombs", "25"));

    }


    private void newGame() {

        if (!mRewardedVideoAd.isLoaded())
            loadRewardedVideoAd();

        timeStamp = System.currentTimeMillis();

        obtnOpenAll.setVisibility(View.GONE);

        tvBomb.setText("Mine: " + game.getBombsLeft());
        tvTime.setText(setTime());

        if (llRows != null) {
            for (int i = 0; i < llRows.length; i++) {
                llRows[i].removeAllViews();
            }
        }

        timer.cancel();
        timer = new Timer();
        timer.schedule(new updateTime(), 0, 1000);


        llRows = new LinearLayout[game.getRows()];
        llGrids = new FrameLayout[game.getRows()][game.getCols()];

        for (int i = 0; i < game.getRows(); i++) {
            llRows[i] = (LinearLayout) getLayoutInflater().inflate(R.layout.row, null);

            for (int j = 0; j < game.getCols(); j++) {
                llGrids[i][j] = (FrameLayout) getLayoutInflater().inflate(R.layout.grid, null);
                updateView(i, j);

                final int finalI = i;
                final int finalJ = j;
                llGrids[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {


                        clicked_i=finalI;
                        clicked_j=finalJ;

                        if (game.getGridsStatus()[finalI][finalJ].getOpen() == 0 && game.getGridsStatus()[finalI][finalJ].getFlagged() == 0) {
                            game.setMoves(game.getMoves() + 1);
                            if (game.getGridsStatus()[finalI][finalJ].getBomb() == 1) {

                                if (mRewardedVideoAd.isLoaded() && game.getRewarded() == 0 && game.getMoves()>10) {
                                    showAlertDialog();
                                } else {
                                    gameOver(0);
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

                            flagOrUnflaf(finalI,finalJ);


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

    private void flagOrUnflaf(int finalI, int finalJ) {

        int f = game.getGridsStatus()[finalI][finalJ].getFlagged();
        if (f == 1 && game.getBombsLeft() != game.getNoOfBombs()) {

            game.setMoves(game.getMoves() + 1);

            game.setBombsLeft(game.getBombsLeft() + 1);
            game.getGridsStatus()[finalI][finalJ].setFlagged(0);
            mAdView.setVisibility(View.VISIBLE);
            obtnOpenAll.setVisibility(View.GONE);
        } else if (f == 0 && game.getBombsLeft() != 0) {
            game.setMoves(game.getMoves() + 1);
            game.setBombsLeft(game.getBombsLeft() - 1);
            game.getGridsStatus()[finalI][finalJ].setFlagged(1);

            if (game.getBombsLeft() == 0) {
                obtnOpenAll.setVisibility(View.VISIBLE);
                mAdView.setVisibility(View.GONE);
            }

        }
        tvBomb.setText("Mine: " + game.getBombsLeft());
    }

    private void showAlertDialog() {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.save_layout, null);

        TextView yes = (TextView) alertLayout.findViewById(R.id.btn_yes);
        TextView no = (TextView) alertLayout.findViewById(R.id.btn_no);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertLayout);
        alert.setCancelable(false);
        final AlertDialog dialog = alert.create();
        dialog.show();

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                flagOrUnflaf(clicked_i,clicked_j);
                updateView(clicked_i,clicked_j);

                dialog.dismiss();
                mRewardedVideoAd.show();
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                gameOver(0);
            }
        });
    }


    private void openAll() {
        game.setGameOver(1);

        for (int i = 0; i < game.getRows(); i++) {
            for (int j = 0; j < game.getCols(); j++) {

                Grid grid = game.getGridsStatus()[i][j];
                if (grid.getOpen() == 0) {
                    if (grid.getBomb() == 1 && grid.getFlagged() == 0) {
                        gameOver(0);
                        return;
                    }
                    grid.setOpen(1);
                    updateView(i, j);
                }
            }
        }

        int t = (int) (game.getTime() + (System.currentTimeMillis() - timeStamp) / 1000);
        game.setTime(t);
        timer.cancel();
        //Log.i("time","in open all" + game.getTime());


        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.win_alert, null);
        TextView timwv = (TextView) alertLayout.findViewById(R.id.time);
        timwv.setText(rows+"x"+cols+" Grids "+bombs +" Mines"+"\n"+ tvTime.getText().toString() + "\n" + "Moves: " + game.getMoves());
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertLayout);
        alert.setCancelable(true);
        final AlertDialog dialog = alert.create();
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                game = new MSGame(rows, cols, bombs);
                newGame();
            }
        });


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
                    if (game.getGridsStatus()[p][q].getOpen() == 0 && game.getGridsStatus()[p][q].getBomb() != 1 && game.getGridsStatus()[p][q].getFlagged() != 1) {
                        click(p, q);
                    }
                }
            }
        }
        updateView(i, j);
    }

    private void gameOver(int TimeSet) {

        //TODO remove this
        //to reduce the first click game over
        if(game.getMoves()==1  && Math.abs(new Random().nextInt())%2==0){   //probability 25%
            //Toast.makeText(this, "gameover", Toast.LENGTH_SHORT).show();

            int[] placed=new int[rows*cols];
            placed[cols*clicked_i+clicked_j]=1;

            game=new MSGame(rows,cols,bombs,clicked_i,clicked_j);
            newGame();
            click(clicked_i,clicked_j);
            updateView(clicked_i,clicked_j);
            return;
        }

        game.setGameOver(1);
        mediaBlast.start();
        timer.cancel();

        if (TimeSet == 0) {
            int t = (int) (game.getTime() + (System.currentTimeMillis() - timeStamp) / 1000);
            game.setTime(t);
            //Log.i("time","in gameover" + game.getTime());
        }
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
            if (num == -1) {

                if (grid.getFlagged() == 1)
                    base.setImageResource(R.drawable.bombrevealed);
                else
                    base.setImageResource(R.drawable.bombdeath);

            } else
                base.setImageResource(numberImages[num]);
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {

        //Log.i("time","loaded");

    }

    @Override
    public void onRewardedVideoAdOpened() {


    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {

        loadRewardedVideoAd();

        if (game.getRewarded() == 0) {
            gameOver(1);
        }
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {

        loadRewardedVideoAd();

        game.setRewarded(1);

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        //gameOver(0);
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoCompleted() {

    }

    class updateTime extends TimerTask {
        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (game.getGameOver() == 0)
                        tvTime.setText(setTime());
                }
            });
        }
    }

    private String setTime() {

        long time = game.getTime() + (System.currentTimeMillis() - timeStamp) / 1000;

        String str = "Time: ";

        if (time < 60) {
            String s = String.valueOf(time);
            if (time < 10)
                s = "0" + time;
            str += s;
        } else if (time < 3600) {
            String m = String.valueOf(time / 60);
            String s = String.valueOf(time % 60);

            if (m.length() == 1)
                m = "0" + m;
            if (s.length() == 1)
                s = "0" + s;

            str = str + m + ":" + s;
        } else {
            long h = time / 3600;
            time %= 3600;
            String m = String.valueOf(time / 60);
            String s = String.valueOf(time % 60);

            if (m.length() == 1)
                m = "0" + m;
            if (s.length() == 1)
                s = "0" + s;


            str = str + h + ":" + m + ":" + s;
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
                game = new MSGame(rows, cols, bombs);
                newGame();
                return true;
            case R.id.settings:
                showSettingsDialog();
                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSettingsDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.settings_layout, null);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertLayout);
        alert.setCancelable(true);
        final AlertDialog dialog = alert.create();
        dialog.show();

        final int[] level = {0};

        Button custom= (Button) alertLayout.findViewById(R.id.custom);
        final EditText r= (EditText) alertLayout.findViewById(R.id.rows);
        final EditText c= (EditText) alertLayout.findViewById(R.id.cols);
        final EditText m= (EditText) alertLayout.findViewById(R.id.mines);
        final TextView alertaa = (TextView) alertLayout.findViewById(R.id.alert);




        RadioButton l1 = (RadioButton) alertLayout.findViewById(R.id.l1);
        RadioButton l2 = (RadioButton) alertLayout.findViewById(R.id.l2);
        RadioButton l3 = (RadioButton) alertLayout.findViewById(R.id.l3);

        custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


             String   str_r=r.getText().toString();
             String    str_c=c.getText().toString();
             String   str_m=m.getText().toString();

                if(str_c.length()==0 || str_m.length()==0 || str_r.length()==0)
                    return;



                int ro= Integer.parseInt(str_r);
                int r1= Integer.parseInt(str_c);
                int r2= Integer.parseInt(str_m);

                if(ro <= 0 || r1==0 || r2 == 0 || ro*r1==1){
                    return;
                }

                if (ro>24){
                    alertaa.setVisibility(View.VISIBLE);
                    alertaa.setText("Maximum number of rows is 24");
                }else if (r1>24){
                    alertaa.setText("Maximum number of columns is 24");
                    alertaa.setVisibility(View.VISIBLE);

                }
                else if(r2>=ro*r1){
                    alertaa.setText("Number of mines can not exceed number of grids ");
                    alertaa.setVisibility(View.VISIBLE);
                }
                else {
                    alertaa.setVisibility(View.GONE);

                    dialog.dismiss();

                    edit.putString("rows", String.valueOf(ro));
                    edit.putString("cols", String.valueOf(r1));
                    edit.putString("bombs", String.valueOf(r2));
                    edit.apply();


                    rows = ro;
                    cols = r1;
                    bombs = r2;

                    game = new MSGame(rows, cols, bombs);

                    newGame();

                }


            }
        });

        l1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level[0] = 1;
                dialog.dismiss();

                edit.putString("rows", "13");
                edit.putString("cols", "10");
                edit.putString("bombs", "25");
                edit.apply();


                rows = 13;
                cols = 10;
                bombs = 25;

                game = new MSGame(rows, cols, bombs);

                newGame();
            }
        });


        l2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level[0] = 2;
                dialog.dismiss();
                edit.putString("rows", "16");
                edit.putString("cols", "16");
                edit.putString("bombs", "40");
                edit.apply();

                rows = 16;
                cols = 16;
                bombs = 40;

                game = new MSGame(rows, cols, bombs);

                newGame();


            }
        });

        l3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level[0] = 3;
                dialog.dismiss();

                edit.putString("rows", "24");
                edit.putString("cols", "24");
                edit.putString("bombs", "100");
                edit.apply();

                rows = 24;
                cols = 24;
                bombs = 100;

                game = new MSGame(rows, cols, bombs);
                newGame();

            }
        });

    }

    private void saveGameAndQuit() {


        int t = (int) (game.getTime() + (System.currentTimeMillis() - timeStamp) / 1000);
        game.setTime(t);
        //Log.i("time","in save and quit" + game.getTime() );

        Gson gson = new Gson();
        String s = gson.toJson(game);
        edit.putString("gameOn", "1");
        edit.putString("game", s);
        edit.apply();
    }

    private void loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-6690454024464967/6882012207",
                new AdRequest.Builder().build());


 /*       mRewardedVideoAd.loadAd("ca-app-pub-3940256099942544/5224354917",
                new AdRequest.Builder().build());
 */
    }

    @Override
    public void onBackPressed() {

        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.exit_layout, null);

        TextView yes = (TextView) alertLayout.findViewById(R.id.btn_yes);
        TextView no = (TextView) alertLayout.findViewById(R.id.btn_no);


        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setView(alertLayout);
        alert.setCancelable(true);
        final AlertDialog dialog = alert.create();
        dialog.show();


        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                saveGameAndQuit();
                MainActivity.super.onBackPressed();

            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    @Override
    protected void onPause() {

        timer.cancel();
        int t = (int) (game.getTime() + (System.currentTimeMillis() - timeStamp) / 1000);
        game.setTime(t);
        //Log.i("time","in pause" + game.getTime());


        //Toast.makeText(this, "paused "+game.getTime(), //Toast.LENGTH_SHORT).show();

        super.onPause();
    }

    @Override
    protected void onResume() {

        //Log.i("time","resume"+game.getTime());
        //Toast.makeText(this, "resume "+game.getTime(), //Toast.LENGTH_SHORT).show();
        timer = new Timer();
        timer.schedule(new updateTime(), 1000);
        timeStamp = System.currentTimeMillis();
        tvTime.setText(setTime());
        super.onResume();
    }
}
