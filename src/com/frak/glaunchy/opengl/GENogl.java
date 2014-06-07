//   _______  _______  _______                __ 
//  |     __||    ___||    |  |.-----..-----.|  |
//  |    |  ||    ___||       ||  _  ||  _  ||  |
//  |_______||_______||__|____||_____||___  ||__|
//                                    |_____|    
//
//  GENogl.c: Generic OpenGL stuff
//  oogieIPAD
//
//  Created by dave scruton on 5/29/13.
//  Copyright (c) 2013 fractallonomy. All rights reserved.
//
// DHS 5/30/13: READY FOR RELEASE! 
// DHS 5/31/13: flipped sphere texture vertically, now it matches other shapes
//=========================================================================
// APP submittal to Apple, June 13th!!!
//=========================================================================
// DHS 7/4...6 Added float/int conversion to/from 24-bit pixel data,
//              used by read/write setups in main...
// DHS 7/11/13: After a trip to the library and heatstroke, it looks like
//               the second try at packing/unpacking to pixels works!
//               we will use the 3-value pack -> 4 color pixel scheme.
// DHS 10/19/13: FINALLY got smartImage pack/unpack working. 
//               NOTE: all unsigned byte stuff needs to be dealt with by
//               integers.  Do all copying, etc BEFORE final bit packing!
//=========================================================================

package com.frak.glaunchy.opengl;

//import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.frak.glaunchy.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.util.Log;
//import dalvik.system.VMRuntime;





//import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.lang.*;

public class GENogl {
	public static String TAG = "GENogl"; 
	public static final float PI 					=  3.141592627f;
	public static final float TWOPI 				=  PI * 2.0f;
	public static final float DEGREES_TO_RADIANS 	=  PI / 180.0f;
	public static final int IMPACTFONT_BASE 		=  37;
	public static final float WSPHERE_SIZE 			=  0.65f;
	public static final int MAX_WSPHERE_SEGS 		= 30;
	
	
	  //Used in impact font, glyph points/offsets...
	  public static final int MAXGLYPHGAP =  64;
	  int[] gaps     = new int[MAXGLYPHGAP];
	  int[] gaps2    = new int[MAXGLYPHGAP];
	  float[] fgaps  = new float[MAXGLYPHGAP];
	  float[] fgaps2 = new float[MAXGLYPHGAP];
	
	float[] glyphVertices = new float[]  {
			-0.05f,  0.05f, 0f, 
			 0.05f,  0.05f, 0f,
			-0.05f, -0.05f, 0f,
			 0.05f, -0.05f, 0f,
			 0.05f, -0.05f, 0f,
			 0.05f, -0.05f, 0f,
		};
	float[] glyphTexcoords = new float[]  {
			0f,0f,
			0f,0f,
			0f,0f,
			0f,0f,
			0f,0f,
			0f,0f,
	};

	   Resources res;

	
	
	public static final float[] GENTexcoords = 
		{
		0, 0,
		1, 0,
		0, 1,
		1, 1,
		};
	 static final float[] GENcubeFaceVertices = 
		{
	    1.0f,  1.0f, -1.0f,
		-1.0f,  1.0f, -1.0f,
	    1.0f, -1.0f, -1.0f,
		-1.0f, -1.0f, -1.0f,
		};
	static float[] square = new float[] { 
			 1f, -1f, 1.0f,
			-1f, -1f, 1.0f,
            1f,  1f, 1.0f,
           -1f,  1f, 1.0f };  
	static float[] genericTex = new float[] {0f,0f,1f,0f,0f,1f,1f,1f};
	public static int texCubeDrawn    = 0;
	public static int texSphereDrawn  = 0;
	public static int texConeDrawn    = 0;
	public static int texCylDrawn     = 0;
	public static int texTorDrawn     = 0;
	public static int bbAlloc		  = 0;
	
	static float[] shapeVertices;
	static float[] shapeTex;
    //float[] torusVertices;
    //float[] torusTex;
	static ByteBuffer bbv;
	static ByteBuffer bbt;
	static FloatBuffer textureBuff;
	static FloatBuffer vectorBuff;
	static FloatBuffer squareBuff,genericVBuff,tbBuff,colorBoxBuff;
	static FloatBuffer genericBuff;
	//Use this stuff as we evolve; try keeping vertices as globals,
	//  only init first time. speeds up things...
	//public static final double *sphVertices; 
	//GLfloat *sphTex;
	//GLint *sphFaces;
	//GLfloat *conVertices; 
	//GLfloat *conTex;
	//GLint *conFaces;
	//GLfloat *cylVertices; 
	//GLfloat *cylTex;
	//GLint *cylFaces;
	//GLfloat *torVertices; 
	//GLfloat *torTex;
	//GLint *torFaces;

	//Pack/Unpack stubbed for now...
	public static  int packSize,packPtr,packMod;
	public static   byte[] packData;
	public static   int r0,r1,r2,r3;
	public static   int g0,g1,g2,g3;
	public static   int b0,b1,b2,b3;
	public static   byte[] b4 = new byte[4];
	public static  int[] b4i = new int[4];

	
//--------(GENogl)------------------------------------------------
public static void GENoglInit()
{
    texSphereDrawn  = 0;
    texConeDrawn    = 0;
    texCylDrawn     = 0;
    texTorDrawn     = 0;
    packSize = packPtr = packMod = 0;
} //end GENoglInit



 

//---------(packpixels)----------------(packpixels)--------
static int getPackPtr()
{
    return packPtr ;
}

//---------(packpixels)----------------(packpixels)--------
static int pulloutPack(int index)
{
	int a,b,c;
	int aa,bb,cc;
	int tint,lindex = index;
    if (index >= packSize-4 ) return 0;  //out of bounds

    int bbc,bbb,bba;
    bbc = packData[lindex++]; 
    bbb = packData[lindex++]; 
    bba = packData[lindex++]; 
    //Log.e(TAG,index + ":: bbc:" + bbc + ","   + bbb + "," +   bba   );
    
    c = (int)bbc & 255; //This is opposite of ipad version. why?
    b = (int)bbb & 255;
    a = (int)bba & 255;
    //Log.e(TAG,index + ":: cba:" + c + "," + Integer.toHexString(c) + "," + b + "," + Integer.toHexString(b) + "," + a + Integer.toHexString(a) );
    
    aa = a;
    bb = b<<8;
    cc = c<<16;
    tint = a;
    tint+= b<<8;
    tint+= c<<16;
    tint+= (255 << 24);
    //Log.e(TAG,index + ":: pop:" + Integer.toHexString(tint) );
    return tint;
} //end pulloutPack

//---------(packpixels)----------------(packpixels)--------
// Data comes in BGRA,BGRA, etc
static void loadinPack(int index,int ival)
{
	int tint = ival;
	int lindex = index;
    if (index >= packSize-4 ) return;  //out of bounds
    packData[lindex+2] = (byte)(tint & 255);   //This is opposite of ipad version. why?
    tint = tint >> 8;
    packData[lindex+1] = (byte)(tint & 255);
    tint = tint >> 8;
    packData[lindex+0] = (byte)(tint & 255);
    tint = tint >> 8;
    packData[lindex+3] = (byte)(tint & 255);
    //Log.e(TAG,"1packdata[" + (lindex)  + "]:" + packData[lindex]);
    //Log.e(TAG,"2packdata[" + (lindex+1)  + "]:" + packData[lindex+1]);
    //Log.e(TAG,"3packdata[" + (lindex+2)  + "]:" + packData[lindex+2]);
    //Log.e(TAG,"4packdata[" + (lindex+3)  + "]:" + packData[lindex+3]);
} //end loadinPack

//---------(packpixels)----------------(packpixels)--------
static void packInit(int size)
{
    packPtr = 0;
    packMod = 0;
    r0 = r1 = r2 = r3 =0;
    g0 = g1 = g2 = g3 =0;
    b0 = b1 = b2 = b3 =0;
    
    if (packData != null)
    {
        packData = null;  //This is probably leaky!
    }
    packData =  new byte[4*size];
    if (packData != null)
    {
        packSize = 4*size;
    }
    else 
    {
        packSize = 0;
    }
}  // end packInit 

//---------(packpixels)----------------(packpixels)--------
static void packTerm()
{
    if (packData != null)
    {
         
        packData = null;  //This is probably leaky!
    }
}  // end packTerm


//---------(packpixels)----------------(packpixels)--------
static void packInt(int ival)
{
    int tint;
    int t0,t1,t2,t3;
    tint = ival;
    //NSLog( @" pI[%d ] %x %d",packPtr,tint,tint);
    t0 = tint;
    t1 = tint>>8;
    t2 = tint>>16;
    t3 = tint>>24;
    t0 = (t0 & 255);
    t1 = (t1 & 255);
    t2 = (t2 & 255);
    t3 = (t3 & 255);
    b4i[0] =  (t0);
    b4i[1] =  (t1);
    b4i[2] =  (t2);
    b4i[3] =  (t3);
    //Log.e(TAG,"pI["+(packPtr/4)+"]:" + Integer.toHexString(tint) + " " + tint );

    packPixels();
}//end packInt


 
//---------(packpixels)----------------(packpixels)--------
static void packFloat(float rval)
{
    int tint;
    int t0,t1,t2,t3;
    //NSLog( @" pI[%d ] %x %d",packPtr,tint,tint);
    tint = Float.floatToRawIntBits(rval);
    t0 = tint;
    t1 = tint>>8;
    t2 = tint>>16;
    t3 = tint>>24;
    t0 = (t0 & 255);
    t1 = (t1 & 255);
    t2 = (t2 & 255);
    t3 = (t3 & 255);
    b4i[0] =  (t0 );
    b4i[1] =  (t1 );
    b4i[2] =  (t2 );
    b4i[3] =  (t3 );
    //Log.e(TAG,"pf["+(packPtr/4)+"]:" + Integer.toHexString(tint) + " " + rval );
    packPixels();

}//end packFloat


//---------(packpixels)----------------(packpixels)--------
// NOTE packdata now is bytes, NOT ints
static void packPixels()
{
    byte[] workc = new byte[4];
    if (packPtr > packSize - 16) return;//out of space!
    //Log.e(TAG,"pP:" + packMod);
    if (packMod %3 == 0)
    { 
        r0=b4i[0];
        r1=b4i[1];
        r2=b4i[2];
        r3=b4i[3];
    }
    if (packMod %3 == 1)
    { 
        g0=b4i[0];
        g1=b4i[1];
        g2=b4i[2];
        g3=b4i[3];
    }
    if (packMod %3 == 2)
    { 
        b0=b4i[0];
        b1=b4i[1];
        b2=b4i[2];
        b3=b4i[3];
    }
    packMod++;
    if (packMod %3 == 0) //time to write pixels
    {
        //NSLog(@" -----p ");
        //Log.e(TAG,"rgb0:" + Integer.toHexString(r0) + ","+ Integer.toHexString(g0) + ","+ Integer.toHexString(b0) );
        workc[0]=(byte)((int)r0 & 255);
        workc[1]=(byte)((int)g0 & 255);
        workc[2]=(byte)((int)b0 & 255);
        workc[3]=(byte)255;   //alpha first or last?
        packData[packPtr++]=workc[0];
        packData[packPtr++]=workc[1];
        packData[packPtr++]=workc[2];
        packData[packPtr++]=workc[3];
         
        //Log.e(TAG,"rgb1:" + Integer.toHexString(r1) + ","+ Integer.toHexString(g1) + ","+ Integer.toHexString(b1) );
        workc[0]=(byte)((int)r1 & 255);
        workc[1]=(byte)((int)g1 & 255);
        workc[2]=(byte)((int)b1 & 255);
        workc[3]=(byte)255;   //alpha first or last?
        packData[packPtr++]=workc[0];
        packData[packPtr++]=workc[1];
        packData[packPtr++]=workc[2];
        packData[packPtr++]=workc[3];
        
        //Log.e(TAG,"rgb2:" + Integer.toHexString(r2) + ","+ Integer.toHexString(g2) + ","+ Integer.toHexString(b2) );
        workc[0]=(byte)((int)r2 & 255);
        workc[1]=(byte)((int)g2 & 255);
        workc[2]=(byte)((int)b2 & 255);
        workc[3]=(byte)255;   //alpha first or last?
        packData[packPtr++]=workc[0];
        packData[packPtr++]=workc[1];
        packData[packPtr++]=workc[2];
        packData[packPtr++]=workc[3];
        
        //Log.e(TAG,"rgb3:" + Integer.toHexString(r3) + ","+ Integer.toHexString(g3) + ","+ Integer.toHexString(b3) );
        workc[0]=(byte)((int)r3 & 255);
        workc[1]=(byte)((int)g3 & 255);
        workc[2]=(byte)((int)b3 & 255);
        workc[3]=(byte)255;   //alpha first or last?
        packData[packPtr++]=workc[0];
        packData[packPtr++]=workc[1];
        packData[packPtr++]=workc[2];
        packData[packPtr++]=workc[3];
        //Log.e(TAG,"-----p");
        if (false) Log.e(TAG,"  r:" +  r0  + " "+  r1  + " "+  r2  + " " +  r3   + 
      		  "  g:" +  g0 + " "+  g1 + " "+  g2 + " " +  g3  + 
      		  "  b:" +  b0 + " "+  b1 + " "+  b2 + " " +  b3 );
        if (false) Log.e(TAG,"  r:" + Integer.toHexString(r0) + " "+ Integer.toHexString(r1) + " "+ Integer.toHexString(r2) + " " + Integer.toHexString(r3)  + 
        		  "  g:" + Integer.toHexString(g0) + " "+ Integer.toHexString(g1) + " "+ Integer.toHexString(g2) + " " + Integer.toHexString(g3)  + 
        		  "  b:" + Integer.toHexString(b0) + " "+ Integer.toHexString(b1) + " "+ Integer.toHexString(b2) + " " + Integer.toHexString(b3) );
        		
    }
}//end packPixels


//---------(packpixels)----------------(packpixels)--------
static int unpackInt()
{
  int tint;
  int tduh0,tduh1,tduh2,tduh3;
  unpackPixels();
  tduh0 = b4i[0] & 255;
  tduh1 = b4i[1] & 255;
  tduh2 = b4i[2] & 255;
  tduh3 = b4i[3] & 255;
  //Log.e(TAG," b40:" + b4[0] + " hex:" + Integer.toHexString(tduh0));
  //Log.e(TAG," b41:" + b4[1] + " hex:" + Integer.toHexString(tduh1));
  //Log.e(TAG," b42:" + b4[2] + " hex:" + Integer.toHexString(tduh2));
  //Log.e(TAG," b43:" + b4[3] + " hex:" + Integer.toHexString(tduh3));
  tint = tduh0;
  tint +=(tduh1 << 8);
  tint +=(tduh2 << 16);
  tint +=(tduh3 << 24);
  //Log.e( TAG," Upi [" + packPtr/4 + "]:"+ Integer.toHexString(tint)  + " " + tint);
  return tint;
} //end unpackint

//---------(packpixels)----------------(packpixels)--------
static int unpackInt2()
{
	  int tint;
	  int tduh0,tduh1,tduh2,tduh3;
	  unpackPixels();
	  tduh0 = b4i[0] & 255;
	  tduh1 = b4i[1] & 255;
	  tduh2 = b4i[2] & 255;
	  tduh3 = b4i[3] & 255;
	  //Log.e(TAG," b40:" + b4[0] + " hex:" + Integer.toHexString(tduh0));
	  //Log.e(TAG," b41:" + b4[1] + " hex:" + Integer.toHexString(tduh1));
	  //Log.e(TAG," b42:" + b4[2] + " hex:" + Integer.toHexString(tduh2));
	  //Log.e(TAG," b43:" + b4[3] + " hex:" + Integer.toHexString(tduh3));
	  tint = tduh0;
	  tint +=(tduh1 << 8);
	  tint +=(tduh2 << 16);
	  tint +=(tduh3 << 24);
	  //Log.e( TAG," Upi [" + packPtr/4 + "]:"+ Integer.toHexString(tint)  + " " + tint);
	  return tint;} //end unpackint

//---------(packpixels)----------------(packpixels)--------
static float unpackFloat()
{
	int tint;
    float tf;
    int tduh0,tduh1,tduh2,tduh3;
    unpackPixels();
    tduh0 = b4i[0] & 255;
    tduh1 = b4i[1] & 255;
    tduh2 = b4i[2] & 255;
    tduh3 = b4i[3] & 255;

	tint = tduh0;
	tint +=(tduh1 << 8);
	tint +=(tduh2 << 16);
	tint +=(tduh3 << 24);
    tf = Float.intBitsToFloat(tint);
    //Log.e( TAG," Upf [" + packPtr/4 + "]:"+ Integer.toHexString(tint)  + " " + tf);
    return tf;
} //end unpackFloat

 
//---------(packpixels)----------------(packpixels)--------
static void unpackPixels()
{
     int ruint;
     int alpha;
     byte[] workc = new byte[4];
    if (packMod % 3 ==0) //need to unload pixels?
    {
    	workc[0] = packData[packPtr++];
    	workc[1] = packData[packPtr++];
    	workc[2] = packData[packPtr++];
    	workc[3] = packData[packPtr++];
        r0=  workc[0];
        g0=  workc[1];
        b0=  workc[2];
        alpha = workc[3];
        //Log.e(TAG,"rgb0:" + Integer.toHexString(r0) + ","+ Integer.toHexString(g0) + ","+ Integer.toHexString(b0)+ ","+ Integer.toHexString(alpha));
    	workc[0] = packData[packPtr++];
    	workc[1] = packData[packPtr++];
    	workc[2] = packData[packPtr++];
    	workc[3] = packData[packPtr++];
        r1=  workc[0];
        g1=  workc[1];
        b1=  workc[2];
        alpha = workc[3];
        //Log.e(TAG,"rgb1:" + Integer.toHexString(r1) + ","+ Integer.toHexString(g1) + ","+ Integer.toHexString(b1)+ ","+ Integer.toHexString(alpha));
        //Log.e(TAG,"rgb1:" + r1 + ","+ g1 + ","+ b1);
    	workc[0] = packData[packPtr++];
    	workc[1] = packData[packPtr++];
    	workc[2] = packData[packPtr++];
    	workc[3] = packData[packPtr++];
        r2=  workc[0];
        g2=  workc[1];
        b2=  workc[2];
        alpha = workc[3];
        //Log.e(TAG,"rgb2:" + Integer.toHexString(r2) + ","+ Integer.toHexString(g2) + ","+ Integer.toHexString(b2)+ ","+ Integer.toHexString(alpha));
        //Log.e(TAG,"rgb2:" + r2 + ","+ g2 + ","+ b2);
    	workc[0] = packData[packPtr++];
    	workc[1] = packData[packPtr++];
    	workc[2] = packData[packPtr++];
    	workc[3] = packData[packPtr++];
        r3=  workc[0];
        g3=  workc[1];
        b3=  workc[2];
        alpha = workc[3];
        //Log.e(TAG,"rgb3:" + Integer.toHexString(r3) + ","+ Integer.toHexString(g3) + ","+ Integer.toHexString(b3)+ ","+ Integer.toHexString(alpha));
        //Log.e(TAG,"rgb3:" + r3 + ","+ g3 + ","+ b3);

    } //end unload pixels
    if (packMod %3 == 0)
    { 
        b4i[0]= r0;
        b4i[1]= r1;
        b4i[2]= r2;
        b4i[3]= r3;
        //Log.e(TAG,"packmod0 b4:" + Integer.toHexString(b4[0]) + ","+ Integer.toHexString(b4[1]) + ","+ Integer.toHexString(b4[2])+ ","+ Integer.toHexString(b4[3]));
        
    }
    if (packMod %3 == 1)
    { 
        b4i[0]= g0;
        b4i[1]= g1;
        b4i[2]= g2;
        b4i[3]= g3;
        //Log.e(TAG,"packmod1 b4:" + Integer.toHexString(b4[0]) + ","+ Integer.toHexString(b4[1]) + ","+ Integer.toHexString(b4[2])+ ","+ Integer.toHexString(b4[3]));
    }
    if (packMod %3 == 2)
    { 
        b4i[0]= b0;
        b4i[1]= b1;
        b4i[2]= b2;
        b4i[3]= b3;
        //Log.e(TAG,"packmod2 b4:" + Integer.toHexString(b4[0]) + ","+ Integer.toHexString(b4[1]) + ","+ Integer.toHexString(b4[2])+ ","+ Integer.toHexString(b4[3]));
    }
    packMod ++;
    
} //end unpackPixels 

 

//--------(GENogl)------------------------------------------------
public static void GENoglTerm()
{
    if (texSphereDrawn != 0)
    {
       // free(sphFaces);	
       // free(sphTex);	
       // free(sphVertices);	
        texSphereDrawn = 0;
    }
    if (texConeDrawn  != 0)
    {
        //free(conFaces);	
        //free(conTex);	
        //free(conVertices);	
        texConeDrawn = 0;
    }
    if (texCylDrawn  != 0)
    {
        //free(cylFaces);	
        //free(cylTex);	
        //free(cylVertices);	
        texCylDrawn = 0;
    }
    if (texTorDrawn  != 0)
    {
        //free(torFaces);	
        //free(torTex);	
        //free(torVertices);	
        texTorDrawn = 0;
    }
    
} //end GENoglTerm




//--------(GENogl)------------------------------------------------
//  args are: x/y tesselation, xy size of cone
public static void GENdrawTexCone(GL10 gl,int xtess,int ytess,float psx,float psy)  
{   
	float r1,r2,angle,x,y,y2,z,texx,texy,tex0;
	float  tsx,tsy;
	int i,j,ijptr,tptr,fptr ;
	int numLat,numLong,conNumIndices;


	if (texSphereDrawn == 0)
	{
		setupBuffers(xtess,ytess);
		texSphereDrawn = 1;
	}  


	tsx   = 1.0f/(float)(xtess+1);
	tsy   = 1.0f/(float)(ytess-1);
	//angle = 1.0001;
	numLong = xtess;
	numLat  = ytess;

	texy = 0.0f;
	tex0 = 0.0f;
		
	ijptr = tptr = fptr = conNumIndices = 0;
	for (i=0;i<numLat-1;i++)
	{ 	y=   -2.0f * (-0.5f +   ( (float)i)    /(float)(numLat-1));
		y2=  -2.0f * (-0.5f +   ( (float)(i+1))/(float)(numLat-1));
		texx = tex0;
		r1 = (float)i/(float)(numLat-1);
		r2 = (float)(i+1)/(float)(numLat-1);
		y*=psy;
		y2*=psy;
		for (j=0;j<numLong+1;j++)
		{
			angle=(TWOPI * j)/(float)numLong; //this is our equatorial angle...
			x= psx*(float)Math.sin(angle)*r1;
			z= psy*(float)Math.cos(angle)*r1;
			shapeVertices[ijptr++] = x;
			shapeVertices[ijptr++] = y;
			shapeVertices[ijptr++] = z;
			
			shapeTex[tptr++] = texx; 
			shapeTex[tptr++] = texy;
			
			x= psx*(float)Math.sin(angle)*r2;
			z= psy*(float)Math.cos(angle)*r2;
			texy+=tsy;
			shapeVertices[ijptr++] = x;
			shapeVertices[ijptr++] = y2;
			shapeVertices[ijptr++] = z;
			
			shapeTex[tptr++] = texx;  
			shapeTex[tptr++] = texy;
			fptr+=2;
			
			texx+=tsx;
			texy-=tsy;
		} //end for j
		
		texy+=tsy; //get texture for next row down
	}
	gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

	 
	//As soon as vertices are filled, stick'em into java constructs...
	vectorBuff.put(shapeVertices);
	vectorBuff.position(0);
	textureBuff.put(shapeTex);
	textureBuff.position(0);
	gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vectorBuff);
	gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuff);
	conNumIndices = fptr;
	gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, conNumIndices);

	gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	texConeDrawn=1;

} //end drawTexCone
//--------(GENogl)------------------------------------------------
//  args are: x/y tesselation, xy size of cube
public static void GENdrawTexCube(GL10 gl) 
{
	
	if (texCubeDrawn == 0)
	{
		  ByteBuffer bb = ByteBuffer.allocateDirect(square.length*4);
		   bb.order(ByteOrder.nativeOrder());
		   //ByteBuffer bb2 = ByteBuffer.allocateDirect(topBar.length*4);
		   //bb2.order(ByteOrder.nativeOrder());
		   ByteBuffer bb3 = ByteBuffer.allocateDirect(genericTex.length*4);
		   bb3.order(ByteOrder.nativeOrder());
		  	  squareBuff = bb.asFloatBuffer();
		  squareBuff.put(square);
		  squareBuff.position(0);
		 
		  genericBuff = bb3.asFloatBuffer();
		  genericBuff.put(genericTex);
		  genericBuff.position(0);
		  texCubeDrawn=1;
		
	}
	
		 	gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		 	gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, genericBuff);
			gl.glRotatef(180f,1f,0f,0f);
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, squareBuff); 
			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);			
			gl.glRotatef(90f,0f,1f,0f);
		    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);			
			gl.glRotatef(90f,0f,1f,0f);
		    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);			
			gl.glRotatef(90f,0f,1f,0f);
		    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);			
			gl.glRotatef(90f,0f,1f,0f); //OK back to start pos. Rot to top!
			gl.glRotatef(90f,1f,0f,0f);
		    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);			
			gl.glRotatef(180f,1f,0f,0f);
		    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);			
			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
} //end drawTexCube


//--------(GENogl)------------------------------------------------
public static void GENdrawTexCylinder (GL10 gl, int xtess,int ytess, float psx, float psy)
{   
double r1,r2,angle,x,y,y2,z,texx,texy,tex0;
double  tsx,tsy;
int i,j,ijptr,tptr,fptr ;
int numLat,numLong,conNumIndices;

//DHS aug '13: CLUGE: this assumes only one set of buffers
//and constant xytess!
if (texSphereDrawn == 0)
{
setupBuffers(xtess,ytess);
texSphereDrawn = 1;
}  

gl.glScalef(4f,3f,4f);
tsx   = 1.0/(double)(xtess+1);
//DHS android: this is quite different from ios...
tsy   = 2.0/(double)(ytess-2);
angle = 1.0001;
numLong = xtess;
numLat  = ytess;

texy = 0.0;
tex0 = 0.0;
	
ijptr = tptr = fptr = conNumIndices = 0;
//if (numLat > 1)
//   dnl1 = (double)(numLat-1);
//else
//	dnl1 = 0.1;   //BOGUS SHOULD NEVER HAPPEN
for (i=0;i<numLat-1;i++)
{
	//OK this makes a half-high cylinder; the top (or bottom) side is centered, so it's offset...
	//DHS ANDROID: NOTE coords have been changed from IOS version!!
	y=   -0.6 + -2.0 * (-0.5 +   ( (double)i)    /(double)(numLat-1));
	y2=  -0.6 + -2.0 * (-0.5 +   ( (double)(i+1))/(double)(numLat-1));
	texx = tex0;
	//r1 = (double)i/dnl1;
	//r2 = (double)(i+1)/dnl1;
	r1 = r2 = .3f;
	y*=psy;
	y2*=psy;
	for (j=0;j<numLong+1;j++) //dhs was nmLong+1
	{
		angle=(TWOPI * j)/(double)numLong; //this is our equatorial angle...
		x= psx*(double)Math.sin(angle)*r1;
		z= psy*(double)Math.cos(angle)*r1;
		shapeVertices[ijptr++] = (float)x;
		shapeVertices[ijptr++] = (float)y;
		shapeVertices[ijptr++] = (float)z;
		
		shapeTex[tptr++] = (float)texx; 
		shapeTex[tptr++] = (float)texy;
		
		x= psx*Math.sin(angle)*r2;
		z= psy*Math.cos(angle)*r2;
		texy+=tsy;
		shapeVertices[ijptr++] = (float)x;
		shapeVertices[ijptr++] = (float)y2;
		shapeVertices[ijptr++] = (float)z;
		
		shapeTex[tptr++] = (float)texx;  
		shapeTex[tptr++] = (float)texy;
		//DHS KLUGE!
		fptr++;
		texx+=tsx;
		texy-=tsy;
	} //end for j
	
	texy+=tsy; //get texture for next row down
}
//if (false && dookie2 % 10 == 0)
//{
//	Log.e(TAG,"fptr:" + fptr);
//}

//DHS KLUGE 
fptr-=numLong;
gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
//As soon as vertices are filled, stick'em into java constructs...
vectorBuff.put(shapeVertices);
vectorBuff.position(0);
textureBuff.put(shapeTex);
textureBuff.position(0);
gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vectorBuff);
gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuff);
conNumIndices = fptr;
gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, conNumIndices);
gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

} //end GENdrawTexCylinder

public static final int NUMCSEGS	= 60;

//--------(GENogl)------------------------------------------------
//  draws a fancy array of  ANIMATED cylinders... uses shape data
public static void GENdrawTexCylz(GL10 gl,int xtess,int ytess,float psx,float psy,
                    float x,float y,float z)
{   
	int loop,loop1,vptr,tptr,cptr,ctptr;
	float sx,sy;
	float angle,dangle,thickness,thickness2,xx,yy,zz,xt,yt;

	float[] cylzVerts 	= new float[3*(NUMCSEGS+2)];
	float[] cylzEVerts 	= new float[6*(NUMCSEGS+2)];
	float[] cylzETex 	= new float[4*(NUMCSEGS+2)];
	float[] cylzTex 	= new float[4*(NUMCSEGS+2)];

	
	thickness = 0.1f;
	thickness2 = 0.0999f;
	cylzVerts[0] = cylzVerts[1] = 0.0f; //Center
	cylzVerts[2] = thickness;   //thin coin shape
	cylzTex[0]   = cylzTex[1] = 0.5f;
	vptr = 3;
	tptr = 2;
	angle=0.0f;
	dangle = TWOPI/(float)NUMCSEGS;
    sx = x; //OSgetXYZpos(snum,0); 
    sy = y; //OSgetXYZpos(snum,1); 
    //sz = z; //OSgetXYZpos(snum,0); 
	//construct our data...
    for(loop=0;loop<NUMCSEGS+1;loop++)
    {
        cylzVerts[vptr] = (float)Math.cos(angle);
        cylzTex[tptr++] =  cylzVerts[vptr++]/2.0f + 0.5f;
        cylzVerts[vptr] = (float)Math.sin(angle);
        cylzTex[tptr++] = -cylzVerts[vptr++]/2.0f + 0.5f;
        cylzVerts[vptr++] = thickness;
        angle+=dangle;
    }
	//angle = 0.0;
	vptr = 3;
	tptr = 2;
	cptr = 0;
	ctptr=0;
	zz   = thickness2;
	//OK THE EDGE PART IS FUCKED UP! Crashes horribly on first draw
	for(loop=0;loop<NUMCSEGS+1;loop++)
	{ //get next xyz point from our circle above
		cylzEVerts[cptr++]=xx=cylzVerts[vptr++];
		xt = cylzTex[tptr++];
		if(xt >= 1.0f) xt = 0.999f;
		if(xt <= 0.0f) xt = 0.0f;
		cylzETex[ctptr++] = xt;
		cylzEVerts[cptr++]=yy=cylzVerts[vptr++];
		yt = cylzTex[tptr++];
		if(yt >= 1.0f) yt = 0.999f;
		if(yt <= 0.0f) yt = 0.0f;
		cylzETex[ctptr++] = yt;
		cylzEVerts[cptr++]=thickness2;
		vptr++;
		cylzEVerts[cptr++]= xx;   //2nd point in trimesh;same xy, diff z
		cylzETex[ctptr++] = xt;   //  same w/ textures too
		cylzEVerts[cptr++]= yy;
		cylzETex[ctptr++] = yt;
		cylzEVerts[cptr++]=-zz;
	}
	//NSLog(@" sxyz %f %f %f",sx,sy,sz);
    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	for(loop1=0;loop1<16;loop1++)//draw some coin-shaped objects
    {
        gl.glPushMatrix();  // 
        gl.glRotatef(60.0f*(float)loop1 * (float)sy,0.0f,1.0f,0.0f);
        gl.glTranslatef(8.0f*(float)sx,0.0f,0.0f);
        
        gl.glPushMatrix();
 
		vectorBuff.put(cylzVerts);
		vectorBuff.position(0);
		textureBuff.put(cylzTex);
		textureBuff.position(0);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vectorBuff);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuff);

        
        gl.glPushMatrix();
        for(loop=0;loop<2;loop++)
        {
        	gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0, 2+NUMCSEGS);
        	gl.glTranslatef(0,0,-2.0f*thickness);
        }
        gl.glPopMatrix();

		vectorBuff.put(cylzEVerts);
		vectorBuff.position(0);
		textureBuff.put(cylzETex);
		textureBuff.position(0);
        
        gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP,0,2*(NUMCSEGS+1)); //draw our rim
        gl.glPopMatrix();
        gl.glPopMatrix();
    }      //end loop1

		
	gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

		
	
} //end GENdrawTexCylz

//--------(GENogl)------------------------------------------------
//  draws a fancy array of cylinders...
void GENdrawTexToob(GL10 gl, int xtess,int ytess,float psx,float psy)
{   
	int loop;
	gl.glPushMatrix();
	gl.glRotatef(90.0f,1.0f,0.0f,0.0f);
	gl.glScalef(1.0f,4.0f,1.0f);
	gl.glTranslatef(0.0f,-8.8f,0.0f);
	for(loop=0;loop<7;loop++)
	{
		GENdrawTexCylinder(gl,xtess,ytess,psx,psy);
		gl.glTranslatef(0.0f,2.2f,0.0f);
	}
    
	gl.glPopMatrix();
} //end drawTexToob

 

 
//--------(GENogl)------------------------------------------------
//  All args are vestigial for now...
public static void GENdrawTexDiamond(GL10 gl, int xtess,int ytess,float psx,float psy)
{   
	float[] diaVertices = new float[] { 
			0.0f,1.4f,0.0f,
			0.0f,0.0f,1.4f,
			0.0f,1.4f,0.0f,
			1.4f,0.0f,0.0f,
			0.0f,1.4f,0.0f,
			0.0f,0.0f,-1.4f,
			0.0f,1.4f,0.0f,
			-1.4f,0.0f,0.0f,
			0.0f,1.4f,0.0f,
			0.0f,0.0f,1.4f,
			0.0f,0.0f,1.4f,
			0.0f,-1.4f,0.0f,
			1.4f,0.0f,0.0f,
			0.0f,-1.4f,0.0f,
			0.0f,0.0f,-1.4f,
			0.0f,-1.4f,0.0f,
			-1.4f,0.0f,0.0f,
			0.0f,-1.4f,0.0f,
			0.0f,0.0f,1.4f,
			0.0f,-1.4f,0.0f 
		    
		};

		float[] diaTex = new float[] { 
			0.0f,0.0f,
			0.0f,0.5f,
			.25f,0.0f,
			.25f,0.5f,
			.5f,0.0f,
			.5f,0.5f,
			.75f,0.0f,
			.75f,0.5f,
			1.0f,0.0f,
			1.0f,0.5f,
			0.0f,0.5f,
			0.0f,1.0f,
			.25f,0.5f,
			.25f,1.f,
			.5f,0.5f,
			.5f,1.f,
			.75f,0.5f,
			.75f,1.f,
			1.0f,0.5f,
			1.0f,1.f 
		};

		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		vectorBuff.put(diaVertices);
		vectorBuff.position(0);
		textureBuff.put(diaTex);
		textureBuff.position(0);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vectorBuff);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuff);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 20);

		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

} //end drawTexDiamond



//--------(GENogl)------------------------------------------------
static void setupBuffers(int xtess,int ytess)
{
	int vlen,tlen;
    vlen = (4* 3 * 2 *  (xtess+2) * (ytess+2)); 
    shapeVertices = new float[vlen] ;
	tlen = (4* 2 * 2 * (xtess+2) * (ytess+2));
	shapeTex = new float[tlen];
	bbv = ByteBuffer.allocateDirect( vlen*4);
	bbv.order(ByteOrder.nativeOrder());
	vectorBuff = bbv.asFloatBuffer();
	bbt = ByteBuffer.allocateDirect( tlen*4);
	bbt.order(ByteOrder.nativeOrder());
	textureBuff = bbt.asFloatBuffer();

}

//--------(GENogl)------------------------------------------------
//  args are: x/y tesselation, xy size of sphere
// NOTE! if x or y tess change dynamically, this will krash!
// DHS Sept 2012 oogie ipad seems to load tex upside down. FIX!
//--------(SpareOGL)------------------------------------------------------------------
//args are: x/y tesselation, xy size of sphere
static void GENdrawTexSphere (GL10 gl, int xtess,int ytess, float psx, float psy)
{   
double r1,r2,angle,x,y,y2,z,texx,texy,tex0;
double  tsx,tsy;
int i,j,ijptr,tptr,fptr ;
int numLat,numLong,sphNumIndices;

//DHS 2/13/12: allocate vertex/texture space for sphere/cylinder/torus/etc...
if (texSphereDrawn == 0)
{
	setupBuffers(xtess,ytess);
	texSphereDrawn = 1;
}  

tsx   = 1.0/(double)(xtess+0);   // was +1
tsy   = 1.0/(double)(ytess-2) ; //was +1
angle = 1.0001;
numLat = xtess;
numLong = ytess;

texy = 0.0;
tex0 = 0.0;

//gl.glScalef(1.5f,1.5f,1.5f);

ijptr = tptr = fptr = sphNumIndices = 0;
for (i=0;i<numLat-1;i++)
{ 	y=  Math.cos((3.1415*(double)i)/(double)(numLat-1));
	y2= Math.cos((3.1415*(double)(i+1))/(double)(numLat-1));
	texx = tex0;
	
	r1=(double)Math.sqrt(1.0 - y*y);
	r2=(double)Math.sqrt(1.0-y2*y2);
	y*=psy;
	y2*=psy;
	for (j=0;j<numLong+1;j++)
	{
		angle=(TWOPI * j)/(double)numLong;
		x= psx*(double)Math.sin(angle)*r1;
		z= psy*(double)Math.cos(angle)*r1;
		//sphFaces[fptr++]     = ijptr; 
		//NSLog(@"1sph face [%d]   vert   %f %f %f tx %f %f",ijptr,x,y,z,1.0-texx,texy);			
		shapeVertices[ijptr++] = (float)x;
		shapeVertices[ijptr++] = (float)y;
		shapeVertices[ijptr++] = (float)z;
		
//		shapeTex[tptr++] = 1.0f - (float)texx; //DHS 4/20 this aligns textures; udderwise bkwds...
		shapeTex[tptr++] = (float)texx; //Android feb 13 2012: tex wuz bkwds?
		shapeTex[tptr++] = (float)texy;
		
		x= psx*Math.sin(angle)*r2;
		z= psy*Math.cos(angle)*r2;
		texy+=tsy;
		//sphFaces[fptr++]     = ijptr; 
		shapeVertices[ijptr++] = (float)x;
		shapeVertices[ijptr++] = (float)y2;
		shapeVertices[ijptr++] = (float)z;
		
		//shapeTex[tptr++] = 1.0f - (float)texx; //DHS 4/20 this aligns textures; udderwise bkwds...
		shapeTex[tptr++] = (float)texx; //Android feb 13 2012: tex wuz bkwds?
		shapeTex[tptr++] = (float)texy;
		fptr+=2;
		texx+=tsx;
		texy-=tsy;
	} //end for j
	
	texy+=tsy; //get texture for next row down
}
gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
//As soon as vertices are filled, stick'em into java constructs...
vectorBuff.put(shapeVertices);
vectorBuff.position(0);
textureBuff.put(shapeTex);
textureBuff.position(0);
gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vectorBuff);
gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuff);
sphNumIndices = fptr;
gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, sphNumIndices);
gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
} //end GENdrawTexSphere



//--------(SpareOGL)------------------------------------------------------------------
//args are:  x/y tesselation, xy size of sphere
//DHS Torus has noticeable SEAM at the 12-oclock position!!!
static void GENdrawTexTorus (GL10 gl, int xtess,int ytess, float psx, float psy)
{   
double angle,angle2,astep,astep2,rr,r1,r2,x,y,z,texx,texy,tex0;
double  tsx,tsy;
int i,j,ijptr,tptr,fptr ;
int numLat,numLong,torNumIndices;

//DHS aug '13: CLUGE: this assumes only one set of buffers
//   and constant xytess!
if (texSphereDrawn == 0)
{
	setupBuffers(xtess,ytess);
	texSphereDrawn = 1;
}  
  
tsx   = 1.0/(double)(xtess );
tsy   = 1.0/(double)(ytess );
angle = 1.0001;
numLong = xtess;
numLat  = ytess;
astep = TWOPI / (double)ytess;
astep2 = TWOPI /(double)xtess; 
texy = 0.0;
tex0 = 0.0;
//Our toroidal radii, major followed by minor
r1 = 1.0;
r2 = 0.55;
ijptr = tptr = fptr = torNumIndices = 0;
angle = 0.0;
//NSLog(@"torus....");
	for (i=0;i<numLong ;i++) //this is the big loop around the torus...
{ 	
	texx = tex0;
	angle2 = 0.0;
	for (j=0;j<numLat+1;j++)
	{
		rr = r1 + r2*Math.sin(angle2);
		x  = rr * Math.sin(angle);
		y  = rr * Math.cos(angle);
		z  = r2 * Math.cos(angle2);
		//torFaces[fptr++]     = ijptr; 
		shapeVertices[ijptr++] = (float)x;
		shapeVertices[ijptr++] = (float)y;
		shapeVertices[ijptr++] = (float)z;
		
		
		// NSLog(@"1shapexyz[%d,%d] angles %f %f: %f %f %f",i,j,angle,angle2,x,y,z);			
		shapeTex[tptr++] = 1.0f - (float)texx; //DHS 4/20 this aligns textures; udderwise bkwds...
		shapeTex[tptr++] = (float)texy;
		
		rr = r1 + r2*Math.sin(angle2);
		x  = rr * Math.sin((angle+astep));
		y  = rr * Math.cos((angle+astep));
		texy+=tsy;
		//torFaces[fptr++]     = ijptr; 
		shapeVertices[ijptr++] = (float)x;
		shapeVertices[ijptr++] = (float)y;
		shapeVertices[ijptr++] = (float)z;
		//NSLog(@"2torxyz[%d,%d] angles %f %f: %f %f %f",i,j,angle,angle2,x,y,z);			
		
		shapeTex[tptr++] = 1.0f - (float)texx; //DHS 4/20 this aligns textures; udderwise bkwds...
		shapeTex[tptr++] = (float)texy;
		fptr+=2;
		texx+=tsx;
		texy-=tsy;
		angle2+= astep2;
	} //end for j
	angle += astep;
	
	texy+=tsy; //get texture for next row down
}
	gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	//As soon as vertices are filled, stick'em into java constructs...
	vectorBuff.put(shapeVertices);
	vectorBuff.position(0);
	textureBuff.put(shapeTex);
	textureBuff.position(0);
	gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vectorBuff);
	gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuff);
	torNumIndices = fptr;
	gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, torNumIndices);
	gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
} //end GENdrawTexTorus



//--------(GENogl)------------------------------------------------
public static void GENdrawWireGrid(GL10 gl,float mx,float my,float mz)  
{   int loop;
    float[] cvecs = new float[6];
	cvecs[0] = -2.0f;
	cvecs[1] = -2.0f;
	cvecs[2] = -1.0f;
	cvecs[3] =  2.0f;
	cvecs[4] = -2.0f;
	cvecs[5] = -1.0f;
	gl.glPushMatrix();
	gl.glLoadIdentity();
	gl.glTranslatef(mx,my,mz);
	gl.glLineWidth(2.0f);
	//THIS DONT WERK gl.glEnableClientState(GL_VERTEX_ARRAY);
	//THIS DONT WERK gl.glVertexPointer(3, GL_FLOAT, 0,  cvecs);
	//Draw horizontal lines...
	for(loop=0;loop<12;loop++)
	{cvecs[1]+=0.25;
        cvecs[4]+=0.25;
      //THIS DONT WERK gl.glDrawArrays(GL_LINES, 0,  2);  //horiz line
	}
	cvecs[0] = -2.0f;
	cvecs[1] = -2.0f;
	cvecs[2] = -1.0f;
	cvecs[3] = -2.0f;
	cvecs[4] =  2.0f;
	cvecs[5] = -1.0f;
	//Draw Vertical lines...
	for(loop=0;loop<12;loop++)
	{cvecs[0]+=0.25;
        cvecs[3]+=0.25;
      //THIS DONT WERK gl.glDrawArrays(GL_LINES, 0,  2);  //horiz line
	}
	//THIS DONT WERK gl.glDisableClientState(GL_VERTEX_ARRAY);
	gl.glPopMatrix();
	
} //end drawWireGrid


//--------(GENogl)------------------------------------------------
//NEEDS WORK!
public static void GENdrawWireSphere(GL10 gl)
{
    int loop,cptr,fptr,circNumIndices;
    float angle,dangle;
    float sin30,cos30,sin60,cos60;
    float[] cvecs = new float[3*(MAX_WSPHERE_SEGS+3)];

  //KLUGE, REMOVE
    if (texSphereDrawn == 0)
    {
    	setupBuffers(32,32);
    	texSphereDrawn = 1;
    }  

    
    //first make a circle
    gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
    angle = 0.2f;
    dangle = TWOPI / MAX_WSPHERE_SEGS;
    cptr = 0;
    fptr = 0;
    for(loop=0;loop<MAX_WSPHERE_SEGS+1;loop++)
    {   cvecs[cptr++] = (float)Math.cos(angle);
        cvecs[cptr++] = (float)Math.sin(angle);
        cvecs[cptr++] = 0.0f;
        fptr++;
        angle+=dangle;
    }
    circNumIndices = fptr;
    gl.glPushMatrix();
    gl.glScalef(WSPHERE_SIZE,WSPHERE_SIZE,WSPHERE_SIZE);
    vectorBuff.put(cvecs);
    vectorBuff.position(0);
    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vectorBuff);
    //Log.e(TAG, "Drawit:" + fptr);
    gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, circNumIndices);
    
    gl.glPushMatrix();
    sin30 = (float)Math.sin(DEGREES_TO_RADIANS * 30.0);
    sin60 = (float)Math.sin(DEGREES_TO_RADIANS * 60.0);
    cos30 = (float)Math.cos(DEGREES_TO_RADIANS * 30.0);
    cos60 = (float)Math.cos(DEGREES_TO_RADIANS * 60.0);
    //First, draw longitudes
    for(loop=0;loop<5;loop++)
    {
        gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, circNumIndices);
    	gl.glRotatef( 30.0f, 0,1,0 );
    }
    gl.glPopMatrix();
    //Now draw latitudes
    gl.glPushMatrix();
    gl.glRotatef( 90,1,0,0 );
    gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, circNumIndices);
    gl.glPushMatrix();
    gl.glScalef(cos30,cos30,1.0f);
    gl.glTranslatef(0.0f, 0.0f, sin30);
    gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, circNumIndices);
    gl.glTranslatef(0.0f, 0.0f, -2.0f*sin30);
    gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, circNumIndices);
    gl.glPopMatrix();
    gl.glScalef(cos60 ,cos60 ,1.0f );
    gl.glTranslatef(0.0f, 0.0f, sin60);
    gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, circNumIndices);
    gl.glTranslatef(0.0f, 0.0f,  -2.0f*sin60 );
    gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, circNumIndices);
  //THIS DONT WERK gl.glDrawArrays(GL_LINE_LOOP, 0,  MAX_WSPHERE_SEGS);  
    gl.glPopMatrix();
    
    gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    gl.glPopMatrix();
        
} //end drawWireSphere
               

//=====OOGIE-ANDROID=============================================2012
public ByteBuffer extract(Bitmap bmp)
{
			int w,h,bbsize,err;
			ByteBuffer bb =  null;
			w = bmp.getWidth();
			h = bmp.getHeight();
			bbsize = h*w*4;
			err=0;
			//Log.e(TAG," extract:try to alloc:" + bbsize);
			try{
				bb = ByteBuffer.allocateDirect(bbsize);
			}
			 catch (Exception e)
	         {
	             Log.e(TAG, "Exception: " + e.toString());
	             err=1;
	         }
			if (err == 0)
			{
				bb.order(ByteOrder.BIG_ENDIAN);
				IntBuffer ib = bb.asIntBuffer();
				// Convert ARGB -> RGBA
				for (int y = h - 1; y > -1; y--)
				{

					for (int x = 0; x < w; x++)
					{
						int pix = bmp.getPixel(x, h - y - 1);
						int alpha = ((pix >> 24) & 0xFF);
						int red   = ((pix >> 16) & 0xFF);
						int green = ((pix >> 8) & 0xFF);
						int blue  = ((pix) & 0xFF);
						ib.put(red << 24 | green << 16 | blue << 8 | alpha);
					}
				}
				bb.position(0);
				//Log.e(TAG," extractbmp w:" + w + " h:" + h);
			}
		return bb;
}  //end extract

/** This will be used to pass in model texture coordinate information. */
private int mTextureCoordinateHandle;

/** How many bytes per float. */
private final int mBytesPerFloat = 4;	

/** Size of the position data in elements. */
private final int mPositionDataSize = 3;	

/** Size of the color data in elements. */
private final int mColorDataSize = 4;	

/** Size of the normal data in elements. */
private final int mNormalDataSize = 3;

/** Size of the texture coordinate data in elements. */
private final int mTextureCoordinateDataSize = 2;
/** This is a handle to our texture data. */
private int mTextureDataHandle;
//private final FloatBuffer mCubePositions;
//private final FloatBuffer mCubeColors;
//private final FloatBuffer mCubeNormals;
//private final FloatBuffer mCubeTextureCoordinates;

final float[] cubeTextureCoordinateData =
{												
		// Front face
		0.0f, 0.0f, 				
		0.0f, 1.0f,
		1.0f, 0.0f,
		0.0f, 1.0f,
		1.0f, 1.0f,
		1.0f, 0.0f,				
		
};

// X, Y, Z
final float[] cubePositionData =
{
		// In OpenGL counter-clockwise winding is default. This means that when we look at a triangle, 
		// if the points are counter-clockwise we are looking at the "front". If not we are looking at
		// the back. OpenGL has an optimization where all back-facing triangles are culled, since they
		// usually represent the backside of an object and aren't visible anyways.
		
		// Front face
		-1.0f, 1.0f, 1.0f,				
		-1.0f, -1.0f, 1.0f,
		1.0f, 1.0f, 1.0f, 
		-1.0f, -1.0f, 1.0f, 				
		1.0f, -1.0f, 1.0f,
		1.0f, 1.0f, 1.0f,
};

//--------(OGLUI)------------------------------------------------------------
// INCOMPLETE! NEEDS WORK!
//OK this uses automated glyph-finding.  Uses canned Impact font...
//top row of texture is uppercase + numerals
//bottom row is lowercase + punc...
public void drawIText(float tx, float ty, String txtstr) 
{
int loop,tchar,lowercase,length;
float gdel,glyphgap,ggu;
float ttop,tbot;
FloatBuffer gvBuff;
FloatBuffer gtBuff;
float xoffset = 0.0f;

float[] lglyphTexcoords = new float[] {
		  0f, 0.2f,
		.06f, 0.2f,
		  0f, 0.8f,
		.06f, 0.8f,
			 0.0f,0.0f,
		 0.0f,0.0f,
		};
float[] lglyphVerts = new float[] {
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		 0.0f,  0.0f, 1f, 
		};
ByteBuffer bb = ByteBuffer.allocateDirect(lglyphVerts.length*4);
bb.order(ByteOrder.nativeOrder());
gvBuff = bb.asFloatBuffer();

ByteBuffer bb2 = ByteBuffer.allocateDirect(lglyphTexcoords.length*4);
bb2.order(ByteOrder.nativeOrder());
gtBuff = bb2.asFloatBuffer();


length = txtstr.length(); 
//Log.e(TAG," txt:" + txtstr + ",len:" + length);

//get glyph started; we will change it for different #'s
for(loop=0;loop<12;loop++) lglyphTexcoords[loop] = glyphTexcoords[loop];

GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
// Bind the texture to this unit.
GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

FloatBuffer locTexCoordinates;
FloatBuffer locXYZCoordinates;


locTexCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * mBytesPerFloat)
.order(ByteOrder.nativeOrder()).asFloatBuffer();

locXYZCoordinates = ByteBuffer.allocateDirect(cubePositionData.length * mBytesPerFloat)
.order(ByteOrder.nativeOrder()).asFloatBuffer();

// Pass in the position information
// DHS UNCOMMENT GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
// DHS UNCOMMENT 		0, mCubePositions);        
// DHS UNCOMMENT GLES20.glEnableVertexAttribArray(mPositionHandle);        

	//gl.glTranslatef( tx , ty,  0.0f);
glyphgap = 0.001f;

xoffset = 0.0f;
length=1;
for(loop=0;loop<length;loop++) 
{
	tchar = txtstr.charAt(loop); 
	Log.d(TAG,"...nexttchar " + tchar);
	if (tchar != 32) //lowercase is above 90!
	{
		lowercase=0;
		if ((tchar < 47)||(tchar > 96)) lowercase = 1;
		if (lowercase == 0)
		{   //Top half of texture
			ttop = 0.02f;
			tbot = 0.40f;
			//gl.glBindTexture(GL10.GL_TEXTURE_2D, mTextureDataHandle); //IMPACT_TEX );
			if (tchar >= 65)  tchar-=65; 
			else if (tchar > 47 && tchar < 58) //number
				tchar = (tchar-48) + 26; //numbers are after alpha
		}
		else
		{
			//bottom half
			ttop = 0.52f;
			tbot = 0.95f;
			if (tchar >= 97)  tchar-=97; 
			else if (tchar < 47) tchar-=(33-26); //punc starts at 33, offset to glyph 26
			//gl.glBindTexture(GL10.GL_TEXTURE_2D,mTextureDataHandle);  //IMPACT_TEX );
		}
		
		if (tchar < 0) tchar = 0;  
		Log.d(TAG,"...lc "+ lowercase + " finalchar " + tchar);
		//NOTE: if letter is past 'A', we need to account for a little
		// 'glyphgap'...
		ggu = 0.0f;
		if (tchar > 0) ggu = glyphgap;
		
		if (lowercase == 0)
		{   //left side , right side
			lglyphTexcoords[0] = lglyphTexcoords[2] = lglyphTexcoords[6]  =  fgaps[tchar]+ggu;
			lglyphTexcoords[4] = lglyphTexcoords[8] = lglyphTexcoords[10] =  fgaps[tchar+1];
			gdel = 3.0f * (fgaps[tchar+1] - fgaps[tchar]); 
		}
		else
		{  //left side,right side
			lglyphTexcoords[0] = lglyphTexcoords[2] = lglyphTexcoords[6]  =  fgaps2[tchar]+ggu;
			lglyphTexcoords[4] = lglyphTexcoords[8] = lglyphTexcoords[10] =  fgaps2[tchar+1];
			gdel = 2.0f * (fgaps2[tchar+1] - fgaps2[tchar]);
		}
		lglyphTexcoords[1] = lglyphTexcoords[5] = lglyphTexcoords[11] =  ttop;
		lglyphTexcoords[3] = lglyphTexcoords[7] = lglyphTexcoords[9]  =  tbot;
		
		lglyphVerts[0] = lglyphVerts[3]   = lglyphVerts[9]  =  xoffset;  //Left
		lglyphVerts[1] = lglyphVerts[7]   = lglyphVerts[16] =  ttop;  //Top
		lglyphVerts[6] = lglyphVerts[12]  = lglyphVerts[15] =  xoffset+gdel;  //Right
		lglyphVerts[4] = lglyphVerts[10]  = lglyphVerts[13] =  tbot;  //Bottom
		gvBuff.put(lglyphVerts);
		gvBuff.position(0);
		gtBuff.put(lglyphTexcoords);
		gtBuff.position(0);

		 
		
		Log.d(TAG,"--Left/Right:" + xoffset + "," + (xoffset+gdel));
		Log.d(TAG,"--Top/Bottom:" + ttop + "," + tbot);
		//for(int lll=0;lll<4;lll++)
		//{
		//	Log.d(TAG,"..." + lglyphTexcoords[2*lll] + "," + lglyphTexcoords[1 + 2*lll]);
		//}
			
        // Pass in the texture coordinate information
		locTexCoordinates.put(lglyphTexcoords).position(0);
		locTexCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 
        		0, locTexCoordinates);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        
        locXYZCoordinates.put(lglyphVerts).position(0);  //was cubePositionData
		locXYZCoordinates.position(0);
		
		  GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6); //last arg was 36, full cube?   
		// Log.e(TAG,"loop/tchar "+ loop + "," + tchar );
		//gl.glTranslatef( 1.0f * gdel , 0.0f,  0.0f);
		  xoffset+=gdel;
	}
	//else //spacebar
		//gl.glTranslatef( 0.05f , 0.0f,  0.0f);
	
}

	

} //end drawIText






//=====OOGIE-ANDROID=============================================2012
// NOTE: we need to set up res!
//    	   res = context.getResources();       

public void setupFont()
{

	int lgptr,loop;
	int ri,gi,bi;
	int gptr,gptr2,limit;
	Bitmap bmp;
	int ww,hh,bptr;
	float fuckwid;
	char rc,gc,bc;

	//int nb = textureNumbers.length;
	//Log.e(TAG,"load " + nb+ " textures..");
	  bmp = BitmapFactory.decodeResource(res, R.drawable.oog2_impact_t);
	 
	if (bmp == null) //bogus bitmap?
	{
		Log.e(TAG," ERROR setupFont, no bmp");
		return;  //get out of here !
	}
	//Log.e(TAG," get bmp size???");
	ww = bmp.getWidth();
	hh = bmp.getHeight();
	ByteBuffer bb = this.extract(bmp);
	if (bb == null) return;
	limit  = bb.limit();
	// Log.e(TAG," font wid/hit: " + ww + "/" + hh + " lim:" + limit);
	//OK. we were using 512 as assumed width in fgaps[] calc below,
	//  but had to switch to using an unpredictable width?!?!?! of 427??
	//  even though original file is 512.  Android must compress or such?
	fuckwid = (float)ww;
	bptr = 0;

	//i = 0;
	gptr = 0;
	lgptr = -99;
 	//Look at top row. Find where our letters are (non-white pixels mark'em)
	fgaps[gptr] = 0.001f;
	gptr++;
	//parse out top row, find delimiters (blue dots)
	//  these tell us where each letter begins/ends
	//  Log.e(TAG,"TOP ROW........");
	for (loop=0;loop<ww;loop++)
	{
		ri = bb.getChar(bptr++);
		gi = bb.getChar(bptr++);
		bi = bb.getChar(bptr++);
		bptr++;
		rc = (char)ri;
		gc = (char)gi;
		bc = (char)bi;
 	    //  Log.e(TAG,"..rgba[" + loop + "]: " + ri + "," + gi + "," + bi + "," + a  );
		if (rc != 65535 || gc != 65535 || bc != 65535) //got non-white? Markit!
//		if (rc < 100 || gc < 100 || bc < 100) //got non-white? Markit!
		{
			gaps[gptr] = loop;
			fgaps[gptr] = (float)loop/fuckwid;
	 		//  Log.e(TAG," achar[" + loop + "] gap: " + gaps[gptr] + ", fgap:" + fgaps[gptr]);
		    //Make sure we don't get a 'wide' pixel...
			if ((loop  - lgptr > 5) && gptr < MAXGLYPHGAP)
			{
				gptr++;
				lgptr = loop;
			}
		}  //end if rc
	}     //end for loop
	//  Log.e(TAG,"BOTTOM ROW........");
	//Look at bottom row. Find where our letters are (non-white pixels mark'em)
	bptr = 4 * ww *(hh - 1);
	gptr2 = 0;
	lgptr = -99;
	fgaps2[gptr2] = 0.001f;
	gptr2++;
	for (loop=0;loop<ww && bptr < limit-8;loop++)
	{
		ri = bb.getChar(bptr++);
		gi = bb.getChar(bptr++);
		bi = bb.getChar(bptr++);
		bptr++;
		rc = (char)ri;
		gc = (char)gi;
		bc = (char)bi;
		//  Log.e(TAG,"..rgb[" + loop + "]: " + ri + "," + gi + "," + bi  );
//		if (rc < 100 || gc < 100 || bc < 100) //got non-white? Markit!
		if (rc != 65535 || gc != 65535 || bc != 65535) //got non-white? Markit!
		{
			gaps2[gptr2] = loop;
			fgaps2[gptr2] = (float)loop/fuckwid;
	 		//  Log.e(TAG," bchar[" + loop + "] gap2: " + gaps2[gptr2] + ", fgap2:" + fgaps2[gptr2]);
			//Make sure we don't get a 'wide' pixel...
			if ((loop  - lgptr > 5) && gptr2 < MAXGLYPHGAP)
			{
				gptr2++;
				lgptr = loop;
			}
		}  //end if rc
	}     //end for loop
}    //end setupFont



            
} //end class GENogl
//END GENogl stuff....
