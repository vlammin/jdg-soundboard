/*
 * SoundboardActivity
 * JDGSoundboard
 *
 * Copyright (c) 2017 Vincent Lammin
 */

package com.atmx.android.jdgspl;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.atmx.android.jdgspl.adapters.SoundboardPagerAdapter;
import com.atmx.android.jdgspl.dialogs.PlayerDialog;
import com.atmx.android.jdgspl.dialogs.SoundDetailDialog;
import com.atmx.android.jdgspl.events.PlaySoundEvent;
import com.atmx.android.jdgspl.events.RefreshSoundsEvent;
import com.atmx.android.jdgspl.events.ShowSoundDetailEvent;
import com.atmx.android.jdgspl.models.SoundModel;
import com.atmx.android.jdgspl.models.VideoModel;
import com.atmx.android.jdgspl.objects.Sound;
import com.atmx.android.jdgspl.tools.Player;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class SoundboardActivity extends AppCompatActivity {

    static final String STATE_SORT = "stateSort";
    static final String STATE_ORDER = "stateOrder";
    static final String STATE_SEARCH = "stateSearch";

    public static ArrayList<Integer> sTitles;

    private String sort = SoundModel.Sort.TITLE;
    private String order = "ASC";
    private String search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            sort = savedInstanceState.getString(STATE_SORT);
            order = savedInstanceState.getString(STATE_ORDER);
            search = savedInstanceState.getString(STATE_SEARCH);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_soundboard);
        Bundle b = getIntent().getExtras();

        Toolbar toolbar = findViewById(R.id.toolbar_soundboard);
        setSupportActionBar(toolbar);

        sTitles = new ArrayList<>();
        sTitles.add(R.string.cat1_allsounds);
        sTitles.add(R.string.cat2_music);
        sTitles.add(R.string.cat3_gamesucks);
        sTitles.add(R.string.cat4_rage);
        sTitles.add(R.string.cat5_cult);
        sTitles.add(R.string.cat6_new);
        sTitles.add(R.string.cat7_favorite);
        sTitles.add(R.string.cat8_search);

        int startPage = sTitles.indexOf(b.getInt("startPage"));
        if (startPage == -1) startPage = 0;

        ViewPager mViewPager = findViewById(R.id.pager);
        PagerAdapter mPagerAdapter = new SoundboardPagerAdapter(
            getSupportFragmentManager(),
            sTitles,
            this
        );
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(startPage);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(STATE_SORT, sort);
        savedInstanceState.putString(STATE_ORDER, order);
        savedInstanceState.putString(STATE_SEARCH, search);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        Player.getInstance().release();
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        if (!isChangingConfigurations())
            Player.getInstance().release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FragmentManager fm = getSupportFragmentManager();
        PlayerDialog playerDialog = (PlayerDialog) fm.findFragmentByTag("player_dialog");
        if (playerDialog != null) {
            Player.getInstance().replacePlayerDialog(playerDialog);
            Player.getInstance().refreshPlayerDialog();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        if (!isChangingConfigurations())
            Player.getInstance().release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.soundboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_default:
                sort = SoundModel.Sort.TITLE;
                order = "ASC";
                EventBus.getDefault().post(new RefreshSoundsEvent());
                return true;

            case R.id.sort_video_title:
                sort = VideoModel.Sort.TITLE;
                order = "ASC";
                EventBus.getDefault().post(new RefreshSoundsEvent());
                return true;

            case R.id.sort_video_publish_date_asc:
                sort = VideoModel.Sort.PUBLISH_DATE;
                order = "ASC";
                EventBus.getDefault().post(new RefreshSoundsEvent());
                return true;

            case R.id.sort_video_publish_date_desc:
                sort = VideoModel.Sort.PUBLISH_DATE;
                order = "DESC";
                EventBus.getDefault().post(new RefreshSoundsEvent());
                return true;

            case R.id.action_home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void showSoundDetailDialog(Sound sound) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("sound", sound);

        SoundDetailDialog detailDialog = new SoundDetailDialog();
        detailDialog.setArguments(bundle);
        detailDialog.show(getSupportFragmentManager(), "sound_detail_dialog");
    }

    public String getSort() {
        return sort;
    }

    public String getOrder() {
        return order;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    /**
     * Event handling
     **/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PlaySoundEvent event) {
        Sound sound = event.getSound();
        if (sound != null)
            Player.getInstance().play(this, sound);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ShowSoundDetailEvent event) {
        Sound sound = event.getSound();
        if (sound != null)
            showSoundDetailDialog(sound);
    }
}
