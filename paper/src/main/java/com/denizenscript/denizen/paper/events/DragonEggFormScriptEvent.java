package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import io.papermc.paper.event.block.DragonEggFormEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DragonEggFormScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // dragon egg forms
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Location true
    //
    // @Triggers when the ender dragon is defeated and the dragon egg forms.
    //
    // @Context
    // <context.entity> returns the EntityTag of the ender dragon right before it's removed.
    // <context.location> returns the LocationTag of the dragon egg.
    // <context.end_portal_location> returns the LocationTag of the end portal.
    // <context.previously_killed> returns an ElementTag(Boolean) of whether the dragon has been previously killed.
    // <context.respawn_phase> returns an ElementTag of the respawn phase. Valid values can be found at <@link url https://jd.papermc.io/paper/1.21.3/org/bukkit/boss/DragonBattle.RespawnPhase.html>.
    // <context.healing_crystals> returns a ListTag(EntityTag) of the healing end crystals.
    // <context.respawn_crystals> returns a ListTag(EntityTag) of the respawn end crystals.
    //
    // -->

    public DragonEggFormScriptEvent() {
        registerCouldMatcher("dragon egg forms");
    }

    public LocationTag location;
    public EntityTag entity;
    public DragonEggFormEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runInCheck(path, location)) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "entity" -> entity;
            case "location" -> location;
            case "end_portal_location" -> new LocationTag(event.getDragonBattle().getEndPortalLocation());
            case "previously_killed" -> new ElementTag(event.getDragonBattle().hasBeenPreviouslyKilled());
            case "respawn_phase" -> new ElementTag(event.getDragonBattle().getRespawnPhase());
            case "healing_crystals" -> new ListTag(event.getDragonBattle().getHealingCrystals(), EntityTag::new);
            case "respawn_crystals" -> new ListTag(event.getDragonBattle().getRespawnCrystals(), EntityTag::new);
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onDragonEggForms(DragonEggFormEvent event) {
        location = new LocationTag(event.getBlock().getLocation());
        entity = new EntityTag(event.getDragonBattle().getEnderDragon());
        this.event = event;
        EntityTag.rememberEntity(entity.getBukkitEntity());
        fire(event);
        EntityTag.forgetEntity(entity.getBukkitEntity());
    }
}
