package id.matin.nusatv;

/** Both TV canvas and native film views use the same undistorted design frame. */
final class DisplayViewport {
 static final int WIDTH=1280,HEIGHT=720;
 static float scale(int width,int height){return Math.max(.01f,Math.min(width/(float)WIDTH,height/(float)HEIGHT));}
 static float left(int width,int height){return (width-WIDTH*scale(width,height))/2;}
 static float top(int width,int height){return (height-HEIGHT*scale(width,height))/2;}
}
