package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.List;

public class dBiome implements dObject, Adjustable {

    // <--[language]
    // @name dBiome
    // @group Object System
    // @description
    // A dBiome represents a world biome type.
    //
    // A list of all valid Bukkit biomes can found be at
    // <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Biome.html>
    //
    // For format info, see <@link language b@>
    //
    // -->

    // <--[language]
    // @name b@
    // @group Object Fetcher System
    // @description
    // b@ refers to the 'object identifier' of a dBiome. The 'b@' is notation for Denizen's Object
    // Fetcher. The constructor for a dBiome is the name of a valid biome (in Bukkit).
    // For example, 'b@desert'.
    //
    // For general info, see <@link language dBiome>
    //
    // -->

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    public static dBiome valueOf(String string) {
        return valueOf(string, null);
    }

    /**
     * Gets a Biome Object from a string form.
     *
     * @param string the string
     */
    @Fetchable("b")
    public static dBiome valueOf(String string, TagContext context) {

        if (string.startsWith("b@")) {
            string = string.substring(2);
        }

        for (Biome biome : Biome.values()) {
            if (biome.name().equalsIgnoreCase(string)) {
                return new dBiome(biome);
            }
        }

        return null;
    }

    /**
     * Determines whether a string is a valid biome.
     *
     * @param arg the string
     * @return true if matched, otherwise false
     */
    public static boolean matches(String arg) {

        if (arg.startsWith("b@")) {
            arg = arg.substring(2);
        }

        for (Biome b : Biome.values()) {
            if (b.name().equalsIgnoreCase(arg)) {
                return true;
            }
        }

        return false;
    }


    ///////////////
    //   Constructors
    /////////////

    public dBiome(Biome biome) {
        this.biome = NMSHandler.getInstance().getBiomeNMS(biome);
    }


    /////////////////////
    //   INSTANCE FIELDS/METHODS
    /////////////////

    private BiomeNMS biome;

    public BiomeNMS getBiome() {
        return biome;
    }

    String prefix = "biome";

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public boolean isUnique() {
        return false;
    }

    @Override
    public String getObjectType() {
        return "Biome";
    }

    @Override
    public String identify() {
        return "b@" + CoreUtilities.toLowerCase(biome.getName());
    }

    @Override
    public String identifySimple() {
        return identify();
    }

    @Override
    public String toString() {
        return identify();
    }

    @Override
    public dObject setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }


    public static void registerTags() {

        // <--[tag]
        // @attribute <b@biome.downfall_type>
        // @returns Element
        // @description
        // Returns this biome's downfall type for when a world has weather.
        // This can be RAIN, SNOW, or NONE.
        // -->
        registerTag("downfall_type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(CoreUtilities.toLowerCase(((dBiome) object).biome.getDownfallType().name()))
                        .getAttribute(attribute.fulfill(1));
            }
        });

        // <--[tag]
        // @attribute <b@biome.humidity>
        // @returns Element(Decimal)
        // @description
        // Returns the humidity of this biome.
        // -->
        registerTag("humidity", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dBiome) object).biome.getHumidity())
                        .getAttribute(attribute.fulfill(1));
            }
        });
        // <--[tag]
        // @attribute <b@biome.temperature>
        // @returns Element(Decimal)
        // @description
        // Returns the temperature of this biome.
        // -->
        registerTag("temperature", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element(((dBiome) object).biome.getTemperature())
                        .getAttribute(attribute.fulfill(1));
            }
        });
        // <--[tag]
        // @attribute <b@biome.spawnable_entities>
        // @returns dList(dEntity)
        // @description
        // Returns all entities that spawn naturally in this biome.
        // -->
        registerTag("spawnable_entities", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                attribute = attribute.fulfill(1);
                BiomeNMS biome = ((dBiome) object).biome;

                List<EntityType> entityTypes;
                boolean hasAttribute = true;

                // <--[tag]
                // @attribute <b@biome.spawnable_entities.ambient>
                // @returns dList(dEntity)
                // @description
                // Returns the entities that spawn naturally in ambient locations.
                // Default examples: BAT
                // -->
                if (attribute.startsWith("ambient")) {
                    entityTypes = biome.getAmbientEntities();
                }

                // <--[tag]
                // @attribute <b@biome.spawnable_entities.creatures>
                // @returns dList(dEntity)
                // @description
                // Returns the entities that spawn naturally in creature locations.
                // Default examples: PIG, COW, CHICKEN...
                // -->
                else if (attribute.startsWith("creatures")) {
                    entityTypes = biome.getCreatureEntities();
                }

                // <--[tag]
                // @attribute <b@biome.spawnable_entities.monsters>
                // @returns dList(dEntity)
                // @description
                // Returns the entities that spawn naturally in monster locations.
                // Default examples: CREEPER, ZOMBIE, SKELETON...
                // -->
                else if (attribute.startsWith("monsters")) {
                    entityTypes = biome.getMonsterEntities();
                }

                // <--[tag]
                // @attribute <b@biome.spawnable_entities.water>
                // @returns dList(dEntity)
                // @description
                // Returns the entities that spawn naturally in underwater locations.
                // Default examples: SQUID
                // -->
                else if (attribute.startsWith("water")) {
                    entityTypes = biome.getWaterEntities();
                }
                else {
                    entityTypes = biome.getAllEntities();
                    hasAttribute = false;
                }

                dList list = new dList();
                for (EntityType entityType : entityTypes) {
                    list.add(entityType.name());
                }
                return list.getAttribute(hasAttribute ? attribute.fulfill(1) : attribute);
            }
        });

        // <--[tag]
        // @attribute <b@biome.type>
        // @returns Element
        // @description
        // Always returns 'Biome' for dBiome objects. All objects fetchable by the Object Fetcher will return the
        // type of object that is fulfilling this attribute.
        // -->
        registerTag("type", new TagRunnable() {
            @Override
            public String run(Attribute attribute, dObject object) {
                return new Element("Biome").getAttribute(attribute.fulfill(1));
            }
        });
    }

    public static HashMap<String, TagRunnable> registeredTags = new HashMap<>();

    public static void registerTag(String name, TagRunnable runnable) {
        if (runnable.name == null) {
            runnable.name = name;
        }
        registeredTags.put(name, runnable);
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // TODO: Scrap getAttribute, make this functionality a core system
        String attrLow = CoreUtilities.toLowerCase(attribute.getAttributeWithoutContext(1));
        TagRunnable tr = registeredTags.get(attrLow);
        if (tr != null) {
            if (!tr.name.equals(attrLow)) {
                com.denizenscript.denizencore.utilities.debugging.Debug.echoError(attribute.getScriptEntry() != null ? attribute.getScriptEntry().getResidingQueue() : null,
                        "Using deprecated form of tag '" + tr.name + "': '" + attrLow + "'.");
            }
            return tr.run(attribute, this);
        }

        return new Element(identify()).getAttribute(attribute);
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        Debug.echoError("Cannot apply properties to a biome!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dBiome
        // @name humidity
        // @input Element(Decimal)
        // @description
        // Sets the humidity for this biome server-wide.
        // If this is greater than 0.85, fire has less chance
        // to spread in this biome.
        // @tags
        // <b@biome.humidity>
        // -->
        if (mechanism.matches("humidity") && mechanism.requireFloat()) {
            biome.setHumidity(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object dBiome
        // @name temperature
        // @input Element(Decimal)
        // @description
        // Sets the temperature for this biome server-wide.
        // If this is less than 1.5, snow will form on the ground
        // when weather occurs in the world and a layer of ice
        // will form over water.
        // @tags
        // <b@biome.temperature>
        // -->
        if (mechanism.matches("temperature") && mechanism.requireFloat()) {
            biome.setTemperature(mechanism.getValue().asFloat());
        }

    }
}
