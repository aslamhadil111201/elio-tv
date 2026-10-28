package id.matin.nusatv;
import java.nio.file.*;
import java.util.*;
import java.net.*;
import java.io.*;
import org.json.*;
public class CatalogStoreTest {
 static int checks;
 static void check(boolean v){checks++;if(!v)throw new AssertionError("case "+checks);}
 static String base="[{\"id\":\"one\",\"name\":\"One\",\"servers\":[{\"name\":\"S1\",\"url\":\"https://example.org/one.m3u8\"}]},{\"id\":\"two\",\"name\":\"Two\",\"servers\":[]}]";
 static String snapshot(JSONArray rows){return new JSONObject().put("schema",1).put("revision",1).put("channels",rows).toString();}
 static void bad(String data){try{CatalogStore.merge(base,data);throw new AssertionError("accepted invalid data");}catch(JSONException expected){checks++;}}
 static JSONObject row(String id){return new JSONObject().put("id",id);}
 static class Fake extends HttpURLConnection {
  int status;String location;byte[] body;long size;boolean disconnected;
  Fake(URL u,int status,String location,byte[] body,long size){super(u);this.status=status;this.location=location;this.body=body;this.size=size;}
  public void connect(){} public boolean usingProxy(){return false;} public void disconnect(){disconnected=true;}
  public int getResponseCode(){return status;} public String getHeaderField(String name){return "Location".equals(name)?location:null;}
  public int getContentLength(){return (int)size;} public InputStream getInputStream(){return new ByteArrayInputStream(body);}
 }
 static Queue<Fake> replies=new ArrayDeque<>();
 static void httpBad(String url,int max){try{CatalogHttp.fetch(url,max);throw new AssertionError("accepted bad HTTP");}catch(IOException expected){checks++;}}
 public static void main(String[] args)throws Exception{
  check(CatalogStore.revision(snapshot(new JSONArray()))==1);
  JSONArray unchanged=CatalogStore.merge(base,snapshot(new JSONArray()));check(unchanged.length()==2);
  JSONArray rename=new JSONArray().put(row("one").put("name","New name"));JSONArray result=CatalogStore.merge(base,snapshot(rename));check(result.getJSONObject(0).getString("name").equals("New name"));check(result.getJSONObject(0).getJSONArray("servers").length()==1);
  check(new JSONArray(base).getJSONObject(0).getString("name").equals("One"));
  JSONObject add=row("three").put("name","Three").put("servers",new JSONArray());check(CatalogStore.merge(base,snapshot(new JSONArray().put(add))).length()==3);
  JSONObject source=row("one").put("servers",new JSONArray().put(new JSONObject().put("url","https://example.org/new.m3u8")));
  check(CatalogStore.merge(base,snapshot(new JSONArray().put(source))).getJSONObject(0).getJSONArray("servers").getJSONObject(0).getString("url").endsWith("new.m3u8"));
  String deleted=new JSONObject(snapshot(new JSONArray())).put("removed",new JSONArray().put("two")).toString();check(CatalogStore.merge(base,deleted).length()==1);
  bad("not json");bad("{}");bad(snapshot(new JSONArray()).replace("\"schema\":1","\"schema\":2"));bad(snapshot(new JSONArray()).replace("\"revision\":1","\"revision\":0"));
  bad(snapshot(new JSONArray().put(row("one")).put(row("one"))));bad(snapshot(new JSONArray().put(row("../bad"))));bad(snapshot(new JSONArray().put(row("new"))));
  bad(snapshot(new JSONArray().put(row("one").put("servers","bad"))));bad(snapshot(new JSONArray().put(row("one").put("logoUrl","http://example.org/a.png"))));
  bad(snapshot(new JSONArray().put(row("one").put("name",JSONObject.NULL))));bad(snapshot(new JSONArray().put(row("one").put("epgIds",new JSONObject().put("faae",17)))));
  for(String u:new String[]{"file:///etc/passwd","javascript:alert(1)","https://user:password@example.org/a","ftp://example.org/a"}){bad(snapshot(new JSONArray().put(row("one").put("servers",new JSONArray().put(new JSONObject().put("url",u))))));}
  bad(snapshot(new JSONArray().put(row("one").put("servers",new JSONArray().put(new JSONObject().put("url","https://example.org/a").put("headers",new JSONObject().put("X-Test","bad\r\nheader")))))));
  bad(new JSONObject(snapshot(new JSONArray())).put("removed",new JSONArray().put("one").put("two")).toString());
  bad(new JSONObject(snapshot(new JSONArray().put(row("one")))).put("removed",new JSONArray().put("one")).toString());
  bad(new JSONObject(snapshot(new JSONArray())).put("removed",true).toString());
  String real=Files.readString(Paths.get(args[0])),remote=Files.readString(Paths.get(args[1]));check(CatalogStore.merge(real,remote).length()==131);
  URL.setURLStreamHandlerFactory(protocol->"https".equals(protocol)?new URLStreamHandler(){protected URLConnection openConnection(URL u){Fake f=replies.remove();return f;}}:null);
  URL u=new URL("https://example.org/catalog.json");Fake good=new Fake(u,200,null,"{}".getBytes(),2);replies.add(good);check(new String(CatalogHttp.fetch(u.toString(),10)).equals("{}"));check(good.disconnected);
  replies.add(new Fake(u,503,null,new byte[0],0));httpBad(u.toString(),10);
  replies.add(new Fake(u,200,null,new byte[11],11));httpBad(u.toString(),10);
  replies.add(new Fake(u,200,null,new byte[11],-1));httpBad(u.toString(),10);
  replies.add(new Fake(u,302,"http://example.org/insecure",new byte[0],0));httpBad(u.toString(),10);
  replies.add(new Fake(u,302,"/new",new byte[0],0));replies.add(new Fake(u,200,null,new byte[]{42},1));check(CatalogHttp.fetch(u.toString(),10)[0]==42);
  for(int i=0;i<5;i++)replies.add(new Fake(u,302,"/again",new byte[0],0));httpBad(u.toString(),10);
  httpBad("file:///bad",10);check(replies.isEmpty());
  System.out.println(checks+" catalog merge/validation/HTTP tests PASS");
 }
}
