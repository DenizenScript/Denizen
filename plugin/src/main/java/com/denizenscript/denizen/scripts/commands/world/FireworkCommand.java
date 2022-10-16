package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.Conversion;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Collections;
import java.util.List;

public class FireworkCommand extends AbstractCommand {

    public FireworkCommand() {
        setName("firework");
        setSyntax("firework (<location>) (power:<#>) (<type>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail) (life:<duration>)");
        setRequiredArguments(0, 8);
        isProcedural = false;
        setPrefixesHandled("life", "power", "primary", "fade");
        setBooleansHandled("flicker", "trail");
    }

    // <--[command]
    // @Name Firework
    // @Syntax firework (<location>) (power:<#>) (<type>/random) (primary:<color>|...) (fade:<color>|...) (flicker) (trail) (life:<duration>)
    // @Required 0
    // @Maximum 8
    // @Short Launches a firework with customizable style.
    // @Group world
    //
    // @Description
    // This command launches a firework from the specified location.
    //
    // If no location is given, the linked NPC or player's location will be used by default.
    //
    // The power option, which defaults to 1 if left empty, specifies the 'power' integer of the firework, which mainly controls how high the firework will go before exploding.
    // Alternately, the "life" option allows you to manually specify a specific duration.
    //
    // The type option which specifies the shape the firework will explode with. If unspecified, 'ball' will be used.
    // Can be any of: ball, ball_large, star, burst, or creeper
    //
    // The primary option specifies what color the firework explosion will start with, as a ColorTag. If unspecified, 'yellow' will be used.
    //
    // The fade option specifies what color the firework explosion will fade into, as a ColorTag.
    //
    // The trail option means the firework will leave a trail behind it.
    //
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
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addNotesOfType(LocationTag.class);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("location", arg.asType(LocationTag.class));
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("random")) {
                scriptEntry.addObject("type", new ElementTag(FireworkEffect.Type.values()[CoreUtilities.getRandom().nextInt(FireworkEffect.Type.values().length)]));
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matchesEnum(FireworkEffect.Type.class)) {
                scriptEntry.addObject("type", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        scriptEntry.defaultObject("location", Utilities.entryDefaultLocation(scriptEntry, false));
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Missing location!");
        }
        scriptEntry.defaultObject("type", new ElementTag("ball"));
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        final LocationTag location = (LocationTag) scriptEntry.getObject("location");
        ElementTag type = scriptEntry.getElement("type");
        List<ColorTag> primary = scriptEntry.argForPrefixList("primary", ColorTag.class, true);
        if (primary == null) {
            primary = Collections.singletonList(new ColorTag(Color.YELLOW));
        }
        List<ColorTag> fade = scriptEntry.argForPrefixList("fade", ColorTag.class, true);
        boolean flicker = scriptEntry.argAsBoolean("flicker");
        boolean trail = scriptEntry.argAsBoolean("trail");
        ElementTag power = scriptEntry.argForPrefixAsElement("power", "1");
        DurationTag life = scriptEntry.argForPrefix("life", DurationTag.class, true);
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), location, type, power, life, db("flicker", flicker), db("trail", trail), db("primary colors", primary), db("fade colors", fade));
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
        if (life != null) {
            NMSHandler.entityHelper.setFireworkLifetime(firework, life.getTicksAsInt());
        }
        scriptEntry.addObject("launched_firework", new EntityTag(firework));
    }
}
