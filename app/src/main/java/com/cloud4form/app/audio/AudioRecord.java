package com.cloud4form.app.audio;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.cloud4form.app.R;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class AudioRecord extends AppCompatActivity {

    public static final String DURATION="DURATION";
    private Button mBtnDone;
    private ImageButton mBtnPlay,mBtnRecord,mBtnStop;
    private static final String LOG_TAG = "AudioRecordTest";
    private static String mFileName = null;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private String _STAT="NEW";
    private boolean isRecording=false;
    private AudioVisual mAudioVisual;
    private Handler handler;
    private long startTime;
    private int duration=60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_record);

        this.mBtnDone=(Button)findViewById(R.id.buttonFinish);
        this.mBtnPlay=(ImageButton)findViewById(R.id.buttonPlay);
        this.mBtnRecord=(ImageButton)findViewById(R.id.buttonRecord);
        this.mBtnStop=(ImageButton)findViewById(R.id.buttonStop);
        this.mAudioVisual=(AudioVisual)findViewById(R.id.audioVisual);
        this.mBtnStop.setEnabled(false);
        this.mBtnDone.setEnabled(false);
        handler=new Handler();

        File storageDir = Environment.getExternalStorageDirectory();
        File filePath = new File(storageDir.getAbsolutePath(), "c4f_files");
        if (!filePath.exists()) {
            filePath.mkdir();
        }
        mFileName=filePath.getAbsolutePath()+"/"+System.currentTimeMillis()+".3gp";

        duration=this.getIntent().getExtras().getInt(AudioRecord.DURATION,60);

        this.mBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioRecord.this.onDoneClick();
            }
        });

        this.mBtnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioRecord.this.startPlaying();
                AudioRecord.this._STAT="PLAY";
                AudioRecord.this.mBtnPlay.setVisibility(View.GONE);
                AudioRecord.this.mBtnStop.setVisibility(View.VISIBLE);
            }
        });

        this.mBtnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioRecord.this.startRecording();
                AudioRecord.this._STAT="REC";
                AudioRecord.this.mBtnStop.setEnabled(true);
            }
        });

        this.mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(AudioRecord.this._STAT.equals("REC")){
                    AudioRecord.this.stopRecording();
                }else{
                    AudioRecord.this.stopPlaying();
                }

                AudioRecord.this.mBtnRecord.setEnabled(false);
                AudioRecord.this.mBtnDone.setEnabled(true);
                AudioRecord.this.mBtnStop.setVisibility(View.GONE);
                AudioRecord.this.mBtnPlay.setVisibility(View.VISIBLE);
            }
        });
    }

    private void onDoneClick(){
        stopPlaying();
        Intent returnIntent = new Intent();
        returnIntent.putExtra("data",mFileName);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    AudioRecord.this.mBtnRecord.setEnabled(false);
                    AudioRecord.this.mBtnDone.setEnabled(true);
                    AudioRecord.this.mBtnStop.setVisibility(View.GONE);
                    AudioRecord.this.mBtnPlay.setVisibility(View.VISIBLE);
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
        isRecording=true;
        startTime=new Date().getTime();
        handler.post(updateVisualizer);
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        isRecording=false;

    }

    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (isRecording){
                int x = mRecorder.getMaxAmplitude();
                mAudioVisual.addAmplitude(x);
                mAudioVisual.invalidate();
                // update in 40 milliseconds
                handler.postDelayed(this,100);

                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int totsec=seconds;
                seconds=duration-seconds;
                int minutes = seconds / 60;
                seconds     = seconds % 60;


                mAudioVisual.setText(String.format("%02d:%02d", minutes, seconds));

                if(totsec>=duration){
                    AudioRecord.this.stopRecording();
                    AudioRecord.this.mBtnRecord.setEnabled(false);
                    AudioRecord.this.mBtnDone.setEnabled(true);
                    AudioRecord.this.mBtnStop.setVisibility(View.GONE);
                    AudioRecord.this.mBtnPlay.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    public void onBackPressed() {
        new File(mFileName).delete();
        stopRecording();
        stopPlaying();
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
