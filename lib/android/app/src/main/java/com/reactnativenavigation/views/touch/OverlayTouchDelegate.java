package com.reactnativenavigation.views.touch;

import android.graphics.Rect;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.reactnativenavigation.parse.params.Bool;
import com.reactnativenavigation.parse.params.NullBool;
import com.reactnativenavigation.utils.UiUtils;
import com.reactnativenavigation.viewcontrollers.IReactView;

public class OverlayTouchDelegate {
    private static final String TAG = "OverlayTouchDelegate";

    private enum TouchLocation {Outside, Inside}
    private final Rect hitRect = new Rect();
    private IReactView reactView;
    private Bool interceptTouchOutside = new NullBool();

    public OverlayTouchDelegate(IReactView reactView) {
        this.reactView = reactView;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (interceptTouchOutside instanceof NullBool) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return handleDown(event);
            default:
                reactView.dispatchTouchEventToJs(event);
                return false;

        }
    }

    @VisibleForTesting
    public boolean handleDown(MotionEvent event) {
        TouchLocation location = getTouchLocation(event);
        if (location == null) {
            Log.w(TAG, "could not get a touch location");
            return false;
        }
        if (location == TouchLocation.Inside) {
            reactView.dispatchTouchEventToJs(event);
        }
        if (interceptTouchOutside.isTrue()) {
            return location == TouchLocation.Inside;
        }
        return location == TouchLocation.Outside;
    }

    private TouchLocation getTouchLocation(MotionEvent ev) {
        View child = ((ViewGroup) reactView.asView()).getChildAt(0);
        if (child == null) {
            return null;
        }
        child.getHitRect(hitRect);
        return hitRect.contains((int) ev.getRawX(), (int) ev.getRawY() - UiUtils.getStatusBarHeight(reactView.asView().getContext())) ?
                TouchLocation.Inside :
                TouchLocation.Outside;
    }

    public void setInterceptTouchOutside(Bool interceptTouchOutside) {
        this.interceptTouchOutside = interceptTouchOutside;
    }
}
