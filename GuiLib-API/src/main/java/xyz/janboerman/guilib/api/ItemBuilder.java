package xyz.janboerman.guilib.api;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
     * Specify the damage for the items being built.
     * @param damage the damage (0 = full health)
     * @return a new ItemBuilder
     * @throws ClassCastException when the item is not damageable
     */
    public ItemBuilder damage(int damage) {
        return changeMeta(meta -> ((Damageable) meta).setDamage(damage));
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
     * Specify the localised display name of the items being built.
     * @param localisedName the display name
     * @return a new ItemBuilder
     */
    public ItemBuilder localisedName(String localisedName) {
        return changeMeta(meta -> meta.setLocalizedName(localisedName));
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
     * Specify the attribute modifiers for the items being built.
     * @param attributeModifiers the attribute modifiers
     * @return a new ItemBuilder
     */
    public ItemBuilder attributes(Multimap<Attribute, AttributeModifier> attributeModifiers) {
        return changeMeta(meta -> meta.setAttributeModifiers(attributeModifiers));
    }

    /**
     * Specify the attribute modifiers for the items being built.
     * @param attributeModifiers the attribute modifiers
     * @return a new ItemBuilder
     */
    public ItemBuilder attributes(Map.Entry<Attribute, AttributeModifier>... attributeModifiers) {
        return attributes(ImmutableMultimap.copyOf(List.of(attributeModifiers)));
    }

    /**
     * Specify an attribute for the items being built.
     * @param attribute the attribute type
     * @param attributeModifier the attribute modifier
     * @return a new ItemBuilder
     */
    public ItemBuilder addAttribute(Attribute attribute, AttributeModifier attributeModifier) {
        return changeMeta(meta -> meta.addAttributeModifier(attribute, attributeModifier));
    }

    /**
     * Specify attributes to be added to the items being built.
     * @param attributeModifiers the attribute modifiers
     * @return a new ItemBuilder
     */
    public ItemBuilder addAttributes(Multimap<Attribute, AttributeModifier> attributeModifiers) {
        return attributeModifiers.entries().stream().reduce(this, (itemBuilder, entry) -> itemBuilder.addAttribute(entry.getKey(), entry.getValue()), (first, second) -> second);
    }

    /**
     * Specify attributes to be added to the items being built.
     * @param attributeModifiers the attribute modifiers
     * @return a new ItemBuilder
     */
    public ItemBuilder addAttributes(Map.Entry<Attribute, AttributeModifier>... attributeModifiers) {
        ItemBuilder result = this;
        for (var entry : attributeModifiers) {
            result = result.addAttribute(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Specify custom tags for the items being built.
     * @param key the key of the tag
     * @param type the type of the tag
     * @param value the value of the tag
     * @param <T> the tag's primitive type
     * @param <Z> the tag's complex type
     * @return a new ItemBuilder
     */
    public <T, Z> ItemBuilder tag(NamespacedKey key, ItemTagType<T, Z> type, Z value) {
        return changeMeta(meta -> meta.getCustomTagContainer().setCustomTag(key, type, value));
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the byte type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     */
    public ItemBuilder tag(NamespacedKey key, byte value) {
        return tag(key, ItemTagType.BYTE, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the byte[] type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     */
    public ItemBuilder tag(NamespacedKey key, byte[] value) {
        return tag(key, ItemTagType.BYTE_ARRAY, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the double type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     */
    public ItemBuilder tag(NamespacedKey key, double value) {
        return tag(key, ItemTagType.DOUBLE, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the float type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     */
    public ItemBuilder tag(NamespacedKey key, float value) {
        return tag(key, ItemTagType.FLOAT, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the int type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     */
    public ItemBuilder tag(NamespacedKey key, int value) {
        return tag(key, ItemTagType.INTEGER, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the int[] type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     */
    public ItemBuilder tag(NamespacedKey key, int[] value) {
        return tag(key, ItemTagType.INTEGER_ARRAY, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the long type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     */
    public ItemBuilder tag(NamespacedKey key, long value) {
        return tag(key, ItemTagType.LONG, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the long[] type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     */
    public ItemBuilder tag(NamespacedKey key, long[] value) {
        return tag(key, ItemTagType.LONG_ARRAY, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the short type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     */
    public ItemBuilder tag(NamespacedKey key, short value){
        return tag(key, ItemTagType.SHORT, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the String type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     */
    public ItemBuilder tag(NamespacedKey key, String value) {
        return tag(key, ItemTagType.STRING, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the CustomItemTagContainer type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     */
    public ItemBuilder tag(NamespacedKey key, CustomItemTagContainer value) {
        return tag(key, ItemTagType.TAG_CONTAINER, value);
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
