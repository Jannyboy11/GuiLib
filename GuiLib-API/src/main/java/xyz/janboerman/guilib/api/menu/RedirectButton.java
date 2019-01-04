package xyz.janboerman.guilib.api.menu;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public interface RedirectButton<MH extends MenuHolder<?>> extends MenuButton<MH> {

    /**
     * Callback that is invoked when the button is clicked.
     * After one tick the player's inventory will close and the inventory supplied by
     * {@link #to(MenuHolder, InventoryClickEvent)} will be opened.
     *
     * @param holder the MenuHolder
     * @param event the InventoryClickEvent
     */
    @Override
    public default void onClick(MH holder, InventoryClickEvent event) {
        holder.getPlugin().getServer().getScheduler().runTask(holder.getPlugin(), () -> {
           event.getView().close();
           
           HumanEntity player = event.getWhoClicked();
           Inventory to = to(holder, event);
           if (to != null) player.openInventory(to);
        });
    }

    /**
     * Get the inventory to which this button redirects.
     *
     * @param MenuHolder the MenuHolder
     * @param event the InventoryClickEvent
     * @return the inventory
     */
    public Inventory to(MH MenuHolder, InventoryClickEvent event);

}
