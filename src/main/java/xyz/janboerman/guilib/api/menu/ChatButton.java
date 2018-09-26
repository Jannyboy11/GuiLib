package xyz.janboerman.guilib.api.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import xyz.janboerman.guilib.api.ItemBuilder;

import java.util.Objects;

/**
 * A button that makes the player who clicks it say a message in chat.
 *
 * @param <MH> the menu holder type
 */
public class ChatButton<MH extends MenuHolder<?>> extends ItemButton<MH> {

    private String message;

    /**
     * Protected constructor for ChatButtons that wish to use non-constant chat messages.
     * Subclasses that use this constructor must override either {@link #getMessage()} or {@link #getMessage(MenuHolder, InventoryClickEvent)}.
     * @param icon the icon
     */
    protected ChatButton(ItemStack icon) {
        super(icon);
    }

    /**
     * Creates the ChatButton.
     *
     * @param icon the icon
     * @param message the chat message
     */
    public ChatButton(ItemStack icon, String message) {
        super(icon);
        setMessage(message);
    }

    /**
     * Creates the ChatButton. The display name of the icon will be set to the message.
     *
     * @param material the icon material
     * @param message the chat message
     */
    public ChatButton(Material material, String message) {
        this(new ItemBuilder(material).name(message).build(), message);
    }

    /**
     * Makes the player who clicked the button say the message in chat.
     *
     * @param menuHolder the menu holder
     * @param event the InventoryClickEvent
     */
    @Override
    public void onClick(MH menuHolder, InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            player.chat(getMessage(menuHolder, event));
        }
    }

    /**
     * Set the chat message.
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = Objects.requireNonNull(message, "Message cannot be null");
    }

    /**
     * Compute the chat message that will be sent by {@link #onClick(MenuHolder, InventoryClickEvent)}.
     * Subclasses can override this method for chat messages that are not constant.
     * The default implementation delegates to {@link #getMessage()}.
     *
     * @param menuHolder the menu holder
     * @param event the InventoryClickEvent
     * @return the customized message
     */
    protected String getMessage(MH menuHolder, InventoryClickEvent event) {
        return getMessage();
    }

    /**
     * Get the chat message.
     * @return the message
     */
    public String getMessage() {
        return message;
    }
}
