/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0,
 * with full text available at http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This software is provided "as is". Use at your own risk.
 */
package ru.kolotnev.codoma;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class ClipboardPanel extends Panel {
	private final boolean mFlushedHandle; //whether the handle should be wrapped to the contents
	private final GestureDetector _gestureDetector;

	public ClipboardPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ClipboardPanel);
		mFlushedHandle = a.getBoolean(R.styleable.ClipboardPanel_flushedHandle, false);
		a.recycle();

		GestureDetector.SimpleOnGestureListener _contentGestureListener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				int orientation = getPosition();
				float MIN_FLICK_VELOCITY = 150;
				if (isOpen() && ((orientation == Panel.TOP && velocityY < -MIN_FLICK_VELOCITY) ||
						(orientation == Panel.BOTTOM && velocityY > MIN_FLICK_VELOCITY) ||
						(orientation == Panel.LEFT && velocityX < -MIN_FLICK_VELOCITY) ||
						(orientation == Panel.RIGHT && velocityX > MIN_FLICK_VELOCITY))) {
					setOpen(false, true);
				}
				return true;
			}
		};
		_gestureDetector = new GestureDetector(context, _contentGestureListener);
		_gestureDetector.setIsLongpressEnabled(false);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mFlushedHandle && !isInEditMode()) {
			// Calculate the dimensions of the wrapped panel contents.
			// If this is a vertical panel, restrict the handle width to the content width;
			// If this is a horizontal panel, restrict the handle height to the content height;
			View content = getContent();
			content.measure(widthMeasureSpec, heightMeasureSpec);
			if (getPosition() == Panel.TOP || getPosition() == Panel.BOTTOM) {
				widthMeasureSpec = MeasureSpec.makeMeasureSpec(
						content.getMeasuredWidth(), MeasureSpec.EXACTLY);
			} else {
				heightMeasureSpec = MeasureSpec.makeMeasureSpec(
						content.getMeasuredHeight(), MeasureSpec.EXACTLY);
			}
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
		_gestureDetector.onTouchEvent(event);
		return super.dispatchTouchEvent(event);
	}

}
