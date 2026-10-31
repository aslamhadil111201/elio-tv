package id.matin.nusatv;
import java.util.*;
import com.lagradost.cloudstream3.*;

/** Provider metadata only: no invented genres or movie entries. */
final class CloudCatalog {
 static int columns(int widthDp){return widthDp>=1100?6:widthDp>=800?5:widthDp>=600?4:widthDp>=420?3:2;}
 static int kind(SearchResponse row){String t=row.getType()==null?"":row.getType().name().toLowerCase(Locale.ROOT);if(t.contains("anime"))return 3;if(t.contains("series")||t.contains("drama"))return 2;if(t.contains("movie")||t.contains("documentary"))return 1;return 0;}
 static String label(SearchResponse row){return new String[]{"Video","Film","Serial","Anime"}[kind(row)];}
 static List<SearchResponse> select(List<SearchResponse> input,int kind){ArrayList<SearchResponse> out=new ArrayList<>();Set<String> seen=new HashSet<>();if(input!=null)for(SearchResponse r:input){if(r==null||r.getUrl()==null||r.getName()==null||r.getName().trim().isEmpty()||r.getUrl().trim().isEmpty())continue;if(kind!=0&&kind(r)!=kind)continue;if(seen.add(r.getApiName()+"\n"+r.getUrl()))out.add(r);if(out.size()==120)break;}return out;}
 static int down(int index,int count,int columns){return index/columns==(count-1)/columns?index:Math.min(count-1,index+columns);}
 static List<SearchResponse> grouped(List<SearchResponse> input){
  List<SearchResponse> out=new ArrayList<>();Set<String> seen=new HashSet<>();
  Map<String,ArrayDeque<SearchResponse>> queues=new LinkedHashMap<>();
  if(input!=null)for(SearchResponse row:input){
   if(row==null||row.getName()==null||row.getName().trim().isEmpty()||row.getUrl()==null||row.getUrl().trim().isEmpty())continue;
   queues.computeIfAbsent(row.getApiName(),k->new ArrayDeque<>()).add(row);
  }
  boolean more=true;
  while(more&&out.size()<120){more=false;for(ArrayDeque<SearchResponse> queue:queues.values()){
   if(queue.isEmpty())continue;more=true;SearchResponse row=queue.remove();
   Integer year=row instanceof MovieSearchResponse?((MovieSearchResponse)row).getYear():null;
   String key=year==null?row.getApiName()+"\n"+row.getUrl():CloudMatch.title(row.getName())+"\n"+year+"\n"+kind(row);
   if(seen.add(key))out.add(row);if(out.size()>=120)break;
  }}
  return out;
 }
}
