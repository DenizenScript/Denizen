package com.denizenscript.denizen.objects;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.abstracts.BiomeNMS;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.flags.AbstractFlagTracker;
import com.denizenscript.denizencore.flags.FlaggableObject;
import com.denizenscript.denizencore.flags.RedirectionFlagTracker;
import com.denizenscript.denizencore.objects.Adjustable;
import com.denizenscript.denizencore.objects.Fetchable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
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
    // A list of all vanilla biomes can be found at <@link url https://minecraft.wiki/w/Biome#Biome_IDs>.
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
        // @deprecated Minecraft changed the way biome downfall works, use <@link tag BiomeTag.downfall_at> on 1.19+.
        // @description
        // Deprecated in favor of <@link tag BiomeTag.downfall_at> on 1.19+, as downfall is block-specific now.
        // Returns this biome's downfall type for when a world has weather.
        // This can be RAIN, SNOW, or NONE.
        // @example
        // # In a plains biome, this fills with 'RAIN'.
        // - narrate "The downfall type in plains biomes is: <biome[plains].downfall_type>!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "downfall_type", (attribute, object) -> {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
                BukkitImplDeprecations.biomeGlobalDownfallType.warn(attribute.context);
            }
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
        // @attribute <BiomeTag.base_temperature>
        // @returns ElementTag(Decimal)
        // @mechanism BiomeTag.base_temperature
        // @description
        // Returns the base temperature of this biome, which is used for per-location temperature calculations (see <@link tag BiomeTag.temperature_at>).
        // @example
        // # In a plains biome, this fills with '0.8'.
        // - narrate "Stay warm! In a plains biome, the base temperature is <biome[plains].base_temperature>!"
        // -->
        tagProcessor.registerTag(ElementTag.class, "base_temperature", (attribute, object) -> {
            return new ElementTag(object.biome.getBaseTemperature());
        }, "temperature");

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
                entityTypes = switch (type) {
                    case "ambient" -> object.biome.getAmbientEntities();
                    case "creatures" -> object.biome.getCreatureEntities();
                    case "monsters" -> object.biome.getMonsterEntities();
                    case "water" -> object.biome.getWaterEntities();
                    default -> object.biome.getAllEntities();
                };
            }
            return new ListTag(entityTypes, ElementTag::new);
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
            return ColorTag.fromRGB(object.biome.getFoliageColor());
        });

        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {

            // <--[tag]
            // @attribute <BiomeTag.temperature_at[<location>]>
            // @returns ElementTag(Decimal)
            // @description
            // Returns the temperature of a specific location in this biome.
            // If this is less than 0.15, snow will form on the ground when weather occurs in the world and a layer of ice will form over water.
            // Generally <@link tag LocationTag.temperature> should be preferred, other than some special cases.
            // @example
            // # Gives the player water if they are standing in a warm location.
            // - if <player.location.biome.temperature_at[<player.location]> > 0.5:
            //   - give water_bucket
            // -->
            tagProcessor.registerTag(ElementTag.class, LocationTag.class, "temperature_at", (attribute, object, param) -> {
                return new ElementTag(object.biome.getTemperatureAt(param));
            });

            // <--[tag]
            // @attribute <BiomeTag.downfall_at[<location>]>
            // @returns ElementTag
            // @description
            // Returns this biome's downfall type at a location (for when a world has weather).
            // This can be RAIN, SNOW, or NONE.
            // Generally <@link tag LocationTag.downfall_type> should be preferred, other than some special cases.
            // @example
            // # Tells the linked player what the downfall type at their location is.
            // - narrate "The downfall type at your location is: <player.location.biome.downfall_at[<player.location>]>!"
            // -->
            tagProcessor.registerTag(ElementTag.class, LocationTag.class, "downfall_at", (attribute, object, param) -> {
                return new ElementTag(object.biome.getDownfallTypeAt(param));
            });

            // <--[tag]
            // @attribute <BiomeTag.has_downfall>
            // @returns ElementTag(Boolean)
            // @mechanism BiomeTag.has_downfall
            // @description
            // Returns whether the biome has downfall (rain/snow).
            // @example
            // # Tells the linked player whether there's a possibility of rain.
            // - if <player.location.biome.has_downfall>:
            //   - narrate "It might rain or snow!"
            // - else:
            //   - narrate "It will be dry."
            // -->
            tagProcessor.registerTag(ElementTag.class, "has_downfall", (attribute, object) -> {
                return new ElementTag(object.biome.hasDownfall());
            });

            // <--[tag]
            // @attribute <BiomeTag.fog_color>
            // @returns ColorTag
            // @mechanism BiomeTag.fog_color
            // @description
            // Returns the biome's fog color, which is visible when outside water (see also <@link tag BiomeTag.water_fog_color>).
            // @example
            // # Sends the player a message in their current biome's fog color.
            // - narrate "You are currently seeing fog that looks like <&color[<player.location.biome.fog_color>]>this!"
            // -->
            tagProcessor.registerTag(ColorTag.class, "fog_color", (attribute, object) -> {
                return ColorTag.fromRGB(object.biome.getFogColor());
            });

            // <--[tag]
            // @attribute <BiomeTag.water_fog_color>
            // @returns ColorTag
            // @mechanism BiomeTag.water_fog_color
            // @description
            // Returns the biome's water fog color, which is visible when underwater (see also <@link tag BiomeTag.fog_color>).
            // @example
            // # Sends the player a message in their current biome's water fog color.
            // - narrate "If you are underwater, everything looks like <&color[<player.location.biome.water_fog_color>]>this!"
            // -->
            tagProcessor.registerTag(ColorTag.class, "water_fog_color", (attribute, object) -> {
                return ColorTag.fromRGB(object.biome.getWaterFogColor());
            });

            // <--[mechanism]
            // @object BiomeTag
            // @name fog_color
            // @input ColorTag
            // @description
            // Sets the biome's fog color, which is visible when outside water (see also <@link mechanism BiomeTag.water_fog_color>).
            // @tags
            // <BiomeTag.fog_color>
            // @example
            // # Makes the plains biome's fog color red permanently, using a server start event to keep it applied.
            // on server start:
            // - adjust <biome[plains]> fog_color:red
            // -->
            tagProcessor.registerMechanism("fog_color", false, ColorTag.class, (object, mechanism, input) -> {
                object.biome.setFogColor(input.asRGB());
            });

            // <--[mechanism]
            // @object BiomeTag
            // @name water_fog_color
            // @input ColorTag
            // @description
            // Sets the biome's water fog color, which is visible when underwater (see also <@link mechanism BiomeTag.fog_color>).
            // @tags
            // <BiomeTag.water_fog_color>
            // @example
            // # Makes the plains biome's water fog color fuchsia permanently, using a server start event to keep it applied.
            // on server start:
            // - adjust <biome[plains]> water_fog_color:fuchsia
            // -->
            tagProcessor.registerMechanism("water_fog_color", false, ColorTag.class, (object, mechanism, input) -> {
                object.biome.setWaterFogColor(input.asRGB());
            });

            // <--[mechanism]
            // @object BiomeTag
            // @name has_downfall
            // @input ElementTag(Boolean)
            // @description
            // Sets whether the biome has downfall (rain/snow).
            // @tags
            // <BiomeTag.has_downfall>
            // @example
            // # Disables downfall for the plains biome permanently, using a server start event to keep it applied.
            // on server start:
            // - adjust <biome[plains]> has_downfall:false
            // -->
            tagProcessor.registerMechanism("has_downfall", false, ElementTag.class, (object, mechanism, input) -> {
                if (mechanism.requireBoolean()) {
                    object.biome.setHasDownfall(input.asBoolean());
                }
            });
        }

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
        tagProcessor.registerMechanism("humidity", false, ElementTag.class, (object, mechanism, input) -> {
            if (mechanism.requireFloat()) {
                object.biome.setHumidity(input.asFloat());
            }
        });

        // <--[mechanism]
        // @object BiomeTag
        // @name base_temperature
        // @input ElementTag(Decimal)
        // @description
        // Sets the base temperature for this biome server-wide.
        // This is used as a base for temperature calculations, but the end temperature is calculated per-location (see <@link tag BiomeTag.temperature_at>).
        // Resets on server restart.
        // @tags
        // <BiomeTag.base_temperature>
        // @example
        // # Adjusts the temperature of the plains biome permanently, using a server start event to keep it applied.
        // on server start:
        // - adjust <biome[plains]> temperature:0.5
        // -->
        tagProcessor.registerMechanism("base_temperature", false, ElementTag.class, (object, mechanism, input) -> {
            if (mechanism.requireFloat()) {
                object.biome.setBaseTemperature(input.asFloat());
            }
        }, "temperature");

        // <--[mechanism]
        // @object BiomeTag
        // @name downfall_type
        // @input ElementTag
        // @deprecated This functionality was removed from Minecraft as of 1.19.
        // @description
        // Deprecated on 1.19+, as Minecraft removed the ability to set this value.
        // Sets the downfall-type for this biome server-wide.
        // This can be RAIN, SNOW, or NONE.
        // Resets on server restart.
        // @tags
        // <BiomeTag.base_temperature>
        // @example
        // # Adjusts the downfall type of the plains biome permanently, using a server start event to keep it applied.
        // on server start:
        // - adjust <biome[plains]> temperature:-0.2
        // - adjust <biome[plains]> downfall_type:SNOW
        // -->
        tagProcessor.registerMechanism("downfall_type", false, ElementTag.class, (object, mechanism, input) -> {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
                BukkitImplDeprecations.biomeSettingDownfallType.warn(mechanism.context);
                return;
            }
            if (mechanism.requireEnum(BiomeNMS.DownfallType.class)) {
                object.biome.setPrecipitation(input.asEnum(BiomeNMS.DownfallType.class));
            }
        });
    }

    public static final ObjectTagProcessor<BiomeTag> tagProcessor = new ObjectTagProcessor<>();

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
        tagProcessor.processMechanism(this, mechanism);
    }
}
