package net.aufdemrand.denizen.scripts.commands.entity;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.npc.traits.InvisibleTrait;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.ArmorStand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InvisibleCommand extends AbstractCommand {

    enum Action {TRUE, FALSE, TOGGLE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (!scriptEntry.hasObject("state")
                    && arg.matchesEnum(Action.values())) {
                scriptEntry.addObject("state", arg.asElement());
            }

            else if (!scriptEntry.hasObject("target")
                    && arg.matches("PLAYER")
                    && ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
                scriptEntry.addObject("target", ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity());
            }

            else if (!scriptEntry.hasObject("target")
                    && arg.matches("NPC")
                    && ((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
                scriptEntry.addObject("target", ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity());
            }

            else if (!scriptEntry.hasObject("target")
                    && arg.matchesArgumentType(dEntity.class)) {
                scriptEntry.addObject("target", arg.asType(dEntity.class));
            }

            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("state")) {
            scriptEntry.addObject("state", new Element("TRUE"));
        }

        if (!scriptEntry.hasObject("target") || !((dEntity) scriptEntry.getdObject("target")).isValid()) {
            throw new InvalidArgumentsException("Must specify a valid target!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Element state = scriptEntry.getElement("state");
        dEntity target = (dEntity) scriptEntry.getObject("target");

        // Report to dB
        dB.report(scriptEntry, getName(), state.debug() + target.debug());

        if (target.isCitizensNPC()) {
            NPC npc = target.getDenizenNPC().getCitizen();
            if (!npc.hasTrait(InvisibleTrait.class)) {
                npc.addTrait(InvisibleTrait.class);
            }
            InvisibleTrait trait = npc.getTrait(InvisibleTrait.class);
            switch (Action.valueOf(state.asString().toUpperCase())) {
                case FALSE:
                    trait.setInvisible(false);
                    break;
                case TRUE:
                    trait.setInvisible(true);
                    break;
                case TOGGLE:
                    trait.toggle();
                    break;
            }
        }
        else {
            switch (Action.valueOf(state.asString().toUpperCase())) {
                case FALSE:
                    if (target.getBukkitEntity() instanceof ArmorStand) {
                        ((ArmorStand) target.getBukkitEntity()).setVisible(true);
                    }
                    else {
                        target.getLivingEntity().removePotionEffect(PotionEffectType.INVISIBILITY);
                    }
                    break;
                case TRUE:
                    if (target.getBukkitEntity() instanceof ArmorStand) {
                        ((ArmorStand) target.getBukkitEntity()).setVisible(false);
                    }
                    else {
                        new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1).apply(target.getLivingEntity());
                    }
                    break;
                case TOGGLE:
                    if (target.getLivingEntity().hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                        target.getLivingEntity().removePotionEffect(PotionEffectType.INVISIBILITY);
                    }
                    else {
                        new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1).apply(target.getLivingEntity());
                    }
                    break;
            }
        }
    }
}
