package id.matin.nusatv;
public class SeekWindowTest {
 static int checks;
 static void check(boolean v){checks++;if(!v)throw new AssertionError();}
 public static void main(String[] args){check(SeekWindow.parse("5")==300000);check(SeekWindow.parse("05:30")==330000);check(SeekWindow.parse("0")==0);check(SeekWindow.parse(" 01:02 ")==62000);check(SeekWindow.parse("999999:59")==59999999000L);
 for(String value:new String[]{"-1","","1:60","1:2","1:00:00","abc","9999999","1.5"})try{SeekWindow.parse(value);throw new AssertionError();}catch(IllegalArgumentException expected){checks++;}
 check(SeekWindow.clamp(-30000,100000)==0);check(SeekWindow.clamp(130000,100000)==100000);check(SeekWindow.clamp(30000,100000)==30000);check(SeekWindow.format(330000).equals("05:30"));check(SeekWindow.format(3661000).equals("61:01"));System.out.println(checks+" seek parser and bounds tests PASS");}
}
