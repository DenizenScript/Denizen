package com.denizenscript.denizen.utilities.flags;

import com.denizenscript.denizen.utilities.DataPersistenceHelper;
import com.denizenscript.denizencore.flags.MapTagBasedFlagTracker;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.persistence.PersistentDataHolder;

import java.util.Collection;
import java.util.stream.Collectors;

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

    @Override
    public MapTag getRootMap(String key) {
        return (MapTag) DataPersistenceHelper.getDenizenKey(holder, keyPrefix + CoreUtilities.toLowerCase(key));
    }

    @Override
    public void setRootMap(String key, MapTag map) {
        if (map == null) {
            DataPersistenceHelper.removeDenizenKey(holder, keyPrefix + CoreUtilities.toLowerCase(key));
            return;
        }
        DataPersistenceHelper.setDenizenKey(holder, keyPrefix + CoreUtilities.toLowerCase(key), map);
    }

    @Override
    public Collection<String> listAllFlags() {
        return holder.getPersistentDataContainer().getKeys().stream()
                .filter(k -> k.getNamespace().equals("denizen") && k.getKey().startsWith(keyPrefix))
                .map(k -> k.getKey().substring(keyPrefix.length())).collect(Collectors.toList());
    }
}
