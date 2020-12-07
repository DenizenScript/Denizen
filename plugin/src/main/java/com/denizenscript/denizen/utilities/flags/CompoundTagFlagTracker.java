package com.denizenscript.denizen.utilities.flags;

import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.util.jnbt.StringTag;
import com.denizenscript.denizencore.flags.MapTagBasedFlagTracker;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;

import java.util.Collection;
import java.util.Map;

public class CompoundTagFlagTracker extends MapTagBasedFlagTracker {

    public CompoundTagFlagTracker(CompoundTag tag) {
        this.map = (Map) tag.getValue();
    }

    public Map<String, StringTag> map;

    @Override
    public MapTag getRootMap(String key) {
        StringTag str = map.get(CoreUtilities.toLowerCase(key));
        if (str == null) {
            return null;
        }
        return MapTag.valueOf(str.getValue(), CoreUtilities.errorButNoDebugContext);
    }

    @Override
    public void setRootMap(String key, MapTag value) {
        map.put(CoreUtilities.toLowerCase(key), new StringTag(value.toString()));
    }

    @Override
    public Collection<String> listAllFlags() {
        return map.keySet();
    }
}
