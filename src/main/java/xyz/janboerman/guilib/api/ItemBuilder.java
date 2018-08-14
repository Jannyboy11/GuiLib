package xyz.janboerman.guilib.api;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility to create ItemStacks.
 * <p>
 * Every builder method in this class returns a new ItemBuilder instance
 * so that instances can be reused.
 */
public class ItemBuilder {
    private final ItemStack itemStack;

    /**
     * Creates a new ItemBuilder with the given Material.
     * @param material the Material
     */
    public ItemBuilder(Material material) {
        this.itemStack = new ItemStack(Objects.requireNonNull(material, "Material cannot be null"));
    }

    /**
     * Creates a new ItemBuilder with the given ItemStack.
     * The ItemBuilder clones the ItemStack so that it won't change its state.
     * @param itemStack the ItemStack
     */
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = Objects.requireNonNull(itemStack, "ItemStack cannot be null").clone();
    }

    /**
     * Specify the number of items being built.
     * @param amount the number of items
     * @return a new ItemBuilder
     */
    public ItemBuilder amount(int amount) {
        return change(i -> i.setAmount(amount));
    }

    /**
     * Specify an enchantment for the items being built.
     * @param enchantment the enchantment type
     * @param level the enchantment level
     * @return a new ItemBuilder
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        return change(i -> i.addUnsafeEnchantment(enchantment, level));
    }

    /**
     * Specify that an enchanment is removed from the item being built.
     * @param enchantment the enchantment type to remove
     * @return a new ItemBuilder
     */
    public ItemBuilder unEnchant(Enchantment enchantment) {
        return change(i -> i.removeEnchantment(enchantment));
    }

    /**
     * Specify the data for items being built.
     * @param data the data
     * @return a new ItemBuilder
     * @deprecated MaterialData is deprecated since Bukkit 1.13
     */
    @Deprecated(forRemoval = true, since = "1.5.0")
    public ItemBuilder data(MaterialData data) {
        return change(i -> i.setData(data));
    }

    /**
     * Specify the data for the items being built.
     * @param data the data
     * @return a new ItemBuilder
     * @deprecated magic value
     */
    @Deprecated(forRemoval = true, since = "1.5.0")
    public ItemBuilder data(byte data) {
        return change(i -> {
            MaterialData materialData = i.getData();
            materialData.setData(data);
            i.setData(materialData);
        });
    }

    /**
     * Specify the durability for the items being built.
     * @param durability the damage (0 = full health)
     * @return a new ItemBuilder
     */
    public ItemBuilder durability(short durability) {
        return change(i -> i.setDurability(durability));
    }

    /**
     * Specify the type of the items being built.
     * @param type the type
     * @return a new ItemBuilder
     */
    public ItemBuilder type(Material type) {
        return change(i -> i.setType(type));
    }

    /**
     * Specify the display name of the items being built.
     * @param displayName the display name
     * @return a new ItemBuilder
     */
    public ItemBuilder name(String displayName) {
        return changeMeta(meta -> meta.setDisplayName(displayName));
    }

    /**
     * Specify the lore of the items being built.
     * @param lore the lore
     * @return a new ItemBuilder
     */
    public ItemBuilder lore(List<String> lore) {
        return changeMeta(meta -> meta.setLore(lore));
    }

    /**
     * Specify the lore of the items being built.
     * @param lore the lore
     * @return a new ItemBuilder
     */
    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    /**
     * Add a line of lore to the items being built.
     * @param line th elore
     * @return a new ItemBuilder
     */
    public ItemBuilder addLore(String line) {
        List<String> lore = itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore() ?
                new ArrayList<>(itemStack.getItemMeta().getLore()) : new ArrayList<>();
        lore.add(line);
        return lore(lore);
    }

    /**
     * Specify that the items being built are unbreakable
     * @return a new ItemBuilder
     */
    public ItemBuilder unbreakable() {
        return unbreakable(true);
    }

    /**
     * Specify that the items builing built are (un)breakable
     * @param unbreakable unbreakable when true, breakable when false
     * @return a new ItemBuilder
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        return changeMeta(meta -> meta.setUnbreakable(unbreakable));
    }

    /**
     * Specify the item flags for the items being built.
     * @param flags the flags
     * @return a new ItemBuilder
     */
    public ItemBuilder flags(ItemFlag... flags) {
        return changeMeta(meta -> meta.addItemFlags(flags));
    }

    /**
     * Customise meta for the items being built. Example:
     *
     * <pre> {@code
     * ItemStack bookStack = new ItemBuilder(Material.WRITTEN_BOOK)
     *         .changeMeta((BookMeta bookMeta) -> bookMeta.setAuthor("Jannyboy11"))
     *         .build();
     * }</pre>
     *
     * This method is especially helpful when dealing with subclasses of {@link ItemMeta}.
     *
     * @param consumer the ItemMeta consumer
     * @param <IM> the type of meta
     * @return a new ItemBuilder
     */
    @SuppressWarnings("unchecked")
    public <IM extends ItemMeta> ItemBuilder changeMeta(Consumer<IM> consumer) {
        return changeItemMeta(m -> consumer.accept((IM) m));
    }

    /**
     * Customise the meta for items being built.
     * @param consumer the ItemMeta consumer
     * @return a new ItemBuilder
     */
    public ItemBuilder changeItemMeta(Consumer<? super ItemMeta> consumer) {
        return change(i -> {
            ItemMeta meta = i.getItemMeta();
            consumer.accept(meta);
            i.setItemMeta(meta);
        });
    }

    /**
     * Customise the items being built.
     * @param consumer the item consumer
     * @return a new ItemBuilder
     */
    public ItemBuilder change(Consumer<? super ItemStack> consumer) {
        ItemBuilder builder = new ItemBuilder(itemStack);
        consumer.accept(builder.itemStack);
        return builder;
    }

    /**
     * Apply a function of type ItemStack -&gt; ItemStack to the items being built.
     * @param function the ItemStack mapper
     * @return a new ItemBuilder
     */
    public ItemBuilder map(Function<? super ItemStack, ? extends ItemStack> function) {
        return new ItemBuilder(function.apply(build()));
    }

    /**
     * Builds the ItemStack.
     * @return the result of this builder
     */
    public ItemStack build() {
        return itemStack.clone();
    }

}
