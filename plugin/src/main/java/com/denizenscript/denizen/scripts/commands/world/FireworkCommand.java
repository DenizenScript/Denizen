package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Arrays;
import java.util.List;

public class FireworkCommand extends AbstractCommand {

    public FireworkCommand() {
        setName("firework");
        setSyntax("firework (<location>) (power:<#>) (<type>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail)");
        setRequiredArguments(0, 7);
        isProcedural = false;
    }

    // <--[command]
    // @Name Firework
    // @Syntax firework (<location>) (power:<#>) (<type>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail)
    // @Required 0
    // @Maximum 7
    // @Short Launches a firework with specific coloring
    // @Group world
    //
    // @Description
    // This command launches a firework from the specified location.
    // If no location is given, the linked NPC or player's location will be used by default.
    // The power option, which defaults to 1 if left empty, specifies how high the firework will go before exploding.
    // The type option which specifies the shape the firework will explode with. If unspecified, 'ball' will be used.
    // The primary option specifies what colour the firework will initially explode as. If unspecified, 'yellow' will be used.
    // The fade option specifies a colour that the firework will fade into after exploding.
    // The trail option means the firework will leave a trail behind it.
    // The flicker option means the firework will explode with a flicker effect.
    //
    // @Tags
    // <EntityTag.firework_item>
    // <ItemTag.is_firework>
    // <ItemTag.firework>
    // <entry[saveName].launched_firework> returns a EntityTag of the firework that was launched.
    //
    // @Usage
    // Use to launch a star firework which explodes yellow and fades to white afterwards at the player's location.
    // - firework <player.location> star primary:yellow fade:white
    //
    // @Usage
    // Use to make the firework launch double the height before exploding.
    // - firework <player.location> power:2 star primary:yellow fade:white
    //
    // @Usage
    // Use to launch a firework which leaves a trail.
    // - firework <player.location> random trail
    //
    // @Usage
    // Use to launch a firework which leaves a trail and explodes with a flicker effect at related location.
    // - firework <context.location> random trail flicker
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {
            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("random")) {
                scriptEntry.addObject("type", new ElementTag(FireworkEffect.Type.values()[CoreUtilities.getRandom().nextInt(FireworkEffect.Type.values().length)].name()));
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(FireworkEffect.Type.values())) {
                scriptEntry.addObject("type", arg.asElement());
            }
            else if (!scriptEntry.hasObject("power")
                    && arg.matchesInteger()) {
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
                    && arg.matchesArgumentList(ColorTag.class)) {
                scriptEntry.addObject("primary", arg.asType(ListTag.class).filter(ColorTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("fade")
                    && arg.matchesPrefix("fade")
                    && arg.matchesArgumentList(ColorTag.class)) {
                scriptEntry.addObject("fade", arg.asType(ListTag.class).filter(ColorTag.class, scriptEntry));
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("location", Utilities.entryDefaultLocation(scriptEntry, false));
        scriptEntry.defaultObject("type", new ElementTag("ball"));
        scriptEntry.defaultObject("power", new ElementTag(1));
        scriptEntry.defaultObject("primary", Arrays.asList(new ColorTag(Color.YELLOW)));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {
        final LocationTag location = scriptEntry.hasObject("location") ?
                (LocationTag) scriptEntry.getObject("location") :
                Utilities.getEntryNPC(scriptEntry).getLocation();
        ElementTag type = scriptEntry.getElement("type");
        ElementTag power = scriptEntry.getElement("power");
        boolean flicker = scriptEntry.hasObject("flicker");
        boolean trail = scriptEntry.hasObject("trail");
        List<ColorTag> primary = (List<ColorTag>) scriptEntry.getObject("primary");
        List<ColorTag> fade = (List<ColorTag>) scriptEntry.getObject("fade");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), location.debug() +
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
        scriptEntry.addObject("launched_firework", new EntityTag(firework));
    }
}
