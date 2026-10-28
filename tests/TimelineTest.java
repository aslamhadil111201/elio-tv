package id.matin.nusatv;
import java.util.*;
public class TimelineTest {
 static int checks;static void ck(boolean b){checks++;if(!b)throw new AssertionError("case "+checks);}
 public static void main(String[] args){
 ck(SeekWindow.position(94,94,1108,600000)==0);ck(SeekWindow.position(1108,94,1108,600000)==600000);ck(SeekWindow.position(601,94,1108,600000)==300000);ck(SeekWindow.position(-100,94,1108,600000)==0);ck(SeekWindow.position(5000,94,1108,600000)==600000);ck(SeekWindow.position(Float.NaN,94,1108,600000)==0);ck(SeekWindow.position(500,10,10,600000)==0);ck(SeekWindow.position(500,94,1108,-1)==0);
 ck(SeekWindow.rebase(300000,1000000,1015000,600000)==285000);ck(SeekWindow.rebase(300000,-1,-1,600000)==300000);ck(SeekWindow.rebase(0,1000,1000,600000)==0);
 try{SeekWindow.rebase(10000,1000000,1030000,600000);throw new AssertionError();}catch(IllegalArgumentException e){checks++;}
 try{SeekWindow.rebase(700000,-1,-1,600000);throw new AssertionError();}catch(IllegalArgumentException e){checks++;}
 EpgStore epg=new EpgStore();long now=1000000000;Map<String,List<EpgStore.Programme>> rows=new HashMap<>();rows.put("sctv",Arrays.asList(new EpgStore.Programme(now-7200000,now-3600000,"Sinetron","faae"),new EpgStore.Programme(now-3600000,now-1800000,"Liputan 6","faae"),new EpgStore.Programme(now-1800000,now+1800000,"Sekarang","faae"),new EpgStore.Programme(now-90000000,now-86400001,"Kemarin","faae")));
 epg.replace("faae",rows);List<EpgStore.Programme> history=epg.history("sctv",now);ck(history.size()==2);ck(history.get(0).title.equals("Liputan 6"));ck(history.get(1).title.equals("Sinetron"));ck(epg.history("unknown",now).isEmpty());
 rows=new HashMap<>();List<EpgStore.Programme> many=new ArrayList<>();for(int i=1;i<=30;i++)many.add(new EpgStore.Programme(now-i*60000,now-i*60000+30000,"Acara "+i,"faae"));rows.put("sctv",many);epg.replace("faae",rows);ck(epg.history("sctv",now).size()==24);
 System.out.println(checks+" timeline coordinates, moving DVR and programme-history tests PASS");
 }
}
