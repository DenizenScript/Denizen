package net.aufdemrand.denizen.nms.util;

import org.bukkit.inventory.ItemStack;

public class TradeOffer {

    private ItemStack product;
    private ItemStack firstCost;
    private ItemStack secondCost;
    private boolean usedMaxTimes;
    private int currentUses;
    private int maxUses;

    public TradeOffer(ItemStack product, ItemStack firstCost, ItemStack secondCost,
                      boolean usedMaxTimes, int currentUses, int maxUses) {
        this.product = product;
        this.firstCost = firstCost;
        this.secondCost = secondCost;
        this.usedMaxTimes = usedMaxTimes;
        this.currentUses = currentUses;
        this.maxUses = maxUses;
    }

    public ItemStack getProduct() {
        return product;
    }

    public void setProduct(ItemStack product) {
        this.product = product;
    }

    public ItemStack getFirstCost() {
        return firstCost;
    }

    public void setFirstCost(ItemStack firstCost) {
        this.firstCost = firstCost;
    }

    public boolean hasSecondCost() {
        return secondCost != null;
    }

    public ItemStack getSecondCost() {
        return secondCost;
    }

    public void setSecondCost(ItemStack secondCost) {
        this.secondCost = secondCost;
    }

    public boolean isUsedMaxTimes() {
        return usedMaxTimes;
    }

    public int getCurrentUses() {
        return currentUses;
    }

    public int getMaxUses() {
        return maxUses;
    }
}
