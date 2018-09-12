package xyz.janboerman.guilib.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Objects;

/**
 * A button that sends a plugin message to the player when clicked.
 * @param <MH> the menu holder type.
 */
public class PluginMessageButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

    private Plugin sendingPlugin;
    private String channel;
    private byte[] pluginMessage;

    /**
     * Protected constructor for PluginMessageButtons without a constant plugin message.
     * Subclasses that use this constructor must override {@link #getSendingPlugin()}, {@link #getChannel(MenuHolder, InventoryClickEvent)}
     * and {@link #getPluginMessage(MenuHolder, InventoryClickEvent)}, or their corresponding no-arg equivalents.
     * @param icon the icon of the buoon
     */
    protected PluginMessageButton(ItemStack icon) {
        super(icon);
    }

    /**
     * Protected constructor for PluginMessageButtons without a constant plugin message.
     * Subclasses that use this constructor must override either {@link #getPluginMessage()} or {@link #getPluginMessage(MenuHolder, InventoryClickEvent)}.
     * @param icon the icon of the button
     * @param sendingPlugin the plugin that sends the plugin message
     * @param channel the channel on which the plugin message is sent
     */
    protected PluginMessageButton(ItemStack icon, Plugin sendingPlugin, String channel) {
        super(icon);
        setSendingPlugin(sendingPlugin);
        setChannel(channel);
    }

    /**
     * Creates the PluginMessageButton.
     * @param icon the icon of the button
     * @param plugin the plugin that sends the plugin message
     * @param channel the channel on which the plugin message is sent
     * @param pluginMessage the plugin message
     */
    public PluginMessageButton(ItemStack icon, Plugin plugin, String channel, byte[] pluginMessage) {
        this(icon, plugin, channel);
        setPluginMessage(pluginMessage);
    }

    /**
     * Sends the plugin message to the player.
     * @param menuHolder the menu holder
     * @param event the InventoryClickEvent
     */
    @Override
    public void onClick(MH menuHolder, InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            player.sendPluginMessage(getSendingPlugin(menuHolder, event),
                    getChannel(menuHolder, event),
                    getPluginMessage(menuHolder, event));
        }
    }

    /**
     * Set the plugin message
     * @param pluginMessage the plugin message
     */
    public void setPluginMessage(byte[] pluginMessage) {
        this.pluginMessage = Arrays.copyOf(Objects.requireNonNull(pluginMessage, "PluginMessage cannot be null"), pluginMessage.length);
    }

    /**
     * Set the plugin that sends the plugin messages.
     * @param sendingPlugin the sending plugin
     */
    public void setSendingPlugin(Plugin sendingPlugin) {
        this.sendingPlugin = Objects.requireNonNull(sendingPlugin, "Plugin cannot be null");
    }

    /**
     * Set the channel over which plugin messages are sent.
     * @param channel the channel
     */
    public void setChannel(String channel) {
        this.channel = Objects.requireNonNull(channel, "Channel cannot be null");
    }

    /**
     * Get the plugin message.
     * @param menuHolder the menu holder
     * @param event the InventoryClickEvent
     * @return the plugin message
     */
    protected byte[] getPluginMessage(MH menuHolder, InventoryClickEvent event) {
        return getPluginMessage();
    }

    /**
     * Compute the sending plugin for non-constant plugin messages.
     * @param menuHolder the menu holder
     * @param event the InventoryClickEvent
     * @return the plugin that sends the plugin message.
     */
    protected Plugin getSendingPlugin(MH menuHolder, InventoryClickEvent event) {
        return getSendingPlugin();
    }

    /**
     * Computes the channel for non-constant plugin messages.
     * @param menuHolder the menu holder
     * @param event the InventoryClickEvent
     * @return the channel
     */
    protected String getChannel(MH menuHolder, InventoryClickEvent event) {
        return getChannel();
    }

    /**
     * Get the plugin message.
     * @return the plugin message
     */
    public byte[] getPluginMessage() {
        return Arrays.copyOf(pluginMessage, pluginMessage.length);
    }

    /**
     * Get the plugin that sends the plugin message.
     * @return the plugin
     */
    public Plugin getSendingPlugin() {
        return sendingPlugin;
    }

    /**
     * Get the channel over which plugin messages are sent.
     * @return the channel
     */
    public String getChannel() {
        return channel;
    }

}
