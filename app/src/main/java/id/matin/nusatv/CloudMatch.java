package id.matin.nusatv;

import java.text.Normalizer;
import java.util.Locale;

/** Conservative cross-provider identity: never substitute a sequel or another episode. */
final class CloudMatch {
 static String title(String s) {
  if(s==null)return "";
  return Normalizer.normalize(s,Normalizer.Form.NFKD).replaceAll("\\p{M}+", "")
   .toLowerCase(Locale.ROOT).replaceAll("\\s*[\\[(](?:19|20)\\d{2}[\\])]\\s*$", "")
   .replaceAll("[^\\p{L}\\p{N}]+", " ").trim().replaceAll(" +", " ");
 }
 static boolean titleMatches(String a,String b){String x=title(a);return !x.isEmpty()&&x.equals(title(b));}
 static boolean identity(String a,Integer ay,boolean series,String b,Integer by,boolean otherSeries){
  return series==otherSeries&&titleMatches(a,b)&&ay!=null&&ay.equals(by);
 }
 static boolean episode(Integer season,Integer number,Integer otherSeason,Integer otherNumber){
  return season!=null&&number!=null&&season.equals(otherSeason)&&number.equals(otherNumber);
 }
}
