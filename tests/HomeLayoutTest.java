package id.matin.nusatv;
public final class HomeLayoutTest {
 static int n;static void ok(boolean b){n++;if(!b)throw new AssertionError("home "+n);}
 public static void main(String[] args){
  ok(HomeLayout.navStep(0,1)==6);ok(HomeLayout.navStep(6,1)==4);ok(HomeLayout.navStep(0,-1)==7);ok(HomeLayout.navStep(7,1)==0);
  ok(HomeLayout.heroHit(624,326)==0);ok(HomeLayout.heroHit(851,373)==0);ok(HomeLayout.heroHit(858,350)==-1);ok(HomeLayout.heroHit(880,350)==1);ok(HomeLayout.heroHit(950,350)==2);ok(HomeLayout.heroHit(700,375)==-1);
  ok(TouchNavigation.gridIndex(45,512,0,13)==0);ok(TouchNavigation.gridIndex(245,512,0,13)==1);ok(TouchNavigation.gridIndex(238,512,0,13)==-1);
  ok(TouchNavigation.gridIndex(45,512,134,13)==6);ok(TouchNavigation.gridIndex(45,512,268,13)==12);ok(TouchNavigation.gridIndex(245,512,268,13)==-1);
  ok(TouchNavigation.maxScroll(6)==0);ok(TouchNavigation.maxScroll(7)==134);ok(TouchNavigation.maxScroll(13)==268);ok(TouchNavigation.clampScroll(9999,13)==268);
  ok(!TouchNavigation.inGrid(50,400));ok(!TouchNavigation.inGrid(50,650));ok(TouchNavigation.drawerIndex(50,120,0,12)==0);
  System.out.println(n+" home checks passed: visible navigation, hero actions, scroll limits, final partial row and drawer hit geometry.");
 }
}
