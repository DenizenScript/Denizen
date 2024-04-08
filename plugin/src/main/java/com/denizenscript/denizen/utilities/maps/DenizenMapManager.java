package com.denizenscript.denizen.utilities.maps;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.AsciiMatcher;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.NaturalOrderComparator;
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

    private final static Map<Integer, DenizenMapRenderer> mapRenderers = new HashMap<>();
    private final static Map<String, String> downloadedByUrl = new HashMap<>();
    private final static File imagesFolder = new File(Denizen.getInstance().getDataFolder(), "images");
    private final static File imageDownloads = new File(imagesFolder, "downloaded");
    private final static File mapsFile = new File(Denizen.getInstance().getDataFolder(), "maps.yml");

    private static int downloadCount = (imageDownloads.exists() ? imageDownloads.listFiles().length : 0) + 1;

    private static YamlConfiguration mapsConfig;

    public static void reloadMaps() {
        Map<Integer, List<MapRenderer>> oldMapRenderers = new HashMap<>();
        for (Map.Entry<Integer, DenizenMapRenderer> entry : mapRenderers.entrySet()) {
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
            int mapId = Integer.parseInt(key);
            MapView mapView = Bukkit.getServer().getMap((short) mapId); // TODO: ??? (deprecated short method)
            if (mapView == null) {
                Debug.echoError("Map #" + key + " does not exist. Has it been removed? Deleting from maps.yml...");
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
            boolean contextual = mapsSection.getBoolean(key + ".contextual", true);
            DenizenMapRenderer renderer = new DenizenMapRenderer(oldRenderers, mapsSection.getBoolean(key + ".auto update", false), contextual);
            renderer.displayOriginal = mapsSection.getBoolean(key + ".original", true);
            List<String> objects = new ArrayList<>(objectsData.getKeys(false));
            objects.sort(new NaturalOrderComparator());
            for (String objectKey : objects) {
                ConfigurationSection objectConfig = objectsData.getConfigurationSection(objectKey);
                String type = objectConfig.getString("type").toUpperCase();
                String xTag = objectConfig.getString("x");
                String yTag = objectConfig.getString("y");
                String visibilityTag = objectConfig.getString("visibility");
                boolean debug = objectConfig.getString("debug", "false").equalsIgnoreCase("true");
                MapObject object = null;
                switch (type) {
                    case "CURSOR":
                        object = new MapCursor(xTag, yTag, visibilityTag, debug, objectConfig.getString("direction"), objectConfig.getString("cursor"));
                        break;
                    case "IMAGE":
                        String file = objectConfig.getString("image");
                        int width = objectConfig.getInt("width", 0);
                        int height = objectConfig.getInt("height", 0);
                        object = new MapImage(renderer, xTag, yTag, visibilityTag, debug, file, width, height);
                        break;
                    case "TEXT":
                        object = new MapText(xTag, yTag, visibilityTag, debug, objectConfig.getString("text"), objectConfig.getString("color"),
                                objectConfig.getString("font"), objectConfig.getString("size"), objectConfig.getString("style"));
                        break;
                    case "DOT":
                        object = new MapDot(xTag, yTag, visibilityTag, debug, objectConfig.getString("radius"), objectConfig.getString("color"));
                        break;
                }
                if (object != null) {
                    object.worldCoordinates = objectConfig.getString("world_coordinates", "false").equalsIgnoreCase("true");
                    object.showPastEdge = objectConfig.getString("show_past_edge", "false").equalsIgnoreCase("true");
                    renderer.addObject(object);
                }
            }
            mapView.addRenderer(renderer);
            mapRenderers.put(mapId, renderer);
        }
        for (Map.Entry<Integer, List<MapRenderer>> entry : oldMapRenderers.entrySet()) {
            int id = entry.getKey();
            if (!mapRenderers.containsKey(id)) {
                MapView mapView = Bukkit.getServer().getMap((short) id); // TODO: ??? (deprecated short method)
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
        for (Map.Entry<Integer, DenizenMapRenderer> entry : mapRenderers.entrySet()) {
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
            Debug.echoError(e);
        }
    }

    public static void setMap(MapView map, DenizenMapRenderer denizenMapRenderer) {
        List<MapRenderer> oldRenderers = map.getRenderers();
        for (MapRenderer renderer : oldRenderers) {
            map.removeRenderer(renderer);
        }
        map.addRenderer(denizenMapRenderer);
        mapRenderers.put(map.getId(), denizenMapRenderer);
        denizenMapRenderer.hasChanged = true;
    }

    public static DenizenMapRenderer getDenizenRenderer(MapView map) {
        int mapId = map.getId();
        DenizenMapRenderer dmr;
        if (!mapRenderers.containsKey(mapId)) {
            boolean contextual = map.isTrackingPosition() || map.isUnlimitedTracking();
            dmr = new DenizenMapRenderer(map.getRenderers(), false, contextual);
            setMap(map, dmr);
        }
        else {
            dmr = mapRenderers.get(mapId);
        }
        return dmr;
    }

    public static List<MapRenderer> removeDenizenRenderers(MapView map) {
        List<MapRenderer> oldRenderers = new ArrayList<>();
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
            File f = new File(imagesFolder, file);
            if (!Utilities.canReadFile(f)) {
                Debug.echoError("Cannot read from that file path due to security settings in Denizen/config.yml.");
                return null;
            }
            return f.getPath();
        }
        else {
            try {
                return downloadImage(new URL(file));
            }
            catch (MalformedURLException e) {
                Debug.echoError("URL is malformed: " + file);
                return null;
            }
        }
    }

    public static HashSet<String> failedUrls = new HashSet<>();

    public static AsciiMatcher allowedExtensionText = new AsciiMatcher(AsciiMatcher.LETTERS_LOWER + AsciiMatcher.DIGITS);

    private static String downloadImage(URL url) {
        try {
            if (failedUrls.contains(url.toString())) {
                return null;
            }
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
                    + (lastDot > 0 ? "." + allowedExtensionText.trimToMatches(urlString.substring(lastDot)) : "");
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
            failedUrls.add(url.toString());
            Debug.echoError(e);
        }
        return null;
    }
}
