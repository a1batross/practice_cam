package su.xash.practice.cam;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.*; // API 21
import android.hardware.camera2.params.*; // API 21
import android.widget.*;
import android.view.*;
import android.graphics.*;
import android.os.*;
import android.util.Size;
import android.util.Log;
import android.content.pm.PackageManager;
import android.Manifest;
import android.support.v4.app.ActivityCompat;
import java.util.Arrays;
import su.xash.practice.cam.PixelView;
import su.xash.practice.cam.CrossView;
import su.xash.practice.cam.R;

public class CamPracticeActivity extends Activity implements TextureView.SurfaceTextureListener
{
	private static final String TAG = "CamPracticeActivity";
	public static final int sdk = Integer.valueOf(Build.VERSION.SDK);
	
	private static final int REQUEST_CAMERA_PERMISSION = 0;
	
	private CameraDevice  m_CamDevice;
	
	private TextureView  m_CamView;
	private CrossView    m_CamCrossView;
	private TextView     m_Resolution;
	private NumberPicker m_XPos;
	private NumberPicker m_YPos;
	private PixelView    m_PixelView;
	private TextView     m_PixelInfo; 
	private CaptureRequest.Builder m_CaptureRequestBuilder;
	protected CameraCaptureSession m_CameraCaptureSessions;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

	private Size m_ImageSize;
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		
		Log.e( TAG, "onCreate()" );
		
		setContentView( R.layout.campractice );
		
		m_CamView = (TextureView) findViewById( R.id.camview );
		m_CamCrossView = (CrossView) findViewById( R.id.crossoverlay );
		m_Resolution = (TextView) findViewById( R.id.resolution );
		m_XPos = (NumberPicker)   findViewById( R.id.xpos );
		m_YPos = (NumberPicker)   findViewById( R.id.ypos );
		m_PixelView = (PixelView) findViewById( R.id.pixelview );
		m_PixelInfo = (TextView)  findViewById( R.id.pixelinfo );
		
		m_CamView.setSurfaceTextureListener( this );
	}
	
	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) 
	{
		openCamera();
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) 
	{
		RectF viewRect = new RectF( 0, 0, width, height );
		float centerX = viewRect.centerX(), centerY = viewRect.centerY();
		Matrix matrix = new Matrix();

		int rotation = getWindowManager().getDefaultDisplay().getRotation();
		switch( rotation )
		{
		// camera is rotated in landscape mode
		case Surface.ROTATION_90:
		case Surface.ROTATION_270:
		{
			RectF bufferRect = new RectF( 0, 0, m_ImageSize.getHeight(), m_ImageSize.getWidth() );
		
			bufferRect.offset( centerX - bufferRect.centerX(), centerY - bufferRect.centerY() );
			matrix.setRectToRect( viewRect, bufferRect, Matrix.ScaleToFit.FILL );
			
			float scale = Math.max( (float) height / m_ImageSize.getHeight(), (float) width / m_ImageSize.getWidth());
			matrix.postScale( scale, scale, centerX, centerY );
			
			matrix.postRotate( 90 * (rotation - 2), centerX, centerY );
			break;
		}
		// just rotate by 180
		case Surface.ROTATION_180:
			matrix.postRotate(180, centerX, centerY);
			break;
		default: break;
		}
		
		m_CamView.setTransform(matrix);
	}
	
	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) 
	{
		return false;
	}
	
	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) 
	{
		int x = m_XPos.getValue();
		int y = m_YPos.getValue();
		int color = m_CamView.getBitmap().getPixel( x, y );
		m_PixelView.setColor( color );
		m_PixelInfo.setText( "R: " + ((color >> 16) & 255) + "\nG: " + ((color >> 8) & 255) + "\nB: " + (color & 255) );
		m_CamCrossView.setCrossPosition( x, y );
	}
	
	private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback( ) 
	{
		@Override
		public void onOpened( CameraDevice camera ) 
		{
			//This is called when the camera is open
			Log.d(TAG, "onOpened");
			m_CamDevice = camera;
			createCameraPreview();
		}
		@Override
		public void onDisconnected( CameraDevice camera ) 
		{
			if( m_CamDevice != null )
				m_CamDevice.close();
		}
		@Override
		public void onError( CameraDevice camera, int error ) 
		{
			Log.e(TAG, "Camera error: " + error );
			if( m_CamDevice != null )
			{
				m_CamDevice.close();
				m_CamDevice = null;
			}
		}
	};

	protected void openCamera( )
	{
		Log.d( TAG, "openCamera()" );
		try 
		{
			CameraManager manager = ( CameraManager )getSystemService( Context.CAMERA_SERVICE );
			String cameraId = manager.getCameraIdList()[0];
			CameraCharacteristics characteristics = manager.getCameraCharacteristics( cameraId );
			StreamConfigurationMap map = characteristics.get( CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP );
			m_ImageSize = map.getOutputSizes( SurfaceTexture.class )[0];
			
			int previewWidth, previewHeight;
			int rotation = getWindowManager().getDefaultDisplay().getRotation();
			
			if( rotation == Surface.ROTATION_90 || 
				rotation == Surface.ROTATION_270 )
			{
				previewWidth = m_ImageSize.getWidth();
				previewHeight = m_ImageSize.getHeight();
			}
			else
			{
				previewWidth = m_ImageSize.getHeight();
				previewHeight = m_ImageSize.getWidth();
			}
			
			// recalc width and height to fit in current window
			int height = (int)((float)getWindow().getDecorView().getHeight() / 2.0f);
			int width = (int)((float)previewWidth * ((float)(height) / (float)previewHeight ));
			
			// set resolution info
			m_Resolution.setText("Resolution: " + width + "x" + height );
			
			// set numberpicker maximum range(0 is default)
			m_XPos.setMaxValue( width - 1 );
			m_YPos.setMaxValue( height - 1 );
			
			m_XPos.setValue( width / 2 );
			m_YPos.setValue( height / 2 );

			// set textureview params
			RelativeLayout.LayoutParams camParams = new RelativeLayout.LayoutParams( width, height );
			m_CamView.setLayoutParams( camParams );
			
			// set overlay params
			RelativeLayout.LayoutParams crossOverlayParams = new RelativeLayout.LayoutParams( width, height );
			crossOverlayParams.setMargins( 0, 0, -width, -height );
			m_CamCrossView.setLayoutParams( crossOverlayParams );
			
			// request permissions for >=6.0
			if (ActivityCompat.checkSelfPermission( this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED ) 
			{
				Log.e( TAG, "requesting permissions..." );
				ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION );
				return;
			}
			
			// itself camera opening
			manager.openCamera( cameraId, stateCallback, null );
		} 
		catch( CameraAccessException e ) 
		{
			e.printStackTrace( );
		}
	}
	
	private void createCameraPreview() 
	{
		try 
		{
			SurfaceTexture texture = m_CamView.getSurfaceTexture();
			texture.setDefaultBufferSize( m_ImageSize.getWidth(), m_ImageSize.getHeight() );
			
			Surface surface = new Surface( texture );
			m_CaptureRequestBuilder = m_CamDevice.createCaptureRequest( CameraDevice.TEMPLATE_PREVIEW );
			m_CaptureRequestBuilder.addTarget( surface );
			
			m_CamDevice.createCaptureSession( Arrays.asList(surface), 
			new CameraCaptureSession.StateCallback()
			{
				@Override
				public void onConfigured( CameraCaptureSession cameraCaptureSession ) 
				{
					// camera is already closed
					if( null == m_CamDevice )
					{
						return;
					}
					
					// when the session is ready, we start displaying the preview.
					m_CameraCaptureSessions = cameraCaptureSession;
					updatePreview();
				}
				
				@Override
				public void onConfigureFailed( CameraCaptureSession cameraCaptureSession ) 
				{
					Log.d( TAG, "onConfigureFailed()" );
				}
			}, null);
		} 
		catch( CameraAccessException e )
		{
			e.printStackTrace();
		}
    }
    
	private void updatePreview() 
	{
		m_CaptureRequestBuilder.set( CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO );
		try 
		{
			m_CameraCaptureSessions.setRepeatingRequest( m_CaptureRequestBuilder.build(), null, mBackgroundHandler );
		} 
		catch( CameraAccessException e ) 
		{
			e.printStackTrace();
		}
	}
	
	protected void startBackgroundThread() 
	{
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() 
    {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
	
    @Override
	public void onRequestPermissionsResult( int requestCode,  String[] permissions,  int[] grantResults ) 
	{
		if( requestCode == REQUEST_CAMERA_PERMISSION ) 
		{
			if( grantResults[0] == PackageManager.PERMISSION_DENIED ) 
			{
				Toast.makeText( CamPracticeActivity.this, R.string.no_camera_permission, Toast.LENGTH_LONG ).show();
				finish();
			}
			else
			{
				// open again?
			}
		}
	}

}
