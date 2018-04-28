package com.slensky.focussis.views.adapters;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.util.TypedValue;

import com.slensky.focussis.FocusApplication;
import com.slensky.focussis.R;

import java.util.List;

/**
 * Created by slensky on 4/20/18.
 */

public abstract class SelectableItemAdapter<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {
    private static final int[] ATTRS = new int[]{
            android.R.attr.selectableItemBackground
    };
    private SparseBooleanArray selectedItems;

    public SelectableItemAdapter() {
        this.selectedItems = new SparseBooleanArray();
    }

    @Override
    public void onBindViewHolder(@NonNull T holder, int position) {
        /* Change background color of the selected items in list view  */
        if (selectedItems.size() > 0) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = holder.itemView.getContext().getTheme();
            theme.resolveAttribute(R.attr.selectedItemBackground, typedValue, true);
            holder.itemView.setBackgroundColor(selectedItems.get(position) ? typedValue.data : Color.TRANSPARENT);
        }
        else {
            TypedArray ta = holder.itemView.getContext().getApplicationContext().obtainStyledAttributes(ATTRS);
            Drawable selectableItemBackground = ta.getDrawable(0);
            holder.itemView.setBackground(selectableItemBackground);
            ta.recycle();
        }
    }

    //Toggle selection methods
    public void toggleSelection(int position) {
        selectView(position, !selectedItems.get(position));
    }

    //Remove selected selections
    public void removeSelection() {
        selectedItems = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    //Put or delete selected position into SparseBooleanArray
    public void selectView(int position, boolean value) {
        if (value)
            selectedItems.put(position, value);
        else
            selectedItems.delete(position);

        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedItems.size();
    }

    //Return all selected
    public SparseBooleanArray getSelected() {
        return selectedItems;
    }


}
