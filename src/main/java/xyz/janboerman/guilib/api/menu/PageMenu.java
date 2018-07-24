package xyz.janboerman.guilib.api.menu;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.janboerman.guilib.api.GuiInventoryHolder;
import xyz.janboerman.guilib.api.ItemBuilder;
import xyz.janboerman.guilib.util.CachedSupplier;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A menu that implements pages. This menu by default only has two buttons - on the bottom row of the top inventory.
 * The pages themselves can therefore not be larger than 45 slots.
 * @param <P> your plugin type
 */
public class PageMenu<P extends Plugin> extends MenuHolder<P> {

    private static final ItemStack DEFAULT_PREVIOUS_PAGE_BUTTON = new ItemBuilder(Material.MAGENTA_GLAZED_TERRACOTTA).name("Previous").build();
    private static final ItemStack DEFAULT_NEXT_PAGE_BUTTON = new ItemBuilder(Material.MAGENTA_GLAZED_TERRACOTTA).name("Next").build();

    /** The holder of the page in this menu */
    private final GuiInventoryHolder myPage;
    /** Positions of the previous and next buttons in our inventory */
    protected final int previousButtonIndex, nextButtonIndex;
    /** ItemStacks used for the previous-page and next-page buttons */
    protected final ItemStack nextPageButton, previousPageButton;
    /** The suppliers that supply the previoius-page and next-page menus */
    private Supplier<PageMenu<P>> previous, next;

    /** hack to initialize the buttons when the inventory is opened for the first time */
    private boolean weHaveBeenOpened;

    /**
     * Creates a page.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param previous the previous page - can be null
     * @param next the tryToggle page - can be null
     */
    public PageMenu(P plugin, GuiInventoryHolder page, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next) {
        this(plugin, page, previous, next, DEFAULT_PREVIOUS_PAGE_BUTTON.clone(), DEFAULT_NEXT_PAGE_BUTTON.clone());
    }

    /**
     * Creates a page.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param title the title of the page
     * @param previous the previous page - can be null
     * @param next the tryToggle page - can be null
     */
    public PageMenu(P plugin, GuiInventoryHolder page, String title, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next) {
        this(plugin, page, title, previous, next, DEFAULT_PREVIOUS_PAGE_BUTTON.clone(), DEFAULT_NEXT_PAGE_BUTTON.clone());
    }

    /**
     * Creates a page.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param previous the previous page - can be null
     * @param next the tryToggle page - can be null
     * @param previousPageButton - the ItemStack used for the previous-page button
     * @param nextPageButton - the ItemStack used for the tryToggle-page button
     */
    public PageMenu(P plugin, GuiInventoryHolder page, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next, ItemStack previousPageButton, ItemStack nextPageButton) {
        super(plugin, calculateInnerPageSize(page) + 9);
        this.myPage = page;
        this.previousButtonIndex = myPage.getInventory().getSize() + 2;
        this.nextButtonIndex = myPage.getInventory().getSize() + 6;
        this.previous = previous;
        this.next = next;
        this.nextPageButton = nextPageButton;
        this.previousPageButton = previousPageButton;
    }

    /**
     * Creates a page.
     * @param plugin your plugin
     * @param page the gui in this page - cannot be larger than 45 slots
     * @param title the title of the page
     * @param previous the previous page - can be null
     * @param next the tryToggle page - can be null
     * @param previousPageButton - the ItemStack used for the previous-page button
     * @param nextPageButton - the ItemStack used for the tryToggle-page button
     */
    public PageMenu(P plugin, GuiInventoryHolder page, String title, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next, ItemStack previousPageButton, ItemStack nextPageButton) {
        super(plugin, calculateInnerPageSize(page) + 9, title);
        this.myPage = page;
        this.previousButtonIndex = myPage.getInventory().getSize() + 2;
        this.nextButtonIndex = myPage.getInventory().getSize() + 6;
        this.previous = previous;
        this.next = next;
        this.nextPageButton = nextPageButton;
        this.previousPageButton = previousPageButton;
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
     * Get the supplier that supplies the menu for the tryToggle page.
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
     * Initialises the previous-page and next-page buttons.
     */
    protected void resetButtons() {
        getNextPageMenu().ifPresentOrElse(next -> this.setButton(nextButtonIndex,
                new RedirectItemButton(nextPageButton, () -> next.get().getInventory())),
                () -> this.unsetButton(nextButtonIndex));

        getPreviousPageMenu().ifPresentOrElse(previous -> this.setButton(previousButtonIndex,
                new RedirectItemButton(previousPageButton, () -> previous.get().getInventory())),
                () -> this.unsetButton(previousButtonIndex));
    }

    /**
     * @deprecated use {@link #resetButtons()} instead.
     */
    @Deprecated(forRemoval = true, since = "1.4.0")
    protected void initButtons() {
        resetButtons();
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
     * Opens the page. Subclasses that override this method should always call super.onOpen(openEvent)
     * @param openEvent the event
     */
    @Override
    public void onOpen(InventoryOpenEvent openEvent) {
        if (!weHaveBeenOpened) { //is there a nicer way to lazily init the buttons? I can't think of any
            resetButtons();
            weHaveBeenOpened = true;
        }

        //delegate event to myPage
        InventoryView view = openEvent.getView();
        InventoryView proxyView = new InventoryView() {
            @Override
            public Inventory getTopInventory() {
                return myPage.getInventory();
            }

            @Override
            public Inventory getBottomInventory() {
                return view.getBottomInventory();
            }

            @Override
            public HumanEntity getPlayer() {
                return view.getPlayer();
            }

            @Override
            public InventoryType getType() {
                return InventoryType.CHEST;
            }
        };

        InventoryOpenEvent proxyEvent = new InventoryOpenEvent(proxyView);
        getPlugin().getServer().getPluginManager().callEvent(proxyEvent);

        //copy icons back to my inventory
        for (int index = 0; index < myPage.getInventory().getSize(); index++) {
            getInventory().setItem(index, myPage.getInventory().getItem(index));
        }
    }

    /**
     * Clicks the page. Subclasses that override this method should always call super.onClick(openEvent)
     * @param clickEvent the event
     */
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
            InventoryView proxyView = new InventoryView() {
                @Override
                public Inventory getTopInventory() {
                    return myPage.getInventory();
                }

                @Override
                public Inventory getBottomInventory() {
                    return view.getBottomInventory();
                }

                @Override
                public HumanEntity getPlayer() {
                    return view.getPlayer();
                }

                @Override
                public InventoryType getType() {
                    return InventoryType.CHEST;
                }
            };

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

            //copy icons back to my inventory
            for (int index = 0; index < myPage.getInventory().getSize(); index++) {
                getInventory().setItem(index, myPage.getInventory().getItem(index));
            }
        }
    }

    /**
     * Closes the page. Subclasses that override this method should always call super.onClose(openEvent)
     * @param closeEvent the event
     */
    @Override
    public void onClose(InventoryCloseEvent closeEvent) {
        //delegate event to myPage
        InventoryView view = closeEvent.getView();
        getPlugin().getServer().getPluginManager().callEvent(new InventoryCloseEvent(new InventoryView() {
            @Override
            public Inventory getTopInventory() {
                return myPage.getInventory();
            }

            @Override
            public Inventory getBottomInventory() {
                return view.getBottomInventory();
            }

            @Override
            public HumanEntity getPlayer() {
                return view.getPlayer();
            }

            @Override
            public InventoryType getType() {
                return InventoryType.CHEST;
            }
        }));
    }

    private static int calculateInnerPageSize(GuiInventoryHolder guiInventoryHolder) {
        int containedSize = guiInventoryHolder.getInventory().getSize();
        if (containedSize == 0) {
            throw new IllegalArgumentException("Page cannot be a 0-sized inventory");
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

}
