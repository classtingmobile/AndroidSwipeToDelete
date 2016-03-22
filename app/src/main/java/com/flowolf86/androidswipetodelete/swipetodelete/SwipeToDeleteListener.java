package com.flowolf86.androidswipetodelete.swipetodelete;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by github.com/flowolf86/ on 20.07.15
 *
 * A swipe to delete compatible adapter
 */
public interface SwipeToDeleteListener {

    /**
     * get all adapter data
     * @return
     */
    List<?> getData();

    /**
     * add data at index to adapter
     * @param index
     * @param data
     * @return
     */
    boolean addData(int index, @NonNull Object data);

    boolean removeData(int index, @NonNull Object data);

    /**
     * adapt the list height to match the new number of list items
     * @param context
     * @param view
     */
//    void adaptHeight(@NonNull Context context, @NonNull RecyclerView view);
}