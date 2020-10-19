package com.knziha.polymer.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

public class UnCrashableSpannedTextView extends TextView {  
    public UnCrashableSpannedTextView(Context context) {  
        super(context);  
        init(context);  
    }  
  
    public UnCrashableSpannedTextView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        init(context);  
  
    }  
  
    public UnCrashableSpannedTextView(Context context, AttributeSet attrs, int defStyleAttr) {  
        super(context, attrs, defStyleAttr);  
        init(context);  
    }  
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public UnCrashableSpannedTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	private Context mContext;  
  

    private void init(Context context) {  
        mContext = context; 
    } 
    

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        /*/ FIXME simple workaround to https://code.google.com/p/android/issues/detail?id=191430
        int startSelection = getSelectionStart();
        int endSelection = getSelectionEnd();
        if (startSelection != endSelection) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                final CharSequence text = getText();
                setText(null);
                setText(text);
            }
        }
        return super.dispatchTouchEvent(event);*/
    	try {
    		 return super.dispatchTouchEvent(event);
    	}catch(Exception e) {
    		e.printStackTrace();
    		//CMN.show("fuck dispatchTouchEvent");
    	}
    	return false;
    }
   
    @Override
	    public boolean onPreDraw() {
	    	try {
	   		   return super.onPreDraw();
		   	}catch(Exception e) {
		   		e.printStackTrace();
		   		//CMN.show("fuck onPreDraw");
		   	}
        return false;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
    	try {
	   		   super.onDraw(canvas);
		   	}catch(Exception e) {
		   		e.printStackTrace();
		   	}
    }
    
}  