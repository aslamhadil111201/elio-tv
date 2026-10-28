package id.matin.nusatv;

final class SeekWindow {
 static long parse(String value){
  String s=value.trim();if(!s.matches("[0-9]{1,6}(:[0-5][0-9])?"))throw new IllegalArgumentException("Use minutes or mm:ss");
  String[] parts=s.split(":");return (Long.parseLong(parts[0])*60+(parts.length==2?Long.parseLong(parts[1]):0))*1000;
 }
 static long clamp(long position,long duration){return Math.max(0,Math.min(position,Math.max(0,duration)));}
 static String format(long value){long sec=Math.max(0,value/1000);return String.format(java.util.Locale.ROOT,"%02d:%02d",sec/60,sec%60);}
}
