/*
TODO:
Draw Glowing animations at button Locations
Draw Circle glow using radians
Draw Canvas around each button to draw a circe glow around each button.
Handle invalid file and play next song in array
Play files from current playing files directory
Update to have animated background while playing
 */
package com.example.macyg.androidmediaplayer;

import android.Manifest;
import android.animation.AnimatorSet;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.media.AudioManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.graphics.Color;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements AudioManager.OnAudioFocusChangeListener {

    final int REQUEST_OPEN_FILE = 1, MY_PERMISSION_REQUEST = 2;
    final int maxMediaTextLength = 22;
    final String no_artist = "Unknown Artist";
    final String no_album = "Unknown Album";
    final String no_title = "Untitled";
    String removableStoragePath, filePath, chosenPath2, fileNotFoundException;
    public int start, stop, aCount, bCount, j, seconds;
    public int songOrderCounter, dirCounter, playxWidth, playyWidth, focusState;
    public boolean isUri, firstCount, playIcon;
    MediaPlayer mediaPlayer = new MediaPlayer();
    Uri audioFileUri;
    float stopx, stopy, pausex, pausey, playx, playy;
    ArrayList<Integer> iterationCounterLocations = new ArrayList<Integer>();
    SeekBar seekBar;
    Object allMusicFiles[], currentDirectory[] = null;
    ImageView album_art;
    TextView album, artist, song, trackLength, currTime;
    Handler seekBarHandler = new Handler();
    Handler songUpdateTimeHandler = new Handler();
    Handler abLoopHandler = new Handler();
    MediaMetadataRetriever metaRetriever;
    byte art[];
    Button playButton, stopButton, pauseButton, abutton, bButton;
    AudioManager AM;
    ArrayList<String> musicList = new ArrayList<String>();
    ArrayList<String> currentDirList = new ArrayList<String>();
    ArrayList<String> downloadList = new ArrayList<String>();
    ArrayList<String> sdDownloadList = new ArrayList<String>();
    ArrayList<String> sdMusicList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //show the activity in full screen

        seekBar = findViewById(R.id.seekbar);
        trackLength = findViewById(R.id.trackLength);
        currTime = findViewById(R.id.currTime);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        } else {
            /*doStuff();*/
        }

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
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentDirectory == null) {
                }
                int requestAudioFocus = (AM.requestAudioFocus(MainActivity.this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN));
                System.out.println("Play Audio Focus: " + requestAudioFocus);
                if(requestAudioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    if(audioFileUri == null){

                    }else {
                        if (playIcon && !mediaPlayer.isPlaying()) {
                            playButton.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_media_pause));
                            play();
                            playIcon = false;
                        } else {
                            mediaPlayer.pause();
                            playButton.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_media_embed_play));
                            playIcon = true;
                        }
                    }
                }else{System.out.println("FOCUS NOT GRANTED BY PLAY");}
            }
        });
        pauseButton = findViewById(R.id.forward);
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
                forward();
            }
        });

        stopButton = findViewById(R.id.backward);
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
                reverse();
                /*stopButton.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.scrolltext));*/
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

    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
        }
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
            isUri = true;

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
        if (data == null) {
            return;
        } else {
            System.out.println(resultCode);
            if (requestCode == REQUEST_OPEN_FILE) {
                if (resultCode == RESULT_OK) {
                    firstCount = true;
                    currentDirectory = null;
                    fileNotFoundException = "";
                    audioFileUri = data.getData();

                    System.out.println("URI: " + audioFileUri);
                    System.out.println("URI AUTHORITY: " + audioFileUri.getAuthority());

                    //-----------------------------------------------------------
                    /*for (int i = 0; i < audioFileUri.toString().length(); i++) {
                        char c = audioFileUri.toString().charAt(i);
                        //Process char
                        if (c == '/') {
                            dirCounterSD += 1;
                            iterationCounterLocationsSD.add(i);
                        }
                    }
                    Object iterationArraySD[] = iterationCounterLocationsSD.toArray();
                    int lastIndexPathSD = iterationArraySD.length;
                    int lastIndexSlashSD = (int) iterationArraySD[lastIndexPathSD - 1];
                    System.out.println("# Slashes: " + lastIndexPathSD + " Slash index: "
                            + iterationArraySD[lastIndexPathSD - 1].toString());

                    sdCardLabel = audioFileUri.toString().substring(lastIndexSlashSD + 1, lastIndexSlashSD + 10);
                    if(sdCardLabel.toCharArray()[lastIndexSlashSD] == "-".toCharArray()[1]){
                        sdCardPathDetection = true;
                    }
                    System.out.println("SD Card Label: " + sdCardLabel);

                    filePath = PathUtil.getPath(this, audioFileUri);

                    System.out.println("ACCENTUATED URI: " + filePath);*/

                    //--------------------------------------------------------------
                    filePath = PathUtil.getPath(this, audioFileUri);
                    for (int i = 0; i < filePath.length(); i++) {
                        char c = filePath.charAt(i);
                        //Process char
                        if (c == '/') {
                            dirCounter += 1;
                            iterationCounterLocations.add(i);
                        }
                    }
                    Object iterationArray[] = iterationCounterLocations.toArray();
                    int lastIndexPath = iterationArray.length;
                    int lastIndexSlash = (int) iterationArray[lastIndexPath - 1];

                    System.out.println("# Slashes: " + lastIndexPath + " Slash index: "
                            + iterationArray[lastIndexPath - 1].toString());

                    chosenPath2 = filePath.substring(0, lastIndexSlash);
                    chosenPath2 = chosenPath2 + "/";
                    System.out.println("NEW_PATH " + chosenPath2);
                    metaRetriever();
                }
            }
        }

        //Now get all file paths where music is stored so they are readily accessible by the mediaplayer.

        String pathDownload = Environment.getExternalStorageDirectory().toString() + "/Download/";
        String pathMusic = Environment.getExternalStorageDirectory().toString() + "/Music/";
        Log.d("Files", "Download Path: " + pathDownload);
        File downloadDirectory = new File(pathDownload);
        File musicDirectory = new File(pathMusic);
        File currDirectory = new File(chosenPath2);
        File[] downloadFiles = downloadDirectory.listFiles();
        File[] musicFiles = musicDirectory.listFiles();
        File[] currFiles = currDirectory.listFiles();

        File fileList[] = new File("/storage/").listFiles();
        for (File file : fileList) {
            if (!file.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()) && file.isDirectory() && file.canRead())
                removableStoragePath = file.getAbsolutePath() + "/";
        }

        System.out.println("EXTERNAL_SD " + removableStoragePath);
        String pathSdDownload = removableStoragePath + "/Download/";
        String pathSdMusic = removableStoragePath + "/Music/";
        File sdDownloadDirectory = new File(pathSdDownload);
        File sdMusicDirectory = new File(pathSdMusic);
        File[] sdDownloadFiles = sdDownloadDirectory.listFiles();
        File[] sdMusicFiles = sdMusicDirectory.listFiles();

        Log.d("Files", "music Size: " + musicFiles.length);
        Log.d("Files", "download Size: " + downloadFiles.length);

        //if the current directory list is still full from last chosen song directory, clear it.
        currentDirList.clear();

        if (currFiles != null) {
            for (int i = 0; i < currFiles.length; i++) {
                if(currFiles[i].getName().contains(".mp3") || currFiles[i].getName().contains(".m4a")
                        || currFiles[i].getName().contains(".aac") || currFiles[i].getName().contains(".mkv")
                        || currFiles[i].getName().contains(".wav") || currFiles[i].getName().contains(".mp3")
                        || currFiles[i].getName().contains(".3gp") || currFiles[i].getName().contains(".flac")
                        || currFiles[i].getName().contains(".ogg")) {
                    currentDirList.add(currDirectory + "/" + currFiles[i].getName());
                    System.out.println("CURRENT DIRECTORY LIST: " + currDirectory + "/" + currFiles[i].getName());

                }
            }
        }
        //Make the current directory File[] list into an Array then sort
        //the array based on the order of files in accordance with filebrowser
        currentDirectory = currentDirList.toArray();
        Arrays.sort(currentDirectory);

        musicList.addAll(downloadList);
        musicList.addAll(sdDownloadList);
        musicList.addAll(sdMusicList);

        allMusicFiles = musicList.toArray();

        System.out.println("DOWNLOAD_LIST: " + downloadList);
        System.out.println("MUSIC_LIST: " + musicList);

        for (int i = 0; i < allMusicFiles.length; i++) {
            System.out.println("ALL_MUSIC: " + allMusicFiles[i]);
        }

        startMediaPlayer();
        seekBar.setVisibility(View.VISIBLE);
    }

    public void startMediaPlayer() {
        try {
            playButton.setBackground(ContextCompat.getDrawable(this, R.drawable.ic_media_pause));
            aCount = 0;
            bCount = 0;
            start = 0;
            stop = 0;
            if (audioFileUri == null) {

            } else {

                int requestAudioFocus = AM.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (requestAudioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    Intent i = new Intent("com.android.music.musicservicecommand");
                    i.putExtra("command", "pause");
                    MainActivity.this.sendBroadcast(i);
                    if (mediaPlayer.isPlaying() || !mediaPlayer.isPlaying()) {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(this, audioFileUri);
                        for(int f = 0; f < currentDirectory.length; f++){
                            if(filePath.equals(currentDirectory[f].toString())){
                                songOrderCounter = f;
                            }
                        }
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        seekBar.setProgress(0);
                        seekBar.setMax(mediaPlayer.getDuration()); // Set the Maximum range of the
                        seekBar.setVisibility(View.VISIBLE);
                        artist.setVisibility(View.VISIBLE);
                        song.setVisibility(View.VISIBLE);
                        album.setVisibility(View.VISIBLE);
                        /*songOrderCounter += 1;*/
                        System.out.println("Song order Counter: " + songOrderCounter);
                        firstCount = true;
                    } else {
                        mediaPlayer.setDataSource(this, audioFileUri);
                        for(int f = 0; f < currentDirectory.length; f++){
                            if(filePath.equals(currentDirectory[f].toString())){
                                songOrderCounter = f;
                            }
                        }
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        seekBar.setProgress(0);
                        seekBar.setMax(mediaPlayer.getDuration()); // Set the Maximum range of the
                        seekBar.getVisibility();
                        seekBar.setVisibility(View.VISIBLE);
                        artist.setVisibility(View.VISIBLE);
                        song.setVisibility(View.VISIBLE);
                        album.setVisibility(View.VISIBLE);
                        /*songOrderCounter += 1;*/
                        System.out.println("Song order Counter: " + songOrderCounter);
                        firstCount = true;
                    }
                }
            }
            trackTime();
        } catch (Exception e) {
            e.printStackTrace();
            seekBar.setVisibility(View.INVISIBLE);
            currTime.setVisibility(View.INVISIBLE);
            trackLength.setVisibility(View.INVISIBLE);
            album.setVisibility(View.INVISIBLE);
            artist.setVisibility(View.INVISIBLE);
            song.setVisibility(View.INVISIBLE);
            album_art.setVisibility(View.INVISIBLE);
            if (e.toString().contains("setDataSource failed.: status=0x80000000")) {
                fileNotFoundException = e.toString();
                Toast.makeText(this, "FILE DOES NOT EXIST!", Toast.LENGTH_LONG).show();
                seekBarHandler.removeCallbacks(moveSeekBarThread);
                songUpdateTimeHandler.removeCallbacks(updateSongTime);
                mediaPlayer.reset();
            }
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                if (currentDirectory.length <= 1) {
                    seekBar.setProgress(0);
                    currTime.setText(R.string.default_time);
                    playButton.setBackground(getResources().getDrawable(R.drawable.ic_media_embed_play, getTheme()));
                    playIcon = true;
                    seekBarHandler.removeCallbacks(moveSeekBarThread);
                    songUpdateTimeHandler.removeCallbacks(updateSongTime);
                } else {
                    seekBarHandler.removeCallbacks(moveSeekBarThread);
                    songUpdateTimeHandler.removeCallbacks(updateSongTime);
                    isUri = false;
                    if (firstCount) {
                        firstCount = false;
                        songOrderCounter = 0;
                    } else {
                        songOrderCounter += 1;
                    }
                    if (songOrderCounter >= currentDirectory.length) {
                        songOrderCounter = 0;
                    }
                    for(int i = 0; i < currentDirectory.length; i++){
                        if(filePath.equals(currentDirectory[i].toString())){
                            songOrderCounter = i;
                        }
                    }
                    try {
                        if (currentDirectory[songOrderCounter].toString().equals(filePath)) {
                            songOrderCounter += 1;
                            if (songOrderCounter >= currentDirectory.length) {
                                songOrderCounter = 0;
                            }
                            filePath = "";
                            if (!currentDirectory[songOrderCounter].toString().contains(".mp3")) {
                                songOrderCounter += 1;
                            }
                            Log.d("PATH_WORKING", currentDirectory[songOrderCounter].toString() + " index counter: " + songOrderCounter);
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(currentDirectory[songOrderCounter].toString());
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mediaPlayer) {
                                    mediaPlayer.start();
                                    metaRetriever();
                                    trackTime();
                                }
                            });
                            mediaPlayer.prepareAsync();
                        } else {
                            if (!currentDirectory[songOrderCounter].toString().contains(".mp3")) {
                                songOrderCounter += 1;
                            }
                            Log.d("PATH_WORKING", currentDirectory[songOrderCounter].toString() + " index counter: " + songOrderCounter);
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(currentDirectory[songOrderCounter].toString());
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mediaPlayer) {
                                    mediaPlayer.start();
                                    metaRetriever();
                                    trackTime();
                                }
                            });
                            mediaPlayer.prepareAsync();
                        }
                    } catch (Exception e) {
                        System.out.println("NEXT_SONG_FAILED: " + e.toString());
                        seekBar.setVisibility(View.INVISIBLE);
                        currTime.setVisibility(View.INVISIBLE);
                        trackLength.setVisibility(View.INVISIBLE);
                        album.setVisibility(View.INVISIBLE);
                        artist.setVisibility(View.INVISIBLE);
                        song.setVisibility(View.INVISIBLE);
                        album_art.setVisibility(View.INVISIBLE);
                        if (e.toString().equals("java.io.IOException: setDataSource failed.")) {
                            Toast.makeText(MainActivity.this, "Invalid File Name", Toast.LENGTH_SHORT).show();
                        }
                        if (e.toString().equals("setDataSource failed.: status=0x80000000")) {
                            Toast.makeText(MainActivity.this, "FILE DOES NOT EXIST!", Toast.LENGTH_LONG).show();
                            mediaPlayer.reset();
                            album.setVisibility(View.INVISIBLE);
                            artist.setVisibility(View.INVISIBLE);
                            song.setVisibility(View.INVISIBLE);
                        }

                        try {
                            songOrderCounter += 1;
                            if (songOrderCounter >= currentDirectory.length) {
                                songOrderCounter = 0;
                            }
                            if (!currentDirectory[songOrderCounter].toString().contains(".mp3")) {
                                songOrderCounter += 1;
                            }
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(currentDirectory[songOrderCounter].toString());
                            System.out.println("PLAYING ANYWAYS.... " + allMusicFiles[songOrderCounter].toString());
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                            updateSongTime.run();
                            moveSeekBarThread.run();
                            trackTime();
                            metaRetriever();
                        } catch (Exception f) {
                            f.printStackTrace();
                            if (f.toString().equals("setDataSource failed.: status=0x80000000")) {
                                Toast.makeText(MainActivity.this, "FILE DOES NOT EXIST!", Toast.LENGTH_LONG).show();
                                mediaPlayer.reset();
                            }
                        }
                    }
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    seekBar.setProgress(progress);
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    public void trackTime() {
        int mediaPos = mediaPlayer.getCurrentPosition();
        final int mediaMax = mediaPlayer.getDuration();
        seekBar.setMax(mediaMax);
        seekBar.setProgress(mediaPos);
        seekBarHandler.removeCallbacks(moveSeekBarThread);
        seekBarHandler.postDelayed(moveSeekBarThread, 100);

        int milliSongMax = mediaMax / 1000;
        double minutesDouble = Math.floor(milliSongMax / 60);
        int minutes = (int) minutesDouble;
        seconds = (milliSongMax % 60);
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
    }

    public void quit() {
        mediaPlayer.stop();
        mediaPlayer.release();
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
        System.exit(0);
    }

    public void play() {
        if(currentDirectory == null){

        }else {
            if (fileNotFoundException.contains("setDataSource failed.: status=0x80000000")) {

            } else {
                int result = AM.requestAudioFocus(this,
                        // Use the music stream.
                        AudioManager.STREAM_MUSIC,
                        // Request permanent focus.
                        AudioManager.AUDIOFOCUS_GAIN);

                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    /*AM.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0);*/
                    /*Toast.makeText(this, "Focus Granted", Toast.LENGTH_SHORT).show();*/
                    if (!mediaPlayer.isPlaying() || mediaPlayer == null) {
                        mediaPlayer.start();
                        seekBarHandler.postDelayed(moveSeekBarThread, 100);
                        songUpdateTimeHandler.postDelayed(updateSongTime, 100);
                    }
                }
            }
        }
    }


    public void forward() {
        if (currentDirectory == null) {

        } else {
            if (mediaPlayer.isPlaying() || !mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                songUpdateTimeHandler.removeCallbacks(updateSongTime);
                seekBarHandler.removeCallbacks(moveSeekBarThread);
                songOrderCounter += 1;
                try {
                    mediaPlayer.reset();
                    if (songOrderCounter >= currentDirectory.length) {
                        songOrderCounter = 0;
                    }
                    /*if(!currentDirectory[songOrderCounter].toString().contains("*.mp3")){
                        songOrderCounter += 1;
                    }*/
                    System.out.println("Forward counter: " + songOrderCounter);
                    mediaPlayer.setDataSource(currentDirectory[songOrderCounter].toString());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    playButton.setBackground(getDrawable(R.drawable.ic_media_pause));
                    updateSongTime.run();
                    moveSeekBarThread.run();
                    metaRetriever();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                start = 0;
                stop = 0;
                aCount = 0;
                bCount = 0;
                /*AM.abandonAudioFocus(this);*/
            }
        }
    }

    public void reverse() {
        if(currentDirectory == null) {

        }else {
            if (seconds < 3) {
                if (currentDirectory.length == 0) {
                    seekBar.setProgress(0);
                    mediaPlayer.seekTo(0);
                    updateSongTime.run();
                    start = 0;
                    stop = 0;
                    aCount = 0;
                    bCount = 0;
                } else {
                    try {
                        songUpdateTimeHandler.removeCallbacks(updateSongTime);
                        seekBarHandler.removeCallbacks(moveSeekBarThread);
                        mediaPlayer.reset();
                        songOrderCounter -= 1;
                        if (songOrderCounter <= currentDirectory.length) {
                            songOrderCounter = 0;
                        }
                        System.out.println("Reverse counter: " + songOrderCounter);
                        if (songOrderCounter < 0) {
                            songOrderCounter = currentDirectory.length;
                        }
                        mediaPlayer.setDataSource(currentDirectory[songOrderCounter].toString());
                        System.out.println("Reverse counter: " + songOrderCounter);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        updateSongTime.run();
                        moveSeekBarThread.run();
                        metaRetriever();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                seekBar.setProgress(0);
                mediaPlayer.seekTo(0);
                updateSongTime.run();
                start = 0;
                stop = 0;
                aCount = 0;
                bCount = 0;
            }
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
            if (mediaPlayer.getCurrentPosition() != mediaPlayer.getDuration()) {
                int mediaPos_new = mediaPlayer.getCurrentPosition();
                int mediaMax_new = mediaPlayer.getDuration();
                seekBar.setMax(mediaMax_new);
                seekBar.setProgress(mediaPos_new);
                seekBarHandler.postDelayed(this, 100); //Looping the thread after 0.3 second
                // seconds
            } else {
            }
        }
    };
    private Runnable updateSongTime = new Runnable() {
        public void run() {
            if (mediaPlayer.isPlaying() || !mediaPlayer.isPlaying()) {
                int mediaPos_new = mediaPlayer.getCurrentPosition();
                if(mediaPos_new >= mediaPlayer.getDuration() && currentDirList == null){
                    mediaPlayer.pause();
                    mediaPlayer.seekTo(0);
                    seekBar.setProgress(0);
                    playButton.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_media_embed_play));
                }
                int currentTime = mediaPos_new / 1000;
                double currentTime1 = Math.floor(currentTime / 60);
                int minutes = (int) currentTime1;
                seconds = (currentTime % 60);
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
        }/*else if(mediaPlayer.getCurrentPosition() == mediaPlayer.getDuration()){}*/
    };

    public void metaRetriever() {

        if (artist == null && album_art == null && song == null) {
            getInit();
        }
        metaRetriever = new MediaMetadataRetriever();

        if (isUri == false) {
            metaRetriever.setDataSource(currentDirectory[songOrderCounter].toString());
        } else {
            try {
                metaRetriever.setDataSource(this, audioFileUri);
                isUri = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
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
                mediaPlayer.pause();
                playIcon = true;
                playButton.setBackgroundResource(R.drawable.ic_media_embed_play);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                System.out.println(focusChange);
                /*Toast.makeText(this, "FOCUS LOSS TRANSIENT", Toast.LENGTH_LONG).show();*/
                playButton.setBackgroundResource(R.drawable.ic_media_embed_play);
                mediaPlayer.pause();
                playIcon = true;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                System.out.println(focusChange);
                /*Toast.makeText(this, "Focus Loss Transient Can Duck", Toast.LENGTH_SHORT).show();*/
                mediaPlayer.setVolume(.2f,.2f);
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                System.out.println(focusChange);
                mediaPlayer.setVolume(1f, 1f);
                /*Toast.makeText(this, "FOCUS GAIN", Toast.LENGTH_LONG).show();*/
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                mediaPlayer.start();
                mediaPlayer.setVolume(1f, 1f);
                break;
            case AudioManager.AUDIOFOCUS_NONE:
                System.out.println(focusChange);
                break;
        }
    }

}