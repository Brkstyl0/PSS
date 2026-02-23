package me.brkstyl.pss.menus;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class PssMenuHolder implements InventoryHolder {

    public enum MenuType {
        MAIN,
        REPORTS
    }

    private final MenuType type;

    public PssMenuHolder(MenuType type) {
        this.type = type;
    }

    public MenuType getType() {
        return type;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
