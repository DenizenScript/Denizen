package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.dObject;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

/**
 * Helper class for PersistentDataContainers.
 */
public class DataPersistenceHelper {

    public static class DenizenObjectType implements PersistentDataType<String, dObject> {
        @Override
        public Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        public Class<dObject> getComplexType() {
            return dObject.class;
        }

        @Override
        public String toPrimitive(dObject complex, PersistentDataAdapterContext context) {
            return complex.toString();
        }

        @Override
        public dObject fromPrimitive(String primitive, PersistentDataAdapterContext context) {
            return ObjectFetcher.pickObjectFor(primitive);
        }
    }

    public static final DenizenObjectType PERSISTER_TYPE = new DenizenObjectType();

    public static void setDenizenKey(Entity entity, String keyName, dObject keyValue) {
        entity.getPersistentDataContainer().set(new NamespacedKey(DenizenAPI.getCurrentInstance(), keyName), PERSISTER_TYPE, keyValue);
    }

    public static boolean hasDenizenKey(Entity entity, String keyName) {
        return entity.getPersistentDataContainer().has(new NamespacedKey(DenizenAPI.getCurrentInstance(), keyName), PERSISTER_TYPE);
    }

    public static dObject getDenizenKey(Entity entity, String keyName) {
        try {
            return entity.getPersistentDataContainer().get(new NamespacedKey(DenizenAPI.getCurrentInstance(), keyName), PERSISTER_TYPE);
        }
        catch (NullPointerException ex) {
            return null;
        }
        catch (IllegalArgumentException ex) {
            dB.echoError("Failed to read dObject from entity key '" + keyName + "' for entity " + entity.getUniqueId() + "...");
            dB.echoError(ex);
            return null;
        }
    }
}
