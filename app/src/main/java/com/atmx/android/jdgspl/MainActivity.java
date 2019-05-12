/*
 * MainActivity
 * JDGSoundboard
 *
 * Copyright (c) 2019 Vincent Lammin
 */

package com.atmx.android.jdgspl;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.atmx.android.jdgspl.dialogs.InfoDialog;
import com.atmx.android.jdgspl.models.DbHelper;
import com.atmx.android.jdgspl.objects.RssEntry;
import com.atmx.android.jdgspl.tools.Player;


public class MainActivity extends AppCompatActivity {

    private RssEntry lastRssEntry;
    private TextView newsTitle;
    private TextView newsPubDate;
    private GetLastNews mAsyncHandler = new GetLastNews();

    private DbHelper dbHelper;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = DbHelper.getInstance(getApplicationContext());

        if (isFirstRun()) {
            dbHelper.fillDbFromXml(getApplicationContext());
            dbHelper.migrateOldFavorites();
        }

        setContentView(R.layout.activity_main);


        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        initDashboard();

        mAsyncHandler.execute();

        RelativeLayout lastNewsLayout = findViewById(R.id.last_news_layout);
        lastNewsLayout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (mAsyncHandler.getStatus() == Status.RUNNING) {
                    return false;
                } else if (newsTitle.getText().equals(getString(R.string.error_get_rss))) {
                    mAsyncHandler = new GetLastNews();
                    mAsyncHandler.execute();
                } else {
                    goToLastNews();
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        Player.getInstance().release();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                showInfoDialog();
                return true;

            case R.id.action_youtube:
                goToYoutubeChannel();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private boolean isFirstRun() {
        boolean firstRun = false;
        try {
            SharedPreferences mPrefs = getSharedPreferences(getPackageName(), MODE_PRIVATE);
            PackageInfo pInfo = getPackageManager().getPackageInfo(
                getPackageName(), PackageManager.GET_META_DATA
            );

            if (mPrefs.getLong("lastRunVersionCode", MODE_PRIVATE) < pInfo.versionCode) {
                Editor editor = mPrefs.edit();
                editor.putLong("lastRunVersionCode", pInfo.versionCode);
                editor.apply();

                firstRun = true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return firstRun;
    }

    private void initDashboardElement(
        final int layoutId,
        final int textViewId,
        final int categoryId,
        String mode
    ) {
        Long count;
        switch (mode) {
            case "all_sounds":
                count = dbHelper.getSoundModel().getCount();
                break;
            case "new":
                count = dbHelper.getSoundModel().getNewsCount();
                break;
            case "favorites":
                count = dbHelper.getFavoriteModel().getCount();
                break;
            case "search":
                count = null;
                break;
            default:
                count = dbHelper.getSoundModel().getCountByCategoryId(categoryId);
                break;
        }

        TextView countTxt = findViewById(textViewId);
        if (count == null)
            countTxt.setVisibility(View.GONE);
        else
            countTxt.setText(
                getResources().getQuantityString(
                    R.plurals.sound_count,
                    count.intValue(),
                    count
                )
            );

        RelativeLayout layout = findViewById(layoutId);
        layout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                onDashboardItemClick(categoryId);
            }
        });
    }

    private void initDashboard() {
        initDashboardElement(R.id.rl_cat1, R.id.cat1_count, R.string.cat1_allsounds, "all_sounds");
        initDashboardElement(R.id.rl_cat2, R.id.cat2_count, R.string.cat2_music, "sounds");
        initDashboardElement(R.id.rl_cat3, R.id.cat3_count, R.string.cat3_gamesucks, "sounds");
        initDashboardElement(R.id.rl_cat4, R.id.cat4_count, R.string.cat4_rage, "sounds");
        initDashboardElement(R.id.rl_cat5, R.id.cat5_count, R.string.cat5_cult, "sounds");
        initDashboardElement(R.id.rl_cat6, R.id.cat6_count, R.string.cat6_new, "new");
        initDashboardElement(R.id.rl_cat7, R.id.cat7_count, R.string.cat7_favorite, "favorites");
        initDashboardElement(R.id.rl_cat8, R.id.cat8_count, R.string.cat8_search, "search");
    }

    public void showInfoDialog() {
        InfoDialog infoDialog = new InfoDialog();
        infoDialog.show(getSupportFragmentManager(), "info_dialog");
    }

    public void goToYoutubeChannel() {
        String url = getString(R.string.url_youtube);
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri u = Uri.parse(url);
        i.setData(u);
        startActivity(i);
    }

    public void goToLastNews() {
        String url = lastRssEntry.getLink();
        Intent i = new Intent(Intent.ACTION_VIEW);
        Uri u = Uri.parse(url);
        i.setData(u);
        startActivity(i);
    }

    public void onDashboardItemClick(int categoryId) {
        Intent i = new Intent(getBaseContext(), SoundboardActivity.class);
        i.putExtra("categoryId", categoryId);
        startActivityForResult(i, 0);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            initDashboard();
        }
    }

    private class GetLastNews extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPostExecute(Void result) {
            displayRssData();
        }

        @Override
        protected void onPreExecute() {
            preReadRssFlow();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            readRssFlow();
            return null;
        }
    }

    private void preReadRssFlow() {
        newsTitle = findViewById(R.id.news_title);
        newsPubDate = findViewById(R.id.news_pubdate);
        newsPubDate.setText("");
        newsTitle.setText(R.string.in_progress);
    }

    private void readRssFlow() {
        lastRssEntry = RssEntry.getLastRssNews(getString(R.string.url_rss));
    }

    private void displayRssData() {
        if (lastRssEntry == null) {
            newsTitle.setText(getString(R.string.error_get_rss));
            newsPubDate.setText(getString(R.string.touch_to_retry));
        } else {
            newsTitle.setText(lastRssEntry.getTitle());
            newsTitle.setGravity(Gravity.START);
            newsPubDate.setText(lastRssEntry.getFormattedPublishDate());
            newsPubDate.setGravity(Gravity.END);
        }
    }
}
