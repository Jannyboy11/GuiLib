package xyz.janboerman.guilib.api.menu;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.janboerman.guilib.api.ItemBuilder;
import xyz.janboerman.guilib.util.Scheduler;

/**
 * A MenuButton that closes the Inventory when clicked.
 */
public class CloseButton<P extends Plugin> extends ItemButton<MenuHolder<P>> {

    /**
     * Creates the close button with an oak door icon and "Close" as the display name.
     */
    public CloseButton() {
        this(Material.OAK_DOOR);
    }

    /**
     * Creates the close button with a custom material and "Close" as the display name.
     * @param material the icon material
     */
    public CloseButton(Material material) {
        this(material, "Close");
    }

    /**
     * Creates the close button with the custom icon.
     * @param icon the icon
     */
    public CloseButton(ItemStack icon) {
        super(icon);
    }

    /**
     * Creates the close button with a custom material and custom display name.
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
    public final void onClick(MenuHolder<P> holder, InventoryClickEvent event) {
        Scheduler.get().runTaskLater(holder.getPlugin(), event.getWhoClicked(), event.getView()::close);
    }

}
