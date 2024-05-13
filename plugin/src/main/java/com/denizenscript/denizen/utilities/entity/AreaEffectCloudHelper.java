package com.denizenscript.denizen.utilities.entity;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;

// TODO: 1.20.6: PotionData API
public class AreaEffectCloudHelper {
    private AreaEffectCloud entity;

    public AreaEffectCloudHelper(Entity entity) {
        this.entity = (AreaEffectCloud) entity;
    }

    ////////////////
    // Base Potion Data
    /////////

    // TODO: 1.20.6: PotionData API
    private PotionData getBPData() {
        return entity.getBasePotionData();
    }

    public String getBPName() {
        return getBPData().getType().name();
    }

    public boolean getBPUpgraded() {
        return getBPData().isUpgraded();
    }

    public boolean getBPExtended() {
        return getBPData().isExtended();
    }

    public void setBP(PotionType type, boolean extended, boolean upgraded) {
        entity.setBasePotionData(new PotionData(type, extended, upgraded));
    }

    ////////////////
    // Particles
    /////////

    public Color getColor() {
        return entity.getColor();
    }

    public void setColor(Color color) {
        entity.setColor(color);
    }

    public String getParticle() {
        return entity.getParticle().name();
    }

    public void setParticle(String name) {
        Particle particle = Particle.valueOf(name);
        if (particle != null) {
            entity.setParticle(particle);
        }
    }

    ////////////////
    // Radius
    /////////

    public float getRadius() {
        return entity.getRadius();
    }

    public float getRadiusOnUse() {
        return entity.getRadiusOnUse();
    }

    public float getRadiusPerTick() {
        return entity.getRadiusPerTick();
    }

    public void setRadius(float radius) {
        entity.setRadius(radius);
    }

    public void setRadiusOnUse(float radius) {
        entity.setRadiusOnUse(radius);
    }

    public void setRadiusPerTick(float radius) {
        entity.setRadiusPerTick(radius);
    }

    ////////////////
    // Duration
    /////////

    public long getDuration() {
        return (long) entity.getDuration();
    }

    public long getDurationOnUse() {
        return (long) entity.getDurationOnUse();
    }

    public long getReappDelay() {
        return (long) entity.getReapplicationDelay();
    }

    public long getWaitTime() {
        return (long) entity.getWaitTime();
    }

    public void setDuration(int ticks) {
        entity.setDuration(ticks);
    }

    public void setDurationOnUse(int ticks) {
        entity.setDurationOnUse(ticks);
    }

    public void setReappDelay(int ticks) {
        entity.setReapplicationDelay(ticks);
    }

    public void setWaitTime(int ticks) {
        entity.setWaitTime(ticks);
    }

    ////////////////
    // Custom Effects
    /////////

    public List<PotionEffect> getCustomEffects() {
        return entity.getCustomEffects();
    }

    public boolean hasCustomEffects() {
        return entity.hasCustomEffects();
    }

    public void clearEffects() {
        entity.clearCustomEffects();
    }

    public void removeEffect(PotionEffectType type) {
        entity.removeCustomEffect(type);
    }

    public void addEffect(PotionEffect effect, boolean override) {
        entity.addCustomEffect(effect, override);
    }

    ////////////////
    // Misc
    /////////

    public ProjectileSource getSource() {
        return entity.getSource();
    }

    public void setSource(ProjectileSource source) {
        entity.setSource(source);
    }
}
