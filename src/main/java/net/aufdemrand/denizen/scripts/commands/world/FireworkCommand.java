package net.aufdemrand.denizen.scripts.commands.world;

import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dColor;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.Conversion;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;

import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * Shoots a firework into the air from a location.
 * If no location is chosen, the firework is shot from
 * the NPC or player's location.
 *
 * @author David Cernat
 */

public class FireworkCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("location")
                && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }

            else if (!scriptEntry.hasObject("type")
                     && arg.matches("random")) {
                scriptEntry.addObject("type", new Element(FireworkEffect.Type.values()[Utilities.getRandom().nextInt(FireworkEffect.Type.values().length)].name()));
            }

            else if (!scriptEntry.hasObject("type")
                     && arg.matchesEnum(FireworkEffect.Type.values())) {
                scriptEntry.addObject("type", arg.asElement());
            }

            else if (!scriptEntry.hasObject("power")
                     && arg.matchesPrimitive(aH.PrimitiveType.Integer)) {
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
                scriptEntry.addObject("primary", ((dList) arg.asType(dList.class)).filter(dColor.class));
            }

            else if (!scriptEntry.hasObject("fade")
                     && arg.matchesPrefix("fade")
                     && arg.matchesArgumentList(dColor.class)) {
                scriptEntry.addObject("fade", ((dList) arg.asType(dList.class)).filter(dColor.class));
            }

            else dB.echoError(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg.raw_value);
        }

        // Use the NPC or player's locations as the location if one is not specified
        scriptEntry.defaultObject("location",
                scriptEntry.hasNPC() ? scriptEntry.getNPC().getLocation() : null,
                scriptEntry.hasPlayer() ? scriptEntry.getPlayer().getLocation() : null);

        scriptEntry.defaultObject("type", new Element("BALL"));
        scriptEntry.defaultObject("power", new Element(1));
        scriptEntry.defaultObject("primary", Arrays.asList(dColor.valueOf("yellow")));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects

        final dLocation location = scriptEntry.hasObject("location") ?
                                   (dLocation) scriptEntry.getObject("location") :
                                   (dLocation) scriptEntry.getNPC().getLocation();

        Element type = (Element) scriptEntry.getObject("type");
        Element power = (Element) scriptEntry.getObject("power");
        boolean flicker = scriptEntry.hasObject("flicker");
        boolean trail = scriptEntry.hasObject("trail");
        List<dColor> primary = (List<dColor>) scriptEntry.getObject("primary");
        List<dColor> fade = (List<dColor>) scriptEntry.getObject("fade");

        // Report to dB
        dB.report(getName(), location.debug() +
                             type.debug() +
                             power.debug() +
                             (flicker ? aH.debugObj("flicker", flicker) : "") +
                             (trail ? aH.debugObj("trail", trail) : "") +
                             aH.debugObj("primary colors", primary.toString()) +
                             (fade != null ? aH.debugObj("fade colors", fade.toString()) : ""));

        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta fireworkMeta = firework.getFireworkMeta();
        fireworkMeta.setPower(power.asInt());

        Builder fireworkBuilder = FireworkEffect.builder();
        fireworkBuilder.with(FireworkEffect.Type.valueOf(type.asString()));

                          fireworkBuilder.withColor(Conversion.convertColors(primary));
        if (fade != null) fireworkBuilder.withFade(Conversion.convertColors(fade));
        if (flicker)      fireworkBuilder.withFlicker();
        if (trail)        fireworkBuilder.withTrail();

        fireworkMeta.addEffects(fireworkBuilder.build());
        firework.setFireworkMeta(fireworkMeta);
    }
}
