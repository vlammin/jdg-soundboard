/*
 * SectionGridAdapter
 * JDGSoundboard
 *
 * Copyright (c) 2018 Vincent Lammin
 */

package com.atmx.android.jdgspl.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/*
 * Based on https://github.com/velos/SectionedGrid
 */

abstract class SectionGridAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private int rowID;
    private int headerID;
    private int itemHolderID;
    private int colCount;
    private String noDataText;

    /**
     * Constructor.
     *
     * @param inflater     inflater to create rows within the grid.
     * @param rowID        layout resource ID for each row within the grid.
     * @param headerID     layout resource ID for the header element contained within the grid row.
     * @param itemHolderID layout resource ID for the cell wrapper contained within the grid row.
     * @param noDataText   text to show in header if there is no data
     */
    SectionGridAdapter(
        LayoutInflater inflater,
        int rowID,
        int headerID,
        int itemHolderID,
        String noDataText) {
        super();
        this.inflater = inflater;
        this.rowID = rowID;
        this.headerID = headerID;
        this.itemHolderID = itemHolderID;
        this.noDataText = noDataText;

        this.colCount = getColCount();
    }

    /**
     * @return how many columns the row uses.
     */
    private int getColCount() {
        View row = inflater.inflate(rowID, null);
        if (row == null)
            throw new IllegalArgumentException("Invalid row layout ID provided.");

        ViewGroup holder = (ViewGroup) row.findViewById(itemHolderID);
        if (holder == null)
            throw new IllegalArgumentException("Item holder ID was not found in the row.");
        if (holder.getChildCount() == 0)
            throw new IllegalArgumentException("Item holder does not contain any items.");

        return holder.getChildCount();
    }

    /**
     * @return the total number of items to display.
     */
    protected abstract int getDataCount();

    /**
     * @return the number of sections to display.
     */
    protected abstract int getSectionsCount();

    /**
     * @param index the 0-based index of the section to count.
     * @return the number of items in the requested section.
     */
    protected abstract int getCountInSection(int index);

    /**
     * @param position the 0-based index of the data element in the grid.
     * @return which section this item belongs to.
     */
    protected abstract int getSectionIndex(int position);

    /**
     * @param section the 0-based index of the section.
     * @return the text to display for this section.
     */
    protected abstract String getHeaderForSection(int section);

    /**
     * Populate the View and attach any listeners.
     *
     * @param cell     the inflated cell View to populate.
     * @param position the 0-based index of the data element in the grid.
     */
    protected abstract void bindView(View cell, int position);

    @Override
    public int getCount() {
        int rowCount = 0;
        int sectionsCount = getSectionsCount();
        for (int i = 0; i < sectionsCount; i++) {
            rowCount += (getCountInSection(i) + colCount - 1) / colCount;
        }
        return rowCount > 0 ? rowCount : 1;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int realPosition = 0;
        int viewsToDraw = 0;
        int rowCount = 0;
        int totalCount = 0;

        int sectionsCount = getSectionsCount();
        for (int i = 0; i < sectionsCount; i++) {
            int count = getCountInSection(i);
            totalCount += count;

            if (count > 0 && position <= rowCount + (count - 1) / colCount) {
                realPosition += (position - rowCount) * colCount;
                viewsToDraw = totalCount - realPosition;
                break;
            } else {
                rowCount += (count + colCount - 1) / colCount;
                realPosition += count;
            }
        }

        if (convertView == null)
            convertView = inflater.inflate(rowID, parent, false);

        int lastSectionId = -1;
        if (realPosition > 0)
            lastSectionId = getSectionIndex(realPosition - 1);

        TextView header = (TextView) convertView.findViewById(headerID);
        if (getDataCount() > 0) {
            int newSectionId = getSectionIndex(realPosition);
            if (newSectionId != lastSectionId) {
                header.setText(getHeaderForSection(newSectionId));
                header.setVisibility(View.VISIBLE);
            } else {
                header.setVisibility(View.GONE);
            }
        } else {
            if (noDataText != null)
                header.setText(noDataText);
            else
                header.setVisibility(View.GONE);
        }

        ViewGroup itemHolder = (ViewGroup) convertView.findViewById(itemHolderID);
        for (int i = 0; i < colCount; i++) {
            View child = itemHolder.getChildAt(i);
            if (child != null) {
                if (i < viewsToDraw) {
                    bindView(child, realPosition + i);
                    child.setVisibility(View.VISIBLE);
                } else {
                    child.setVisibility(View.INVISIBLE);
                }
            }
        }

        return convertView;
    }
}
