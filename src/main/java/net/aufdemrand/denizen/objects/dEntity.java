package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.EntityScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.CustomNBT;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Ocelot.Type;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dEntity implements dObject {

    /////////////////////
    //   PATTERNS
    /////////////////
    
    final static Pattern entity_by_id =
            Pattern.compile("(n@|e@|p@)(.+)",
                    Pattern.CASE_INSENSITIVE);
    final static Pattern entity_with_data =
            Pattern.compile("(\\w+),?(\\w+)?,?(\\w+)?",
                    Pattern.CASE_INSENSITIVE);
    
    
    /////////////////////
    //   STATIC METHODS
    /////////////////

    public static Map<String, dEntity> uniqueObjects = new HashMap<String, dEntity>();

    public static boolean isSaved(String id) {
        return uniqueObjects.containsKey(id.toUpperCase());
    }

    public static boolean isSaved(dEntity entity) {
        return uniqueObjects.containsValue(entity);
    }

    public static dEntity getSaved(String id) {
        if (uniqueObjects.containsKey(id.toUpperCase()))
            return uniqueObjects.get(id.toUpperCase());
        else return null;
    }

    public static String getSaved(dEntity entity) {
        for (Map.Entry<String, dEntity> i : uniqueObjects.entrySet())
            if (i.getValue() == entity) return i.getKey();
        return null;
    }

    public static void saveAs(dEntity entity, String id) {
        if (entity == null) return;
        uniqueObjects.put(id.toUpperCase(), entity);
    }

    public static void remove(String id) {
        uniqueObjects.remove(id.toUpperCase());
    }


    //////////////////
    //    OBJECT FETCHER
    ////////////////

    /**
     * Gets a dEntity Object from a string form. </br>
     * </br>
     * Unique dEntities: </br>
     * n@13 will return the entity object of NPC 13 </br>
     * e@5884 will return the entity object for the entity with the entityid of 5884 </br>
     * e@jimmys_pet will return the saved entity object for the id 'jimmys pet' </br>
     * p@aufdemrand will return the entity object for aufdemrand </br>
     * </br>
     * New dEntities: </br>
     * zombie will return an unspawned Zombie dEntity </br>
     * super_creeper will return an unspawned custom 'Super_Creeper' dEntity </br>
     *
     * @param string  the string or dScript argument String
     * @return  a dEntity, or null
     */
    @ObjectFetcher("e")
    public static dEntity valueOf(String string) {
        if (string == null) return null;

        ///////
        // Match @object format

        // Make sure string matches what this interpreter can accept.


        Matcher m;
        m = entity_by_id.matcher(string);

        if (m.matches()) {
            
            String entityGroup = m.group(1).toUpperCase();

            // NPC entity
            if (entityGroup.matches("N@")) {
                NPC returnable = CitizensAPI.getNPCRegistry()
                        .getById(Integer.valueOf(m.group(2)));

                if (returnable != null) return new dEntity(returnable.getBukkitEntity());
                else dB.echoError("Invalid NPC! '" + entityGroup
                        + "' could not be found. Has it been despawned or killed?");
            }

            // Player entity
            else if (entityGroup.matches("P@")) {
                LivingEntity returnable = aH.getPlayerFrom(m.group(2)).getPlayerEntity();

                if (returnable != null) return new dEntity(returnable);
                else dB.echoError("Invalid Player! '" + entityGroup
                        + "' could not be found. Has the player logged off?");
            }

            // Assume entity
            else {
                if (aH.matchesInteger(m.group(2))) {
                    int entityID = Integer.valueOf(m.group(2));
                    Entity entity = null;

                    for (World world : Bukkit.getWorlds()) {
                        entity = ((CraftWorld) world).getHandle().getEntity(entityID).getBukkitEntity();
                        if (entity != null) break;
                    }
                    if (entity != null) return new dEntity(entity);
                }

                else if (isSaved(m.group(2)))
                    return getSaved(m.group(2));
            }
        }

        string = string.replace("e@", "");

        ////////
        // Match Custom Entity

        if (ScriptRegistry.containsScript(string, EntityScriptContainer.class)) {
            // Construct a new custom unspawned entity from script
            return ScriptRegistry.getScriptContainerAs(m.group(0), EntityScriptContainer.class).getEntityFrom();
        }

        ////////
        // Match Entity_Type

        m = entity_with_data.matcher(string);

        String data = null;
        
        if (m.matches()) {
            
        	if (m.group(2) != null) {
        		
        		data = m.group(2).toUpperCase();
        	}
        
            for (EntityType type : EntityType.values()) {
                if (type.name().equalsIgnoreCase(m.group(1)))
                    // Construct a new 'vanilla' unspawned dEntity                	
                    return new dEntity(type, data);
            }
        }

        dB.log("valueOf dEntity returning null: " + string);

        return null;
    }


    public static boolean matches(String arg) {

        Matcher m;
        m = entity_by_id.matcher(arg);
        if (m.matches()) return true;

        arg = arg.replace("e@", "");

        if (ScriptRegistry.containsScript(arg, EntityScriptContainer.class))
            return true;

        m = entity_with_data.matcher(arg);
        
        if (m.matches()) {
        
        	for (EntityType type : EntityType.values())
            	if (type.name().equalsIgnoreCase(m.group(1))) return true;
        }

        return false;
    }


    /////////////////////
    //   CONSTRUCTORS
    //////////////////

    public dEntity(Entity entity) {
        if (entity != null) {
            this.entity = entity;
            this.entity_type = entity.getType();
        } else dB.echoError("Entity referenced is null!");
    }

    public dEntity(EntityType entityType) {
        if (entityType != null) {
            this.entity = null;
            this.entity_type = entityType;
        } else dB.echoError("Entity_type referenced is null!");
    }
    
    public dEntity(EntityType entityType, String data) {
        if (entityType != null) {
            this.entity = null;
            this.entity_type = entityType;
            this.data = data;
        } else dB.echoError("Entity_type referenced is null!");
    }


    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////


    private Entity entity = null;
    private EntityType entity_type = null;
    private String data = null;
    private DespawnedEntity despawned_entity = null;

    public Entity getBukkitEntity() {
        return entity;
    }

    public LivingEntity getLivingEntity() {
        if (entity instanceof LivingEntity)
            return (LivingEntity) entity;
        else return null;
    }

    public boolean isLivingEntity() {
        return (entity instanceof LivingEntity);
    }

    public void spawnAt(Location location) {
        // If the entity is already spawned, teleport it.
        if (entity != null && isUnique()) entity.teleport(location);

        else {
            if (entity_type != null) {
                if (despawned_entity != null) {
                    // If entity had a custom_script, use the script to rebuild the base entity.
                    if (despawned_entity.custom_script != null)
                    { } // TODO: Build entity from custom script
                    // Else, use the entity_type specified/remembered
                    else entity = location.getWorld().spawnEntity(location, entity_type);

                    getLivingEntity().teleport(location);
                    getLivingEntity().getEquipment().setArmorContents(despawned_entity.equipment);
                    getLivingEntity().setHealth(despawned_entity.health);
                    
                    despawned_entity = null;
                }

                else {
                    org.bukkit.entity.Entity ent = location.getWorld().spawnEntity(location, entity_type);
                    
                    if (ent instanceof LivingEntity) {
                    	entity = (LivingEntity) ent;
                    }
                    
                    // If there is some special data associated with this dEntity,
                    // use the setSubtype method to set it in a clean, object-oriented
                    // way that uses reflection
                    if (data != null) {
                    	
                    	try {
                    		
                    		if (ent instanceof Ocelot) {
                            
                    			setSubtype(Ocelot.class, "Type", "setCatType", data);
                            }
                    		else if (ent instanceof Skeleton) {
                                
                    			setSubtype(Skeleton.class, "SkeletonType", "setSkeletonType", data);
                            }
    						
    					} catch (IllegalArgumentException e) {
    						e.printStackTrace();
    					} catch (SecurityException e) {
    						e.printStackTrace();
    					} catch (IllegalAccessException e) {
    						e.printStackTrace();
    					} catch (InvocationTargetException e) {
    						e.printStackTrace();
    					} catch (NoSuchMethodException e) {
    						e.printStackTrace();
    					} catch (ClassNotFoundException e) {
    						e.printStackTrace();
    					}
                    }
                }
            }

            else dB.echoError("Cannot spawn a null dEntity!");
        }
    }

    public void despawn() {
        despawned_entity = new DespawnedEntity(this);
        getLivingEntity().remove();
    }

    public void respawn() {
        if (despawned_entity != null)
            spawnAt(despawned_entity.location);
        else if (entity != null)
            dB.echoDebug("Entity " + identify() + " is already spawned!");
        else
            dB.echoError("Cannot respawn a null dEntity!");

    }

    public boolean isSpawned() {
        return entity != null;
    }

    public dEntity rememberAs(String id) {
        dEntity.saveAs(this, id);
        return this;
    }
    
    /**
     * Set the subtype of this entity by using the chosen method and Enum from
     * this Bukkit entity's class and:
     * 1) using a random subtype if value is "RANDOM"
     * 2) looping through the entity's subtypes until one matches the value string
     *
     * Example: setSubtype(Ocelot.class, "Type", "setCatType", "SIAMESE_CAT");
     * 
     * @param entityClass  The Bukkit entity class of the entity.
     * @param typeName  The name of the entity class' Enum with subtypes.
     * @param method  The name of the method used to set the subtype of this entity.
     * @param value  The value of the subtype.
     */

    public void setSubtype (Class<? extends Entity> entityClass, String typeName, String method, String value)
    		throws IllegalArgumentException, SecurityException, IllegalAccessException,
    		InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
    	
    	Class<?> typeClass = Class.forName(entityClass.getName() + "$" + typeName);
    	Object[] types = typeClass.getEnumConstants();
    	
    	if (value.matches("RANDOM")) {
    	
    		entityClass.getMethod(method, typeClass).invoke(entity, types[new Random().nextInt(types.length)]);
    	}
    	else { 
    		for (Object type : types) {
    		
    			if (type.toString().equalsIgnoreCase(value)) {
    			
    				entityClass.getMethod(method, typeClass).invoke(entity, type);
    				break;
    			}
    		}
    	}
    }


    // Used to store some information about a livingEntity while it's despawned
    private class DespawnedEntity {

        Integer health = null;
        Location location = null;
        ItemStack[] equipment = null;
        String custom_script = null;

        public DespawnedEntity(dEntity entity) {
            if (entity != null) {
                // Save some important info to rebuild the entity
                health = entity.getLivingEntity().getHealth();
                location = entity.getLivingEntity().getLocation();
                equipment = entity.getLivingEntity().getEquipment().getArmorContents();

                if (CustomNBT.hasCustomNBT(entity.getLivingEntity(), "denizen-script-id"))
                    custom_script = CustomNBT.getCustomNBT(entity.getLivingEntity(), "denizen-script-id");
            }
        }
    }



    //////////////////////////////
    //  DSCRIPT ARGUMENT METHODS
    /////////////////////////

    private String prefix = "Entity";

    @Override
    public String getType() {
        return "dEntity";
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public dEntity setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public String debug() {
        return "<G>" + prefix + "='<Y>" + identify() + "<G>'  ";
    }

    @Override
    public String identify() {

        // Check if entity is a Player or NPC
        if (getBukkitEntity() != null) {
            if (CitizensAPI.getNPCRegistry().isNPC(getBukkitEntity()))
                return "n@" + CitizensAPI.getNPCRegistry().getNPC(getBukkitEntity()).getId();
            else if (getBukkitEntity() instanceof Player)
                return "p@" + ((Player) getBukkitEntity()).getName();
        }

        // Check if entity is a 'saved entity'
        else if (isUnique())
            return "e@" + getSaved(this);

            // Check if entity is spawned, therefore having a bukkit entityId
        else if (isSpawned())
            return "e@" + getBukkitEntity().getEntityId();

            // Check if an entity_type is available
        else if (entity_type != null)
            return "e@" + entity_type.name();

        return "null";
    }

    @Override
    public boolean isUnique() {
        if (entity instanceof Player) return true;
        if (CitizensAPI.getNPCRegistry().isNPC(entity)) return true;
        return isSaved(this);
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return null;

        if (entity == null) {
            dB.echoDebug("dEntity has returned null.");
            return "null";
        }



        if (attribute.startsWith("custom_name")) {
            if (getLivingEntity().getCustomName() == null) return "null";
            return new Element(getLivingEntity().getCustomName()).getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("name")) {
            if (CitizensAPI.getNPCRegistry().isNPC(entity))
                return new Element(CitizensAPI.getNPCRegistry().getNPC(entity).getName())
                .getAttribute(attribute.fulfill(1));
            if (entity instanceof Player)
                return new Element(((Player) entity).getName())
                        .getAttribute(attribute.fulfill(1));
            return new Element(entity.getType().getName())
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("entity_type"))
            return new Element(entity.getType().getName())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("custom_id")) {
            if (CustomNBT.hasCustomNBT(getLivingEntity(), "denizen-script-id"))
                return new dScript(CustomNBT.getCustomNBT(getLivingEntity(), "denizen-script-id"))
                        .getAttribute(attribute.fulfill(1));
            else
                return new Element(getBukkitEntity().getType().name())
                        .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("location.cursor_on")) {
            int range = attribute.getIntContext(2);
            if (range < 1) range = 50;
            return new dLocation(getLivingEntity().getTargetBlock(null, range).getLocation())
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("location.standing_on"))
            return new dLocation(entity.getLocation().add(0, -1, 0))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("location"))
            return new dLocation(entity.getLocation())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("health.formatted")) {
            int maxHealth = getLivingEntity().getMaxHealth();
            if (attribute.hasContext(2))
                maxHealth = attribute.getIntContext(2);
            if ((float) getLivingEntity().getHealth() / maxHealth < .10)
                return new Element("dying").getAttribute(attribute.fulfill(2));
            else if ((float) getLivingEntity().getHealth() / maxHealth < .40)
                return new Element("seriously wounded").getAttribute(attribute.fulfill(2));
            else if ((float) getLivingEntity().getHealth() / maxHealth < .75)
                return new Element("injured").getAttribute(attribute.fulfill(2));
            else if ((float) getLivingEntity().getHealth() / maxHealth < 1)
                return new Element("scraped").getAttribute(attribute.fulfill(2));

            else return new Element("healthy").getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("health.percentage")) {
            int maxHealth = getLivingEntity().getMaxHealth();
            if (attribute.hasContext(2))
                maxHealth = attribute.getIntContext(2);
            return new Element(String.valueOf(((float) getLivingEntity().getHealth() / maxHealth) * 100))
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("health.max"))
            return new Element(String.valueOf(getLivingEntity().getMaxHealth()))
                    .getAttribute(attribute.fulfill(2));

        if (attribute.startsWith("health"))
            return new Element(String.valueOf(getLivingEntity().getHealth()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("is_inside_vehicle"))
            return new Element(String.valueOf(entity.isInsideVehicle()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("killer"))
            return new dPlayer(getLivingEntity().getKiller())
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("last_damage_cause"))
            return new Element(String.valueOf(entity.getLastDamageCause().getCause().toString()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("last_damage"))
            return new Element(String.valueOf(getLivingEntity().getLastDamage()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("time_lived"))
            return new Duration(entity.getTicksLived() / 20)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("can_pickup_items"))
            return new Element(String.valueOf(getLivingEntity().getCanPickupItems()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("entity_id"))
            return new Element(String.valueOf(entity.getEntityId()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("fall_distance"))
            return new Element(String.valueOf(entity.getFallDistance()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("uuid"))
            return new Element(String.valueOf(entity.getUniqueId().toString()))
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("has_effect")) {
            // Add later
        }

        if (attribute.startsWith("equipment")) {
            // Add later
        }

        if (attribute.startsWith("world")) {
            return new dWorld(entity.getWorld())
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("prefix"))
            return new Element(prefix)
                    .getAttribute(attribute.fulfill(1));

        if (attribute.startsWith("debug.log")) {
            dB.log(debug());
            return new Element(Boolean.TRUE.toString())
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug.no_color")) {
            return new Element(ChatColor.stripColor(debug()))
                    .getAttribute(attribute.fulfill(2));
        }

        if (attribute.startsWith("debug")) {
            return new Element(debug())
                    .getAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("type")) {
            return new Element(getType())
                    .getAttribute(attribute.fulfill(1));
        }

        return new Element(identify()).getAttribute(attribute.fulfill(0));
    }

}
