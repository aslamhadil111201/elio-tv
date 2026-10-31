package id.matin.nusatv;
final class PreviewPolicy {
 static boolean shouldRun(boolean playing,boolean foreground,boolean film,boolean hasChannels){return !playing&&foreground&&!film&&hasChannels;}
 static int serverIndex(int attempt,int count,int preferred){return count<=0?-1:(Math.max(0,preferred)+attempt)%count;}
 static final int SELECT_DELAY_MS=350,FIRST_FRAME_TIMEOUT_MS=9000;
}
