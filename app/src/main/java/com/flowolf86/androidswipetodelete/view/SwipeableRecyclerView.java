package com.flowolf86.androidswipetodelete.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.view.View;

import com.flowolf86.androidswipetodelete.R;

/**
 * Created by github.com/flowolf86/ on 02.08.15
 *
 * Custom RecyclerView with additional swipe to delete and undo functionality
 */
public class SwipeableRecyclerView extends RecyclerView
{
    private View mEmptyView;
    protected static final int DEFAULT_DELETE_DRAWABLE = R.drawable.delete_background;
    protected int mDeleteDrawable = DEFAULT_DELETE_DRAWABLE;

    public interface RecyclerViewSwipeListener {
        void onSwipe(int position);
        Boolean isSwipeableItem(int position);
    }

    public SwipeableRecyclerView(Context context) {
        super(context);
        init(null, 0);
    }

    public SwipeableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(attrs, 0);
    }

    public SwipeableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    protected void init(AttributeSet attrs, int defStyle) {
        final TypedArray attrArray = getContext().obtainStyledAttributes(attrs, R.styleable.SwipeableRecyclerView, defStyle, 0);
        mDeleteDrawable = attrArray.getResourceId(R.styleable.SwipeableRecyclerView_deleteDrawable, DEFAULT_DELETE_DRAWABLE);

        attrArray.recycle();
    }

    final private AdapterDataObserver observer = new AdapterDataObserver() {

        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

    private void checkIfEmpty() {

        if (mEmptyView != null && getAdapter() != null) {

            final boolean emptyViewVisible = getAdapter().getItemCount() == 0;
            mEmptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
            setVisibility(emptyViewVisible ? GONE : VISIBLE);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }
        checkIfEmpty();
    }

    public void setEmptyView(View emptyView) {

        this.mEmptyView = emptyView;
        checkIfEmpty();
    }

    /**
     * Glue the swipelistener to the recyclerview. Only ViewHolders that implement the SwipeableViewholder interface, can be swiped.
     * after the swipe has been performed, the callback onSwipe() is being invoked
     */
    public void setSwipeListener(final RecyclerViewSwipeListener listener)
    {
        // init swipe to dismiss logic
        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.LEFT, ItemTouchHelper.LEFT) {

            private NinePatchDrawable drawable = (NinePatchDrawable) ContextCompat.getDrawable(getContext(), mDeleteDrawable);

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false; //no drag-n-drop
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                listener.onSwipe(viewHolder.getAdapterPosition());
            }

            @Override
            public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (!listener.isSwipeableItem(viewHolder.getAdapterPosition())) {
                    return;
                }

                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                    // Get RecyclerView item from the ViewHolder
                    View itemView = viewHolder.itemView;

                    //x offset // dX is negative value
                    int xToDraw = (int) (itemView.getRight() + dX);

                    //view, which is moved
                    Rect outer = new Rect(xToDraw, itemView.getTop(), itemView.getRight(), itemView.getBottom());

                    drawable.setBounds(outer);
                    drawable.draw(canvas);
                }
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        });
        swipeToDismissTouchHelper.attachToRecyclerView(this);
    }
}
