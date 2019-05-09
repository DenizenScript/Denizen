package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.abstracts.ParticleHelper;
import net.aufdemrand.denizen.nms.interfaces.Particle;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.*;


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
// - ANVIL_BREAK, ANVIL_LAND, ANVIL_USE, BARRIER, BAT_TAKEOFF, BLAZE_SHOOT, BOW_FIRE, BREWING_STAND_BREW,
//   CHORUS_FLOWER_DEATH, CHORUS_FLOWER_GROW, CLICK1, CLICK2, CLOUD, COLOURED_DUST, CRIT, CRIT_MAGIC, DAMAGE_INDICATOR,
//   DOOR_CLOSE, DOOR_TOGGLE, DRAGON_BREATH, DRIP_LAVA, DRIP_WATER, ENCHANTMENT_TABLE, END_GATEWAY_SPAWN, END_ROD,
//   ENDER_SIGNAL, ENDERDRAGON_GROWL, ENDERDRAGON_SHOOT, ENDEREYE_LAUNCH, EXPLOSION, EXPLOSION_HUGE, EXPLOSION_LARGE,
//   EXPLOSION_NORMAL, EXTINGUISH, FALLING_DUST, FENCE_GATE_CLOSE, FENCE_GATE_TOGGLE, FIREWORK_SHOOT, FIREWORKS_SPARK,
//   FLAME, FLYING_GLYPH, FOOTSTEP, GHAST_SHOOT, GHAST_SHRIEK, HAPPY_VILLAGER, HEART, INSTANT_SPELL, IRON_DOOR_CLOSE,
//   IRON_DOOR_TOGGLE, IRON_TRAPDOOR_CLOSE, IRON_TRAPDOOR_TOGGLE, ITEM_TAKE, LARGE_SMOKE, LAVA, LAVA_POP,
//   LAVADRIP, MAGIC_CRIT, MOB_APPEARANCE, MOBSPAWNER_FLAMES, NOTE, PARTICLE_SMOKE, PORTAL, PORTAL_TRAVEL, POTION_BREAK,
//   POTION_SWIRL, POTION_SWIRL_TRANSPARENT, RECORD_PLAY, REDSTONE, SLIME, SMALL_SMOKE, SMOKE, SMOKE_LARGE,
//   SMOKE_NORMAL, SNOW_SHOVEL, SNOWBALL, SNOWBALL_BREAK, SPELL, SPELL_INSTANT, SPELL_MOB, SPELL_MOB_AMBIENT,
//   SPELL_WITCH, SPLASH, STEP_SOUND, SUSPENDED, SUSPENDED_DEPTH, SWEEP_ATTACK, TOWN_AURA,
//   TRAPDOOR_CLOSE, TRAPDOOR_TOGGLE, VILLAGER_ANGRY, VILLAGER_HAPPY, VILLAGER_PLANT_GROW, VILLAGER_THUNDERCLOUD,
//   VOID_FOG, WATER_BUBBLE, WATER_DROP, WATER_SPLASH, WATER_WAKE, WATERDRIP, WITCH_MAGIC, WITHER_BREAK_BLOCK,
//   WITHER_SHOOT, ZOMBIE_CHEW_IRON_DOOR, ZOMBIE_CHEW_WOODEN_DOOR, ZOMBIE_CONVERTED_VILLAGER, ZOMBIE_DESTROY_DOOR,
//   ZOMBIE_INFECT
// TODO: split the above list between sounds, visual effects, and particles?
//
// - RANDOM (chooses a random visual effect from the list starting with 'huge_explosion')
// -->

public class PlayEffectCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        ParticleHelper particleHelper = NMSHandler.getInstance().getParticleHelper();

        // Iterate through arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

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
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("visibility", "v", "radius", "r")) {

                scriptEntry.addObject("radius", arg.asElement());
            }
            else if (!scriptEntry.hasObject("data")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("data", "d")) {

                scriptEntry.addObject("data", arg.asElement());
            }
            else if (!scriptEntry.hasObject("special_data")
                    && arg.matchesOnePrefix("special_data")) {
                scriptEntry.addObject("special_data", arg.asElement());
            }
            else if (!scriptEntry.hasObject("qty")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)
                    && arg.matchesPrefix("qty", "q", "quantity")) {

                scriptEntry.addObject("qty", arg.asElement());
            }
            else if (!scriptEntry.hasObject("offset")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
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
                ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() && ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().isSpawned() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getLocation()) : null,
                ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() && ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().isOnline() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getLocation()) : null);
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
            dB.report(scriptEntry, getName(), (effect != null ? aH.debugObj("effect", effect.name()) :
                    particleEffect != null ? aH.debugObj("special effect", particleEffect.getName()) :
                            (iconcrack != null ? iconcrack.debug()
                                    : blockcrack != null ? blockcrack.debug()
                                    : blockdust.debug())) +
                    aH.debugObj("locations", locations.toString()) +
                    (targets != null ? aH.debugObj("targets", targets.toString()) : "") +
                    radius.debug() +
                    data.debug() +
                    qty.debug() +
                    offset.debug() +
                    (special_data != null ? special_data.debug() : "") +
                    (should_offset ? aH.debugObj("note", "Location will be offset 1 block-height upward (see documentation)") : ""));
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
                                float size = aH.getFloatFrom(dataList.get(0));
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
