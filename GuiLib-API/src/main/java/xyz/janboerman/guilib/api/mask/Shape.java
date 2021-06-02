package xyz.janboerman.guilib.api.mask;

import org.bukkit.entity.Llama;
import org.bukkit.entity.Mule;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.inventory.AbstractHorseInventory;
import org.bukkit.inventory.HorseInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.LlamaInventory;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents the shape of an inventory.
 */
public interface Shape {

    public static final Shape ANVIL = combine(generic(2, SlotType.CRAFTING), generic(1, SlotType.RESULT));
    public static final Shape BARREL = grid(9, 3, SlotType.CONTAINER);
    public static final Shape BEACON = generic(1, SlotType.CRAFTING);
    public static final Shape BLAST_FURNACE = combine(generic(1, SlotType.CRAFTING), generic(1, SlotType.FUEL), generic(1, SlotType.RESULT));
    public static final Shape BREWING = combine(generic(3, SlotType.RESULT), generic(1, SlotType.CRAFTING), generic(1, SlotType.FUEL));
    public static final Shape CARTOGRAPHY = combine(generic(2, SlotType.CRAFTING), generic(1, SlotType.RESULT));
    public static final Shape CHEST1 = chest(1);
    public static final Shape CHEST2 = chest(2);
    public static final Shape CHEST3 = chest(3);
    public static final Shape CHEST4 = chest(4);
    public static final Shape CHEST5 = chest(5);
    public static final Shape CHEST6 = chest(6);
    public static final Shape CRAFTING = combine(grid(2, 2, SlotType.CRAFTING), generic(1, SlotType.RESULT));
    public static final Shape CREATIVE = grid(9, 1, SlotType.QUICKBAR);
    public static final Shape DISPENSER = grid(3, 3, SlotType.CONTAINER);
    public static final Shape DROPPER = grid(3, 3, SlotType.CONTAINER);
    public static final Shape ENDER_CHEST = chest(3);
    public static final Shape FURNACE = combine(generic(1, SlotType.CRAFTING), generic(1, SlotType.FUEL), generic(1, SlotType.RESULT));
    public static final Shape GRINDSTONE = combine(generic(2, SlotType.CRAFTING), generic(1, SlotType.RESULT));
    public static final Shape HOPPER = grid(5, 1, SlotType.CONTAINER);
    public static final Shape LECTERN = generic(1, SlotType.CONTAINER/*TODO SlotType.BOOK*/);
    public static final Shape LOOM = combine(generic(3, SlotType.CRAFTING), generic(1, SlotType.RESULT));
    public static final Shape MERCHANT = combine(generic(2, SlotType.CRAFTING), generic(1, SlotType.RESULT));
    public static final Shape PLAYER = combine(grid(9, 1, SlotType.QUICKBAR), grid(9, 3, SlotType.CONTAINER), generic(4, SlotType.ARMOR), generic(1, SlotType.CONTAINER /*off hand*/));
    public static final Shape SHULKER_BOX = grid(3, 3, SlotType.CONTAINER);
    public static final Shape SMITHING = combine(generic(2, SlotType.CRAFTING), generic(1, SlotType.RESULT));
    public static final Shape SMOKER = combine(generic(1, SlotType.CRAFTING), generic(1, SlotType.FUEL), generic(1, SlotType.RESULT));
    public static final Shape STONECUTTER = combine(generic(1, SlotType.CRAFTING), generic(1, SlotType.RESULT));
    public static final Shape WORKBENCH = combine(grid(3, 3, SlotType.CRAFTING), generic(1, SlotType.RESULT));
    public static final Shape HORSE = generic(2, SlotType.ARMOR);
    public static final Shape MULE = generic(1, SlotType.ARMOR);
    public static final Shape CHEST_MULE = combine(MULE, grid(5, 3, SlotType.CONTAINER));
    public static final Shape LLAMA = generic(1, SlotType.ARMOR);
    public static final Shape CHEST_LLAMA = combine(LLAMA, grid(3, 3, SlotType.CONTAINER));

    public static Shape determine(Inventory inventory) {
        switch (inventory.getType()) {
            case ANVIL: return ANVIL;
            case BARREL: return BARREL;
            case BEACON: return BEACON;
            case BLAST_FURNACE: return BLAST_FURNACE;
            case BREWING: return BREWING;
            case CARTOGRAPHY: return CARTOGRAPHY;
            case CHEST:
                switch (inventory.getSize()){
                    case 9: return CHEST1;
                    case 18: return CHEST2;
                    case 27: return CHEST3;
                    case 36: return CHEST4;
                    case 45: return CHEST5;
                    case 54: return CHEST6;
                    default: assert false; return chest(inventory.getSize() / 9); //impossibru!
                }
            case CRAFTING: return CRAFTING;
            case CREATIVE: return CREATIVE;
            case DISPENSER: return DISPENSER;
            case DROPPER: return DROPPER;
            case ENDER_CHEST: return ENDER_CHEST;
            case FURNACE: return FURNACE;
            case GRINDSTONE: return GRINDSTONE;
            case HOPPER: return HOPPER;
            case LECTERN: return LECTERN;
            case LOOM: return LOOM;
            case MERCHANT: return MERCHANT;
            case PLAYER: return PLAYER;
            case SHULKER_BOX: return SHULKER_BOX;
            case SMITHING: return SMITHING;
            case SMOKER: return SMOKER;
            case STONECUTTER: return STONECUTTER;
            case WORKBENCH: return WORKBENCH;
            default:
                //there does not seem to be any horse- or llama inventory type.
                //I think bukkit should have these. (with isCreatable() returning false).

                if (inventory instanceof LlamaInventory) {
                    if (((Llama) inventory.getHolder()).isCarryingChest()) {
                        return CHEST_LLAMA;
                    } else {
                        return LLAMA;
                    }
                } else if (inventory instanceof HorseInventory) {
                    return HORSE;
                } else if (inventory instanceof AbstractHorseInventory) {
                    if (((Mule) inventory.getHolder()).isCarryingChest()) {
                        return CHEST_MULE;
                    } else {
                        return MULE;
                    }
                }

                //fallback
                assert false; return generic(inventory.getSize(), SlotType.CONTAINER);
        }
    }

    public int size();

    public Pattern<SlotType> toPattern();

    static GridShape chest(int rows) {
        return grid(9, rows, SlotType.CONTAINER);
    }

    static GenericShape generic(int size, SlotType slotType) {
        return new GenericShape(size, slotType);
    }

    static CombinedShape combine(Shape... shapes) {
        Objects.requireNonNull(shapes, "shapes cannot be null");
        if (shapes.length <= 1) throw new IllegalArgumentException("It is non-sensical to combine less than 2 shapes");

        return new CombinedShape(shapes);
    }

    static GridShape grid(int width, int height, SlotType slotType) {
        return new GridShape(height, width, slotType);
    }

}

final class GenericShape implements Shape {

    private final int size;
    private final SlotType slotType;

    GenericShape(int size, SlotType slotType) {
        this.size = size;
        this.slotType = slotType;
    }

    @Override
    public int size() {
        return getSize();
    }

    @Override
    public Pattern<SlotType> toPattern() {
        return i -> {
            if (0 <= i && i < size()) {
                return getSlotType();
            } else  {
                return SlotType.OUTSIDE;
            }
        };
    }

    int getSize() {
        return size;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof GenericShape)) return false;

        GenericShape that = (GenericShape) o;
        return this.getSize() == that.getSize();
    }

    @Override
    public int hashCode() {
        return size;
    }

    @Override
    public String toString() {
        return "GenericShape(size=" + getSize() + ")";
    }
}

final class CombinedShape implements Shape {

    private final Shape[] shapes;

    CombinedShape(Shape... shapes) {
        this.shapes = shapes;
    }

    @Override
    public int size() {
        return Arrays.stream(shapes).mapToInt(Shape::size).sum();
    }

    @Override
    public Pattern<SlotType> toPattern() {
        return slotIndex -> {
            if (slotIndex < 0) return SlotType.OUTSIDE;

            int sum = 0;
            int shapeIndex = 0;

            do {
                int lastSum = sum;

                Shape shape = shapes[shapeIndex];
                sum += shape.size();

                if (slotIndex < sum) {
                    int innerIndex = slotIndex - lastSum;
                    return shape.toPattern().getSymbol(innerIndex);
                }

                shapeIndex += 1;
            } while (shapeIndex < shapes.length);

            return SlotType.OUTSIDE;
        };
    }

    Shape[] getShapes() {
        return shapes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CombinedShape)) return false;

        CombinedShape that = (CombinedShape) o;
        return Arrays.equals(this.getShapes(), that.getShapes());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(shapes);
    }

    @Override
    public String toString() {
        return "CombinedShape(shapes=" + Arrays.toString(shapes) + ")";
    }
}

final class GridShape implements Shape {

    private final int rows, columns;
    private final SlotType slotType;

    GridShape(int rows, int columns, SlotType slotType) {
        this.rows = rows;
        this.columns = columns;
        this.slotType = slotType;
    }

    @Override
    public int size() {
        return getRows() * getColumns();
    }

    @Override
    public Pattern<SlotType> toPattern() {
        return slotIndex -> {
            if (slotIndex < 0 || slotIndex >= size()) {
                return SlotType.OUTSIDE;
            } else {
                return getSlotType();
            }
        };
    }

    int getRows() {
        return rows;
    }

    int getColumns() {
        return columns;
    }

    public SlotType getSlotType() {
        return slotType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof GridShape)) return false;

        GridShape that = (GridShape) o;
        return this.getRows() == that.getRows()
                && this.getColumns() == that.getColumns();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRows(), getColumns());
    }

    @Override
    public String toString() {
        return "GridShape(rows=" + getRows() + ",columns=" + getColumns() + ")";
    }
}
