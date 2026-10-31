package id.matin.nusatv;

import org.json.*;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.zip.*;

/** Repository metadata is data; installing executable plugins is a separate user action. */
final class CloudRepository {
 static final int JSON_LIMIT=1024*1024, PLUGIN_LIMIT=8*1024*1024;
 static boolean https(String s){try{URI u=new URI(s);return "https".equalsIgnoreCase(u.getScheme())&&u.getHost()!=null&&u.getUserInfo()==null&&u.getFragment()==null;}catch(Exception e){return false;}}
 static String digest(byte[] bytes)throws Exception{StringBuilder s=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}
 static byte[] fetch(String url,int limit)throws Exception{
  for(int redirect=0;redirect<5;redirect++){
   if(!https(url))throw new IOException("Alamat harus HTTPS tanpa kredensial");
   HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(10000);c.setReadTimeout(15000);c.setInstanceFollowRedirects(false);c.setRequestProperty("User-Agent","ELIO-TV-Cloudstream/experimental");
   try{int code=c.getResponseCode();if(code>=300&&code<400){String location=c.getHeaderField("Location");if(location==null)throw new IOException("Redirect tidak valid");url=new URL(new URL(url),location).toString();continue;}if(code!=200)throw new IOException("HTTP "+code);if(c.getContentLengthLong()>limit)throw new IOException("File terlalu besar");
    try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>limit)throw new IOException("File terlalu besar");out.write(b,0,n);}return out.toByteArray();}
   }finally{c.disconnect();}
  }throw new IOException("Terlalu banyak redirect");
 }
 static JSONObject manifest(String text)throws Exception{JSONObject o=new JSONObject(text);if(o.optInt("manifestVersion",1)!=1)throw new IOException("Versi repo belum didukung");JSONArray lists=o.getJSONArray("pluginLists");if(lists.length()<1||lists.length()>10)throw new IOException("Daftar repo tidak valid");for(int i=0;i<lists.length();i++)if(!https(lists.getString(i)))throw new IOException("Daftar ekstensi harus HTTPS");return o;}
 static JSONArray plugins(String text)throws Exception{JSONArray rows=new JSONArray(text);if(rows.length()>500)throw new IOException("Daftar ekstensi terlalu besar");HashSet<String> seen=new HashSet<>();for(int i=0;i<rows.length();i++){JSONObject p=rows.getJSONObject(i);String url=p.getString("url");if(!https(url)||p.optString("name").trim().isEmpty()||p.optString("name").length()>160||!seen.add(url))throw new IOException("Metadata ekstensi tidak valid");if(p.optInt("apiVersion",1)!=1)throw new IOException("API ekstensi belum didukung");}return rows;}
 static JSONObject inspect(byte[] bytes)throws Exception{
  if(bytes.length<8||bytes.length>PLUGIN_LIMIT)throw new IOException("Paket ekstensi tidak valid");JSONObject manifest=null;boolean dex=false;int entries=0;HashSet<String> names=new HashSet<>();
  int total=0;
  try(ZipInputStream z=new ZipInputStream(new ByteArrayInputStream(bytes))){ZipEntry e;while((e=z.getNextEntry())!=null){if(++entries>10||!names.add(e.getName()))throw new IOException("Isi paket tidak valid");String name=e.getName();if(!name.equals("manifest.json")&&!name.equals("classes.dex"))throw new IOException("Paket dengan resource tambahan belum didukung");ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;int max=name.equals("manifest.json")?32768:PLUGIN_LIMIT;while((n=z.read(b))!=-1){total+=n;if(out.size()+n>max||total>PLUGIN_LIMIT+32768)throw new IOException("Isi paket terlalu besar");out.write(b,0,n);}byte[] content=out.toByteArray();if(name.equals("manifest.json"))manifest=new JSONObject(new String(content,"UTF-8"));else dex=content.length>8&&content[0]=='d'&&content[1]=='e'&&content[2]=='x'&&content[3]=='\n';z.closeEntry();}}

  if(manifest==null||!dex||!manifest.optString("pluginClassName").matches("[A-Za-z_$][A-Za-z0-9_$.]{1,240}"))throw new IOException("Manifest atau DEX tidak valid");
  if(manifest.optBoolean("requiresResources"))throw new IOException("Ekstensi dengan resource belum didukung");return manifest;
 }
}
