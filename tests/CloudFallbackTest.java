package id.matin.nusatv;
import java.util.*;
import org.json.*;
import kotlin.Unit;
import com.lagradost.cloudstream3.*;

public final class CloudFallbackTest {
 static int checks;
 static void ok(boolean b){checks++;if(!b)throw new AssertionError("check "+checks);}
 static class P extends MainAPI {final String n;P(String s){n=s;}public String getName(){return n;}public String getMainUrl(){return "https://example.org/"+n;}}
 static P a=new P("a"),b=new P("b"),c=new P("c");
 static LoadResponse movie(MainAPI p,String title,Integer year){return (LoadResponse)MainAPIKt.newMovieLoadResponse(p,title,"https://example.org/detail",TvType.Movie,"data",(r,k)->{r.setYear(year);return Unit.INSTANCE;},null);}
 static Episode ep(int s,int n){return new Episode("episode-"+s+"-"+n,"ep",s,n,null,null,null,null,null);}
 static LoadResponse series(MainAPI p,int season,int episode){return (LoadResponse)MainAPIKt.newTvSeriesLoadResponse(p,"Example","https://example.org/detail",TvType.TvSeries,Arrays.asList(ep(season,episode)),(r,k)->{r.setYear(2020);return Unit.INSTANCE;},null);}
 static JSONArray link(String suffix){return new JSONArray().put(new JSONObject().put("url","https://example.org/"+suffix).put("name","test"));}
 static class Backend implements CloudResolver.Backend {
  List<MainAPI> ps=Arrays.asList(a,b,c);Map<MainAPI,LoadResponse> details=new HashMap<>();Map<MainAPI,JSONArray> links=new HashMap<>();Set<MainAPI> errors=new HashSet<>();int searchCalls,detailCalls,linkCalls;
  public List<MainAPI> providers(){return ps;}
  public List<SearchResponse> search(MainAPI p,String title){searchCalls++;return Arrays.asList(MainAPIKt.newMovieSearchResponse(p,title,"https://example.org/detail",TvType.Movie,false,r->Unit.INSTANCE));}
  public LoadResponse detail(MainAPI p,String u){detailCalls++;return details.get(p);}
  public JSONArray links(MainAPI p,String d)throws Exception{linkCalls++;if(errors.contains(p))throw new Exception("null source");return links.getOrDefault(p,new JSONArray());}
 }
 static CloudResolver.Session session(){return new CloudResolver.Session(a,movie(a,"Example",2020),null,"Example");}
 static void fails(CloudResolver r,CloudResolver.Session s)throws Exception{try{r.resolve(s);throw new AssertionError("expected failure");}catch(IllegalStateException expected){checks++;}}
 public static void main(String[] args)throws Exception{
  ok(CloudMatch.titleMatches("Amélie (2001)","Amelie"));ok(!CloudMatch.titleMatches("Example 2","Example"));ok(!CloudMatch.titleMatches("",""));
  ok(!CloudMatch.identity("Example",2020,false,"Example",2021,false));ok(!CloudMatch.identity("Example",2020,false,"Example",2020,true));ok(!CloudMatch.identity("Example",null,false,"Example",null,false));
  ok(CloudMatch.episode(2,3,2,3));ok(!CloudMatch.episode(2,3,1,3));ok(!CloudMatch.episode(null,3,null,3));
  Backend x=new Backend();x.links.put(a,link("a"));CloudResolver r=new CloudResolver(x);ok(r.resolve(session()).provider==a);ok(x.searchCalls==0);
  x=new Backend();x.details.put(b,movie(b,"Example",2020));x.links.put(b,link("b"));r=new CloudResolver(x);CloudResolver.Session s=session();CloudResolver.Result result=r.resolve(s);ok(result.provider==b);ok(x.searchCalls==1);ok(result.links.length()==1);
  r.failedLinks(s,result.links);x.details.put(c,movie(c,"Example",2020));x.links.put(c,link("c"));ok(r.resolve(s).provider==c);fails(r,s);
  x=new Backend();x.errors.add(a);x.details.put(b,movie(b,"Example",2020));x.links.put(b,link("b"));ok(new CloudResolver(x).resolve(session()).provider==b);
  x=new Backend();x.details.put(b,movie(b,"Example",2010));x.links.put(b,link("wrong"));r=new CloudResolver(x);fails(r,session());ok(x.linkCalls==1);
  x=new Backend();r=new CloudResolver(x);fails(r,new CloudResolver.Session(a,movie(a,"Example",null),null,"Example"));ok(x.searchCalls==2);
  x=new Backend();x.details.put(b,movie(b,"Example",2020));x.links.put(b,link("candidate"));r=new CloudResolver(x);s=new CloudResolver.Session(a,movie(a,"Example",null),null,"Example");result=r.resolve(s);ok(result.candidate!=null);ok(result.provider==b);ok(result.links.length()==1);
  x=new Backend();x.details.put(b,movie(b,"Example",null));x.links.put(b,link("unknown"));r=new CloudResolver(x);ok(r.resolve(session()).candidate!=null);
  x=new Backend();x.details.put(b,movie(b,"Example 2",null));x.links.put(b,link("wrong-title"));r=new CloudResolver(x);fails(r,session());ok(x.linkCalls==1);
  x=new Backend();r=new CloudResolver(x);s=session();s.cancelled=true;try{r.resolve(s);throw new AssertionError();}catch(java.util.concurrent.CancellationException good){checks++;}ok(x.linkCalls==0);
  x=new Backend();x.details.put(b,series(b,2,3));x.links.put(b,link("ep"));r=new CloudResolver(x);ok(r.resolve(new CloudResolver.Session(a,series(a,2,3),ep(2,3),"Example S2E3")).provider==b);
  x=new Backend();x.details.put(b,series(b,1,3));x.links.put(b,link("wrong-ep"));r=new CloudResolver(x);fails(r,new CloudResolver.Session(a,series(a,2,3),ep(2,3),"Example S2E3"));ok(x.linkCalls==1);
  x=new Backend();x.ps=Arrays.asList(a,new P("1"),new P("2"),new P("3"),new P("4"),new P("5"));r=new CloudResolver(x);fails(r,session());ok(x.searchCalls==4);
  x=new Backend();x.details.put(b,movie(b,"Example",2020));x.links.put(b,link("same"));x.links.put(a,link("same"));r=new CloudResolver(x);s=session();result=r.resolve(s);r.failedLinks(s,result.links);fails(r,s);
  ok(Arrays.equals(VideoLayout.size(1920,1080,640,480,VideoLayout.STRETCH),new int[]{1920,1080}));ok(Arrays.equals(VideoLayout.size(1080,1920,1920,1080,VideoLayout.STRETCH),new int[]{1080,1920}));
  ok(Arrays.equals(VideoLayout.size(1920,1080,640,480,VideoLayout.FIT),new int[]{1440,1080}));ok(Arrays.equals(VideoLayout.size(1920,1080,640,480,VideoLayout.ZOOM),new int[]{1920,1440}));
  CloudDemoPlugin.EmptyDemo empty=new CloudDemoPlugin.EmptyDemo();CloudDemoPlugin.Demo demo=new CloudDemoPlugin.Demo();
  CloudResolver.Backend demoBackend=new CloudResolver.Backend(){
   public List<MainAPI> providers(){return Arrays.asList(empty,demo);}
   public List<SearchResponse> search(MainAPI p,String q){return (List<SearchResponse>)p.search(q,null);}
   public LoadResponse detail(MainAPI p,String u){return (LoadResponse)p.load(u,null);}
   public JSONArray links(MainAPI p,String d){JSONArray result=new JSONArray();p.loadLinks(d,false,sub->Unit.INSTANCE,l->{result.put(new JSONObject().put("url",l.getUrl()));return Unit.INSTANCE;},null);return result;}
  };
  LoadResponse sample=demoBackend.detail(empty,"demo");CloudResolver.Result dr=new CloudResolver(demoBackend).resolve(new CloudResolver.Session(empty,sample,null,sample.getName()));ok(dr.provider==demo);ok(dr.links.length()==1);ok(dr.links.getJSONObject(0).getString("url").equals("asset:///cloud-demo-video.mp4"));
  System.out.println(checks+" checks passed: primary/empty/error fallback, exhausted/duplicate links, year/type/episode identity, cancellation, provider cap, and video geometry.");
 }
}
