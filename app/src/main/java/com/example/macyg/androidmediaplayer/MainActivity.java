/*
TODO:
Draw Glowing animations at button Locations
Draw Circle glow using radians
Draw Canvas around each button to draw a circe glow around each button.
 */
package com.example.macyg.androidmediaplayer;

import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.media.AudioManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.graphics.Color;
import android.content.Context;
import android.os.Handler;
import android.widget.ImageView;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.net.Uri;
import android.view.View;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener {

    final int REQUEST_OPEN_FILE = 1;
    MediaPlayer mediaPlayer = new MediaPlayer();
    Uri audioFileUri;
    public int start, stop, aCount, bCount;
    float stopx, stopy, pausex, pausey, playx, playy;
    final int maxMediaTextLength = 22;
    final String no_artist = "Unknown Artist";
    final String no_album = "Unknown Album";
    final String no_title = "Untitled";
    SeekBar seekBar;
    ImageView album_art;
    TextView album, artist, song, trackLength, currTime;
    Handler handler = new Handler();
    Handler songUpdateTimeHandler = new Handler();
    Handler abLoopHandler = new Handler();
    MediaMetadataRetriever metaRetriever;
    byte art[];
    Button playButton, stopButton, pauseButton, abutton, bButton;
    Bitmap circBm;
    AudioManager AM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //show the activity in full screen


        seekBar = findViewById(R.id.seekbar);
        trackLength = findViewById(R.id.trackLength);
        currTime = findViewById(R.id.currTime);

        AM = (AudioManager) getApplicationContext().getSystemService(AUDIO_SERVICE);

        getInit();

        GlowingText gtSong = new GlowingText(MainActivity.this, getApplicationContext(),
                findViewById(R.id.songName), 1, 20, 1, Color.WHITE, 1);
        GlowingText gtArtist = new GlowingText(MainActivity.this, getApplicationContext(),
                findViewById(R.id.artist_name), 1, 20, 1, Color.WHITE, 1);
        GlowingText gtAlbum = new GlowingText(MainActivity.this, getApplicationContext(),
                findViewById(R.id.album_name), 1, 20, 1, Color.WHITE, 1);

        playButton = findViewById(R.id.play);

        playButton.post(new Runnable() {
            @Override
            public void run() {
                playx = playButton.getX();
                playy = playButton.getY();
                System.out.println("play x = " + playx + " play y = " + playy);
                circBm = Bitmap.createBitmap(playButton.getWidth(), playButton.getHeight(), Bitmap.Config.ARGB_8888);
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
        pauseButton = findViewById(R.id.pause);
        pauseButton.post(new Runnable() {
            @Override
            public void run() {
                pausex = pauseButton.getX();
                pausey = pauseButton.getY();
                System.out.println("pause x = " + pausex + " pause y = " + pausey);
            }
        });
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });

        stopButton = findViewById(R.id.stop);
        stopButton.post(new Runnable() {
            @Override
            public void run() {
                stopx = stopButton.getX();
                stopy = stopButton.getY();
                System.out.println("stop x = " + stopx + " stop y = " + stopy);

            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
        abutton = findViewById(R.id.aButton);
        abutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aButton();
            }
        });
        bButton = findViewById(R.id.bButton);
        bButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bButton();
            }
        });

        seekBar.setVisibility(View.INVISIBLE);
        trackLength.setVisibility(View.INVISIBLE);
        currTime.setVisibility(View.INVISIBLE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.open_file_action) {
            Intent audiofile_chooser_intent;
            audiofile_chooser_intent = new Intent();
            audiofile_chooser_intent.setAction(Intent.ACTION_GET_CONTENT);
            audiofile_chooser_intent.setType("audio/*");

            startActivityForResult(Intent.createChooser(audiofile_chooser_intent,
                    getString(R.string.select_audio_file_title)), REQUEST_OPEN_FILE);

            return true;

        } else if (id == R.id.second_item) {
            quit();
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_OPEN_FILE) {
            if (resultCode == RESULT_OK) {
                audioFileUri = data.getData();
                metaRetriever();
            }
            /*AudioManager mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

            if (mAudioManager.isMusicActive()) {

                Intent i = new Intent("com.android.music.musicservicecommand");

                i.putExtra("command", "pause");
                MainActivity.this.sendBroadcast(i);
            }*/
        }
        startMediaPlayer();
    }

    public void startMediaPlayer() {
        try {
            int requestAudioFocus = AM.requestAudioFocus(this, AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
            if (requestAudioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Intent i = new Intent("com.android.music.musicservicecommand");
                i.putExtra("command", "pause");
                MainActivity.this.sendBroadcast(i);
                if (mediaPlayer.isPlaying() || !mediaPlayer.isPlaying()) {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(this, audioFileUri);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    seekBar.setProgress(0);
                    seekBar.setMax(mediaPlayer.getDuration()); // Set the Maximum range of the
                    seekBar.setVisibility(View.VISIBLE);
                } else {
                    mediaPlayer.setDataSource(this, audioFileUri);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    seekBar.setProgress(0);
                    seekBar.setMax(mediaPlayer.getDuration()); // Set the Maximum range of the
                    seekBar.getVisibility();
                    seekBar.setVisibility(View.VISIBLE);
                }
            }
        }/*else {
                Toast.makeText(this, "UNABLE TO FOCUS", Toast.LENGTH_SHORT).show();
            }*/ catch (Exception e) {
            e.printStackTrace();
        }


        int mediaPos = mediaPlayer.getCurrentPosition();
        int mediaMax = mediaPlayer.getDuration();
        seekBar.setMax(mediaMax);
        seekBar.setProgress(mediaPos);
        handler.removeCallbacks(moveSeekBarThread);
        handler.postDelayed(moveSeekBarThread, 300);

        int milliSongMax = mediaMax / 1000;
        double minutesDouble = Math.floor(milliSongMax / 60);
        int minutes = (int) minutesDouble;
        int seconds = (milliSongMax % 60);
        System.out.println("Minutes: " + minutes + " Seconds: " + seconds);
        if (seconds < 10) {
            String time = Integer.toString(minutes) + ":0" + seconds;
            trackLength.setText(time);
            trackLength.setVisibility(View.VISIBLE);
        } else {
            String maxTime = Integer.toString(minutes) + ":" + Integer.toString(seconds);
            trackLength.setText(maxTime);
            trackLength.setVisibility(View.VISIBLE);
        }

        songUpdateTimeHandler.removeCallbacks(updateSongTime);
        songUpdateTimeHandler.postDelayed(updateSongTime, 100);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void quit() {
        mediaPlayer.stop();
        mediaPlayer.release();
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(0);
    }

    public void play() {
        int result = AM.requestAudioFocus(this,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            /*AM.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0);*/
            Toast.makeText(this, "Focus Granted", Toast.LENGTH_SHORT).show();
            if (!mediaPlayer.isPlaying() || mediaPlayer == null) {
                mediaPlayer.start();
                handler.removeCallbacks(moveSeekBarThread);
                handler.postDelayed(moveSeekBarThread, 300);
            }
        }
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            /*AM.abandonAudioFocus(this);*/
        }
    }

    public void stop() {
        if (mediaPlayer != null || mediaPlayer.isPlaying() || mediaPlayer == null) {
            seekBar.setProgress(0);
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            start = 0;
            stop = 0;
            aCount = 0;
            bCount = 0;
        }
    }

    public void aButton() {
        start = mediaPlayer.getCurrentPosition();

        if (aCount > 1 && bCount == 1) {
            aCount = 0;
            bCount = 0;
        }
        if (bCount == 1 && aCount == 0) {
            bCount = 0;
            aCount += 1;
        } else {
            aCount += 1;
        }

    }

    public void bButton() {
        stop = mediaPlayer.getCurrentPosition();

        /*bCount += 1;*/

        if (bCount > 1 && aCount >= 1) {
            bCount = 0;
            aCount = 0;
        }

        if (aCount != 1) {
            Toast.makeText(this, "Please press A first", Toast.LENGTH_SHORT).show();
            bCount = 0;
            aCount = 0;
        }
        if (bCount == 0 && aCount == 1) {
            bCount += 1;
        }
        if (bCount == 1 && aCount == 0) {
            bCount = 0;
        }

        System.out.println("AB Button Count: " + bCount);
        /*mediaPlayer.seekTo(start);*/
        abLoopHandler.removeCallbacks(abLoop);
        abLoopHandler.postDelayed(abLoop, 1);
    }

    private Runnable abLoop = new Runnable() {
        @Override
        public void run() {
            if (aCount == 1 && bCount == 1) {
                int currPos = mediaPlayer.getCurrentPosition();
                if (currPos >= stop || currPos <= start) {
                    mediaPlayer.seekTo(start);
                }
                abLoopHandler.postDelayed(this, 1);
            }
        }
    };

    private Runnable moveSeekBarThread = new Runnable() {
        public void run() {
            if (mediaPlayer.isPlaying()) {

                int mediaPos_new = mediaPlayer.getCurrentPosition();
                int mediaMax_new = mediaPlayer.getDuration();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        seekBar.setProgress(0);// finish current activity
                        mediaPlayer.seekTo(0);
                    }
                });
                seekBar.setMax(mediaMax_new);
                seekBar.setProgress(mediaPos_new);
                handler.postDelayed(this, 300); //Looping the thread after 0.3 second
                // seconds
            }

        }

    };

    private Runnable updateSongTime = new Runnable() {
        public void run() {
            if (mediaPlayer.isPlaying() || !mediaPlayer.isPlaying()) {
                int mediaPos_new = mediaPlayer.getCurrentPosition();
                int currentTime = mediaPos_new / 1000;
                double currentTime1 = Math.floor(currentTime / 60);
                int minutes = (int) currentTime1;
                int seconds = (currentTime % 60);
                if (seconds < 10) {
                    String currenTime2 = Integer.toString(minutes) + ":0" + Integer.toString(seconds);
                    /*TextView currTime = (TextView) findViewById(R.id.currTime);*/
                    currTime.setText(currenTime2);
                    currTime.setVisibility(View.VISIBLE);
                } else {
                    String currenTime2 = Integer.toString(minutes) + ":" + Integer.toString(seconds);
                    /*TextView currTime = (TextView) findViewById(R.id.currTime);*/
                    currTime.setText(currenTime2);
                    currTime.setVisibility(View.VISIBLE);
                }
            }
            songUpdateTimeHandler.postDelayed(this, 100);
        }
    };

    public void metaRetriever() {

        if (artist == null && album_art == null && song == null) {
            getInit();
        }
        metaRetriever = new MediaMetadataRetriever();
        if (audioFileUri == null) {
            return;
        } else {
            metaRetriever.setDataSource(this, audioFileUri);
        }

        try {
            art = metaRetriever.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            album_art.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 150, 150, false));

            int albumLength = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM).length();
            if (albumLength > maxMediaTextLength) {
                album.setText(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
                album.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scrolltext));
            } else {
                album.clearAnimation();
                album.setText(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            }

            int artistLength = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).length();
            String artistString = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

            if (artistLength > maxMediaTextLength) {
                artist.setText(artistString);
                artist.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scrolltext));
            } else {
                artist.clearAnimation();
                artist.setText(artistString);
            }

            int songLength = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).length();
            if (songLength > maxMediaTextLength) {
                song.setText(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
                song.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scrolltext));
            } else {
                song.clearAnimation();
                song.setText(metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            }

        } catch (Exception e) {
            album_art.setBackgroundColor(Color.GRAY);
            album_art.setImageResource(R.drawable.headphones);

            song.clearAnimation();
            song.setText(no_title);
            artist.clearAnimation();
            artist.setText(no_artist);
            album.clearAnimation();
            album.setText(no_album);
        }
        album_art.setVisibility(View.VISIBLE);
    }

    public void getInit() {
        album_art = (ImageView) findViewById(R.id.album_art);
        album = (TextView) this.findViewById(R.id.album_name);
        artist = (TextView) this.findViewById(R.id.artist_name);
        song = (TextView) this.findViewById(R.id.songName);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                System.out.println(focusChange);
                /*Toast.makeText(this, "FOCUS LOST", Toast.LENGTH_LONG).show();*/
                AM.abandonAudioFocus(this);
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                System.out.println(focusChange);
                /*Toast.makeText(this, "FOCUS LOSS TRANSIENT", Toast.LENGTH_LONG).show();*/
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                System.out.println(focusChange);
                /*Toast.makeText(this, "Focus Loss Transiet can Duck", Toast.LENGTH_SHORT).show();*/
                AM.abandonAudioFocus(this);
                pause();
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                System.out.println(focusChange);
                /*Toast.makeText(this, "FOCUS GAIN", Toast.LENGTH_LONG).show();*/
                play();
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                int result = AM.requestAudioFocus(this, AudioManager.AUDIOFOCUS_GAIN, AudioManager.STREAM_MUSIC);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    play();
                }
                break;
            case AudioManager.AUDIOFOCUS_NONE:
                System.out.println(focusChange);
                break;
        }
    }
}