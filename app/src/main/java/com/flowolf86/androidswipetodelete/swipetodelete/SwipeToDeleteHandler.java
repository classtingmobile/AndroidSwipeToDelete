package com.flowolf86.androidswipetodelete.swipetodelete;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.flowolf86.androidswipetodelete.util.GuiUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by github.com/flowolf86/ on 20.07.15
 *
 * A handler handling swipe to delete UNDO actions. This is not trivial
 * because of the ability to swipe multiple list elements and restore them
 * according to their original position in the recyclerview
 */
public class SwipeToDeleteHandler {

    private Snackbar mCurrentActiveSnackbar = null;
    private View mRootView = null;
    private String mUndoMessage = null;
    private String mUndoButtonText = null;
    private UndoCallback mCallback = null;
    private boolean mSingleDelete = false;
    // Required stuff
    private RecyclerView.Adapter mAdapter = null;

    /**
     * The queue with trips that have to be deleted if the user swipes more than one trip while the other can still be undone
     *
     * key = position in list
     * value = data of list item at position key
     *
     */
    private ConcurrentSkipListMap<Integer, Object> mDeleteQueue = new ConcurrentSkipListMap<>();
    private ConcurrentSkipListMap<Integer, Object> mRestoreQueue = new ConcurrentSkipListMap<>();

    public SwipeToDeleteHandler(@NonNull View view, @NonNull RecyclerView.Adapter adapter, @NonNull UndoCallback callback,
                                @NonNull boolean sigleDelete, @NonNull String undoMessage, @NonNull String undoButtonText) {
        mRootView = view;
        mAdapter = adapter;
        mCallback = callback;
        mSingleDelete = sigleDelete;
        mUndoMessage = undoMessage;
        mUndoButtonText = undoButtonText;
    }

    public void deleteData(final int currentPosition, final Object value){

        // We can not use the current position as key in the list here
        // because a new item may get the same position after the swipe
        // e.g. position 0. Therefore we have to use the key from the
        // restore map to store as key in the delete queue too

        int restoreIndex = mSingleDelete? 0 : getInitialListPosition(value);
        mDeleteQueue.put(restoreIndex, value);

        notifyUser(mRootView);
        setUpRestoreQueue(((SwipeToDeleteListener) mAdapter).getData());
        ((SwipeToDeleteListener) mAdapter).removeData(currentPosition, value);
//        ((SwipeToDeleteListener) mAdapter).adaptHeight(mContext, mRecyclerView);
    }

    public void undo() {
        // Restore the elements in reverse order
        for (Map.Entry<Integer, Object> entry : mDeleteQueue.entrySet()) {
            int insertIndex = getInitialListPosition(entry.getValue());
            ((SwipeToDeleteListener) mAdapter).addData(insertIndex, entry.getValue());
            setUpRestoreQueue(((SwipeToDeleteListener) mAdapter).getData());
        }
//                            ((SwipeToDeleteListener) mAdapter).adaptHeight(mContext, mRecyclerView);
        setUpDeleteQueue();
        mCurrentActiveSnackbar = null;
    }

    public void setup(@Nullable List<?> data){
        setUpDeleteQueue();
        setUpRestoreQueue(data);
    }

    public void reset(@Nullable List<?> data){
        setup(data);
        if(mCurrentActiveSnackbar != null){
            mCurrentActiveSnackbar.dismiss();
            mCurrentActiveSnackbar = null;
        }
    }

    public void finish(){
        // Hide snackbar so that it does not show up again if we quickly navigate to the fragment again
        if(mCurrentActiveSnackbar != null){
            mCurrentActiveSnackbar.dismiss();
            mCurrentActiveSnackbar = null;
        }
    }

    /**
     * This is an exact copy of the recyclerviews data with the exception of
     * having the list position as int key stored linked to the data.
     *
     * We need this for restoring the list elements later if the user wants
     * to undo his remove action. After a swipe of one element the others all
     * change position in list leading to potentially duplicated keys and the
     * disability to restore trip properly at their previous position
     */
    private void setUpRestoreQueue(@Nullable final List<?> data) {

        mRestoreQueue.clear();

        if(data != null){
            int i = 0;
            for(Object object : data){
                mRestoreQueue.put(i, object);
                i++;
            }
        }
    }

    /**
     * A simple delete queue setup is just clearing the list
     */
    private void setUpDeleteQueue() {
        mDeleteQueue.clear();
    }

    /**
     * Display a snackbar to the user and give him the chance to undo his actions
     *
     * @param view
     */
    private void notifyUser(final View view) {

        // Check if we already have an active snackbar. If yes, extend the duration, if no, show new one
        if(mCurrentActiveSnackbar == null){

            mCurrentActiveSnackbar = GuiUtils.displaySnackbar(view,
                    mUndoMessage,
                    mUndoButtonText,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            for (Map.Entry<Integer, Object> entry : mDeleteQueue.entrySet()) {
                                int insertIndex = getInitialListPosition(entry.getValue());
                                if (mCallback != null) {
                                    mCallback.undo(insertIndex);
                                }
                            }
                        }
                    },
                    new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            super.onDismissed(snackbar, event);
                            setUpRestoreQueue(((SwipeToDeleteListener) mAdapter).getData());
                            setUpDeleteQueue();
                            mCurrentActiveSnackbar = null;
                        }
                    }, GuiUtils.SNACKBAR_NO_DELAY, Snackbar.LENGTH_INDEFINITE);
        } else {
            mCurrentActiveSnackbar.setText(mUndoMessage);
        }
    }

    private int getInitialListPosition(final Object value) {

        int initialIndex = Integer.MAX_VALUE;

        for(Map.Entry<Integer, Object> entry : mRestoreQueue.entrySet()){

            if(value.equals(entry.getValue())){
                initialIndex = entry.getKey();
                break;
            }
        }

        return initialIndex;
    }
}
