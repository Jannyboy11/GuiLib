package xyz.janboerman.guilib;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.janboerman.guilib.api.GuiInventoryHolder;

public class GuiLibrary extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof GuiInventoryHolder) {
            GuiInventoryHolder guiHolder = (GuiInventoryHolder) event.getInventory().getHolder();
            getServer().getPluginManager().registerEvents(guiHolder.guiListener, this);
            guiHolder.onOpen(event);
        }
    }

}
