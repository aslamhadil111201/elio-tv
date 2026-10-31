package id.matin.nusatv;
import java.util.*;
public final class RemoteFilterPreviewTest {
 static int checks;static void ok(boolean b){checks++;if(!b)throw new AssertionError("remote/preview "+checks);}
 public static void main(String[] args){
  int focus=0;focus=HomeLayout.filterStep(focus,1);ok(focus==1);focus=HomeLayout.filterStep(focus,1);ok(focus==2);
  ok(HomeLayout.filterStep(0,-1)==0);ok(HomeLayout.filterStep(4,1)==4);ok(HomeLayout.filterStep(2,-1)==1);
  ok(PreviewPolicy.shouldRun(false,true,false,true));
  ok(!PreviewPolicy.shouldRun(true,true,false,true));ok(!PreviewPolicy.shouldRun(false,false,false,true));
  ok(!PreviewPolicy.shouldRun(false,true,true,true));ok(!PreviewPolicy.shouldRun(false,true,false,false));
  for(int count=1;count<=5;count++)for(int preferred=0;preferred<count;preferred++){
   Set<Integer> seen=new HashSet<>();for(int attempt=0;attempt<count;attempt++)seen.add(PreviewPolicy.serverIndex(attempt,count,preferred));
   ok(seen.size()==count);ok(PreviewPolicy.serverIndex(0,count,preferred)==preferred);
  }
  ok(PreviewPolicy.serverIndex(0,0,0)==-1);
  System.out.println(checks+" remote/filter/preview checks passed: International filter traversal, boundaries, lifecycle eligibility and complete preferred-server permutations.");
 }
}
