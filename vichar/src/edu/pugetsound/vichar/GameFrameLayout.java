package edu.pugetsound.vichar;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Fame layout which adds extra space to right side of screen
 * @author Nathan P
 *
 */
public class GameFrameLayout extends FrameLayout {
	 	private Context appContext;

	    public GameFrameLayout(Context context, AttributeSet attrs) {
	        super(context, attrs);
	        appContext = context;
	    }

	    @Override
	    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)    {
	        super.onMeasure(widthMeasureSpec+(int)appContext.getResources().getDimension(R.dimen.gameframelayout_offset), 
	        					heightMeasureSpec);
	    }
}
