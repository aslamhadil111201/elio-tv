package id.matin.nusatv;

import java.io.*;
import java.net.*;

final class CatalogHttp {
 static byte[] fetch(String url,int max)throws IOException {
  for(int redirect=0;redirect<=4;redirect++){
   if(!CatalogStore.webUrl(url,true))throw new IOException("HTTPS required");
   HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();
   c.setConnectTimeout(6000);c.setReadTimeout(6000);c.setInstanceFollowRedirects(false);c.setRequestProperty("User-Agent","ELIO-TV/1.9.0");c.setRequestProperty("Accept-Encoding","identity");c.setUseCaches(false);
   try{int status=c.getResponseCode();if(status>=300&&status<=399){String location=c.getHeaderField("Location");if(location==null)throw new IOException("Missing redirect");url=new URL(new URL(url),location).toString();continue;}
    if(status!=200)throw new IOException("HTTP "+status);if(c.getContentLength()>max)throw new IOException("Too large");
    try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;long deadline=System.nanoTime()+15000000000L;while((n=in.read(b))!=-1){if(out.size()+n>max||System.nanoTime()>deadline)throw new IOException("Download limit");out.write(b,0,n);}return out.toByteArray();}
   }finally{c.disconnect();}
  }throw new IOException("Too many redirects");
 }
}
