package com.denizenscript.denizen.utilities.flags;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.DataPersistenceHelper;
import com.denizenscript.denizencore.flags.MapTagBasedFlagTracker;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.AsciiMatcher;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.Collection;

public class DataPersistenceFlagTracker extends MapTagBasedFlagTracker {

    public DataPersistenceFlagTracker(PersistentDataHolder holder) {
        this.holder = holder;
    }

    public DataPersistenceFlagTracker(PersistentDataHolder holder, String keyPrefix) {
        this.holder = holder;
        this.keyPrefix = keyPrefix;
    }

    public PersistentDataHolder holder;

    public String keyPrefix = "flag_";

    public static AsciiMatcher allowedKeyText = new AsciiMatcher(AsciiMatcher.LETTERS_LOWER + AsciiMatcher.DIGITS + "_/.-");

    public static String cleanKeyName(String input) {
        return allowedKeyText.trimToMatches(CoreUtilities.toLowerCase(input));
    }

    @Override
    public MapTag getRootMap(String key) {
        return (MapTag) DataPersistenceHelper.getDenizenKey(holder, keyPrefix + cleanKeyName(key));
    }

    @Override
    public void setRootMap(String key, MapTag map) {
        if (map == null) {
            DataPersistenceHelper.removeDenizenKey(holder, keyPrefix + cleanKeyName(key));
            return;
        }
        if (map.containsKey(expirationString) || map.getObject(valueString) instanceof MapTag) {
            holder.getPersistentDataContainer().set(expireNeededKey, PersistentDataType.STRING, "true");
        }
        DataPersistenceHelper.setDenizenKey(holder, keyPrefix + cleanKeyName(key), map);
    }

    @Override
    public Collection<String> listAllFlags() {
        return NMSHandler.instance.containerListFlags(holder.getPersistentDataContainer(), keyPrefix);
    }

    public static NamespacedKey expireNeededKey = new NamespacedKey(Denizen.getInstance(), "expire_flag_check_needed");

    @Override
    public void doTotalClean() {
        if (!holder.getPersistentDataContainer().has(expireNeededKey, PersistentDataType.STRING)) {
            return;
        }
        boolean containsAnyToCheck = false;
        for (NamespacedKey key : holder.getPersistentDataContainer().getKeys()) {
            if (!key.getNamespace().equals("denizen") || !key.getKey().startsWith("flag_")) {
                continue;
            }
            ObjectTag map = DataPersistenceHelper.getDenizenKey(holder, key.getKey());
            if (!(map instanceof MapTag)) {
                continue;
            }
            if (isExpired(((MapTag) map).getObject(expirationString))) {
                holder.getPersistentDataContainer().remove(key);
                containsAnyToCheck = true;
                continue;
            }
            ObjectTag subValue = ((MapTag) map).getObject(valueString);
            if (subValue instanceof MapTag) {
                if (doClean((MapTag) subValue)) {
                    holder.getPersistentDataContainer().set(key, DataPersistenceHelper.PERSISTER_TYPE, map);
                }
                containsAnyToCheck = true;
            }
        }
        if (!containsAnyToCheck) {
            holder.getPersistentDataContainer().remove(expireNeededKey);
        }
    }
}
