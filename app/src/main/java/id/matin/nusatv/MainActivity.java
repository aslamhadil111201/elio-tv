package id.matin.nusatv;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.media.MediaMetadata;
import android.media.AudioManager;
import android.media.session.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import android.text.InputType;
import androidx.media3.common.*;
import androidx.media3.common.text.CueGroup;
import androidx.media3.datasource.*;
import androidx.media3.exoplayer.*;
import androidx.media3.exoplayer.drm.*;
import androidx.media3.exoplayer.source.*;
import androidx.media3.ui.SubtitleView;
import org.json.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;

public class MainActivity extends Activity {
 static class Server {
  String name,url,drm,licenseUrl,keyId,key,mimeType; Map<String,String> headers=new HashMap<>();
  Server(JSONObject j){mimeType=j.optString("mimeType");name=j.optString("name","Server");url=j.optString("url");drm=j.optString("drm");licenseUrl=j.optString("licenseUrl");keyId=j.optString("keyId");key=j.optString("key");JSONObject h=j.optJSONObject("headers");if(h!=null){Iterator<String> it=h.keys();while(it.hasNext()){String k=it.next();headers.put(k,h.optString(k));}}}
 }
 static class Channel {
  String id,name,region,section,owner,legacyId,availabilityNote,officialUrl,liveLabel,logoUrl;JSONObject epgIds; Bitmap logo; boolean logoDark=false; ArrayList<Server> servers=new ArrayList<>();
  Channel(JSONObject j){logoUrl=j.optString("logoUrl");id=j.optString("id");name=j.optString("name");region=j.optString("group","Indonesia");section=j.optString("section",region);owner=j.optString("owner");legacyId=j.optString("legacyId");availabilityNote=j.optString("availabilityNote");officialUrl=j.optString("officialUrl");liveLabel=j.optString("liveLabel");epgIds=j.optJSONObject("epgIds");logoDark=j.optBoolean("logoDark",false);JSONArray a=j.optJSONArray("servers");if(a!=null)for(int i=0;i<a.length();i++)servers.add(new Server(a.optJSONObject(i)));}
 }
 final ArrayList<Channel> channels=new ArrayList<>(),visible=new ArrayList<>(),playQueue=new ArrayList<>();
 final HashSet<String> favorites=new HashSet<>(); final HashSet<Integer> triedServers=new HashSet<>();
 final Handler handler=new Handler(Looper.getMainLooper());final ExecutorService io=Executors.newSingleThreadExecutor();
 final String[] menu={"Semua channel","Indonesia","Internasional","Olahraga","Favorit","Pencarian","Film","Pengaturan"};
 CloudUi cloudUi;CloudDashboard cloudDashboard;
 void films(){if(cloudUi==null)cloudUi=new CloudUi(this);if(cloudDashboard==null)cloudDashboard=new CloudDashboard(this,cloudUi);cloudDashboard.open();}
 void playCloudVideo(String title,String provider,JSONArray links)throws Exception{playCloudVideo(title,provider,links,0);}
 void playCloudVideo(String title,String provider,JSONArray links,long position)throws Exception{if(cloudDashboard!=null)cloudDashboard.hideForPlayback();cloudResumePosition=position;stopPreview();cancelTimeline();Channel item=new Channel(new JSONObject().put("id","film-"+hexDigest(title+provider)).put("name",title).put("group","Film").put("section","Film").put("owner",provider).put("servers",links));playQueue.clear();playQueue.add(item);openChannel(0);}
 android.content.SharedPreferences prefs; FrameLayout root;SurfaceView surface;TextureView stretchSurface;boolean videoUsesTexture=false,videoSurfaceAttached=false;long cloudResumePosition=0;SubtitleView subtitles;TvView ui;ExoPlayer player;MediaSession session;
 int section=0,navFocus=0,selected=0,rowOffset=0,playerIndex=0,drawerIndex=0,serverIndex=0,attempt=0,generation=0,theme=0;
 int heroFocus=-1,filterFocus=-1;
 boolean firstFrame=false;android.widget.ProgressBar busy;
 boolean nav=false,playing=false,prepared=false,paused=false,buffering=false,error=false,drawer=false,resumePlayback=false,longPress=false;
 String query="",status="",toast="",quality="Auto";long toastUntil=0,overlayUntil=0,backAt=0;int videoHeight=0,videoWidth=0;int screenMode=VideoLayout.FIT;float videoPixelRatio=1f;Runnable timeout,retry;
 final EpgStore epg=new EpgStore();final ExecutorService epgIo=Executors.newSingleThreadExecutor();
 volatile boolean epgRefreshing=false,epgClosed=false;boolean epgCacheLoaded=false;long epgNextRefresh=0;
 final String[] epgFeeds={"faae","community","indonesia","iptvepg","rtm"};
 final String[] epgUrls={"https://raw.githubusercontent.com/apistech/project/refs/heads/main/epgs/guide.xml","https://raw.githubusercontent.com/dhasap/dhanytv/main/epg.xml","https://raw.githubusercontent.com/AqFad2811/epg/main/indonesia.xml","https://iptv-epg.org/files/epg-id.xml","https://raw.githubusercontent.com/AqFad2811/epg/main/rtmklik.xml"};
 final Runnable epgTicker=new Runnable(){public void run(){if(foreground){refreshEpg(false);refreshCatalog(false);}handler.postDelayed(this,60000);}};
 final Runnable ticker=new Runnable(){public void run(){if(ui!=null){syncPreview();ui.invalidate();}handler.postDelayed(this,350);}};
 ExoPlayer previewPlayer;android.view.TextureView previewSurface;String previewId="",previewStatus="Pilih channel untuk preview";int previewGeneration=0,previewPreferred=0;Runnable previewDelay,previewTimeout;boolean previewFrame=false,foreground=true;
 void stopPreview(){previewGeneration++;if(previewDelay!=null)handler.removeCallbacks(previewDelay);if(previewTimeout!=null)handler.removeCallbacks(previewTimeout);if(previewPlayer!=null){previewPlayer.release();previewPlayer=null;}previewFrame=false;if(previewSurface!=null)previewSurface.setVisibility(View.GONE);}
 void syncPreview(){if(!PreviewPolicy.shouldRun(playing,foreground,cloudDashboard!=null&&cloudDashboard.visible(),!visible.isEmpty())){if(previewPlayer!=null||previewDelay!=null){stopPreview();previewDelay=null;}previewId="";if(visible.isEmpty())previewStatus="Pilih channel untuk preview";return;}Channel ch=visible.get(selected);if(ch.id.equals(previewId))return;stopPreview();previewId=ch.id;previewStatus="Memuat preview…";final int token=previewGeneration;previewDelay=()->{previewDelay=null;startPreview(ch,0,token);};handler.postDelayed(previewDelay,PreviewPolicy.SELECT_DELAY_MS);}
 void startPreview(Channel ch,int index,int token){if(token!=previewGeneration||playing||!foreground)return;if(index>=ch.servers.size()){if(previewPlayer!=null){previewPlayer.release();previewPlayer=null;}previewSurface.setVisibility(View.GONE);previewFrame=false;previewStatus="Preview belum tersedia · pilih Tonton untuk mencoba siaran";ui.invalidate();return;}if(previewPlayer!=null){previewPlayer.release();previewPlayer=null;}if(index==0){previewPreferred=0;String last=prefs.getString("good-source-"+ch.id,"");for(int i=0;i<ch.servers.size();i++)if(ch.servers.get(i).url.equals(last))previewPreferred=i;}Server sv=ch.servers.get(PreviewPolicy.serverIndex(index,ch.servers.size(),previewPreferred));if(!sv.drm.isEmpty()){startPreview(ch,index+1,token);return;}try{DefaultHttpDataSource.Factory http=new DefaultHttpDataSource.Factory().setUserAgent(sv.headers.containsKey("User-Agent")?sv.headers.get("User-Agent"):"Mozilla/5.0").setDefaultRequestProperties(sv.headers).setConnectTimeoutMs(5000).setReadTimeoutMs(5000).setAllowCrossProtocolRedirects(true);previewPlayer=new ExoPlayer.Builder(this).setMediaSourceFactory(new DefaultMediaSourceFactory(new DefaultDataSource.Factory(this,http))).build();ExoPlayer pp=previewPlayer;pp.setVolume(0);pp.setTrackSelectionParameters(pp.getTrackSelectionParameters().buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_AUDIO,true).setTrackTypeDisabled(C.TRACK_TYPE_TEXT,true).setMaxVideoSize(854,480).build());pp.setVideoTextureView(previewSurface);previewSurface.setVisibility(View.VISIBLE);pp.addListener(new Player.Listener(){public void onRenderedFirstFrame(){if(token!=previewGeneration||previewPlayer!=pp)return;previewFrame=true;prefs.edit().putString("good-source-"+ch.id,sv.url).apply();previewStatus="Preview siaran · Tanpa suara";if(previewTimeout!=null)handler.removeCallbacks(previewTimeout);ui.invalidate();}public void onPlayerError(PlaybackException e){if(token==previewGeneration&&previewPlayer==pp){if(previewTimeout!=null)handler.removeCallbacks(previewTimeout);previewFrame=false;previewSurface.setVisibility(View.GONE);startPreview(ch,index+1,token);}}});MediaItem.Builder item=new MediaItem.Builder().setUri(sv.url);if(sv.url.contains(".mpd"))item.setMimeType(MimeTypes.APPLICATION_MPD);else if(sv.url.contains(".m3u")||sv.url.contains("workers.dev"))item.setMimeType(MimeTypes.APPLICATION_M3U8);pp.setMediaItem(item.build());pp.prepare();pp.play();previewTimeout=()->{if(token==previewGeneration&&previewPlayer==pp&&!previewFrame){previewSurface.setVisibility(View.GONE);startPreview(ch,index+1,token);}};handler.postDelayed(previewTimeout,PreviewPolicy.FIRST_FRAME_TIMEOUT_MS);}catch(Exception e){startPreview(ch,index+1,token);}}

 static final String CATALOG_URL="https://raw.githubusercontent.com/aslamhadil111201/elio-tv/main/catalog/channels.json";
 final ExecutorService catalogIo=Executors.newSingleThreadExecutor();
 volatile boolean catalogClosed=false;boolean catalogRefreshing=false,catalogPending=false;
 String bundledCatalog="",catalogSnapshot="",catalogStatus="Belum diperiksa";long catalogRevision=0,catalogNextCheck=0;
 final Map<String,Bitmap> onlineLogos=new HashMap<>();
 void applyCatalog(String snapshot){
  if(playing){catalogPending=true;return;}
  try{
   JSONArray rows=snapshot.isEmpty()?new JSONArray(bundledCatalog):CatalogStore.merge(bundledCatalog,snapshot);
   ArrayList<Channel> replacement=new ArrayList<>();HashMap<String,Channel> old=new HashMap<>();for(Channel ch:channels)old.put(ch.id,ch);
   String selectedId=visible.isEmpty()?"":visible.get(Math.min(selected,visible.size()-1)).id;
   HashSet<String> ids=new HashSet<>();
   for(int i=0;i<rows.length();i++){Channel ch=new Channel(rows.getJSONObject(i));Channel prior=old.get(ch.id);
    if(prior!=null&&prior.logoUrl.equals(ch.logoUrl))ch.logo=prior.logo;
    if(ch.logo==null)try{ch.logo=BitmapFactory.decodeStream(getAssets().open("logos/"+ch.id+".png"));}catch(Exception ignored){}
    if(!ch.logoUrl.isEmpty())try{File cache=new File(getCacheDir(),"catalog-logo-"+hexDigest(ch.logoUrl)+".img");if(cache.exists()){byte[] bytes=readBytes(cache);BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,bounds);if(bounds.outWidth>0&&bounds.outHeight>0&&bounds.outWidth<=2048&&bounds.outHeight<=2048){BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=Math.max(1,Math.max(bounds.outWidth,bounds.outHeight)/384);Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);if(bitmap!=null)ch.logo=bitmap;}}}catch(Exception ignored){}
    if(onlineLogos.containsKey(ch.id+"|"+ch.logoUrl))ch.logo=onlineLogos.get(ch.id+"|"+ch.logoUrl);
    if(!ch.legacyId.isEmpty()&&favorites.remove(ch.legacyId))favorites.add(ch.id);ids.add(ch.id);replacement.add(ch);
   }
   JSONArray custom=new JSONArray(prefs.getString("customChannels","[]"));for(int i=0;i<custom.length();i++){Channel ch=new Channel(custom.getJSONObject(i));if(ids.add(ch.id))replacement.add(ch);}
   stopPreview();previewId="";channels.clear();channels.addAll(replacement);
   if(ui!=null){epgNextRefresh=0;filter();for(int i=0;i<visible.size();i++)if(visible.get(i).id.equals(selectedId)){selected=i;break;}ensureVisible();redraw();}
  }catch(Exception ignored){catalogStatus="Data pembaruan tidak valid; katalog terakhir dipertahankan";}
 }
 void catalogInfo(){long checked=prefs.getLong("catalog-checked",0);String when=checked==0?"Belum pernah":new SimpleDateFormat("d MMM yyyy HH:mm",new Locale("id","ID")).format(new Date(checked));
  new AlertDialog.Builder(this).setTitle("Pembaruan channel").setMessage("Revisi katalog: "+catalogRevision+"\nPemeriksaan berhasil: "+when+"\n"+catalogStatus+(catalogPending?"\nPembaruan diterapkan setelah keluar dari player.":"")+"\n\nKatalog diperiksa saat aplikasi dibuka, paling sering setiap 4 jam. Favorit dan playlist pribadi tetap tersimpan. Pembaruan katalog tidak menjamin semua siaran tersedia.").setPositiveButton(catalogRefreshing?"Sedang memeriksa…":"Perbarui sekarang",(d,w)->refreshCatalog(true)).setNegativeButton("Tutup",null).show();
 }
 void refreshCatalog(boolean manual){
  if(catalogClosed||bundledCatalog.isEmpty())return;
  if(catalogRefreshing){if(manual)message("Pembaruan channel sedang diperiksa");return;}
  long now=System.currentTimeMillis();if(!manual&&now<catalogNextCheck)return;
  catalogRefreshing=true;catalogStatus="Memeriksa pembaruan…";if(manual)message(catalogStatus);
  final String base=bundledCatalog,previous=catalogSnapshot;final long oldRevision=catalogRevision;
  catalogIo.execute(()->{
   String next="";long revision=oldRevision;String result="Koneksi gagal; katalog terakhir tetap digunakan";boolean success=false,changed=false;
   final HashMap<String,Bitmap> logos=new HashMap<>();
   try{
    next=new String(CatalogHttp.fetch(CATALOG_URL,CatalogStore.MAX_BYTES),java.nio.charset.StandardCharsets.UTF_8);
    JSONArray merged=CatalogStore.merge(base,next);revision=CatalogStore.revision(next);
    if(revision<oldRevision)throw new IOException("Old revision");
    if(revision==oldRevision&&!previous.isEmpty()&&!new JSONObject(previous).toString().equals(new JSONObject(next).toString()))throw new IOException("Revision reused");
    changed=revision>oldRevision;
    if(changed){android.util.AtomicFile file=new android.util.AtomicFile(new File(getFilesDir(),"catalog-v1.json"));FileOutputStream out=null;try{out=file.startWrite();out.write(next.getBytes(java.nio.charset.StandardCharsets.UTF_8));file.finishWrite(out);}catch(Exception e){if(out!=null)file.failWrite(out);throw e;}}
    long logoDeadline=System.nanoTime()+30000000000L;int logoCount=0;
    for(int i=0;i<merged.length()&&!catalogClosed&&System.nanoTime()<logoDeadline&&logoCount<64;i++){JSONObject row=merged.getJSONObject(i);String url=row.optString("logoUrl");if(url.isEmpty())continue;logoCount++;
     try{String filename="catalog-logo-"+hexDigest(url)+".img";File cache=new File(getCacheDir(),filename);byte[] bytes;
      if(cache.exists()&&cache.length()<=262144)bytes=readBytes(cache);else{bytes=CatalogHttp.fetch(url,262144);BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,bounds);if(bounds.outWidth<1||bounds.outHeight<1||bounds.outWidth>2048||bounds.outHeight>2048)continue;try(FileOutputStream out=new FileOutputStream(cache)){out.write(bytes);}}
      BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,bounds);if(bounds.outWidth<1||bounds.outHeight<1||bounds.outWidth>2048||bounds.outHeight>2048)continue;
      BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=Math.max(1,Math.max(bounds.outWidth,bounds.outHeight)/384);Bitmap logo=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);if(logo!=null)logos.put(row.getString("id")+"|"+url,logo);
     }catch(Exception ignored){}
    }
    success=true;result=changed?"Katalog channel berhasil diperbarui":"Katalog sudah terbaru";
   }catch(Exception ignored){}
   final String data=next,notice=result;final long rev=revision;final boolean ok=success,updated=changed;
   handler.post(()->{if(catalogClosed)return;catalogRefreshing=false;catalogStatus=notice;catalogNextCheck=System.currentTimeMillis()+(ok?4*60*60*1000L:15*60*1000L);
    if(ok){prefs.edit().putLong("catalog-checked",System.currentTimeMillis()).apply();onlineLogos.clear();onlineLogos.putAll(logos);catalogRevision=rev;catalogSnapshot=data;if(updated||!logos.isEmpty())applyCatalog(data);}
    if(manual)message(notice+(catalogPending?" · diterapkan setelah selesai menonton":""));
   });
  });
 }
 static String hexDigest(String s)throws Exception{byte[] bytes=java.security.MessageDigest.getInstance("SHA-256").digest(s.getBytes("UTF-8"));StringBuilder out=new StringBuilder();for(byte b:bytes)out.append(String.format(Locale.ROOT,"%02x",b&255));return out.toString();}
 static byte[] readBytes(File file)throws IOException{try(InputStream in=new FileInputStream(file);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>262144)throw new IOException("Too large");out.write(b,0,n);}return out.toByteArray();}}

 int accent(){return theme==1?0xffb4c9ef:theme==2?0xffd3bf9f:0xff9abce8;}
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().setFlags(1024,1024);getWindow().addFlags(128);setVolumeControlStream(AudioManager.STREAM_MUSIC);immersive();prefs=getSharedPreferences("nusatv",MODE_PRIVATE);favorites.addAll(prefs.getStringSet("favorites",new HashSet<String>()));theme=prefs.getInt("theme",0)%3;screenMode=VideoLayout.mode(prefs.getInt("video-screen-mode",VideoLayout.FIT));
  try{bundledCatalog=read(getAssets().open("channels.json"),8000000);try{catalogSnapshot=read(new android.util.AtomicFile(new File(getFilesDir(),"catalog-v1.json")).openRead(),CatalogStore.MAX_BYTES);CatalogStore.merge(bundledCatalog,catalogSnapshot);catalogRevision=CatalogStore.revision(catalogSnapshot);}catch(Exception ignored){catalogSnapshot="";}if(!catalogSnapshot.isEmpty())catalogNextCheck=prefs.getLong("catalog-checked",0)+4*60*60*1000L;applyCatalog(catalogSnapshot);}catch(Exception e){status="Daftar channel tidak dapat dibuka";}

  root=new FrameLayout(this);root.setBackgroundColor(Color.BLACK);root.setClipChildren(true);surface=new SurfaceView(this);surface.setVisibility(View.GONE);root.addView(surface,new FrameLayout.LayoutParams(-1,-1));stretchSurface=new TextureView(this);stretchSurface.setVisibility(View.GONE);root.addView(stretchSurface,new FrameLayout.LayoutParams(-1,-1));subtitles=new SubtitleView(this);subtitles.setUserDefaultStyle();subtitles.setFractionalTextSize(.043f);subtitles.setBottomPaddingFraction(.12f);root.addView(subtitles,new FrameLayout.LayoutParams(-1,-1));ui=new TvView();root.addView(ui,new FrameLayout.LayoutParams(-1,-1));busy=new android.widget.ProgressBar(this);busy.setIndeterminate(true);busy.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(0xffa9dfff));busy.setVisibility(View.GONE);FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(44,44,Gravity.CENTER);bp.topMargin=-90;root.addView(busy,bp);previewSurface=new android.view.TextureView(this);previewSurface.setVisibility(View.GONE);root.addView(previewSurface,new FrameLayout.LayoutParams(1,1));root.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{float sc=Math.min(root.getWidth()/1280f,root.getHeight()/720f);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(Math.round(HomeLayout.PREVIEW_W*sc),Math.round(HomeLayout.PREVIEW_H*sc));lp.leftMargin=Math.round((root.getWidth()-1280*sc)/2+HomeLayout.PREVIEW_X*sc);lp.topMargin=Math.round((root.getHeight()-720*sc)/2+HomeLayout.PREVIEW_Y*sc);previewSurface.setLayoutParams(lp);if(cloudDashboard!=null)cloudDashboard.layoutViewport();if(playing)applyVideoLayout();});setContentView(root);
  session=new MediaSession(this,"ELIO TV");session.setCallback(new MediaSession.Callback(){public void onPlay(){if(playing&&(paused||error))pauseToggle();}public void onPause(){if(playing&&!paused)pauseToggle();}public void onSkipToNext(){if(playing)next(1);}public void onSkipToPrevious(){if(playing)next(-1);}public void onStop(){stopPlayback();}});filter();handler.post(ticker);handler.post(epgTicker);
 }
 String read(InputStream in,int max)throws IOException{try{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n,total=0;while((n=in.read(b))!=-1){total+=n;if(total>max)throw new IOException("Data terlalu besar");out.write(b,0,n);}return out.toString("UTF-8");}finally{in.close();}}
 void immersive(){getWindow().getDecorView().setSystemUiVisibility(5894);}
 @Override public void onWindowFocusChanged(boolean b){super.onWindowFocusChanged(b);if(b)immersive();}
 void filter(){heroFocus=-1;filterFocus=-1;visible.clear();for(Channel c:channels){if(section==1&&!c.region.equals("Indonesia"))continue;if(section==2&&!c.region.equals("Internasional"))continue;if(section==3&&!c.section.equals("Olahraga")&&!c.name.equals("TVRI Sport"))continue;if(section==4&&!favorites.contains(c.id))continue;if(!query.isEmpty()&&!c.name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))continue;visible.add(c);}selected=Math.min(selected,Math.max(0,visible.size()-1));rowOffset=0;ensureVisible();redraw();}
 void redraw(){if(ui!=null){syncPreview();if(busy!=null)busy.setVisibility(playing&&buffering&&!error?View.VISIBLE:View.GONE);ui.invalidate();}}
 void ensureVisible(){if(ui!=null)ui.resetTouchScroll();rowOffset=selected/6;}
 void message(String s){toast=s;toastUntil=System.currentTimeMillis()+2800;redraw();}
 Channel current(){return playQueue.isEmpty()?null:playQueue.get(Math.min(playerIndex,playQueue.size()-1));}
 Server currentServer(){Channel c=current();return c==null||c.servers.isEmpty()?null:c.servers.get(Math.min(serverIndex,c.servers.size()-1));}
 void toggleFavorite(Channel c){if(c==null)return;if(favorites.remove(c.id))message("Dihapus dari favorit");else{favorites.add(c.id);message("Ditambahkan ke favorit");}prefs.edit().putStringSet("favorites",new HashSet<>(favorites)).apply();if(!playing&&section==4)filter();redraw();}
 void chooseNav(){if(navFocus<5){section=navFocus;selected=0;filter();nav=false;}else if(navFocus==5)search();else if(navFocus==6)films();else settings();redraw();}

 String epgTime(long time){return new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date(time));}
 String nowLine(Channel c){EpgStore.Programme p=epg.at(c.id,System.currentTimeMillis()).now;return p==null&&!c.liveLabel.isEmpty()?"Siaran langsung · "+c.liveLabel:p==null?(epgRefreshing&&c.epgIds!=null&&c.epgIds.length()>0?"Memuat jadwal acara…":"Jadwal belum tersedia"):"Sedang tayang · "+epgTime(p.start)+"–"+epgTime(p.end)+"  "+p.title;}
 String nextLine(Channel c){EpgStore.Programme p=epg.at(c.id,System.currentTimeMillis()).next;return p==null&&!c.liveLabel.isEmpty()?c.owner:p==null?"Berikutnya: jadwal belum tersedia":"Berikutnya · "+epgTime(p.start)+"  "+p.title;}
 String cardProgramme(Channel c){EpgStore.Programme p=epg.at(c.id,System.currentTimeMillis()).now;return p==null&&!c.liveLabel.isEmpty()?"Live · "+c.liveLabel:p==null?"Jadwal belum tersedia":epgTime(p.start)+" · "+p.title;}
 Map<String,String> epgAliases(String feed){Map<String,String> a=new HashMap<>();for(Channel c:channels)if(c.epgIds!=null){String id=c.epgIds.optString(feed);if(!id.isEmpty())a.put(id,c.id);}return a;}
 Map<String,String> epgNames(){Map<String,String> a=new HashMap<>();for(Channel c:channels)a.put(c.id,c.name);return a;}
 void epgInfo(){long synced=prefs.getLong("epg-synced",0);String checked=synced==0?"Belum tersinkron":new SimpleDateFormat("d MMM yyyy HH:mm",new Locale("id","ID")).format(new Date(synced));new AlertDialog.Builder(this).setTitle("Jadwal acara / EPG").setMessage("Sinkronisasi terakhir: "+checked+"\n\nJudul berganti otomatis mengikuti waktu jadwal. Data diperbarui setiap 4 jam saat aplikasi dibuka. Waktu mengikuti pengaturan perangkat.\n\nSumber: apistech/project dan iptv-epg.org (juga digunakan FaaeTV); dhasap/dhanytv; AqFad2811/epg (Indonesia dan RTMKlik). Tidak semua channel memiliki data. Siaran dapat berubah atau terlambat dari jadwal. EPG bukan deteksi isi video.\n\nData yang sudah berakhir tidak ditampilkan sebagai Sedang tayang.").setPositiveButton(epgRefreshing?"Sedang memuat":"Perbarui sekarang",(d,w)->{refreshEpg(true);message(epgRefreshing?"Memperbarui jadwal…":"Jadwal sedang diperbarui");}).setNegativeButton("Tutup",null).show();}
 void refreshEpg(boolean force){long now=System.currentTimeMillis();if(epgClosed||epgRefreshing||(!force&&now<epgNextRefresh))return;epgRefreshing=true;epgNextRefresh=now+900000;final Map<String,String> names=epgNames();final ArrayList<Map<String,String>> maps=new ArrayList<>();for(String f:epgFeeds)maps.add(epgAliases(f));epgIo.execute(()->{
  File dir=new File(getFilesDir(),"epg");dir.mkdirs();int success=0;
  try{
   if(!epgCacheLoaded){for(int i=0;i<epgFeeds.length&&!epgClosed;i++){String feed=epgFeeds[i];try(InputStream in=new java.util.zip.GZIPInputStream(getAssets().open("epg-"+feed+".xmlz"))){epg.replace(feed,EpgStore.parse(in,feed,maps.get(i),names));}catch(Exception ignored){}File cached=new File(dir,feed+".xml");if(cached.isFile())try(InputStream in=new FileInputStream(cached)){Map<String,List<EpgStore.Programme>> rows=EpgStore.parse(in,feed,maps.get(i),names);if(!rows.isEmpty())epg.replace(feed,rows);}catch(Exception ignored){}}
    epgCacheLoaded=true;handler.post(()->{if(!epgClosed)redraw();});}
   for(int i=0;i<epgFeeds.length&&!epgClosed;i++){String feed=epgFeeds[i];File cached=new File(dir,feed+".xml"),tmp=new File(dir,feed+".tmp");HttpURLConnection con=null;
    try{con=(HttpURLConnection)new URL(epgUrls[i]).openConnection();con.setConnectTimeout(10000);con.setReadTimeout(15000);con.setRequestProperty("User-Agent","ELIO-TV/1.8.1 EPG");if(cached.isFile())con.setIfModifiedSince(cached.lastModified());int code=con.getResponseCode();if(code==304&&cached.isFile()){success++;continue;}if(code!=200)throw new IOException("EPG HTTP "+code);
     try(PushbackInputStream raw=new PushbackInputStream(con.getInputStream(),2)){int a=raw.read(),b=raw.read();if(b>=0)raw.unread(b);if(a>=0)raw.unread(a);InputStream in=a==31&&b==139?new java.util.zip.GZIPInputStream(raw):raw;try(FileOutputStream out=new FileOutputStream(tmp)){byte[] buf=new byte[16384];int n,total=0;while((n=in.read(buf))!=-1){if(epgClosed||Thread.currentThread().isInterrupted())throw new InterruptedIOException();total+=n;if(total>16000000)throw new IOException("EPG too large");out.write(buf,0,n);}}}
     Map<String,List<EpgStore.Programme>> rows;try(InputStream in=new FileInputStream(tmp)){rows=EpgStore.parse(in,feed,maps.get(i),names);}if(rows.isEmpty())throw new IOException("No usable listings");if(epgClosed)break;epg.replace(feed,rows);if(!tmp.renameTo(cached))tmp.delete();success++;
    }catch(Exception ignored){tmp.delete();}finally{if(con!=null)con.disconnect();}
   }
  }finally{final int updated=success;handler.post(()->{if(epgClosed)return;epgRefreshing=false;if(updated>0){prefs.edit().putLong("epg-synced",System.currentTimeMillis()).apply();epgNextRefresh=System.currentTimeMillis()+14400000;}redraw();});}
 });}
 void search(){final EditText edit=input("Nama channel",query);new AlertDialog.Builder(this).setTitle("Cari channel").setView(edit).setPositiveButton("Cari",(d,w)->{query=edit.getText().toString().trim();selected=0;filter();nav=false;}).setNeutralButton("Hapus",(d,w)->{query="";filter();nav=false;}).setNegativeButton("Batal",null).show();}
 EditText input(String hint,String value){EditText e=new EditText(this);e.setSingleLine(true);e.setText(value);e.setHint(hint);e.setTextColor(Color.WHITE);e.setHintTextColor(0xff929ba6);e.setPadding(28,20,28,20);return e;}
 void settings(){new AlertDialog.Builder(this).setTitle("Pengaturan ELIO TV").setItems(new String[]{"Pengaturan layar · "+screenModeName(),"Tema tampilan","Tambahkan playlist M3U","Panduan remote","Tentang ELIO TV","Jadwal acara / EPG","Perbarui channel","Film & Cloudstream · Percobaan"},(d,w)->{if(w==0){screenModeDialog();return;}w--;if(w==0)new AlertDialog.Builder(this).setTitle("Tema").setSingleChoiceItems(new String[]{"Grafit","Midnight","Sandstone"},theme,(dd,i)->{theme=i;prefs.edit().putInt("theme",i).apply();redraw();dd.dismiss();}).setNegativeButton("Tutup",null).show();else if(w==1)importPlaylist();else if(w==2)new AlertDialog.Builder(this).setTitle("Kontrol remote").setMessage("Beranda\nArah: pilih channel atau menu\nOK: tonton\nTahan OK: favorit\n\nSaat menonton\nAtas/bawah atau CH +/−: ganti channel\nOK / Play / Pause: jeda atau lanjut\nKanan: daftar channel\nMenu: server, kualitas, audio, subtitle\nBack: kembali\n\nVolume mengikuti pengaturan perangkat.\n\nHP / layar sentuh\nGeser daftar ke atas/bawah untuk scroll.\nKetuk channel untuk menonton.\nTahan channel untuk favorit.\nKetuk video untuk menampilkan kontrol.\nTombol Jeda, Daftar, Pilihan dan Beranda dapat disentuh.").setPositiveButton("Tutup",null).show();else if(w==4)epgInfo();else if(w==5)catalogInfo();else if(w==6)films();else new AlertDialog.Builder(this).setTitle("ELIO TV 1.9.3-cloud-test8").setMessage("Televisi Indonesia dan internasional untuk Android TV/STB.\n\nDaftar mencakup channel nasional, berita, anak, religi, TVRI, dan internasional. Satu channel dapat memiliki beberapa server. Server gagal dilewati otomatis. Sumber terakhir yang berhasil diprioritaskan.\n\nSumber dapat dibatasi wilayah, jaringan, atau lisensi siaran. Channel yang tercantum belum tentu tersedia setiap saat. TVMU, JakTV, dan Ajwa TV belum memiliki sumber IPTV yang berhasil diuji. Gunakan layanan resmi melalui info ketersediaan.\n\nTanpa login ELIO TV. Tidak ada izin penyimpanan atau kontak.").setPositiveButton("Tutup",null).show();}).setNegativeButton("Kembali",null).show();}
 void importPlaylist(){final EditText edit=input("https://…/playlist.m3u","");edit.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_URI);new AlertDialog.Builder(this).setTitle("Tambahkan playlist M3U").setView(edit).setPositiveButton("Tambahkan",(d,w)->{String u=edit.getText().toString().trim();if(!u.startsWith("https://")&&!u.startsWith("http://")){message("Masukkan URL HTTP atau HTTPS");return;}message("Memuat playlist…");io.execute(()->{try{HttpURLConnection con=(HttpURLConnection)new URL(u).openConnection();con.setConnectTimeout(12000);con.setReadTimeout(15000);con.setRequestProperty("User-Agent","ELIO-TV/1.1");String text=read(con.getInputStream(),8000000);JSONArray imported=parseM3u(text,u);if(imported.length()==0)throw new IOException("Playlist kosong");handler.post(()->{try{JSONArray old=new JSONArray(prefs.getString("customChannels","[]"));for(int i=0;i<imported.length();i++){JSONObject j=imported.getJSONObject(i);boolean exists=false;for(Channel c:channels)if(c.id.equals(j.optString("id")))exists=true;if(!exists){old.put(j);channels.add(new Channel(j));}}prefs.edit().putString("customChannels",old.toString()).apply();section=0;navFocus=0;query="";filter();message("Playlist ditambahkan");}catch(Exception e){message("Playlist tidak dapat disimpan");}});}catch(Exception e){handler.post(()->message("Playlist tidak dapat dibuka"));}});}).setNegativeButton("Batal",null).show();}
 JSONArray parseM3u(String text,String base)throws Exception{JSONArray rows=new JSONArray();String name="",group="Playlist",key="",kid="",drm="";JSONObject h=new JSONObject();for(String raw:text.split("\\r?\\n")){String l=raw.trim();if(l.startsWith("#EXTINF")){name=l.substring(l.lastIndexOf(',')+1).trim();h=new JSONObject();key="";kid="";drm="";}else if(l.startsWith("#EXTVLCOPT:http-user-agent="))h.put("User-Agent",l.substring(l.indexOf('=')+1));else if(l.startsWith("#EXTVLCOPT:http-referrer="))h.put("Referer",l.substring(l.indexOf('=')+1));else if(l.startsWith("#EXTVLCOPT:http-origin="))h.put("Origin",l.substring(l.indexOf('=')+1));else if(l.startsWith("#KODIPROP:inputstream.adaptive.license_key=")){String v=l.substring(l.indexOf('=')+1);if(v.startsWith("http")){key=v;drm="widevine";}else if(v.contains(":")){String[] a=v.split(":",2);kid=a[0];key=a[1];drm="clearkey";}}else if(!l.isEmpty()&&!l.startsWith("#")){String u=new URL(new URL(base),l.split("\\|",2)[0]).toString();if(!u.startsWith("http"))continue;JSONObject s=new JSONObject().put("name","Server 1").put("url",u).put("headers",h).put("drm",drm).put("licenseUrl",drm.equals("widevine")?key:"").put("key",drm.equals("clearkey")?key:"").put("keyId",kid);JSONObject j=new JSONObject().put("id","custom-"+Integer.toHexString(u.hashCode())).put("name",name.isEmpty()?"Channel "+(rows.length()+1):name).put("group","Playlist").put("section",group).put("owner","Playlist pribadi").put("servers",new JSONArray().put(s));rows.put(j);if(rows.length()>=2000)break;}}JSONArray grouped=new JSONArray();HashMap<String,JSONObject> map=new HashMap<>();for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);String n=row.optString("name");JSONObject existing=map.get(n.toLowerCase(Locale.ROOT));if(existing==null){row.put("id","custom-"+Integer.toHexString(n.toLowerCase(Locale.ROOT).hashCode()));map.put(n.toLowerCase(Locale.ROOT),row);grouped.put(row);}else existing.getJSONArray("servers").put(row.getJSONArray("servers").getJSONObject(0));}return grouped;}
 void play(int index){if(visible.isEmpty())return;stopPreview();previewId="";playQueue.clear();playQueue.addAll(visible);openChannel(Math.max(0,Math.min(index,playQueue.size()-1)));}
 void openChannel(int index){Channel c=playQueue.get(index);if(!"Film".equals(c.section)){cloudResumePosition=0;if(cloudUi!=null)cloudUi.cancelPlayback();}if(c.servers.isEmpty()){unavailable(c);return;}playerIndex=index;playing=true;drawer=false;paused=false;error=false;attempt=0;videoHeight=0;videoWidth=0;videoPixelRatio=1f;triedServers.clear();serverIndex=preferredSource(c);surface.setVisibility(View.VISIBLE);applyVideoLayout();subtitles.setVisibility(View.VISIBLE);overlayUntil=System.currentTimeMillis()+5500;startMedia();}

 String sourceFailureKey(Server s){return "failed-source-"+s.url;}
 int preferredSource(Channel c){long now=System.currentTimeMillis();String last=prefs.getString("good-source-"+c.id,"");int first=-1,good=-1;for(int i=0;i<c.servers.size();i++){Server sv=c.servers.get(i);if(prefs.getLong(sourceFailureKey(sv),0)>now){triedServers.add(i);continue;}if(first<0)first=i;if(sv.url.equals(last))good=i;}if(first<0){triedServers.clear();return 0;}return good>=0?good:first;}
 String playbackError(PlaybackException e){Throwable t=e;for(int i=0;t!=null&&i<12;i++,t=t.getCause()){if(t instanceof HttpDataSource.InvalidResponseCodeException){int code=((HttpDataSource.InvalidResponseCodeException)t).responseCode;if(code==403||code==401)return "Akses sumber ditolak ("+code+")";if(code==404||code==410)return "Sumber tidak ditemukan ("+code+")";if(code>=500)return "Server siaran bermasalah ("+code+")";}}if(e.errorCode==PlaybackException.ERROR_CODE_DRM_LICENSE_ACQUISITION_FAILED)return "Lisensi siaran tidak tersedia";if(e.errorCode==PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT)return "Sumber melewati batas waktu";if(e.errorCode==PlaybackException.ERROR_CODE_PARSING_MANIFEST_MALFORMED)return "Playlist siaran tidak valid";if(e.errorCode==PlaybackException.ERROR_CODE_DECODING_FAILED)return "Video tidak dapat didekode perangkat";return "Sambungan siaran gagal";}
 void unavailable(Channel c){AlertDialog.Builder b=new AlertDialog.Builder(this).setTitle(c.name).setMessage(c.availabilityNote.isEmpty()?"Ketersediaan mengikuti server penyiar. Jika gagal, ELIO TV mencoba sumber lain secara otomatis.":c.availabilityNote);if(!c.officialUrl.isEmpty())b.setPositiveButton("Buka layanan resmi",(d,w)->{try{startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,android.net.Uri.parse(c.officialUrl)));}catch(Exception e){message("Tidak ada aplikasi untuk membuka layanan resmi");}});b.setNegativeButton("Kembali",null).show();}
 void releasePlayer(){cancelTimeline();generation++;if(timeout!=null)handler.removeCallbacks(timeout);timeout=null;if(retry!=null)handler.removeCallbacks(retry);retry=null;if(player!=null){player.release();player=null;}videoUsesTexture=false;videoSurfaceAttached=false;prepared=false;if(subtitles!=null)subtitles.setCues(Collections.emptyList());}
 void armTimeout(final int token){if(timeout!=null)return;timeout=()->{timeout=null;if(token==generation&&playing&&!paused&&(!firstFrame||buffering))fail(token,"Server tidak merespons");};handler.postDelayed(timeout,firstFrame?8000:ServerFailover.startupTimeoutMs(currentServer()!=null&&!currentServer().drm.isEmpty()));}
 byte[] keyResponse(Server s)throws Exception{return new JSONObject().put("keys",new JSONArray().put(new JSONObject().put("kty","oct").put("kid",base64Key(s.keyId)).put("k",base64Key(s.key)))).put("type","temporary").toString().getBytes("UTF-8");}
 String base64Key(String s){if(s.matches("[0-9a-fA-F]{32}")){byte[] a=new byte[16];for(int i=0;i<16;i++)a[i]=(byte)Integer.parseInt(s.substring(i*2,i*2+2),16);return android.util.Base64.encodeToString(a,android.util.Base64.URL_SAFE|android.util.Base64.NO_WRAP|android.util.Base64.NO_PADDING);}return s.replace('+','-').replace('/','_').replace("=","");}
 void startMedia(){releasePlayer();if(!playing||currentServer()==null)return;final int token=generation;firstFrame=false;Server server=currentServer();buffering=true;error=false;status="Menghubungkan";videoHeight=0;videoWidth=0;videoPixelRatio=1f;applyVideoLayout();redraw();
  try{HashMap<String,String> h=new HashMap<>(server.headers);String ua=h.containsKey("User-Agent")?h.remove("User-Agent"):"Mozilla/5.0";DefaultHttpDataSource.Factory http=new DefaultHttpDataSource.Factory().setUserAgent(ua).setDefaultRequestProperties(h).setConnectTimeoutMs(4000).setReadTimeoutMs(5000).setAllowCrossProtocolRedirects(true);DefaultDataSource.Factory data=new DefaultDataSource.Factory(this,http);DefaultMediaSourceFactory factory=new DefaultMediaSourceFactory(data).setLoadErrorHandlingPolicy(new androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy(0));
   MediaItem.Builder item=new MediaItem.Builder().setUri(server.url);if(!server.mimeType.isEmpty())item.setMimeType(server.mimeType);else if(server.url.toLowerCase(Locale.ROOT).contains(".mpd"))item.setMimeType(MimeTypes.APPLICATION_MPD);else if(server.url.contains(".m3u")||server.url.contains("workers.dev"))item.setMimeType(MimeTypes.APPLICATION_M3U8);
   if(server.drm.equals("widevine")&&!server.licenseUrl.isEmpty()){item.setDrmConfiguration(new MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID).setLicenseUri(server.licenseUrl).setLicenseRequestHeaders(server.headers).build());DefaultDrmSessionManagerProvider provider=new DefaultDrmSessionManagerProvider();provider.setDrmHttpDataSourceFactory(http);factory.setDrmSessionManagerProvider(provider);}else if(server.drm.equals("clearkey")){item.setDrmConfiguration(new MediaItem.DrmConfiguration.Builder(C.CLEARKEY_UUID).build());final DrmSessionManager manager=new DefaultDrmSessionManager.Builder().setUuidAndExoMediaDrmProvider(C.CLEARKEY_UUID,FrameworkMediaDrm.DEFAULT_PROVIDER).build(new LocalMediaDrmCallback(keyResponse(server)));factory.setDrmSessionManagerProvider(mediaItem->manager);}
   player=new ExoPlayer.Builder(this).setMediaSourceFactory(factory).setLoadControl(new androidx.media3.exoplayer.DefaultLoadControl.Builder().setBufferDurationsMs(5000,15000,750,1500).build()).build();final ExoPlayer p=player;p.setAudioAttributes(new androidx.media3.common.AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(),true);p.setHandleAudioBecomingNoisy(true);applyVideoLayout();p.setTrackSelectionParameters(p.getTrackSelectionParameters().buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_TEXT,true).build());applyQuality();
   p.addListener(new Player.Listener(){public void onPlaybackStateChanged(int state){if(token!=generation)return;if(state==Player.STATE_READY){prepared=true;buffering=false;error=false;if(firstFrame){if(timeout!=null)handler.removeCallbacks(timeout);timeout=null;}overlayUntil=System.currentTimeMillis()+4500;}else if(state==Player.STATE_BUFFERING){buffering=true;status=prepared?"Buffering":"Menghubungkan";armTimeout(token);}else if(state==Player.STATE_ENDED){if(current()!=null&&"Film".equals(current().section)){stopPlayback();message("Video selesai");}else fail(token,"Siaran berakhir");}updateSession();redraw();}public void onPlayerError(PlaybackException e){if(token!=generation)return;if(e.errorCode==PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW){p.seekToDefaultPosition();p.prepare();armTimeout(token);message("Rentang putar ulang berakhir · kembali ke siaran langsung");}else fail(token,playbackError(e));}public void onRenderedFirstFrame(){if(token!=generation)return;firstFrame=true;attempt=0;applyVideoLayout();if(cloudResumePosition>0){long target=cloudResumePosition;cloudResumePosition=0;if(p.isCurrentMediaItemSeekable())p.seekTo(p.getDuration()>0?Math.min(target,Math.max(0,p.getDuration()-1000)):target);}if(timeout!=null)handler.removeCallbacks(timeout);timeout=null;prefs.edit().putInt("server-"+current().id,serverIndex).putString("good-source-"+current().id,currentServer().url).remove(sourceFailureKey(currentServer())).apply();}public void onVideoSizeChanged(VideoSize v){if(token!=generation)return;videoWidth=v.width;videoHeight=v.height;videoPixelRatio=v.pixelWidthHeightRatio;applyVideoLayout();redraw();}public void onCues(CueGroup cues){if(token==generation)subtitles.setCues(cues.cues);}public void onIsPlayingChanged(boolean b){if(token==generation){paused=!p.getPlayWhenReady();updateSession();redraw();}}});
   p.setMediaItem(item.build());p.prepare();p.setPlayWhenReady(!paused);armTimeout(token);updateSession();
  }catch(Exception e){fail(token,"Server tidak didukung perangkat");}
 }
 void cloudResolveStopped(String why){if(playing&&current()!=null&&"Film".equals(current().section)){buffering=false;error=true;status=why;overlayUntil=System.currentTimeMillis()+6500;updateSession();redraw();}}
 void applyVideoLayout(){if(root==null||surface==null)return;boolean texture=screenMode==VideoLayout.STRETCH&&(currentServer()==null||currentServer().drm.isEmpty());if(player!=null&&(!videoSurfaceAttached||texture!=videoUsesTexture)){if(texture){if(videoSurfaceAttached)player.clearVideoSurfaceView(surface);player.setVideoTextureView(stretchSurface);}else{if(videoSurfaceAttached)player.clearVideoTextureView(stretchSurface);player.setVideoSurfaceView(surface);}videoUsesTexture=texture;videoSurfaceAttached=true;}if(stretchSurface!=null)stretchSurface.setVisibility(playing&&texture?View.VISIBLE:View.GONE);surface.setVisibility(playing&&!texture?View.VISIBLE:View.GONE);int[] size=VideoLayout.size(root.getWidth(),root.getHeight(),videoWidth*videoPixelRatio,videoHeight,screenMode);if(size[0]<=0||size[1]<=0)return;FrameLayout.LayoutParams prior=(FrameLayout.LayoutParams)surface.getLayoutParams();if(prior.width!=size[0]||prior.height!=size[1]||prior.gravity!=Gravity.CENTER)surface.setLayoutParams(new FrameLayout.LayoutParams(size[0],size[1],Gravity.CENTER));if(stretchSurface!=null){FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)stretchSurface.getLayoutParams();if(lp.width!=root.getWidth()||lp.height!=root.getHeight())stretchSurface.setLayoutParams(new FrameLayout.LayoutParams(root.getWidth(),root.getHeight(),Gravity.CENTER));}}
 String screenModeName(){return new String[]{"Otomatis (pas layar)","Penuhi layar","Regangkan"}[VideoLayout.mode(screenMode)];}
 void screenModeDialog(){new AlertDialog.Builder(this).setTitle("Pengaturan layar (global)").setSingleChoiceItems(new String[]{"Otomatis / pas layar · gambar utuh","Penuhi layar · tepi gambar dipotong","Regangkan · gambar bisa melebar"},screenMode,(dialog,which)->{screenMode=VideoLayout.mode(which);prefs.edit().putInt("video-screen-mode",screenMode).apply();applyVideoLayout();overlayUntil=System.currentTimeMillis()+4500;dialog.dismiss();message("Layar: "+screenModeName());}).setNegativeButton("Tutup",null).show();}
 void fail(final int token,String why){if(token!=generation||!playing||error||retry!=null)return;if(timeout!=null)handler.removeCallbacks(timeout);timeout=null;triedServers.add(serverIndex);if(currentServer()!=null)prefs.edit().putLong(sourceFailureKey(currentServer()),System.currentTimeMillis()+300000).apply();int next=ServerFailover.next(serverIndex,current().servers.size(),triedServers);if(next>=0){serverIndex=next;attempt=0;status="Beralih ke Server "+(serverIndex+1);buffering=true;retry=()->{if(token==generation&&playing)startMedia();};handler.postDelayed(retry,150);}else if(current().servers.size()==1&&attempt==0){attempt=1;status="Menyambungkan ulang";buffering=true;retry=()->{if(token==generation&&playing)startMedia();};handler.postDelayed(retry,300);}else{long position=player==null?0:player.getCurrentPosition();if(current()!=null&&"Film".equals(current().section)&&cloudUi!=null&&cloudUi.playbackFailed(current().id,position)){releasePlayer();buffering=true;status="Mencari provider film cadangan";redraw();return;}error=true;buffering=false;status="Semua server telah dicoba · "+why;paused=false;if(player!=null)player.stop();}overlayUntil=System.currentTimeMillis()+6500;updateSession();redraw();}
 void updateSession(){if(session==null)return;session.setActive(playing);if(current()!=null)session.setMetadata(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE,current().name).putString(MediaMetadata.METADATA_KEY_ARTIST,"ELIO TV").build());session.setPlaybackState(new PlaybackState.Builder().setActions(PlaybackState.ACTION_PLAY|PlaybackState.ACTION_PAUSE|PlaybackState.ACTION_PLAY_PAUSE|PlaybackState.ACTION_SKIP_TO_NEXT|PlaybackState.ACTION_SKIP_TO_PREVIOUS|PlaybackState.ACTION_STOP).setState(!playing?PlaybackState.STATE_STOPPED:error?PlaybackState.STATE_ERROR:paused?PlaybackState.STATE_PAUSED:buffering?PlaybackState.STATE_BUFFERING:PlaybackState.STATE_PLAYING,PlaybackState.PLAYBACK_POSITION_UNKNOWN,1).build());}
 void stopPlayback(){if(cloudUi!=null)cloudUi.cancelPlayback();cloudResumePosition=0;releasePlayer();playing=false;drawer=false;buffering=false;paused=false;error=false;surface.setVisibility(View.GONE);stretchSurface.setVisibility(View.GONE);subtitles.setVisibility(View.GONE);updateSession();Channel ch=current();if(ch!=null)for(int i=0;i<visible.size();i++)if(visible.get(i).id.equals(ch.id)){selected=i;break;}ensureVisible();if(section==4)filter();if(catalogPending){catalogPending=false;applyCatalog(catalogSnapshot);}redraw();if(cloudDashboard!=null)cloudDashboard.returnFromPlayback();}
 void pauseToggle(){overlayUntil=System.currentTimeMillis()+4500;if(error){attempt=0;triedServers.clear();startMedia();return;}if(player==null)return;paused=player.getPlayWhenReady();player.setPlayWhenReady(!paused);if(!paused&&(!firstFrame||buffering))armTimeout(generation);updateSession();redraw();}
 void next(int d){if(playQueue.isEmpty())return;for(int i=1;i<=playQueue.size();i++){int j=(playerIndex+d*i+playQueue.size()*2)%playQueue.size();if(!playQueue.get(j).servers.isEmpty()){openChannel(j);return;}}}
 void playbackOptions(){if(current()==null)return;new AlertDialog.Builder(this).setTitle(current().name).setItems(new String[]{"Pilih server ("+(serverIndex+1)+"/"+current().servers.size()+")","Kualitas video · "+quality,"Bahasa audio","Subtitle",favorites.contains(current().id)?"Hapus dari favorit":"Tambah ke favorit","Info ketersediaan","Putar ulang / posisi siaran","Acara sebelumnya / arsip","Pengaturan layar · "+screenModeName()},(d,w)->{if(w==0)serverDialog();else if(w==1)qualityDialog();else if(w==2)trackDialog(C.TRACK_TYPE_AUDIO);else if(w==3)trackDialog(C.TRACK_TYPE_TEXT);else if(w==4)toggleFavorite(current());else if(w==6)seekOptions();else if(w==7)previousProgrammes();else if(w==8)screenModeDialog();else unavailable(current());}).setNegativeButton("Tutup",null).show();}


 boolean timelineFocus=false,timelineDragging=false;long timelineDraft=0,timelineStart=-1;int timelineToken=-1;
 long windowStart(){if(player==null||player.getCurrentTimeline().isEmpty())return -1;Timeline.Window w=new Timeline.Window();player.getCurrentTimeline().getWindow(player.getCurrentMediaItemIndex(),w);return w.windowStartTimeMs==C.TIME_UNSET?-1:w.windowStartTimeMs;}
 void beginTimeline(){if(!canSeek()){message("Sumber belum menyediakan putar ulang");return;}timelineFocus=true;timelineToken=generation;timelineStart=windowStart();timelineDraft=player.getCurrentPosition();overlayUntil=System.currentTimeMillis()+10000;redraw();}
 void cancelTimeline(){timelineFocus=false;timelineDragging=false;timelineToken=-1;}
 void commitTimeline(){if(!timelineFocus)return;long selected=timelineDraft,oldStart=timelineStart;boolean valid=timelineToken==generation&&canSeek();cancelTimeline();if(!valid){message("Channel atau rentang video telah berubah");return;}try{long position=SeekWindow.rebase(selected,oldStart,windowStart(),player.getDuration());player.seekTo(position);}catch(IllegalArgumentException e){message("Bagian ini sudah di luar rentang putar ulang");}overlayUntil=System.currentTimeMillis()+6000;redraw();}
 void moveTimeline(long delta){if(!canSeek()){cancelTimeline();return;}timelineDraft=SeekWindow.clamp(timelineDraft+delta,player.getDuration());overlayUntil=System.currentTimeMillis()+10000;redraw();}
 void previousProgrammes(){
  Channel ch=current();if(ch==null)return;final int token=generation;final String id=ch.id;
  List<EpgStore.Programme> history=epg.history(ch.id,System.currentTimeMillis());long start=windowStart(),duration=canSeek()?player.getDuration():0;
  String[] labels=new String[history.size()+1];for(int i=0;i<history.size();i++){EpgStore.Programme item=history.get(i);boolean available=duration>0&&start>=0&&item.start>=start&&item.start<=start+duration;labels[i]=epgTime(item.start)+" · "+item.title+(available?" · Putar":" · Di luar DVR");}
  labels[history.size()]=ch.officialUrl.isEmpty()?"Tentang arsip tayangan":"Buka layanan resmi / arsip";
  new AlertDialog.Builder(this).setTitle(history.isEmpty()?"Jadwal sebelumnya belum tersedia":"Acara sebelumnya · "+ch.name).setItems(labels,(d,index)->{
   if(current()==null||!current().id.equals(id)||token!=generation)return;
   if(index==history.size()){if(!ch.officialUrl.isEmpty())new AlertDialog.Builder(this).setTitle("Arsip pada layanan resmi").setMessage("Arsip tayangan dapat memerlukan login atau paket dari penyedia. ELIO TV akan membuka layanan resminya. EPG bukan rekaman video.").setPositiveButton("Buka layanan",(dd,w)->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(ch.officialUrl)));}catch(Exception e){message("Tidak ada aplikasi untuk membuka layanan");}}).setNegativeButton("Batal",null).show();else archiveUnavailable();return;}
   EpgStore.Programme item=history.get(index);long currentStart=windowStart();if(!canSeek()||currentStart<0||item.start<currentStart||item.start>currentStart+player.getDuration()){archiveUnavailable();return;}
   cancelTimeline();player.seekTo(item.start-currentStart);overlayUntil=System.currentTimeMillis()+6000;redraw();
  }).setNegativeButton("Tutup",null).show();
 }
 void archiveUnavailable(){new AlertDialog.Builder(this).setTitle("Acara tidak tersedia di DVR").setMessage("Sumber IPTV ini tidak menyimpan bagian tersebut dalam rentang video yang tersedia. Untuk menonton acara sebelum siaran sekarang, diperlukan sumber catch-up atau arsip resmi. Memiliki jadwal EPG saja tidak cukup untuk memutar videonya.").setPositiveButton("Tutup",null).show();}

 boolean canSeek(){return player!=null&&playing&&!error&&player.isCurrentMediaItemSeekable()&&player.getDuration()!=C.TIME_UNSET&&player.getDuration()>0;}
 void seekOptions(){
  if(!canSeek()){new AlertDialog.Builder(this).setTitle("Putar ulang belum tersedia").setMessage("Sumber siaran ini belum menyediakan rentang waktu yang bisa diputar ulang. Jadwal acara / EPG hanya menunjukkan acara; bukan arsip video.\n\nAcara yang sudah lewat hanya bisa ditonton jika penyedia menyediakan DVR atau arsip tayangan.").setPositiveButton("Tutup",null).show();return;}
  long duration=player.getDuration(),position=player.getCurrentPosition();boolean live=player.isCurrentMediaItemLive();
  new AlertDialog.Builder(this).setTitle("Putar ulang · "+current().name+"\n"+SeekWindow.format(position)+" / "+SeekWindow.format(duration)).setItems(new String[]{"Mundur 30 detik","Maju 30 detik","Pilih menit…",live?"Kembali ke siaran langsung":"Ke awal tayangan","Geser timeline"},(d,w)->{if(!canSeek()){message("Rentang putar ulang sudah berubah");return;}if(w==0)seekBy(-30000);else if(w==1)seekBy(30000);else if(w==2)seekPositionDialog();else if(w==4)beginTimeline();else{player.seekToDefaultPosition();overlayUntil=System.currentTimeMillis()+6000;redraw();}}).setNegativeButton("Tutup",null).show();
 }
 void seekBy(long delta){if(!canSeek()){message("Sumber belum mendukung putar ulang");return;}player.seekTo(SeekWindow.clamp(player.getCurrentPosition()+delta,player.getDuration()));overlayUntil=System.currentTimeMillis()+6000;redraw();}
 void seekPositionDialog(){if(!canSeek())return;final ExoPlayer target=player;final int token=generation;final EditText edit=input("Menit atau mm:ss, contoh 5 atau 05:30",SeekWindow.format(player.getCurrentPosition()));edit.setInputType(InputType.TYPE_CLASS_DATETIME|InputType.TYPE_DATETIME_VARIATION_TIME);
  AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Pilih posisi siaran").setMessage("Masukkan posisi dari awal rentang yang tersedia, bukan jam tayang. Rentang saat ini 00:00–"+SeekWindow.format(player.getDuration())+". Pada siaran langsung, rentang terus bergeser.").setView(edit).setPositiveButton("Putar",null).setNegativeButton("Batal",null).create();
  dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{if(player!=target||generation!=token||!canSeek()){dialog.dismiss();message("Channel atau rentang siaran telah berubah");return;}try{long position=SeekWindow.parse(edit.getText().toString());if(position>player.getDuration()){edit.setError("Di luar rentang tersedia");return;}player.seekTo(position);overlayUntil=System.currentTimeMillis()+6000;redraw();dialog.dismiss();}catch(IllegalArgumentException e){edit.setError("Gunakan menit atau mm:ss, contoh 05:30");}}));dialog.show();
 }

 void serverDialog(){String[] names=new String[current().servers.size()];for(int i=0;i<names.length;i++)names[i]="Server "+(i+1)+(i==serverIndex?"  ·  Digunakan":"");new AlertDialog.Builder(this).setTitle("Server · "+current().name).setSingleChoiceItems(names,serverIndex,(d,w)->{serverIndex=w;attempt=0;triedServers.clear();paused=false;startMedia();d.dismiss();}).setNegativeButton("Kembali",null).show();}
 void qualityDialog(){String[] labels={"Auto","1080p","720p","480p","360p"};int i=Arrays.asList(labels).indexOf(quality);new AlertDialog.Builder(this).setTitle("Batas kualitas video").setSingleChoiceItems(labels,i,(d,w)->{quality=labels[w];applyQuality();message("Kualitas "+quality);d.dismiss();}).setNegativeButton("Tutup",null).show();}
 void applyQuality(){if(player==null)return;TrackSelectionParameters.Builder p=player.getTrackSelectionParameters().buildUpon();if(quality.equals("Auto"))p.clearVideoSizeConstraints();else{int h=Integer.parseInt(quality.replace("p",""));p.setMaxVideoSize(h*16/9,h);}player.setTrackSelectionParameters(p.build());}
 void trackDialog(int type){if(player==null)return;ArrayList<String> labels=new ArrayList<>();ArrayList<Tracks.Group> groups=new ArrayList<>();ArrayList<Integer> indexes=new ArrayList<>();if(type==C.TRACK_TYPE_TEXT){labels.add("Nonaktif");groups.add(null);indexes.add(-1);}for(Tracks.Group g:player.getCurrentTracks().getGroups()){if(g.getType()!=type)continue;for(int i=0;i<g.length;i++){if(!g.isTrackSupported(i))continue;Format f=g.getTrackFormat(i);String label=f.label;if(label==null||label.isEmpty())label=f.language==null?"Track "+(labels.size()+1):new Locale(f.language).getDisplayLanguage(new Locale("id"));labels.add(label+(g.isTrackSelected(i)?"  ·  Aktif":""));groups.add(g);indexes.add(i);}}if(labels.isEmpty()||(labels.size()==1&&type==C.TRACK_TYPE_TEXT)){message(type==C.TRACK_TYPE_TEXT?"Subtitle tidak disediakan server":"Pilihan audio belum tersedia");return;}new AlertDialog.Builder(this).setTitle(type==C.TRACK_TYPE_TEXT?"Subtitle":"Audio").setItems(labels.toArray(new String[0]),(d,w)->{TrackSelectionParameters.Builder b=player.getTrackSelectionParameters().buildUpon();if(groups.get(w)==null)b.setTrackTypeDisabled(type,true);else b.setTrackTypeDisabled(type,false).setOverrideForType(new TrackSelectionOverride(groups.get(w).getMediaTrackGroup(),indexes.get(w)));player.setTrackSelectionParameters(b.build());}).setNegativeButton("Tutup",null).show();}
 @Override protected void onStop(){super.onStop();foreground=false;stopPreview();previewId="";if(playing){resumePlayback=true;releasePlayer();if(session!=null)session.setActive(false);}}
 @Override protected void onResume(){super.onResume();foreground=true;if(prefs!=null){refreshEpg(false);refreshCatalog(false);}if(resumePlayback){resumePlayback=false;attempt=0;startMedia();}}
 @Override protected void onDestroy(){catalogClosed=true;catalogIo.shutdownNow();epgClosed=true;epgIo.shutdownNow();handler.removeCallbacksAndMessages(null);stopPreview();releasePlayer();io.shutdownNow();if(session!=null)session.release();if(cloudDashboard!=null)cloudDashboard.close();if(cloudUi!=null)cloudUi.close();super.onDestroy();}
 @Override public void onBackPressed(){if(cloudDashboard!=null&&cloudDashboard.back())return;if(playing){if(timelineFocus){cancelTimeline();redraw();}else if(drawer){drawer=false;redraw();}else stopPlayback();}else if(filterFocus>=0){filterFocus=-1;heroFocus=visible.isEmpty()?-1:0;redraw();}else if(heroFocus>=0){heroFocus=-1;redraw();}else if(!query.isEmpty()){query="";filter();}else if(section!=0){section=0;navFocus=0;nav=false;filter();}else{long n=System.currentTimeMillis();if(n-backAt<2200)finish();else{backAt=n;message("Tekan Back sekali lagi untuk keluar");}}}
 @Override public boolean dispatchKeyEvent(KeyEvent e){if(cloudDashboard!=null&&cloudDashboard.visible())return super.dispatchKeyEvent(e);int k=e.getKeyCode();if(ui!=null&&e.getAction()==KeyEvent.ACTION_DOWN&&(k==19||k==20||k==21||k==22||k==23||k==66||k==160)){ui.touchMode=false;ui.resetTouchScroll();}if(k==24||k==25||k==164)return super.dispatchKeyEvent(e);boolean ok=k==23||k==66||k==160;if(ok){if(e.getAction()==0){if(e.getRepeatCount()==0)longPress=false;if(e.getRepeatCount()==1&&!timelineFocus&&filterFocus<0){toggleFavorite(playing?current():visible.isEmpty()?null:visible.get(selected));longPress=true;}return true;}if(e.getAction()==1){if(longPress)return true;if(playing){if(drawer)openChannel(drawerIndex);else if(timelineFocus)commitTimeline();else pauseToggle();}else if(nav)chooseNav();else if(filterFocus>=0){int chosen=filterFocus;section=chosen;navFocus=chosen==4?4:0;selected=0;filter();filterFocus=chosen;nav=false;redraw();}else if(heroFocus==1){toggleFavorite(visible.isEmpty()?null:visible.get(selected));}else if(heroFocus==2){if(!visible.isEmpty())unavailable(visible.get(selected));}else play(selected);return true;}}if(e.getAction()!=0)return super.dispatchKeyEvent(e);if(k==4){onBackPressed();return true;}if(k==82){if(playing)playbackOptions();else toggleFavorite(visible.isEmpty()?null:visible.get(selected));return true;}if(playing){overlayUntil=System.currentTimeMillis()+4500;if(timelineFocus){if(k==21){moveTimeline(-15000);return true;}if(k==22){moveTimeline(15000);return true;}if(k==19||k==20){cancelTimeline();redraw();return true;}}if(k==89){seekBy(-30000);return true;}if(k==90){seekBy(30000);return true;}if(k==85){pauseToggle();return true;}if(k==126){if(paused||error)pauseToggle();return true;}if(k==127){if(!paused)pauseToggle();return true;}if(k==86){stopPlayback();return true;}if(k==22){drawer=true;drawerIndex=playerIndex;redraw();return true;}if(k==21){if(drawer)drawer=false;else if(canSeek())beginTimeline();redraw();return true;}if(k==19||k==166){if(drawer)drawerIndex=(drawerIndex-1+playQueue.size())%playQueue.size();else next(1);redraw();return true;}if(k==20||k==167){if(drawer)drawerIndex=(drawerIndex+1)%playQueue.size();else next(-1);redraw();return true;}}else{if(k==84){search();return true;}if(nav){if(k==21||k==19)navFocus=HomeLayout.navStep(navFocus,-1);else if(k==22)navFocus=HomeLayout.navStep(navFocus,1);else if(k==20){nav=false;heroFocus=visible.isEmpty()?-1:0;if(visible.isEmpty())filterFocus=section;}else return super.dispatchKeyEvent(e);}else if(filterFocus>=0){if(k==21)filterFocus=HomeLayout.filterStep(filterFocus,-1);else if(k==22)filterFocus=HomeLayout.filterStep(filterFocus,1);else if(k==19){filterFocus=-1;if(visible.isEmpty()){nav=true;navFocus=0;}else heroFocus=0;}else if(k==20){filterFocus=-1;if(visible.isEmpty()){nav=true;navFocus=0;}}else return super.dispatchKeyEvent(e);}else if(heroFocus>=0){if(k==19){heroFocus=-1;nav=true;navFocus=section==4?4:0;}else if(k==20){heroFocus=-1;filterFocus=section;}else if(k==21){heroFocus=Math.max(0,heroFocus-1);}else if(k==22){int last=!visible.isEmpty()&&!visible.get(selected).availabilityNote.isEmpty()?2:1;heroFocus=Math.min(last,heroFocus+1);}else return super.dispatchKeyEvent(e);}else{if(k==21){if(selected%6==0){nav=true;navFocus=section==4?4:0;}else selected--;}else if(k==22){if(selected+1<visible.size())selected++;}else if(k==19){if(selected>=6)selected-=6;else{heroFocus=-1;filterFocus=section;}}else if(k==20){if(selected+6<visible.size())selected+=6;else if(selected/6<(visible.size()-1)/6)selected=visible.size()-1;}else return super.dispatchKeyEvent(e);ensureVisible();}redraw();return true;}return super.dispatchKeyEvent(e);}
 class TvView extends View {
  final Paint p=new Paint(3);Bitmap brandWord;Bitmap brandMark;Bitmap landscape;Canvas c;float scale=1,ox=0,oy=0;
  final int white=0xfff1f3f5,muted=0xff929ba7;
  boolean touchMode=(getResources().getConfiguration().uiMode&android.content.res.Configuration.UI_MODE_TYPE_MASK)!=android.content.res.Configuration.UI_MODE_TYPE_TELEVISION;float gridFraction=0,drawerDrag=0;final OverScroller touchScroller=new OverScroller(MainActivity.this);
  final GestureDetector gestures=new GestureDetector(MainActivity.this,new GestureDetector.SimpleOnGestureListener(){
   @Override public boolean onDown(MotionEvent e){touchScroller.forceFinished(true);drawerDrag=0;return true;}
   @Override public boolean onSingleTapUp(MotionEvent e){performClick();handleTap((e.getX()-ox)/scale,(e.getY()-oy)/scale);return true;}
   @Override public void onLongPress(MotionEvent e){if(playing)return;int i=TouchNavigation.gridIndex((e.getX()-ox)/scale,(e.getY()-oy)/scale,gridPosition(),visible.size());if(i>=0){selected=i;nav=false;toggleFavorite(visible.get(i));redraw();}}
   @Override public boolean onScroll(MotionEvent down,MotionEvent now,float dx,float dy){if(down==null)return false;float x=(down.getX()-ox)/scale,y=(down.getY()-oy)/scale;if(!playing&&TouchNavigation.inGrid(x,y)){scrollGrid(gridPosition()+dy/scale);return true;}if(playing&&drawer&&x<394&&y>=118&&y<667){drawerDrag+=dy/scale;int steps=(int)(drawerDrag/61);if(steps!=0){drawerIndex=Math.max(0,Math.min(playQueue.size()-1,drawerIndex+steps));drawerDrag-=steps*61;redraw();}return true;}return true;}
   @Override public boolean onFling(MotionEvent down,MotionEvent up,float vx,float vy){if(down!=null&&!playing&&TouchNavigation.inGrid((down.getX()-ox)/scale,(down.getY()-oy)/scale)&&Math.abs(vy)>Math.abs(vx)){touchScroller.fling(0,Math.round(gridPosition()),0,Math.round(-vy/scale),0,0,0,Math.round(TouchNavigation.maxScroll(visible.size())));postInvalidateOnAnimation();return true;}return false;}
  });
  float gridPosition(){return rowOffset*134+gridFraction;}
  void resetTouchScroll(){touchScroller.forceFinished(true);gridFraction=0;}
  void scrollGrid(float value){float pos=TouchNavigation.clampScroll(value,visible.size());rowOffset=(int)(pos/134);gridFraction=pos-rowOffset*134;int first=rowOffset*6,last=Math.min(visible.size()-1,(rowOffset+1)*6-1);if(!visible.isEmpty())selected=Math.max(first,Math.min(selected,last));nav=false;invalidate();}
  @Override public void computeScroll(){if(touchScroller.computeScrollOffset()){if(!playing){scrollGrid(touchScroller.getCurrY());postInvalidateOnAnimation();}else touchScroller.forceFinished(true);}}
  @Override public boolean performClick(){super.performClick();return true;}

  TvView(){super(MainActivity.this);try{brandWord=BitmapFactory.decodeStream(getAssets().open("brand-wordmark.png"));}catch(Exception ignored){}try{brandWord=BitmapFactory.decodeStream(getAssets().open("brand-wordmark.png"));}catch(Exception ignored){}try{brandMark=BitmapFactory.decodeStream(getAssets().open("brand-mark.png"));}catch(Exception ignored){}try{landscape=BitmapFactory.decodeStream(getAssets().open("hero-reference.png"));}catch(Exception ignored){}setFocusable(true);setFocusableInTouchMode(true);requestFocus();}
  int bg(){return theme==1?0xff0d1521:theme==2?0xff191817:0xff111419;}
  int panel(){return theme==1?0xff142133:theme==2?0xff252320:0xff1b2027;}
  void text(String s,float x,float y,float size,int color,boolean bold){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(size);p.setTypeface(Typeface.create(bold?"sans-serif-medium":"sans-serif",Typeface.NORMAL));c.drawText(s,x,y,p);}
  void rightText(String s,float right,float y,float size,int color,boolean bold){p.setTextAlign(Paint.Align.RIGHT);text(s,right,y,size,color,bold);p.setTextAlign(Paint.Align.LEFT);}
  void box(float x,float y,float w,float h,float r,int color){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRoundRect(new RectF(x,y,x+w,y+h),r,r,p);}
  void border(float x,float y,float w,float h,float r,int color,float sw){p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(sw);c.drawRoundRect(new RectF(x,y,x+w,y+h),r,r,p);p.setStyle(Paint.Style.FILL);}
  void line(float x,float y,float w,int col){p.setColor(col);p.setStrokeWidth(1);c.drawLine(x,y,x+w,y,p);}
  String fit(String s,float size,float w){p.setTextSize(size);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));if(p.measureText(s)<=w)return s;while(s.length()>1&&p.measureText(s+"…")>w)s=s.substring(0,s.length()-1);return s+"…";}
  String clock(){return new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date());}
  void logo(float x,float y,boolean small){float f=small?.65f:1;c.save();c.translate(x-6,y-9);c.scale(f,f);p.setColor(Color.WHITE);p.setFilterBitmap(true);if(brandMark!=null)c.drawBitmap(brandMark,null,new RectF(0,7,55,48),p);if(brandWord!=null)c.drawBitmap(brandWord,null,new RectF(61,19,192,45),p);c.restore();}
  void stationLogo(Channel ch,float x,float y,float w,float h){box(x,y,w,h,6,ch.logoDark?0xff263241:0xfff3f4f5);if(ch.logo!=null){float f=Math.min((w-22)/ch.logo.getWidth(),(h-14)/ch.logo.getHeight());float bw=ch.logo.getWidth()*f,bh=ch.logo.getHeight()*f;p.setFilterBitmap(true);p.setColor(Color.WHITE);c.drawBitmap(ch.logo,null,new RectF(x+(w-bw)/2,y+(h-bh)/2,x+(w+bw)/2,y+(h+bh)/2),p);}else{String s=ch.name;float size=s.length()>20?15:s.length()>12?18:24;text(fit(s,size,w-20),x+10,y+h/2+size/3,size,0xff293442,true);}}
  void navIcon(int i,float x,float y,int color){p.setColor(color);p.setStrokeWidth(1.6f);p.setStyle(Paint.Style.STROKE);if(i==0){for(int r=0;r<2;r++)for(int col=0;col<2;col++)c.drawRoundRect(new RectF(x+col*10,y+r*10,x+col*10+6,y+r*10+6),1,1,p);}else if(i==1){c.drawRoundRect(new RectF(x,y+2,x+19,y+15),2,2,p);c.drawLine(x+5,y+20,x+14,y+20,p);c.drawLine(x+9,y+15,x+9,y+20,p);}else if(i==2){c.drawCircle(x+9,y+9,10,p);c.drawOval(new RectF(x+4,y-1,x+14,y+19),p);c.drawLine(x-1,y+9,x+19,y+9,p);}else if(i==3){c.drawCircle(x+9,y+9,10,p);c.drawCircle(x+9,y+9,3,p);for(int k=0;k<5;k++){double a=k*Math.PI*2/5;c.drawLine(x+9+(float)Math.cos(a)*3,y+9+(float)Math.sin(a)*3,x+9+(float)Math.cos(a)*10,y+9+(float)Math.sin(a)*10,p);}}else if(i==4){Path path=new Path();for(int k=0;k<10;k++){double a=-Math.PI/2+k*Math.PI/5;float r=k%2==0?10:4.5f;float xx=x+9+(float)Math.cos(a)*r,yy=y+9+(float)Math.sin(a)*r;if(k==0)path.moveTo(xx,yy);else path.lineTo(xx,yy);}path.close();c.drawPath(path,p);}else if(i==5){c.drawCircle(x+6,y+6,7,p);c.drawLine(x+11,y+11,x+19,y+19,p);}else{c.drawCircle(x+9,y+9,9,p);c.drawCircle(x+9,y+9,3,p);for(int k=0;k<8;k++){double a=k*Math.PI/4;c.drawLine(x+9+(float)Math.cos(a)*9,y+9+(float)Math.sin(a)*9,x+9+(float)Math.cos(a)*12,y+9+(float)Math.sin(a)*12,p);}}p.setStyle(Paint.Style.FILL);}
  @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);c=canvas;scale=DisplayViewport.scale(getWidth(),getHeight());ox=DisplayViewport.left(getWidth(),getHeight());oy=DisplayViewport.top(getWidth(),getHeight());if(!playing)c.drawColor(bg());c.save();c.translate(ox,oy);c.scale(scale,scale);if(playing)drawPlayer();else drawHome();if(System.currentTimeMillis()<toastUntil){p.setTextSize(16);float w=p.measureText(toast)+48;box((1280-w)/2,623,w,42,8,0xf0333b46);text(toast,(1280-w)/2+24,650,16,white,false);}c.restore();}
  void drawAppHeader(Canvas target,int activeTarget,int focusTarget){
   Canvas previous=c;c=target;
   try{
    logo(42,22,false);
    String[] labels={"TV","Film","Favorit","Cari","Setelan"};float[] nx={300,414,528,642,756};
    for(int i=0;i<labels.length;i++){
     int item=HomeLayout.NAV[i];boolean active=item==activeTarget,focus=item==focusTarget;
     if(focus)box(nx[i]-12,18,96,44,6,0xff30475b);
     text(labels[i],nx[i],46,18,active||focus?0xffa9dfff:white,active||focus);
     if(active)box(nx[i],65,Math.min(76,labels[i].length()*11),3,1,0xffa9dfff);
     if(focus)border(nx[i]-12,18,96,44,6,white,2);
    }
    Date now=new Date();rightText(clock(),1236,35,23,white,true);
    rightText(new SimpleDateFormat("EEEE, d MMMM yyyy",new Locale("id","ID")).format(now),1236,58,12,0xffbdcedd,false);
    line(44,80,1192,0xff33404e);
   }finally{c=previous;}
  }
  void drawHome(){
   p.setColor(bg());c.drawRect(0,0,1280,720,p);
   drawAppHeader(c,section==4?4:0,nav?navFocus:-1);
   box(44,96,548,308,6,0xff18222d);
   if(!previewFrame){
    if(!visible.isEmpty())stationLogo(visible.get(selected),214,161,208,104);
    text(fit(previewStatus,15,500),68,371,15,0xffbdcedd,false);
   }
   text("Preview tanpa suara",44,425,12,0xffbdcedd,false);
   if(!visible.isEmpty()){
    Channel ch=visible.get(selected);EpgStore.Programme airing=epg.at(ch.id,System.currentTimeMillis()).now;
    stationLogo(ch,624,103,70,45);text(fit(ch.name,28,518),712,136,28,white,true);
    text(airing==null?"Siaran langsung":"Sedang tayang",624,178,14,0xffa9dfff,false);
    text(fit(airing==null?(ch.liveLabel.isEmpty()?"Jadwal belum tersedia":ch.liveLabel):airing.title,26,612),624,215,26,white,true);
    if(airing!=null){
     text(epgTime(airing.start)+"–"+epgTime(airing.end),624,245,16,0xffbdcedd,false);
     float elapsed=Math.max(0,Math.min(1,(System.currentTimeMillis()-airing.start)/(float)Math.max(1,airing.end-airing.start)));
     box(624,263,612,4,2,0xff344352);box(624,263,612*elapsed,4,2,0xffa9dfff);
    }
    text(fit(nextLine(ch),15,612),624,298,15,0xffbdcedd,false);
    box(624,326,228,48,6,0xffa9dfff);
    text(ch.servers.isEmpty()?"Info ketersediaan":"▶  Tonton sekarang",643,357,17,0xff081725,true);
    box(868,326,52,48,6,0xff243441);border(868,326,52,48,6,0xff9eb5c8,1);
    text(favorites.contains(ch.id)?"★":"☆",882,359,28,0xffa9dfff,false);
    if(!ch.availabilityNote.isEmpty()){box(936,326,128,48,6,0xff243441);text("Info siaran",955,357,16,white,false);}
    if(!touchMode&&!nav&&heroFocus>=0){
     float hx=heroFocus==0?624:heroFocus==1?868:936,hw=heroFocus==0?228:heroFocus==1?52:128;
     border(hx-4,322,hw+8,56,8,white,3);
    }
   }
   text(query.isEmpty()?"Jelajahi channel":"Hasil pencarian",44,455,25,white,true);
   rightText(visible.size()+" channel",1236,455,14,0xffbdcedd,false);
   String[] filters={"Semua","Nasional","Internasional","Olahraga","Favorit"};
   float[] fx={44,141,254,394,511};float[] fw={85,101,128,105,91};
   for(int i=0;i<5;i++){
    boolean active=i==section;text(filters[i],fx[i]+8,486,14,active?0xffa9dfff:0xffbdcedd,active);
    if(active)box(fx[i]+8,495,fw[i]-16,3,1,0xffa9dfff);
    if(!touchMode&&!nav&&filterFocus==i)border(fx[i],461,fw[i],41,5,white,2);
   }
   if(visible.isEmpty()){text(section==4?"Belum ada channel favorit":"Tidak ada hasil",44,557,24,white,true);text("Gunakan Cari atau pilih kategori lain.",44,591,16,0xffbdcedd,false);}
   c.save();c.clipRect(38,507,1241,644);
   for(int row=0;row<2;row++)for(int col=0;col<6;col++){
    int i=(row+rowOffset)*6+col;if(i>=visible.size())continue;
    Channel ch=visible.get(i);float x=44+col*201,y=HomeLayout.GRID_Y+row*134-gridFraction;
    boolean focus=!nav&&heroFocus<0&&filterFocus<0&&i==selected;
    box(x,y,191,126,6,focus?0xff30475b:0xff202a35);stationLogo(ch,x+18,y+8,155,62);
    if(focus)border(x-3,y-3,197,132,8,white,3);
    text(fit(ch.name,15,favorites.contains(ch.id)?143:169),x+11,y+93,15,white,true);
    text(fit(cardProgramme(ch),12,169),x+11,y+115,12,0xffbdcedd,false);
    if(favorites.contains(ch.id))text("★",x+166,y+93,15,0xffa9dfff,true);
   }
   c.restore();
   if(visible.size()>6){
    int total=(visible.size()+5)/6;box(1255,511,3,126,1,0xff33404e);
    box(1255,511+(rowOffset+gridFraction/134)*(126f/total),3,Math.max(8,126f/total),1,0xffa9dfff);
   }
   line(44,666,1192,0xff33404e);
   text(touchMode?"Geser ↑ ↓ untuk scroll":"↑ ↓ ← →  Navigasi",44,691,14,0xffbdcedd,false);
   text(touchMode?"Ketuk channel  Tonton":"OK  Tonton",270,691,14,white,false);
   text(touchMode?"Tahan channel  Favorit":"Tahan OK  Favorit",touchMode?485:421,691,14,white,false);
   text(touchMode?"":"BACK  Kembali",646,691,14,white,false);
   rightText("ELIO TV · UJI 8",1236,691,12,0xffbdcedd,false);
  }

  void drawTimeline(){
   if(!canSeek()){text("LANGSUNG · Putar ulang belum tersedia dari sumber",92,517,13,0xffbdcedd,false);return;}
   long duration=player.getDuration(),position=timelineFocus?timelineDraft:player.getCurrentPosition();float progress=Math.max(0,Math.min(1,position/(float)duration));
   box(78,445,1124,92,6,0xee111b25);text(timelineFocus?"Pilih posisi · OK untuk memutar":"Posisi tayangan",94,466,12,0xffbdcedd,false);
   text(SeekWindow.format(position),94,489,15,white,true);rightText(SeekWindow.format(duration),1108,489,15,0xffbdcedd,false);
   box(94,505,1014,5,2,0xff496172);box(94,505,1014*progress,5,2,0xffa9dfff);p.setColor(timelineFocus?white:0xffa9dfff);c.drawCircle(94+1014*progress,507.5f,timelineFocus?9:7,p);
   box(1124,479,66,34,5,0xffa9dfff);text(player.isCurrentMediaItemLive()?"LIVE":"AWAL",1136,501,12,0xff081725,true);
   text(touchMode?"Tarik titik untuk memilih menit/detik":"←  Pilih timeline · ← → Geser 15 detik · OK Putar",94,530,10,0xffbdcedd,false);
  }

  void playbackBar(){
   box(28,650,1224,58,6,0xf5111b25);
   float[] x={36,238,368,518,810,1050};float[] w={186,114,134,276,224,182};
   String[] labels=touchMode?new String[]{"‹ Sebelumnya",error?"Coba lagi":paused?"▶ Lanjut":"Ⅱ Jeda","Daftar","Pilihan","Berikutnya ›","Beranda"}:new String[]{"↑↓ Ganti channel","OK "+(paused?"Lanjut":"Jeda"),"→ Daftar",timelineFocus?"← → Geser · OK Putar":"← Timeline · MENU Pilihan","","BACK Beranda"};
   for(int i=0;i<labels.length;i++){
    if(labels[i].isEmpty())continue;
    if(touchMode)box(x[i],658,w[i],42,5,0xff2a3b4b);
    text(labels[i],x[i]+12,685,touchMode?15:14,white,touchMode);
   }
  }
  void drawPlayer(){Channel ch=current();if(ch==null)return;boolean show=timelineFocus||drawer||paused||buffering||error||System.currentTimeMillis()<overlayUntil;
   if(buffering||error){p.setColor(0x80000000);c.drawRect(0,0,1280,720,p);if(error){box(382,265,516,155,10,0xef1b2027);text("Siaran belum tersedia",414,308,24,white,true);text(fit(status,15,450),414,339,15,muted,false);text(touchMode?"Ketuk untuk coba lagi · Pilihan: ganti server":"OK  Coba lagi     MENU  Pilih server",414,382,15,white,false);}else{p.setTextAlign(Paint.Align.CENTER);text(status,640,373,18,white,false);text("Server "+(serverIndex+1)+" dari "+ch.servers.size(),640,400,13,muted,false);p.setTextAlign(Paint.Align.LEFT);}}
   if(show){p.setShader(new LinearGradient(0,395,0,720,new int[]{0,0xcc090c11,0xf5090c11},new float[]{0,.45f,1},Shader.TileMode.CLAMP));c.drawRect(0,395,1280,720,p);p.setShader(null);box(32,30,142,47,8,0xb510141b);logo(48,43,true);box(190,30,95,47,8,0xb510141b);text(clock(),205,61,18,white,true);stationLogo(ch,44,568,86,52);text(ch.name,150,594,29,white,true);String detail="Server "+(serverIndex+1)+"/"+ch.servers.size();if(videoHeight>0)detail+="   ·   "+videoHeight+"p";if(paused)detail+="   ·   Dijeda";text(detail,151,562,12,0xffc4d2df,false);text(fit(nowLine(ch),16,1010),151,619,16,white,false);text(fit(nextLine(ch),13,1010),151,640,13,0xffbdcedd,false);if(!drawer)drawTimeline();if(favorites.contains(ch.id))text("★",1196,596,21,0xffd5bd8d,true);line(44,649,1192,0xff363c45);if(touchMode){text("‹  Sebelumnya",45,684,16,white,true);text(error?"Coba lagi":paused?"▶  Lanjut":"Ⅱ  Jeda",247,684,16,white,true);text("☷  Daftar",380,684,16,white,true);text("⋮  Pilihan",527,684,16,white,true);text("Berikutnya ›",835,684,16,white,true);text("⌂  Beranda",1108,684,16,white,true);if(!drawer&&!buffering&&!error){box(601,321,78,78,39,0xb510141b);p.setTextAlign(Paint.Align.CENTER);text(paused?"▶":"Ⅱ",640,372,32,white,true);p.setTextAlign(Paint.Align.LEFT);}}else{text("↑↓  Ganti channel",45,684,14,white,false);text("OK  "+(paused?"Lanjut":"Jeda"),247,684,14,white,false);text("→  Daftar",380,684,14,white,false);text(timelineFocus?"← →  Geser · OK  Putar":"← Timeline · MENU Pilihan",527,684,14,white,false);text("BACK  Beranda",1108,684,14,white,false);}}
   if(show)playbackBar();
   if(drawer){
    box(24,24,370,672,8,0xff111b25);border(24,24,370,672,8,0xff43586a,1);
    text("Daftar channel",48,67,24,white,true);if(touchMode)text("×",350,67,28,white,true);
    text(touchMode?"Geser · Ketuk untuk tonton":"↑↓ Pilih   OK Tonton   ← Tutup",48,96,13,0xffbdcedd,false);
    int first=TouchNavigation.drawerFirst(drawerIndex,playQueue.size());
    for(int n=0;n<9&&first+n<playQueue.size();n++){
     int i=first+n;float y=118+n*61;
     if(i==drawerIndex){box(35,y,348,55,5,0xff30475b);border(35,y,348,55,5,white,2);}
     Channel cc=playQueue.get(i);stationLogo(cc,46,y+10,52,34);
     text(fit(cc.name,16,245),111,y+23,16,white,i==drawerIndex);
     text(fit(cardProgramme(cc),12,245),111,y+43,12,0xffbdcedd,false);
     if(i==playerIndex)box(375,y+15,3,23,1,0xffa9dfff);
    }
   }
  }
  @Override public boolean onTouchEvent(MotionEvent e){heroFocus=-1;filterFocus=-1;touchMode=true;
   float tx=(e.getX()-ox)/scale,ty=(e.getY()-oy)/scale;boolean controls=timelineFocus||paused||buffering||error||System.currentTimeMillis()<overlayUntil;
   if(playing&&!drawer&&e.getPointerCount()==1){
    if(e.getActionMasked()==MotionEvent.ACTION_DOWN&&controls&&canSeek()&&tx>=78&&tx<=1118&&ty>=477&&ty<=536){beginTimeline();timelineDragging=true;timelineDraft=SeekWindow.position(tx,94,1108,player.getDuration());redraw();return true;}
    if(timelineDragging){if(e.getActionMasked()==MotionEvent.ACTION_MOVE){if(canSeek())timelineDraft=SeekWindow.position(tx,94,1108,player.getDuration());overlayUntil=System.currentTimeMillis()+10000;redraw();}else if(e.getActionMasked()==MotionEvent.ACTION_UP){if(canSeek())timelineDraft=SeekWindow.position(tx,94,1108,player.getDuration());commitTimeline();}else if(e.getActionMasked()==MotionEvent.ACTION_CANCEL){cancelTimeline();redraw();}return true;}
   }
   if(e.getPointerCount()>1&&timelineDragging){cancelTimeline();redraw();return true;}
if(e.getActionMasked()==MotionEvent.ACTION_DOWN)invalidate();if(e.getPointerCount()>1){MotionEvent cancel=MotionEvent.obtain(e);cancel.setAction(MotionEvent.ACTION_CANCEL);gestures.onTouchEvent(cancel);cancel.recycle();touchScroller.forceFinished(true);return true;}gestures.onTouchEvent(e);if(e.getActionMasked()==MotionEvent.ACTION_CANCEL)touchScroller.forceFinished(true);return true;}
  void handleTap(float x,float y){if(x<0||x>=1280||y<0||y>=720)return;
   if(playing){if(drawer){if(x>=335&&x<394&&y<110){drawer=false;redraw();return;}int i=TouchNavigation.drawerIndex(x,y,drawerIndex,playQueue.size());if(i>=0)openChannel(i);else if(x>=394){drawer=false;redraw();}return;}
    boolean controls=timelineFocus||paused||buffering||error||System.currentTimeMillis()<overlayUntil;overlayUntil=System.currentTimeMillis()+6000;
    if(!controls){redraw();return;}
    if(canSeek()&&x>=1124&&x<=1190&&y>=479&&y<=513){cancelTimeline();player.seekToDefaultPosition();redraw();return;}
    if(y>=649){if(x<230)next(-1);else if(x<360)pauseToggle();else if(x<510){drawer=true;drawerIndex=playerIndex;}else if(x<800)playbackOptions();else if(x<1040)next(1);else if(x>=1040)stopPlayback();redraw();return;}
    if(error&&x>=382&&x<=898&&y>=265&&y<=420){pauseToggle();return;}
    if(!buffering&&!error&&x>=590&&x<=690&&y>=310&&y<=410){pauseToggle();return;}
    redraw();return;
   }
   if(y<80&&x>=288&&x<850){float[] nx={288,402,516,630,744,850};for(int i=0;i<5;i++)if(x>=nx[i]&&x<nx[i+1]){resetTouchScroll();navFocus=HomeLayout.NAV[i];chooseNav();break;}return;}
   if(y>=462&&y<503){float[] nx={44,141,254,394,511};float[] widths={85,101,128,105,91};for(int i=0;i<5;i++)if(x>=nx[i]&&x<nx[i]+widths[i]){resetTouchScroll();navFocus=i;chooseNav();break;}return;}
   int hero=HomeLayout.heroHit(x,y);
   if(hero==0){resetTouchScroll();play(selected);return;}
   if(hero==1){toggleFavorite(visible.isEmpty()?null:visible.get(selected));return;}
   if(hero==2&&!visible.isEmpty()&&!visible.get(selected).availabilityNote.isEmpty()){unavailable(visible.get(selected));return;}
   int i=TouchNavigation.gridIndex(x,y,gridPosition(),visible.size());if(i>=0){selected=i;nav=false;play(i);}
  }
 }
}


