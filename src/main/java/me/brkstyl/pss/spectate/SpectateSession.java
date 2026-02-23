package me.brkstyl.pss.spectate;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SpectateSession {
    public enum ConversationState {
        NONE,
        WAITING_BAN_DURATION,
        WAITING_BAN_REASON,
        WAITING_WARN_MESSAGE
    }

    private final UUID staffId;
    private final UUID targetId;

    private final ItemStack[] contents;
    private final ItemStack[] armor;
    private final ItemStack offHand;

    private final GameMode previousGameMode;
    private final boolean previousAllowFlight;
    private final boolean previousFlying;
    private final Location previousLocation;

    private ConversationState state = ConversationState.NONE;
    private String pendingBanDuration;

    public SpectateSession(Player staff, Player target) {
        this.staffId = staff.getUniqueId();
        this.targetId = target.getUniqueId();

        this.contents = staff.getInventory().getContents();
        this.armor = staff.getInventory().getArmorContents();
        this.offHand = staff.getInventory().getItemInOffHand();

        this.previousGameMode = staff.getGameMode();
        this.previousAllowFlight = staff.getAllowFlight();
        this.previousFlying = staff.isFlying();
        this.previousLocation = staff.getLocation();
    }

    public UUID getStaffId() {
        return staffId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public ItemStack[] getArmor() {
        return armor;
    }

    public ItemStack getOffHand() {
        return offHand;
    }

    public GameMode getPreviousGameMode() {
        return previousGameMode;
    }

    public boolean wasPreviousAllowFlight() {
        return previousAllowFlight;
    }

    public boolean wasPreviousFlying() {
        return previousFlying;
    }

    public Location getPreviousLocation() {
        return previousLocation;
    }

    public ConversationState getState() {
        return state;
    }

    public void setState(ConversationState state) {
        this.state = state;
    }

    public String getPendingBanDuration() {
        return pendingBanDuration;
    }

    public void setPendingBanDuration(String pendingBanDuration) {
        this.pendingBanDuration = pendingBanDuration;
    }
}
