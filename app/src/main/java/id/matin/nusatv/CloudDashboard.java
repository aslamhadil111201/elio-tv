package id.matin.nusatv;

import android.app.*;
import android.os.Build;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;
import android.text.TextUtils;
import android.util.LruCache;
import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;
import org.json.*;
import com.lagradost.cloudstream3.*;

/** Native, scrollable movie browser; D-pad focus and touch share the same controls. */
final class CloudDashboard {
 final MainActivity a;final CloudUi cloud;
 final ExecutorService work=Executors.newSingleThreadExecutor(),images=Executors.newFixedThreadPool(2);
 final LruCache<String,Bitmap> cache=new LruCache<String,Bitmap>(8*1024*1024){protected int sizeOf(String k,Bitmap b){return b.getAllocationByteCount();}};
 LinearLayout page,body,toolbar,filters;ScrollView scroll;GridLayout grid;TextView status,heading,subtitle,footer;Button searchButton,providerButton,categoryButton;HorizontalScrollView categoryStrip;boolean compact;
 List<MainAPI> providers=new ArrayList<>();List<SearchResponse> rows=new ArrayList<>();MainAPI provider;
 boolean combined=true;Map<SearchResponse,MainAPI> origins=new IdentityHashMap<>();int headerFocus=-1;
 static final class Feed {final List<SearchResponse> rows=new ArrayList<>();final Map<SearchResponse,MainAPI> origins=new IdentityHashMap<>();int loaded,failed;boolean partial;}
 volatile long epoch=0,imageEpoch=0;Future<?> job;boolean detail=false,returnAfterPlayback=false;int category=0,collection=0;String query="",selectedUrl="";int savedScroll=0;
 final int bg=0xff111419,panel=0xff202a35,ink=0xfff1f3f5,muted=0xffbdcedd,accent=0xffa9dfff;
 interface Job<T>{T run()throws Exception;}interface Result<T>{void accept(T value)throws Exception;}
 CloudDashboard(MainActivity host,CloudUi ui){a=host;cloud=ui;compact=false;}
 // Legacy helper name; values are now design pixels, transformed once by the page.
 int dp(float v){return Math.round(v);}
 void layoutViewport(){if(page==null)return;int w=a.root.getWidth(),h=a.root.getHeight();if(w<=0||h<=0)return;float scale=DisplayViewport.scale(w,h);page.setPivotX(0);page.setPivotY(0);page.setScaleX(scale);page.setScaleY(scale);page.setTranslationX(DisplayViewport.left(w,h));page.setTranslationY(DisplayViewport.top(w,h));}
 boolean visible(){return page!=null&&page.getVisibility()==View.VISIBLE;}
 TextView text(String s,int size,int color){TextView t=new TextView(a);t.setText(s);t.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,size);t.setTextColor(color);t.setFontFeatureSettings("kern");return t;}
 GradientDrawable shape(int fill,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(5));if(stroke!=0)d.setStroke(dp(2),stroke);return d;}
 android.graphics.drawable.StateListDrawable focusBackground(){android.graphics.drawable.StateListDrawable d=new android.graphics.drawable.StateListDrawable();d.addState(new int[]{android.R.attr.state_focused},shape(0xff30475b,ink));d.addState(new int[]{android.R.attr.state_pressed},shape(0xff30475b,ink));d.addState(new int[]{},shape(panel,0));return d;}
 Button button(String label,Runnable action){Button b=new Button(a);b.setId(View.generateViewId());b.setText(label);b.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX,18);b.setAllCaps(false);b.setTextColor(ink);b.setMinWidth(0);b.setMinimumWidth(0);b.setMinHeight(0);b.setMinimumHeight(0);b.setPadding(dp(14),0,dp(14),0);b.setBackground(focusBackground());b.setOnClickListener(v->action.run());return b;}
 void addButton(LinearLayout bar,Button b){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(44));lp.setMargins(0,0,dp(8),0);bar.addView(b,lp);}
 void open(){if(Build.VERSION.SDK_INT<26){cloud.show();return;}if(a.playing)a.stopPlayback();a.stopPreview();a.previewId="";if(page==null)build();page.setVisibility(View.VISIBLE);a.root.setBackgroundColor(bg);layoutViewport();a.ui.setVisibility(View.GONE);a.previewSurface.setVisibility(View.GONE);returnAfterPlayback=false;searchButton.requestFocus();if(provider==null)loadProviders();}
 void build(){page=new LinearLayout(a);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(44),dp(16),dp(44),dp(12));page.setBackgroundColor(bg);page.setClipChildren(true);page.setClipToPadding(false);a.root.addView(page,new FrameLayout.LayoutParams(DisplayViewport.WIDTH,DisplayViewport.HEIGHT));layoutViewport();
  FrameLayout mast=new FrameLayout(a);
  View artwork=new View(a){@Override protected void onDraw(Canvas canvas){super.onDraw(canvas);a.ui.drawAppHeader(canvas,6,headerFocus);if(isShown())postInvalidateDelayed(1000);}};
  mast.addView(artwork,new FrameLayout.LayoutParams(1280,81));
  String[] navLabels={"TV","Film","Favorit","Cari","Setelan"};
  for(int i=0;i<navLabels.length;i++){
   final int target=HomeLayout.NAV[i];Button hit=button("",()->{
    if(target==0)closePage();else if(target==6){category=0;tabs();loadHome();}
    else if(target==4){closePage();a.section=4;a.navFocus=4;a.nav=false;a.filter();}
    else if(target==5)search();else advanced();
   });
   hit.setBackgroundColor(Color.TRANSPARENT);hit.setContentDescription(navLabels[i]);
   hit.setOnFocusChangeListener((v,focused)->{headerFocus=focused?target:-1;artwork.invalidate();});
   FrameLayout.LayoutParams hp=new FrameLayout.LayoutParams(96,44);hp.leftMargin=288+i*114;hp.topMargin=18;mast.addView(hit,hp);
  }
  LinearLayout.LayoutParams mastParams=new LinearLayout.LayoutParams(1280,81);mastParams.leftMargin=-44;mastParams.rightMargin=-44;mastParams.topMargin=-16;mastParams.bottomMargin=15;page.addView(mast,mastParams);
  toolbar=new LinearLayout(a);toolbar.setGravity(Gravity.CENTER_VERTICAL);heading=text("Mau nonton apa malam ini?",24,ink);heading.setTypeface(Typeface.DEFAULT,Typeface.BOLD);toolbar.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
  providerButton=button("Provider",this::chooseProvider);searchButton=button("Cari film atau serial",this::search);addButton(toolbar,searchButton);categoryButton=button("Kategori",this::chooseCategory);addButton(toolbar,categoryButton);page.addView(toolbar);
  subtitle=text("Katalog film dari provider terpasang.",13,muted);subtitle.setPadding(0,dp(4),0,dp(8));page.addView(subtitle);
  categoryStrip=new HorizontalScrollView(a);categoryStrip.setHorizontalScrollBarEnabled(false);filters=new LinearLayout(a);categoryStrip.addView(filters);page.addView(categoryStrip);tabs();
  status=text("",14,muted);status.setPadding(0,dp(10),0,dp(8));status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);page.addView(status);
  scroll=new ScrollView(a);scroll.setFillViewport(false);scroll.setClipToPadding(false);scroll.setPadding(0,dp(6),0,dp(6));scroll.setSmoothScrollingEnabled(true);body=new LinearLayout(a);body.setOrientation(LinearLayout.VERTICAL);scroll.addView(body,new ScrollView.LayoutParams(-1,-2));page.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  footer=text("Arah Navigasi     OK Detail film     Back Kembali ke TV",14,muted);footer.setPadding(0,dp(6),0,0);page.addView(footer);chrome();
 }
 void chrome(){heading.setVisibility(compact?View.GONE:View.VISIBLE);subtitle.setVisibility(compact?View.GONE:View.VISIBLE);footer.setVisibility(compact?View.GONE:View.VISIBLE);categoryStrip.setVisibility(compact?View.GONE:View.VISIBLE);categoryButton.setVisibility(compact?View.VISIBLE:View.GONE);toolbar.setPadding(0,0,0,0);page.setPadding(dp(44),dp(compact?10:16),dp(44),dp(compact?8:12));status.setPadding(0,dp(4),0,dp(4));status.setMaxLines(compact?2:3);status.setEllipsize(TextUtils.TruncateAt.END);}
 void advanced(){new AlertDialog.Builder(a).setTitle("Setelan film").setItems(new String[]{"Semua provider terpasang","Muat ulang katalog","Pilih satu provider","Koleksi provider","Kelola ekstensi / repo","Demo offline"},(d,w)->{if(w==0){combined=true;category=0;tabs();if(provider==null)loadProviders();else loadHome();}else if(w==1){if(provider==null)loadProviders();else if(query.isEmpty())loadHome();else searchFor(query);}else if(w==2)chooseProvider();else if(w==3)collections();else if(w==4)cloud.show();else demo();}).setNegativeButton("Tutup",null).show();}
 void chooseCategory(){String[] labels={"Semua","Film","Serial","Anime"};new AlertDialog.Builder(a).setTitle("Kategori").setSingleChoiceItems(labels,category,(d,w)->{category=w;categoryButton.setText(labels[w]);tabs();if(detail)detail=false;render();d.dismiss();}).setNegativeButton("Tutup",null).show();}
 void tabs(){
  filters.removeAllViews();String[] labels={"Semua","Film","Serial","Anime"};
  for(int i=0;i<labels.length;i++){
   final int selected=i;
   Button tab=new Button(a){
    final Paint paint=new Paint(3);
    @Override protected void onDraw(Canvas canvas){
     int color=selected==category?accent:muted;
     paint.setColor(color);paint.setTextSize(14);paint.setTypeface(Typeface.create(selected==category?"sans-serif-medium":"sans-serif",Typeface.NORMAL));
     canvas.drawText(labels[selected],8,27,paint);
     if(selected==category){paint.setStrokeWidth(3);canvas.drawLine(8,38,getWidth()-12,38,paint);}
     if(isFocused()){paint.setColor(ink);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2);canvas.drawRoundRect(new RectF(2,2,getWidth()-2,getHeight()-2),4,4,paint);paint.setStyle(Paint.Style.FILL);}
    }
   };
   tab.setId(View.generateViewId());tab.setBackgroundColor(Color.TRANSPARENT);tab.setMinWidth(0);tab.setMinimumWidth(0);tab.setMinHeight(0);tab.setMinimumHeight(0);tab.setContentDescription(labels[i]);tab.setSelected(i==category);
   tab.setOnFocusChangeListener((v,focused)->v.invalidate());tab.setOnClickListener(v->{category=selected;detail=false;tabs();filters.getChildAt(selected).requestFocus();render();});
   LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(92,44);lp.rightMargin=8;filters.addView(tab,lp);
  }
 }
 <T>void task(String message,Job<T> job,Result<T> done){long token=++epoch;if(this.job!=null)this.job.cancel(true);status.setText(message);this.job=work.submit(()->{try{T result=job.run();a.handler.post(()->{if(token!=epoch||!visible()||a.isDestroyed())return;try{done.accept(result);}catch(Exception e){status.setText("Tidak dapat membuka katalog. Coba lagi atau pilih provider lain.");}});}catch(Throwable e){a.handler.post(()->{if(token==epoch&&visible()&&!a.isDestroyed())status.setText("Provider belum merespons atau belum kompatibel. Coba muat ulang, pencarian, atau provider lain.");});}});}
 void loadProviders(){task("Memuat provider terpasang…",()->{if(cloud.runtime==null)cloud.runtime=new CloudRuntime(a);JSONArray installed=cloud.installed();for(int i=0;i<installed.length();i++)try{cloud.runtime.load(installed.getJSONObject(i));}catch(Exception ignored){}return cloud.runtime.providers();},list->{providers=list;if(list.isEmpty()){rows.clear();render();heading.setText("Mulai koleksi Anda");subtitle.setText("Buka Setelan → Kelola ekstensi / repo untuk memasang provider, atau coba Demo offline.");status.setText("Belum ada provider yang berhasil dimuat.");}else{combined=true;provider=list.get(0);loadHome();}});}
 void chooseProvider(){if(providers.isEmpty()){loadProviders();return;}String[] names=new String[providers.size()+1];for(int i=0;i<providers.size();i++)names[i]=providers.get(i).getName();names[names.length-1]="Periksa ekstensi terpasang";new AlertDialog.Builder(a).setTitle("Provider katalog").setItems(names,(d,w)->{if(w==providers.size())loadProviders();else selectProvider(providers.get(w));}).setNegativeButton("Tutup",null).show();}
 void selectProvider(MainAPI p){combined=false;origins.clear();provider=p;collection=0;category=0;query="";detail=false;a.prefs.edit().putString("film-dashboard-provider",CloudResolver.key(p)).apply();providerButton.setText(p.getName());tabs();rows.clear();render();loadHome();}
 void loadHome(){if(provider==null)return;if(combined){loadCombined("");return;}final MainAPI p=provider;final int index=collection;query="";detail=false;heading.setText("Mau nonton apa malam ini?");subtitle.setText(p.getName());task("Memuat koleksi…",()->cloud.runtime.shelves(p,index),shelves->{ArrayList<SearchResponse> all=new ArrayList<>();ArrayList<String> labels=new ArrayList<>();for(HomePageList shelf:shelves){if(shelf==null)continue;all.addAll(shelf.getList());labels.add(shelf.getName());}rows=CloudCatalog.select(all,0);subtitle.setText(p.getName()+(labels.isEmpty()?"":" · "+TextUtils.join(" / ",labels)));render();if(rows.isEmpty())status.setText("Koleksi belum tersedia. Gunakan Cari film / serial atau pilih koleksi lain.");});}
 void collections(){if(provider==null){chooseProvider();return;}List<MainPageData> sections=provider.getMainPage();if(sections==null||sections.isEmpty()){status.setText("Provider tidak menyediakan koleksi. Gunakan pencarian judul.");return;}int n=Math.min(50,sections.size());String[] names=new String[n];for(int i=0;i<n;i++)names[i]=sections.get(i).getName();new AlertDialog.Builder(a).setTitle("Koleksi · "+provider.getName()).setItems(names,(d,w)->{combined=false;origins.clear();collection=w;category=0;tabs();loadHome();}).setNegativeButton("Tutup",null).show();}
 void loadCombined(String term){
  query=term;detail=false;heading.setText(term.isEmpty()?"Mau nonton apa malam ini?":"Hasil pencarian");
  subtitle.setText(term.isEmpty()?"Katalog gabungan · provider terpasang":"“"+term+"” · semua provider terpasang");
  final List<MainAPI> selected=new ArrayList<>(providers);
  task(term.isEmpty()?"Memuat katalog gabungan…":"Mencari di provider terpasang…",()->{
   Feed feed=new Feed();long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(40);
   for(MainAPI source:selected){
    if(Thread.currentThread().isInterrupted())throw new CancellationException();
    if(System.nanoTime()>deadline){feed.partial=true;break;}
    if(source.getName().startsWith("ELIO Demo"))continue;
    try{
     List<SearchResponse> found=new ArrayList<>();
     if(term.isEmpty()){for(HomePageList shelf:cloud.runtime.shelves(source,0))if(shelf!=null)found.addAll(shelf.getList());}
     else found=cloud.runtime.search(source,term);
     for(SearchResponse row:CloudCatalog.select(found,0)){feed.rows.add(row);feed.origins.put(row,source);}
     feed.loaded++;
    }catch(Exception e){if(Thread.currentThread().isInterrupted())throw new CancellationException();feed.failed++;}
   }
   return feed;
  },feed->{
   origins=feed.origins;rows=CloudCatalog.grouped(feed.rows);render();
   subtitle.setText("Katalog gabungan · "+feed.loaded+" provider dimuat"+(feed.failed>0?" · "+feed.failed+" belum merespons":"")+(feed.partial?" · hasil sebagian":""));
   if(rows.isEmpty())status.setText("Belum ada judul yang berhasil dimuat. Coba pencarian atau Muat ulang di Setelan.");
  });
 }
 void search(){if(provider==null){chooseProvider();return;}EditText field=new EditText(a);field.setSingleLine(true);field.setText(query);field.setHint("Judul atau kata kunci");new AlertDialog.Builder(a).setTitle(combined?"Cari film atau serial":"Cari di "+provider.getName()).setView(field).setPositiveButton("Cari",(d,w)->{String q=field.getText().toString().trim();if(!q.isEmpty())searchFor(q);}).setNegativeButton("Batal",null).show();}
 void searchFor(String q){if(combined){loadCombined(q);return;}query=q;detail=false;final MainAPI p=provider;heading.setText("Hasil pencarian");subtitle.setText("“"+q+"” · "+p.getName());task("Mencari judul…",()->cloud.runtime.search(p,q),found->{rows=CloudCatalog.select(found,0);render();});}
 void demo(){task("Memuat demo offline…",()->{if(cloud.runtime==null)cloud.runtime=new CloudRuntime(a);cloud.runtime.demo();return cloud.runtime.providers();},all->{providers=all;for(MainAPI p:all)if(p.getName().equals("ELIO Demo · Big Buck Bunny")){selectProvider(p);return;}status.setText("Demo belum berhasil dimuat.");});}
 void render(){if(body==null)return;detail=false;imageEpoch++;body.removeAllViews();List<SearchResponse> shown=CloudCatalog.select(rows,category);status.setText(shown.isEmpty()?"Belum ada judul dalam kategori ini.":shown.size()+" judul · "+new String[]{"Semua kategori","Film","Serial","Anime"}[category]);
  grid=new GridLayout(a);int width=DisplayViewport.WIDTH-88;int cols=CloudCatalog.columns(width);grid.setColumnCount(cols);int cardWidth=width/cols;ArrayList<View> cards=new ArrayList<>();final long imagesToken=imageEpoch;
  for(SearchResponse r:shown){LinearLayout card=new LinearLayout(a);card.setOrientation(LinearLayout.VERTICAL);card.setId(View.generateViewId());card.setFocusable(true);card.setClickable(true);card.setBackground(focusBackground());card.setPadding(dp(5),dp(5),dp(5),dp(8));card.setTag(r.getUrl());card.setContentDescription(r.getName()+", "+CloudCatalog.label(r)+", buka detail");card.setClipToOutline(true);
   ImageView poster=new ImageView(a);poster.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);poster.setScaleType(ImageView.ScaleType.FIT_CENTER);poster.setBackground(shape(0xff21384b,0));poster.setImageDrawable(placeholder());int imageHeight=Math.min(compact?130:216,Math.max(128,(cardWidth-20)*3/2));card.addView(poster,new LinearLayout.LayoutParams(-1,dp(imageHeight)));poster(poster,r.getPosterUrl(),r.getPosterHeaders(),imagesToken);
   TextView title=text(r.getName(),14,ink);title.setMaxLines(2);title.setEllipsize(TextUtils.TruncateAt.END);title.setPadding(dp(4),dp(8),dp(4),0);card.addView(title,new LinearLayout.LayoutParams(-1,dp(43)));TextView kind=text(CloudCatalog.label(r),11,muted);kind.setPadding(dp(4),0,0,0);card.addView(kind);
   GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=dp(cardWidth-10);lp.height=-2;lp.setMargins(dp(5),dp(5),dp(5),dp(9));grid.addView(card,lp);cards.add(card);card.setOnClickListener(v->{selectedUrl=r.getUrl();savedScroll=scroll.getScrollY();loadDetail(r);});
  }
  for(int i=0;i<cards.size();i++){View v=cards.get(i);v.setNextFocusLeftId(cards.get(i%cols==0?i:i-1).getId());v.setNextFocusRightId(cards.get((i+1)%cols==0||i+1==cards.size()?i:i+1).getId());v.setNextFocusUpId(i<cols?searchButton.getId():cards.get(i-cols).getId());v.setNextFocusDownId(cards.get(CloudCatalog.down(i,cards.size(),cols)).getId());}body.addView(grid);scroll.scrollTo(0,0);
 }
 void loadDetail(SearchResponse row){final MainAPI p=origins.containsKey(row)?origins.get(row):provider;task("Memuat detail “"+row.getName()+"”…",()->cloud.runtime.detail(p,row.getUrl()),item->{if(item==null){status.setText("Detail belum tersedia. Pilih judul atau provider lain.");return;}showDetail(p,item);});}
 void showDetail(MainAPI p,LoadResponse item){detail=true;imageEpoch++;body.removeAllViews();status.setText("Detail · "+p.getName());LinearLayout layout=new LinearLayout(a);layout.setPadding(dp(5),dp(8),dp(5),dp(16));ImageView image=new ImageView(a);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setImageDrawable(placeholder());layout.addView(image,new LinearLayout.LayoutParams(dp(155),dp(232)));poster(image,item.getPosterUrl(),item.getPosterHeaders(),imageEpoch);LinearLayout info=new LinearLayout(a);info.setOrientation(LinearLayout.VERTICAL);info.setPadding(dp(22),0,0,0);layout.addView(info,new LinearLayout.LayoutParams(0,-2,1));TextView name=text(item.getName(),27,ink);name.setTypeface(Typeface.DEFAULT,Typeface.BOLD);info.addView(name);String meta=(item.getYear()==null?"":item.getYear()+" · ")+(item instanceof MovieLoadResponse?"Film":"Serial / Anime");info.addView(text(meta,14,accent));if(item.getTags()!=null&&!item.getTags().isEmpty()){TextView tags=text(TextUtils.join(" · ",item.getTags()),13,muted);tags.setPadding(0,dp(8),0,dp(8));info.addView(tags);}String plot=item.getPlot();TextView synopsis=text(plot==null||plot.trim().isEmpty()?"Sinopsis belum tersedia dari provider.":plot.substring(0,Math.min(plot.length(),2200)),15,ink);synopsis.setLineSpacing(dp(4),1);synopsis.setPadding(0,dp(10),0,dp(16));Button watch=button(item instanceof MovieLoadResponse?"▶  Tonton film":"Pilih episode",()->cloud.watch(p,item));info.addView(watch,new LinearLayout.LayoutParams(-1,dp(46)));Button back=button("Kembali ke poster",this::backToGrid);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,dp(44));bp.topMargin=dp(10);info.addView(back,bp);info.addView(synopsis);body.addView(layout);scroll.scrollTo(0,0);watch.requestFocus();}
 void backToGrid(){epoch++;if(job!=null)job.cancel(true);detail=false;render();View target=null;for(int i=0;i<grid.getChildCount();i++)if(selectedUrl.equals(grid.getChildAt(i).getTag()))target=grid.getChildAt(i);if(target!=null)target.requestFocus();else if(grid.getChildCount()>0)grid.getChildAt(0).requestFocus();scroll.post(()->scroll.scrollTo(0,savedScroll));}
 boolean back(){if(!visible())return false;if(detail){backToGrid();return true;}if(!query.isEmpty()){category=0;tabs();loadHome();return true;}closePage();return true;}
 void hideForPlayback(){if(visible()){returnAfterPlayback=true;epoch++;imageEpoch++;page.setVisibility(View.GONE);a.root.setBackgroundColor(Color.BLACK);a.ui.setVisibility(View.VISIBLE);}}
 void returnFromPlayback(){if(returnAfterPlayback&&page!=null){returnAfterPlayback=false;page.setVisibility(View.VISIBLE);a.root.setBackgroundColor(bg);layoutViewport();a.ui.setVisibility(View.GONE);a.stopPreview();if(detail){View v=body.findFocus();if(v==null)searchButton.requestFocus();}}}
 void closePage(){epoch++;imageEpoch++;if(job!=null)job.cancel(true);returnAfterPlayback=false;page.setVisibility(View.GONE);a.root.setBackgroundColor(Color.BLACK);a.ui.setVisibility(View.VISIBLE);a.previewId="";a.nav=true;a.navFocus=6;a.redraw();}
 void close(){epoch++;imageEpoch++;if(job!=null)job.cancel(true);work.shutdownNow();images.shutdownNow();cache.evictAll();}
 Drawable placeholder(){return new Drawable(){final Paint paint=new Paint(3);public void draw(Canvas c){Rect b=getBounds();paint.setColor(0xff21384b);c.drawRect(b,paint);paint.setColor(muted);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(2));float x=b.centerX(),y=b.centerY();c.drawRoundRect(new RectF(x-dp(20),y-dp(27),x+dp(20),y+dp(8)),dp(3),dp(3),paint);paint.setStyle(Paint.Style.FILL);paint.setTextSize(dp(12));paint.setTextAlign(Paint.Align.CENTER);c.drawText("Tanpa poster",x,y+dp(32),paint);}public void setAlpha(int v){paint.setAlpha(v);}public void setColorFilter(ColorFilter f){paint.setColorFilter(f);}public int getOpacity(){return android.graphics.PixelFormat.OPAQUE;}};}
 void poster(ImageView view,String url,Map<String,String> headers,long token){if(url==null||url.isEmpty())return;view.setTag(url);String key=url+String.valueOf(headers);Bitmap ready=cache.get(key);if(ready!=null){view.setImageBitmap(ready);return;}images.submit(()->{if(token!=imageEpoch||Thread.currentThread().isInterrupted())return;try{byte[] data;if(url.equals("asset:///cloud-demo-poster.jpg")){try(InputStream in=a.getAssets().open("cloud-demo-poster.jpg")){data=readImage(in);}}else{if(!url.startsWith("https://"))return;String next=url;data=null;for(int redirects=0;redirects<4;redirects++){if(!next.startsWith("https://"))return;HttpURLConnection c=(HttpURLConnection)new URL(next).openConnection();c.setInstanceFollowRedirects(false);c.setConnectTimeout(5000);c.setReadTimeout(5000);c.setRequestProperty("User-Agent","ELIO-TV/1.9.3");if(next.equals(url)&&headers!=null)for(Map.Entry<String,String> h:headers.entrySet())if(h.getKey().matches("[A-Za-z0-9-]{1,80}")&&h.getValue()!=null&&!h.getValue().contains("\n")&&!h.getValue().contains("\r"))c.setRequestProperty(h.getKey(),h.getValue());try{int code=c.getResponseCode();if(code>=300&&code<400){String loc=c.getHeaderField("Location");if(loc==null)return;next=new URL(new URL(next),loc).toString();continue;}if(code!=200||c.getContentLengthLong()>2*1024*1024)return;try(InputStream in=c.getInputStream()){data=readImage(in);}break;}finally{c.disconnect();}}if(data==null)return;}
   BitmapFactory.Options bounds=new BitmapFactory.Options();bounds.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,bounds);if(bounds.outWidth<=0||bounds.outHeight<=0||bounds.outWidth>20000||bounds.outHeight>20000)return;BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=1;while(bounds.outWidth/options.inSampleSize>200||bounds.outHeight/options.inSampleSize>300)options.inSampleSize*=2;Bitmap bitmap=BitmapFactory.decodeByteArray(data,0,data.length,options);if(bitmap==null)return;cache.put(key,bitmap);a.handler.post(()->{if(token==imageEpoch&&url.equals(view.getTag())&&!a.isDestroyed())view.setImageBitmap(bitmap);});
  }catch(Exception ignored){}});}
 byte[] readImage(InputStream in)throws Exception{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(Thread.currentThread().isInterrupted()||out.size()+n>2*1024*1024)throw new IOException("Image limit");out.write(b,0,n);}return out.toByteArray();}
}
