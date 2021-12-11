package xyz.janboerman.guilib.api;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
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
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility to create ItemStacks.
 * <p>
 * Every builder method in this class returns a new ItemBuilder instance
 * so that instances can be reused.
 */
public class ItemBuilder {
    //TODO if JEP 303 ever gets accepted, update this class to use a custom?? 'immutable' itemstack - which will be Constable.
    //TODO such that the compiler only generates an LDC for the itemstack being built (and maybe a conversion?) in the class of the caller.

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
     * Specify the display name of the items being built.
     * @param displayName the display name
     * @return a new ItemBuilder
     */
    public ItemBuilder name(Component displayName) {
        return changeMeta(meta -> meta.displayName(displayName));
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
    public ItemBuilder loreC(List<Component> lore) {
        return changeMeta(meta -> meta.lore(lore));
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
     * Set custom model data for the items being built.
     * @param customModelData an integer that may be associated client side with a custom item model
     * @return a new ItemBuilder
     */
    public ItemBuilder customModelData(Integer customModelData) {
        return changeMeta(meta -> meta.setCustomModelData(customModelData));
    }

    /**
     * Change the persistent data of the ItemMeta of the ItemStack being built.
     * @param consumer the PersistentDataContainer consumer
     * @return a new ItemBuilder
     * @see #persistentData(NamespacedKey, PersistentDataType, Object)
     */
    public ItemBuilder changePersistentData(Consumer<? super PersistentDataContainer> consumer) {
        return changeMeta(meta -> consumer.accept(meta.getPersistentDataContainer()));
    }

    /**
     * Set custom data on the item that will persist when the item is being transferred to different inventories,
     * falls on the ground and will persist after server restarts.
     * @param key the key of the data
     * @param type the type of the data
     * @param value the value of the data
     * @param <T> the data's primitive type
     * @param <Z> the data's complex type
     * @return a new ItemBuilder
     * @see #persistentData(NamespacedKey, byte)
     * @see #persistentData(NamespacedKey, byte[])
     * @see #persistentData(NamespacedKey, double)
     * @see #persistentData(NamespacedKey, float)
     * @see #persistentData(NamespacedKey, int)
     * @see #persistentData(NamespacedKey, int[])
     * @see #persistentData(NamespacedKey, long)
     * @see #persistentData(NamespacedKey, long[])
     * @see #persistentData(NamespacedKey, short)
     * @see #persistentData(NamespacedKey, String)
     * @see #persistentData(NamespacedKey, PersistentDataContainer)
     * @see #persistentData(NamespacedKey, PersistentDataContainer[])
     */
    public <T, Z> ItemBuilder persistentData(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        return changePersistentData(data -> data.set(key, type, value));
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the byte type.
     * @param key the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(NamespacedKey key, byte value) {
        return persistentData(key, PersistentDataType.BYTE, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the byte[] type.
     * @param key the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(NamespacedKey key, byte[] value) {
        return persistentData(key, PersistentDataType.BYTE_ARRAY, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the double type.
     * @param key the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(NamespacedKey key, double value) {
        return persistentData(key, PersistentDataType.DOUBLE, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the float type.
     * @param key the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(NamespacedKey key, float value) {
        return persistentData(key, PersistentDataType.FLOAT, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the int type.
     * @param key the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(NamespacedKey key, int value) {
        return persistentData(key, PersistentDataType.INTEGER, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the int[] type.
     * @param key the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(NamespacedKey key, int[] value) {
        return persistentData(key, PersistentDataType.INTEGER_ARRAY, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the long type.
     * @param key the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(NamespacedKey key, long value) {
        return persistentData(key, PersistentDataType.LONG, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the long[] type.
     * @param key the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(NamespacedKey key, long[] value) {
        return persistentData(key, PersistentDataType.LONG_ARRAY, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the short type.
     * @param key the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(NamespacedKey key, short value) {
        return persistentData(key, PersistentDataType.SHORT, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the String type.
     * @param key the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(NamespacedKey key, String value) {
        return persistentData(key, PersistentDataType.STRING, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the PersistentDataContainer type.
     * @param key the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(NamespacedKey key, PersistentDataContainer value) {
        return persistentData(key, PersistentDataType.TAG_CONTAINER, value);
    }

    /**
     * Specialised case of {@link #persistentData(NamespacedKey, PersistentDataType, Object)} for the PersistentDataContainer[] type.
     * @param key the key of the data
     * @param value the value of the data
     * @return a new ItemBuilder
     */
    public ItemBuilder persistentData(NamespacedKey key, PersistentDataContainer[] value) {
        return persistentData(key, PersistentDataType.TAG_CONTAINER_ARRAY, value);
    }

    // deprecated CustomTagContainer variants of persistentData

    /**
     * Specify custom tags for the items being built.
     * @param key the key of the tag
     * @param type the type of the tag
     * @param value the value of the tag
     * @param <T> the tag's primitive type
     * @param <Z> the tag's complex type
     * @return a new ItemBuilder
     * @deprecated Use {@link #persistentData(NamespacedKey, PersistentDataType, Object)} instead.
     */
    @Deprecated
    public <T, Z> ItemBuilder tag(NamespacedKey key, ItemTagType<T, Z> type, Z value) {
        return changeMeta(meta -> meta.getCustomTagContainer().setCustomTag(key, type, value));
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the byte type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     * @deprecated Use {@link #persistentData(NamespacedKey, byte)} instead.
     */
    @Deprecated
    public ItemBuilder tag(NamespacedKey key, byte value) {
        return tag(key, ItemTagType.BYTE, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the byte[] type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     * @deprecated Use {@link #persistentData(NamespacedKey, byte[])} instead.
     */
    @Deprecated
    public ItemBuilder tag(NamespacedKey key, byte[] value) {
        return tag(key, ItemTagType.BYTE_ARRAY, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the double type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     * @deprecated Use {@link #persistentData(NamespacedKey, double)} instead.
     */
    @Deprecated
    public ItemBuilder tag(NamespacedKey key, double value) {
        return tag(key, ItemTagType.DOUBLE, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the float type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     * @deprecated Use {@link #persistentData(NamespacedKey, float)} instead.
     */
    @Deprecated
    public ItemBuilder tag(NamespacedKey key, float value) {
        return tag(key, ItemTagType.FLOAT, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the int type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     * @deprecated Use {@link #persistentData(NamespacedKey, int)} instead.
     */
    @Deprecated
    public ItemBuilder tag(NamespacedKey key, int value) {
        return tag(key, ItemTagType.INTEGER, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the int[] type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     * @deprecated Use {@link #persistentData(NamespacedKey, int[])} instead.
     */
    @Deprecated
    public ItemBuilder tag(NamespacedKey key, int[] value) {
        return tag(key, ItemTagType.INTEGER_ARRAY, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the long type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     * @deprecated Use {@link #persistentData(NamespacedKey, long)} instead.
     */
    @Deprecated
    public ItemBuilder tag(NamespacedKey key, long value) {
        return tag(key, ItemTagType.LONG, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the long[] type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     * @deprecated Use {@link #persistentData(NamespacedKey, long[])} instead.
     */
    @Deprecated
    public ItemBuilder tag(NamespacedKey key, long[] value) {
        return tag(key, ItemTagType.LONG_ARRAY, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the short type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     * @deprecated Use {@link #persistentData(NamespacedKey, short)} instead.
     */
    @Deprecated
    public ItemBuilder tag(NamespacedKey key, short value) {
        return tag(key, ItemTagType.SHORT, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the String type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     * @deprecated Use {@link #persistentData(NamespacedKey, String)} instead.
     */
    @Deprecated
    public ItemBuilder tag(NamespacedKey key, String value) {
        return tag(key, ItemTagType.STRING, value);
    }

    /**
     * Specialised case of {@link #tag(NamespacedKey, ItemTagType, Object)} for the CustomItemTagContainer type.
     * @param key the key of the tag
     * @param value the value of the tag
     * @return a new ItemBuilder
     * @deprecated Use {@link #persistentData(NamespacedKey, PersistentDataContainer)} instead.
     */
    @Deprecated
    public ItemBuilder tag(NamespacedKey key, CustomItemTagContainer value) {
        return tag(key, ItemTagType.TAG_CONTAINER, value);
    }
    // It was nice knowing you Bukkit 1.13.2, thanks for the first ever official nbt (although not really) api.

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
