package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.Particle;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.Effect;
import org.bukkit.Vibration;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class PlayEffectCommand extends AbstractCommand {

    public PlayEffectCommand() {
        setName("playeffect");
        setSyntax("playeffect [effect:<name>] [at:<location>|...] (data:<#.#>) (special_data:<data>) (visibility:<#.#>) (quantity:<#>) (offset:<#.#>,<#.#>,<#.#>) (targets:<player>|...) (velocity:<vector>)");
        setRequiredArguments(2, 8);
        isProcedural = false;
    }

    // <--[language]
    // @name Particle Effects
    // @group Useful Lists
    // @description
    // All of the effects listed here can be used by <@link command PlayEffect> to display visual effects or play sounds
    //
    // Effects:
    // - Everything on <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html>
    // - Everything on <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Effect.html>
    // - RANDOM (chooses a random visual effect from the Particle list)
    // -->

    // <--[command]
    // @Name PlayEffect
    // @Syntax playeffect [effect:<name>] [at:<location>|...] (data:<#.#>) (special_data:<data>) (visibility:<#.#>) (quantity:<#>) (offset:<#.#>,<#.#>,<#.#>) (targets:<player>|...) (velocity:<vector>)
    // @Required 2
    // @Maximum 8
    // @Short Plays a visible or audible effect at the location.
    // @Synonyms Particle
    // @Group world
    //
    // @Description
    // Allows the playing of particle effects anywhere without the need of the source it comes from originally.
    // The particles you may use, can come from sources such as a potion effect or a portal/Enderman with their particles respectively.
    // Some particles have different data which may include different behavior depending on the data. Default data is 0
    // Specifying a visibility value changes the sight radius of the effect. For example if visibility is 15; Targeted players won't see it unless they are 15 blocks or closer.
    // You can add a quantity value that allow multiple of the same effect played at the same time. If an offset is set, each particle will be played at a different location in the offset area.
    // Everyone will see the particle effects unless a target has been specified.
    // See <@link language Particle Effects> for a list of valid effect names.
    //
    // Version change note: The original PlayEffect command raised all location inputs 1 block-height upward to avoid effects playing underground when played at eg a player's location.
    // This was found to cause too much confusion, so it is no longer on by default. However, it will still happen for older commands.
    // The distinction is in whether you include the (now expected to use) "at:" prefix on your location argument.
    // If you do not have this prefix, the system will assume your command is older, and will apply the 1-block height offset.
    //
    // Some particles will require input to the "special_data" argument. The data input is unique per particle.
    // - For REDSTONE particles, the input is of format: <size>|<color>, for example: "1.2|red". Color input is any valid ColorTag object.
    // - For DUST_COLOR_TRANSITION particles, the input is of format <size>|<from_color>|<to_color>, for example "1.2|red|blue". Color input is any valid ColorTag object.
    // - For BLOCK_MARKER, FALLING_DUST, BLOCK_CRACK, or BLOCK_DUST particles, the input is any valid MaterialTag, eg "stone".
    // - For VIBRATION, the input is <duration>|<origin>|<destination> where origin is a LocationTag and destination is either LocationTag or EntityTag, for example "5s|<context.location>|<player>"
    // - For ITEM_CRACK particles, the input is any valid ItemTag, eg "stick".
    //
    // Optionally specify a velocity vector for standard particles to move. Note that this ignores the 'data' input if used.
    //
    // @Tags
    // <server.effect_types>
    // <server.particle_types>
    //
    // @Usage
    // Use to create a fake explosion.
    // - playeffect effect:EXPLOSION_HUGE at:<player.location> visibility:500 quantity:10 offset:2.0
    //
    // @Usage
    // Use to play a cloud effect.
    // - playeffect effect:CLOUD at:<player.location.add[0,5,0]> quantity:20 data:1 offset:0.0
    //
    // @Usage
    // Use to play some effects at spawn.
    // - playeffect effect:FIREWORKS_SPARK at:<world[world].spawn_location> visibility:100 quantity:375 data:0 offset:50.0
    // -->

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addWithPrefix("effect:", NMSHandler.particleHelper.particles.keySet());
        tab.addWithPrefix("effect:", Effect.values());
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentList(LocationTag.class)
                    && (arg.matchesPrefix("at") || !arg.hasPrefix())) {
                if (arg.matchesPrefix("at")) {
                    scriptEntry.addObject("no_offset", new ElementTag(true));
                }
                scriptEntry.addObject("location", arg.asType(ListTag.class).filter(LocationTag.class, scriptEntry));
                continue;
            }
            else if (!scriptEntry.hasObject("effect") &&
                    !scriptEntry.hasObject("particleeffect") &&
                    !scriptEntry.hasObject("iconcrack")) {
                if (NMSHandler.particleHelper.hasParticle(arg.getValue())) {
                    scriptEntry.addObject("particleeffect", NMSHandler.particleHelper.getParticle(arg.getValue()));
                    continue;
                }
                else if (arg.matches("barrier") && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_18)) {
                    scriptEntry.addObject("particleeffect", NMSHandler.particleHelper.getParticle("block_marker"));
                    scriptEntry.addObject("special_data", new ElementTag("barrier"));
                    continue;
                }
                else if (arg.matches("random")) {
                    // Get another effect if "RANDOM" is used
                    List<Particle> visible = NMSHandler.particleHelper.getVisibleParticles();
                    scriptEntry.addObject("particleeffect", visible.get(CoreUtilities.getRandom().nextInt(visible.size())));
                    continue;
                }
                else if (arg.startsWith("iconcrack_")) {
                    BukkitImplDeprecations.oldPlayEffectSpecials.warn(scriptEntry);
                    // Allow iconcrack_[item] for item break effects (ex: iconcrack_stone)
                    String shrunk = arg.getValue().substring("iconcrack_".length());
                    ItemTag item = ItemTag.valueOf(shrunk, scriptEntry.context);
                    if (item != null) {
                        scriptEntry.addObject("iconcrack", item);
                    }
                    else {
                        Debug.echoError("Invalid iconcrack_[item]. Must be a valid ItemTag!");
                    }
                    continue;
                }
                else if (arg.matchesEnum(Effect.class)) {
                    scriptEntry.addObject("effect", Effect.valueOf(arg.getValue().toUpperCase()));
                    continue;
                }
            }
            if (!scriptEntry.hasObject("radius")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("visibility", "v", "radius", "r")) {
                scriptEntry.addObject("radius", arg.asElement());
            }
            else if (!scriptEntry.hasObject("data")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("data", "d")) {
                scriptEntry.addObject("data", arg.asElement());
            }
            else if (!scriptEntry.hasObject("special_data")
                    && arg.matchesPrefix("special_data")) {
                scriptEntry.addObject("special_data", arg.asElement());
            }
            else if (!scriptEntry.hasObject("quantity")
                    && arg.matchesInteger()
                    && arg.matchesPrefix("qty", "q", "quantity")) {
                if (arg.matchesPrefix("q", "qty")) {
                    BukkitImplDeprecations.qtyTags.warn(scriptEntry);
                }
                scriptEntry.addObject("quantity", arg.asElement());
            }
            else if (!scriptEntry.hasObject("offset")
                    && arg.matchesFloat()
                    && arg.matchesPrefix("offset", "o")) {
                double offset = arg.asElement().asDouble();
                scriptEntry.addObject("offset", new LocationTag(null, offset, offset, offset));
            }
            else if (!scriptEntry.hasObject("offset")
                    && arg.matchesArgumentType(LocationTag.class)
                    && arg.matchesPrefix("offset", "o")) {
                scriptEntry.addObject("offset", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("velocity")
                    && arg.matchesArgumentType(LocationTag.class)
                    && arg.matchesPrefix("velocity")) {
                scriptEntry.addObject("velocity", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("targets")
                    && arg.matchesArgumentList(PlayerTag.class)
                    && arg.matchesPrefix("targets", "target", "t")) {
                scriptEntry.addObject("targets", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("data", new ElementTag(0));
        scriptEntry.defaultObject("radius", new ElementTag(15));
        scriptEntry.defaultObject("quantity", new ElementTag(1));
        scriptEntry.defaultObject("offset", new LocationTag(null, 0.5, 0.5, 0.5));
        if (!scriptEntry.hasObject("effect") &&
                !scriptEntry.hasObject("particleeffect") &&
                !scriptEntry.hasObject("iconcrack")) {
            throw new InvalidArgumentsException("Missing effect argument!");
        }
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        List<LocationTag> locations = (List<LocationTag>) scriptEntry.getObject("location");
        List<PlayerTag> targets = (List<PlayerTag>) scriptEntry.getObject("targets");
        Effect effect = (Effect) scriptEntry.getObject("effect");
        Particle particleEffect = (Particle) scriptEntry.getObject("particleeffect");
        ItemTag iconcrack = scriptEntry.getObjectTag("iconcrack");
        ElementTag radius = scriptEntry.getElement("radius");
        ElementTag data = scriptEntry.getElement("data");
        ElementTag quantity = scriptEntry.getElement("quantity");
        ElementTag no_offset = scriptEntry.getElement("no_offset");
        boolean should_offset = no_offset == null || !no_offset.asBoolean();
        LocationTag offset = scriptEntry.getObjectTag("offset");
        ElementTag special_data = scriptEntry.getElement("special_data");
        LocationTag velocity = scriptEntry.getObjectTag("velocity");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (effect != null ? db("effect", effect.name()) : particleEffect != null ? db("special effect", particleEffect.getName()) : iconcrack),
                    db("locations", locations), db("targets", targets), radius, data, quantity, offset, special_data, velocity, (should_offset ? db("note", "Location will be offset 1 block-height upward (see documentation)") : ""));
        }
        for (LocationTag location : locations) {
            if (should_offset) {
                // Slightly increase the location's Y so effects don't seem to come out of the ground
                location = new LocationTag(location.clone().add(0, 1, 0));
            }
            // Play the Bukkit effect the number of times specified
            if (effect != null) {
                for (int n = 0; n < quantity.asInt(); n++) {
                    if (targets != null) {
                        for (PlayerTag player : targets) {
                            if (player.isValid() && player.isOnline()) {
                                player.getPlayerEntity().playEffect(location, effect, data.asInt());
                            }
                        }
                    }
                    else {
                        location.getWorld().playEffect(location, effect, data.asInt(), radius.asInt());
                    }
                }
            }
            // Play a ParticleEffect
            else if (particleEffect != null) {
                List<Player> players = new ArrayList<>();
                if (targets == null) {
                    float rad = radius.asFloat();
                    for (Player player : location.getWorld().getPlayers()) {
                        if (player.getLocation().distanceSquared(location) < rad * rad) {
                            players.add(player);
                        }
                    }
                }
                else {
                    for (PlayerTag player : targets) {
                        if (player.isValid() && player.isOnline()) {
                            players.add(player.getPlayerEntity());
                        }
                    }
                }
                Class clazz = particleEffect.neededData();
                Object dataObject = null;
                if (clazz != null) {
                    if (special_data == null) {
                        Debug.echoError("Missing required special data for particle: " + particleEffect.getName());
                        return;
                    }
                    else if (clazz == org.bukkit.Particle.DustOptions.class) {
                        ListTag dataList = ListTag.valueOf(special_data.asString(), scriptEntry.getContext());
                        if (dataList.size() != 2) {
                            Debug.echoError("DustOptions special_data must have 2 list entries for particle: " + particleEffect.getName());
                            return;
                        }
                        else {
                            float size = Float.parseFloat(dataList.get(0));
                            ColorTag color = ColorTag.valueOf(dataList.get(1), scriptEntry.context);
                            dataObject = new org.bukkit.Particle.DustOptions(BukkitColorExtensions.getColor(color), size);
                        }
                    }
                    else if (clazz == BlockData.class) {
                        MaterialTag blockMaterial = MaterialTag.valueOf(special_data.asString(), scriptEntry.getContext());
                        dataObject = blockMaterial.getModernData();
                    }
                    else if (clazz == ItemStack.class) {
                        ItemTag itemType = ItemTag.valueOf(special_data.asString(), scriptEntry.getContext());
                        dataObject = itemType.getItemStack();
                    }
                    else if (clazz == org.bukkit.Particle.DustTransition.class) {
                        ListTag dataList = ListTag.valueOf(special_data.asString(), scriptEntry.getContext());
                        if (dataList.size() != 3) {
                            Debug.echoError("DustTransition special_data must have 3 list entries for particle: " + particleEffect.getName());
                            return;
                        }
                        else {
                            float size = Float.parseFloat(dataList.get(0));
                            ColorTag fromColor = ColorTag.valueOf(dataList.get(1), scriptEntry.context);
                            ColorTag toColor = ColorTag.valueOf(dataList.get(2), scriptEntry.context);
                            dataObject = new org.bukkit.Particle.DustTransition(BukkitColorExtensions.getColor(fromColor), BukkitColorExtensions.getColor(toColor), size);
                        }
                    }
                    else if (clazz == Vibration.class) {
                        ListTag dataList = ListTag.valueOf(special_data.asString(), scriptEntry.getContext());
                        if (dataList.size() != 3) {
                            Debug.echoError("Vibration special_data must have 3 list entries for particle: " + particleEffect.getName());
                            return;
                        }
                        else {
                            DurationTag duration = dataList.getObject(0).asType(DurationTag.class, scriptEntry.context);
                            LocationTag origin = dataList.getObject(1).asType(LocationTag.class, scriptEntry.context);
                            ObjectTag destination = dataList.getObject(2);
                            Vibration.Destination destObj;
                            if (destination.shouldBeType(EntityTag.class)) {
                                destObj = new Vibration.Destination.EntityDestination(destination.asType(EntityTag.class, scriptEntry.context).getBukkitEntity());
                            }
                            else {
                                destObj = new Vibration.Destination.BlockDestination(destination.asType(LocationTag.class, scriptEntry.context));
                            }
                            dataObject = new Vibration(origin, destObj, duration.getTicksAsInt());
                        }
                    }
                    else {
                        Debug.echoError("Unknown particle data type: " + clazz.getCanonicalName() + " for particle: " + particleEffect.getName());
                        return;
                    }
                }
                else if (special_data != null) {
                    Debug.echoError("Particles of type '" + particleEffect.getName() + "' cannot take special_data as input.");
                    return;
                }
                Random random = CoreUtilities.getRandom();
                int quantityInt = quantity.asInt();
                for (Player player : players) {
                    if (velocity == null) {
                        particleEffect.playFor(player, location, quantityInt, offset.toVector(), data.asDouble(), dataObject);
                    }
                    else {
                        Vector velocityVector = velocity.toVector();
                        for (int i = 0; i < quantityInt; i++) {
                            LocationTag singleLocation = location.clone().add((random.nextDouble() - 0.5) * offset.getX(),
                                    (random.nextDouble() - 0.5) * offset.getY(),
                                    (random.nextDouble() - 0.5) * offset.getZ());
                            particleEffect.playFor(player, singleLocation, 0, velocityVector, 1f, dataObject);
                        }
                    }
                }
            }
            // Play an iconcrack (item break) effect
            else {
                List<Player> players = new ArrayList<>();
                if (targets == null) {
                    float rad = radius.asFloat();
                    for (Player player : location.getWorld().getPlayers()) {
                        if (player.getLocation().distanceSquared(location) < rad * rad) {
                            players.add(player);
                        }
                    }
                }
                else {
                    for (PlayerTag player : targets) {
                        if (player.isValid() && player.isOnline()) {
                            players.add(player.getPlayerEntity());
                        }
                    }
                }
                if (iconcrack != null) {
                    ItemStack itemStack = iconcrack.getItemStack();
                    Particle particle = NMSHandler.particleHelper.getParticle("ITEM_CRACK");
                    for (Player player : players) {
                        particle.playFor(player, location, quantity.asInt(), offset.toVector(), data.asFloat(), itemStack);
                    }
                }
            }
        }
    }
}
