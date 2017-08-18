package su.xash.practice.cam;

import android.widget.*;
import android.graphics.*;
import android.content.Context;
import android.view.*;
import android.util.AttributeSet;

public class PixelView extends View
{
	private int m_Color;
	private Paint m_Paint = null;
	
	public PixelView( Context ctx )
	{
		super( ctx );
		init();
	}
	
	public PixelView( Context ctx, AttributeSet attrs )
	{
		super( ctx, attrs );
		init();
	}
	
	public PixelView( Context ctx, AttributeSet attrs, int defStyle )
	{
		super( ctx, attrs, defStyle );
		init();
	}
	
	public PixelView( Context ctx, AttributeSet attrs, int defStyleAttr, int defStyleRes )
	{
		super( ctx, attrs, defStyleAttr, defStyleRes );
		init();
	}
	
	private void init()
	{
		m_Paint = new Paint();
	}
	
	@Override
	public void onDraw( Canvas canvas )
	{
		super.onDraw( canvas );
		
		m_Paint.setColor( m_Color );
		
		canvas.drawRect( getSelfRect(), m_Paint );
	}
	
	Rect getSelfRect()
	{
		int[] pos = new int[2];
		getLocationOnScreen( pos );
		return new Rect( 0, 0, getWidth(), getHeight() );
	}
	
	void setColor( int color )
	{
		m_Color = color;
		invalidate();
	}
}
