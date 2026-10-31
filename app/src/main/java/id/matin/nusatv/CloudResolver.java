package id.matin.nusatv;

import java.util.*;
import java.util.concurrent.CancellationException;
import org.json.JSONArray;
import com.lagradost.cloudstream3.*;

/** Bounded, cancellable fallback across explicitly installed providers only. */
final class CloudResolver {
 interface Backend {
  List<MainAPI> providers();
  List<SearchResponse> search(MainAPI p,String title)throws Exception;
  LoadResponse detail(MainAPI p,String url)throws Exception;
  JSONArray links(MainAPI p,String data)throws Exception;
 }
 static final class Session {
  final MainAPI initial;final LoadResponse item;final Episode episode;final String data,title;
  final Set<String> tried=new HashSet<>(),urls=new HashSet<>();
  volatile boolean cancelled;int providersTried;
  Session(MainAPI p,LoadResponse item,Episode e,String display){initial=p;this.item=item;episode=e;title=display;data=e==null?((MovieLoadResponse)item).getDataUrl():e.getData();}
 }
 static final class Result {
  final MainAPI provider;final JSONArray links;final LoadResponse candidate;
  Result(MainAPI p,JSONArray l){this(p,l,null);}
  Result(MainAPI p,JSONArray l,LoadResponse candidate){provider=p;links=l;this.candidate=candidate;}
 }
 final Backend backend;
 CloudResolver(Backend b){backend=b;}
 static String key(MainAPI p){return p.getName()+"\n"+p.getMainUrl();}
 void check(Session s,long deadline){if(s.cancelled||Thread.currentThread().isInterrupted())throw new CancellationException();if(System.nanoTime()>deadline)throw new IllegalStateException("Pencarian cadangan melewati batas waktu. Coba lagi atau pilih provider lain.");}
 JSONArray unique(Session s,JSONArray rows)throws Exception {JSONArray out=new JSONArray();if(rows!=null)for(int i=0;i<rows.length();i++){String u=rows.getJSONObject(i).optString("url");if(!u.isEmpty()&&!s.urls.contains(u))out.put(rows.getJSONObject(i));}return out;}
 void failedLinks(Session s,JSONArray rows){for(int i=0;i<rows.length();i++)s.urls.add(rows.optJSONObject(i).optString("url"));}
 Result resolve(Session s)throws Exception{
  long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(75);check(s,deadline);
  if(s.tried.add(key(s.initial))){try{JSONArray links=unique(s,backend.links(s.initial,s.data));check(s,deadline);if(links.length()>0)return new Result(s.initial,links);}catch(CancellationException e){throw e;}catch(Exception e){check(s,deadline);}}
  boolean series=s.episode!=null;
  if(series&&(!(s.item instanceof TvSeriesLoadResponse)||s.episode.getSeason()==null||s.episode.getEpisode()==null))throw new IllegalStateException("Sumber kosong/error. Pencarian cadangan serial memerlukan musim dan nomor episode yang jelas; pilih provider lain secara manual.");
  for(MainAPI p:backend.providers()){
   check(s,deadline);if(s.tried.contains(key(p)))continue;if(s.providersTried>=4)break;s.providersTried++;s.tried.add(key(p));
   try{
    List<SearchResponse> rows=backend.search(p,s.item.getName());check(s,deadline);int inspected=0;
    for(SearchResponse row:rows){
     check(s,deadline);if(!CloudMatch.titleMatches(s.item.getName(),row.getName()))continue;if(inspected++>=3)break;
     try{
      LoadResponse other=backend.detail(p,row.getUrl());check(s,deadline);if(other==null)continue;
      boolean otherSeries=other instanceof TvSeriesLoadResponse;
      if(!(other instanceof MovieLoadResponse)&&!otherSeries)continue;
      boolean unknownYear=s.item.getYear()==null||other.getYear()==null;
      if(!CloudMatch.titleMatches(s.item.getName(),other.getName())||series!=otherSeries)continue;
      if(!unknownYear&&!CloudMatch.identity(s.item.getName(),s.item.getYear(),series,other.getName(),other.getYear(),otherSeries))continue;
      String data=null;
      if(!series)data=((MovieLoadResponse)other).getDataUrl();
      else for(Episode e:((TvSeriesLoadResponse)other).getEpisodes())if(CloudMatch.episode(s.episode.getSeason(),s.episode.getEpisode(),e.getSeason(),e.getEpisode())){data=e.getData();break;}
      if(data==null||data.trim().isEmpty())continue;
      JSONArray links=unique(s,backend.links(p,data));check(s,deadline);if(links.length()>0)return new Result(p,links,unknownYear?other:null);
     }catch(CancellationException e){throw e;}catch(Exception e){check(s,deadline);}
    }
   }catch(CancellationException e){throw e;}catch(Exception e){check(s,deadline);}
  }
  throw new IllegalStateException("Belum ada sumber yang cocok dan dapat diputar dari provider terpasang yang dicoba. Coba provider lain atau perbarui ekstensi. Judul/tahun dan episode yang berbeda tidak dipilih otomatis.");
 }
}
