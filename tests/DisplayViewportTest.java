package id.matin.nusatv;
public final class DisplayViewportTest {
 static int count;static void near(float a,float b){count++;if(Math.abs(a-b)>.01)throw new AssertionError(a+" != "+b);}
 public static void main(String[] args){
  near(DisplayViewport.scale(1600,720),1);near(DisplayViewport.left(1600,720),160);near(DisplayViewport.top(1600,720),0);
  near(DisplayViewport.scale(1920,1080),1.5f);near(DisplayViewport.left(1920,1080),0);near(DisplayViewport.top(1920,1080),0);
  near(DisplayViewport.scale(1280,720),1);near(DisplayViewport.scale(960,540),.75f);near(DisplayViewport.scale(2400,1080),1.5f);near(DisplayViewport.left(2400,1080),240);
  near(DisplayViewport.scale(1080,1920),.84375f);near(DisplayViewport.top(1080,1920),656.25f);
  int[][] sizes={{1600,720},{1920,1080},{960,540},{2400,1080},{1080,1920}};
  for(int[] size:sizes){float s=DisplayViewport.scale(size[0],size[1]);near(DisplayViewport.WIDTH*s+2*DisplayViewport.left(size[0],size[1]),size[0]);near(DisplayViewport.HEIGHT*s+2*DisplayViewport.top(size[0],size[1]),size[1]);near((HomeLayout.PREVIEW_X*s+DisplayViewport.left(size[0],size[1])-DisplayViewport.left(size[0],size[1]))/s,HomeLayout.PREVIEW_X);}
  System.out.println(count+" viewport checks passed: phone 20:9, TV 16:9, portrait, uniform scale and coordinate round trips.");
 }
}
