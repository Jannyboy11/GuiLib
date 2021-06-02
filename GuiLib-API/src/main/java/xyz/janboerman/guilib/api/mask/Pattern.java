package xyz.janboerman.guilib.api.mask;

import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.Inventory;
import xyz.janboerman.guilib.api.mask.patterns.BorderPattern;
import xyz.janboerman.guilib.api.mask.patterns.CheckerboardPattern;
import xyz.janboerman.guilib.api.menu.MenuHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * A Pattern represents a mapping from slot to some kind of object.
 * This object type MUST have a proper equals and hashCode implementation.
 * </p>
 * <p> Patterns are best used in conjunction with Masks, see {@link Mask#applyInventory(Mask, Pattern, Inventory)}, {@link Mask#applyMenu(Mask, Pattern, MenuHolder)}.
 * </p>
 * @param <Symbol> the type of object. Typically this is Boolean, Integer, Character or an enum.
 * @see Mask
 */
public interface Pattern<Symbol> {

    /**
     * Get the symbol.
     * @param location the inventory slot index
     * @return the symbol
     */
    public Symbol getSymbol(int location);

    /**
     * Creates a pattern that is backed by a Map.
     * @param symbols the map
     * @param <Symbol> the type of symbols provided by the pattern
     * @return the pattern
     */
    public static <Symbol> Pattern<Symbol> ofMap(Map<Integer, Symbol> symbols) {
        Objects.requireNonNull(symbols, "symbols map cannot be null");

        return symbols::get;
    }

    /**
     * Creates a pattern that maps indices to characters in the provided string.
     * The newline characters '\r' and '\n' are not counted, making this method ideal to use with Java's Text Blocks.
     *
     * @param grid the string-literal form of the pattern
     * @return the pattern
     */
    public static Pattern<Character> ofGrid(String grid) {
        Objects.requireNonNull(grid, "grid cannot be null");

        Map<Integer, Character> map = new HashMap<>();

        int slot = 0;
        for (int i = 0; i < grid.length(); i++) {
            char x = grid.charAt(i);
            if (x == '\r' || x == '\n') continue;
            map.put(slot++, x);
        }

        return ofMap(map);
    }

    /**
     * Creates a pattern that maps a slot to {@link BorderPattern.Border#OUTER} if the slot is at the edge of inventory grid.
     * Other slots are mapped to {@link BorderPattern.Border#INNER}.
     * Slot indices outside the grid are mapped to null.
     * @param width the width of the inventory grid
     * @param height the height of the inventory grid
     * @return the pattern
     */
    public static BorderPattern border(int width, int height) {
        return new BorderPattern(width, height);
    }

    /**
     * A pattern that maps every even slot to {@link CheckerboardPattern.Tile#BLACK} and every odd slot to {@link CheckerboardPattern.Tile#WHITE}.
     * @param size the size of the inventory
     * @return the checkerboard pattern
     */
    public static CheckerboardPattern checkerboard(int size) {
        return new CheckerboardPattern(size, CheckerboardPattern.Tile.BLACK);
    }

    /**
     * A pattern that maps every slot to its index.
     * @return the pattern
     */
    public static Pattern<Integer> ofIndex() {
        return i -> Integer.valueOf(i);
    }

    /**
     * A pattern that maps every slot to its slot type.
     * Indices outside the inventory are mapped to {@link SlotType#OUTSIDE}.
     * @param shape the shape of the inventory.
     * @return the pattern
     */
    public static Pattern<SlotType> ofShape(Shape shape) {
        return shape.toPattern();
    }
}


