package id.matin.nusatv;
/** Shared canvas hit geometry, in 1280 x 720 design coordinates. */
final class TouchNavigation {
 static final int COLUMNS=6, ROW_HEIGHT=134;
 static float maxScroll(int count){return Math.max(0,((count+COLUMNS-1)/COLUMNS-1)*ROW_HEIGHT);}
 static float clampScroll(float value,int count){return Math.max(0,Math.min(value,maxScroll(count)));}
 static boolean inGrid(float x,float y){return x>=44&&x<1240&&y>=HomeLayout.GRID_Y&&y<HomeLayout.GRID_BOTTOM;}
 static int gridIndex(float x,float y,float scroll,int count){if(!inGrid(x,y))return -1;float localX=x-44,localY=y-HomeLayout.GRID_Y+scroll;int col=(int)(localX/201),row=(int)(localY/ROW_HEIGHT);if(col>=COLUMNS||localX-col*201>=191||localY-row*ROW_HEIGHT>=126)return -1;int i=row*COLUMNS+col;return i>=0&&i<count?i:-1;}
 static int drawerFirst(int index,int count){return Math.max(0,Math.min(index-4,count-9));}
 static int drawerIndex(float x,float y,int selected,int count){if(x<35||x>=383||y<118||y>=667)return -1;int row=(int)((y-118)/61);if(row>=9||y-118-row*61>=55)return -1;int index=drawerFirst(selected,count)+row;return index<count?index:-1;}
}
