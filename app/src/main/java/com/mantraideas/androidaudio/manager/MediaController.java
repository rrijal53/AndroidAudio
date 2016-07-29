/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.mantraideas.androidaudio.manager;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;


import com.mantraideas.androidaudio.MyApplication;
import com.mantraideas.androidaudio.SongDetail;
import com.mantraideas.androidaudio.Utilities;
import com.mantraideas.androidaudio.phonemidea.DMPlayerUtility;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MediaController implements NotificationManager.NotificationCenterDelegate, SensorEventListener {

    private boolean isPaused = true;
    private MediaPlayer audioPlayer = null;
    private AudioTrack audioTrackPlayer = null;
    private int lastProgress = 0;
    private boolean useFrontSpeaker;

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private boolean ignoreProximity;
    private PowerManager.WakeLock proximityWakeLock;

    private final Object playerSync = new Object();
    private final Object playerSongDetailSync = new Object();
    private boolean playMusicAgain = false;

    private int lastTag = 0;
    public static int currentPlaylistNum;
    private boolean shuffleMusic;

    private final Object progressTimerSync = new Object();
    private Timer progressTimer = null;

    private final Object sync = new Object();
    private int ignoreFirstProgress = 0;
    private long lastPlayPcm;
    private long currentTotalPcmDuration;

    public int type = 0;
    public int id = -1;
    public String path = "";
    private int repeatMode;

    private static volatile MediaController Instance = null;

    public static MediaController getInstance() {
        MediaController localInstance = Instance;
        if (localInstance == null) {
            synchronized (MediaController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new MediaController();
                }
            }
        }
        return localInstance;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void didReceivedNotification(int id, Object... args) {

    }

    @Override
    public void newSongLoaded(Object... args) {

    }

    public int generateObserverTag() {
        return lastTag++;
    }

    public SongDetail getPlayingSongDetail() {
        return MusicPreferance.playingSongDetail;
    }

    public boolean isPlayingAudio(SongDetail messageObject) {
        return !(audioTrackPlayer == null && audioPlayer == null || messageObject == null || MusicPreferance.playingSongDetail == null || MusicPreferance.playingSongDetail != null);
    }

    public boolean isAudioPaused() {
        return isPaused;
    }


    public void playNextSong() {
//        playNextSong(false);
        Utilities.log("Next .....");
        if (getPlayingSongDetail() != null)
            if (currentPlaylistNum <= MyApplication.audiosList.size()) {
                cleanupPlayer(true, true);
                playAudio(MyApplication.audiosList.get(MediaController.currentPlaylistNum++));
            }
    }

    public void playPreviousSong() {

        if (getPlayingSongDetail() != null)
            if (currentPlaylistNum >= 0) {
                cleanupPlayer(true, true);
                playAudio(MyApplication.audiosList.get(MediaController.currentPlaylistNum--));
            }
    }

    private void stopProgressTimer() {
        synchronized (progressTimerSync) {
            if (progressTimer != null) {
                try {
                    progressTimer.cancel();
                    progressTimer = null;
                } catch (Exception e) {
                    Log.e("tmessages", e.toString());
                }
            }
        }
    }

    private void stopProximitySensor() {
        if (ignoreProximity) {
            return;
        }
        try {
            useFrontSpeaker = false;
            NotificationManager.getInstance().postNotificationName(NotificationManager.audioRouteChanged, useFrontSpeaker);
            if (sensorManager != null && proximitySensor != null) {
                sensorManager.unregisterListener(this);
            }
            if (proximityWakeLock != null && proximityWakeLock.isHeld()) {
                proximityWakeLock.release();
            }
        } catch (Throwable e) {
            Log.e("tmessages", e.toString());
        }
    }

    public boolean playAudio(final SongDetail mSongDetail) {
        MusicPreferance.playingSongDetail = mSongDetail;
        if (mSongDetail == null) {
            return false;
        }
        if ((audioTrackPlayer != null || audioPlayer != null) && MusicPreferance.playingSongDetail != null && mSongDetail.getId() == MusicPreferance.playingSongDetail.getId()) {
            if (isPaused) {
                resumeAudio(mSongDetail);
            }
            return true;
        }
        if (audioTrackPlayer != null) {
            MusicPlayerService.setIgnoreAudioFocus();
        }
        //initialize
        // load
        cleanupPlayer(!playMusicAgain, false);

        playMusicAgain = false;
        File file = null;

        try {
            audioPlayer = new MediaPlayer();
//            audioPlayer.setOnCompletionListener(this);
            audioPlayer.setAudioStreamType(useFrontSpeaker ? AudioManager.STREAM_VOICE_CALL : AudioManager.STREAM_MUSIC);
            audioPlayer.setDataSource(mSongDetail.getPath());
            audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    MusicPreferance.playingSongDetail.audioProgress = 0.0f;
                    MusicPreferance.playingSongDetail.audioProgressSec = 0;
//                    if (!MusicPreferance.playlist.isEmpty() && MusicPreferance.playlist.size() > 1) {
                    playNextSong();
//                    } else {
                    cleanupPlayer(true, true);
                }
//                }
            });

            audioPlayer.prepare();
            audioPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {


                    postPrepare(mSongDetail);
                }
            });
//            audioPlayer.start();
//            startProgressTimer();
        } catch (Exception e) {
            if (audioPlayer != null) {
                audioPlayer.release();
                audioPlayer = null;
                isPaused = false;
                MusicPreferance.playingSongDetail = null;
            }
            return false;
        }
        return true;
    }

    private void postPrepare(SongDetail mSongDetail) {
        audioPlayer.start();
        isPaused = false;
        lastProgress = 0;
        Utilities.log("Sending notification");
        NotificationManager.getInstance().postNotificationName(NotificationManager.audioDidStarted, mSongDetail);


        if (MusicPreferance.playingSongDetail != null) {
            Intent intent = new Intent(MyApplication.applicationContext, MusicPlayerService.class);
            MyApplication.applicationContext.startService(intent);
        } else {
            Intent intent = new Intent(MyApplication.applicationContext, MusicPlayerService.class);
            MyApplication.applicationContext.stopService(intent);
        }

        NotificationManager.getInstance().notifyNewSongLoaded(NotificationManager.newaudioloaded, mSongDetail);

    }

//
//    private void playNextSong(boolean byStop) {
//
//        if (byStop && repeatMode == 2) {
//            cleanupPlayer(false, false);
//            playAudio(MyApplication.audiosList.get(currentPlaylistNum));
//            return;
//        }
//        currentPlaylistNum++;
//        if (currentPlaylistNum >= MyApplication.audiosList.size()) {
//            currentPlaylistNum = 0;
//            if (byStop && repeatMode == 0) {
//                stopProximitySensor();
//                if (audioPlayer != null || audioTrackPlayer != null) {
//                    if (audioPlayer != null) {
//                        try {
//                            audioPlayer.stop();
//                        } catch (Exception e) {
//                        }
//                        try {
//                            audioPlayer.release();
//                            audioPlayer = null;
//                        } catch (Exception e) {
//                        }
//                    } else if (audioTrackPlayer != null) {
//                        synchronized (playerSongDetailSync) {
//                            try {
//                                audioTrackPlayer.pause();
//                                audioTrackPlayer.flush();
//                            } catch (Exception e) {
//                            }
//                            try {
//                                audioTrackPlayer.release();
//                                audioTrackPlayer = null;
//                            } catch (Exception e) {
//                            }
//                        }
//                    }
//                    stopProgressTimer();
//                    lastProgress = 0;
//                    isPaused = true;
//                    MusicPreferance.playingSongDetail.audioProgress = 0.0f;
//                    MusicPreferance.playingSongDetail.audioProgressSec = 0;
//                    NotificationManager.getInstance().postNotificationName(NotificationManager.audioPlayStateChanged, MusicPreferance.playingSongDetail.getId());
//                }
//                return;
//            }
//        }
//        if (currentPlaylistNum < 0 || currentPlaylistNum >= MyApplication.audiosList.size()) {
//            return;
//        }
//        playMusicAgain = true;
//        MusicPreferance.playingSongDetail.audioProgress = 0.0f;
//        MusicPreferance.playingSongDetail.audioProgressSec = 0;
//        playAudio(MyApplication.audiosList.get(currentPlaylistNum));
//    }

    public long getDuration() {
        if (audioPlayer != null) {
            return audioPlayer.getDuration() / 1000;
        }
        return 0;
    }

    public long getCurrentPosition() {
        if (audioPlayer != null) {
            return audioPlayer.getCurrentPosition();
        }
        return 0;
    }

    public boolean pauseAudio(SongDetail messageObject) {
        stopProximitySensor();
        if (audioTrackPlayer == null && audioPlayer == null || messageObject == null || MusicPreferance.playingSongDetail == null || MusicPreferance.playingSongDetail != null
                && MusicPreferance.playingSongDetail.getId() != messageObject.getId()) {
            return false;
        }
        stopProgressTimer();
        try {
            if (audioPlayer != null) {
                audioPlayer.pause();
            } else if (audioTrackPlayer != null) {
                audioTrackPlayer.pause();
            }
            isPaused = true;
            NotificationManager.getInstance().postNotificationName(NotificationManager.audioPlayStateChanged, MusicPreferance.playingSongDetail.getId());
        } catch (Exception e) {
            Log.e("tmessages", e.toString());
            isPaused = true;
            return false;
        }
        return true;
    }


    public boolean resumeAudio(SongDetail messageObject) {
        if (audioTrackPlayer == null && audioPlayer == null || messageObject == null || MusicPreferance.playingSongDetail == null || MusicPreferance.playingSongDetail != null
                && MusicPreferance.playingSongDetail.getId() != messageObject.getId()) {
            return false;
        }
        try {
            startProgressTimer();
            if (audioPlayer != null) {
                audioPlayer.start();
            } else if (audioTrackPlayer != null) {
                audioTrackPlayer.play();
            }
            isPaused = false;
            NotificationManager.getInstance().postNotificationName(NotificationManager.audioPlayStateChanged, MusicPreferance.playingSongDetail.getId());
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void stopAudio() {
        stopProximitySensor();
        if (audioTrackPlayer == null && audioPlayer == null || MusicPreferance.playingSongDetail == null) {
            return;
        }
        try {
            if (audioPlayer != null) {
                audioPlayer.stop();
            } else if (audioTrackPlayer != null) {
                audioTrackPlayer.pause();
                audioTrackPlayer.flush();
            }
        } catch (Exception e) {
        }
        try {
            if (audioPlayer != null) {
                audioPlayer.release();
                audioPlayer = null;
            } else if (audioTrackPlayer != null) {
                synchronized (playerSongDetailSync) {
                    audioTrackPlayer.release();
                    audioTrackPlayer = null;
                }
            }
        } catch (Exception e) {
        }
        stopProgressTimer();
        isPaused = false;

        Intent intent = new Intent(MyApplication.applicationContext, MusicPlayerService.class);
        MyApplication.applicationContext.stopService(intent);
    }

    private void startProgressTimer() {
        synchronized (progressTimerSync) {
            if (progressTimer != null) {
                try {
                    progressTimer.cancel();
                    progressTimer = null;
                } catch (Exception e) {
                    // FileLog.e("tmessages", e);
                }
            }
            progressTimer = new Timer();
            progressTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (sync) {
                        DMPlayerUtility.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (MusicPreferance.playingSongDetail != null && (audioPlayer != null || audioTrackPlayer != null) && !isPaused) {
                                    try {
                                        if (ignoreFirstProgress != 0) {
                                            ignoreFirstProgress--;
                                            return;
                                        }
                                        int progress;
                                        float value;
                                        if (audioPlayer != null) {
                                            progress = audioPlayer.getCurrentPosition();
                                            value = (float) lastProgress / (float) audioPlayer.getDuration();
                                            if (progress <= lastProgress) {
                                                return;
                                            }
                                        } else {
                                            progress = (int) (lastPlayPcm / 48.0f);
                                            value = (float) lastPlayPcm / (float) currentTotalPcmDuration;
                                            if (progress == lastProgress) {
                                                return;
                                            }
                                        }
                                        lastProgress = progress;
                                        MusicPreferance.playingSongDetail.audioProgress = value;
                                        MusicPreferance.playingSongDetail.audioProgressSec = lastProgress / 1000;
                                        NotificationManager.getInstance().postNotificationName(NotificationManager.audioProgressDidChanged,
                                                MusicPreferance.playingSongDetail.getId(), value);
                                    } catch (Exception e) {
                                    }
                                }
                            }
                        });
                    }
                }
            }, 0, 17);
        }
    }


    public boolean setPlaylist(ArrayList<SongDetail> allSongsList, SongDetail current, int type_, int id_) {
        type = type_;
        id = id_;

        if (MusicPreferance.playingSongDetail == current) {
            return playAudio(current);
        }
        playMusicAgain = !MusicPreferance.playlist.isEmpty();
        MusicPreferance.playlist.clear();
        if (allSongsList != null && allSongsList.size() >= 1) {
            MusicPreferance.playlist.addAll(allSongsList);
        }

        currentPlaylistNum = MusicPreferance.playlist.indexOf(current);
        if (currentPlaylistNum == -1) {
            MusicPreferance.playlist.clear();
            MusicPreferance.shuffledPlaylist.clear();
            return false;
        }
        if (shuffleMusic) {
            currentPlaylistNum = 0;
        }
        return playAudio(current);
    }

    public boolean seekToProgress(SongDetail mSongDetail, float progress) {
        if (audioTrackPlayer == null && audioPlayer == null) {
            return false;
        }
        try {
            if (audioPlayer != null) {
                int seekTo = (int) (audioPlayer.getDuration() * progress);
                audioPlayer.seekTo(seekTo);
                lastProgress = seekTo;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void cleanupPlayer(Context context, boolean notify, boolean stopService) {
        cleanupPlayer(notify, stopService);
    }

    public void cleanupPlayer(boolean notify, boolean stopService) {
        pauseAudio(getPlayingSongDetail());
        stopProximitySensor();
        if (audioPlayer != null) {
            try {
                audioPlayer.reset();
            } catch (Exception e) {
            }
            try {
                audioPlayer.stop();
            } catch (Exception e) {
            }
            try {
                audioPlayer.release();
                audioPlayer = null;
            } catch (Exception e) {
            }
        } else if (audioTrackPlayer != null) {
            synchronized (playerSongDetailSync) {
                try {
                    audioTrackPlayer.pause();
                    audioTrackPlayer.flush();
                } catch (Exception e) {
                }
                try {
                    audioTrackPlayer.release();
                    audioTrackPlayer = null;
                } catch (Exception e) {
                }
            }
        }
        stopProgressTimer();
        isPaused = true;
        if (stopService) {
            Intent intent = new Intent(MyApplication.applicationContext, MusicPlayerService.class);
            MyApplication.applicationContext.stopService(intent);
        }
    }

//    @Override
//    public void onCompletion(MediaPlayer mp) {
//        playNextSong();
//    }
}
