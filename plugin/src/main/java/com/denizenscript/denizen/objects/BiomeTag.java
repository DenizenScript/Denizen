package com.denizenscript.denizen.objects;

import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.RedirectionFlagTracker;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;

import java.util.List;

public class BiomeTag implements ObjectTag, Adjustable, FlaggableObject {

    // <--[ObjectType]
    // @name BiomeTag
    // @prefix b
    // @base ElementTag
    // @implements FlaggableObject
    // @ExampleTagBase biome[desert]
    // @ExampleValues desert
    // @ExampleForReturns
    // - adjust <player.location.to_ellipsoid[60,3,10].blocks> biome:%VALUE%
    // @ExampleForReturns
    // - adjust <player.location.chunk> set_all_biomes:%VALUE%
    // @format
    // The identity format for biomes is a world name, then a comma, then the biome key. For example: 'hub,desert', or 'space,minecraft:desert'.
    //
    // @description
    // A BiomeTag represents a world biome type. Vanilla biomes are globally available, however some biomes are world-specific when added by datapacks.
    //
    // A list of all vanilla biomes can be found at <@link url https://minecraft.fandom.com/wiki/Biome#Biome_IDs>.
    //
    // BiomeTags without a specific world will work as though they are in the server's default world.
    //
    // This object type is flaggable.
    // Flags on this object type will be stored in the server saves file, under special sub-key "__biomes"
    //
    // -->

    //////////////////
    //    OBJECT FETCHER
    ////////////////

    @Fetchable("b")
    public static BiomeTag valueOf(String string, TagContext context) {
        if (string.startsWith("b@")) {
            string = string.substring(2);
        }
        string = CoreUtilities.toLowerCase(string);
        int comma = string.indexOf(',');
        String worldName = null, biomeName = string;
        if (comma != -1) {
            worldName = string.substring(0, comma);
            biomeName = string.substring(comma + 1);
        }
        World world = Bukkit.getWorlds().get(0);
        if (worldName != null) {
            WorldTag worldTag = WorldTag.valueOf(worldName, context);
            if (worldTag == null || worldTag.getWorld() == null) {
                return null;
            }
            world = worldTag.getWorld();
        }
        BiomeNMS biome = NMSHandler.instance.getBiomeNMS(world, biomeName);
        if (biome == null) {
            return null;
        }
        return new BiomeTag(biome);
    }

    public static boolean matches(String arg) {
        if (arg.startsWith("b@")) {
            return true;
        }
        return valueOf(arg, CoreUtilities.noDebugContext) != null;
    }

    ///////////////
    //   Constructors
    /////////////

    public BiomeTag(Biome biome) {
        String key;
        if (biome.getKey().getNamespace().equals("minecraft")) {
            key = biome.getKey().getKey();
        }
        else {
            key = biome.getKey().toString();
        }
        this.biome = NMSHandler.instance.getBiomeNMS(Bukkit.getWorlds().get(0), key);
    }

    public BiomeTag(BiomeNMS biome) {
        this.biome = biome;
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
    public String identify() {
        return "b@" + biome.world.getName() + "," + biome.getName();
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
    public ObjectTag setPrefix(String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        return this;
    }

    @Override
    public AbstractFlagTracker getFlagTracker() {
        return new RedirectionFlagTracker(DenizenCore.serverFlagMap, "__biomes." + biome.getName().replace(".", "&dot"));
    }

    @Override
    public void reapplyTracker(AbstractFlagTracker tracker) {
        // Nothing to do.
    }

    public static void register() {

        AbstractFlagTracker.registerFlagHandlers(tagProcessor);

        // <--[tag]
        // @attribute <BiomeTag.downfall_type>
        // @returns ElementTag
        // @mechanism BiomeTag.downfall_type
        // @description
        // Returns this biome's downfall type for when a world has weather.
        // This can be RAIN, SNOW, or NONE.
        // @example
        // # In a plains biome, this fills with 'RAIN'.
        // - narrate "The downfall type in plains biomes is: <biome[plains].downfall_type>!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "downfall_type", (attribute, object) -> {
            return new ElementTag(object.biome.getDownfallType());
        });

        // <--[tag]
        // @attribute <BiomeTag.name>
        // @returns ElementTag
        // @description
        // Returns this biome's name.
        // @example
        // # In a plains biome, this fills with 'plains'.
        // - narrate "You are currently in a <biome[plains].name> biome!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "name", (attribute, object) -> {
            return new ElementTag(CoreUtilities.toLowerCase(object.biome.getName()));
        });

        // <--[tag]
        // @attribute <BiomeTag.humidity>
        // @returns ElementTag(Decimal)
        // @mechanism BiomeTag.humidity
        // @description
        // Returns the humidity of this biome.
        // @example
        // # In a plains biome, this fills with '0.4'.
        // - narrate "Humidity in a plains biome is <biome[plains].humidity>! So humid!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "humidity", (attribute, object) -> {
            return new ElementTag(object.biome.getHumidity());
        });

        // <--[tag]
        // @attribute <BiomeTag.temperature>
        // @returns ElementTag(Decimal)
        // @mechanism BiomeTag.temperature
        // @description
        // Returns the temperature of this biome.
        // @example
        // # In a plains biome, this fills with '0.8'.
        // - narrate "Stay warm! In a plains biome, the temperature is <biome[plains].temperature>!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "temperature", (attribute, object) -> {
            return new ElementTag(object.biome.getTemperature());
        });

        // <--[tag]
        // @attribute <BiomeTag.spawnable_entities[(<type>)]>
        // @returns ListTag
        // @description
        // Returns all entities that spawn naturally in this biome.
        // Optionally specify a type as: AMBIENT, CREATURES, MONSTERS, WATER, or ALL.
        // (By default, will be "ALL").
        // @example
        // # Narrates the types of entities of type MONSTERS that can spawn in the player's biome.
        // # For example, in a plains biome this could contain "SPIDER", "ZOMBIE", "CREEPER", etc.
        // - narrate <player.location.biome.spawnable_entities[MONSTERS].formatted>
        // -->
        tagProcessor.registerTag(ListTag.class, "spawnable_entities", (attribute, object) -> {
            List<EntityType> entityTypes;
            if (attribute.startsWith("ambient", 2)) {
                BukkitImplDeprecations.biomeSpawnableTag.warn(attribute.context);
                attribute.fulfill(1);
                entityTypes = object.biome.getAmbientEntities();
            }
            else if (attribute.startsWith("creatures", 2)) {
                BukkitImplDeprecations.biomeSpawnableTag.warn(attribute.context);
                attribute.fulfill(1);
                entityTypes = object.biome.getCreatureEntities();
            }
            else if (attribute.startsWith("monsters", 2)) {
                BukkitImplDeprecations.biomeSpawnableTag.warn(attribute.context);
                attribute.fulfill(1);
                entityTypes = object.biome.getMonsterEntities();
            }
            else if (attribute.startsWith("water", 2)) {
                BukkitImplDeprecations.biomeSpawnableTag.warn(attribute.context);
                attribute.fulfill(1);
                entityTypes = object.biome.getWaterEntities();
            }
            else {
                String type = attribute.hasParam() ? CoreUtilities.toLowerCase(attribute.getParam()) : "all";
                switch (type) {
                    case "ambient":
                        entityTypes = object.biome.getAmbientEntities();
                        break;
                    case "creatures":
                        entityTypes = object.biome.getCreatureEntities();
                        break;
                    case "monsters":
                        entityTypes = object.biome.getMonsterEntities();
                        break;
                    case "water":
                        entityTypes = object.biome.getWaterEntities();
                        break;
                    default:
                        entityTypes = object.biome.getAllEntities();
                        break;
                }
            }

            ListTag list = new ListTag();
            for (EntityType entityType : entityTypes) {
                list.add(entityType.name());
            }
            return list;
        });

        // <--[tag]
        // @attribute <BiomeTag.foliage_color>
        // @returns ColorTag
        // @mechanism BiomeTag.foliage_color
        // @description
        // Returns the approximate foliage color of this biome. Foliage includes leaves and vines.
        // The "swamp", "mangrove_swamp", "badlands", "wooded_badlands", and "eroded_badlands" biomes are the only biomes with hard-coded foliage colors.
        // Biomes with no set foliage color already will have their foliage colors based on temperature and humidity of the biome.
        // -->
        tagProcessor.registerTag(ColorTag.class, "foliage_color", (attribute, object) -> {
            return new ColorTag(ColorTag.fromRGB(object.biome.getFoliageColor()));
        });

        // <--[mechanism]
        // @object BiomeTag
        // @name foliage_color
        // @input ColorTag
        // @description
        // Sets the foliage color of this biome. Foliage includes leaves and vines.
        // Colors reset on server restart. For the change to take effect on the players' clients, they must quit and rejoin the server.
        // @tags
        // <BiomeTag.foliage_color>
        // @example
        // # Adjusts the foliage color of the plains biome permanently, using a server start event to keep it applied.
        // # Now the leaves and vines will be a nice salmon-pink!
        // on server start:
        // - adjust <biome[plains]> foliage_color:#F48D8D
        // -->
        tagProcessor.registerMechanism("foliage_color", false, ColorTag.class, (object, mechanism, color) -> {
            object.biome.setFoliageColor(color.asRGB());
        });
    }

    public static ObjectTagProcessor<BiomeTag> tagProcessor = new ObjectTagProcessor<>();

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {
        return tagProcessor.getObjectAttribute(this, attribute);
    }

    @Override
    public void applyProperty(Mechanism mechanism) {
        mechanism.echoError("Cannot apply properties to a biome!");
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object BiomeTag
        // @name humidity
        // @input ElementTag(Decimal)
        // @description
        // Sets the humidity for this biome server-wide.
        // If this is greater than 0.85, fire has less chance to spread in this biome.
        // Resets on server restart.
        // @tags
        // <BiomeTag.humidity>
        // @example
        // # Adjusts the humidity of the plains biome permanently, using a server start event to keep it applied.
        // on server start:
        // - adjust <biome[plains]> humidity:0.5
        // -->
        if (mechanism.matches("humidity") && mechanism.requireFloat()) {
            biome.setHumidity(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object BiomeTag
        // @name temperature
        // @input ElementTag(Decimal)
        // @description
        // Sets the temperature for this biome server-wide.
        // If this is less than 0.15, snow will form on the ground when weather occurs in the world and a layer of ice will form over water.
        // Resets on server restart.
        // @tags
        // <BiomeTag.temperature>
        // @example
        // # Adjusts the temperature of the plains biome permanently, using a server start event to keep it applied.
        // on server start:
        // - adjust <biome[plains]> temperature:0.5
        // -->
        if (mechanism.matches("temperature") && mechanism.requireFloat()) {
            biome.setTemperature(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object BiomeTag
        // @name downfall_type
        // @input ElementTag
        // @description
        // Sets the downfall-type for this biome server-wide.
        // This can be RAIN, SNOW, or NONE.
        // Resets on server restart.
        // @tags
        // <BiomeTag.temperature>
        // @example
        // # Adjusts the downfall type of the plains biome permanently, using a server start event to keep it applied.
        // on server start:
        // - adjust <biome[plains]> temperature:-0.2
        // - adjust <biome[plains]> downfall_type:SNOW
        // -->
        if (mechanism.matches("downfall_type") && mechanism.requireEnum(BiomeNMS.DownfallType.class)) {
            biome.setPrecipitation(BiomeNMS.DownfallType.valueOf(mechanism.getValue().asString().toUpperCase()));
        }

        tagProcessor.processMechanism(this, mechanism);
    }
}
