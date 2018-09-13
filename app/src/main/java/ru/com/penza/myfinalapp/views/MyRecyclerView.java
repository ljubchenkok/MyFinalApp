package ru.com.penza.myfinalapp.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;


public class MyRecyclerView extends RecyclerView {
    private float speedFactor = 1f;
    private static final int VISIBLE_THRESHOLD = 10;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public void setSpeedFactor(float speedFactor) {
        this.speedFactor = speedFactor;
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public MyRecyclerView(Context context, OnLoadMoreListener onLoadMoreListener) {
        super(context);

    }

    public void setLoaded() {
        loading = false;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        return super.fling(velocityX, Float.floatToIntBits(velocityY * speedFactor));
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        totalItemCount = getLayoutManager().getItemCount();
        lastVisibleItem = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
       if (!loading && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD)) {
            if (onLoadMoreListener != null) {
                onLoadMoreListener.onLoadMore(totalItemCount);
                loading = true;
            }

        }
    }
}
