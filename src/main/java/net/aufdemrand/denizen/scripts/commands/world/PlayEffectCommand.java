package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.minecraft.server.v1_8_R1.EnumParticle;
import net.minecraft.server.v1_8_R1.PacketPlayOutWorldParticles;
import org.bukkit.Effect;
import org.bukkit.Material;

import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


// <--[language]
// @name Particle Effects
// @group Useful Lists
// @description
// All of the effects listed here can be used by <@link command PlayEffect> to display visual effects or play sounds
//
// Sounds:
// - BLAZE_SHOOT, BOW_FIRE, CLICK1, CLICK2, DOOR_TOGGLE, EXTINGUISH, GHAST_SHOOT, GHAST_SHRIEK,
//   RECORD_PLAY, STEP_SOUND, ZOMBIE_CHEW_IRON_DOOR, ZOMBIE_CHEW_WOODEN_DOOR, ZOMBIE_DESTROY_DOOR
//
// Visual effects:
// - iconcrack_[id] (item break effect - examples: iconcrack_7, iconcrack_268)
// - ENDER_SIGNAL, MOBSPAWNER_FLAMES, POTION_BREAK, SMOKE,
// - HUGE_EXPLOSION, LARGE_EXPLODE, FIREWORKS_SPARK, BUBBLE, SUSPEND, DEPTH_SUSPEND, TOWN_AURA,
//   CRIT, MAGIC_CRIT, MOB_SPELL, MOB_SPELL_AMBIENT, SPELL, INSTANT_SPELL, WITCH_MAGIC, NOTE, STEP_SOUND,
//   PORTAL, ENCHANTMENT_TABLE, EXPLODE, FLAME, LAVA, FOOTSTEP, SPLASH, LARGE_SMOKE, CLOUD, RED_DUST,
//   SNOWBALL_POOF, DRIP_WATER, DRIP_LAVA, SNOW_SHOVEL, SLIME, HEART, ANGRY_VILLAGER, HAPPY_VILLAGER, BARRIER
//
// - RANDOM (chooses a random visual effect from the list starting with 'huge_explosion')
// -->

public class PlayEffectCommand extends AbstractCommand {

    public static enum ParticleEffect {
        ENDER_SIGNAL(EnumParticle.HEART), // TODO
        MOBSPAWNER_FLAMES(EnumParticle.MOB_APPEARANCE),
        POTION_BREAK(EnumParticle.HEART), // TODO
        SMOKE(EnumParticle.SMOKE_NORMAL),
        HUGE_EXPLOSION(EnumParticle.EXPLOSION_HUGE),
        LARGE_EXPLODE(EnumParticle.EXPLOSION_LARGE),
        FIREWORKS_SPARK(EnumParticle.FIREWORKS_SPARK),
        BUBBLE(EnumParticle.WATER_BUBBLE),
        SUSPEND(EnumParticle.SUSPENDED),
        DEPTH_SUSPEND(EnumParticle.SUSPENDED_DEPTH),
        TOWN_AURA(EnumParticle.TOWN_AURA),
        CRIT(EnumParticle.CRIT),
        MAGIC_CRIT(EnumParticle.CRIT_MAGIC),
        MOB_SPELL(EnumParticle.SPELL_MOB),
        MOB_SPELL_AMBIENT(EnumParticle.SPELL_MOB_AMBIENT),
        SPELL(EnumParticle.SPELL),
        INSTANT_SPELL(EnumParticle.SPELL_INSTANT),
        WITCH_MAGIC(EnumParticle.SPELL_WITCH),
        NOTE(EnumParticle.NOTE),
        STEP_SOUND(EnumParticle.HEART), // TODO
        PORTAL(EnumParticle.PORTAL),
        ENCHANTMENT_TABLE(EnumParticle.ENCHANTMENT_TABLE),
        EXPLODE(EnumParticle.EXPLOSION_NORMAL),
        FLAME(EnumParticle.FLAME),
        LAVA(EnumParticle.LAVA),
        FOOTSTEP(EnumParticle.FOOTSTEP),
        SPLASH(EnumParticle.WATER_SPLASH),
        LARGE_SMOKE(EnumParticle.SMOKE_LARGE),
        CLOUD(EnumParticle.CLOUD),
        RED_DUST(EnumParticle.REDSTONE),
        SNOWBALL_POOF(EnumParticle.SNOWBALL),
        DRIP_WATER(EnumParticle.DRIP_WATER),
        DRIP_LAVA(EnumParticle.DRIP_LAVA),
        SNOW_SHOVEL(EnumParticle.SNOW_SHOVEL),
        SLIME(EnumParticle.SLIME),
        HEART(EnumParticle.HEART),
        ANGRY_VILLAGER(EnumParticle.VILLAGER_ANGRY),
        HAPPY_VILLAGER(EnumParticle.VILLAGER_HAPPY),
        BARRIER(EnumParticle.BARRIER);
        public EnumParticle effect;
        ParticleEffect(EnumParticle eff) {
            effect = eff;
        }
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Iterate through arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentList(dLocation.class)) {

                scriptEntry.addObject("location", arg.asType(dList.class).filter(dLocation.class));
            }

            else if (!scriptEntry.hasObject("effect") &&
                    !scriptEntry.hasObject("particleeffect") &&
                    !scriptEntry.hasObject("iconcrack")) {

                if (arg.matchesEnum(ParticleEffect.values())) {
                    scriptEntry.addObject("particleeffect",
                            ParticleEffect.valueOf(arg.getValue().toUpperCase()));
                }
                else if (arg.matches("random")) {
                    // Get another effect if "RANDOM" is used
                    ParticleEffect effect = null;
                    // Make sure the new effect is not an invisible effect
                    while (effect == null || effect.toString().matches("^(BUBBLE|SUSPEND|DEPTH_SUSPEND)$")) { // TODO: Don't use regex for this?
                        effect = ParticleEffect.values()[CoreUtilities.getRandom().nextInt(ParticleEffect.values().length)];
                    }
                    scriptEntry.addObject("particleeffect", effect);
                }
                else if (arg.startsWith("iconcrack_")) {
                    // Allow iconcrack_[id] for item break effects (ex: iconcrack_1)
                    Element typeId = new Element(arg.getValue().substring(10));
                    if (typeId.isInt() && typeId.asInt() > 0 && Material.getMaterial(typeId.asInt()) != null)
                        scriptEntry.addObject("iconcrack", typeId);
                    else
                        dB.echoError("Invalid iconcrack_[id]. Must be a valid Material ID, besides 0.");
                }
                else if (arg.matchesEnum(Effect.values())) {
                    scriptEntry.addObject("effect", Effect.valueOf(arg.getValue().toUpperCase()));
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
                    && arg.matchesPrefix("qty", "q")) {

                scriptEntry.addObject("qty", arg.asElement());
            }

            else if (!scriptEntry.hasObject("offset")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("offset", "o")) {

                scriptEntry.addObject("offset", arg.asElement());
            }

            else if (!scriptEntry.hasObject("targets")
                && arg.matchesArgumentList(dPlayer.class)
                && arg.matchesPrefix("targets", "target", "t")) {

                scriptEntry.addObject("targets", arg.asType(dList.class).filter(dPlayer.class));
            }

            else
                arg.reportUnhandled();
        }

        // Use default values if necessary
        scriptEntry.defaultObject("location",
                ((BukkitScriptEntryData)scriptEntry.entryData).hasNPC() && ((BukkitScriptEntryData)scriptEntry.entryData).getNPC().isSpawned() ? Arrays.asList(((BukkitScriptEntryData)scriptEntry.entryData).getNPC().getLocation()) : null,
                ((BukkitScriptEntryData)scriptEntry.entryData).hasPlayer() && ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().isOnline() ? Arrays.asList(((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getLocation()): null);
        scriptEntry.defaultObject("data", new Element(0));
        scriptEntry.defaultObject("radius", new Element(15));
        scriptEntry.defaultObject("qty", new Element(1));
        scriptEntry.defaultObject("offset", new Element(0.5));

        // Check to make sure required arguments have been filled

        if (!scriptEntry.hasObject("effect") &&
                !scriptEntry.hasObject("particleeffect") &&
                !scriptEntry.hasObject("iconcrack"))
            throw new InvalidArgumentsException("Missing effect argument!");

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException("Missing location argument!");
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Extract objects from ScriptEntry
        List<dLocation> locations = (List<dLocation>) scriptEntry.getObject("location");
        List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");
        Effect effect = (Effect) scriptEntry.getObject("effect");
        ParticleEffect particleEffect = (ParticleEffect) scriptEntry.getObject("particleeffect");
        Element iconcrack = scriptEntry.getElement("iconcrack");
        Element radius = scriptEntry.getElement("radius");
        Element data = scriptEntry.getElement("data");
        Element qty = scriptEntry.getElement("qty");
        Element offset = scriptEntry.getElement("offset");

        // Report to dB
        dB.report(scriptEntry, getName(), (effect != null ? aH.debugObj("effect", effect.name()) :
                particleEffect != null ? aH.debugObj("special effect", particleEffect.name()) :
                iconcrack.debug()) +
                aH.debugObj("locations", locations.toString()) +
                (targets != null ? aH.debugObj("targets", targets.toString()): "") +
                radius.debug() +
                data.debug() +
                qty.debug() +
                offset.debug() +
                (effect != null ? "" : offset.debug()));

        for (dLocation location: locations) {
            // Slightly increase the location's Y so effects don't seem to come out of the ground
            location.add(0, 1, 0);

            // Play the Bukkit effect the number of times specified
            if (effect != null) {
                for (int n = 0; n < qty.asInt(); n++) {
                    if (targets != null) {
                        for (dPlayer player: targets)
                            if (player.isValid() && player.isOnline()) player.getPlayerEntity().playEffect(location, effect, data.asInt());
                    }
                    else {
                        location.getWorld().playEffect(location, effect, data.asInt(), radius.asInt());
                    }
                }
            }

            // Play a ParticleEffect
            else if (particleEffect != null) {
                float os = offset.asFloat();
                List<Player> players = new ArrayList<Player>();
                if (targets == null) {
                    float rad = radius.asFloat();
                    for (Player player: location.getWorld().getPlayers()) {
                        if (player.getLocation().distanceSquared(location) < rad * rad) {
                            players.add(player);
                        }
                    }
                }
                else {
                    for (dPlayer player: targets)
                        if (player.isValid() && player.isOnline()) players.add(player.getPlayerEntity());
                }
                PacketPlayOutWorldParticles o = new PacketPlayOutWorldParticles(particleEffect.effect, false, (float)location.getX(),
                        (float)location.getY(), (float)location.getZ(), os, os, os, data.asFloat(), qty.asInt());
                for (Player player: players) {
                    ((CraftPlayer)player).getHandle().playerConnection.sendPacket(o);
                }
            }

            // Play an iconcrack (item break) effect
            else {
                float os = offset.asFloat();
                List<Player> players = new ArrayList<Player>();
                if (targets == null) {
                    float rad = radius.asFloat();
                    for (Player player: location.getWorld().getPlayers()) {
                        if (player.getLocation().distanceSquared(location) < rad * rad) {
                            players.add(player);
                        }
                    }
                }
                else {
                    for (dPlayer player: targets)
                        if (player.isValid() && player.isOnline()) players.add(player.getPlayerEntity());
                }
                PacketPlayOutWorldParticles o = new PacketPlayOutWorldParticles(EnumParticle.ITEM_CRACK, false, (float)location.getX(),
                        (float)location.getY(), (float)location.getZ(), os, os, os, data.asFloat(), qty.asInt(),
                        iconcrack.asInt(), iconcrack.asInt()); // TODO: ???
                for (Player player: players) {
                    ((CraftPlayer)player).getHandle().playerConnection.sendPacket(o);
                }
            }
        }
    }
}
