package id.matin.nusatv;

/** TV home uses one visible station row so the live preview keeps its 16:9 frame. */
final class HomeLayout {
 static final int PREVIEW_X=44,PREVIEW_Y=96,PREVIEW_W=548,PREVIEW_H=308;
 static final int GRID_Y=511,GRID_BOTTOM=644,ROW_HEIGHT=134;
 static final int[] NAV={0,6,4,5,7};
 static int filterStep(int current,int delta){return Math.max(0,Math.min(4,current+delta));}
 static int navStep(int current,int delta){
  int at=0;for(int i=0;i<NAV.length;i++)if(NAV[i]==current)at=i;
  return NAV[(at+delta+NAV.length)%NAV.length];
 }
 static int heroHit(float x,float y){
  if(y<326||y>=374)return -1;
  if(x>=624&&x<852)return 0;
  if(x>=868&&x<920)return 1;
  if(x>=936&&x<1064)return 2;
  return -1;
 }
}
