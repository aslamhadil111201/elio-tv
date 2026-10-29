package id.matin.nusatv;
public class VideoLayoutTest {
 static int checks;
 static void expect(int vw,int vh,float w,float h,int mode,int ew,int eh){int[] s=VideoLayout.size(vw,vh,w,h,mode);if(s[0]!=ew||s[1]!=eh)throw new AssertionError(s[0]+"x"+s[1]+" expected "+ew+"x"+eh);checks++;}
 public static void main(String[] args){
  expect(2400,1080,1920,1080,0,1920,1080);
  expect(2400,1080,1920,1080,1,2400,1350);
  expect(2400,1080,1920,1080,2,2400,1080);
  expect(1920,1080,640,480,0,1440,1080);
  expect(1920,1080,640,480,1,1920,1440);
  expect(1920,1080,640,480,2,1920,1080);
  expect(1920,1080,1920,1080,0,1920,1080);
  expect(1920,1080,1920,1080,1,1920,1080);
  expect(1920,1080,720*(16f/15),576,0,1440,1080);
  expect(1080,1920,1920,1080,0,1080,608);
  expect(1080,1920,1920,1080,1,3413,1920);
  expect(0,1080,1920,1080,1,0,0);
  expect(1920,0,1920,1080,1,0,0);
  expect(1920,1080,0,0,0,1920,1080);
  expect(1920,1080,Float.NaN,1080,1,1920,1080);
  expect(1920,1080,Float.POSITIVE_INFINITY,1080,1,1920,1080);
  expect(1920,1080,1920,-1,0,1920,1080);
  expect(2400,1080,1920,1080,99,1920,1080);
  for(int mode=0;mode<3;mode++)for(int[] view:new int[][]{{2400,1080},{1280,720},{1920,1080}})for(int[] video:new int[][]{{640,480},{1920,1080},{1080,1920}}){int[] s=VideoLayout.size(view[0],view[1],video[0],video[1],mode);if(mode==0&&(s[0]>view[0]||s[1]>view[1]))throw new AssertionError("Fit exceeds viewport");if(mode==1&&(s[0]<view[0]||s[1]<view[1]))throw new AssertionError("Zoom leaves gaps");if(mode==2&&(s[0]!=view[0]||s[1]!=view[1]))throw new AssertionError("Stretch leaves gaps");checks++;}
  if(VideoLayout.mode(-1)!=0||VideoLayout.mode(3)!=0)throw new AssertionError("Invalid preference");checks++;
  System.out.println(checks+" video layout checks passed");
 }
}
