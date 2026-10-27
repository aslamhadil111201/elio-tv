package id.matin.nusatv;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

/** XMLTV schedules, isolated from the player. Times are absolute UTC milliseconds. */
final class EpgStore {
 static final String[] ORDER={"faae","community","indonesia","iptvepg","rtm"};
 static final class Programme {
  final long start,end;final String title,source;
  Programme(long a,long b,String t,String s){start=a;end=b;title=t;source=s;}
 }
 static final class Listing {
  final Programme now,next;
  Listing(Programme n,Programme x){now=n;next=x;}
 }
 private volatile Map<String,Map<String,List<Programme>>> feeds=Collections.emptyMap();
 synchronized void replace(String feed,Map<String,List<Programme>> rows){
  Map<String,List<Programme>> copy=new HashMap<>();
  for(Map.Entry<String,List<Programme>> entry:rows.entrySet()){ArrayList<Programme> ps=new ArrayList<>(entry.getValue());Collections.sort(ps,(a,b)->Long.compare(a.start,b.start));copy.put(entry.getKey(),Collections.unmodifiableList(ps));}
  Map<String,Map<String,List<Programme>>> all=new HashMap<>(feeds);all.put(feed,Collections.unmodifiableMap(copy));feeds=Collections.unmodifiableMap(all);
 }
 Listing at(String channel,long time){
  Programme future=null;Map<String,Map<String,List<Programme>>> snapshot=feeds;
  for(String feed:ORDER){Map<String,List<Programme>> rows=snapshot.get(feed);if(rows==null)continue;List<Programme> ps=rows.get(channel);if(ps==null)continue;
   Programme current=null,next=null;
   for(Programme p:ps){if(p.start<=time&&time<p.end){if(current==null||p.start>current.start)current=p;}else if(p.start>time&&(next==null||p.start<next.start))next=p;}
   if(current!=null){Programme following=null;for(Programme p:ps)if(p.start>=current.end&&(following==null||p.start<following.start))following=p;return new Listing(current,following);}
   if(future==null&&next!=null)future=next;
  }
  return new Listing(null,future);
 }
 static long time(String value){
  if(value==null)return -1;String s=value.trim();java.util.regex.Matcher m=java.util.regex.Pattern.compile("^(\\d{14}|\\d{12})\\s*([+-]\\d{4}|Z)?$").matcher(s);if(!m.matches())return -1;
  String digits=m.group(1),offset=m.group(2);if(offset==null||offset.equals("Z"))offset="+0000";
  String input=digits+" "+offset;SimpleDateFormat format=new SimpleDateFormat(digits.length()==14?"yyyyMMddHHmmss Z":"yyyyMMddHHmm Z",Locale.US);format.setLenient(false);format.setTimeZone(TimeZone.getTimeZone("UTC"));ParsePosition pos=new ParsePosition(0);Date d=format.parse(input,pos);return d!=null&&pos.getIndex()==input.length()?d.getTime():-1;
 }
 static String normalize(String value){return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]","");}
 static boolean meaningful(String title,String name,String xmlId){String t=normalize(title);return !t.isEmpty()&&!t.equals(normalize(name))&&!t.equals(normalize(xmlId))&&!t.equals("jadwalbelumtersedia")&&!t.equals("noinformation")&&!t.equals("noepg")&&!t.equals("noprograminformation")&&!t.equals("programmetoinformation")&&!t.equals("siaranlangsung")&&!t.equals("livebroadcast");}
 static Map<String,List<Programme>> parse(InputStream input,String feed,Map<String,String> aliases,Map<String,String> names)throws Exception{
  final Map<String,List<Programme>> rows=new HashMap<>();SAXParserFactory factory=SAXParserFactory.newInstance();factory.setNamespaceAware(false);
  XMLReader reader=factory.newSAXParser().getXMLReader();
  for(String feature:new String[]{"http://xml.org/sax/features/external-general-entities","http://xml.org/sax/features/external-parameter-entities","http://apache.org/xml/features/nonvalidating/load-external-dtd"})try{reader.setFeature(feature,false);}catch(SAXException ignored){}
  reader.setErrorHandler(new DefaultHandler(){public void fatalError(SAXParseException e)throws SAXException{throw e;}public void error(SAXParseException e)throws SAXException{throw e;}});reader.setEntityResolver((publicId,systemId)->new InputSource(new StringReader("")));
  reader.setContentHandler(new DefaultHandler(){
   String channel,xmlId;long start,end;boolean inTitle=false;int count=0;StringBuilder title=new StringBuilder();String savedTitle="";
   public void startElement(String uri,String local,String q,Attributes a)throws SAXException{
    if(q.equals("programme")){if(++count>200000)throw new SAXException("Too many entries");xmlId=a.getValue("channel");channel=aliases.get(xmlId);start=time(a.getValue("start"));end=time(a.getValue("stop"));title.setLength(0);savedTitle="";}
    else if(q.equals("title")&&channel!=null){inTitle=true;title.setLength(0);}
   }
   public void characters(char[] ch,int offset,int length){if(inTitle&&title.length()<300)title.append(ch,offset,Math.min(length,300-title.length()));}
   public void endElement(String uri,String local,String q){
    if(q.equals("title")){if(inTitle&&savedTitle.isEmpty())savedTitle=title.toString().trim().replaceAll("\\s+"," ");inTitle=false;}
    else if(q.equals("programme")){if(channel!=null&&start>=0&&end>start&&meaningful(savedTitle,(names.containsKey(channel)?names.get(channel):""),xmlId)){List<Programme> list=rows.get(channel);if(list==null){list=new ArrayList<>();rows.put(channel,list);}list.add(new Programme(start,end,savedTitle,feed));}channel=null;inTitle=false;}
   }
  });reader.parse(new InputSource(input));return rows;
 }
}
