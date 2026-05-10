package top.mrxiaom.sweet.buildertools.data;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteItemNBT;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.IModifier;
import top.mrxiaom.pluginbase.utils.*;
import top.mrxiaom.pluginbase.utils.depend.PAPI;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.ItemMaterial;

import java.util.*;
import java.util.function.Consumer;

import static top.mrxiaom.pluginbase.func.gui.IModifier.fit;

/**
 * 通用界面图标配置
 */
public class ToolConfigItem {
    /**
     * 物品材质
     */
    private final @NotNull ItemMaterial material;
    /**
     * 物品数量
     */
    private final int amount;
    /**
     * 物品显示名称
     */
    private final @NotNull String display;
    /**
     * 物品 Lore
     */
    private final @NotNull List<String> lore;
    /**
     * 物品是否发光，即物品是否添加一个附魔并隐藏，以产生附魔光泽
     */
    private final boolean glow;
    /**
     * 物品的自定义模型标记
     */
    private final @Nullable Integer customModelData;
    /**
     * 物品的额外 NBT
     */
    private final @NotNull Map<String, String> nbtStrings;
    /**
     * 物品的额外 NBT
     */
    private final @NotNull Map<String, String> nbtInts;

    private final @NotNull ConfigurationSection section;

    ToolConfigItem(SweetBuilderTools plugin, ConfigurationSection current) {
        this.section = current;
        ConfigurationSection section;

        String material, materialStr = current.getString("material");
        if (materialStr != null) {
            if (!materialStr.contains(":") && current.contains("data")) { // 兼容旧的选项
                material = materialStr + ":" + current.getInt("data");
            } else material = materialStr;
        } else material = "PAPER";
        this.material = plugin.parseItemMaterial(material, false);

        this.amount = current.getInt("amount", 1);
        this.display = current.getString("display", "");
        this.lore = current.getStringList("lore");
        this.glow = current.getBoolean("glow");
        this.customModelData = current.contains("custom-model-data") ? current.getInt("custom-model-data") : null;
        this.nbtStrings = new HashMap<>();
        section = current.getConfigurationSection("nbt-strings");
        if (section != null) for (String key : section.getKeys(false)) {
            nbtStrings.put(key, section.getString(key, ""));
        }
        this.nbtInts = new HashMap<>();
        section = current.getConfigurationSection("nbt-ints");
        if (section != null) for (String key : section.getKeys(false)) {
            nbtInts.put(key, section.getString(key, ""));
        }
    }

    @NotNull
    public ItemMaterial material() {
        return material;
    }

    public int amount() {
        return amount;
    }

    @NotNull
    public String display() {
        return display;
    }

    @NotNull
    public List<String> lore() {
        return lore;
    }

    public boolean glow() {
        return glow;
    }

    @Nullable
    public Integer customModelData() {
        return customModelData;
    }

    @NotNull
    public Map<String, String> nbtStrings() {
        return nbtStrings;
    }

    @NotNull
    public Map<String, String> nbtInts() {
        return nbtInts;
    }

    @NotNull
    public ConfigurationSection section() {
        return section;
    }

    /**
     * 生成一个新的物品
     *
     * @param player 玩家，用于替换 PAPI 变量
     * @see ToolConfigItem#generateIcon(ItemStack, Player, IModifier, IModifier, Consumer)
     */
    @NotNull
    public ItemStack generateIcon(@NotNull Player player) {
        return generateIcon(player, null, null, null);
    }

    /**
     * 生成一个新的物品
     *
     * @param player              玩家，用于替换 PAPI 变量
     * @param displayNameModifier 物品名称修饰器
     * @param loreModifier        物品Lore修饰器
     * @see ToolConfigItem#generateIcon(ItemStack, Player, IModifier, IModifier, Consumer)
     */
    @NotNull
    public ItemStack generateIcon(@NotNull Player player, @Nullable IModifier<String> displayNameModifier, @Nullable IModifier<List<String>> loreModifier, @Nullable Consumer<ReadWriteItemNBT> extraNBT) {
        if (amount <= 0) return new ItemStack(Material.AIR);
        ItemStack item = material.create(player, amount);
        return generateIcon(item, player, displayNameModifier, loreModifier, extraNBT);
    }

    /**
     * 基于已有物品，覆盖图标配置到该物品上。这个方法会忽略 <code>material</code> 选项。
     *
     * @param item   原物品
     * @param player 玩家，用于替换 PAPI 变量
     * @return <code>item</code> 的引用
     * @see ToolConfigItem#generateIcon(ItemStack, Player, IModifier, IModifier, Consumer)
     */
    @NotNull
    public ItemStack generateIcon(@Nullable ItemStack item, @NotNull Player player) {
        return generateIcon(item, player, null, null, null);
    }

    /**
     * 基于已有物品，覆盖图标配置到该物品上。这个方法会忽略 <code>material</code> 选项。
     *
     * @param item                原物品
     * @param player              玩家，用于替换 PAPI 变量
     * @param displayNameModifier 物品名称修饰器
     * @param loreModifier        物品Lore修饰器
     * @param extraNBT            额外修改物品NBT标签
     * @return 如果 <code>item</code> 不是 <code>null</code>，返回原物品的引用
     */
    @NotNull
    public ItemStack generateIcon(@Nullable ItemStack item, @NotNull Player player, @Nullable IModifier<String> displayNameModifier, @Nullable IModifier<List<String>> loreModifier, @Nullable Consumer<ReadWriteItemNBT> extraNBT) {
        if (item == null || amount == 0) return new ItemStack(Material.AIR);
        item.setAmount(amount);
        applyItemMeta(item, player, displayNameModifier, loreModifier, extraNBT);
        return item;
    }

    /**
     * 应用该图标配置中的 物品名、物品Lore、发光、自定义标记… 等元数据到指定物品
     *
     * @param item   原物品
     * @param player 玩家，用于替换 PAPI 变量
     * @see ToolConfigItem#applyItemMeta(ItemStack, Player, IModifier, IModifier, Consumer)
     */
    public void applyItemMeta(@NotNull ItemStack item, @NotNull Player player) {
        applyItemMeta(item, player, null, null, null);
    }

    /**
     * 应用该图标配置中的 物品名、物品Lore、发光、自定义标记… 等元数据到指定物品
     *
     * @param item                原物品
     * @param player              玩家，用于替换 PAPI 变量
     * @param displayNameModifier 物品名称修饰器
     * @param loreModifier        物品Lore修饰器
     * @param extraNBT            额外修改物品NBT标签
     */
    public void applyItemMeta(@NotNull ItemStack item, @NotNull Player player, @Nullable IModifier<String> displayNameModifier, @Nullable IModifier<List<String>> loreModifier, @Nullable Consumer<ReadWriteItemNBT> extraNBT) {
        if (!display.isEmpty()) {
            String displayName = PAPI.setPlaceholders(player, fit(displayNameModifier, display));
            AdventureItemStack.setItemDisplayName(item, displayName);
        }
        if (!lore.isEmpty()) {
            List<String> loreList = PAPI.setPlaceholders(player, fit(loreModifier, lore));
            AdventureItemStack.setItemLoreMiniMessage(item, loreList);
        }
        if (glow) ItemStackUtil.setGlow(item);
        if (customModelData != null) ItemStackUtil.setCustomModelData(item, customModelData);
        if (!nbtStrings.isEmpty() || !nbtInts.isEmpty()) {
            NBT.modify(item, nbt -> {
                for (Map.Entry<String, String> entry : nbtStrings.entrySet()) {
                    String value = PAPI.setPlaceholders(player, entry.getValue());
                    nbt.setString(entry.getKey(), value);
                }
                for (Map.Entry<String, String> entry : nbtInts.entrySet()) {
                    String value = PAPI.setPlaceholders(player, entry.getValue());
                    Integer i = Util.parseInt(value).orElse(null);
                    if (i == null) continue;
                    nbt.setInteger(entry.getKey(), i);
                }
                if (extraNBT != null) {
                    extraNBT.accept(nbt);
                }
            });
        }
    }

    /**
     * 加载图标配置
     *
     * @param plugin  插件主类
     * @param section 配置
     * @param id      配置根部分的位置键，若为 <code>null</code>，则配置根部分为 <code>section</code>
     */
    public static @NotNull ToolConfigItem load(@NotNull SweetBuilderTools plugin, @NotNull ConfigurationSection section, @Nullable String id) {
        ConfigurationSection current = id == null ? section : section.getConfigurationSection(id);
        if (current == null) throw new IllegalArgumentException("Can't find root section of ToolConfigItem");

        return new ToolConfigItem(plugin, current);
    }
}
