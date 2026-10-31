package id.matin.nusatv;

import android.content.Context;
import dalvik.system.DexClassLoader;
import org.json.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import kotlin.Unit;
import kotlin.coroutines.*;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import com.lagradost.cloudstream3.*;
import com.lagradost.cloudstream3.plugins.Plugin;
import com.lagradost.cloudstream3.utils.*;

/** Experimental Cloudstream API bridge. No extension is loaded on IPTV startup. */
final class CloudRuntime implements CloudResolver.Backend {
 final ExecutorService calls=new ThreadPoolExecutor(2,2,0L,TimeUnit.MILLISECONDS,new SynchronousQueue<Runnable>(),r->{Thread t=new Thread(r,"elio-provider");t.setDaemon(true);return t;});
 void close(){calls.shutdownNow();}
 <T>T bounded(Callable<T> call)throws Exception{Future<T> f;try{f=calls.submit(call);}catch(RejectedExecutionException e){throw new IOException("Provider sebelumnya belum berhenti. Buka ulang aplikasi jika tetap sibuk.",e);}try{return f.get(12,TimeUnit.SECONDS);}catch(TimeoutException e){throw new IOException("Provider melewati batas waktu",e);}catch(ExecutionException e){Throwable cause=e.getCause();if(cause instanceof Exception)throw (Exception)cause;throw new IOException("Provider tidak kompatibel",cause);}finally{if(!f.isDone())f.cancel(true);}}
 final Context context;final File dir;final Map<String,Plugin> loaded=new LinkedHashMap<>();
 CloudRuntime(Context c){context=c.getApplicationContext();com.lagradost.cloudstream3.MainActivityKt.setInsecureApp(com.lagradost.cloudstream3.MainActivityKt.getApp());dir=new File(context.getFilesDir(),"cloudstream");dir.mkdirs();}
 static final class Await<T> implements Continuation<T>{final CountDownLatch latch=new CountDownLatch(1);Object value;public CoroutineContext getContext(){return EmptyCoroutineContext.INSTANCE;}public void resumeWith(Object r){value=r;latch.countDown();}T result(Object immediate)throws Exception{if(immediate!=IntrinsicsKt.getCOROUTINE_SUSPENDED())return (T)immediate;if(!latch.await(35,TimeUnit.SECONDS))throw new IOException("Provider melewati batas waktu");kotlin.ResultKt.throwOnFailure(value);return (T)value;}}
 void demo()throws Exception{String id=CloudRepository.digest("builtin:elio-demo".getBytes("UTF-8"));if(loaded.containsKey(id))return;byte[] bytes;try(InputStream in=context.getAssets().open("cloud-demo.cs3");ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);bytes=out.toByteArray();}JSONObject record=installBytes(new JSONObject().put("name","ELIO Demo").put("url","builtin:elio-demo"),bytes);load(record);}

 public List<MainAPI> providers(){return new ArrayList<MainAPI>(APIHolder.INSTANCE.getAllProviders());}
 synchronized void load(JSONObject record)throws Exception{
  String id=record.getString("id");if(!id.matches("[a-f0-9]{64}"))throw new IOException("ID tidak valid");if(loaded.containsKey(id))return;
  File file=new File(dir,id+".cs3");byte[] bytes=read(file);if(!CloudRepository.digest(bytes).equals(record.getString("sha256")))throw new IOException("Integritas ekstensi gagal");JSONObject manifest=CloudRepository.inspect(bytes);file.setReadOnly();
  try{DexClassLoader loader=new DexClassLoader(file.getAbsolutePath(),context.getCodeCacheDir().getAbsolutePath(),null,context.getClassLoader());Class<?> type=loader.loadClass(manifest.getString("pluginClassName"));if(!Plugin.class.isAssignableFrom(type))throw new IOException("API plugin tidak kompatibel");Plugin plugin=(Plugin)type.getDeclaredConstructor().newInstance();plugin.setFilename(file.getAbsolutePath());plugin.load(context);loaded.put(id,plugin);}catch(Throwable e){for(MainAPI p:providers())if(file.getAbsolutePath().equals(p.getSourcePlugin()))APIHolder.INSTANCE.getAllProviders().remove(p);throw new IOException("Ekstensi belum kompatibel: "+e.getClass().getSimpleName(),e);}
 }
 byte[] read(File f)throws Exception{try(InputStream in=new FileInputStream(f);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>CloudRepository.PLUGIN_LIMIT)throw new IOException("File terlalu besar");out.write(b,0,n);}return out.toByteArray();}}
 JSONObject install(JSONObject info)throws Exception{
  return installBytes(info,CloudRepository.fetch(info.getString("url"),CloudRepository.PLUGIN_LIMIT));}
 JSONObject installBytes(JSONObject info,byte[] bytes)throws Exception{JSONObject manifest=CloudRepository.inspect(bytes);String id=CloudRepository.digest(info.getString("url").getBytes("UTF-8"));if(loaded.containsKey(id))throw new IOException("Tutup dan buka ulang aplikasi sebelum mengganti ekstensi ini");
  File temp=new File(dir,id+".part");try(FileOutputStream out=new FileOutputStream(temp)){if(!temp.setReadOnly())throw new IOException("Tidak dapat mengamankan file ekstensi");out.write(bytes);out.getFD().sync();}catch(Exception e){temp.delete();throw e;}
  File target=new File(dir,id+".cs3");if(target.exists()&&!target.delete()){temp.delete();throw new IOException("Tidak dapat memperbarui ekstensi");}if(!temp.renameTo(target)){temp.delete();throw new IOException("Tidak dapat menyimpan ekstensi");}
  JSONObject result=new JSONObject(info.toString());result.put("id",id).put("sha256",CloudRepository.digest(bytes)).put("class",manifest.getString("pluginClassName"));return result;
 }
 public List<SearchResponse> search(MainAPI api,String query)throws Exception{return bounded(()->searchNow(api,query));}
 List<SearchResponse> searchNow(MainAPI api,String query)throws Exception{Await<List<? extends SearchResponse>> a=new Await<>();List<? extends SearchResponse> rows=a.result(api.search(query,a));return rows==null?Collections.emptyList():new ArrayList<SearchResponse>(rows);}
 List<HomePageList> shelves(MainAPI api,int index)throws Exception{return bounded(()->{if(!api.getHasMainPage()||api.getMainPage()==null||api.getMainPage().isEmpty())return Collections.emptyList();int i=Math.max(0,Math.min(index,api.getMainPage().size()-1));MainPageData page=api.getMainPage().get(i);Await<HomePageResponse> wait=new Await<>();HomePageResponse response=wait.result(api.getMainPage(1,new MainPageRequest(page.getName(),page.getData(),page.getHorizontalImages()),wait));return response==null?Collections.emptyList():response.getItems();});}
 List<SearchResponse> home(MainAPI api)throws Exception{if(!api.getHasMainPage()||api.getMainPage().isEmpty())return Collections.emptyList();Await<HomePageResponse> a=new Await<>();HomePageResponse r=a.result(api.getMainPage(1,new MainPageRequest(api.getMainPage().get(0).getName(),api.getMainPage().get(0).getData(),api.getMainPage().get(0).getHorizontalImages()),a));ArrayList<SearchResponse> rows=new ArrayList<>();if(r!=null)for(HomePageList list:r.getItems())rows.addAll(list.getList());return rows;}
 public LoadResponse detail(MainAPI api,String url)throws Exception{return bounded(()->detailNow(api,url));}
 LoadResponse detailNow(MainAPI api,String url)throws Exception{Await<LoadResponse> a=new Await<>();return a.result(api.load(url,a));}
 List<Episode> episodes(LoadResponse r){ArrayList<Episode> list=new ArrayList<>();if(r instanceof TvSeriesLoadResponse)list.addAll(((TvSeriesLoadResponse)r).getEpisodes());else if(r instanceof AnimeLoadResponse)for(List<Episode> rows:((AnimeLoadResponse)r).getEpisodes().values())list.addAll(rows);return list;}
 public JSONArray links(MainAPI api,String data)throws Exception{if(data==null||data.trim().isEmpty())return new JSONArray();return bounded(()->linksNow(api,data));}
 JSONArray linksNow(MainAPI api,String data)throws Exception{List<ExtractorLink> links=Collections.synchronizedList(new ArrayList<>());Await<Boolean> a=new Await<>();a.result(api.loadLinks(data,false,s->Unit.INSTANCE,l->{links.add(l);return Unit.INSTANCE;},a));JSONArray rows=new JSONArray();HashSet<String> seen=new HashSet<>();synchronized(links){for(ExtractorLink l:links){if(l==null)continue;String u=l.getUrl();if(!(CatalogStore.webUrl(u,false)||"asset:///cloud-demo-video.mp4".equals(u))||!seen.add(u)||l instanceof DrmExtractorLink||l.getType()==ExtractorLinkType.TORRENT||l.getType()==ExtractorLinkType.MAGNET)continue;JSONObject h=new JSONObject();for(Map.Entry<String,String> e:l.getHeaders().entrySet())if(e.getKey().matches("[A-Za-z0-9-]{1,80}")&&!e.getValue().contains("\n")&&!e.getValue().contains("\r"))h.put(e.getKey(),e.getValue());if(!l.getReferer().isEmpty())h.put("Referer",l.getReferer());rows.put(new JSONObject().put("name",l.getName()+" · "+l.getQuality()+"p").put("url",u).put("headers",h).put("mimeType",l.isM3u8()?"application/x-mpegURL":l.isDash()?"application/dash+xml":""));}}return rows;}
}
