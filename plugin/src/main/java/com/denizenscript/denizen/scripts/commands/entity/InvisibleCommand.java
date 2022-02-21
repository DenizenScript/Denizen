package com.denizenscript.denizen.scripts.commands.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.npc.traits.InvisibleTrait;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.ArmorStand;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class InvisibleCommand extends AbstractCommand {

    public InvisibleCommand() {
        setName("invisible");
        setSyntax("invisible [<entity>] (state:true/false/toggle)");
        setRequiredArguments(1, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name Invisible
    // @Syntax invisible [<entity>] (state:true/false/toggle)
    // @Required 1
    // @Maximum 2
    // @Short Makes an NPC or entity go invisible
    // @Group entity
    //
    // @Description
    // For non-armor stand entities, applies a maximum duration invisibility potion.
    // For armor stands, toggles them invisible.
    // Applies the 'invisible' trait to NPCs.
    //
    // NPCs can't be made invisible if not added to the playerlist.
    // (The invisible trait adds the NPC to the playerlist when set)
    // See <@link language invisible trait>)
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to makes the player invisible.
    // - invisible <player> state:true
    //
    // @Usage
    // Use to make the attached NPC visible if previously invisible, and invisible if not
    // - invisible <npc> state:toggle
    // -->

    enum Action {TRUE, FALSE, TOGGLE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("state")
                    && arg.matchesEnum(Action.class)) {
                scriptEntry.addObject("state", arg.asElement());
            }
            else if (!scriptEntry.hasObject("target")
                    && arg.matches("player")
                    && Utilities.entryHasPlayer(scriptEntry)) {
                scriptEntry.addObject("target", Utilities.getEntryPlayer(scriptEntry).getDenizenEntity());
            }
            else if (!scriptEntry.hasObject("target")
                    && arg.matches("npc")
                    && Utilities.entryHasNPC(scriptEntry)) {
                scriptEntry.addObject("target", Utilities.getEntryNPC(scriptEntry).getDenizenEntity());
            }
            else if (!scriptEntry.hasObject("target")
                    && arg.matchesArgumentType(EntityTag.class)) {
                scriptEntry.addObject("target", arg.asType(EntityTag.class));
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("state")) {
            scriptEntry.addObject("state", new ElementTag("TRUE"));
        }
        if (!scriptEntry.hasObject("target") || !((EntityTag) scriptEntry.getObjectTag("target")).isValid()) {
            throw new InvalidArgumentsException("Must specify a valid target!");
        }
    }

    public void setInvisible(EntityTag entity, boolean visible) {
        if (entity.getBukkitEntity() instanceof ArmorStand) {
            ((ArmorStand) entity.getBukkitEntity()).setVisible(visible);
        }
        else if (entity.isLivingEntity() && !entity.isFake) {
            if (visible) {
                entity.getLivingEntity().removePotionEffect(PotionEffectType.INVISIBILITY);
            }
            else {
                new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1).apply(entity.getLivingEntity());
            }
        }
        else {
            NMSHandler.getEntityHelper().setInvisible(entity.getBukkitEntity(), !visible);
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag state = scriptEntry.getElement("state");
        EntityTag target = scriptEntry.getObjectTag("target");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), state, target);
        }
        if (target.isCitizensNPC()) {
            NPC npc = target.getDenizenNPC().getCitizen();
            if (!npc.hasTrait(InvisibleTrait.class)) {
                npc.addTrait(InvisibleTrait.class);
            }
            InvisibleTrait trait = npc.getOrAddTrait(InvisibleTrait.class);
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
                    setInvisible(target, true);
                    break;
                case TRUE:
                    setInvisible(target, false);
                    break;
                case TOGGLE:
                    if (target.getBukkitEntity() instanceof ArmorStand) {
                        setInvisible(target, !((ArmorStand) target.getBukkitEntity()).isVisible());
                    }
                    else {
                        setInvisible(target, target.getLivingEntity().hasPotionEffect(PotionEffectType.INVISIBILITY));
                    }
                    break;
            }
        }
    }
}
