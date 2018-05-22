package com.michael.numbersensegame;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    int orientation;
    float bitmapWidthFactor;
    float bitmapHeightFactor;
    float bitmapTextSizeFactor;
    float textSizeFactor;

    MediaPlayer mediaPlayer_right_1;
    MediaPlayer mediaPlayer_wrong_1;
    MediaPlayer mediaPlayer_finished_1;
    MediaPlayer mediaPlayer_start;
    MediaPlayer mediaPlayer_level_change;

    final Handler handler = new Handler();

    ImageButton helpImageButton;
    TextView gameHeader;
    ImageButton menuImageButton;

    TextView rightHeaderTextView;
    TextView totalHeaderTextView;
    TextView wrongHeaderTextView;
    TextView rightScoreTextView;
    TextView totalScoreTextView;
    TextView wrongScoreTextView;

    ImageView imageViewLeft;
    ImageView imageViewRight;

    Button nextButton;

    Button blueButton;
    TextView gameMessageTextView;
    Button redButton;
    Spinner gameLevelSpinner;

    CustomAdapter<String> gameLevelAdapter;

    int screenPixelWidth;
    int screenPixelHeight;

    Bitmap bitmapRight;
    Canvas canvasRight;

    Paint paintCircle1;
    Paint paintCircle2;

    Bitmap bitmapLeft;
    Canvas canvasLeft;
    Random r;
    int randomRadius;


    int bitmapWidth;
    int bitmapHeight;

    int tempInt;

    PopupWindow pw;
    RadioGroup timeRadioGroup;
    CheckBox soundsCheckBox;
    RadioGroup dotsRadioGroup;
    Button popupOKButton;

    ArrayList<Integer> xCoordinates= new ArrayList<Integer>();
    ArrayList<Integer> yCoordinates= new ArrayList<Integer>();
    boolean nextLevel =  false;
    int gameLevel = 0;
    int numberDotsBlue;
    int numberDotsRed;
    int rightScore = 0;
    int totalScore = 0;
    int wrongScore = 0;
    boolean isSound =  true;
    int displayTimeCode;
    int displayTime;
    int maxDotsCode;
    int maxDotsNumber;

    boolean isNextButton = true;
    boolean isBlueButton = false;
    boolean isRedButton = false;

    PopupWindow aboutPopupWindow;
    Button aboutOKButton;

    DBHelper dbHelper;

    private static final String DATABASE_NAME = "NumberSense.db";
    private static final String TABLE_NAME = "number_sense_settings";
    private static final String DISPLAY_TIME = "display_time";
    private static final String IS_SOUND = "is_sound";
    private static final String MAX_DOTS = "max_dots";
    private static final String GAME_LEVEL = "game_level";
    private static final String[] COLUMN_NAMES = {DISPLAY_TIME, IS_SOUND, MAX_DOTS, GAME_LEVEL};
    private static final String[] COLUMN_DATA_TYPES = {"INTEGER", "INTEGER", "INTEGER", "INTEGER"};
    private static final int[] INSERT_VALUES = {Integer.MAX_VALUE, 1, 9, 0};

    int remainingDisplayTime;
    long time;

    final Handler handler2 = new Handler();
    final Runnable run =  new Runnable() {
        @Override
        public void run() {
            canvasLeft.drawColor(Color.WHITE);
            canvasRight.drawColor(Color.WHITE);
            imageViewLeft.setImageBitmap(bitmapLeft);
            imageViewRight.setImageBitmap(bitmapRight);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        r = new Random(System.currentTimeMillis());

        orientation = getResources().getConfiguration().orientation;
        if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            bitmapWidthFactor = 0.45f;
            bitmapHeightFactor = 0.4f;
            bitmapTextSizeFactor = 0.07f;
            textSizeFactor = 0.04f;
         }else{
            bitmapWidthFactor = 0.41f;
            bitmapHeightFactor = 0.45f;
            bitmapTextSizeFactor = 0.055f;
            textSizeFactor = 0.04f;
        }

        screenPixelWidth = (int)(getResources().getDisplayMetrics().widthPixels);
        screenPixelHeight = (int)(getResources().getDisplayMetrics().heightPixels);



        if(screenPixelWidth < 801 && orientation == Configuration.ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if(screenPixelWidth > screenPixelHeight){
            int temp = screenPixelWidth;
            screenPixelWidth = screenPixelHeight;
            screenPixelHeight = temp;
        }

        float heightWidthRatio = (float)screenPixelHeight / (float)screenPixelWidth;
        if(heightWidthRatio > 1.3 && heightWidthRatio < 1.5){
            bitmapWidthFactor = 0.30f;
            bitmapHeightFactor = 0.65f;
            bitmapTextSizeFactor = 0.035f;
        }


        rightScore = 0;
        totalScore = 0;
        wrongScore = 0;
        nextLevel = false;

        dbHelper = new DBHelper(this, DATABASE_NAME);

        if(!dbHelper.isDatabaseExists(this, DATABASE_NAME)){
            displayTimeCode = 3;
            displayTime = Integer.MAX_VALUE;
            isSound = true;
            maxDotsCode = 0;
            maxDotsNumber = 9;
            gameLevel = 0;

            dbHelper.createTableIfNotExists(TABLE_NAME, COLUMN_NAMES, COLUMN_DATA_TYPES);
            dbHelper.insertTableRow(TABLE_NAME, COLUMN_NAMES, INSERT_VALUES);
        }else{
            displayTime = dbHelper.getTableValue(TABLE_NAME, DISPLAY_TIME, 0);
            switch(displayTime){
                case 2000: displayTimeCode = 0; break;
                case 4000: displayTimeCode = 1; break;
                case 6000: displayTimeCode = 2; break;
                default: displayTimeCode = 3; break;
            }

            isSound = dbHelper.getTableValue(TABLE_NAME, IS_SOUND, 0) == 1;

            maxDotsNumber = dbHelper.getTableValue(TABLE_NAME, MAX_DOTS, 0);

            switch(maxDotsNumber){
                case 11: maxDotsCode = 1; break;
                case 13: maxDotsCode = 2; break;
                case 15: maxDotsCode = 3; break;
                default: maxDotsCode = 0; break;
            }

            gameLevel = dbHelper.getTableValue(TABLE_NAME, GAME_LEVEL, 0);
        }


            gameHeader = (TextView) findViewById(R.id.gameHeader);
            gameHeader.setTextSize(0, (screenPixelWidth * (textSizeFactor + 0.01f)));

            menuImageButton = (ImageButton) findViewById(R.id.menuImageButton);
            menuImageButton.setOnClickListener(MenuImageButtonHandler);

            helpImageButton = (ImageButton) findViewById(R.id.helpImageButton);
            helpImageButton.setOnClickListener(HelpImageButtonHandler);


            rightHeaderTextView = (TextView) findViewById(R.id.rightHeaderTextView);
            rightHeaderTextView.setTextSize(0, (screenPixelWidth * (textSizeFactor + 0.01f)));
            totalHeaderTextView = (TextView) findViewById(R.id.totalHeaderTextView);
            totalHeaderTextView.setTextSize(0, (screenPixelWidth * (textSizeFactor + 0.01f)));
            wrongHeaderTextView = (TextView) findViewById(R.id.wrongHeaderTextView);
            wrongHeaderTextView.setTextSize(0, (screenPixelWidth * (textSizeFactor + 0.01f)));


            rightScoreTextView = (TextView) findViewById(R.id.rightScoreTextView);
            rightScoreTextView.setTextSize(0, (screenPixelWidth * (textSizeFactor + 0.02f)));
            totalScoreTextView = (TextView) findViewById(R.id.totalScoreTextView);
            totalScoreTextView.setTextSize(0, (screenPixelWidth * (textSizeFactor + 0.02f)));
            wrongScoreTextView = (TextView) findViewById(R.id.wrongScoreTextView);
            wrongScoreTextView.setTextSize(0, (screenPixelWidth * (textSizeFactor + 0.02f)));



            imageViewLeft = (ImageView) findViewById(R.id.imageViewLeft);
            imageViewRight = (ImageView) findViewById(R.id.imageViewRight);


            gameLevelSpinner = (Spinner) findViewById(R.id.gameLevelSpinner);
            gameLevelAdapter = new CustomAdapter<String>(this, R.layout.spinner_text_view_game_level, getResources().getStringArray(R.array.game_level), screenPixelWidth, textSizeFactor);


            gameLevelSpinner.setAdapter(gameLevelAdapter);
            gameLevelSpinner.setOnItemSelectedListener(this);
            gameLevelSpinner.setBackgroundResource(R.drawable.spinner);
            gameLevelSpinner.setPopupBackgroundResource(R.drawable.spinner);
            gameLevelSpinner.setSelection(gameLevel);

            bitmapWidth = (int) (screenPixelWidth * bitmapWidthFactor);
            bitmapHeight = screenPixelHeight < 2395 ? (int) (screenPixelHeight * bitmapHeightFactor) : (int) (screenPixelHeight * (bitmapHeightFactor +0.075));

            bitmapLeft = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            canvasLeft = new Canvas(bitmapLeft);

            bitmapRight = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
            canvasRight = new Canvas(bitmapRight);


            imageViewLeft.setImageBitmap(bitmapLeft);
            imageViewRight.setImageBitmap(bitmapRight);


            paintCircle1 = new Paint();
            paintCircle1.setStyle(Paint.Style.FILL);
            paintCircle1.setColor(Color.BLUE);

            paintCircle2 = new Paint();
            paintCircle2.setStyle(Paint.Style.FILL);
            paintCircle2.setColor(Color.RED);


            tempInt = (int) (bitmapWidth * 0.063);
            for (int i = 0; i < 15; i++) {
                xCoordinates.add(tempInt);
                tempInt += (int) (bitmapWidth * 0.063);

            }

            tempInt = (int) (bitmapHeight * 0.063);
            for (int i = 0; i < 15; i++) {
                yCoordinates.add(tempInt);
                tempInt += (int) (bitmapHeight * 0.063);

            }


        gameMessageTextView = (TextView) findViewById(R.id.gameMessageTextView);
        gameMessageTextView.setTextSize(0, (screenPixelWidth * textSizeFactor));

        nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setTextSize(0, (screenPixelWidth * textSizeFactor));
        nextButton.setMinimumWidth((int) (screenPixelWidth * 0.15f));
        nextButton.setMinimumHeight((int) (screenPixelWidth * 0.1f));

        blueButton = (Button) findViewById(R.id.blueButton);
        blueButton.setTextSize(0, (screenPixelWidth * textSizeFactor));
        blueButton.setMinimumWidth((int) (screenPixelWidth * 0.15f));
        blueButton.setMinimumHeight((int) (screenPixelWidth * 0.1f));


        redButton = (Button) findViewById(R.id.redButton);
        redButton.setTextSize(0, (screenPixelWidth * textSizeFactor));
        redButton.setMinimumWidth((int) (screenPixelWidth * 0.15f));
        redButton.setMinimumHeight((int) (screenPixelWidth * 0.1f));




        if(savedInstanceState == null || savedInstanceState.getString("nextButtonText").equals("START")){

            nextButton.setOnClickListener(NextButtonHandler);

            blueButton.setBackgroundColor(Color.parseColor("#60FFFFFF"));

            redButton.setBackgroundColor(Color.parseColor("#60FFFFFF"));

            displayGameInstructions();

            createMediaPlayers();
        }else{
            nextLevel = savedInstanceState.getBoolean("nextLevel", false);
            numberDotsBlue = savedInstanceState.getInt("numberDotsBlue", 9);
            numberDotsRed = savedInstanceState.getInt("numberDotsRed", 5);
            rightScore = savedInstanceState.getInt("rightScore", 0);
            totalScore = savedInstanceState.getInt("totalScore", 0);
            wrongScore = savedInstanceState.getInt("wrongScore", 0);
            isSound =  savedInstanceState.getBoolean("isSound", true);

            paintBlueDots(numberDotsBlue);
            paintRedDots(numberDotsRed);

            time = savedInstanceState.getLong("time", System.currentTimeMillis());
            remainingDisplayTime = savedInstanceState.getInt("remainingDisplayTime", Integer.MAX_VALUE);
            handler2.postDelayed(run, remainingDisplayTime);


            isNextButton = savedInstanceState.getBoolean("isNextButton", true);
            isBlueButton = savedInstanceState.getBoolean("isBlueButton", false);
            isRedButton = savedInstanceState.getBoolean("isRedButton", false);


            rightScoreTextView.setText(String.valueOf(rightScore));
            totalScoreTextView.setText(String.valueOf(totalScore));
            wrongScoreTextView.setText(String.valueOf(wrongScore));
            gameMessageTextView.setText(savedInstanceState.getString("gameTextMessage",
                    Html.fromHtml("Which&#160;side") + "\n" + Html.fromHtml("has&#160;more?")));

            if(isSound){
                createMediaPlayers();
            }

            if(isNextButton){
                nextButton.setText(savedInstanceState.getString("nextButtonText", "NEXT"));
                nextButton.setOnClickListener(NextButtonHandler);
            }else{
                nextButton.setText(savedInstanceState.getString("nextButtonText", "NEXT"));
                nextButton.setBackgroundColor(Color.parseColor("#60FFFFFF"));
            }

            if(isBlueButton){
                blueButton.setOnClickListener(BlueButtonHandler);
                blueButton.setBackgroundResource(R.drawable.blue_button);
            }else{
                blueButton.setBackgroundColor(Color.parseColor("#60FFFFFF"));
            }

            if(isRedButton){
                redButton.setOnClickListener(RedButtonHandler);
                redButton.setBackgroundResource(R.drawable.red_button);
            }else{
                redButton.setBackgroundColor(Color.parseColor("#60FFFFFF"));
            }


        }

    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean("nextLevel", nextLevel);
        savedInstanceState.putInt("numberDotsBlue", numberDotsBlue);
        savedInstanceState.putInt("numberDotsRed", numberDotsRed);
        savedInstanceState.putInt("rightScore", rightScore);
        savedInstanceState.putInt("totalScore", totalScore);
        savedInstanceState.putInt("wrongScore", wrongScore);
        savedInstanceState.putBoolean("isSound", isSound);

        savedInstanceState.putLong("time", time);
        remainingDisplayTime = (int)(displayTime + time - System.currentTimeMillis());

        savedInstanceState.putInt("remainingDisplayTime", remainingDisplayTime > 0 ? remainingDisplayTime : 0);
        savedInstanceState.putBoolean("isNextButton", isNextButton);
        savedInstanceState.putString("nextButtonText", nextButton.getText().toString());
        savedInstanceState.putBoolean("isBlueButton", isBlueButton);
        savedInstanceState.putBoolean("isRedButton", isRedButton);
        savedInstanceState.putString("gameTextMessage", gameMessageTextView.getText().toString());
    }




    private void createMediaPlayers(){
        if(isSound) {
            mediaPlayer_right_1 = MediaPlayer.create(this, R.raw.yes_sound);
            mediaPlayer_wrong_1 = MediaPlayer.create(this, R.raw.no_sound);
            mediaPlayer_finished_1 = MediaPlayer.create(this, R.raw.end_music);
            mediaPlayer_start = MediaPlayer.create(this, R.raw.start_bell);
            mediaPlayer_level_change = MediaPlayer.create(this, R.raw.level_change);
        }
    }

    private void destroyMediaPlayers(){
        if(mediaPlayer_right_1 != null){mediaPlayer_right_1.release(); mediaPlayer_right_1 = null;}
        if(mediaPlayer_wrong_1 != null){mediaPlayer_wrong_1.release(); mediaPlayer_wrong_1 = null;}
        if(mediaPlayer_finished_1 != null){mediaPlayer_finished_1.release(); mediaPlayer_finished_1 = null;}
        if(mediaPlayer_start != null){mediaPlayer_start.release(); mediaPlayer_start = null;}
        if(mediaPlayer_level_change != null){mediaPlayer_level_change.release(); mediaPlayer_level_change = null;}
    }


    @Override
    public void onPause(){
        super.onPause();
        if(pw != null){
            pw.dismiss();
        }
        if(aboutPopupWindow != null){
            aboutPopupWindow.dismiss();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        if(pw != null){
            pw.dismiss();
        }
        if(aboutPopupWindow != null){
            aboutPopupWindow.dismiss();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(pw != null){
            pw.dismiss();
        }
        if(aboutPopupWindow != null){
            aboutPopupWindow.dismiss();
        }

        destroyMediaPlayers();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onClick(View v) {}



    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.gameLevelSpinner){

            if(gameLevel != position) {
                gameLevel = position;
                rightScore = 0;
                totalScore = 0;
                wrongScore = 0;

                rightScoreTextView.setText(String.valueOf(rightScore));
                totalScoreTextView.setText(String.valueOf(totalScore));
                wrongScoreTextView.setText(String.valueOf(wrongScore));

                if((blueButton != null && redButton != null) && nextButton != null) {
                    nextButton.setText("START");
                    nextButton.setBackgroundResource(R.drawable.header);
                    nextButton.setOnClickListener(NextButtonHandler);
                    isNextButton = true;

                    blueButton.setOnClickListener(null);
                    blueButton.setBackgroundColor(Color.parseColor("#60FFFFFF"));
                    redButton.setOnClickListener(null);
                    redButton.setBackgroundColor(Color.parseColor("#60FFFFFF"));
                }
                displayGameInstructions();
                if(isSound && mediaPlayer_level_change != null) {
                    if (mediaPlayer_level_change.isPlaying()) {
                        mediaPlayer_level_change.stop();
                        mediaPlayer_level_change.start();
                    } else {
                        mediaPlayer_level_change.start();
                    }
                }
            }

            dbHelper.updateTableValue(TABLE_NAME, GAME_LEVEL, 0, gameLevel);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}


    View.OnClickListener BlueButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(numberDotsBlue > numberDotsRed){
                rightScore++;
                totalScore++;
                if(isSound) {
                    if (mediaPlayer_right_1.isPlaying()) {
                        mediaPlayer_right_1.stop();
                        mediaPlayer_right_1.start();
                    } else {
                        mediaPlayer_right_1.start();
                    }
                }

            }else{
                wrongScore++;
                totalScore++;
                if(isSound) {
                    if (mediaPlayer_wrong_1.isPlaying()) {
                        mediaPlayer_wrong_1.stop();
                        mediaPlayer_wrong_1.start();
                    } else {
                        mediaPlayer_wrong_1.start();
                    }
                }
            }
            checkFinish();
        }
    };

    View.OnClickListener RedButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(numberDotsBlue < numberDotsRed){
                rightScore++;
                totalScore++;
                if(isSound) {
                    if (mediaPlayer_right_1.isPlaying()) {
                        mediaPlayer_right_1.stop();
                        mediaPlayer_right_1.start();
                    } else {
                        mediaPlayer_right_1.start();
                    }
                }
            }else{
                wrongScore++;
                totalScore++;
                if(isSound) {
                    if (mediaPlayer_wrong_1.isPlaying()) {
                        mediaPlayer_wrong_1.stop();
                        mediaPlayer_wrong_1.start();
                    } else {
                        mediaPlayer_wrong_1.start();
                    }
                }
            }
            checkFinish();
        }
    };




    View.OnClickListener HelpImageButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            initiateAboutWindow();
        }
    };


    View.OnClickListener MenuImageButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            initiatePopupWindow();
        }
    };




    View.OnClickListener NextButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if(!nextButton.getText().toString().equals("NEXT")) {
                gameMessageTextView.setText(Html.fromHtml("Which&#160;side") + "\n" + Html.fromHtml("has&#160;more?"));
                nextButton.setText("NEXT");

                rightScore = 0;
                totalScore = 0;
                wrongScore = 0;

                rightScoreTextView.setText(String.valueOf(rightScore));
                totalScoreTextView.setText(String.valueOf(totalScore));
                wrongScoreTextView.setText(String.valueOf(wrongScore));
            }

            if(isSound) {
                if (mediaPlayer_start.isPlaying()) {
                    mediaPlayer_start.stop();
                    mediaPlayer_start.start();
                } else {
                    mediaPlayer_start.start();
                }
            }

            if(nextLevel){
                gameLevelSpinner.setSelection(gameLevel);
            }

            canvasLeft.drawColor(Color.WHITE);
            canvasRight.drawColor(Color.WHITE);




            switch(gameLevel){
                case 0:
                    if(r.nextInt(2) == 1){
                        numberDotsBlue = maxDotsNumber;
                        numberDotsRed = maxDotsNumber - 4;
                    }else{
                        numberDotsBlue = maxDotsNumber - 4;
                        numberDotsRed = maxDotsNumber;
                    }
                    break;
                case 1:
                    if(r.nextInt(2) == 1){
                        numberDotsBlue = maxDotsNumber;
                        numberDotsRed = maxDotsNumber - 3;
                    }else{
                        numberDotsBlue = maxDotsNumber - 3;
                        numberDotsRed = maxDotsNumber;
                    }
                    break;
                case 2:
                    if(r.nextInt(2) == 1){
                        numberDotsBlue = maxDotsNumber;
                        numberDotsRed = maxDotsNumber - 2;
                    }else{
                        numberDotsBlue = maxDotsNumber - 2;
                        numberDotsRed = maxDotsNumber;
                    }
                    break;
                case 3:
                    if(r.nextInt(2) == 1){
                        numberDotsBlue = maxDotsNumber;
                        numberDotsRed = maxDotsNumber - 1;
                    }else{
                        numberDotsBlue = maxDotsNumber - 1;
                        numberDotsRed = maxDotsNumber;
                    }
                    break;
                default: break;
            }

            paintBlueDots(numberDotsBlue);
            paintRedDots(numberDotsRed);


            blueButton.setOnClickListener(BlueButtonHandler);
            redButton.setOnClickListener(RedButtonHandler);

            nextButton.setBackgroundColor(Color.parseColor("#60FFFFFF"));
            nextButton.setOnClickListener(null);
            isNextButton = false;
            blueButton.setBackgroundResource(R.drawable.blue_button);
            isBlueButton = true;
            redButton.setBackgroundResource(R.drawable.red_button);
            isRedButton = true;

            handler2.removeCallbacks(run);
            time = System.currentTimeMillis();
            handler2.postDelayed(run, displayTime);
        }

    };



    public void paintBlueDots(int dotsNumber){
        r = new Random(System.currentTimeMillis());
        Collections.shuffle(xCoordinates);
        Collections.shuffle(yCoordinates);

        for(int i = 0; i < dotsNumber; i++) {
            randomRadius = r.nextInt((int)(bitmapWidth*0.02)) + (int)(bitmapWidth*0.025);

            canvasLeft.drawCircle(xCoordinates.get(i), yCoordinates.get(i), randomRadius, paintCircle1);
        }
        imageViewLeft.setImageBitmap(bitmapLeft);


    }


    public void paintRedDots(int dotsNumber){
        r = new Random(System.currentTimeMillis());
        Collections.shuffle(xCoordinates);
        Collections.shuffle(yCoordinates);


        for(int i = 0; i < dotsNumber; i++) {
            randomRadius = r.nextInt((int)(bitmapWidth*0.02)) + (int)(bitmapWidth*0.025);

            canvasRight.drawCircle(xCoordinates.get(i), yCoordinates.get(i), randomRadius, paintCircle2);

        }
        imageViewRight.setImageBitmap(bitmapRight);
    }


    public void checkFinish(){

        rightScoreTextView.setText(String.valueOf(rightScore));
        totalScoreTextView.setText(String.valueOf(totalScore));
        wrongScoreTextView.setText(String.valueOf(wrongScore));

        blueButton.setOnClickListener(null);
        redButton.setOnClickListener(null);
        blueButton.setBackgroundColor(Color.parseColor("#60FFFFFF"));
        redButton.setBackgroundColor(Color.parseColor("#60FFFFFF"));
        isBlueButton = false;
        isRedButton = false;

        if(totalScore == 10){
            nextButton.setText("TRAIN AGAIN");
            gameMessageTextView.setText("                    ");
            isNextButton = true;
            if(isSound) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mediaPlayer_finished_1.start();
                    }
                }, 1000);
            }
            
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    nextButton.setBackgroundResource(R.drawable.header);
                    nextButton.setOnClickListener(NextButtonHandler);
                    isNextButton = true;
                }
            }, 1500);
        }else{
            nextButton.setBackgroundResource(R.drawable.header);
            nextButton.setOnClickListener(NextButtonHandler);
            isNextButton = true;
        }
    }



    private void initiatePopupWindow() {
        if(pw != null){
            pw.dismiss();
        }

        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            View layout = inflater.inflate(R.layout.popup_menu_layout, (ViewGroup) findViewById(R.id.popupElement));

            pw = new PopupWindow(layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);


            pw.showAtLocation(layout, Gravity.CLIP_VERTICAL, 0, 0);

            pw.getContentView().setEnabled(true);
            pw.getContentView().setOnClickListener(CancelPopupElementHandler);

            TextView headerTextView = (TextView) pw.getContentView().findViewById(R.id.headerTextView);
            headerTextView.setTextSize(0, (screenPixelWidth * 0.05f));

            popupOKButton = (Button) pw.getContentView().findViewById(R.id.popupOKButton);
            popupOKButton.setOnClickListener(PopupOKButtonHandler);
            popupOKButton.setTextSize(0, (screenPixelWidth * 0.04f));

            TextView textView1 = (TextView) pw.getContentView().findViewById(R.id.textView1);
            textView1.setTextSize(0, (screenPixelWidth * 0.04f));
            TextView textView2 = (TextView) pw.getContentView().findViewById(R.id.textView2);
            textView2.setTextSize(0, (screenPixelWidth * 0.04f));

            RadioButton timeRadioButton1 = (RadioButton) pw.getContentView().findViewById(R.id.timeRadioButton1);
            timeRadioButton1.setTextSize(0, (screenPixelWidth * 0.03f));
            RadioButton timeRadioButton2 = (RadioButton) pw.getContentView().findViewById(R.id.timeRadioButton2);
            timeRadioButton2.setTextSize(0, (screenPixelWidth * 0.03f));
            RadioButton timeRadioButton3 = (RadioButton) pw.getContentView().findViewById(R.id.timeRadioButton3);
            timeRadioButton3.setTextSize(0, (screenPixelWidth * 0.03f));
            RadioButton timeRadioButton4 = (RadioButton) pw.getContentView().findViewById(R.id.timeRadioButton4);
            timeRadioButton4.setTextSize(0, (screenPixelWidth * 0.03f));

            soundsCheckBox = (CheckBox) pw.getContentView().findViewById(R.id.soundsCheckBox);
            soundsCheckBox.setTextSize(0, (screenPixelWidth * 0.04f));

            RadioButton dotsRadioButton1 = (RadioButton) pw.getContentView().findViewById(R.id.dotsRadioButton1);
            dotsRadioButton1.setTextSize(0, (screenPixelWidth * 0.03f));
            RadioButton dotsRadioButton2 = (RadioButton) pw.getContentView().findViewById(R.id.dotsRadioButton2);
            dotsRadioButton2.setTextSize(0, (screenPixelWidth * 0.03f));
            RadioButton dotsRadioButton3 = (RadioButton) pw.getContentView().findViewById(R.id.dotsRadioButton3);
            dotsRadioButton3.setTextSize(0, (screenPixelWidth * 0.03f));
            RadioButton dotsRadioButton4 = (RadioButton) pw.getContentView().findViewById(R.id.dotsRadioButton4);
            dotsRadioButton4.setTextSize(0, (screenPixelWidth * 0.03f));


            timeRadioGroup = (RadioGroup) pw.getContentView().findViewById(R.id.timeRadioGroup);
            ((RadioButton) timeRadioGroup.getChildAt(displayTimeCode)).setChecked(true);
            timeRadioGroup.setOnCheckedChangeListener(TimeRadioGroupOnCheckedChangeListener);

            soundsCheckBox = (CheckBox) pw.getContentView().findViewById(R.id.soundsCheckBox);
            soundsCheckBox.setChecked(isSound);
            soundsCheckBox.setOnCheckedChangeListener(SoundsCheckBoxHandler);

            dotsRadioGroup = (RadioGroup) pw.getContentView().findViewById(R.id.dotsRadioGroup);
            ((RadioButton) dotsRadioGroup.getChildAt(maxDotsCode)).setChecked(true);
            dotsRadioGroup.setOnCheckedChangeListener(DotsRadioGroupOnCheckedChangeListener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public View.OnClickListener CancelPopupElementHandler = new View.OnClickListener() {
        public void onClick(View v) {
            if(pw != null){
                pw.dismiss();
            }
        }
    };

    View.OnClickListener PopupOKButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(pw != null){
                pw.dismiss();
            }
        }
    };


    RadioGroup.OnCheckedChangeListener TimeRadioGroupOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            View checkedRadioButton = group.findViewById(checkedId);
            displayTimeCode = group.indexOfChild(checkedRadioButton);

            switch(displayTimeCode){
                case 0: displayTime = 2000; break;
                case 1: displayTime = 4000; break;
                case 2: displayTime = 6000; break;
                case 3: displayTime = Integer.MAX_VALUE; break;
                default: break;
            }

            dbHelper.updateTableValue(TABLE_NAME, DISPLAY_TIME, 0, displayTime);
        }
    };


    CheckBox.OnCheckedChangeListener SoundsCheckBoxHandler = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            isSound = isChecked;
            dbHelper.updateTableValue(TABLE_NAME, IS_SOUND, 0, (isSound ? 1 : 0));

            if(isSound){
                createMediaPlayers();
            }else{
                destroyMediaPlayers();
            }
        }
    };


    RadioGroup.OnCheckedChangeListener DotsRadioGroupOnCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

            View checkedRadioButton = group.findViewById(checkedId);
            maxDotsCode = group.indexOfChild(checkedRadioButton);
            switch(maxDotsCode){
                case 0: maxDotsNumber = 9; break;
                case 1: maxDotsNumber = 11; break;
                case 2: maxDotsNumber = 13; break;
                case 3: maxDotsNumber = 15; break;
                default: break;
            }

            dbHelper.updateTableValue(TABLE_NAME, MAX_DOTS, 0, maxDotsNumber);
        }
    };



    private void initiateAboutWindow() {
        if(aboutPopupWindow != null){
            aboutPopupWindow.dismiss();
        }

        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            View layout = inflater.inflate(R.layout.popup_about_layout, (ViewGroup) findViewById(R.id.popupAboutElement));

            aboutPopupWindow = new PopupWindow(layout, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);


            aboutPopupWindow.showAtLocation(layout, Gravity.CLIP_VERTICAL, 0, 0);

            aboutPopupWindow.getContentView().setEnabled(true);
            aboutPopupWindow.getContentView().setOnClickListener(CancelAboutPopupElementHandler);


            aboutOKButton = (Button) aboutPopupWindow.getContentView().findViewById(R.id.aboutOKButton);
            aboutOKButton.setOnClickListener(AboutPopupOKButtonHandler);
            aboutOKButton.setTextSize(0, (screenPixelWidth * 0.04f));

            TextView aboutHeaderTextView = (TextView) aboutPopupWindow.getContentView().findViewById(R.id.aboutHeaderTextView);
            aboutHeaderTextView.setTextSize(0, (screenPixelWidth * 0.05f));

            TextView aboutTextView = (TextView) aboutPopupWindow.getContentView().findViewById(R.id.aboutTextView);
            aboutTextView.setTextSize(0, (screenPixelWidth * 0.04f));





        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public View.OnClickListener CancelAboutPopupElementHandler = new View.OnClickListener() {
        public void onClick(View v) {
            if(aboutPopupWindow != null){
                aboutPopupWindow.dismiss();
            }
        }
    };

    View.OnClickListener AboutPopupOKButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(aboutPopupWindow != null){
                aboutPopupWindow.dismiss();
            }
        }
    };


    private void displayGameInstructions(){
        canvasLeft.drawColor(Color.WHITE);
        canvasRight.drawColor(Color.WHITE);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize((int) (canvasLeft.getHeight() * bitmapTextSizeFactor));
        paint.setFakeBoldText(true);
        paint.setSubpixelText(true);
        canvasLeft.drawText("1. Press the", (int) (canvasLeft.getWidth() * 0.07), (int) (canvasLeft.getHeight() * 0.1), paint);
        canvasLeft.drawText("START", (int) (canvasLeft.getWidth() * 0.2), (int) (canvasLeft.getHeight() * 0.2), paint);
        canvasLeft.drawText("button below", (int) (canvasLeft.getWidth() * 0.2), (int) (canvasLeft.getHeight() * 0.3), paint);

        canvasLeft.drawText("2. Guess which", (int) (canvasLeft.getWidth() * 0.07), (int) (canvasLeft.getHeight() * 0.5), paint);
        canvasLeft.drawText("side has", (int) (canvasLeft.getWidth() * 0.2), (int) (canvasLeft.getHeight() * 0.6), paint);
        canvasLeft.drawText("more dots", (int) (canvasLeft.getWidth() * 0.2), (int) (canvasLeft.getHeight() * 0.7), paint);

        canvasRight.drawText("3. Press", (int) (canvasRight.getWidth() * 0.07), (int) (canvasRight.getHeight() * 0.1), paint);
        canvasRight.drawText("LEFT or RIGHT", (int) (canvasRight.getWidth() * 0.2), (int) (canvasRight.getHeight() * 0.2), paint);
        canvasRight.drawText("button below", (int) (canvasRight.getWidth() * 0.2), (int) (canvasRight.getHeight() * 0.3), paint);

        canvasRight.drawText("4. Look at your", (int) (canvasRight.getWidth() * 0.07), (int) (canvasRight.getHeight() * 0.5), paint);
        canvasRight.drawText("score above", (int) (canvasRight.getWidth() * 0.2), (int) (canvasRight.getHeight() * 0.6), paint);
        canvasRight.drawText("for feedback", (int) (canvasRight.getWidth() * 0.2), (int) (canvasRight.getHeight() * 0.7), paint);


        paint.setColor(Color.parseColor("#FF9100"));
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize((int) (canvasLeft.getHeight() * bitmapTextSizeFactor));
        paint.setSubpixelText(true);
        canvasLeft.drawText("START", (int) (canvasLeft.getWidth() * 0.2), (int) (canvasLeft.getHeight() * 0.2), paint);
        canvasRight.drawText("LEFT or RIGHT", (int) (canvasRight.getWidth() * 0.2), (int) (canvasRight.getHeight() * 0.2), paint);
        canvasRight.drawText("score above", (int) (canvasRight.getWidth() * 0.2), (int) (canvasRight.getHeight() * 0.6), paint);
    }



}
