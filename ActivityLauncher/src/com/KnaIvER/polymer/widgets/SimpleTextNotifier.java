package com.KnaIvER.polymer.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.widget.TextView;

public class SimpleTextNotifier extends TextView {
	public int offset=-1000;
	public SimpleTextNotifier(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	@SuppressLint("MissingSuperCall")
	@Override
	public void draw(Canvas canvas) {
		//canvas.translate(0, offset);
		super.draw(canvas);
	}
}
