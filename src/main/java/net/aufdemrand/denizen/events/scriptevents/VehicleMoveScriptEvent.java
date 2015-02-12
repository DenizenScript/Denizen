package net.aufdemrand.denizen.events.scriptevents;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizencore.events.ScriptEvent;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import java.util.HashMap;

public class VehicleMoveScriptEvent extends ScriptEvent implements Listener {

    // <--[event]
    // @Events
    // vehicle moves
    // <vehicle> moves
    //
    // @Warning This event fires very very rapidly!
    // @Warning This is a listen-only event: it can't be stopped!
    //
    // @Triggers when a vehicle moves in the slightest.
    // @Context
    // <context.from> returns the location of where the vehicle was.
    // <context.to> returns the location of where the vehicle is.
    //
    // -->

    public VehicleMoveScriptEvent() {
        instance = this;
    }

    public static VehicleMoveScriptEvent instance;

    public dEntity vehicle;

    public dLocation from;

    public dLocation to;

    public VehicleMoveEvent event;

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        String lower = s.toLowerCase();
        return (lower.matches("[^\\s]+ moves.*"));
    }

    @Override
    public boolean matches(ScriptContainer scriptContainer, String s) {
        String lower = s.toLowerCase();
        String ename = vehicle.getEntityType().getLowercaseName();
        String ename2 = vehicle.identifySimple().substring(2);
        String ename3 = vehicle.identifySimpleType();
        return lower.startsWith(ename + " moves")
                || lower.startsWith("vehicle moves")
                || lower.startsWith(ename2 + " moves")
                || lower.startsWith(ename3 + " moves");
    }

    @Override
    public String getName() {
        return "VehicleMoves";
    }

    @Override
    public void init() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    @Override
    public void destroy() {
        VehicleMoveEvent.getHandlerList().unregister(this);
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        return super.applyDetermination(container, determination);
    }

    @Override
    public HashMap<String, dObject> getContext() {
        HashMap<String, dObject> context = super.getContext();
        context.put("from", from);
        context.put("to", to);
        context.put("entity", vehicle);
        return context;
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        // TODO: Cuboids?
        to = new dLocation(event.getTo());
        from = new dLocation(event.getFrom());
        vehicle = new dEntity(event.getVehicle());
        this.event = event;
        fire();
    }
}
