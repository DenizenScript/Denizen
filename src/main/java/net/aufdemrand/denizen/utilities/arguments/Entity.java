package net.aufdemrand.denizen.utilities.arguments;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entity implements dScriptArgument {

    public static Map<String, Entity> entities = new HashMap<String, Entity>();

    /**
     * Gets a saved location based on an Id.
     *
     * @param id  the Id key of the location
     * @return  the Location associated
     */
    public static Entity getSavedEntity(String id) {
        if (entities.containsKey(id.toUpperCase()))
                return entities.get(id.toUpperCase());
        else return null;
    }

    public static void saveEntity(Entity entity) {
        if (entity.id == null) return;
        entities.put(entity.id.toUpperCase(), entity);
    }

    /**
     * Checks if there is a saved item with this Id.
     *
     * @param id  the Id to check
     * @return  true if it exists, false if not
     */
    public static boolean isSavedEntity(String id) {
        return entities.containsKey(id.toUpperCase());
    }

    /**
     * Called on server startup or /denizen reload locations. Should probably not be called manually.
     */
    public static void _recallEntities() {
        List<Map<?, ?>> entitylist = DenizenAPI.getCurrentInstance().getSaves().getMapList("dScript.Entities");
        entities.clear();
        // TODO: Figure out de-serialization of this.
    }

    /**
     * Called by Denizen internally on a server shutdown or /denizen save. Should probably
     * not be called manually.
     */
    public static void _saveEntities() {
        // TODO: Figure out serialization
    }

    /**
     * Gets a Item Object from a string form.
     *
     * @param string  the string or dScript argument String
     * @return  an Item, or null if incorrectly formatted
     *
     */
    public static Entity valueOf(String string) {

        // Create entity!

        return null;
    }

    private String id = null;
    private String prefix = "Entity";

    LivingEntity entity;

    public Entity(LivingEntity entity) {
        this.entity = entity;
    }

    public Entity(String id, LivingEntity entity) {
        this.entity = entity;
        this.id = id;
        saveEntity(this);
    }

    public void identifyAs(String id) {
        if (this.id == null) {
            this.id = id;
            saveEntity(this);
        }
    }

    public LivingEntity getBukkitEntity() {
        return entity;
    }

    public boolean isAlive() {
        return entity != null;
    }

    public Entity setId(String id) {
        this.id = id.toUpperCase();
        return this;
    }

    @Override
    public String getDefaultPrefix() {
        return prefix;
    }

    @Override
    public String debug() {
        return null;
    }

    @Override
    public String as_dScriptArg() {
        return null;
    }

    public String dScriptArgValue() {
        return getDefaultPrefix().toLowerCase() + ":" + as_dScriptArg();
    }

    @Override
    public String toString() {
        return entity.getUniqueId().toString();
    }

    @Override
    public dScriptArgument setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String getAttribute(String attribute) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
