package xyz.janboerman.guilib.api.menu;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.janboerman.guilib.util.FoliaSupport;

import java.util.Objects;

/**
 * A button that teleports the player to a location when clicked.
 * @param <MH> the menu holder type
 */
public class TeleportButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

    private Location location;

    /**
     * Protected constructor for TeleportButtons that don't have a fixed destination location.
     * Subclasses that use this button must override either {@link #getTo()} or {@link #getTo(MenuHolder, InventoryClickEvent)}.
     * @param icon the icon
     */
    protected TeleportButton(ItemStack icon) {
        super(icon);
    }

    /**
     * Creates the TeleportButton.
     * @param icon the icon
     * @param to the location the player will be teleported to.
     */
    public TeleportButton(ItemStack icon, Location to) {
        super(icon);
        setTo(to);
    }

    /**
     * Teleports the player.
     * @param menuHolder the menu holder
     * @param event the InventoryClickEvent
     */
    @Override
    public void onClick(MH menuHolder, InventoryClickEvent event) {
        HumanEntity player = event.getWhoClicked();
        //I bet that teleporting the player closes the inventory, so I better put this in a task.
        if (FoliaSupport.isFolia()) {
            player.getScheduler().run(menuHolder.getPlugin(), scheduledTask -> player.teleportAsync(getTo(menuHolder, event)), null);
        } else {
            player.getServer().getScheduler().runTask(menuHolder.getPlugin(), () -> player.teleport(getTo(menuHolder, event)));
        }
    }

    /**
     * Set the location to which this button will teleport players.
     * @param to the destination location.
     */
    public void setTo(Location to) {
        this.location = Objects.requireNonNull(to, "Location cannot be null").clone();
    }

    /**
     * Get the location to which the player will be teleported when the button is clicked.
     * Subclasses can override this method to use non-constant locations.
     * The default implementation delegates to {@link #getTo()}.
     *
     * @param menuHolder the menu holder
     * @param event the InventoryClickEvent
     * @return the location to which the player will be teleported.
     */
    protected Location getTo(MH menuHolder, InventoryClickEvent event) {
        return getTo();
    }

    /**
     * Get the location to which the player will be teleported.
     * @return the location
     */
    public Location getTo() {
        return location.clone();
    }

}
