package xyz.janboerman.guilib.api.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import xyz.janboerman.guilib.api.GuiInventoryHolder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A GuiInventoryHolder that only responds to clicks in the top inventory of the {@link InventoryView}.
 * <br>
 * Functionality of clicks is passed to the registered {@link MenuButton}s through their {@link MenuButton#onClick(MenuHolder, InventoryClickEvent)} methods.
 * @param <P> your plugin
 *
 * @see #setButton(int, MenuButton)
 * @see MenuButton
 * @see ItemButton
 * @see RedirectButton
 * @see ToggleButton
 * @see RedirectItemButton
 * @see CloseButton
 */
public class MenuHolder<P extends Plugin> extends GuiInventoryHolder<P> {
    
    private final Map<Integer, MenuButton> buttons = new HashMap<>();

    /**
     * Creates a new MenuHolder for your Plugin with the given InventoryType and title.
     * @param plugin your plugin
     * @param type the inventory type
     * @param title the title
     */
    public MenuHolder(P plugin, InventoryType type, String title) {
        super(plugin, type, title);
    }

    /**
     * Creates a new MenuHolder for your Plugin with the given size and title.
     * @param plugin your plugin
     * @param size the chest size (should be a multiple of 9 and between 9 - 54 (inclusive)
     * @param title the title
     */
    public MenuHolder(P plugin, int size, String title) {
        super(plugin, size, title);
    }

    /**
     * Creates a new MenuHolder for your Plugin with the given InventoryType.
     * @param plugin your plugin
     * @param type the inventory type
     */
    public MenuHolder(P plugin, InventoryType type) {
        super(plugin, type);
    }

    /**
     * Creates a new MenuHolder for your Plugin with the given size.
     * @param plugin your plugin
     * @param size the chest size (should be a multiple of 9 and between 9 - 54 (inclusive)
     */
    public MenuHolder(P plugin, int size) {
        super(plugin, size);
    }

    /**
     * Called by the framework. Delegates the event to a registered button on the slot, if one is present.
     * <p>
     * Subclasses that override this method should always call {@code super.onClick(event);}.
     * @param event the inventory click event
     */
    @Override
    public void onClick(InventoryClickEvent event) {
        //only use the buttons when the top inventory was clicked.
        Inventory clickedInventory = getClickedInventory(event);
        if (clickedInventory == null) return;
        if (clickedInventory.getHolder() != this) return;

        int slot = event.getSlot();
        MenuButton button = buttons.get(slot);
        if (button != null) button.onClick(this, event);
    }

    /**
     * Set a button on a slot.
     * @param slot the slot number
     * @param button the button
     */
    public void setButton(int slot, MenuButton button) {
        getInventory().setItem(slot, button.getIcon());
        this.buttons.put(slot, button);
    }

    /**
     * Remove a button from a slot.
     * @param slot the slot number
     * @return whether a button was removed successfully from the slot
     */
    public boolean unsetButton(int slot) {
        MenuButton button = this.buttons.remove(slot);
        if (button != null) {
            getInventory().setItem(slot, null);
            return true;
        }
        
        return false;
    }

    /**
     * Removes all buttons from the menu.
     */
    public void clearButtons() {
        Iterator<Integer> slotIterator = buttons.keySet().iterator();
        while (slotIterator.hasNext()) {
            int slot = slotIterator.next();
            getInventory().setItem(slot, null);
            slotIterator.remove();            
        }
    }


    private static Inventory getClickedInventory(InventoryClickEvent event) {
        //Adopted from the spigot-api patches
        //See https://hub.spigotmc.org/stash/projects/SPIGOT/repos/spigot/browse/Bukkit-Patches/0010-InventoryClickEvent-getClickedInventory.patch
        int slot = event.getRawSlot();
        if (slot < 0) {
            return null;
        } else {
            InventoryView view = event.getView();
            Inventory topInventory = view.getTopInventory();
            //apparently it is possible that the top inventory is null.
            //does this happen when a player opens his/her own inventory?
            if (topInventory != null && slot < topInventory.getSize()) {
                return topInventory;
            } else {
                return view.getBottomInventory();
            }
        }
    }
    
}
