package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizencore.flags.MapTagFlagTracker;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

public class DenizenFlagsTrait extends Trait {

    @Persist("full_flag_data")
    public MapTagFlagTracker fullFlagData = new MapTagFlagTracker();


    public static class MapTagFlagTrackerPersister implements Persister<MapTagFlagTracker> {
        @Override
        public MapTagFlagTracker create(DataKey dataKey) {
            return new MapTagFlagTracker(dataKey.getString(""), CoreUtilities.noDebugContext);
        }

        @Override
        public void save(MapTagFlagTracker o, DataKey dataKey) {
            dataKey.setString("", o.toString());
        }
    }

    static {
        PersistenceLoader.registerPersistDelegate(MapTagFlagTracker.class, MapTagFlagTrackerPersister.class);
    }

    public DenizenFlagsTrait() {
        super("denizen_flags");
    }
}
