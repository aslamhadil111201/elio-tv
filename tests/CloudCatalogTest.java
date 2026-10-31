package id.matin.nusatv;
import java.util.*;
import com.lagradost.cloudstream3.*;
import kotlin.Unit;
public final class CloudCatalogTest {
 static int n;static void ok(boolean v){n++;if(!v)throw new AssertionError("catalog "+n);}
 static MainAPI p=new MainAPI(){};
 static SearchResponse row(String title,String url,TvType type){return MainAPIKt.newMovieSearchResponse(p,title,url,type,false,r->Unit.INSTANCE);}
 public static void main(String[] ignored){
  ok(CloudCatalog.columns(320)==2);ok(CloudCatalog.columns(480)==3);ok(CloudCatalog.columns(720)==4);ok(CloudCatalog.columns(900)==5);ok(CloudCatalog.columns(1200)==6);
  SearchResponse film=row("Film","https://example.org/film",TvType.Movie),series=row("Serial","https://example.org/series",TvType.TvSeries),anime=row("Anime","https://example.org/anime",TvType.Anime);
  ok(CloudCatalog.kind(film)==1);ok(CloudCatalog.kind(series)==2);ok(CloudCatalog.kind(anime)==3);ok(CloudCatalog.kind(row("Drama","d",TvType.AsianDrama))==2);ok(CloudCatalog.kind(row("Anime movie","a",TvType.AnimeMovie))==3);
  List<SearchResponse> rows=Arrays.asList(film,series,film,null,anime,row(" ","empty",TvType.Movie),row("Empty URL","",TvType.Movie));
  ok(CloudCatalog.select(rows,0).size()==3);ok(CloudCatalog.select(rows,1).equals(Collections.singletonList(film)));ok(CloudCatalog.select(rows,2).equals(Collections.singletonList(series)));ok(CloudCatalog.select(rows,3).equals(Collections.singletonList(anime)));ok(CloudCatalog.select(null,0).isEmpty());
  List<SearchResponse> many=new ArrayList<>();for(int i=0;i<200;i++)many.add(row("Title "+i,"url"+i,TvType.Movie));ok(CloudCatalog.select(many,0).size()==120);
  ok(CloudCatalog.down(0,7,5)==5);ok(CloudCatalog.down(4,7,5)==6);ok(CloudCatalog.down(6,7,5)==6);ok(CloudCatalog.down(0,1,5)==0);
  MovieSearchResponse one=(MovieSearchResponse)row("Example","one",TvType.Movie),same=(MovieSearchResponse)row("Example (2020)","two",TvType.Movie),remake=(MovieSearchResponse)row("Example","three",TvType.Movie);
  one.setYear(2020);same.setYear(2020);remake.setYear(2021);
  ok(CloudCatalog.grouped(Arrays.asList(one,same,remake)).size()==2);
  same.setYear(null);ok(CloudCatalog.grouped(Arrays.asList(one,same)).size()==2);
  ok(CloudCatalog.grouped(rows).size()==3);ok(CloudCatalog.grouped(many).size()==120);ok(CloudCatalog.grouped(null).isEmpty());
  System.out.println(n+" catalog checks passed: responsive columns, actual content types, deduplication, filtering, result limits and partial-row focus.");
 }
}
