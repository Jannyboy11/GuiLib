package xyz.janboerman.guilib.api.menu;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.Plugin;
import xyz.janboerman.guilib.api.GuiInventoryHolder;
import xyz.janboerman.guilib.api.ItemBuilder;
import xyz.janboerman.guilib.util.CachedSupplier;

import java.util.Iterator;
import java.util.function.Supplier;

//TODO add javadocs
public class PageMenu<P extends Plugin> extends MenuHolder<P> {

    private final GuiInventoryHolder myPage;
    private final int previousButtonIndex, nextButtonIndex;

    private Supplier<PageMenu<P>> previous, next;

    public PageMenu(P plugin, GuiInventoryHolder page, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next) {
        super(plugin, calculateInnerPageSize(page) + 9);
        this.myPage = page;
        this.previousButtonIndex = myPage.getInventory().getSize() + 2;
        this.nextButtonIndex = myPage.getInventory().getSize() + 6;
        this.previous = previous;
        this.next = next;
    }

    public PageMenu(P plugin, GuiInventoryHolder page, String title, Supplier<PageMenu<P>> previous, Supplier<PageMenu<P>> next) {
        super(plugin, calculateInnerPageSize(page) + 9, title);
        this.myPage = page;
        this.previousButtonIndex = myPage.getInventory().getSize() + 2;
        this.nextButtonIndex = myPage.getInventory().getSize() + 6;
        this.previous = previous;
        this.next = next;
    }

    public static <P extends Plugin> PageMenu<P> create(P plugin, Iterator<? extends GuiInventoryHolder<?>> nextSupplier) {
        return create(plugin, nextSupplier, null);
    }

    private static <P extends Plugin> PageMenu<P> create(P plugin, Iterator<? extends GuiInventoryHolder<?>> nextSupplier, Supplier<PageMenu<P>> previous) {
        GuiInventoryHolder<?> page = nextSupplier.next();
        PageMenu<P> pageMenu = new PageMenu<>(plugin, page, previous, null);
        if (nextSupplier.hasNext()) pageMenu.next = new CachedSupplier<>(() -> create(plugin, nextSupplier, () -> pageMenu));
        return pageMenu;
    }

    public static <P extends Plugin> PageMenu<P> create(P plugin, String title, Iterator<? extends GuiInventoryHolder<?>> nextSupplier) {
        return create(plugin, title, nextSupplier, null);
    }

    private static <P extends Plugin> PageMenu<P> create(P plugin, String title, Iterator<? extends GuiInventoryHolder<?>> nextSupplier, Supplier<PageMenu<P>> previous) {
        GuiInventoryHolder<?> page = nextSupplier.next();
        PageMenu<P> pageMenu = new PageMenu<>(plugin, page, previous, null);
        if (nextSupplier.hasNext()) pageMenu.next = new CachedSupplier<>(() -> create(plugin, title, nextSupplier, () -> pageMenu));
        return pageMenu;
    }

    private boolean hasNextPage() {
        return next != null;
    }

    private boolean hasPreviousPage() {
        return previous != null;
    }

    @Override
    public void onOpen(InventoryOpenEvent openEvent) {
        // add redirect buttons
        if (hasNextPage()) {
            this.setButton(nextButtonIndex, new RedirectItemButton(new ItemBuilder(Material.MAGENTA_GLAZED_TERRACOTTA)
                    .name("Next")
                    .build(), () -> next.get().getInventory()));
        }
        if (hasPreviousPage()) {
            this.setButton(previousButtonIndex, new RedirectItemButton(new ItemBuilder(Material.MAGENTA_GLAZED_TERRACOTTA)
                    .name("Previous")
                    .build(), () -> previous.get().getInventory()));
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

    @Override
    public void onClose(InventoryCloseEvent closeEvent) {
        //buttons are created when the inventory is opened
        clearButtons();

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
            throw new IllegalArgumentException("The page is larger than 45 slots");
        }
    }

}
