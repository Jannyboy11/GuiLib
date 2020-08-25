package xyz.janboerman.guilib.api.menu;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * A button that sends a plugin message to the player when clicked.
 * Note that the sendingPlugin still needs to be registered using {@link Server#getMessenger()}.
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

    /**
     * Commands that can be sent to BungeeCord.
     * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/">BungeeCord plugin messaging channel wiki</a>
     */
    public static class BungeeCord {
        private static final String BungeeCord = "BungeeCord";
        /**
         * Can be used instead of a server name for
         * {@link #PlayerCount(ItemStack, Plugin, String)},
         * {@link #PlayerList(ItemStack, Plugin, String)},
         * {@link #Forward(ItemStack, Plugin, String, String, byte[])}
         * and instead of a player name for
         * {@link #Message(ItemStack, Plugin, String, String)},
         * {@link #MessageRaw(ItemStack, Plugin, String, String)}.
         */
        public static final String ALL = "ALL";
        /**
         * Can be used instead of a server name for
         * {@link #Forward(ItemStack, Plugin, String, String, byte[])}
         */
        public static final String ONLINE = "ONLINE";
        private static final String Connect = "Connect";
        private static final String ConnectOther = "ConnectOther";
        private static final String IP = "IP";
        private static final String IPOther = "IPOther";
        private static final String PlayerCount = "PlayerCount";
        private static final String PlayerList = "PlayerList";
        private static final String GetServers = "GetServers";
        private static final String Message = "Message";
        private static final String MessageRaw = "MessageRaw";
        private static final String GetServer = "GetServer";
        private static final String Forward = "Forward";
        private static final String ForwardToPlayer = "ForwardToPlayer";
        private static final String UUID = "UUID";
        private static final String UUIDOther = "UUIDOther";
        private static final String ServerIP = "ServerIP";
        private static final String KickPlayer = "KickPlayer";

        /**
         * Creates a PluginMessageButton that will send the player to the said server.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @param toServer the name of the server as known to BungeeCord
         * @return a new PluginMessageButton
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#connect">Connect</a>
         */
        public static PluginMessageButton<?> Connect(ItemStack icon, Plugin sender, String toServer) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(Connect);
            out.writeUTF(toServer);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will connect another player to a said server.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @param playerName the name of the Player to be connected
         * @param toServer the server to which the player will be connected
         * @return a new PluginMessageButton
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#connectother">ConnectOther</a>
         */
        public static PluginMessageButton<?> ConnectOther(ItemStack icon, Plugin sender, String playerName, String toServer) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(ConnectOther);
            out.writeUTF(playerName);
            out.writeUTF(toServer);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will request the IP address of the player.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @return a new PluginMessageButton
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#ip">IP</a>
         */
        public static PluginMessageButton<?> IP(ItemStack icon, Plugin sender) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(IP);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will request the IP address of the given player.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @param playerName the name of the Player whose IP address will be requested
         * @return a new PluginMessageButton
         */
        public static PluginMessageButton<?> IPOther(ItemStack icon, Plugin sender, String playerName) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(IPOther);
            out.writeUTF(playerName);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will request the player count on a certain server, or the global player count.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @param onServer the name of the server known to BungeeCord. Use "ALL" to request the global player count.
         * @return a new PluginMessageButton
         * @see #ALL
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#playercount">PlayerCount</a>
         */
        public static PluginMessageButton<?> PlayerCount(ItemStack icon, Plugin sender, String onServer) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(PlayerCount);
            out.writeUTF(onServer);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will request the player list on a certain server, or the global player list.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @param onServer the name of the server known to BungeeCord. Use "All" to request the global player list.
         * @return a new PluginMessageButton
         * @see #ALL
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#playerlist">PlayerList</a>
         */
        public static PluginMessageButton<?> PlayerList(ItemStack icon, Plugin sender, String onServer) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(PlayerList);
            out.writeUTF(onServer);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will request the global server list as defined in BungeeCord's config.yml.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @return a new PluginMessageButton
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#getservers">GetServers</a>
         */
        public static PluginMessageButton<?> GetServers(ItemStack icon, Plugin sender) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(GetServers);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will send a chat message to a player.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @param playerName the player who will receive the message. Use "ALL" if the message is meant for every player.
         * @param message the chat message
         * @return a new PluginMessageButton
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#message">Message</a>
         * @see #ALL
         */
        public static PluginMessageButton<?> Message(ItemStack icon, Plugin sender, String playerName, String message) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(Message);
            out.writeUTF(playerName);
            out.writeUTF(message);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will send a raw message (as used by /tellraw) to a player, or all players.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @param playerName the player who will receive the message. Use "ALL" if the message is meant for every player.
         * @param rawMessage the message - represented as a serialised chat component
         * @return a new PluginMessageButton
         * @see #ALL
         */
        public static PluginMessageButton<?> MessageRaw(ItemStack icon, Plugin sender, String playerName, String rawMessage) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(MessageRaw);
            out.writeUTF(playerName);
            out.writeUTF(rawMessage);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will request the global the server name that the player is connected to, as defined in BungeeCord's config.yml.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @return a new PluginMessageButton
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#getserver">GetServer</a>
         */
        public static PluginMessageButton<?> GetServer(ItemStack icon, Plugin sender) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(GetServer);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will forward a plugin message to a certain server, all online servers, or all servers.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @param server the server to forward the plugin message to. Use or "ALL" or "ONLINE" to send the message to multiple servers.
         * @param channel the channel on which to send the plugin message (as of Minecraft 1.13 this needs to be formatted as "PluginName:Channel")
         * @param pluginMessage the plugin message
         * @return a new PluginMessageButton
         * @see #ALL
         * @see #ONLINE
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#forward">Forward</a>
         */
        public static PluginMessageButton<?> Forward(ItemStack icon, Plugin sender, String server, String channel, byte[] pluginMessage) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(Forward);
            out.writeUTF(server);
            out.writeUTF(channel);
            out.writeShort(pluginMessage.length);
            out.write(pluginMessage);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will forward a plugin message to a certain player.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @param playerName the name of the player who will receive the plugin message
         * @param channel the channel on which to send the plugin message (as of Minecraft 1.13 this needs to be formatted as "PluginName:Channel")
         * @param pluginMessage the plugin message
         * @return a new PluginMessageButton
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#forwardtoplayer">ForwardToPlayer</a>
         */
        public static PluginMessageButton<?> ForwardToPlayer(ItemStack icon, Plugin sender, String playerName, String channel, byte[] pluginMessage) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(ForwardToPlayer);
            out.writeUTF(playerName);
            out.writeUTF(channel);
            out.writeShort(pluginMessage.length);
            out.write(pluginMessage);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will request the player's UUID.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @return a new PluginMessageButton
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#uuid">UUID</a>
         */
        public static PluginMessageButton<?> UUID(ItemStack icon, Plugin sender) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(UUID);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will request a certain's player UUID.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @param playerName the name of the player
         * @return a new PluginMessageButton
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#uuidother">UUIDOther</a>
         */
        public static PluginMessageButton<?>UUIDOther(ItemStack icon, Plugin sender, String playerName) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(UUIDOther);
            out.writeUTF(playerName);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a PluginMessageButton that will request the IP address of a server on this proxy.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @param ofServer the name of the server
         * @return a new PluginMessageButton
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#serverip">ServerIP</a>
         */
        public static PluginMessageButton<?>ServerIP(ItemStack icon, Plugin sender, String ofServer) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(ServerIP);
            out.writeUTF(ofServer);
            return new PluginMessageButton<>(icon, sender, BungeeCord, out.toByteArray());
        }

        /**
         * Creates a button what will kick a player from the proxy.
         * @param icon the icon of the button
         * @param sender the sending plugin
         * @param playerName the name of the player to be kicked
         * @param reason the kick reason
         * @return a new PluginMessageButton
         * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#kickplayer">KickPlayer</a>
         */
        public static PluginMessageButton<?>KickPlayer(ItemStack icon, Plugin sender, String playerName, String reason) {
            var out = ByteStreams.newDataOutput();
            out.writeUTF(KickPlayer);
            out.writeUTF(playerName);
            out.writeUTF(reason);
            return new PluginMessageButton(icon, sender, BungeeCord, out.toByteArray());
        }
    }

}
