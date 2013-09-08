package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.citizensnpcs.api.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class ScriptRepo {

    private static String API = "http://scripts.citizensnpcs.com/api.php";

    public static void info(CommandSender cs, String id) throws JSONException {
        JSONObject data = queryAPI("view&dscript&pubID="+id);
        if(data.getBoolean("success")){
            JSONObject entry = data.getJSONObject("entryData");
            Messaging.send(cs, entry.getString("pubID")+" - "+entry.getString("name")+" by "+entry.getString("author"));
            Messaging.send(cs, entry.getString("description").replaceAll("\\r\\n", "\n"));
            if(entry.getInt("dscript")==1){
                Messaging.send(cs, "You can load this script with /citizens denizen repo load "+entry.getString("pubID"));
            }else{
                Messaging.send(cs, "This script does not support being loaded ingame. Check http://scripts.citizensnpcs.com/view/"+entry.getString("pubID")+" for info.");
            }
        }else{
            Messaging.send(cs, "Could not find that script!");
        }
    }

    public static void search(CommandSender cs, String query) throws JSONException {
        search(cs, query, 1);
    }

    public static void search(CommandSender cs, String query, int page) throws JSONException {
        JSONObject data = queryAPI("search&dscript&count=8&page="+page+"&query="+query);
        if(data.getBoolean("success")){
            JSONArray resultlist = data.getJSONArray("results");
            ArrayList<JSONObject> results = new ArrayList<JSONObject>();
            for(int i=0; i<resultlist.length(); i++){
                if(!resultlist.isNull(i)){
                    results.add(resultlist.getJSONObject(i));
                }
            }
            if(!results.isEmpty()){
                Messaging.send(cs, results.size()+" results found:");
                for(JSONObject j : results){
                    Messaging.send(cs, j.getString("pubID")+" - "+j.getString("name")+" by "+j.getString("author"));
                }
            }else{
                Messaging.send(cs, "No results found. Try a different search query!");
            }
        }else{
            Messaging.send(cs, "Uh oh, something went wrong...");
        }
    }

    public static void load(CommandSender cs, String id) throws JSONException {
        JSONObject data = queryAPI("download&dscript&pubID="+id);
        if(data.getBoolean("success")){
            String yaml = data.getString("code").replaceAll("\\r\\n", "\n");
            Denizen plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
            File file = new File(plugin.getDataFolder().getAbsolutePath()+File.separator+"scripts"+File.separator+data.getString("name")+".dscript");
            if(!file.exists()){
                BufferedWriter bw = null;
                try {
                    bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
                    bw.write(yaml);
                    bw.close();
                    Messaging.send(cs, "Downloaded script from repo. Reloading scripts...");
                    ScriptHelper.reloadScripts();
                    Messaging.send(cs, "Reloaded scripts!");
                } catch (IOException e) {
                    Messaging.send(cs, "Looks like something went wrong while writing the file. Check console for details.");
                    e.printStackTrace();
                } finally {
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }else{
                Messaging.send(cs, "A script by that name appears to already exist!");
            }
        }else if(data.has("cause") && data.getString("cause").equalsIgnoreCase("dscript")){
            Messaging.send(cs, "This script does not support dscript functionality!");
        }else{
            Messaging.send(cs, "Could not download that script!");
        }
    }

    private static JSONObject queryAPI(String queryArgs) {
        JSONObject j=null;
        try {
            InputStreamReader is = new InputStreamReader(new URL(API + "?" + queryArgs).openStream(), Charset.forName("UTF-8"));
            try {
                j = new JSONObject(readAll(new BufferedReader(is)));
            } finally {
                is.close();
            }
        }catch(IOException e) {
            e.printStackTrace();
        }catch(JSONException e) {
            e.printStackTrace();
        }
        return j;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
