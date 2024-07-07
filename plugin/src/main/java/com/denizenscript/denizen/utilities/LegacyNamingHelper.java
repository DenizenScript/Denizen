package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.DebugInternals;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class LegacyNamingHelper<T extends Enum<T>> {

    // TODO once 1.21 is the minimum supported version, replace with direct registry-based handling
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Keyed> T convert(Class<T> type, String string) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21)) {
            return Bukkit.getRegistry(type).get(Utilities.parseNamespacedKey(CoreUtilities.toLowerCase(string)));
        }
        return (T) ElementTag.asEnum((Class<? extends Enum>) type, string);
    }

    public static <T extends Keyed> Optional<T> requireType(Mechanism mechanism, Class<T> type) {
        T converted = convert(type, mechanism.getValue().asString());
        if (converted == null) {
            mechanism.echoError("Invalid " + DebugInternals.getClassNameOpti(type) + " specified: must specify a valid name.");
            return Optional.empty();
        }
        return Optional.of(converted);
    }

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
