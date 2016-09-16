package net.aufdemrand.denizen.listeners;


import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.interfaces.RegistrationableInstance;
import org.bukkit.Bukkit;

public abstract class AbstractListenerType implements RegistrationableInstance {

    Denizen denizen;
    String name;
    Class<? extends AbstractListener> instanceClass;

    @Override
    public AbstractListenerType activate() {
        this.denizen = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
        return this;
    }

    @Override
    public AbstractListenerType as(String type) {
        name = type.toUpperCase();
        denizen.getListenerRegistry()
                .register(this.name, this);
        onEnable();
        return this;
    }

    public AbstractListener createInstance(dPlayer player, String listenerId) {
        try {
            denizen.getListenerRegistry().addListenerFor(player, instanceClass.newInstance(), listenerId);
            return denizen.getListenerRegistry().getListenerFor(player, listenerId);
        }
        catch (InstantiationException e) {
            dB.echoError(e);
        }
        catch (IllegalAccessException e) {
            dB.echoError(e);
        }
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Part of the Plugin disable sequence.
     * <p/>
     * Can be '@Override'n by a Listener which requires a method when bukkit sends a
     * onDisable() to Denizen. (ie. Server shuts down or restarts)
     */
    public void onDisable() {

    }

    public AbstractListenerType withClass(Class<? extends AbstractListener> listenerInstanceClass) {
        this.instanceClass = listenerInstanceClass;
        return null;
    }
}


