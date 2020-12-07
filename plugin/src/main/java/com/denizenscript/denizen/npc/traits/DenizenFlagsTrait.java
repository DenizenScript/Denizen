package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.persistence.PersistenceLoader;
import net.citizensnpcs.api.persistence.Persister;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;

import java.util.Map;

public class DenizenFlagsTrait extends Trait {

    @Persist("full_flag_data")
    public SavableMapFlagTracker fullFlagData = new SavableMapFlagTracker();


    public static class MapTagFlagTrackerPersister implements Persister<SavableMapFlagTracker> {
        @Override
        public SavableMapFlagTracker create(DataKey dataKey) {
            SavableMapFlagTracker toRet = new SavableMapFlagTracker();
            for (DataKey key : dataKey.getSubKeys()) {
                toRet.setRootMap(key.name(), MapTag.valueOf(key.getString(""), CoreUtilities.errorButNoDebugContext));
            }
            return toRet;
        }

        @Override
        public void save(SavableMapFlagTracker o, DataKey dataKey) {
            for (Map.Entry<StringHolder, SavableMapFlagTracker.SaveOptimizedFlag> flag : o.map.entrySet()) {
                dataKey.setString(flag.getKey().str, flag.getValue().getString());
            }
        }
    }

    static {
        PersistenceLoader.registerPersistDelegate(SavableMapFlagTracker.class, MapTagFlagTrackerPersister.class);
    }

    public DenizenFlagsTrait() {
        super("denizen_flags");
    }
}
