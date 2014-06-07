/*
   ____ _                           _           
  / ___| |    __ _ _   _ _ __   ___| |__  _   _ 
 | |  _| |   / _` | | | | '_ \ / __| '_ \| | | |
 | |_| | |__| (_| | |_| | | | | (__| | | | |_| |
  \____|_____\__,_|\__,_|_| |_|\___|_| |_|\__, |
                                          |___/ 
   OpenGL chooser: Creates a 3D interactive view with a list of apps to choose from...
     Responds to touch events and selects with a Tap... 

 
    Copyright (C) 2014 fractallonomy. All Rights Reserved.
    GLaunchy: OpenGL App Launcher.  Does NOT rely on Glass' ever-changing API
               Only uses touch Gestural input, and sends out Activity startup calls...

    DHS June 6 2014: Initial Build for submittal...
               Displays a 3D "Paddlewheel" with text entries for every app found
                 on Glass, up to a max of 24 for now.
               User can revolve the paddlewheel up and down by swiping
               User taps to selects a choice, the OGL code stops revolving.
               User taps again, and the choice is zoomed out in OGL-land,
                   while this activity is continually polling the OGL code
                   to see if the animation is complete.
                   At that point the desired App is invoked and this Activity is killed.

		   Here's where there's some room for improvement:
		      - Make the OGL paddlewheel so it handles infinite titles
		         by using a circular texture buffer to store app names
              - Couldn't get the Settings Bundle ID properly, so Settings is ommitted for now.....    

*/

package com.frak.glaunchy;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class LGLActivity extends Activity  
    implements GestureDetector.BaseListener, GestureDetector.FingerListener {

    private static final String TAG = LGLActivity.class.getSimpleName();

    private static GLSurfaceView mGLView;

    private LGLActivity thisOne;
    private Timer myTimer;
 
	 private static ArrayList<ApplicationInfo> mApplications = null;
	 private final ArrayList<String> mExcludedApps = new ArrayList<String>();

    // Index of api demo cards.
    // Visible for testing.
    static final int CARDS = 0;
    static final int GESTURE_DETECTOR = 1;
    static final int THEMING = 2;
    static final int OPENGL = 3;

    static int didLaunch = 0;

    private GestureDetector mGestureDetector;

    private static int needToSetTitles = 1;
    

	//=====Glaunchy ACT==========================================2014
   @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        //DHS Stay on!
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        thisOne = this;
        // Initialize the gesture detector and set the activity to listen to discrete gestures.
        //mGestureDetector = new GestureDetector(this, new GlAppGestureListener(this));
        // Initialize the gesture detector and set the activity to listen to discrete gestures.
        mGestureDetector = new GestureDetector(this).setBaseListener(this).setFingerListener(this);
        
             // Create a GLSurfaceView instance and set it
            // as the ContentView for this Activity.
            mGLView = new MyGLSurfaceView(this);
            setContentView(mGLView);
    		//Timer section... timer is used to handle popup menus and such
    		//  that get requested by the render module...
    		myTimer = new Timer();
    		myTimer.schedule(new TimerTask() {			
    			@Override
    			public void run() {
    				mHandler.obtainMessage(1).sendToTarget();
    			}
    			
    		}, 0, 100); // 1/10 sec checks...

    	setupExclusions();
    	loadApplications(false);
        
    } //end onCreate

     
    
	//=====Glaunchy ACT=======================================2014
    private void setupExclusions() {
        mExcludedApps.add("com.google.glass.home");
        mExcludedApps.add(getPackageName());
    }

	//=====Glaunchy ACT=======================================2014
    public void loadApplications(boolean isLaunching) 
    {
        //Log.d(TAG,"LoadApps...1 " + isLaunching);
        if (isLaunching && mApplications != null) {
            return;
        }
        //Log.d(TAG,"LoadApps...2");
        PackageManager manager = getPackageManager();

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
        Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

        if (apps != null) {
            final int count = apps.size();

            if (mApplications == null) {
                mApplications = new ArrayList<ApplicationInfo>(count);
            }
            mApplications.clear();

            //DHS FUCK THIS. Settings can't be reached, if anyone knows how, addit!
            // Create a launcher for Glass Settings or we have no way to hit that
            //createGlassSettingsAppInfo();

            for (int i = 0; i < count; i++) {
                ApplicationInfo application = new ApplicationInfo();
                ResolveInfo info = apps.get(i);
                //Log.d("Launchyi", info.activityInfo.applicationInfo.packageName);
                // Let's filter out this app
                if (!mExcludedApps.contains(info.activityInfo.applicationInfo.packageName)) {
                    application.title = info.loadLabel(manager);
                    application.stitle = (String) info.loadLabel(manager);
                    //Log.d(TAG,"...title:"+ application.stitle);
                    application.setActivity(new ComponentName(
                            info.activityInfo.applicationInfo.packageName, info.activityInfo.name),
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    application.icon = info.activityInfo.loadIcon(manager);

                    mApplications.add(application);
                }
            }

            // PackageManagerclearPackagePreferredActivities in special case
            // This needs to always be last?
        }
    }

	//=====Glaunchy ACT=======================================2014
    // DHS Put this in when the stoopid bundle info can be found!
    private void createGlassSettingsAppInfo() {
        ApplicationInfo application = new ApplicationInfo();

        application.title = "Glass Settings";
        application.setActivity(new ComponentName("com.google.glass.home",
                "com.google.glass.settings.SettingsActivity"),
//      application.setActivity(new ComponentName("com.google.glass.home",
                //"com.google.glass.home.settings.SettingsActivity"),
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        // application.icon = info.activityInfo.loadIcon(manager);

        mApplications.add(application);
    }

	//=====Glaunchy ACT=======================================2014
   @Override
    protected void onDestroy() {
    	//Log.d(TAG,"...DESTROY MAIN");
        super.onDestroy();
    }

  
    
	//=====Glaunchy ACT=======================================2014
    @Override
    protected void onResume() {
        super.onResume();
        //Log.d(TAG,"got onResume....");
        //Reset timer...
   		if (myTimer == null)
   		{
   	       myTimer = new Timer();
   	       myTimer.schedule(new TimerTask() {			
   			@Override
   			public void run() {
   				mHandler.obtainMessage(1).sendToTarget();
   			}
   			
   		}, 0, 100); // 1/10 sec checks...
  			
   		}
 
        didLaunch = 0;
        
        setTitlesAlready();
        
    	if (mGLView != null)
    		{
            setContentView(mGLView);
    		((MyGLSurfaceView) mGLView).clearSelection();
    		}

    }

	//=====Glaunchy ACT=======================================2014
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"got onPause...."); 
        //Clobber timer....
		if(myTimer != null) {
		  	myTimer.cancel();
		   	myTimer = null;
		    }
		if (mGLView != null) 
			   {
			     //Log.d(TAG,"sendpause called...");
			     ((MyGLSurfaceView) mGLView).sendPauseToGL();
			   }

    }

	//=====Glaunchy ACT=======================================2014
    // TIMER ROUTINE: This polls OGL view forever. 
    //                   This way we can tell what user is doing in there...
	//                   (Selection is done asynchronously!)
    // This spawns off the Launch, too, and then kills LGLActivity
    // I tried making this static but there was a problem calling StartActivity
    public Handler mHandler = new Handler() {
	    public void handleMessage(Message msg) {
	    	//Check GL renderer (two levels down)
	    	//  to see if user has made a choice!
	    	int needToLaunch = 0;
        	if (mGLView != null) 
        		{
        		  if (needToSetTitles == 1)
        		  {
        			  setTitlesAlready();
        			  needToSetTitles = 0; //Only happens once!
        		  }
        		
        		   needToLaunch = ((MyGLSurfaceView) mGLView).checkForSelection();
        		   if (needToLaunch > 0 && didLaunch == 0)
        		   {
        			   Log.d(TAG,"..TOPLEVEL: Launching:" + needToLaunch);
        			   Intent launchIntent = mApplications.get(needToLaunch-1).intent;
//  not needed?? 	   ApplicationInfo app = (ApplicationInfo) list.getItemAtPosition(wherezit);
   					   startActivity(launchIntent);   //app.intent);                

   					   didLaunch = 1;
        			   thisOne.finish();  //Clobber us!?
        		   }
        		}
	    	
	    }  //end handleMessage
	} ; //end handler
	

	//=====Glaunchy ACT=======================================2014
    @Override
    public boolean onGesture(Gesture gesture) {
        //mLastGesture.setText(gesture.name());
    	//int x = gesture.values()
    	//Log.d(TAG,"....GESTURE: " + gesture);
        if (gesture == Gesture.SWIPE_DOWN) 
        {
        	//Log.d(TAG,"swipe down");
            return false;
        } 
        else if (gesture == Gesture.SWIPE_LEFT)
        {
        	//Log.d(TAG,"swipe LEFT");
        	if (mGLView != null) ((MyGLSurfaceView) mGLView).sendInputMessage(1);
            return false;
        } 
        else if (gesture == Gesture.SWIPE_RIGHT)
        {
        	//Log.d(TAG,"swipe RIGHT");
        	if (mGLView != null) ((MyGLSurfaceView) mGLView).sendInputMessage(2);
            return false;
        } 
        else if (gesture == Gesture.TAP)
        {
        	//Log.d(TAG,"TAP");
        	((MyGLSurfaceView) mGLView).sendInputMessage(3);
            ((AudioManager) getSystemService(Context.AUDIO_SERVICE)).playSoundEffect(Sounds.TAP);
            return false;
        } 
        else 
        {
            return true;
        }

    }

    
	//=====Glaunchy ACT=======================================2014
	@Override
	public void onFingerCountChanged(int arg0, int arg1) 
	{
		
	}

	
	//=====Glaunchy ACT=======================================2014
    @Override
	public boolean onGenericMotionEvent(MotionEvent event) 
    {
	        return mGestureDetector.onMotionEvent(event);
	}
    
	//=====Glaunchy ACT=======================================2014
   public void setTitlesAlready()
    {
	      int tc   = 0;
		  int nt   = mApplications.size();
		  int oglc = ((MyGLSurfaceView) mGLView).getMaxGLTitles();

		  // Here's where there's some room for improvement:
		  //   make the OGL paddlewheel so it handles infinite titles
		  //   by using a circular texture buffer to store app names
		  if (nt > oglc) nt = oglc;  //Too Many Titles? Too Bad!  
		  ((MyGLSurfaceView) mGLView).setNumTitles(nt);

		  for(int loop=0;loop<nt;loop++)
		  {
			  String title = mApplications.get(loop).stitle;
			  ((MyGLSurfaceView) mGLView).setTitle(loop,title);
			  //Log.d(TAG,"add title..." + title);
			  tc++;
		  }
		  if (tc < oglc)  // Is there room in the OGL view for more titles?
		  {
			  while (tc < oglc)
			  {
			      int tptr = 0;
				  while (tptr < nt && tc < oglc)
				  {
					  String title = mApplications.get(tptr).stitle;
					  ((MyGLSurfaceView) mGLView).setTitle(tc,title);
					  tptr++;
					  tc++;
				  } //end inner while
			  }    //end outer while
		  } //end if tc

    	return;
    }  //end setTitlesAlready

}

//=====Glaunchy SV====================================2014
class MyGLSurfaceView extends GLSurfaceView {
	private MyGLRenderer MyGlRe = null;
    public MyGLSurfaceView(Context context){
        super(context);
        MyGlRe = new MyGLRenderer(context);
        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(MyGlRe);
    }
    
    //=====Glaunchy=============================================2014
    //Message: Clugey. 1 = swipe left, 2=right...
    public void sendInputMessage(int msg)
    {
    	if (MyGlRe == null) return;
    	MyGlRe.sendInputMessage(msg);
    	return;
    }

    //=====Glaunchy=============================================2014
    public int checkForSelection()
    {
    	if (MyGlRe == null) return 0;
    	return MyGlRe.checkForSelection();
    }
 
    //=====Glaunchy=============================================2014
    public void setTitle(int which,String newtitle)
    {
    	if (MyGlRe == null) return;
    	MyGlRe.setTitle(which,newtitle);
    	return;
    }
    
    
    //=====Glaunchy=============================================2014
    // Spawns off a process! Sends ASYNC request!
    public void sendPauseToGL()
    {
        //Log.d("MGLSV","sendpause 1...");
    	if (MyGlRe == null) return;
    	//Send Asynch request to GL to pause,,,,
 	    this.queueEvent(new Runnable() {
	        @Override public void run() {
            // Tell the renderer that it's about to be paused so it can clean up.
	        	setRenderMode(RENDERMODE_WHEN_DIRTY);
            }
        });
    	return;
    }
       
    
   //=====Glaunchy=============================================2014
   public int getMaxGLTitles()
   {
       //Just pass the call on down...
       if (MyGlRe == null) return 0;
       return MyGlRe.getMaxGLTitles();
   }
    	
    	
   //=====Glaunchy=============================================2014
   public void setNumTitles(int newsize)
   {
       //Just pass the call on down...
       if (MyGlRe == null) return;
       MyGlRe.setNumTitles(newsize);
   }
  	
    //=====Glaunchy=============================================2014
	public void clearSelection()
	{
		//Just pass the call on down...
		if (MyGlRe == null) return;
    	MyGlRe.clearSelection();
 	}


}  //end class



