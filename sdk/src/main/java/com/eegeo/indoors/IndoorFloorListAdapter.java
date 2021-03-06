// Copyright eeGeo Ltd (2012-2015), All Rights Reserved

package com.eegeo.indoors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.eegeo.mapapi.R;

import java.util.ArrayList;
import java.util.List;


public class IndoorFloorListAdapter extends BaseAdapter {

    private int m_itemViewId;
    private List<String> m_nameData;

    public IndoorFloorListAdapter(int itemViewId) {
        m_itemViewId = itemViewId;
        m_nameData = new ArrayList<String>();
    }

    public void setData(List<String> nameData) {
        m_nameData = nameData;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return m_nameData.size();
    }

    @Override
    public Object getItem(int index) {
        return m_nameData.get(index);
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int index, View contextView, ViewGroup parent) {
        final String floorName = (String) getItem(index);

        if (contextView == null) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            contextView = inflater.inflate(m_itemViewId, null);
        }

        TextView nameLabel = (TextView) contextView.findViewById(R.id.floor_name);

        nameLabel.setText(floorName);
        nameLabel.setHorizontallyScrolling(false);
        nameLabel.setSingleLine();

        View upperLine = (View) contextView.findViewById(R.id.interiors_explorer_floor_list_item_topline);
        if (upperLine != null) {
            upperLine.setVisibility(index == 0 ? View.INVISIBLE : View.VISIBLE);
        }

        View bottomLine = (View) contextView.findViewById(R.id.interiors_explorer_floor_list_item_bottomline);
        if (bottomLine != null) {
            bottomLine.setVisibility(index == m_nameData.size() - 1 ? View.INVISIBLE : View.VISIBLE);
        }

        return contextView;
    }
}
