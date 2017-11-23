/*
 * GenericFragment
 * JDGSoundboard
 *
 * Copyright (c) 2017 Vincent Lammin
 */

package com.atmx.android.jdgspl;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import com.atmx.android.jdgspl.adapters.SoundboardAdapter;
import com.atmx.android.jdgspl.events.RefreshFavoritesEvent;
import com.atmx.android.jdgspl.events.RefreshSoundsEvent;
import com.atmx.android.jdgspl.events.SearchSoundsEvent;
import com.atmx.android.jdgspl.models.DbHelper;
import com.atmx.android.jdgspl.models.SoundModel;
import com.atmx.android.jdgspl.objects.SoundSection;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class PagerFragment extends Fragment {

    private int position;
    private List<SoundSection> soundSections;
    private SoundboardActivity activity;
    private SoundboardAdapter adapter;

    public static PagerFragment newInstance(int position) {
        PagerFragment gFragment = new PagerFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        gFragment.setArguments(args);

        return gFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments() != null ? getArguments().getInt("position") : 0;
        activity = (SoundboardActivity) getActivity();

        fetchSounds(activity.getSort(), activity.getOrder(), activity.getSearch());
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.pager_fragment, container, false);

        adapter = new SoundboardAdapter(
            activity.getLayoutInflater(),
            soundSections,
            R.layout.grid_row,
            R.id.grid_row_header,
            R.id.grid_row_item_holder,
            activity.getString(R.string.no_sound)
        );

        ListView listView = view.findViewById(R.id.sectioned_grid_list);
        listView.setAdapter(adapter);
        listView.setDividerHeight(0);

        // Search fragment
        if (position == 7) {
            SearchView searchView = view.findViewById(R.id.search_view);
            searchView.setVisibility(View.VISIBLE);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    query = query.trim();
                    activity.setSearch(query);
                    if (query.isEmpty() || query.length() > 2)
                        EventBus.getDefault().post(new SearchSoundsEvent());

                    return true;
                }
            });
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void fetchSounds(String sort, String order, String search) {
        DbHelper dbHelper = DbHelper.getInstance(getActivity().getApplicationContext());
        SoundModel soundModel = dbHelper.getSoundModel();

        switch (position) {
            case 0:
                soundSections = soundModel.getAllMap(sort, order);
                break;
            case 5:
                soundSections = soundModel.getAllNewsMap(sort, order);
                break;
            case 6:
                soundSections = soundModel.getAllFavoritesMap(sort, order);
                break;
            case 7:
                soundSections = soundModel.searchAllMap(sort, order, search);
                break;
            default:
                soundSections = soundModel.getAllByCategoryIdMap(
                    SoundboardActivity.sTitles.get(position),
                    sort,
                    order
                );
        }
    }

    /**
     * Event handling
     **/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshFavoritesEvent event) {
        if (adapter != null && position == 6) {
            fetchSounds(activity.getSort(), activity.getOrder(), null);
            adapter.updateData(soundSections);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(RefreshSoundsEvent event) {
        if (adapter != null) {
            if (position == 7) {
                fetchSounds(activity.getSort(), activity.getOrder(), activity.getSearch());
                adapter.updateData(soundSections);
            } else {
                fetchSounds(activity.getSort(), activity.getOrder(), null);
                adapter.updateData(soundSections);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SearchSoundsEvent event) {
        if (adapter != null && position == 7) {
            fetchSounds(activity.getSort(), activity.getOrder(), activity.getSearch());
            adapter.updateData(soundSections);
        }
    }
}
