package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.nio.charset.StandardCharsets;

/**
 * Helper class for PersistentDataContainers.
 */
public class DataPersistenceHelper {

    public static class DenizenObjectType implements PersistentDataType<byte[], ObjectTag> {
        @Override
        public Class<byte[]> getPrimitiveType() {
            return byte[].class;
        }

        @Override
        public Class<ObjectTag> getComplexType() {
            return ObjectTag.class;
        }

        @Override
        public byte[] toPrimitive(ObjectTag complex, PersistentDataAdapterContext context) {
            return complex.toString().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public ObjectTag fromPrimitive(byte[] primitive, PersistentDataAdapterContext context) {
            return ObjectFetcher.pickObjectFor(new String(primitive, StandardCharsets.UTF_8), CoreUtilities.noDebugContext);
        }
    }

    public static final DenizenObjectType PERSISTER_TYPE = new DenizenObjectType();

    public static void removeDenizenKey(PersistentDataHolder holder, String keyName) {
        holder.getPersistentDataContainer().remove(new NamespacedKey(Denizen.getInstance(), keyName));
    }

    public static void setDenizenKey(PersistentDataHolder holder, String keyName, ObjectTag keyValue) {
        holder.getPersistentDataContainer().set(new NamespacedKey(Denizen.getInstance(), keyName), PERSISTER_TYPE, keyValue);
    }

    public static boolean hasDenizenKey(PersistentDataHolder holder, String keyName) {
        return NMSHandler.instance.containerHas(holder.getPersistentDataContainer(), "denizen:" + keyName);
    }

    public static ObjectTag getDenizenKey(PersistentDataHolder holder, String keyName) {
        try {
            String str = NMSHandler.instance.containerGetString(holder.getPersistentDataContainer(), "denizen:" + keyName);
            if (str == null) {
                return null;
            }
            return ObjectFetcher.pickObjectFor(str, CoreUtilities.noDebugContext);
        }
        catch (IllegalArgumentException ex) {
            if (holder instanceof Entity) {
                Debug.echoError("Failed to read ObjectTag from entity key '" + keyName + "' for entity " + ((Entity) holder).getUniqueId() + "...");
            }
            else {
                Debug.echoError("Failed to read ObjectTag from object key '" + keyName + "' for holder '" + holder + "'...");
            }
            Debug.echoError(ex);
            return null;
        }
    }
}
