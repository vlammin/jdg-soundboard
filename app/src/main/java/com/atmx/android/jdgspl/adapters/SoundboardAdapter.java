/*
 * SoundboardAdapter
 * JDGSoundboard
 *
 * Copyright (c) 2018 Vincent Lammin
 */

package com.atmx.android.jdgspl.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;

import com.atmx.android.jdgspl.R;
import com.atmx.android.jdgspl.events.PlaySoundEvent;
import com.atmx.android.jdgspl.events.ShowSoundDetailEvent;
import com.atmx.android.jdgspl.objects.Sound;
import com.atmx.android.jdgspl.objects.SoundSection;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class SoundboardAdapter extends SectionGridAdapter {

    private List<SoundSection> soundSections;

    public SoundboardAdapter(
        LayoutInflater inflater,
        List<SoundSection> soundSections,
        int rowID,
        int headerID,
        int itemHolderID,
        String noDataText
    ) {
        super(inflater, rowID, headerID, itemHolderID, noDataText);
        this.soundSections = soundSections;
    }

    @Override
    protected int getDataCount() {
        int count = 0;
        for (SoundSection section : soundSections)
            count += section.size();

        return count;
    }

    @Override
    protected int getSectionsCount() {
        return soundSections.size();
    }

    @Override
    protected int getCountInSection(int index) {
        return soundSections.get(index).size();
    }

    @Override
    public Sound getItem(int position) {
        int sectionCount = getSectionsCount();
        for (int i = 0; i < sectionCount; i++) {
            int count = getCountInSection(i);
            if (position < count) {
                return soundSections.get(i).get(position);
            }
            position -= count;
        }
        return null;
    }

    @Override
    protected int getSectionIndex(int position) {
        int sectionCount = getSectionsCount();
        for (int i = 0; i < sectionCount; i++) {
            int count = getCountInSection(i);
            if (position < count) {
                return i;
            }
            position -= count;
        }
        return -1;
    }

    @Override
    protected String getHeaderForSection(int section) {
        return soundSections.get(section).getTitle();
    }

    @Override
    protected void bindView(View convertView, int position) {
        final Sound sound = getItem(position);
        Button button = convertView.findViewById(R.id.sample_button);
        button.setText(sound.getTitle());

        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EventBus.getDefault().post(new PlaySoundEvent(sound));
            }
        });

        button.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                EventBus.getDefault().post(new ShowSoundDetailEvent(sound));
                return true;
            }
        });
    }

    public void updateData(List<SoundSection> soundSections) {
        this.soundSections = soundSections;
        notifyDataSetChanged();
    }
}
