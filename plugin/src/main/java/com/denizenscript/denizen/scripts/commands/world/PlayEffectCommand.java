package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.ParticleHelper;
import com.denizenscript.denizen.nms.interfaces.Particle;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.Effect;
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
    // - For FALLING_DUST, BLOCK_CRACK, or BLOCK_DUST particles, the input is any valid MaterialTag, eg "stone".
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
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        ParticleHelper particleHelper = NMSHandler.getParticleHelper();
        for (Argument arg : scriptEntry.getProcessedArgs()) {
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
                if (particleHelper.hasParticle(arg.getValue())) {
                    scriptEntry.addObject("particleeffect", particleHelper.getParticle(arg.getValue()));
                    continue;
                }
                else if (arg.matches("random")) {
                    // Get another effect if "RANDOM" is used
                    List<Particle> visible = particleHelper.getVisibleParticles();
                    scriptEntry.addObject("particleeffect", visible.get(CoreUtilities.getRandom().nextInt(visible.size())));
                    continue;
                }
                else if (arg.startsWith("iconcrack_")) {
                    Deprecations.oldPlayEffectSpecials.warn(scriptEntry);
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
                else if (arg.matchesEnum(Effect.values())) {
                    scriptEntry.addObject("effect", Effect.valueOf(arg.getValue().toUpperCase()));
                    continue;
                }
                else if (NMSHandler.getParticleHelper().effectRemap.containsKey(arg.getValue().toUpperCase())) {
                    scriptEntry.addObject("effect", NMSHandler.getParticleHelper().effectRemap.get(arg.getValue().toUpperCase()));
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
            else if (!scriptEntry.hasObject("qty")
                    && arg.matchesInteger()
                    && arg.matchesPrefix("qty", "q", "quantity")) {
                scriptEntry.addObject("qty", arg.asElement());
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
        scriptEntry.defaultObject("location",
                Utilities.entryHasNPC(scriptEntry) && Utilities.getEntryNPC(scriptEntry).isSpawned() ? Arrays.asList(Utilities.getEntryNPC(scriptEntry).getLocation()) : null,
                Utilities.entryHasPlayer(scriptEntry) && Utilities.getEntryPlayer(scriptEntry).isOnline() ? Arrays.asList(Utilities.getEntryPlayer(scriptEntry).getLocation()) : null);
        scriptEntry.defaultObject("data", new ElementTag(0));
        scriptEntry.defaultObject("radius", new ElementTag(15));
        scriptEntry.defaultObject("qty", new ElementTag(1));
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

        // Extract objects from ScriptEntry
        List<LocationTag> locations = (List<LocationTag>) scriptEntry.getObject("location");
        List<PlayerTag> targets = (List<PlayerTag>) scriptEntry.getObject("targets");
        Effect effect = (Effect) scriptEntry.getObject("effect");
        Particle particleEffect = (Particle) scriptEntry.getObject("particleeffect");
        ItemTag iconcrack = scriptEntry.getObjectTag("iconcrack");
        ElementTag radius = scriptEntry.getElement("radius");
        ElementTag data = scriptEntry.getElement("data");
        ElementTag qty = scriptEntry.getElement("qty");
        ElementTag no_offset = scriptEntry.getElement("no_offset");
        boolean should_offset = no_offset == null || !no_offset.asBoolean();
        LocationTag offset = scriptEntry.getObjectTag("offset");
        ElementTag special_data = scriptEntry.getElement("special_data");
        LocationTag velocity = scriptEntry.getObjectTag("velocity");

        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), (effect != null ? ArgumentHelper.debugObj("effect", effect.name()) :
                    particleEffect != null ? ArgumentHelper.debugObj("special effect", particleEffect.getName()) :
                            (iconcrack != null ? iconcrack.debug() : "")) +
                    ArgumentHelper.debugObj("locations", locations.toString()) +
                    (targets != null ? ArgumentHelper.debugObj("targets", targets.toString()) : "") +
                    radius.debug() +
                    data.debug() +
                    qty.debug() +
                    offset.debug() +
                    (special_data != null ? special_data.debug() : "") +
                    (velocity != null ? velocity.debug() : "") +
                    (should_offset ? ArgumentHelper.debugObj("note", "Location will be offset 1 block-height upward (see documentation)") : ""));
        }

        for (LocationTag location : locations) {
            if (should_offset) {
                // Slightly increase the location's Y so effects don't seem to come out of the ground
                location = new LocationTag(location.clone().add(0, 1, 0));
            }

            // Play the Bukkit effect the number of times specified
            if (effect != null) {
                for (int n = 0; n < qty.asInt(); n++) {
                    if (targets != null) {
                        for (PlayerTag player : targets) {
                            if (player.isValid() && player.isOnline()) {
                                player.getPlayerEntity().playEffect(location, effect, data.asInt()); // TODO: 1.13
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
                for (Player player : players) {
                    Class clazz = particleEffect.neededData();
                    Object dataObject = null;
                    if (clazz != null) {
                        if (special_data == null) {
                            Debug.echoError(scriptEntry.getResidingQueue(), "Missing required special data for particle: " + particleEffect.getName());
                        }
                        else if (clazz == org.bukkit.Particle.DustOptions.class) {
                            ListTag dataList = ListTag.valueOf(special_data.asString(), scriptEntry.getContext());
                            if (dataList.size() != 2) {
                                Debug.echoError(scriptEntry.getResidingQueue(), "DustOptions special_data must have 2 list entries for particle: " + particleEffect.getName());
                            }
                            else {
                                float size = Float.parseFloat(dataList.get(0));
                                ColorTag color = ColorTag.valueOf(dataList.get(1), scriptEntry.context);
                                dataObject = new org.bukkit.Particle.DustOptions(color.getColor(), size);
                            }
                        }
                        else if (clazz == BlockData.class) {
                            MaterialTag blockMaterial = MaterialTag.valueOf(special_data.asString(), scriptEntry.getContext());
                            dataObject = blockMaterial.getModernData().data;
                        }
                        else if (clazz == ItemStack.class) {
                            ItemTag itemType = ItemTag.valueOf(special_data.asString(), scriptEntry.getContext());
                            dataObject = itemType.getItemStack();
                        }
                        else {
                            Debug.echoError(scriptEntry.getResidingQueue(), "Unknown particle data type: " + clazz.getCanonicalName() + " for particle: " + particleEffect.getName());
                        }
                    }
                    if (velocity == null) {
                        particleEffect.playFor(player, location, qty.asInt(), offset.toVector(), data.asFloat(), dataObject);
                    }
                    else {
                        float osX = (float) offset.getX();
                        float osY = (float) offset.getY();
                        float osZ = (float) offset.getZ();
                        int quantity = qty.asInt();
                        Vector velocityVector = velocity.toVector();
                        Random random = CoreUtilities.getRandom();
                        for (int i = 0; i < quantity; i++) {
                            LocationTag singleLocation = location.clone().add(random.nextDouble() * osX - osX * 0.5,
                                    random.nextDouble() * osY - osY * 0.5,
                                    random.nextDouble() * osZ - osZ * 0.5);
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
                    Particle particle = NMSHandler.getParticleHelper().getParticle("ITEM_CRACK");
                    for (Player player : players) {
                        particle.playFor(player, location, qty.asInt(), offset.toVector(), data.asFloat(), itemStack);
                    }
                }
            }
        }
    }
}
