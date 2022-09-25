package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizencore.flags.SavableMapFlagTracker;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import com.google.common.collect.Iterators;
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
                SavableMapFlagTracker.SaveOptimizedFlag flag = new SavableMapFlagTracker.SaveOptimizedFlag();
                flag.string = key.getString("");
                flag.canExpire = flag.string.startsWith("map@");
                toRet.map.put(new StringHolder(key.name()), flag);
            }
            if (!CoreConfiguration.skipAllFlagCleanings) {
                toRet.doTotalClean();
            }
            return toRet;
        }

        @Override
        public void save(SavableMapFlagTracker o, DataKey dataKey) {
            for (DataKey subkey : Iterators.toArray(dataKey.getSubKeys().iterator(), DataKey.class)) {
                dataKey.removeKey(subkey.name());
            }
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
