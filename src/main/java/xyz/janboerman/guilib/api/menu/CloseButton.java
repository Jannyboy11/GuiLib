package xyz.janboerman.guilib.api.menu;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import xyz.janboerman.guilib.api.ItemBuilder;

/**
 * A MenuButton that closes the Inventory when clicked.
 */
public class CloseButton extends ItemButton<MenuHolder<?>> {

    /**
     * Creates the close button with a wooden door icon and "Close" as displayname
     */
    public CloseButton() {
        this(Material.OAK_DOOR);
    }

    /**
     * Creates the close button with a custom material and "Close" as displayname
     * @param material the icon material
     */
    public CloseButton(Material material) {
        this(material, "Close");
    }

    /**
     * Creates the close button with a custom material and custom display name
     * @param material the icon material
     * @param displayName the display name
     */
    public CloseButton(Material material, String displayName) {
        super(new ItemBuilder(material).name(displayName).build());
    }

    /**
     * Closes the inventory after one tick.
     * @param holder the MenuHolder
     * @param event the InventoryClickEvent
     */
    @Override
    public final void onClick(MenuHolder holder, InventoryClickEvent event) {
        holder.getPlugin().getServer().getScheduler().runTask(holder.getPlugin(), event.getView()::close);
    }

}
