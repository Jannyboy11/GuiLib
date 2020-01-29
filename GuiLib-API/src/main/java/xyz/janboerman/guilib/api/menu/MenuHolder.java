package xyz.janboerman.guilib.api.menu;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import xyz.janboerman.guilib.api.GuiListener;
import xyz.janboerman.guilib.api.GuiInventoryHolder;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.BiConsumer;

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
 * @see PermissionButton
 * @see TeleportButton
 * @see ChatButton
 * @see ClaimButton
 */
public class MenuHolder<P extends Plugin> extends GuiInventoryHolder<P> implements Iterable<MenuButton<?>> {

    private final MenuButton<?>[] buttons;

    private final LinkedList<WeakReference<ButtonAddCallback>> addButtonCallbacks = new LinkedList<>();
    private final LinkedList<WeakReference<ButtonRemoveCallback>> removeButtonCallbacks = new LinkedList<>();

    /**
     * Creates the MenuHolder with the given InventoryType and title.
     * @param plugin your plugin
     * @param type the inventory type
     * @param title the title
     */
    public MenuHolder(P plugin, InventoryType type, String title) {
        this(GuiListener.getInstance(), plugin, type, title);
    }

    /**
     * Creates the MenuHolder with the given InventoryType and title.
     * @param plugin your plugin
     * @param type the inventory type
     * @param title the title
     * @param guiListener the gui listener that calls the onOpen, onClick and onClose methods
     */
    public MenuHolder(GuiListener guiListener, P plugin, InventoryType type, String title) {
        super(guiListener, plugin, type, title);

        this.buttons = new MenuButton<?>[getInventory().getSize()];
    }

    /**
     * Creates the MenuHolder with the given size and title.
     * @param plugin your plugin
     * @param size the chest size (should be a multiple of 9 and between 9 - 54 (inclusive)
     * @param title the title
     */
    public MenuHolder(P plugin, int size, String title) {
        this(GuiListener.getInstance(), plugin, size, title);
    }

    /**
     * Creates the MenuHolder with the given size and title.
     * @param plugin your plugin
     * @param size the chest size (should be a multiple of 9 and between 9 - 54 (inclusive)
     * @param title the title
     * @param guiListener the gui listener that calls the onOpen, onClick and onClose methods
     */
    public MenuHolder(GuiListener guiListener, P plugin, int size, String title) {
        super(guiListener, plugin, size, title);

        this.buttons = new MenuButton<?>[getInventory().getSize()];
    }

    /**
     * Creates the MenuHolder with the given InventoryType.
     * @param plugin your plugin
     * @param type the inventory type
     */
    public MenuHolder(P plugin, InventoryType type) {
        this(GuiListener.getInstance(), plugin, type);
    }

    /**
     * Creates the MenuHolder with the given InventoryType.
     * @param plugin your plugin
     * @param type the inventory type
     * @param guiListener the gui listener that calls the onOpen, onClick and onClose methods
     */
    public MenuHolder(GuiListener guiListener, P plugin, InventoryType type) {
        super(guiListener, plugin, type);

        this.buttons = new MenuButton<?>[getInventory().getSize()];
    }

    /**
     * Creates the MenuHolder with the given size.
     * @param plugin your plugin
     * @param size the chest size (should be a multiple of 9 and between 9 - 54 (inclusive)
     */
    public MenuHolder(P plugin, int size) {
        this(GuiListener.getInstance(), plugin, size);
    }

    /**
     * Creates the MenuHolder with the given size.
     * @param plugin your plugin
     * @param size the chest size (should be a multiple of 9 and between 9 - 54 (inclusive)
     * @param guiListener the gui listener that calls the onOpen, onClick and onClose methods
     */
    public MenuHolder(GuiListener guiListener, P plugin, int size) {
        super(guiListener, plugin, size);

        this.buttons = new MenuButton<?>[getInventory().getSize()];
    }

    /**
     * Creates the MenuHolder with the given inventory.
     * @param plugin your Plugin
     * @param inventory the Inventory
     * @see xyz.janboerman.guilib.api.GuiInventoryHolder#GuiInventoryHolder(Plugin, Inventory)
     */
    public MenuHolder(P plugin, Inventory inventory) {
        this(GuiListener.getInstance(), plugin, inventory);
    }

    /**
     * Creates the MenuHolder with the given inventory.
     * @param plugin your Plugin
     * @param inventory the Inventory
     * @param guiListener the gui listener that calls the onOpen, onClick and onClose methods
     * @see xyz.janboerman.guilib.api.GuiInventoryHolder#GuiInventoryHolder(GuiListener, Plugin, Inventory)
     */
    public MenuHolder(GuiListener guiListener, P plugin, Inventory inventory) {
        super(guiListener, plugin, inventory);

        this.buttons = new MenuButton<?>[getInventory().getSize()];
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

        getButtonOptionally(event.getSlot()).ifPresent((MenuButton button) -> button.onClick(this, event));
    }

    /**
     * Set a button on a slot.
     * Subclasses that override this method must either call {@link MenuButton#onAdd(MenuHolder, int)} or call super.setButton(slot, button).
     *
     * @param slot the slot number
     * @param button the button
     * @return true if the button could be added to this menu, otherwise false
     */
    public boolean setButton(int slot, MenuButton<?> button) {
        if (!unsetButton(slot)) return false;
        if (button == null) return true;

        MenuButton rawButton = (MenuButton) button;

        var iterator = addButtonCallbacks.iterator();
        while (iterator.hasNext()) {
            var nextReference = iterator.next();
            var nextCallback = nextReference.get();
            if (nextCallback == null) {
                iterator.remove(); //if a callback was garbage collected, remove it from our list
            } else {
                if (!nextCallback.onAdd(slot, button)) return false;
            }
        }

        if (rawButton.onAdd(this, slot)) {
            getInventory().setItem(slot, button.getIcon());
            this.buttons[slot] = button;

            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the button at the given slot.
     * @param slot the slot index
     * @return a button if one is present at the given slot, otherwise null
     */
    public MenuButton<?> getButton(int slot) {
        if (slot < 0 || slot >= buttons.length) return null;

        return this.buttons[slot];
    }

    /**
     * Gets the button at the given slot.
     * @param slot the slot index
     * @return the Optional containing a button if one is present at the given slot, otherwise the empty Optional
     */
    public Optional<MenuButton<?>> getButtonOptionally(int slot) {
        return Optional.ofNullable(getButton(slot));
    }

    /**
     * Get a snapshot of all registered buttons. If no buttons are registered an empty map is returned.
     * @return a new SortedMap containing the buttons
     */
    public SortedMap<Integer, MenuButton<?>> getButtons() {
        var map = new TreeMap<Integer, MenuButton<?>>();
        for (int i = 0; i < this.buttons.length; i++) {
            MenuButton button = this.buttons[i];
            if (button != null) map.put(i, button);
        }
        return map;
    }

    /**
     * Remove a button from a slot.
     * Subclasses that override this method must either call {@link MenuButton#onRemove(MenuHolder, int)} when a button is removed, or call super.unsetButton(slot).
     *
     * @param slot the slot number
     * @return whether a button was removed successfully from the slot
     */
    public boolean unsetButton(int slot) {
        MenuButton menuButton = this.buttons[slot];
        if (menuButton == null) return true;

        var iterator = removeButtonCallbacks.iterator();
        while (iterator.hasNext()) {
            var nextReference = iterator.next();
            var nextCallback = nextReference.get();
            if (nextCallback == null) {
                iterator.remove(); //if a callback was garbage collected, remove it from our list
            } else {
                if (!nextCallback.onRemove(slot, menuButton)) return false;
            }
        }

        if (menuButton.onRemove(this, slot)) {
            this.buttons[slot] = null;
            getInventory().setItem(slot, null);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Removes all buttons from the menu.
     * Subclasses that override this method must either call {@link MenuButton#onRemove(MenuHolder, int)} when a button is removed, or call super.unsetButton(slot).
     */
    public void clearButtons() {
        for (int i = 0; i < this.buttons.length; i++) {
            unsetButton(i);
        }
    }

    /**
     * Get an iterator that iterates over all buttons in this menu.
     * @return a new iterator
     */
    @Override
    public ListIterator<MenuButton<?>> iterator() {
        return new ListIterator<>() {
            int cursor = -1;
            int lastFound = -1;
            int lastReturned = -1;
            int lastUpdated = -1;

            private void advanceTillNextButton() {
                if (cursor == -1) cursor = 0;

                while (lastFound < cursor && cursor < buttons.length) {
                    if (buttons[cursor] != null) {
                        lastFound = cursor;
                        break;
                    }
                    cursor += 1;
                }
            }

            private void advanceTillPreviousButton() {
                if (cursor == buttons.length) cursor -= 1;

                while (cursor >= 0) {
                    if (buttons[cursor] != null) {
                        lastFound = cursor;
                        break;
                    }
                    cursor -= 1;
                }
            }

            @Override
            public boolean hasNext() {
                advanceTillNextButton();
                return cursor < buttons.length;
            }

            @Override
            public MenuButton<?> next() {
                advanceTillNextButton(); //no-op if hasNext has been called before us

                if (cursor == buttons.length) throw new NoSuchElementException();   //there are no elements to explore
                if (lastFound == lastReturned) throw new NoSuchElementException();  //if after advancing till the next button we still didn't find a new one

                cursor += 1; //makes sure that advanceTillNextButton can actually advance again
                return buttons[lastReturned = lastFound];
            }

            @Override
            public boolean hasPrevious() {
                advanceTillPreviousButton();
                return cursor >= 0;
            }

            @Override
            public MenuButton<?> previous() {
                advanceTillPreviousButton();

                if (cursor == -1) throw new NoSuchElementException();               //there ae no more elements to explore
                if (lastFound == lastReturned) throw new NoSuchElementException();  //if after advancing till the previous button we still didn't find a new one

                cursor -= 1; //makes sure that advanceTillPreviousButton can actually advance again
                return buttons[lastReturned = lastFound];
            }

            @Override
            public int nextIndex() {
                advanceTillNextButton();
                return cursor;
            }

            @Override
            public int previousIndex() {
                advanceTillPreviousButton();
                return cursor;
            }

            @Override
            public void remove() {
                if (lastUpdated == lastReturned) throw new IllegalStateException("Need to call next() or previous() first");

                unsetButton(lastUpdated = lastReturned);
            }

            @Override
            public void set(MenuButton<?> menuButton) {
                if (lastUpdated == lastReturned) throw new IllegalStateException("Need to call next() or previous() first");

                setButton(lastUpdated = lastReturned, menuButton);
            }

            @Override
            public void add(MenuButton<?> menuButton) {
                throw new UnsupportedOperationException("Adding is not supported by this ListIterator. Use MenuHolder#setButton instead.");
            }
        };
    }

    /**
     * Perform an action for every button in this menu.
     *
     * This action can set or remove buttons in this menu,
     * meaning that this method will not throw a ConcurrentModificationException when doing so.
     *
     * @param action the action
     */
    public void forEach(BiConsumer<Integer, ? super MenuButton<?>> action) {
        for (int i = 0; i < buttons.length; i++) {
            MenuButton<?> button = buttons[i];
            if (button != null) action.accept(i, button);
        }
    }

    /**
     * Add a callback that is invoked when a button is added to this menu.
     *
     * @param buttonAddListener the callback
     * @see #removeButtonAddCallback(ButtonAddCallback)
     */
    public void addButtonAddCallback(ButtonAddCallback buttonAddListener) {
        if (buttonAddListener == null) return;
        addButtonCallbacks.add(new WeakReference<>(buttonAddListener));
    }

    /**
     * Add a callback that is invoked when a button is removed from this menu.
     *
     * @param buttonRemoveListener the callback
     * @see #removeButtonRemoveCallback(ButtonRemoveCallback)
     */
    public void addButtonRemoveCallback(ButtonRemoveCallback buttonRemoveListener) {
        if (buttonRemoveListener == null) return;
        removeButtonCallbacks.add(new WeakReference<>(buttonRemoveListener));
    }

    /**
     * Remove a callback that is (no longer) invoked when a button is added to this menu.
     *
     * @param buttonAddListener the callback
     */
    public void removeButtonAddCallback(ButtonAddCallback buttonAddListener) {
        Objects.requireNonNull(buttonAddListener, "Button-Add callback cannot be null");
        //need to use removeIf since WeakReference doesn't override equals.
        //this doesn't matter though as the remove operation of LinkedList is O(n) anyway.
        addButtonCallbacks.removeIf(ref -> buttonAddListener.equals(ref.get()));
    }

    /**
     * Remove a callback that is (no longer) invoked when a button is removed from this menu.
     *
     * @param buttonRemoveListener the callback
     */
    public void removeButtonRemoveCallback(ButtonRemoveCallback buttonRemoveListener) {
        Objects.requireNonNull(buttonRemoveListener, "Button-Remove callback cannot be null");
        //need to use removeIf since WeakReference doesn't override equals.
        //this doesn't matter though as the remove operation of LinkedList is O(n) anyway.
        removeButtonCallbacks.removeIf(ref -> buttonRemoveListener.equals(ref.get()));
    }

    /**
     * A callback that - when registered - is invoked when buttons are added to a menu.
     * @see #addButtonAddCallback(ButtonAddCallback)
     */
    @FunctionalInterface
    public static interface ButtonAddCallback {

        public boolean onAdd(int slot, MenuButton<?> button);

    }

    /**
     * A callback that - when registered - is invoked when buttons are removed from a menu.
     * @see #addButtonRemoveCallback(ButtonRemoveCallback)
     */
    @FunctionalInterface
    public static interface ButtonRemoveCallback {

        public boolean onRemove(int slot, MenuButton<?> button);

    }
}
