package net.aufdemrand.denizen.nms.impl.effects;

import net.aufdemrand.denizen.nms.interfaces.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Effect_v1_13_R2 implements Effect {

    private org.bukkit.Effect effect;

    public Effect_v1_13_R2(org.bukkit.Effect effect) {
        this.effect = effect;
    }

    @Override
    public void play(Location location, int data, int radius) {
        location.getWorld().playEffect(location, effect, data, radius);
    }

    @Override
    public void playFor(Player player, Location location, int data) {
        player.playEffect(location, effect, data); // TODO: 1.13
    }

    @Override
    public boolean isVisual() {
        return effect.getType() == org.bukkit.Effect.Type.VISUAL;
    }

    @Override
    public String getName() {
        return effect.name();
    }
}
