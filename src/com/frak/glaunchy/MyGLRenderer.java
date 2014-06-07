package com.frak.glaunchy;
//Copyright (C) 2014 fractallonomy. All Rights Reserved.
//    ____ _     ____                _                    
//   / ___| |   |  _ \ ___ _ __   __| | ___ _ __ ___ _ __ 
//  | |  _| |   | |_) / _ \ '_ \ / _` |/ _ \ '__/ _ \ '__|
//  | |_| | |___|  _ <  __/ | | | (_| |  __/ | |  __/ |   
//   \____|_____|_| \_\___|_| |_|\__,_|\___|_|  \___|_|   
//LaunchyGL: OpenGL App Launcher.  Does NOT rely on Glass' ever-changing API
//           Only uses touch Gestural input, and sends out Activity startup calls...
//Stoopid but effective....
//  All done with OpenGL 1.0, 2.0 Was shitty when it came to portability and use with gestures!
// June 5: OK, works first time, but second time we get an empty App list!
//        

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.frak.glaunchy.common.TextureHelper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.SystemClock;
import android.util.Log;

public class MyGLRenderer implements GLSurfaceView.Renderer {

	private String TAG = "LGLRender";
	 
	final int[] textureHandle = new int[1];
	//private float angleInDegrees = 0.0f;

	private Context mContext;
	private float mSceneRotation;
	private long mLastUpdateMillis;
	private int mSceneChoice=-1;
	private int mTapCount = 0;
	private int needToAlign  = 0;
	private int needToZoom   = 0;
	private int needToLaunch = 0;
    private int lockedIn     = 0;

    Resources res;

    private final int MAXWOIDS = 24;
    private int numappsfound = MAXWOIDS;
    private String[] dawoids = 
    	{
    		"Glass Settings",
    		"1 Empty",
    		"2 Empty",
    		"3 Empty",
    		"4 Empty",
    		"5 Empty",
    		"6 Empty",
    		"7 Empty",
    		"8 Empty",
    		"9 Empty",
    		"10 Empty",
    		"11 Empty",
    		"12 Empty",
    		"13 Empty",
    		"14 Empty",
    		"15 Empty",
    		"16 Empty",
    		"17 Empty",
    		"18 Empty",
    		"19 Empty",
    		"20 Empty",
    		"21 Empty",
    		"22 Empty",
    		"23 Empty"
    	};
    
	
    //Basic generic texture maps and vertex arrays for simple polys....	
	float[] generic = new float[] { 
			-1f, -1f, 0.0f,
			 1f, -1f, 0.0f,
            -1f,  1f, 0.0f,
             1f,  1f, 0.0f }; 
	float[] genericTexcoords = new float[] { 
			0, 0,
			1, 0,
			0, 1,
			1, 1,
		};	// used drawing cube
	float[] square = new float[] { 
			 1f, -1f, 1.0f,
			-1f, -1f, 1.0f,
             1f,  1f, 1.0f,
            -1f,  1f, 1.0f }; 
	float[] topBar = new float[] { 
			-1f,  0f,
			 1f,  0f,
            -1f,  1f,
             1f,  1f }; 
	float[] topSplash = new float[] { 
			-1.3f,   .8f,
			 1.3f,   .8f,
            -1.3f,  -.8f,
             1.3f,  -.8f }; 
	float[] glyphVertices = new float[]  {
			-0.05f,  0.05f,  
			 0.05f,  0.05f,
			-0.05f, -0.05f,
			 0.05f, -0.05f,
		};
	float[] glyphTexcoords = new float[]  {
			0.02f,  0.1f,
			0.044f, 0.1f,
			0.02f,  0.9f,
			0.044f, 0.9f,
		};
	float[] colorBoxVertices = new float[]{
			-0.09f,  0.09f,
			 0.09f,  0.09f,
			 0.09f, -0.09f,
			-0.09f, -0.09f,
		};
	
	float[] colorBoxVertices0 = new float[]{   //octagon (for circle cursor)
		     0.00f, -0.12f,
		     0.06f, -0.08f,
		     0.08f,  0.00f,
		     0.06f,  0.07f,
		     0.00f,  0.12f,
			-0.06f,  0.08f,
			-0.08f,  0.00f,
			-0.06f, -0.08f,
		};
	float[] colorBoxVertices1 = new float[]{  //square
			-0.08f,   0.12f,
		     0.085f,  0.12f,
		     0.085f, -0.12f,
			-0.08f,  -0.12f,
		};
	float[] colorBoxVertices2 = new float[]{  //diamond
		     0.00f, -0.143f,
		     0.10f,  0.00f,
		     0.00f,  0.143f,
			-0.10f,  0.00f,
		};
	float[] colorBoxVertices3 = new float[]{   //triangle
		     0.00f,  0.16f,
		     0.11f, -0.11f,
		    -0.11f, -0.11f,
		};
	float[] panelVertices = new float[] {   
			-0.45f, 0.76f,  // was .9...
		     0.45f, 0.76f,
			-0.45f, 0.68f,
		     0.45f, 0.68f,
		};
	float[] panelTexcoords = new float[] {   
			0, 0,
			1, 0,
			0, 0.6f,
			1, 0.6f,
		};
	float[] BPMVertices = new float[] {   
			-0.185f, 0.743f,   // was .9...
		    -0.130f, 0.743f,
			-0.185f, 0.701f,
		    -0.130f, 0.701f,
		};
	float[] BPMTexcoords = new float[] {   
			0.25f, 0,
			0.72f, 0,
			0.25f, 0.04f,
			0.72f, 0.04f,
		};
	float[] BPMWVertices = new float[] {   
			-0.450f, 0.755f,  //was .9...
		    -0.385f, 0.755f,
			-0.450f, 0.686f,
		    -0.385f, 0.686f,
		};
	float[] BPMWTexcoords = new float[] {   
			0.0f, 0,
			1.0f, 0,
			0.0f, 0.11f,
			1.0f, 0.11f,
		};
	float[] BeatVertices = new float[] {   
			-0.365f, 0.737f,    //was .9...
		    -0.225f, 0.737f,
			-0.365f, 0.698f,
		    -0.225f, 0.698f,
		};

	float[] RB1Vertices = new float[] {   
		    0.015f, 0.74f,    //was .9...
		    0.105f, 0.74f,
			0.015f, 0.697f,
		    0.105f, 0.697f,
		};
	float[] RB2Vertices = new float[] {   
		    0.12f, 0.74f,    //was .9...
		    0.21f, 0.74f,
			0.12f, 0.697f,
		    0.21f, 0.697f,
		};
	float[] LLevelVertices = new float[] {   
		    0.258f, 0.735f,    //was .9...
		    0.330f, 0.735f,
			0.258f, 0.705f,
		    0.330f, 0.705f,
		};
	float[] LLevel2Vertices = new float[] {   
		    0.258f, 0.735f,    //was .9...
		    0.330f, 0.735f,
			0.258f, 0.705f,
		    0.330f, 0.705f,
		};
	float[] RLevelVertices = new float[] {   
		    0.337f, 0.735f,    //was .9...
		    0.411f, 0.735f,
			0.337f, 0.705f,
		    0.411f, 0.705f,
		};
	float[] bkgd0sVertices = new float[] {   
			-0.80f,  -0.82f,
		     0.80f,  -0.82f,
			-0.80f,  -0.69f,
		     0.80f,  -0.69f,
		};

	float[] genericTex = new float[] {0f,0f,1f,0f,0f,1f,1f,1f};

	FloatBuffer squareBuff,genericVBuff,tbVBuff,splashBuff,colorBoxBuff;
	FloatBuffer colorBoxBuff0,colorBoxBuff1,colorBoxBuff2,colorBoxBuff3;
	
	FloatBuffer genericBuff;

	float zNear,zFar;
	
    /** The refresh rate, in frames per second. */
    private static final int REFRESH_RATE_FPS = 60;

    /** The duration, in milliseconds, of one frame. */
    private static final float FRAME_TIME_MILLIS = TimeUnit.SECONDS.toMillis(1) / REFRESH_RATE_FPS;

    private int gWidth=0,gHeight=0;
    private static final float STEP_ANGLE = 15.0f;
 
    private static float stepModulo = 0f;
    private static final float jiggleAngle = 2f;
    private static float sceneMinAngle = -15.0f,sceneMaxAngle = 375.0f;
    private static float sceneRotationSpeed     = 0.0f;
    //private static float sceneAlignmentSpeed    = 0.4f;
    private static float sceneRotationIncrement = 0.32f;
    private static float sceneChoiceAngle       = 0.0f;
	/** Maximum commands/textures/items to choose from */
	private final int maxCommands = 32;	
	private int mTextureDataHandles[] = new int[maxCommands];
    private int numTitles = 0;
	
	//=====Glaunchy=============================================2014
	public MyGLRenderer(Context context) 
	{
		//Log.d(TAG,"init...1");
      
	
		mContext = context;
		//Log.d(TAG,"init...2");

		processVectors();  //Nasty vector init...
		//Log.d(TAG,"init DONE");
	}
	
	static int fcounter=0;
	//=====Glaunchy=============================================2014
	@Override
	public void onDrawFrame(GL10 gl) 
	{
		int nearestChoice = 0;
        fcounter++;
        if (fcounter % 600 == 0) Log.d(TAG,"...OGL Heartbeat:" + fcounter + " rot:"+ mSceneRotation);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glEnable(GL10.GL_DEPTH_TEST);
	    gl.glLoadIdentity();

	 	gl.glMatrixMode(GL10.GL_MODELVIEW); 			
	    gl.glLoadIdentity();

		//Clear, get ready to draw our stuff...

	    if (needToZoom == 0) //normal carousel display...
	    	gl.glClearColor(0.02f,0.0f,0.04f,0.0f);
	    else                 //item selected: zoomout display...
	    	gl.glClearColor(0.0f,0.0f,0.02f,0.0f);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glClear(GL10.GL_DEPTH_BUFFER_BIT);

		gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glPushMatrix();
		//Log.d(TAG,"drawit...1");

        //float tranz = -3.0f +  (mSceneRotation%360) / 60.0f;
        gl.glTranslatef(0f,0f,1.4f);  //Was 1.2 at start, Panels were almost 1/2 screen across Larger vals zoom IN 3 too large
        //Log.d(TAG,"duh tranz " + tranz);
       // gl.glRotatef(15.0f,1.0f,0.0f,0.0f);
        
        

        //Overall rotation of our little circle of panels...
        gl.glRotatef(mSceneRotation,1.0f,0.0f,0.0f);
        nearestChoice = (int)(mSceneRotation/STEP_ANGLE);
       float fangle = 0f;
       if (needToZoom == 0) //normal carousel display...
        for(int loop=0;loop<numappsfound;loop++)
        {
    	   gl.glPushMatrix();
        	fangle = (float)loop * -1 * STEP_ANGLE;
            gl.glRotatef(fangle,1.0f,0.0f,0.0f);
    		gl.glTranslatef(0f,0f,-1.3f);
            // Draw our objects.... 
            if (nearestChoice == loop) 
            	gl.glColor4f (.9f,.9f,.0f,1f);
            else
            	gl.glColor4f (.8f,1f,1f,1f);
            	
            drawWordPanel(gl,loop);
            //Log.d(TAG," loop " + loop + "nc:" + nearestChoice);
            //if (nearestChoice == loop) 
            //	drawWordHighlight(gl);
            gl.glPopMatrix();
        }
       else if (needToZoom >= 1)
       {
    	   //OOPS. Here we need the sceneChoice nearest rotation angle???
    	   int loop = mSceneChoice;
    	   gl.glPushMatrix();
            //Let's zoom out straight? Negate any paddlewheel rotation...
    	    gl.glRotatef(-5f,1.0f,0.0f,0.0f);
       		fangle = -1.0f * mSceneRotation;  
            gl.glRotatef(fangle,1.0f,0.0f,0.0f);
            //This sets how fast the selected launch command zooms out... IMPORTANT!
            float sf = 1.0f * 0.12f*(float)needToZoom;
            gl.glScalef(sf, sf, sf);
            gl.glTranslatef(0f,0f,-1.3f);
            // Draw our objects....        
            drawWordPanel(gl,loop);
           gl.glPopMatrix();
       }
       gl.glPopMatrix();

	   gl.glDisable(GL10.GL_TEXTURE_2D);

	   updateSceneRotation();
		//Log.d(TAG,"drawit...4");
		
	}

	//=====Glaunchy=============================================2014
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) 
	{
		//Log.d(TAG," surfaceChanged...");
		gWidth  = width;
		gHeight = height;
		//Handle weirdness...
		if (gHeight < 1) gHeight = 1;
		zNear = 0.2f;
		zFar  = 55.0f;
        gl.glViewport(0, 0, gWidth, gHeight);
        float ratio = (float) gWidth / gHeight;
    	gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();

        GLU.gluPerspective(gl, 60.0f, ratio, 0.1f, 1000.0f);
        
        gl.glShadeModel(GL10.GL_SMOOTH);	
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);  
        // Enable blending : This makes our bitmap tiles TRANSPARENT!!
        //gl.glBlendFunc(GL10.GL_ONE, GL10.GL_ONE_MINUS_SRC_ALPHA);
        //gl.glEnable(GL10.GL_BLEND);
		
	}

	//=====Glaunchy=============================================2014
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) 
	{

		//Log.d(TAG,"onSurfaceCreated...");
		// get thread  Thread.currentThread() 
        // Load the textures from our app names...
        for(int loop=0;loop<numappsfound;loop++) 
        {
            Bitmap wbmp = loadTextBitmap(28,52,dawoids[loop]);  //Keep it under 36 chars! Maxlen/Size/String
            mTextureDataHandles[loop] = TextureHelper.loadTextureFromBitmap(mContext, wbmp);
    		wbmp.recycle();						
        }
        //mCube = new Cube();
		//Log.d(TAG,"bottom  onSurfaceCreated...");
		
	}
	

	//=====Glaunchy=============================================2014
	public void clearSelection()
	{
		lockedIn = needToAlign = needToZoom = needToLaunch = mTapCount = 0; //clear any user selection
		mSceneChoice = -1;
	}
	
	//=====Glaunchy=============================================2014
	// returns one plus the desired app selected...
    public int checkForSelection()
    {
    	 //Log.d(TAG,"  check for selection:" + (1 + mSceneChoice));
         if (needToLaunch != 0) return (1 + mSceneChoice);
         else return 0;
    }

    
    //=====Glaunchy=============================================2014
  	public int getMaxGLTitles()
  	{
  	   int nt = 0;
  	   nt = (int)(360.f/STEP_ANGLE);
  	   return nt;
    }

    //=====Glaunchy=============================================2014
  	// This sets the number of actual titles found! 
  	//   Must use as a max limit when returning selection!
  	public void setNumTitles(int newsize)
  	{
  		numTitles = newsize;
  		stepModulo = STEP_ANGLE*numTitles;
    }

  //=====Glaunchy=============================================2014
	public void setTitle(int which,String newtitle)
	{
	    if (which < 0)         return;
	    if (which >= MAXWOIDS) return;
	    if (newtitle == null)  return;
	    dawoids[which] = newtitle;
 	    return;
	}
    
	//=====Glaunchy=============================================2014
	public void sendInputMessage(int msg)
	{
		//Log.d(TAG,"...sendMessage:" + msg);
		if (msg == 1) //Swipe LEFT
		{
			if (lockedIn == 0) sceneRotationSpeed -= sceneRotationIncrement;
			//Log.d(TAG," swipeLEFT li:" + lockedIn + " sr:" + sceneRotationSpeed);
		    lockedIn = needToAlign = needToZoom = needToLaunch = mTapCount = 0; //clear any user selection

		}
		else if (msg == 2) //Swipe RIGHT
		{
			if (lockedIn == 0) sceneRotationSpeed += sceneRotationIncrement;
			//Log.d(TAG," swipeRITE li:" + lockedIn + " sr:" + sceneRotationSpeed);
		    lockedIn = needToAlign = needToZoom = needToLaunch = mTapCount = 0; //clear any user selection
		}
		else if (msg == 3) //Tap
		{
			float lilAngle = mSceneRotation%360;
			int whichChoice = (int)(lilAngle/STEP_ANGLE);
			sceneRotationSpeed = 0.0f;  //Stop rotation on tap...
			mSceneChoice = whichChoice % numTitles;
			mTapCount++;
			sceneChoiceAngle = STEP_ANGLE * (float)whichChoice; //  desired angle to go to...
			//BUG: If lilAngle > sceneChoiceAngle we get a nice smooth stop.
			//       if sceneChoiceAngle < lilAngle we go really backwards all the way around!
			Log.d(TAG," tapchoice:" + whichChoice + ",sceneChoiceMod:" + mSceneChoice + ",angle:" + lilAngle + ",choiceangle:" + sceneChoiceAngle);
			needToAlign = 1;
		}
	} //end sendInputMessage
	
	//=====Glaunchy=============================================2014
	//  This does a lot of stuff....
	//   Handles a couple of animations
	private void updateSceneRotation() 
    {
    	//int outofbounds = 0;
        if (mLastUpdateMillis != 0) 
        {
            //Log.d(TAG,"srs1:"+ mSceneRotation);
        	float factor = (SystemClock.elapsedRealtime() - mLastUpdateMillis) / FRAME_TIME_MILLIS;
        	if (needToAlign == 0 && lockedIn == 0) //No User choice yet...keep revolving
        	{
                mSceneRotation += sceneRotationSpeed * factor;
                //Log.d(TAG,"srs2:"+ mSceneRotation);
                if (mSceneRotation > sceneMaxAngle)  mSceneRotation -= 360f;  //=sceneMaxAngle - jiggleAngle;
                if (mSceneRotation < sceneMinAngle)  mSceneRotation += 360f;  //= sceneMinAngle + jiggleAngle;
        	}
        	else if (needToAlign != 0 && needToZoom == 0)  //Special animation!
        	{
        		
    			float lilAngle = mSceneRotation%360;
                float stepFactor = sceneRotationSpeed * factor;
        		if (lilAngle > sceneChoiceAngle)
        		{
        			mSceneRotation -= stepFactor;
        			lilAngle = mSceneRotation%360;
        			if (lilAngle <= sceneChoiceAngle) //overshoot! done 
        			{
        				mSceneRotation = sceneChoiceAngle;
        				needToAlign = 0;
        			    lockedIn    = 1;
        				needToZoom  = 1;
        				// Put us back at the lowest possible scene rotation...
        		        //while (mSceneRotation > stepModulo) mSceneRotation-=stepModulo;
        			}
        		}
        		else if (lilAngle < sceneChoiceAngle)
        		{
        			mSceneRotation += stepFactor;
        			lilAngle = mSceneRotation%360;
        			if (lilAngle >= sceneChoiceAngle) //overshoot! done 
        			{
        				mSceneRotation = sceneChoiceAngle;
        				needToAlign = 0;
        			    lockedIn    = 1;
        				needToZoom  = 1;
        				// Put us back at the lowest possible scene rotation...
        		        //while (mSceneRotation > stepModulo) mSceneRotation-=stepModulo;
        			}
        		}
                //Log.d(TAG,"srs3:"+ mSceneRotation);

        	} //end else (special anim)
           if (mTapCount > 1 || (needToZoom > 0 && needToLaunch == 0))
        	{
        		needToZoom++;
        		if (needToZoom > 10)
        		{
        			//if (needToZoom == 10) Log.d(TAG,"LAUNCHIT!.." + mSceneChoice);
        			needToLaunch = 1;  //Sets flag , parent polls to see if it's time to run
        		}
        	}
        }  //end if mlast...
        mLastUpdateMillis = SystemClock.elapsedRealtime();
    } //end updateSceneRotation

	
	//=====Glaunchy=============================================2014
	public Bitmap loadTextBitmap(int maxlen,int textsize,String dawoid)
	{
			//Canned bitmap size for now...
		    int bmpx = 512;
		    int bmpy = 64;
		    Bitmap bm1 = null;
	  	    Bitmap newBitmap = null;
	 	    String captionString = "nothing";
	  	    Config config;
	 	    config = Bitmap.Config.ARGB_8888;
	        int woidlen = dawoid.length();
	        if (woidlen > maxlen) woidlen = maxlen;
	 	    
	        captionString = dawoid.substring(0,woidlen);
	        
	   	    bm1 = Bitmap.createBitmap(bmpx,bmpy, config);
	  	    newBitmap = Bitmap.createBitmap(bmpx,bmpy, config);
	  	    Canvas newCanvas = new Canvas(newBitmap);
	  	   
	  	    newCanvas.drawBitmap(bm1, 0, 0, null);
	  	   
	  	    Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);

	  	    if (dawoid != null)
	  	    {
	      	    paintText.setColor(Color.WHITE);
	      	    paintText.setTextSize(textsize);
	      	    paintText.setStyle(Style.FILL);
	      	    //paintText.setShadowLayer(10f, 10f, 10f, Color.BLACK);
	      	    //Rect rectText = new Rect();
	      	    //paintText.getTextBounds(captionString, 0, captionString.length(), rectText);
	      	    paintText.setTextAlign(Align.CENTER);
	      	    // The text appears a bit too high, nudge it down by 10 pixels...
	      	    newCanvas.drawText(captionString, bmpx/2,10+bmpy/2, paintText);  //was 0,40
	  	    }
	  	    
	  	    //for(loop=0;loop<40;loop++)
	  	   // 	Log.d(TAG,"vb[" + loop + "]:" + viewbuf[loop]);
	  	    int doggen = 0;
	  	    if (doggen == 1)  //for(loop=0;loop<256;loop++)
	  	    { 
	  	       
	  	    	 paintText.setColor(Color.RED);
	        	 newCanvas.drawLine(0, 0, 250, 25, paintText);
	  	       
	  	    }
			 
	  	   return newBitmap;
		 } //end loadTextBitmap

		//=====Glaunchy=============================================2014
		private void drawWordPanel(GL10 gl,int whichtexture)
			{		
			int nsides=0;
			if (whichtexture < 0) return;
			gl.glPushMatrix();
			gl.glScalef(0.5f,0.18f,0.5f);
			//gl.glScalef(1f,1f,1f);
			// Bind the texture to this unit.
		        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureDataHandles[whichtexture]);

				gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
				gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

                gl.glVertexPointer(2, GL10.GL_FLOAT, 0,  splashBuff);
				gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, genericBuff);
                nsides=4;  //square

				gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, nsides);
		        
			    gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
				gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

				gl.glPopMatrix();

				
		} //end 	drawWordPanel
			
		//=====Glaunchy=============================================2014
		// DHS Vestigial for now, highlight is by font color...
		private void drawWordHighlight(GL10 gl)
		{		
			 int nsides=0;
			 gl.glPushMatrix();
			 gl.glScalef(2.0f,0.6f,1.1f);

	        gl.glColor4f (.9f,.9f,.0f,1f);
			gl.glLineWidth(3.0f);

			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

            gl.glVertexPointer(2, GL10.GL_FLOAT, 0,  colorBoxBuff1);
            nsides=4;  //square

		    gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, nsides);
		        
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

			gl.glPopMatrix();
        	gl.glColor4f (1f,1f,1f,1f);
			gl.glLineWidth(1.0f);

				
		}	
			
			

		//=====Glaunchy=============================================2014
			//Must be called once at start time to convert all our nice float
			//arrays of data into stuff than android can eat.  Stoopid, Huh???
			public void processVectors()
			{
				
				// Log.e(TAG, "processVectors... " );
				
			   ByteBuffer bb = ByteBuffer.allocateDirect(square.length*4);
			   bb.order(ByteOrder.nativeOrder());
			   ByteBuffer bb2 = ByteBuffer.allocateDirect(topSplash.length*4);
			   bb2.order(ByteOrder.nativeOrder());
			   ByteBuffer bb3 = ByteBuffer.allocateDirect(genericTex.length*4);
			   bb3.order(ByteOrder.nativeOrder());
			   ByteBuffer bb4 = ByteBuffer.allocateDirect(generic.length*4);
			   bb4.order(ByteOrder.nativeOrder());
			   ByteBuffer bb5 = ByteBuffer.allocateDirect(colorBoxVertices0.length*4);
			   bb5.order(ByteOrder.nativeOrder());
			   ByteBuffer bb6 = ByteBuffer.allocateDirect(colorBoxVertices1.length*4);
			   bb6.order(ByteOrder.nativeOrder());
			   ByteBuffer bb7 = ByteBuffer.allocateDirect(colorBoxVertices2.length*4);
			   bb7.order(ByteOrder.nativeOrder());
			   ByteBuffer bb8 = ByteBuffer.allocateDirect(colorBoxVertices3.length*4);
			   bb8.order(ByteOrder.nativeOrder());
				///Convert some floats into buffers (STOOPID ANDROID/JAVA STEP)   
			  squareBuff = bb.asFloatBuffer();
			  squareBuff.put(square);
			  squareBuff.position(0);
			  
			  splashBuff = bb2.asFloatBuffer();
			  splashBuff.put(topSplash);
			  splashBuff.position(0);    
			 
			  genericBuff = bb3.asFloatBuffer();
			  genericBuff.put(genericTex);
			  genericBuff.position(0);

			  genericVBuff = bb4.asFloatBuffer();
			  genericVBuff.put(generic);
			  genericVBuff.position(0);

			  colorBoxBuff0 = bb5.asFloatBuffer();
			  colorBoxBuff0.put(colorBoxVertices0);
			  colorBoxBuff0.position(0);
			  colorBoxBuff1 = bb6.asFloatBuffer();
			  colorBoxBuff1.put(colorBoxVertices1);
			  colorBoxBuff1.position(0);
			  colorBoxBuff2 = bb7.asFloatBuffer();
			  colorBoxBuff2.put(colorBoxVertices2);
			  colorBoxBuff2.position(0);
			  colorBoxBuff3 = bb8.asFloatBuffer();
			  colorBoxBuff3.put(colorBoxVertices3);
			  colorBoxBuff3.position(0);

			}  //end processVectors
				

	

		

}  //end class
