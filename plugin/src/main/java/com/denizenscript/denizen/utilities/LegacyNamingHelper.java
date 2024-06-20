package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;

import java.util.HashSet;
import java.util.Set;

public class LegacyNamingHelper<T extends Enum<T>> {

    private final Set<String> modernNames;
    private final Class<T> enumType;

    public LegacyNamingHelper(Class<T> enumType) {
        this.enumType = enumType;
        T[] enumConstants = enumType.getEnumConstants();
        modernNames = new HashSet<>(enumConstants.length);
        for (T enumConstant : enumConstants) {
            modernNames.add(enumConstant.name());
        }
    }

    public T fromName(String name, TagContext context) {
        String nameUpper = CoreUtilities.toUpperCase(name);
        T value;
        try {
            value = Enum.valueOf(enumType, nameUpper);
        }
        catch (IllegalArgumentException ignored) {
            return null;
        }
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && !modernNames.contains(nameUpper)) {
            BukkitImplDeprecations.oldSpigotNames.warn(context);
        }
        return value;
    }
}
