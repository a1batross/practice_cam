package su.xash.practice.cam;

import android.widget.*;
import android.graphics.*;
import android.content.Context;
import android.view.*;
import android.util.AttributeSet;

public class CrossView extends View
{
	private Paint m_White = null;
	private Paint m_Black = null;
	private ColorMatrix m_ColorMatrix = null;
	int m_X, m_Y;

	public CrossView( Context ctx )
	{
		super( ctx );
		init();
	}
	
	public CrossView( Context ctx, AttributeSet attrs )
	{
		super( ctx, attrs );
		init();
	}
	
	public CrossView( Context ctx, AttributeSet attrs, int defStyle )
	{
		super( ctx, attrs, defStyle );
		init();
	}
	
	public CrossView( Context ctx, AttributeSet attrs, int defStyleAttr, int defStyleRes )
	{
		super( ctx, attrs, defStyleAttr, defStyleRes );
		init();
	}
	
	private void init()
	{
		m_White = new Paint();
		m_Black = new Paint();
		
		m_White.setColor( 0xFFFFFFFF );
		m_Black.setColor( 0xFF000000 );
		
	}
	
	@Override
	public void onDraw( Canvas canvas )
	{
		super.onDraw( canvas );
		
		int w = getWidth();
		int h = getHeight();
		
		canvas.drawLine( 0, m_Y - 1, w, m_Y - 1, m_Black );
		canvas.drawLine( 0, m_Y, w, m_Y, m_White );
		canvas.drawLine( 0, m_Y + 1, w, m_Y + 1, m_Black );
		
		canvas.drawLine( m_X - 1, 0, m_X - 1, h, m_Black );
		canvas.drawLine( m_X, 0, m_X, h, m_White );
		canvas.drawLine( m_X + 1, 0, m_X + 1, h, m_Black );
	}
	
	public void setCrossPosition( int x, int y )
	{
		m_X = x;
		m_Y = y;
		invalidate();
	}

}
