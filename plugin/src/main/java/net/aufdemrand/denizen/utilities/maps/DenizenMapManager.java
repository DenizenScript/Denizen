package net.aufdemrand.denizen.utilities.maps;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.aufdemrand.denizencore.utilities.NaturalOrderComparator;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import javax.imageio.stream.FileImageOutputStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class DenizenMapManager {

    private final static Map<Short, DenizenMapRenderer> mapRenderers = new HashMap<Short, DenizenMapRenderer>();
    private final static Map<String, String> downloadedByUrl = new HashMap<String, String>();
    private final static File imagesFolder = new File(DenizenAPI.getCurrentInstance().getDataFolder(), "images");
    private final static File imageDownloads = new File(imagesFolder, "downloaded");
    private final static File mapsFile = new File(DenizenAPI.getCurrentInstance().getDataFolder(), "maps.yml");

    private static int downloadCount = (imageDownloads.exists() ? imageDownloads.listFiles().length : 0) + 1;

    private static YamlConfiguration mapsConfig;

    public static void reloadMaps() {
        Map<Short, List<MapRenderer>> oldMapRenderers = new HashMap<Short, List<MapRenderer>>();
        for (Map.Entry<Short, DenizenMapRenderer> entry : mapRenderers.entrySet()) {
            DenizenMapRenderer renderer = entry.getValue();
            oldMapRenderers.put(entry.getKey(), renderer.getOldRenderers());
            renderer.deactivate();
        }
        mapRenderers.clear();
        downloadedByUrl.clear();
        mapsConfig = YamlConfiguration.loadConfiguration(mapsFile);
        ConfigurationSection mapsSection = mapsConfig.getConfigurationSection("MAPS");
        if (mapsSection == null) {
            return;
        }
        for (String key : mapsSection.getKeys(false)) {
            short mapId = Short.valueOf(key);
            MapView mapView = Bukkit.getServer().getMap(mapId);
            if (mapView == null) {
                dB.echoError("Map #" + key + " does not exist. Has it been removed? Deleting from maps.yml...");
                mapsSection.set(key, null);
                continue;
            }
            ConfigurationSection objectsData = mapsSection.getConfigurationSection(key + ".objects");
            List<MapRenderer> oldRenderers;
            if (oldMapRenderers.containsKey(mapId)) {
                oldRenderers = oldMapRenderers.get(mapId);
            }
            else {
                oldRenderers = mapView.getRenderers();
                for (MapRenderer oldRenderer : oldRenderers) {
                    mapView.removeRenderer(oldRenderer);
                }
            }
            DenizenMapRenderer renderer = new DenizenMapRenderer(oldRenderers,
                    mapsSection.getBoolean(key + ".auto update", false));
            renderer.displayOriginal = mapsSection.getBoolean(key + ".original", true);
            List<String> objects = new ArrayList<String>(objectsData.getKeys(false));
            Collections.sort(objects, new NaturalOrderComparator());
            for (String objectKey : objects) {
                String type = objectsData.getString(objectKey + ".type").toUpperCase();
                String xTag = objectsData.getString(objectKey + ".x");
                String yTag = objectsData.getString(objectKey + ".y");
                String visibilityTag = objectsData.getString(objectKey + ".visibility");
                boolean debug = aH.getBooleanFrom(objectsData.getString(objectKey + ".debug", "false"));
                boolean worldC = aH.getBooleanFrom(objectsData.getString(objectKey + ".world_coordinates", "false"));
                MapObject object = null;
                if (type.equals("CURSOR")) {
                    object = new MapCursor(xTag, yTag, visibilityTag, debug,
                            objectsData.getString(objectKey + ".direction"),
                            objectsData.getString(objectKey + ".cursor"));
                }
                else if (type.equals("IMAGE")) {
                    String file = objectsData.getString(objectKey + ".image");
                    int width = objectsData.getInt(objectKey + ".width", 0);
                    int height = objectsData.getInt(objectKey + ".height", 0);
                    if (CoreUtilities.toLowerCase(file).endsWith(".gif")) {
                        object = new MapAnimatedImage(xTag, yTag, visibilityTag, debug, file, width, height);
                    }
                    else {
                        object = new MapImage(xTag, yTag, visibilityTag, debug, file, width, height);
                    }
                }
                else if (type.equals("TEXT")) {
                    object = new MapText(xTag, yTag, visibilityTag, debug,
                            objectsData.getString(objectKey + ".text"));
                }
                if (object != null) {
                    object.worldCoordinates = worldC;
                    renderer.addObject(object);
                }
            }
            mapView.addRenderer(renderer);
            mapRenderers.put(mapId, renderer);
        }
        for (Map.Entry<Short, List<MapRenderer>> entry : oldMapRenderers.entrySet()) {
            short id = entry.getKey();
            if (!mapRenderers.containsKey(id)) {
                MapView mapView = Bukkit.getServer().getMap(id);
                if (mapView != null) {
                    for (MapRenderer renderer : entry.getValue()) {
                        mapView.addRenderer(renderer);
                    }
                }
                // If it's null, the server no longer has the map - don't do anything about it
            }
        }
        ConfigurationSection downloadedImages = mapsConfig.getConfigurationSection("DOWNLOADED");
        if (downloadedImages == null) {
            return;
        }
        for (String image : downloadedImages.getKeys(false)) {
            downloadedByUrl.put(CoreUtilities.toLowerCase(downloadedImages.getString(image)), image.replace("DOT", "."));
        }
    }

    public static void saveMaps() {
        for (Map.Entry<Short, DenizenMapRenderer> entry : mapRenderers.entrySet()) {
            if (entry.getValue().isActive()) {
                mapsConfig.set("MAPS." + entry.getKey(), entry.getValue().getSaveData());
            }
        }
        for (Map.Entry<String, String> entry : downloadedByUrl.entrySet()) {
            mapsConfig.set("DOWNLOADED." + entry.getValue().replace(".", "DOT"), entry.getKey());
        }
        try {
            mapsConfig.save(mapsFile);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    public static void setMap(MapView map, DenizenMapRenderer denizenMapRenderer) {
        List<MapRenderer> oldRenderers = map.getRenderers();
        for (MapRenderer renderer : oldRenderers) {
            map.removeRenderer(renderer);
        }
        map.addRenderer(denizenMapRenderer);
        mapRenderers.put(map.getId(), denizenMapRenderer);
    }

    public static DenizenMapRenderer getDenizenRenderer(MapView map) {
        short mapId = map.getId();
        DenizenMapRenderer dmr;
        if (!mapRenderers.containsKey(mapId)) {
            dmr = new DenizenMapRenderer(map.getRenderers(), false);
            setMap(map, dmr);
        }
        else {
            dmr = mapRenderers.get(mapId);
        }
        return dmr;
    }

    public static List<MapRenderer> removeDenizenRenderers(MapView map) {
        List<MapRenderer> oldRenderers = new ArrayList<MapRenderer>();
        for (MapRenderer renderer : map.getRenderers()) {
            if (renderer instanceof DenizenMapRenderer) {
                map.removeRenderer(renderer);
                oldRenderers.addAll(((DenizenMapRenderer) renderer).getOldRenderers());
                ((DenizenMapRenderer) renderer).deactivate();
                mapRenderers.remove(map.getId());
            }
        }
        return oldRenderers;
    }

    public static String getActualFile(String file) {
        String fileLower = CoreUtilities.toLowerCase(file);
        if (!fileLower.startsWith("http://") && !fileLower.startsWith("https://")) {
            File f = new File(imagesFolder, file).getPath();
            if (!Utilities.canReadFile(f)) {
                dB.echoError("Server config denies reading files in that location.");
                return null;
            }
        }
        else {
            try {
                return downloadImage(new URL(file));
            }
            catch (MalformedURLException e) {
                dB.echoError("URL is malformed: " + file);
                return null;
            }
        }
    }

    private static String downloadImage(URL url) {
        try {
            if (!imageDownloads.exists()) {
                imageDownloads.mkdirs();
            }
            String urlString = CoreUtilities.toLowerCase(url.toString());
            if (downloadedByUrl.containsKey(urlString)) {
                File image = new File(imageDownloads, downloadedByUrl.get(urlString));
                if (image.exists()) {
                    return image.getPath();
                }
            }
            URLConnection connection = url.openConnection();
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            int lastDot = urlString.lastIndexOf('.');
            String fileName = String.format("%0" + (6 - String.valueOf(downloadCount).length()) + "d", downloadCount)
                    + (lastDot > 0 ? urlString.substring(lastDot) : "");
            File output = new File(imageDownloads, fileName);
            FileImageOutputStream out = new FileImageOutputStream(output);
            int i;
            while ((i = in.read()) != -1) {
                out.write(i);
            }
            out.flush();
            out.close();
            in.close();
            downloadedByUrl.put(urlString, fileName);
            downloadCount++;
            return output.getPath();
        }
        catch (IOException e) {
            dB.echoError(e);
        }
        return null;
    }

}
