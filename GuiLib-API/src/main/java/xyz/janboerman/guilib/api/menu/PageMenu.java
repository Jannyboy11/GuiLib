package xyz.janboerman.guilib.api.menu;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.janboerman.guilib.api.GuiListener;
import xyz.janboerman.guilib.api.GuiInventoryHolder;
import xyz.janboerman.guilib.api.ItemBuilder;
import xyz.janboerman.guilib.util.CachedSupplier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A menu that implements pages. This menu by default only has two buttons - on the bottom row of the top inventory.
 * The pages themselves can therefore not be larger than 45 slots.
 * @param <P> your plugin type
 * @see ResetButton
 */
public class PageMenu<P extends Plugin> extends MenuHolder<P> implements MenuHolder.ButtonAddCallback, MenuHolder.ButtonRemoveCallback {

    private static final ItemStack DEFAULT_PREVIOUS_PAGE_BUTTON = new ItemBuilder(Material.MAGENTA_GLAZED_TERRACOTTA).name("Previous").build();
    private static final ItemStack DEFAULT_NEXT_PAGE_BUTTON = new ItemBuilder(Material.MAGENTA_GLAZED_TERRACOTTA).name("Next").build();

    /** The holder of the page in this menu */
    private final GuiInventoryHolder myPage;
    /** Positions of the previous and next buttons in our inventory */
    protected final int previousButtonIndex, nextButtonIndex;
    /** ItemStacks used for the previous-page and next-page buttons */
    protected final ItemStack nextPageButton, previousPageButton;
    /** The suppliers that supply the previous-page and next-page menus */
    private Supplier<PageMenu<P>> previous, next;

    /** hack to initialize the buttons when the inventory is opened for the first time */
    private boolean weHaveBeenOpened;

    /**
     * Creates a page menu.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param previous the previous page - can be null
     * @param next the tryToggle page - can be null
     * @throws IllegalArgumentException if the page size is below 9 or above 45
     */
    public PageMenu(P plugin, GuiInventoryHolder page, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next) throws IllegalArgumentException {
        this(plugin, page, previous, next, DEFAULT_PREVIOUS_PAGE_BUTTON.clone(), DEFAULT_NEXT_PAGE_BUTTON.clone());
    }

    /**
     * Creates a page menu.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param previous the previous page - can be null
     * @param next the tryToggle page - can be null
     * @param guiListener the listener that calls the onOpen, onClick and onClose methods
     * @throws IllegalArgumentException if the page size is below 9 or above 45
     */
    public PageMenu(GuiListener guiListener, P plugin, GuiInventoryHolder page, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next) throws IllegalArgumentException {
        this(guiListener, plugin, page, previous, next, DEFAULT_PREVIOUS_PAGE_BUTTON.clone(), DEFAULT_NEXT_PAGE_BUTTON.clone());
    }

    /**
     * Creates a page menu.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param title the title of the inventory
     * @param previous the previous page - can be null
     * @param next the tryToggle page - can be null
     * @throws IllegalArgumentException if the page size is below 9 or above 45
     */
    public PageMenu(P plugin, GuiInventoryHolder page, String title, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next) throws IllegalArgumentException {
        this(plugin, page, title, previous, next, DEFAULT_PREVIOUS_PAGE_BUTTON.clone(), DEFAULT_NEXT_PAGE_BUTTON.clone());
    }

    /**
     * Creates a page menu.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param title the title of the page
     * @param previous the previous page - can be null
     * @param next the tryToggle page - can be null
     * @param guiListener the listener that calls the onOpen, onClick and onClose methods
     * @throws IllegalArgumentException if the page size is below 9 or above 45
     */
    public PageMenu(GuiListener guiListener, P plugin, GuiInventoryHolder page, String title, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next) throws IllegalArgumentException {
        this(guiListener, plugin, page, title, previous, next, DEFAULT_PREVIOUS_PAGE_BUTTON.clone(), DEFAULT_NEXT_PAGE_BUTTON.clone());
    }

    /**
     * Creates a page menu.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param previous the previous page - can be null
     * @param next the tryToggle page - can be null
     * @param previousPageButton - the ItemStack used for the previous-page button
     * @param nextPageButton - the ItemStack used for the tryToggle-page button
     * @throws IllegalArgumentException if the page size is below 9 or above 45
     */
    public PageMenu(P plugin, GuiInventoryHolder page, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next, ItemStack previousPageButton, ItemStack nextPageButton) throws IllegalArgumentException {
        this(GuiListener.getInstance(), plugin, page, previous, next, previousPageButton, nextPageButton);
    }

    /**
     * Creates a page menu.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param previous the previous page - can be null
     * @param next the tryToggle page - can be null
     * @param previousPageButton - the ItemStack used for the previous-page button
     * @param nextPageButton - the ItemStack used for the tryToggle-page button
     * @param guiListener the listener that calls the onOpen, onClick and onClose methods
     * @throws IllegalArgumentException if the page size is below 9 or above 45
     */
    public PageMenu(GuiListener guiListener, P plugin, GuiInventoryHolder page, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next, ItemStack previousPageButton, ItemStack nextPageButton) throws IllegalArgumentException {
        super(guiListener, plugin, calculateInnerPageSize(page) + 9);
        this.myPage = page;
        this.previousButtonIndex = myPage.getInventory().getSize() + 2;
        this.nextButtonIndex = myPage.getInventory().getSize() + 6;
        this.previous = previous;
        this.next = next;
        this.nextPageButton = nextPageButton;
        this.previousPageButton = previousPageButton;

        addButtonListeners();
    }

    /**
     * Creates a page menu.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param title the title of the page
     * @param previous the previous page - can be null
     * @param next the tryToggle page - can be null
     * @param previousPageButton the ItemStack used for the previous-page button
     * @param nextPageButton the ItemStack used for the tryToggle-page button
     * @throws IllegalArgumentException if the page size is below 9 or above 45
     */
    public PageMenu(P plugin, GuiInventoryHolder page, String title, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next, ItemStack previousPageButton, ItemStack nextPageButton) throws IllegalArgumentException {
        this(GuiListener.getInstance(), plugin, page, title, previous, next, previousPageButton, nextPageButton);
    }

    /**
     * Creates a page menu.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param title the title of the page
     * @param previous the previous page - can be null
     * @param next the tryToggle page - can be null
     * @param previousPageButton the ItemStack used for the previous-page button
     * @param nextPageButton the ItemStack used for the tryToggle-page button
     * @param guiListener the listener that calls the onOpen, onClick and onClose methods
     * @throws IllegalArgumentException if the page size is below 9 or above 45
     */
    public PageMenu(GuiListener guiListener, P plugin, GuiInventoryHolder page, String title, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next, ItemStack previousPageButton, ItemStack nextPageButton) throws IllegalArgumentException {
        super(guiListener, plugin, calculateInnerPageSize(page) + 9, title);
        this.myPage = page;
        this.previousButtonIndex = myPage.getInventory().getSize() + 2;
        this.nextButtonIndex = myPage.getInventory().getSize() + 6;
        this.previous = previous;
        this.next = next;
        this.nextPageButton = nextPageButton;
        this.previousPageButton = previousPageButton;

        addButtonListeners();
    }

    /**
     * Get the page held by this menu.
     * @return the page
     */
    public GuiInventoryHolder<?> getPage() {
        return myPage;
    }

    /**
     * Get the size of the page held by this menu.
     * @return the size of the embedded page
     */
    public int getPageSize() {
        return getPage().getInventory().getSize();
    }

    /**
     * Tests whether this paging menu has a tryToggle page.
     * @return true if it has a tryToggle page, otherwise false
     */
    public boolean hasNextPage() {
        return getNextPageMenu().isPresent();
    }

    /**
     * Tests whether this paging menu has a previous page.
     * @return true if it has a previous page, otherwise false
     */
    public boolean hasPreviousPage() {
        return getPreviousPageMenu().isPresent();
    }

    /**
     * Get the supplier that supplies the menu for the next page.
     * @return the Optional containing the supplier, or the empty Optional of the supplier is absent.
     */
    public Optional<? extends Supplier<? extends PageMenu<P>>> getNextPageMenu() {
        return Optional.ofNullable(next);
    }

    /**
     * Get the supplier that supplies the menu for the previous page.
     * @return the Optional containing the supplier, or the empty Optional of the supplier is absent.
     */
    public Optional<? extends Supplier<? extends PageMenu<P>>> getPreviousPageMenu() {
        return Optional.ofNullable(previous);
    }

    /**
     * Create pages from a series of GUIs.
     * @param plugin your plugin
     * @param pageSupplier the iterator that supplies pages - must have at least one element and can be infinite
     * @param <P> your Plugin type
     * @return the menu containing the first page
     */
    public static <P extends Plugin> PageMenu<P> create(P plugin, Iterator<? extends GuiInventoryHolder<?>> pageSupplier) {
        return create(plugin, Objects.requireNonNull(pageSupplier, "PageSupplier cannot be null"), DEFAULT_PREVIOUS_PAGE_BUTTON.clone(), DEFAULT_NEXT_PAGE_BUTTON.clone());
    }

    /**
     * Create pages from a series of GUIs.
     * @param plugin your plugin
     * @param title the title of the pages
     * @param pageSupplier the iterator that supplies pages - must have at least one element and can be infinite
     * @param <P> your Plugin type
     * @return the menu containing the first page
     */
    public static <P extends Plugin> PageMenu<P> create(P plugin, String title, Iterator<? extends GuiInventoryHolder<?>> pageSupplier) {
        return create(plugin, title, Objects.requireNonNull(pageSupplier, "PageSupplier cannot be null"), DEFAULT_PREVIOUS_PAGE_BUTTON.clone(), DEFAULT_NEXT_PAGE_BUTTON.clone());
    }

    /**
     * Create pages from a series of GUIs.
     * @param plugin your plugin
     * @param pageSupplier the iterator that supplies pages - must have at least one element and can be infinite
     * @param previousPageButton the ItemStack used for the previous-page button
     * @param nextPageButton the ItemStack used for the tryToggle-page button
     * @param <P> your Plugin type
     * @return the menu containing the first page
     */
    public static <P extends Plugin> PageMenu<P> create(P plugin, Iterator<? extends GuiInventoryHolder<?>> pageSupplier, ItemStack previousPageButton, ItemStack nextPageButton) {
        return create(plugin, Objects.requireNonNull(pageSupplier, "PageSupplier cannot be null"), null, previousPageButton, nextPageButton);
    }

    /**
     * Create pages from a series of GUIs.
     * @param plugin your plugin
     * @param title the title of the pages
     * @param pageSupplier the iterator that supplies pages - must have at least one element and can be infinite
     * @param previousPageButton the ItemStack used for the previous-page button
     * @param nextPageButton the ItemStack used for the tryToggle-page button
     * @param <P> your Plugin type
     * @return the menu containing the first page
     */
    public static <P extends Plugin> PageMenu<P> create(P plugin, String title, Iterator<? extends GuiInventoryHolder<?>> pageSupplier, ItemStack previousPageButton, ItemStack nextPageButton) {
        return create(plugin, title, Objects.requireNonNull(pageSupplier, "PageSupplier cannot be null"), null, previousPageButton, nextPageButton);
    }

    //private because the previous page supplier argument can only be provided by recursive calls.
    private static <P extends Plugin> PageMenu<P> create(P plugin, Iterator<? extends GuiInventoryHolder<?>> nextSupplier, Supplier<PageMenu<P>> previous, ItemStack previousPageButton, ItemStack nextPageButton) {
        GuiInventoryHolder<?> page = nextSupplier.next();
        PageMenu<P> pageMenu = new PageMenu<>(plugin, page, previous, null, previousPageButton, nextPageButton);
        if (nextSupplier.hasNext()) pageMenu.next = new CachedSupplier<>(() -> create(plugin,
                nextSupplier,
                () -> pageMenu,
                previousPageButton == null ? null : previousPageButton.clone(),
                nextPageButton == null ? null : nextPageButton.clone()));
        return pageMenu;
    }

    //private because the previous page supplier argument can only be provided by recursive calls.
    private static <P extends Plugin> PageMenu<P> create(P plugin, String title, Iterator<? extends GuiInventoryHolder<?>> nextSupplier, Supplier<PageMenu<P>> previous, ItemStack previousPageButton, ItemStack nextPageButton) {
        GuiInventoryHolder<?> page = nextSupplier.next();
        PageMenu<P> pageMenu = new PageMenu<>(plugin, page, previous, null, previousPageButton, nextPageButton);
        if (nextSupplier.hasNext()) pageMenu.next = new CachedSupplier<>(() -> create(plugin,
                title,
                nextSupplier,
                () -> pageMenu,
                previousPageButton == null ? null : previousPageButton.clone(),
                nextPageButton == null ? null : nextPageButton.clone()));
        return pageMenu;
    }

    /**
     * Updates the view of page that is contained by this menu.
     */
    public void updateView() {
        //copy icons from the page back to my inventory
        for (int index = 0; index < myPage.getInventory().getSize(); index++) {
            getInventory().setItem(index, myPage.getInventory().getItem(index));
        }
    }

    //called from our constructors - needed to update our inventory when the page's inventory updates.
    private void addButtonListeners() {
        GuiInventoryHolder page = getPage();
        if (page instanceof MenuHolder) {
            MenuHolder menuPage = (MenuHolder) page;
            menuPage.addButtonAddCallback(this);
            menuPage.addButtonRemoveCallback(this);
        }
    }

    /**
     * Callback method that is called when a button is added in the page that this menu contains.
     * This callback adds the icon to the inventory of this PageMenu.
     *
     * @param slot the slot in the page
     * @param button the button that was added to the page
     * @return true
     */
    @Override
    public boolean onAdd(int slot, MenuButton button) {
        //called when the button is added to the page that we hold.
        //in that case, we want to set the icon ItemStack in our inventory.
        getInventory().setItem(slot, button.getIcon());
        return true;
    }

    /**
     * Callback method that is called when a button in the page that this menu contains is removed.
     * This callback removes the icon from the inventory of this PageMenu.
     *
     * @param slot the slot in the page
     * @param button the button that was removed from the page
     * @return true
     */
    @Override
    public boolean onRemove(int slot, MenuButton button) {
        //called when the button is removed from the page that we hold.
        //in that case, we want to remove the icon ItemStack from our inventory
        getInventory().setItem(slot, null);
        return true;
    }

    /**
     * Initialises the page view as well as previous-page and next-page buttons.
     */
    protected void resetButtons() {

        //if the page is a menu, then we want to add those buttons to us so that we get icon updates
        GuiInventoryHolder<?> page = getPage();
        if (page instanceof MenuHolder) {
            MenuHolder<?> menu = (MenuHolder<?>) page;
            int pageSize = getPageSize();
            for (int i = 0; i < pageSize; i++) {
                MenuButton button = menu.getButton(i);
                if (button != null) button.onAdd(this, i); //make the button think it exists in our outer inventory.
            }
        }

        //reset next-page and previous-page buttons
        getNextPageMenu().ifPresentOrElse(next -> this.setButton(nextButtonIndex,
                new RedirectItemButton(nextPageButton, () -> next.get().getInventory())),
                () -> this.unsetButton(nextButtonIndex));

        getPreviousPageMenu().ifPresentOrElse(previous -> this.setButton(previousButtonIndex,
                new RedirectItemButton(previousPageButton, () -> previous.get().getInventory())),
                () -> this.unsetButton(previousButtonIndex));
    }

    /**
     * Opens the page. Subclasses that override this method should always call super.onOpen(openEvent)
     * @param openEvent the event
     */
    @Override
    public void onOpen(InventoryOpenEvent openEvent) {
        if (!weHaveBeenOpened) {
            //reset buttons on first open.
            resetButtons();
            weHaveBeenOpened = true;
        }

        //delegate event to myPage
        InventoryView view = openEvent.getView();
        InventoryView proxyView = new ProxyView(view);

        InventoryOpenEvent proxyEvent = new InventoryOpenEvent(proxyView);
        getPlugin().getServer().getPluginManager().callEvent(proxyEvent);

        //if our page is a menu, then we already receive updates because of our callbacks
        //see addButtonListeners and resetButtons
        if (!(getPage() instanceof MenuHolder)) {
            updateView();
        }
    }

    @Override
    public void onClick(InventoryClickEvent clickEvent) {
        int rawSlot = clickEvent.getRawSlot();
        int myPageSize = myPage.getInventory().getSize();

        boolean myButtonRowIsClicked = rawSlot >= myPageSize && rawSlot < myPageSize + 9;
        if (myButtonRowIsClicked) {
            MenuButton button = getButton(rawSlot);
            if (button != null) button.onClick(this, clickEvent);
        } else {
            //my button row is not clicked - delegate event to myPage
            InventoryView view = clickEvent.getView();
            InventoryView proxyView = new ProxyView(view);

            InventoryType.SlotType slotType = clickEvent.getSlotType();
            InventoryType.SlotType proxySlotType;
            if (slotType == InventoryType.SlotType.OUTSIDE || slotType == InventoryType.SlotType.QUICKBAR) {
                proxySlotType = slotType;
            } else {
                proxySlotType = InventoryType.SlotType.CONTAINER;
            }

            int proxyRawSlot = getClickedInventory(clickEvent) == view.getBottomInventory() ? rawSlot - 9 : rawSlot;

            InventoryClickEvent proxyEvent = new InventoryClickEvent(proxyView,
                    proxySlotType,
                    proxyRawSlot,
                    clickEvent.getClick(),
                    clickEvent.getAction(),
                    clickEvent.getHotbarButton());

            getPlugin().getServer().getPluginManager().callEvent(proxyEvent);
            clickEvent.setCancelled(proxyEvent.isCancelled());

            //if our page is a menu, then we already receive updates because of our callbacks
            //see addButtonListeners and resetButtons
            if (!(getPage() instanceof MenuHolder)) {
                updateView();
            }
        }
    }

    @Override
    public void onDrag(InventoryDragEvent dragEvent) {
        dragEvent.setCancelled(false);

        InventoryView view = dragEvent.getView();
        InventoryView proxyView = new ProxyView(view);

        ItemStack newCursor = dragEvent.getCursor();
        ItemStack oldCursor = dragEvent.getOldCursor();
        boolean isRightClick = dragEvent.getType() == DragType.SINGLE;
        Map<Integer, ItemStack> newItems = dragEvent.getNewItems(); //immutable. craftbukkit does not allow changing the slots for dragging items.

        //instead, let's cancel the event if any of the slots is the bottom row. otherwise delegate to page using a proxy event.
        int myPageSize = myPage.getInventory().getSize();
        if (newItems.keySet().stream().anyMatch(i -> i >= myPageSize && i < myPageSize + 9)) {
            dragEvent.setCancelled(true);
        } else {
            final Map<Integer, ItemStack> proxyItems = newItems.entrySet().stream().map(entry -> {
                Integer slot = entry.getKey();
                ItemStack item = entry.getValue();

                //pretend control buttons don't exist in the proxyEvent
                //everything in the bottom inventory moves 9 indices up
                if (slot > myPageSize) slot = slot - 9;
                return Map.entry(slot, item);
            }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            InventoryDragEvent proxyEvent = new InventoryDragEvent(proxyView, newCursor, oldCursor, isRightClick, proxyItems);
            getPlugin().getServer().getPluginManager().callEvent(proxyEvent);
            dragEvent.setCursor(proxyEvent.getCursor());
            dragEvent.setResult(proxyEvent.getResult());
        }

        //genius algorithm to delegate drags to the page. does not work unfortunately because craftbukkit does not allow changing the slots for dragged items.
//        //cancel dragging over the bottom 9 slots of the top inventory - put the items back onto the cursor
//        int myPageSize = myPage.getInventory().getSize();
//        Iterator<Integer> slotIterator = newItems.keySet().stream().filter(i -> i >= myPageSize && i < myPageSize + 9).iterator();
//        while (slotIterator.hasNext()) {
//            Integer slot = slotIterator.next();
//            ItemStack needsToGoBackToCursor = newItems.remove(slot);
//            if (newCursor == null) newCursor = needsToGoBackToCursor;
//            if (newCursor != null) newCursor.setAmount(newCursor.getAmount() + needsToGoBackToCursor.getAmount()); //assume same Material and ItemMeta
//        }
//
//        dragEvent.setCursor(newCursor);
//
//        final Map<Integer, ItemStack> proxyItems = newItems.entrySet().stream().map(entry -> {
//            Integer slot = entry.getKey();
//            ItemStack item = entry.getValue();
//
//            //pretend control buttons don't exist in the proxyEvent
//            //everything in the bottom inventory moves 9 indices up
//            if (slot > myPageSize) slot = slot - 9;
//            return Map.entry(slot, item);
//        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//        boolean cancel = dragEvent.isCancelled();
//
//        final InventoryDragEvent proxyEvent = new InventoryDragEvent(proxyView, newCursor, oldCursor, right, proxyItems);
//
//        getPlugin().getServer().getPluginManager().callEvent(proxyEvent);
//        dragEvent.setCancelled(proxyEvent.isCancelled());
//
//        dragEvent.setCursor(proxyEvent.getCursor());
//        newItems.clear();
//        proxyEvent.getNewItems().forEach((slot, item) -> {
//            if (slot > myPageSize) slot += 9; //skip the page control buttons
//            newItems.put(slot, item);
//        });

        updateView();
    }

    /**
     * Closes the page. Subclasses that override this method should always call super.onClose(openEvent)
     * @param closeEvent the event
     */
    @Override
    public void onClose(InventoryCloseEvent closeEvent) {
        //delegate event to myPage
        InventoryView view = closeEvent.getView();
        getPlugin().getServer().getPluginManager().callEvent(new InventoryCloseEvent(new ProxyView(view)));

        //if our page is a menu, then we already receive updates because of our callbacks
        //see addButtonListeners and resetButtons
        if (!(getPage() instanceof MenuHolder)) {
            updateView();
        }
    }

    private static int calculateInnerPageSize(GuiInventoryHolder guiInventoryHolder) {
        int containedSize = guiInventoryHolder.getInventory().getSize();
        if (containedSize <= 0) {
            throw new IllegalArgumentException("Page cannot have a size of 0 or below");
        } else if (containedSize <= 45) {
            int remainder = containedSize % 9;
            if (remainder == 0) {
                return containedSize;
            } else {
                //pad up to a multiple of 9
                return containedSize + (9 - remainder);
            }
        } else {
            throw new IllegalArgumentException("The page cannot be larger than 45 slots");
        }
    }

    private class ProxyView extends InventoryView {
        private final InventoryView original;
        private ProxyView(InventoryView from) {
            this.original = from;
        }

        @Override
        public Inventory getTopInventory() {
            return myPage.getInventory();
        }

        @Override
        public Inventory getBottomInventory() {
            return original.getBottomInventory();
        }

        @Override
        public HumanEntity getPlayer() {
            return original.getPlayer();
        }

        @Override
        public InventoryType getType() {
            return InventoryType.CHEST;
        }

        @Override
        public String getTitle() {
            return original.getTitle();
        }
    }

}
