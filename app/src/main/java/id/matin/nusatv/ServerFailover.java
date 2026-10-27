package id.matin.nusatv;
import java.util.Set;
final class ServerFailover {
 static int next(int current,int count,Set<Integer> failed){for(int offset=1;offset<count;offset++){int candidate=(current+offset)%count;if(!failed.contains(candidate))return candidate;}return -1;}
 static int startupTimeoutMs(boolean drm){return drm?8000:6000;}
}
