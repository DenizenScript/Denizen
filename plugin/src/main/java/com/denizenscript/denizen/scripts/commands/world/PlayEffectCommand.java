package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.abstracts.ParticleHelper;
import com.denizenscript.denizen.nms.interfaces.Particle;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.*;

public class PlayEffectCommand extends AbstractCommand {

    // <--[language]
    // @name Particle Effects
    // @group Useful Lists
    // @description
    // All of the effects listed here can be used by <@link command PlayEffect> to display visual effects or play sounds
    //
    // Effects:
    // - iconcrack_[item] (item break effect - examples: iconcrack_stone, iconcrack_grass)
    // - blockcrack_[material] (block break effect - examples: blockcrack_stone, blockcrack_grass)
    // - blockdust_[material] (block break effect - examples: blockdust_stone, blockdust_grass)
    // - Everything on <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html>
    // - Everything on <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Effect.html>
    // - RANDOM (chooses a random visual effect from the list starting with 'huge_explosion')
    // -->

    // <--[command]
    // @Name PlayEffect
    // @Syntax playeffect [effect:<name>] [at:<location>|...] (data:<#.#>) (special_data:<data>) (visibility:<#.#>) (quantity:<#>) (offset:<#.#>,<#.#>,<#.#>) (targets:<player>|...)
    // @Required 2
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
    // - For REDSTONE particles, the input is of format: <size>|<color>, for example: "1.2|red". Color input is any valid dColor object.
    //
    // @Tags
    // None
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
    // - playeffect effect:FIREWORKS_SPARK at:<w@world.spawn_location> visibility:100 quantity:375 data:0 offset:50.0
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        ParticleHelper particleHelper = NMSHandler.getInstance().getParticleHelper();

        // Iterate through arguments
        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentList(dLocation.class)) {
                if (arg.matchesOnePrefix("at")) {
                    scriptEntry.addObject("no_offset", new Element(true));
                }

                scriptEntry.addObject("location", arg.asType(dList.class).filter(dLocation.class, scriptEntry));
                continue;
            }
            else if (!scriptEntry.hasObject("effect") &&
                    !scriptEntry.hasObject("particleeffect") &&
                    !scriptEntry.hasObject("iconcrack") &&
                    !scriptEntry.hasObject("blockcrack") &&
                    !scriptEntry.hasObject("blockdust")) {

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
                    // Allow iconcrack_[item] for item break effects (ex: iconcrack_stone)
                    String shrunk = arg.getValue().substring("iconcrack_".length());
                    dItem item = dItem.valueOf(shrunk, scriptEntry.entryData.getTagContext());
                    if (item != null) {
                        scriptEntry.addObject("iconcrack", item);
                    }
                    else {
                        dB.echoError("Invalid iconcrack_[item]. Must be a valid dItem!");
                    }
                    continue;
                }
                else if (arg.startsWith("blockcrack_")) {
                    String shrunk = arg.getValue().substring("blockcrack_".length());
                    dMaterial material = dMaterial.valueOf(shrunk);
                    if (material != null) {
                        scriptEntry.addObject("blockcrack", material);
                    }
                    else {
                        dB.echoError("Invalid blockcrack_[item]. Must be a valid dMaterial!");
                    }
                    continue;
                }
                else if (arg.startsWith("blockdust_")) {
                    String shrunk = arg.getValue().substring("blockdust_".length());
                    dMaterial material = dMaterial.valueOf(shrunk);
                    if (material != null) {
                        scriptEntry.addObject("blockdust", material);
                    }
                    else {
                        dB.echoError("Invalid blockdust_[item]. Must be a valid dMaterial!");
                    }
                    continue;
                }
                else if (arg.matchesEnum(Effect.values())) {
                    scriptEntry.addObject("effect", Effect.valueOf(arg.getValue().toUpperCase()));
                    continue;
                }
                else if (NMSHandler.getInstance().getParticleHelper().effectRemap.containsKey(arg.getValue().toUpperCase())) {
                    scriptEntry.addObject("effect", NMSHandler.getInstance().getParticleHelper().effectRemap.get(arg.getValue().toUpperCase()));
                }
            }
            if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)
                    && arg.matchesPrefix("visibility", "v", "radius", "r")) {

                scriptEntry.addObject("radius", arg.asElement());
            }
            else if (!scriptEntry.hasObject("data")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)
                    && arg.matchesPrefix("data", "d")) {

                scriptEntry.addObject("data", arg.asElement());
            }
            else if (!scriptEntry.hasObject("special_data")
                    && arg.matchesOnePrefix("special_data")) {
                scriptEntry.addObject("special_data", arg.asElement());
            }
            else if (!scriptEntry.hasObject("qty")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Integer)
                    && arg.matchesPrefix("qty", "q", "quantity")) {

                scriptEntry.addObject("qty", arg.asElement());
            }
            else if (!scriptEntry.hasObject("offset")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Double)
                    && arg.matchesPrefix("offset", "o")) {

                double offset = arg.asElement().asDouble();
                scriptEntry.addObject("offset", new dLocation(null, offset, offset, offset));
            }
            else if (!scriptEntry.hasObject("offset")
                    && arg.matchesArgumentType(dLocation.class)
                    && arg.matchesPrefix("offset", "o")) {

                scriptEntry.addObject("offset", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("targets")
                    && arg.matchesArgumentList(dPlayer.class)
                    && arg.matchesPrefix("targets", "target", "t")) {

                scriptEntry.addObject("targets", arg.asType(dList.class).filter(dPlayer.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Use default values if necessary
        scriptEntry.defaultObject("location",
                Utilities.entryHasNPC(scriptEntry) && Utilities.getEntryNPC(scriptEntry).isSpawned() ? Arrays.asList(Utilities.getEntryNPC(scriptEntry).getLocation()) : null,
                Utilities.entryHasPlayer(scriptEntry) && Utilities.getEntryPlayer(scriptEntry).isOnline() ? Arrays.asList(Utilities.getEntryPlayer(scriptEntry).getLocation()) : null);
        scriptEntry.defaultObject("data", new Element(0));
        scriptEntry.defaultObject("radius", new Element(15));
        scriptEntry.defaultObject("qty", new Element(1));
        scriptEntry.defaultObject("offset", new dLocation(null, 0.5, 0.5, 0.5));

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("effect") &&
                !scriptEntry.hasObject("particleeffect") &&
                !scriptEntry.hasObject("iconcrack") &&
                !scriptEntry.hasObject("blockcrack") &&
                !scriptEntry.hasObject("blockdust")) {
            throw new InvalidArgumentsException("Missing effect argument!");
        }

        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        // Extract objects from ScriptEntry
        List<dLocation> locations = (List<dLocation>) scriptEntry.getObject("location");
        List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");
        Effect effect = (Effect) scriptEntry.getObject("effect");
        Particle particleEffect = (Particle) scriptEntry.getObject("particleeffect");
        dItem iconcrack = scriptEntry.getdObject("iconcrack");
        dMaterial blockcrack = scriptEntry.getdObject("blockcrack");
        dMaterial blockdust = scriptEntry.getdObject("blockdust");
        Element radius = scriptEntry.getElement("radius");
        Element data = scriptEntry.getElement("data");
        Element qty = scriptEntry.getElement("qty");
        Element no_offset = scriptEntry.getElement("no_offset");
        boolean should_offset = no_offset == null || !no_offset.asBoolean();
        dLocation offset = scriptEntry.getdObject("offset");
        Element special_data = scriptEntry.getElement("special_data");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), (effect != null ? ArgumentHelper.debugObj("effect", effect.name()) :
                    particleEffect != null ? ArgumentHelper.debugObj("special effect", particleEffect.getName()) :
                            (iconcrack != null ? iconcrack.debug()
                                    : blockcrack != null ? blockcrack.debug()
                                    : blockdust.debug())) +
                    ArgumentHelper.debugObj("locations", locations.toString()) +
                    (targets != null ? ArgumentHelper.debugObj("targets", targets.toString()) : "") +
                    radius.debug() +
                    data.debug() +
                    qty.debug() +
                    offset.debug() +
                    (special_data != null ? special_data.debug() : "") +
                    (should_offset ? ArgumentHelper.debugObj("note", "Location will be offset 1 block-height upward (see documentation)") : ""));
        }

        for (dLocation location : locations) {
            if (should_offset) {
                // Slightly increase the location's Y so effects don't seem to come out of the ground
                location = new dLocation(location.clone().add(0, 1, 0));
            }

            // Play the Bukkit effect the number of times specified
            if (effect != null) {
                for (int n = 0; n < qty.asInt(); n++) {
                    if (targets != null) {
                        for (dPlayer player : targets) {
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
                float osX = (float) offset.getX();
                float osY = (float) offset.getY();
                float osZ = (float) offset.getZ();
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
                    for (dPlayer player : targets) {
                        if (player.isValid() && player.isOnline()) {
                            players.add(player.getPlayerEntity());
                        }
                    }
                }
                for (Player player : players) {
                    Class clazz = particleEffect.neededData();
                    if (clazz == null) {
                        particleEffect.playFor(player, location, qty.asInt(), offset.toVector(), data.asFloat());
                    }
                    else {
                        Object dataObject = null;
                        if (special_data == null) {
                            dB.echoError(scriptEntry.getResidingQueue(), "Missing required special data for particle: " + particleEffect.getName());
                        }
                        else if (clazz == org.bukkit.Particle.DustOptions.class) {
                            dList dataList = dList.valueOf(special_data.asString());
                            if (dataList.size() != 2) {
                                dB.echoError(scriptEntry.getResidingQueue(), "DustOptions special_data must have 2 list entries for particle: " + particleEffect.getName());
                            }
                            else {
                                float size = ArgumentHelper.getFloatFrom(dataList.get(0));
                                dColor color = dColor.valueOf(dataList.get(1));
                                dataObject = new org.bukkit.Particle.DustOptions(color.getColor(), size);
                            }
                        }
                        else {
                            dB.echoError(scriptEntry.getResidingQueue(), "Unknown particle data type: " + clazz.getCanonicalName() + " for particle: " + particleEffect.getName());
                        }
                        particleEffect.playFor(player, location, qty.asInt(), offset.toVector(), data.asFloat(), dataObject);
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
                    for (dPlayer player : targets) {
                        if (player.isValid() && player.isOnline()) {
                            players.add(player.getPlayerEntity());
                        }
                    }
                }

                if (iconcrack != null) {
                    ItemStack itemStack = iconcrack.getItemStack();
                    Particle particle = NMSHandler.getInstance().getParticleHelper().getParticle("ITEM_CRACK");
                    for (Player player : players) {
                        particle.playFor(player, location, qty.asInt(), offset.toVector(), data.asFloat(), itemStack);
                    }
                }
                else if (blockcrack != null) {
                    MaterialData materialData = blockcrack.getMaterialData();
                    Particle particle = NMSHandler.getInstance().getParticleHelper().getParticle("BLOCK_CRACK");
                    for (Player player : players) {
                        particle.playFor(player, location, qty.asInt(), offset.toVector(), data.asFloat(), materialData);
                    }
                }
                else { // blockdust
                    MaterialData materialData = blockdust.getMaterialData();
                    Particle particle = NMSHandler.getInstance().getParticleHelper().getParticle("BLOCK_DUST");
                    for (Player player : players) {
                        particle.playFor(player, location, qty.asInt(), offset.toVector(), data.asFloat(), materialData);
                    }
                }
            }
        }
    }
}
