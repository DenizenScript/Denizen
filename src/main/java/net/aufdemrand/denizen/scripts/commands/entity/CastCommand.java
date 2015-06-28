package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class CastCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Iterate through arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("remove")
                    && arg.matches("remove", "cancel"))
                scriptEntry.addObject("remove", Element.TRUE);

            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("duration", "d")
                    && arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("duration", arg.asType(Duration.class));

            else if (!scriptEntry.hasObject("amplifier")
                    && arg.matchesPrefix("power", "p", "amplifier", "a")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double))
                scriptEntry.addObject("amplifier", arg.asElement());

            else if (!scriptEntry.hasObject("effect")
                    && PotionEffectType.getByName(arg.asElement().asString()) != null) {
                scriptEntry.addObject("effect", PotionEffectType.getByName(arg.asElement().asString()));
            }

            else if (!scriptEntry.hasObject("entities")
                    && arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class));

            }

            else arg.reportUnhandled();

        }

        // No targets specified, let's use defaults if available
        scriptEntry.defaultObject("entities", (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity()) : null),
                (((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity()) : null));

        // No potion specified? Problem!
        if (!scriptEntry.hasObject("effect"))
            throw new InvalidArgumentsException("Must specify a valid PotionType!");

        scriptEntry.defaultObject("duration", new Duration(60));
        scriptEntry.defaultObject("amplifier", new Element(1));
        scriptEntry.defaultObject("remove", Element.FALSE);

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        PotionEffectType effect = (PotionEffectType) scriptEntry.getObject("effect");
        int amplifier = scriptEntry.getElement("amplifier").asInt();
        Duration duration = (Duration) scriptEntry.getObject("duration");
        boolean remove = scriptEntry.getElement("remove").asBoolean();

        // Report to dB
        dB.report(scriptEntry, getName(),
                aH.debugObj("Target(s)", entities.toString())
                        + aH.debugObj("Effect", effect.getName())
                        + aH.debugObj("Amplifier", amplifier)
                        + duration.debug());

        // Apply the PotionEffect to the targets!
        for (dEntity entity : entities) {
            if (entity.getLivingEntity().hasPotionEffect(effect))
                entity.getLivingEntity().removePotionEffect(effect);
            if (remove) continue;
            PotionEffect potion = new PotionEffect(effect, duration.getTicksAsInt(), amplifier);
            if (!potion.apply(entity.getLivingEntity()))
                dB.echoError(scriptEntry.getResidingQueue(), "Bukkit was unable to apply '" + potion.getType().getName() + "' to '" + entity.toString() + "'.");
        }
    }
}
