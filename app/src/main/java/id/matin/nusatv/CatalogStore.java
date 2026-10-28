package id.matin.nusatv;

import org.json.*;
import java.util.*;
import java.net.URI;

/** Remote snapshots contain cumulative overrides of the bundled catalog. */
final class CatalogStore {
 static final int MAX_BYTES=2*1024*1024, MAX_CHANNELS=2000;
 static long revision(String text)throws JSONException {
  JSONObject root=new JSONObject(text);
  if(root.getInt("schema")!=1)throw new JSONException("Unsupported schema");
  long n=root.getLong("revision");if(n<1)throw new JSONException("Invalid revision");return n;
 }
 static JSONArray merge(String bundled,String remote)throws JSONException {
  JSONObject root=new JSONObject(remote);revision(remote);
  JSONArray patches=root.getJSONArray("channels"),base=new JSONArray(bundled);
  if(patches.length()>MAX_CHANNELS||base.length()>MAX_CHANNELS)throw new JSONException("Too many channels");
  LinkedHashMap<String,JSONObject> rows=new LinkedHashMap<>();
  for(int i=0;i<base.length();i++){JSONObject row=base.getJSONObject(i);rows.put(row.getString("id"),new JSONObject(row.toString()));}
  Set<String> seen=new HashSet<>();
  for(int i=0;i<patches.length();i++){
   JSONObject patch=patches.getJSONObject(i);String id=patch.getString("id");
   if(!id.matches("[A-Za-z0-9_-]{1,100}")||!seen.add(id))throw new JSONException("Invalid or duplicate channel id");
   JSONObject row=rows.containsKey(id)?rows.get(id):new JSONObject();
   Iterator<String> keys=patch.keys();while(keys.hasNext()){String key=keys.next();Object value=patch.get(key);if(value==JSONObject.NULL)throw new JSONException("Null field");row.put(key,value);}
   validate(row);rows.put(id,row);
  }
  JSONArray removed=root.optJSONArray("removed");if(root.has("removed")&&removed==null)throw new JSONException("Invalid removed list");
  if(removed!=null){if(removed.length()>MAX_CHANNELS)throw new JSONException("Too many removals");for(int i=0;i<removed.length();i++){String id=removed.getString(i);if(seen.contains(id))throw new JSONException("Conflicting removal");rows.remove(id);}}
  if(rows.isEmpty()||rows.size()>MAX_CHANNELS)throw new JSONException("Invalid catalog size");
  JSONArray result=new JSONArray();for(JSONObject row:rows.values())result.put(row);return result;
 }
 static boolean webUrl(String value,boolean httpsOnly){try{URI u=new URI(value);return u.getHost()!=null&&u.getUserInfo()==null&&("https".equalsIgnoreCase(u.getScheme())||(!httpsOnly&&"http".equalsIgnoreCase(u.getScheme())));}catch(Exception e){return false;}}
 static void validate(JSONObject row)throws JSONException {
  String name=row.getString("name");if(name.trim().isEmpty()||name.length()>160)throw new JSONException("Invalid name");
  for(String k:new String[]{"group","section","owner","legacyId","availabilityNote","officialUrl","liveLabel","logoUrl"})if(row.has(k)&&(!(row.get(k) instanceof String)||row.getString(k).length()>2048))throw new JSONException("Invalid field "+k);
  if(row.has("logoDark")&&!(row.get("logoDark") instanceof Boolean))throw new JSONException("Invalid logo flag");
  if(row.has("epgIds")){JSONObject ids=row.getJSONObject("epgIds");Iterator<String> it=ids.keys();while(it.hasNext())if(!(ids.get(it.next()) instanceof String))throw new JSONException("Invalid EPG id");}
  if(!row.optString("logoUrl").isEmpty()&&!webUrl(row.getString("logoUrl"),true))throw new JSONException("Invalid logo URL");
  if(!row.optString("officialUrl").isEmpty()&&!webUrl(row.getString("officialUrl"),false))throw new JSONException("Invalid official URL");
  JSONArray servers=row.getJSONArray("servers");if(servers.length()>20)throw new JSONException("Too many servers");
  for(int i=0;i<servers.length();i++){JSONObject server=servers.getJSONObject(i);if(!webUrl(server.getString("url"),false))throw new JSONException("Invalid stream URL");
   for(String k:new String[]{"name","drm","licenseUrl","keyId","key"})if(server.has(k)&&!(server.get(k) instanceof String))throw new JSONException("Invalid server field");
   if(!server.optString("licenseUrl").isEmpty()&&!webUrl(server.getString("licenseUrl"),true))throw new JSONException("Invalid license URL");
   if(server.has("headers")){JSONObject headers=server.getJSONObject("headers");if(headers.length()>20)throw new JSONException("Too many headers");Iterator<String> it=headers.keys();while(it.hasNext()){String k=it.next();Object v=headers.get(k);if(!k.matches("[A-Za-z0-9-]{1,80}")||!(v instanceof String)||((String)v).length()>4096||((String)v).contains("\r")||((String)v).contains("\n"))throw new JSONException("Invalid header");}}
  }
 }
}
