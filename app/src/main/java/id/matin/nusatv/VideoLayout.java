package id.matin.nusatv;

/** Pixel geometry shared by phone and TV playback; independent of Android views. */
final class VideoLayout {
 static final int FIT=0, ZOOM=1, STRETCH=2;
 static int mode(int value){return value>=FIT&&value<=STRETCH?value:FIT;}
 static int[] size(int viewportWidth,int viewportHeight,float videoWidth,float videoHeight,int mode){
  if(viewportWidth<=0||viewportHeight<=0)return new int[]{0,0};
  if(mode(mode)==STRETCH||Float.isNaN(videoWidth)||Float.isInfinite(videoWidth)||Float.isNaN(videoHeight)||Float.isInfinite(videoHeight)||videoWidth<=0||videoHeight<=0)
   return new int[]{viewportWidth,viewportHeight};
  double scale=mode(mode)==ZOOM?Math.max(viewportWidth/(double)videoWidth,viewportHeight/(double)videoHeight):Math.min(viewportWidth/(double)videoWidth,viewportHeight/(double)videoHeight);
  double w=videoWidth*scale,h=videoHeight*scale;
  if(w>Integer.MAX_VALUE||h>Integer.MAX_VALUE)return new int[]{viewportWidth,viewportHeight};
  return new int[]{Math.max(1,(int)Math.round(w)),Math.max(1,(int)Math.round(h))};
 }
}
