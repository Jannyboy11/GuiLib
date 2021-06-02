package xyz.janboerman.guilib.api.mask;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import xyz.janboerman.guilib.api.menu.MenuButton;
import xyz.janboerman.guilib.api.menu.MenuHolder;
import xyz.janboerman.guilib.api.util.IntBiConsumer;
import xyz.janboerman.guilib.api.util.Option;

import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * A Mask can be used in combination with a {@link Pattern} to apply bulk operations to item-containers such as inventories or menus.
 * This makes it easier to construct menus with the same items but with different layouts.
 * Another use case for Masks and Patterns are animations.
 * @param <Symbol> the symbol used by the mask. Typically this is Boolean, Integer, Character or an enum.
 * @param <Item> the type of the item in the container.
 * @see Inventory
 * @see MenuHolder
 */
public interface Mask<Symbol, Item> {

    /**
     * Get the item that is mapped to by a symbol.
     * @param symbol a symbol value, or null
     * @return an Option containing the mapped value, or an empty Option if the symbol is not supported by this Mask.
     */
    public Option<Item> getItem(Symbol symbol);

    /**
     * Create a Mask from a Map.
     * @param map the mapping that will be used by the Mask
     * @param <Symbol> the symbol type
     * @param <Item> the item type
     * @return a new Mask
     */
    public static <Symbol, Item> Mask<Symbol, Item> ofMap(Map<Symbol, Item> map) {
        return new MapMask<>(map);
    }

    /**
     * Apply a mask and a pattern to a container.
     * @param mask the mask
     * @param pattern the pattern
     * @param indexGenerator the generator that decided which slots to update
     * @param updater the setter function of the container
     * @param <Symbol> the symbol type
     * @param <Item> element type
     */
    public static <Symbol, Item> void apply(Mask<Symbol, Item> mask, Pattern<Symbol> pattern, IntStream indexGenerator, IntBiConsumer updater) {
        indexGenerator.forEach(index -> {
            Symbol symbol = pattern.getSymbol(index);
            var item = mask.getItem(symbol);
            if (item.isPresent()) updater.accept(index, item.get());
        });
    }

    /**
     * Apply a bulk update to an inventory. All the inventory slots that are supported by the pattern and mask will get an update.
     * @param mask the mask
     * @param pattern the pattern
     * @param inventory the inventory
     * @param <Symbol> the symbol type
     */
    public static <Symbol> void applyInventory(Mask<Symbol, ItemStack> mask, Pattern<Symbol> pattern, Inventory inventory) {
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            Symbol symbol = pattern.getSymbol(slot);
            var item = mask.getItem(symbol);
            if (item.isPresent()) inventory.setItem(slot, item.get());
        }
    }

    /**
     * Apply a bulk update to a menu. All the menu slots that are supported by the pattern and mask will get an update.
     * @param mask the mask
     * @param pattern the pattern
     * @param menu the inventory
     * @param <Symbol> the symbol Type
     * @param <P> the plugin type
     * @param <MH> the MenuHolder type
     */
    public static <Symbol, P extends Plugin, MH extends MenuHolder<P>> void applyMenu(Mask<Symbol, ? extends MenuButton<MH>> mask, Pattern<Symbol> pattern, MH menu) {
        for (int slot = 0; slot < menu.getInventory().getSize(); slot++) {
            Symbol symbol = pattern.getSymbol(slot);
            var button = mask.getItem(symbol);
            if (button.isPresent()) menu.setButton(slot, button.get());
        }
    }

}

class MapMask<Symbol, Item> implements Mask<Symbol, Item> {
    private final Map<Symbol, Item> mapper;

    /**
     * Construct a Mask.
     * @param mapper the mapping
     */
    public MapMask(Map<Symbol, Item> mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper cannot be null");
    }

    /**
     * Get the item that is mapped to by a symbol.
     * @param symbol a symbol value, or null
     * @return Some(item) if this mask contains a mapping, otherwise None
     */
    public Option<Item> getItem(Symbol symbol) {
        if (mapper.containsKey(symbol)) return Option.some(mapper.get(symbol));
        else return Option.none();
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(mapper);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof MapMask)) return false;

        MapMask that = (MapMask) obj;
        return Objects.equals(this.mapper, that.mapper);
    }

    @Override
    public String toString() {
        return "MapMask(mapper=" + mapper + ")";
    }
}
