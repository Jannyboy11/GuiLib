package xyz.janboerman.guilib.api.menu;

import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.janboerman.guilib.api.GuiListener;
import xyz.janboerman.guilib.api.ItemBuilder;

import java.util.function.Consumer;

/**
 * A menu that prompts the player to confirm or cancel an action.
 * This menu closes when the yes-button or no-button is clicked.
 * If the action to be performed is null, nothing will happen.
 * @param <P> your plugin's type
 */
public class YesNoMenu<P extends Plugin> extends MenuHolder<P> {

    private static final String ARE_YOU_SURE = "Are you sure?";
    private static final ItemStack YES_STACK = new ItemBuilder(Material.LIME_CONCRETE).name("Yes - continue").build();
    private static final ItemStack NO_STACK = new ItemBuilder(Material.RED_CONCRETE).name("No - cancel").build();

    /**
     * Callbacks for the yes-button and no-button
     */
    protected Consumer<InventoryClickEvent> yesAction, noAction;

    /**
     * Create the YesNoMenu.
     * @param guiListener the GuiListener
     * @param plugin the plugin
     * @param type the inventory's type
     * @param title the inventory's title
     * @param yesAction the action to perform when the yes-button is clicked
     * @param noAction the action to perform when the no-button is clicked
     */
    public YesNoMenu(GuiListener guiListener, P plugin, InventoryType type, String title, Consumer<InventoryClickEvent> yesAction, Consumer<InventoryClickEvent> noAction) {
        super(guiListener, plugin, type, title);

        this.yesAction = yesAction;
        this.noAction = noAction;
        setupButtons();
    }

    /**
     * Create the YesNoMenu.
     * @param guiListener the GuiListener
     * @param plugin the plugin
     * @param size the inventory's size
     * @param title the inventory's title
     * @param yesAction the action to perform when the yes-button is clicked
     * @param noAction the action to perform when the no-button is clicked
     */
    public YesNoMenu(GuiListener guiListener, P plugin, int size, String title, Consumer<InventoryClickEvent> yesAction, Consumer<InventoryClickEvent> noAction) {
        super(guiListener, plugin, size, title);

        this.yesAction = yesAction;
        this.noAction = noAction;
        setupButtons();
    }

    /**
     * Create the YesNoMenu.
     * @param plugin the plugin
     * @param type the inventory's type
     * @param title the inventory's title
     * @param yesAction the action to perform when the yes-button is clicked
     * @param noAction the action to perform when the no-button is clicked
     */
    public YesNoMenu(P plugin, InventoryType type, String title, Consumer<InventoryClickEvent> yesAction, Consumer<InventoryClickEvent> noAction) {
        super(plugin, type, title);

        this.yesAction = yesAction;
        this.noAction = noAction;
        setupButtons();
    }

    /**
     * Create the YesNoMenu.
     * @param plugin the plugin
     * @param size the inventory's size
     * @param title the inventory's title
     * @param yesAction the action to perform when the yes-button is clicked
     * @param noAction the action to perform when the no-button is clicked
     */
    public YesNoMenu(P plugin, int size, String title, Consumer<InventoryClickEvent> yesAction, Consumer<InventoryClickEvent> noAction) {
        super(plugin, size, title);

        this.yesAction = yesAction;
        this.noAction = noAction;
        setupButtons();
    }

    /**
     * Creates the YesNoMenu.
     * @param plugin the plugin
     * @param inventory the inventory
     * @param yesAction the action to perform when the yes-button is clicked
     * @param noAction the action to perform when the no-button is clicked
     * @see xyz.janboerman.guilib.api.GuiInventoryHolder#GuiInventoryHolder(Plugin, Inventory)
     */
    public YesNoMenu(P plugin, Inventory inventory, Consumer<InventoryClickEvent> yesAction, Consumer<InventoryClickEvent> noAction) {
        super(plugin, inventory);

        this.yesAction = yesAction;
        this.noAction = noAction;
        setupButtons();
    }

    /**
     * Creates the YesNoMenu.
     * @param plugin the plugin
     * @param guiListener the GuiListener
     * @param inventory the inventory
     * @param yesAction the action to perform when the yes-button is clicked
     * @param noAction the action to perform when the no-button is clicked
     * @see xyz.janboerman.guilib.api.GuiInventoryHolder#GuiInventoryHolder(GuiListener, Plugin, Inventory)
     */
    public YesNoMenu(P plugin, GuiListener guiListener, Inventory inventory, Consumer<InventoryClickEvent> yesAction, Consumer<InventoryClickEvent> noAction) {
        super(guiListener, plugin, inventory);

        this.yesAction = yesAction;
        this.noAction = noAction;
        setupButtons();
    }

    /**
     * Creates the YesNoMenu.
     * @param plugin the plugin
     * @param title the title of the inventory
     * @param yesAction the action to perform when the yes-button is clicked
     * @param noAction the action to perform when the no-button is clicked
     */
    public YesNoMenu(P plugin, String title, Consumer<InventoryClickEvent> yesAction, Consumer<InventoryClickEvent> noAction) {
        this(plugin, InventoryType.HOPPER, title, yesAction, noAction);
    }

    /**
     * Creates the YesNoMenu.
     * @param plugin the plugin
     * @param guiListener the GuiListener
     * @param title the title of the inventory
     * @param yesAction the action to perform when the yes-button is clicked
     * @param noAction the action to perform when the no-button is clicked
     */
    public YesNoMenu(GuiListener guiListener, P plugin, String title, Consumer<InventoryClickEvent> yesAction, Consumer<InventoryClickEvent> noAction) {
        this(guiListener, plugin, InventoryType.HOPPER, title, yesAction, noAction);
    }

    /**
     * Creates the YesNoMenu. The inventory title defaults to {@code "Are you sure?"}.
     * @param plugin the plugin
     * @param yesAction the action to perform when the yes-button is clicked
     * @param noAction the action to perform when the no-button is clicked
     */
    public YesNoMenu(P plugin, Consumer<InventoryClickEvent> yesAction, Consumer<InventoryClickEvent> noAction) {
        this(plugin, InventoryType.HOPPER, ARE_YOU_SURE, yesAction, noAction);
    }

    /**
     * Creates the YesNoMenu. The inventory title defaults to {@code "Are you sure?"}.
     * @param guiListener the GuiListener
     * @param plugin the plugin
     * @param yesAction the action to perform when the yes-button is clicked
     * @param noAction the action to perform when the no-button is clicked
     */
    public YesNoMenu(GuiListener guiListener, P plugin, Consumer<InventoryClickEvent> yesAction, Consumer<InventoryClickEvent> noAction) {
        this(guiListener, plugin, ARE_YOU_SURE, yesAction, noAction);
    }

    /**
     * Called from the constructor. Override to customize the buttons.
     */
    protected void setupButtons() {
        setButton(0, makeButton(true));
        setButton(getInventory().getSize() - 1, makeButton(false));
    }

    /**
     * Helper method that assists in creating buttons. Override to customize the creation of buttons.
     * @param yesOrNo - true to create the yes-button, false to create the no-button
     * @return a new button
     */
    protected MenuButton<YesNoMenu<P>> makeButton(boolean yesOrNo) {
        ItemStack stack = yesOrNo ? YES_STACK : NO_STACK;
        Consumer<InventoryClickEvent> action = yesOrNo ? yesAction : noAction;

        return new ItemButton<>(stack) {
            @Override
            public void onClick(YesNoMenu<P> holder, InventoryClickEvent event) {
                getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
                    event.getView().close();
                    if (action != null) {
                        action.accept(event);
                    }
                });
            }
        };
    }

}
