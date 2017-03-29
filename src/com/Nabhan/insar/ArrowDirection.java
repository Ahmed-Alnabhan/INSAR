package com.Nabhan.insar;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ArrowDirection extends ImageView{
    //---- Declarations ----//
	float theAngle;
	Paint paint;
	
	public ArrowDirection(Context context) {
		super(context);
		initialazation();
	}
	
	public ArrowDirection(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialazation();
	}
	
	private void initialazation(){
		this.setImageResource(R.drawable.arrow);
		paint = new Paint();
		//LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(100, 100);
		//this.setLayoutParams(layoutParams);
		//this.getLayoutParams().height = 50;
	}
	public void findOreintation(float orientation) {
		theAngle = orientation;
		postInvalidate();
	}

	@Override
	  public void onDraw(Canvas canvas) {
	    int height = getHeight();
	    int width = getWidth();
	    float xCoords = width / 2;
	    float yCoords = height / 2;
	    canvas.rotate(theAngle, xCoords, yCoords );
	    super.onDraw(canvas);
	  }


}
