package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dColor;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Arrays;
import java.util.List;

public class FireworkCommand extends AbstractCommand {

    // <--[command]
    // @Name Firework
    // @Syntax firework (<location>) (power:<#>) (<type>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail)
    // @Required 0
    // @Short Launches a firework with specific coloring
    // @Group world
    //
    // @Description
    // This command launches a firework from the specified location. The power option, which defaults to 1
    // if left empty, specifies how high the firework will go before exploding. The type option
    // which specifies the shape the firework will explode with. The primary option specifies what colour the
    // firework will initially explode as. The fade option specifies what colour the firework will
    // fade into after exploding. The flicker option means the firework will leave a trail behind it, and the
    // flicker option means the firework will explode with a flicker effect.
    //
    // @Tags
    // <e@entity.firework_item>
    // <i@item.is_firework>
    // <i@item.firework>
    // <entry[saveName].launched_firework> returns a dEntity of the firework that was launched.
    //
    // @Usage
    // Use to launch a star firework which explodes yellow and fades to white afterwards at the player's location
    // - firework <player.location> star primary:yellow fade:white
    //
    // @Usage
    // Use to make the firework launch double the height before exploding
    // - firework <player.location> power:2 star primary:yellow fade:white
    //
    // @Usage
    // Use to launch a firework which leaves a trail
    // - firework <player.location> random trail
    //
    // @Usage
    // Use to launch a firework which leaves a trail and explodes with a flicker effect at 10,43,-76 in world
    // - firework l@10,43,-76,world random trail flicker
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("random")) {
                scriptEntry.addObject("type", new Element(FireworkEffect.Type.values()[CoreUtilities.getRandom().nextInt(FireworkEffect.Type.values().length)].name()));
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(FireworkEffect.Type.values())) {
                scriptEntry.addObject("type", arg.asElement());
            }
            else if (!scriptEntry.hasObject("power")
                    && arg.matchesPrimitive(ArgumentHelper.PrimitiveType.Integer)) {
                scriptEntry.addObject("power", arg.asElement());
            }
            else if (!scriptEntry.hasObject("flicker")
                    && arg.matches("flicker")) {
                scriptEntry.addObject("flicker", "");
            }
            else if (!scriptEntry.hasObject("trail")
                    && arg.matches("trail")) {
                scriptEntry.addObject("trail", "");
            }
            else if (!scriptEntry.hasObject("primary")
                    && arg.matchesPrefix("primary")
                    && arg.matchesArgumentList(dColor.class)) {
                scriptEntry.addObject("primary", arg.asType(dList.class).filter(dColor.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("fade")
                    && arg.matchesPrefix("fade")
                    && arg.matchesArgumentList(dColor.class)) {
                scriptEntry.addObject("fade", arg.asType(dList.class).filter(dColor.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Use the NPC or player's locations as the location if one is not specified
        scriptEntry.defaultObject("location",
                Utilities.entryHasNPC(scriptEntry) ? Utilities.getEntryNPC(scriptEntry).getLocation() : null,
                Utilities.entryHasPlayer(scriptEntry) ? Utilities.getEntryPlayer(scriptEntry).getLocation() : null);

        scriptEntry.defaultObject("type", new Element("ball"));
        scriptEntry.defaultObject("power", new Element(1));
        scriptEntry.defaultObject("primary", Arrays.asList(dColor.valueOf("yellow")));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {
        // Get objects

        final dLocation location = scriptEntry.hasObject("location") ?
                (dLocation) scriptEntry.getObject("location") :
                Utilities.getEntryNPC(scriptEntry).getLocation();

        Element type = (Element) scriptEntry.getObject("type");
        Element power = (Element) scriptEntry.getObject("power");
        boolean flicker = scriptEntry.hasObject("flicker");
        boolean trail = scriptEntry.hasObject("trail");
        List<dColor> primary = (List<dColor>) scriptEntry.getObject("primary");
        List<dColor> fade = (List<dColor>) scriptEntry.getObject("fade");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), location.debug() +
                    type.debug() +
                    power.debug() +
                    (flicker ? ArgumentHelper.debugObj("flicker", flicker) : "") +
                    (trail ? ArgumentHelper.debugObj("trail", trail) : "") +
                    ArgumentHelper.debugObj("primary colors", primary.toString()) +
                    (fade != null ? ArgumentHelper.debugObj("fade colors", fade.toString()) : ""));
        }

        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.setPower(power.asInt());

        Builder fireworkBuilder = FireworkEffect.builder();
        fireworkBuilder.with(FireworkEffect.Type.valueOf(type.asString().toUpperCase()));

        fireworkBuilder.withColor(Conversion.convertColors(primary));
        if (fade != null) {
            fireworkBuilder.withFade(Conversion.convertColors(fade));
        }
        if (flicker) {
            fireworkBuilder.withFlicker();
        }
        if (trail) {
            fireworkBuilder.withTrail();
        }

        fireworkMeta.addEffects(fireworkBuilder.build());
        firework.setFireworkMeta(fireworkMeta);

        scriptEntry.addObject("launched_firework", new dEntity(firework));
    }
}
