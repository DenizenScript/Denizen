package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

/**
 * Helper class for PersistentDataContainers.
 */
public class DataPersistenceHelper {

    public static class DenizenObjectType implements PersistentDataType<String, ObjectTag> {
        @Override
        public Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        public Class<ObjectTag> getComplexType() {
            return ObjectTag.class;
        }

        @Override
        public String toPrimitive(ObjectTag complex, PersistentDataAdapterContext context) {
            return complex.toString();
        }

        @Override
        public ObjectTag fromPrimitive(String primitive, PersistentDataAdapterContext context) {
            return ObjectFetcher.pickObjectFor(primitive, CoreUtilities.noDebugContext);
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
        return holder.getPersistentDataContainer().has(new NamespacedKey(Denizen.getInstance(), keyName), PERSISTER_TYPE);
    }

    public static ObjectTag getDenizenKey(PersistentDataHolder holder, String keyName) {
        try {
            return holder.getPersistentDataContainer().get(new NamespacedKey(Denizen.getInstance(), keyName), PERSISTER_TYPE);
        }
        catch (NullPointerException ex) {
            return null;
        }
        catch (IllegalArgumentException ex) {
            if (holder instanceof Entity) {
                Debug.echoError("Failed to read ObjectTag from entity key '" + keyName + "' for entity " + ((Entity) holder).getUniqueId() + "...");
            }
            else {
                Debug.echoError("Failed to read ObjectTag from object key '" + keyName + "' for holder '" + holder.toString() + "'...");
            }
            Debug.echoError(ex);
            return null;
        }
    }
}
