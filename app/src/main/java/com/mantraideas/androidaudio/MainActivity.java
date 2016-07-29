package com.mantraideas.androidaudio;

import android.app.Notification;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mantraideas.androidaudio.DataSources.MyDataQuery;
import com.mantraideas.androidaudio.DataSources.OnDataReceived;
import com.mantraideas.androidaudio.manager.MediaController;
import com.mantraideas.androidaudio.manager.NotificationManager;
import com.mantraideas.androidaudio.phonemidea.DMPlayerUtility;
import com.mantraideas.androidaudio.uicomponent.Slider;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnDataReceived, View.OnClickListener, Slider.OnValueChangedListener  {

    RecyclerView rv_audios;
    MyDataQuery dq;
    RecyclerView.Adapter adapter;
    //    SlidingUpPanelLayout mLayout;
    private int theme;
    private FrameLayout statusBar;
    private DrawerLayout mDrawerLayout;
    private RecyclerView recyclerViewDrawer;
    private RecyclerView.Adapter adapterDrawer;
    private SlidingUpPanelLayout mLayout;
    private RelativeLayout slidepanelchildtwo_topviewone;
    private RelativeLayout slidepanelchildtwo_topviewtwo;
    private boolean isExpand = false;
    private DisplayImageOptions options;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    //    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    private ImageView songAlbumbg;
    private ImageView img_bottom_slideone;
    private ImageView img_bottom_slidetwo;

    private TextView txt_playesongname;
    private TextView txt_songartistname;
    private TextView txt_playesongname_slidetoptwo;
    private TextView txt_songartistname_slidetoptwo;
    private TextView txt_timeprogress;
    private TextView txt_timetotal;
    private ImageView imgbtn_backward;
    private ImageView imgbtn_forward;
    private ImageView imgbtn_toggle;
    private ImageView imgbtn_suffel;
    private ImageView img_Favorite;
    private PlayPauseView btn_playpause;
    private PlayPauseView btn_playpausePanel;
    private Slider audio_progress;
    private boolean isDragingStart = false;
    private int TAG_Observer;
    private int currentIndex = 0;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initiSlidingUpPanel();
        dq = new MyDataQuery(this, this);
        dq.getRequestData("");
        rv_audios = (RecyclerView) findViewById(R.id.rv_audio);
        rv_audios.setLayoutManager(new LinearLayoutManager(this));
        final LayoutInflater layoutInflater = getLayoutInflater().from(this);
        adapter = new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = layoutInflater.inflate(R.layout.row_audio, parent, false);
                return new ViewHolderAudio(v);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
                Utilities.log("On bind view holder");
                ViewHolderAudio mHolder = (ViewHolderAudio) holder;
                mHolder.title.setText(MyApplication.audiosList.get(position).getTitle());
                mHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        Utilities.log("Url = " + MyApplication.audiosList.get(position).path);
                        MediaController.currentPlaylistNum = position;
                        MediaController.getInstance().cleanupPlayer(true, true);
                        MediaController.getInstance().playAudio(MyApplication.audiosList.get(position));
                        updateTitle(MyApplication.audiosList.get(position));

                    }
                });
            }

            @Override
            public int getItemCount() {
                return MyApplication.audiosList.size();
            }
        };
        rv_audios.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bottombar_play:
                if (MediaController.getInstance().getPlayingSongDetail() != null)
                    PlayPauseEvent(v);
                break;

            case R.id.btn_play:
                if (MediaController.getInstance().getPlayingSongDetail() != null)
                    PlayPauseEvent(v);
                break;

            case R.id.btn_forward:
                if (MediaController.getInstance().getPlayingSongDetail() != null)
                    if (MediaController.currentPlaylistNum <= MyApplication.audiosList.size()) {
                        MediaController.getInstance().cleanupPlayer(true, true);
                        MediaController.getInstance().playAudio(MyApplication.audiosList.get(MediaController.currentPlaylistNum++));
                        updateTitle(MyApplication.audiosList.get(MediaController.currentPlaylistNum++));

                    }
                break;

            case R.id.btn_backward:
                if (MediaController.getInstance().getPlayingSongDetail() != null)
                    if (MediaController.currentPlaylistNum >= 0) {
                        MediaController.getInstance().cleanupPlayer(true, true);
                        MediaController.getInstance().playAudio(MyApplication.audiosList.get(MediaController.currentPlaylistNum--));
                        updateTitle(MyApplication.audiosList.get(MediaController.currentPlaylistNum--));

                    }
                break;

            case R.id.btn_suffel:

                break;

            case R.id.btn_toggle:

                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            updateTitle(MyApplication.audiosList.get(MediaController.currentPlaylistNum));
        }catch (Exception e){
            e.printStackTrace();
        }
        }

    @Override
    public void onValueChanged(int value) {
        MediaController.getInstance().seekToProgress(MediaController.getInstance().getPlayingSongDetail(), (float) value / 100);
    }


    public static class ViewHolderAudio extends RecyclerView.ViewHolder {
        TextView title;

        public ViewHolderAudio(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }


    private void PlayPauseEvent(View v) {
        if (MediaController.getInstance().isAudioPaused()) {
            MediaController.getInstance().playAudio(MediaController.getInstance().getPlayingSongDetail());
            ((PlayPauseView) v).Play();

            Utilities.log("paused.....");
        } else {
            Utilities.log("playing.....");
            MediaController.getInstance().pauseAudio(MediaController.getInstance().getPlayingSongDetail());
            ((PlayPauseView) v).Pause();
        }
    }

    private void initiSlidingUpPanel() {
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        songAlbumbg = (ImageView) findViewById(R.id.image_songAlbumbg_mid);
        img_bottom_slideone = (ImageView) findViewById(R.id.img_bottom_slideone);
        img_bottom_slidetwo = (ImageView) findViewById(R.id.img_bottom_slidetwo);

        txt_timeprogress = (TextView) findViewById(R.id.slidepanel_time_progress);
        txt_timetotal = (TextView) findViewById(R.id.slidepanel_time_total);
        imgbtn_backward = (ImageView) findViewById(R.id.btn_backward);
        imgbtn_forward = (ImageView) findViewById(R.id.btn_forward);
        imgbtn_toggle = (ImageView) findViewById(R.id.btn_toggle);
        imgbtn_suffel = (ImageView) findViewById(R.id.btn_suffel);
        btn_playpause = (PlayPauseView) findViewById(R.id.btn_play);
        audio_progress = (Slider) findViewById(R.id.audio_progress_control);
        btn_playpausePanel = (PlayPauseView) findViewById(R.id.bottombar_play);
        img_Favorite = (ImageView) findViewById(R.id.bottombar_img_Favorite);

        TypedValue typedvaluecoloraccent = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorAccent, typedvaluecoloraccent, true);
        final int coloraccent = typedvaluecoloraccent.data;
        audio_progress.setBackgroundColor(coloraccent);
        audio_progress.setValue(0);

        audio_progress.setOnValueChangedListener(this);
        imgbtn_backward.setOnClickListener(this);
        imgbtn_forward.setOnClickListener(this);
        imgbtn_toggle.setOnClickListener(this);
        imgbtn_suffel.setOnClickListener(this);
        img_Favorite.setOnClickListener(this);
        btn_playpausePanel.Pause();
        btn_playpause.Pause();
        txt_playesongname = (TextView) findViewById(R.id.txt_playesongname);
        txt_songartistname = (TextView) findViewById(R.id.txt_songartistname);
        txt_playesongname_slidetoptwo = (TextView) findViewById(R.id.txt_playesongname_slidetoptwo);
        txt_songartistname_slidetoptwo = (TextView) findViewById(R.id.txt_songartistname_slidetoptwo);

        slidepanelchildtwo_topviewone = (RelativeLayout) findViewById(R.id.slidepanelchildtwo_topviewone);
        slidepanelchildtwo_topviewtwo = (RelativeLayout) findViewById(R.id.slidepanelchildtwo_topviewtwo);

        slidepanelchildtwo_topviewone.setVisibility(View.VISIBLE);
        slidepanelchildtwo_topviewtwo.setVisibility(View.INVISIBLE);

        slidepanelchildtwo_topviewone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

            }
        });

        slidepanelchildtwo_topviewtwo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

            }
        });

        ((PlayPauseView) findViewById(R.id.bottombar_play)).setOnClickListener(this);
        ((PlayPauseView) findViewById(R.id.btn_play)).setOnClickListener(this);

        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if (slideOffset == 0.0f) {
                    isExpand = false;
                    slidepanelchildtwo_topviewone.setVisibility(View.VISIBLE);
                    slidepanelchildtwo_topviewtwo.setVisibility(View.INVISIBLE);
                } else if (slideOffset > 0.0f && slideOffset < 1.0f) {
                    // if (isExpand) {
                    // slidepanelchildtwo_topviewone.setAlpha(1.0f);
                    // slidepanelchildtwo_topviewtwo.setAlpha(1.0f -
                    // slideOffset);
                    // } else {
                    // slidepanelchildtwo_topviewone.setAlpha(1.0f -
                    // slideOffset);
                    // slidepanelchildtwo_topviewtwo.setAlpha(1.0f);
                    // }

                } else {
                    isExpand = true;
                    slidepanelchildtwo_topviewone.setVisibility(View.INVISIBLE);
                    slidepanelchildtwo_topviewtwo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }

        });

    }

    public void loadSongsDetails(SongDetail mDetail) {
//        String contentURI = "content://media/external/audio/media/" + mDetail.getId() + "/albumart";
//        imageLoader.displayImage(contentURI, songAlbumbg, options, animateFirstListener);
//        imageLoader.displayImage(contentURI, img_bottom_slideone, options, animateFirstListener);
//        imageLoader.displayImage(contentURI, img_bottom_slidetwo, options, animateFirstListener);

        txt_playesongname.setText(mDetail.getTitle());
        txt_songartistname.setText(mDetail.getArtist());
        txt_playesongname_slidetoptwo.setText(mDetail.getTitle());
        txt_songartistname_slidetoptwo.setText(mDetail.getArtist());

        if (txt_timetotal != null) {
            long duration = Long.valueOf(mDetail.getDuration());
            txt_timetotal.setText(duration != 0 ? String.format("%d:%02d", duration / 60, duration % 60) : "-:--");
        }
        updateProgress(mDetail);
    }


    private void updateTitle(SongDetail mSongDetail) {
            updateProgress(mSongDetail);
            if (MediaController.getInstance().isAudioPaused()) {
                btn_playpausePanel.Pause();
                btn_playpause.Pause();
            } else {
                btn_playpausePanel.Play();
                btn_playpause.Play();
            }
            SongDetail audioInfo = MediaController.getInstance().getPlayingSongDetail();
            loadSongsDetails(audioInfo);

            if (txt_timetotal != null) {
                long duration = Long.valueOf(MediaController.getInstance().getDuration());
                txt_timetotal.setText(duration != 0 ? String.format("%d:%02d", duration / 60, duration % 60) : "-:--");
                updateProgressBar();

            }
    }
    private void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 500);
    }
    long totalDuration;
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {

            long currentDuration = MediaController.getInstance().getCurrentPosition();
            Utilities.log("Current duration  = " + currentDuration);
            txt_timeprogress.setText("" + milliSecondsToTimer(currentDuration));
            Utilities.log("Mil to timer = " + milliSecondsToTimer(currentDuration));
            int progress = (int) (getProgressPercentage(currentDuration, MediaController.getInstance().getDuration()*1000));
            Utilities.log("Progress .... "+progress + "");
//            audio_progress.setMax(100);
            audio_progress.setValue(progress);
            mHandler.postDelayed(this, 500);
        }
    };
    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

    public int getProgressPercentage(long currentDuration, long totalDuration) {
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        percentage = (((double) currentSeconds) / totalSeconds) * 100;

        return percentage.intValue();
    }

    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double) progress) / 100) * totalDuration);
        return currentDuration * 1000;
    }

    private void updateProgress(SongDetail mSongDetail) {
        if (audio_progress != null) {
            if (!isDragingStart) {
                audio_progress.setValue((int) (mSongDetail.audioProgress * 100));
            }
            long duration = MediaController.getInstance().getDuration();
            String timeString = String.format("%d:%02d", duration/ 60, duration % 60);
            txt_timeprogress.setText(timeString);
        }
    }


    @Override
    public void onSuccess(String table_name, String result) {
        MyApplication.audiosList.addAll(SongDetail.getMp3List(result));
        adapter.notifyDataSetChanged();
    }
}
