package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.abstracts.ParticleHelper;
import net.aufdemrand.denizen.nms.interfaces.Effect;
import net.aufdemrand.denizen.nms.interfaces.Particle;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// <--[language]
// @name Particle Effects
// @group Useful Lists
// @description
// All of the effects listed here can be used by <@link command PlayEffect> to display visual effects or play sounds
//
// Effects:
// - iconcrack_[id],[data] (item break effect - examples: iconcrack_7, iconcrack_17,3)
// - blockcrack_[id] (block break effect)
// - blockdust_[id] (block break effect)
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

                scriptEntry.addObject("location", arg.asType(dList.class).filter(dLocation.class));
            }

            else if (!scriptEntry.hasObject("effect") &&
                    !scriptEntry.hasObject("particleeffect") &&
                    !scriptEntry.hasObject("iconcrack")) {

                if (particleHelper.hasParticle(arg.getValue())) {
                    scriptEntry.addObject("particleeffect", particleHelper.getParticle(arg.getValue()));
                }
                else if (arg.matches("random")) {
                    // Get another effect if "RANDOM" is used
                    if (CoreUtilities.getRandom().nextDouble() < 0.5) {
                        // Make sure the new effect is not an invisible effect
                        List<Particle> visible = particleHelper.getVisibleParticles();
                        scriptEntry.addObject("particleeffect", visible.get(CoreUtilities.getRandom().nextInt(visible.size())));
                    }
                    else {
                        List<Effect> visual = particleHelper.getVisualEffects();
                        scriptEntry.addObject("effect", visual.get(CoreUtilities.getRandom().nextInt(visual.size())));
                    }
                }
                else if (arg.startsWith("iconcrack_")) {
                    // Allow iconcrack_[id],[data] for item break effects (ex: iconcrack_1)
                    String shrunk = arg.getValue().substring("iconcrack_".length());
                    String[] split = shrunk.split(",");
                    Element typeId = new Element(split[0]);
                    if (typeId.isInt() && typeId.asInt() > 0 && Material.getMaterial(typeId.asInt()) != null) {
                        scriptEntry.addObject("iconcrack", typeId);
                    }
                    else {
                        dB.echoError("Invalid iconcrack_[id]. Must be a valid Material ID, besides 0.");
                    }
                    Element dataId = new Element(split.length <= 1 ? "0" : split[1]);
                    scriptEntry.addObject("iconcrack_data", dataId);
                    scriptEntry.addObject("iconcrack_type", new Element("iconcrack"));
                }
                else if (arg.startsWith("blockcrack_")) {
                    String shrunk = arg.getValue().substring("blockcrack_".length());
                    String[] split = shrunk.split(",");
                    Element typeId = new Element(split[0]);
                    if (typeId.isInt() && typeId.asInt() > 0 && Material.getMaterial(typeId.asInt()) != null) {
                        scriptEntry.addObject("iconcrack", typeId);
                    }
                    else {
                        dB.echoError("Invalid blockcrack_[id]. Must be a valid Material ID, besides 0.");
                    }
                    Element dataId = new Element(split.length <= 1 ? "0" : split[1]);
                    scriptEntry.addObject("iconcrack_data", dataId);
                    scriptEntry.addObject("iconcrack_type", new Element("blockcrack"));
                }
                else if (arg.startsWith("blockdust_")) {
                    String shrunk = arg.getValue().substring("blockdust_".length());
                    String[] split = shrunk.split(",");
                    Element typeId = new Element(split[0]);
                    if (typeId.isInt() && typeId.asInt() > 0 && Material.getMaterial(typeId.asInt()) != null) {
                        scriptEntry.addObject("iconcrack", typeId);
                    }
                    else {
                        dB.echoError("Invalid blockdust_[id]. Must be a valid Material ID, besides 0.");
                    }
                    Element dataId = new Element(split.length <= 1 ? "0" : split[1]);
                    scriptEntry.addObject("iconcrack_data", dataId);
                    scriptEntry.addObject("iconcrack_type", new Element("blockdust"));
                }
                else if (particleHelper.hasEffect(arg.getValue())) {
                    scriptEntry.addObject("effect", particleHelper.getEffect(arg.getValue()));
                }
            }

            else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("visibility", "v", "radius", "r")) {

                scriptEntry.addObject("radius", arg.asElement());
            }

            else if (!scriptEntry.hasObject("data")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("data", "d")) {

                scriptEntry.addObject("data", arg.asElement());
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

                scriptEntry.addObject("targets", arg.asType(dList.class).filter(dPlayer.class));
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
                !scriptEntry.hasObject("iconcrack")) {
            throw new InvalidArgumentsException("Missing effect argument!");
        }

        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Extract objects from ScriptEntry
        List<dLocation> locations = (List<dLocation>) scriptEntry.getObject("location");
        List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");
        Effect effect = (Effect) scriptEntry.getObject("effect");
        Particle particleEffect = (Particle) scriptEntry.getObject("particleeffect");
        Element iconcrack = scriptEntry.getElement("iconcrack");
        Element iconcrack_data = scriptEntry.getElement("iconcrack_data");
        Element iconcrack_type = scriptEntry.getElement("iconcrack_type");
        Element radius = scriptEntry.getElement("radius");
        Element data = scriptEntry.getElement("data");
        Element qty = scriptEntry.getElement("qty");
        dLocation offset = scriptEntry.getdObject("offset");

        // Report to dB
        dB.report(scriptEntry, getName(), (effect != null ? aH.debugObj("effect", effect.getName()) :
                particleEffect != null ? aH.debugObj("special effect", particleEffect.getName()) :
                        iconcrack_type.debug() + iconcrack.debug() + (iconcrack_data != null ? iconcrack_data.debug() : "")) +
                aH.debugObj("locations", locations.toString()) +
                (targets != null ? aH.debugObj("targets", targets.toString()) : "") +
                radius.debug() +
                data.debug() +
                qty.debug() +
                offset.debug());

        for (dLocation location : locations) {
            // Slightly increase the location's Y so effects don't seem to come out of the ground
            location.add(0, 1, 0);

            // Play the Bukkit effect the number of times specified
            if (effect != null) {
                for (int n = 0; n < qty.asInt(); n++) {
                    if (targets != null) {
                        for (dPlayer player : targets) {
                            if (player.isValid() && player.isOnline()) {
                                effect.playFor(player.getPlayerEntity(), location, data.asInt());
                            }
                        }
                    }
                    else {
                        effect.play(location, data.asInt(), radius.asInt());
                    }
                }
            }

            // Play a ParticleEffect
            else if (particleEffect != null) {
                float osX = (float) offset.getX();
                float osY = (float) offset.getY();
                float osZ = (float) offset.getZ();
                List<Player> players = new ArrayList<Player>();
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
                    particleEffect.playFor(player, location, qty.asInt(), offset.toVector(), data.asFloat());
                }
            }

            // Play an iconcrack (item break) effect
            else {
                List<Player> players = new ArrayList<Player>();
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
                // TODO: better this all
                if (iconcrack_type.asString().equalsIgnoreCase("iconcrack")) {
                    ItemStack itemStack = new ItemStack(iconcrack.asInt(), 1, (short)(iconcrack_data != null ? iconcrack_data.asInt() : 0));
                    Particle particle = NMSHandler.getInstance().getParticleHelper().getParticle("ITEM_CRACK");
                    for (Player player : players) {
                        particle.playFor(player, location, qty.asInt(), offset.toVector(), data.asFloat(), itemStack);
                    }
                }
                else if (iconcrack_type.asString().equalsIgnoreCase("blockcrack")) {
                    MaterialData materialData = new MaterialData(iconcrack.asInt(), (byte) (iconcrack_data != null ? iconcrack_data.asInt() : 0));
                    Particle particle = NMSHandler.getInstance().getParticleHelper().getParticle("BLOCK_CRACK");
                    for (Player player : players) {
                        particle.playFor(player, location, qty.asInt(), offset.toVector(), data.asFloat(), materialData);
                    }
                }
                else { // blockdust
                    MaterialData materialData = new MaterialData(iconcrack.asInt(), (byte) (iconcrack_data != null ? iconcrack_data.asInt() : 0));
                    Particle particle = NMSHandler.getInstance().getParticleHelper().getParticle("BLOCK_DUST");
                    for (Player player : players) {
                        particle.playFor(player, location, qty.asInt(), offset.toVector(), data.asFloat(), materialData);
                    }
                }
            }
        }
    }
}
