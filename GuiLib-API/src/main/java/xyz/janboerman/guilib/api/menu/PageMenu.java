package xyz.janboerman.guilib.api.menu;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.janboerman.guilib.api.GuiListener;
import xyz.janboerman.guilib.api.GuiInventoryHolder;
import xyz.janboerman.guilib.api.ItemBuilder;
import xyz.janboerman.guilib.util.CachedSupplier;
import xyz.janboerman.guilib.util.Scheduler;

import java.util.*;
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

    private final String title;

    /** The holder of the page in this menu */
    private final GuiInventoryHolder myPage;
    /** Positions of the previous and next buttons in our inventory */
    protected final int previousButtonIndex, nextButtonIndex;
    /** ItemStacks used for the previous-page and next-page buttons */
    protected final ItemStack previousPageButton, nextPageButton;
    /** The suppliers that supply the previous-page and next-page menus */
    private Supplier<PageMenu<P>> previousPageSupplier, nextPageSupplier;


    private PageMenu<P> renderedPage = this, hostingPage = this;
    private ItemStack renderedNextStack, renderedPreviousStack;
    private int renderedNextIndex, renderedPreviousIndex;


    /** hack to initialize the buttons when the inventory is opened for the first time */
    private boolean weHaveBeenOpened;

    /**
     * Creates a page menu.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param previous the previous page - can be null
     * @param next the next page - can be null
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
     * @param next the next page - can be null
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
     * @param next the next page - can be null
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
     * @param next the next page - can be null
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
     * @param next the next page - can be null
     * @param previousPageButton - the ItemStack used for the previous-page button
     * @param nextPageButton - the ItemStack used for the next-page button
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
     * @param next the next page - can be null
     * @param previousPageButton - the ItemStack used for the previous-page button
     * @param nextPageButton - the ItemStack used for the next-page button
     * @param guiListener the listener that calls the onOpen, onClick and onClose methods
     * @throws IllegalArgumentException if the page size is below 9 or above 45
     */
    public PageMenu(GuiListener guiListener, P plugin, GuiInventoryHolder page, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next, ItemStack previousPageButton, ItemStack nextPageButton) throws IllegalArgumentException {
        super(guiListener, plugin, calculateInnerPageSize(page) + 9);
        this.myPage = page;
        this.previousButtonIndex = this.renderedPreviousIndex = calculateInnerPageSize(myPage) + 2;
        this.nextButtonIndex = this.renderedNextIndex = calculateInnerPageSize(myPage) + 6;
        this.previousPageSupplier = previous;
        this.nextPageSupplier = next;
        this.previousPageButton = this.renderedPreviousStack = previousPageButton;
        this.nextPageButton = this.renderedNextStack = nextPageButton;
        this.title = null;

        addButtonListeners(); //receive inventory updates from the page
    }

    /**
     * Creates a page menu.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param title the title of the page
     * @param previous the previous page - can be null
     * @param next the next page - can be null
     * @param previousPageButton the ItemStack used for the previous-page button
     * @param nextPageButton the ItemStack used for the next-page button
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
     * @param next the next page - can be null
     * @param previousPageButton the ItemStack used for the previous-page button
     * @param nextPageButton the ItemStack used for the next-page button
     * @param guiListener the listener that calls the onOpen, onClick and onClose methods
     * @throws IllegalArgumentException if the page size is below 9 or above 45
     */
    public PageMenu(GuiListener guiListener, P plugin, GuiInventoryHolder page, String title, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next, ItemStack previousPageButton, ItemStack nextPageButton) throws IllegalArgumentException {
        super(guiListener, plugin, calculateInnerPageSize(page) + 9, title);
        this.myPage = page;
        this.previousButtonIndex = this.renderedPreviousIndex = calculateInnerPageSize(myPage) + 2;
        this.nextButtonIndex = this.renderedNextIndex = calculateInnerPageSize(myPage) + 6;
        this.previousPageSupplier =  previous;
        this.nextPageSupplier = next;
        this.previousPageButton = this.renderedPreviousStack = previousPageButton;
        this.nextPageButton = this.renderedNextStack = nextPageButton;
        this.title = title;

        addButtonListeners(); //receive inventory updates from the page.
    }

    /**
     * Tests whether this PageMenu implementation requires the next-page and previous-page buttons to use redirects (inventory re-opens).
     * @return true if a re-open is a hard requirement for the next-page and previous-page buttons
     */
    protected boolean needsRedirects() {
        return getClass() != PageMenu.class;
    }

    /**
     * Gets the page menu that is the hosting the next- and previous-buttons shown to the player.
     * @return the hosting page
     */
    protected final PageMenu<P> getHostingPage() {
        return hostingPage;
    }

    /**
     * Gets the page menu whose contents is being shown to the player.
     * @return the rendered page
     */
    protected final PageMenu<P> getRenderedPage() {
        return renderedPage;
    }

    /**
     * Get the page that this menu was initialised with.
     * @return the page owned by this menu
     */
    protected GuiInventoryHolder<?> getOwnedPage() {
        return myPage;
    }

    /**
     * Get the page that is currently rendered by the menu.
     * @return the page
     */
    public GuiInventoryHolder<?> getPage() {
        return getRenderedPage().getOwnedPage();
    }

    /**
     * Get the size of the page rendered by this menu.
     * @return the size of the embedded page
     */
    public int getPageSize() {
        return getPage().getInventory().getSize();
    }

    /**
     * Tests whether this paging menu has a next page.
     * @return true if it has a next page, otherwise false
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
     * Get the supplier that supplies the menu for the page that will be rendered by clicking on the "next page" button.
     * @return the Optional containing the supplier, or the empty Optional of the supplier is absent.
     */
    public Optional<? extends Supplier<? extends PageMenu<P>>> getNextPageMenu() {
        return Optional.ofNullable(nextPageSupplier);
    }

    /**
     * Get the supplier that supplies the menu for the page that will be rendered by clicking on the "previous page" button.
     * @return the Optional containing the supplier, or the empty Optional of the supplier is absent.
     */
    public Optional<? extends Supplier<? extends PageMenu<P>>> getPreviousPageMenu() {
        return Optional.ofNullable(previousPageSupplier);
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
     * @param nextPageButton the ItemStack used for the next-page button
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
     * @param nextPageButton the ItemStack used for the next-page button
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
        if (nextSupplier.hasNext()) pageMenu.nextPageSupplier = new CachedSupplier<>(() -> create(plugin,
                nextSupplier,   //the nextSupplier is the iterator
                () -> pageMenu, //the previousSupplier is the pageMenu that was created just now
                previousPageButton == null ? null : previousPageButton.clone(), //I don't like this because the itemstack that was passed as a parameter may have changed in the meantime. oh well.
                nextPageButton == null ? null : nextPageButton.clone()));       //Idem
        return pageMenu;
    }

    //private because the previous page supplier argument can only be provided by recursive calls.
    private static <P extends Plugin> PageMenu<P> create(P plugin, String title, Iterator<? extends GuiInventoryHolder<?>> nextSupplier, Supplier<PageMenu<P>> previous, ItemStack previousPageButton, ItemStack nextPageButton) {
        GuiInventoryHolder<?> page = nextSupplier.next();
        PageMenu<P> pageMenu = new PageMenu<>(plugin, page, previous, null, previousPageButton, nextPageButton);
        if (nextSupplier.hasNext()) pageMenu.nextPageSupplier = new CachedSupplier<>(() -> create(plugin,
                title,
                nextSupplier,   //the nextSupplier is the iterator
                () -> pageMenu, //the previousSupplier is the pageMenu that was created just now
                previousPageButton == null ? null : previousPageButton.clone(), //Idem
                nextPageButton == null ? null : nextPageButton.clone()));       //Idem
        return pageMenu;
    }

    /**
     * Updates the view of page that is contained by this menu.
     */
    public void updateView() {
        //copy icons from the page back to my inventory
        for (int index = 0; index < getPageSize(); index++) {
            getInventory().setItem(index, getPage().getInventory().getItem(index));
        }
    }

    //called from our constructors - needed to update our inventory when the page's inventory updates.
    private void addButtonListeners() {
        GuiInventoryHolder<?> page = getPage();
        if (page instanceof MenuHolder) {
            MenuHolder<?> menuPage = (MenuHolder<?>) page;
            menuPage.addButtonAddCallback(this);
            menuPage.addButtonRemoveCallback(this);
        }
    }

    //called when we no longer need to receive inventory updates from our page.
    private void removeButtonListeners() {
        GuiInventoryHolder<?> page = getPage();
        if (page instanceof MenuHolder) {
            MenuHolder<?> menuPage = (MenuHolder<?>) page;
            menuPage.removeButtonAddCallback(this);
            menuPage.removeButtonRemoveCallback(this);
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
        getPage().getInventory().setItem(slot, button.getIcon());
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
        getPage().getInventory().setItem(slot, null);
        return true;
    }

    /**
     * Initialises the page view as well as previous-page and next-page buttons.
     */
    public void resetButtons() {

        //if the page is a menu, then we want to add those buttons to us so that we get icon updates

        int currentInvSize = getPageSize();

        //needed for itembuttons that change their itemstack in a bukkit task.
        //is it worth it to cascade those updates into the PageMenu? because this code is not type-safe.
//        GuiInventoryHolder<?> page = getPage();
//        if (page instanceof MenuHolder) {
//            MenuHolder<?> menu = (MenuHolder<?>) page;
//            for (int i = 0; i < currentInvSize; i++) {
//                MenuButton button = menu.getButton(i);
//                if (button != null) button.onAdd(this,  i);
//            }
//        }

        //reset next-page and previous-page buttons
        getRenderedPage().getNextPageMenu().ifPresentOrElse(next -> {

            MenuButton toNextPageButton = new ItemButton(renderedNextStack) {
                @Override
                public void onClick(MenuHolder holder, InventoryClickEvent event) {
                    PageMenu<P> nextPageMenu = next.get();
                    GuiInventoryHolder<?> nextPage = nextPageMenu.getOwnedPage();
                    Inventory nextInventory = nextPage.getInventory();
                    //determine whether a re-open is required
                    if (!needsRedirects() && nextInventory.getSize() == currentInvSize && Objects.equals(nextPageMenu.title, title)) {
                        //no redirect required.

                        //call InventoryCloseEvent for the currently-rendered page
                        InventoryCloseEvent proxyCloseEvent = new InventoryCloseEvent(event.getView());
                        getPlugin().getServer().getPluginManager().callEvent(proxyCloseEvent);

                        //copy stuff over to our own inventory and reset the buttons.
                        removeButtonListeners();
                        renderedPage = nextPageMenu;
                        renderedPage.hostingPage = PageMenu.this;
                        addButtonListeners();
                        renderedPreviousIndex = renderedPage.previousButtonIndex;
                        renderedNextIndex = renderedPage.nextButtonIndex;
                        renderedPreviousStack = renderedPage.previousPageButton;
                        renderedNextStack = renderedPage.nextPageButton;

                        //call InventoryOpenEvent for tne newly-rendered page
                        weHaveBeenOpened = false;
                        InventoryOpenEvent proxyOpenEvent = new InventoryOpenEvent(event.getView());
                        getPlugin().getServer().getPluginManager().callEvent(proxyOpenEvent);

                        //update view
                        updateView();

                    } else {
                        //redirect required
                        Scheduler.get().runTaskLater(holder.getPlugin(), event.getWhoClicked(), () -> {
                            event.getView().close();
                            event.getWhoClicked().openInventory(nextPageMenu.getInventory());
                        });
                    }
                }
            };

            this.setButton(renderedNextIndex, toNextPageButton);

        }, /*next page not present*/ () -> {
            this.unsetButton(renderedNextIndex);
        });

        getRenderedPage().getPreviousPageMenu().ifPresentOrElse(previous -> {
            //TODO un-duplicate this code?
            MenuButton toPreviousPageButton = new ItemButton(renderedPreviousStack) {
                @Override
                public void onClick(MenuHolder holder, InventoryClickEvent event) {
                    PageMenu<P> previousPageMenu = previous.get();
                    GuiInventoryHolder<?> previousPage = previousPageMenu.getOwnedPage();
                    Inventory previousInventory = previousPage.getInventory();
                    //determine whether a re-open is required
                    if (!needsRedirects() && previousInventory.getSize() == currentInvSize && Objects.equals(previousPageMenu.title, title)) {
                        //no redirect required

                        //call InventoryCloseEvent for the currently-rendered page
                        InventoryCloseEvent proxyCloseEvent = new InventoryCloseEvent(event.getView());
                        getPlugin().getServer().getPluginManager().callEvent(proxyCloseEvent);

                        //copy stuff over to our own inventory and reset the buttons.
                        removeButtonListeners();
                        renderedPage = previousPageMenu;
                        renderedPage.hostingPage = PageMenu.this;
                        addButtonListeners();
                        renderedPreviousIndex = renderedPage.previousButtonIndex;
                        renderedNextIndex = renderedPage.nextButtonIndex;
                        renderedPreviousStack = renderedPage.previousPageButton;
                        renderedNextStack = renderedPage.nextPageButton;

                        //call InventoryOpenEvent for tne newly-rendered page
                        weHaveBeenOpened = false;
                        InventoryOpenEvent proxyOpenEvent = new InventoryOpenEvent(event.getView());
                        getPlugin().getServer().getPluginManager().callEvent(proxyOpenEvent);

                        //update view
                        updateView();

                    } else {
                        //redirect required
                        Scheduler.get().runTaskLater(holder.getPlugin(), event.getWhoClicked(), () -> {
                            event.getView().close();
                            event.getWhoClicked().openInventory(previousPageMenu.getInventory());
                        });
                    }
                }
            };

            this.setButton(renderedPreviousIndex, toPreviousPageButton);
        }, /*previous page not present*/ () -> {
            this.unsetButton(renderedPreviousIndex);
        });
    }

    /**
     * Opens the page. Subclasses that override this method should always call super.onOpen(openEvent)
     * @param openEvent the event
     */
    @Override
    public void onOpen(InventoryOpenEvent openEvent) {
        //delegate event to myPage
        InventoryOpenEvent proxyEvent = new InventoryOpenEvent(new ProxyView(openEvent.getView()));
        getPlugin().getServer().getPluginManager().callEvent(proxyEvent);

        if (!weHaveBeenOpened) {
            //reset buttons on first open.
            resetButtons();
            weHaveBeenOpened = true;
        }

        //update our inventory
        updateView();
        addButtonListeners();
    }

    /**
     * Closes the page. Subclasses that override this method should always call super.onClose(openEvent)
     * @param closeEvent the event
     */
    @Override
    public void onClose(InventoryCloseEvent closeEvent) {
        //delegate event to myPage
        InventoryCloseEvent proxyEvent = new InventoryCloseEvent(new ProxyView(closeEvent.getView()));
        getPlugin().getServer().getPluginManager().callEvent(proxyEvent);

        //update our inventory
        updateView();
        removeButtonListeners();
    }

    @Override
    public void onClick(InventoryClickEvent clickEvent) {
        int rawSlot = clickEvent.getRawSlot();
        GuiInventoryHolder currentPage = getPage();
        int myPageSize = getPageSize();
        MenuButton button;

        int topInventorySize = clickEvent.getView().getTopInventory().getSize();
        boolean myButtonRowIsClicked = topInventorySize - 9 <= rawSlot && rawSlot < topInventorySize;
        if (myButtonRowIsClicked) {
            //a button on the bottom row was clicked. this is the next- or previouspage button.
            super.onClick(clickEvent);
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

            if (rawSlot < myPageSize && currentPage instanceof MenuHolder && (button = ((MenuHolder) currentPage).getButton(rawSlot)) != null && button instanceof RedirectButton) {

                //a button from the page was clicked.
                //if it's a redirect, then special-case it so that we stay inside a PageMenu

                RedirectButton redirectButton = (RedirectButton) button;
                Inventory target = redirectButton.to((MenuHolder<?>) currentPage, proxyEvent);
                GuiInventoryHolder<?> page = guiListener.getHolder(target);     //don't use target.getHolder()! https://hub.spigotmc.org/jira/browse/SPIGOT-4274

                if (target.getSize() < 5 * 9) {
                    //we have enough room to put the target inventory in a page in a PageMenu.

                    if (page == null) {
                        //Target inventory was not managed by GuiLib. So let's give it a GuiInventoryHolder.
                        page = new MenuHolder<>(guiListener, getPlugin(), target) {
                            @Override
                            public void onClick(InventoryClickEvent event) {
                                if (event.getResult() == Event.Result.DENY) event.setCancelled(false);
                                super.onClick(event);
                            }

                            @Override
                            public void onDrag(InventoryDragEvent event) {
                                if (event.getResult() == Event.Result.DENY) event.setCancelled(false);
                                super.onDrag(event);
                            }
                        };
                    }

                    PageMenu pageMenu = new PageMenu(getPlugin(), page, view.getTitle(), previousPageSupplier, nextPageSupplier);
                    target = pageMenu.getInventory();
                }

                final Inventory open = target;

                //open the target inventory. It can be either the original inventory to which we were redirected, or in can be a page inside a new PageMenu.
                Scheduler.get().runTaskLater(getPlugin(), clickEvent.getWhoClicked(), () -> {
                    clickEvent.getWhoClicked().closeInventory();
                    clickEvent.getWhoClicked().openInventory(open);
                });

            } else {
                //not a redirect button
                getPlugin().getServer().getPluginManager().callEvent(proxyEvent);
                clickEvent.setCancelled(proxyEvent.isCancelled());
            }

            updateView();
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
        int myPageSize = getPage().getInventory().getSize();
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

            //run a task later such that we take the changes from event listeners on the page into account.
            Scheduler.get().runTaskLater(getPlugin(), dragEvent.getWhoClicked(), () -> {
                for (int i = 0; i < getPageSize(); i++) {
                    Map<Integer, ItemStack> proxyNewItems = proxyEvent.getNewItems();
                    ItemStack oldItem = getPage().getInventory().getItem(i);
                    ItemStack addItem = proxyNewItems.get(i);

                    if (addItem != null) {
                        ItemStack setItem;
                        if (oldItem == null) {
                            setItem = addItem;
                        } else {
                            setItem = oldItem;
                            setItem.setAmount(setItem.getAmount() + addItem.getAmount()); //assume equal Material and ItemMeta
                        }
                        getPage().getInventory().setItem(i, setItem);
                    }
                }
            });
        }

        //genius algorithm to delegate drags to the page. does not work unfortunately because craftbukkit does not allow changing the slots for dragged items.
//        //cancel dragging over the bottom 9 slots of the top inventory - put the items back onto the cursor
//        int myPageSize = getPage().getInventory().getSize();
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

    private static int calculateInnerPageSize(GuiInventoryHolder<?> guiInventoryHolder) {
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
            return getPage().getInventory();
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
