package net.aufdemrand.denizen.nms;

import net.aufdemrand.denizen.nms.interfaces.BlockHelper;
import net.aufdemrand.denizen.nms.interfaces.EntityHelper;
import net.aufdemrand.denizen.nms.interfaces.FishingHelper;
import net.aufdemrand.denizen.nms.interfaces.ItemHelper;
import net.aufdemrand.denizen.nms.interfaces.PlayerHelper;
import net.aufdemrand.denizen.nms.util.PlayerProfile;
import org.bukkit.Server;

public abstract class NMSHandler {

    private static NMSHandler instance;
    private static NMSVersion version;

    public static boolean checkServerVersion(Server server) {
        String packageName = server.getClass().getPackage().getName();
        try {
            // Check if we support this MC version
            version = NMSVersion.valueOf(packageName.substring(packageName.lastIndexOf('.') + 1));
        }
        catch (Exception e) {
            version = NMSVersion.NOT_SUPPORTED;
            instance = null;
            return false;
        }
        try {
            // Get the class of our handler for this version
            final Class<?> clazz = Class.forName("net.aufdemrand.denizen.nms.Handler_" + version.name());
            if (NMSHandler.class.isAssignableFrom(clazz)) {
                // Found and loaded - good to go!
                instance = (NMSHandler) clazz.newInstance();
                return true;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // Someone made an oopsie and didn't implement this version properly :(
        version = NMSVersion.NOT_SUPPORTED;
        instance = null;
        return false;
    }

    public static NMSHandler getInstance() {
        return instance;
    }

    public static NMSVersion getVersion() {
        return version;
    }

    public abstract PlayerProfile fillPlayerProfile(PlayerProfile playerProfile);

    public abstract Thread getMainThread();

    public abstract BlockHelper getBlockHelper();

    public abstract EntityHelper getEntityHelper();

    public abstract FishingHelper getFishingHelper();

    public abstract ItemHelper getItemHelper();

    public abstract PlayerHelper getPlayerHelper();
}
