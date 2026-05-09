package top.mrxiaom.sweet.buildertools.data;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.gui.LoadedIcon;
import top.mrxiaom.pluginbase.utils.ListPair;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.buildertools.Messages;
import top.mrxiaom.sweet.buildertools.SweetBuilderTools;
import top.mrxiaom.sweet.buildertools.api.IMaterial;

import java.util.*;

public class ToolConfig {
    public static final String KEY_ID = "SWEET_BUILDER_TOOLS_ID";
    public static final String KEY_UNIQUE = "SWEET_BUILDER_TOOLS_UNIQUE";
    public static final String KEY_PLAYER = "SWEET_BUILDER_TOOLS_PLAYER";
    public static final String KEY_AMOUNT = "SWEET_BUILDER_TOOLS_AMOUNT";
    public static final String KEY_CURRENT = "SWEET_BUILDER_TOOLS_CURRENT";
    public static final String BLOCK_ID = "SBT_ID";
    private final @NotNull String id;
    private final boolean enable;
    private final @Nullable String permission;
    private final @NotNull IMaterial placeDefault;
    private final boolean placeDisableDrops;
    private final @NotNull List<IMaterial> placeList;
    private final @NotNull Map<String, IMaterial> placeListByKey;
    private final @Nullable Integer amount;
    private final @NotNull LoadedIcon item;

    private ToolConfig(@NotNull SweetBuilderTools plugin, @NotNull String id, @NotNull ConfigurationSection config) {
        this.id = id;
        this.enable = config.getBoolean("enable", false);
        String permission = config.getString("permission", "none");
        if (permission.equals("none") || permission.trim().isEmpty()) {
            this.permission = null;
        } else {
            this.permission = permission;
        }
        IMaterial placeDefault = plugin.parseMaterial(config.getString("place-blocks.default"));
        if (placeDefault == null) {
            throw new IllegalArgumentException("place-blocks.default 的值无效");
        }
        this.placeDefault = placeDefault;
        this.placeDisableDrops = config.getBoolean("place-blocks.disable-drops", true);
        this.placeList = new ArrayList<>();
        for (String str : config.getStringList("place-blocks.list")) {
            IMaterial material = plugin.parseMaterial(str);
            if (material == null) {
                throw new IllegalArgumentException("place-blocks.list 的值 " + str + " 无效");
            }
            this.placeList.add(material);
        }
        this.placeListByKey = new HashMap<>();
        for (IMaterial material : placeList) {
            this.placeListByKey.put(material.key(), material);
        }
        String amountStr = config.getString("amount");
        if ("infinite".equals(amountStr)) {
            this.amount = null;
        } else {
            int amount = Util.parseInt(amountStr).orElse(0);
            if (amount < 1) {
                throw new IllegalArgumentException("amount 的值无效");
            }
            this.amount = amount;
        }
        this.item = LoadedIcon.load(config, "item");
    }

    public @NotNull String id() {
        return id;
    }

    public boolean enable() {
        return enable;
    }

    public @Nullable String permission() {
        return permission;
    }

    public boolean hasPermission(Permissible p) {
        return permission == null || p.hasPermission(permission);
    }

    public @NotNull IMaterial placeDefault() {
        return placeDefault;
    }

    public boolean placeDisableDrops() {
        return placeDisableDrops;
    }

    public @NotNull List<IMaterial> placeList() {
        return placeList;
    }

    public @NotNull Map<String, IMaterial> placeListByKey() {
        return placeListByKey;
    }

    public @Nullable Integer amount() {
        return amount;
    }

    public @NotNull LoadedIcon item() {
        return item;
    }

    public void addAmountReplacements(List<Pair<String, Object>> r, int amount) {
        r.add(Pair.of("%amount%", amount));
        if (this.amount == null) {
            r.add(Pair.of("%current_amount%", Messages.Item.infinite.str()));
            r.add(Pair.of("%max_amount%", Messages.Item.infinite.str()));
        } else {
            r.add(Pair.of("%current_amount%", this.amount - amount));
            r.add(Pair.of("%max_amount%", this.amount));
        }
    }

    public ItemStack createItem(Player player, int amount) {
        ListPair<String, Object> r = new ListPair<>();
        addAmountReplacements(r, amount);
        ItemStack item = item().generateIcon(player, s -> Pair.replace(s, r), l -> Pair.replace(l, r));
        NBT.modify(item, nbt -> {
            nbt.setString(KEY_ID, id());
            nbt.setString(KEY_UNIQUE, UUID.randomUUID().toString());
            nbt.setString(KEY_PLAYER, player.getUniqueId().toString());
            nbt.setString(KEY_CURRENT, placeDefault().key());
            nbt.setInteger(KEY_AMOUNT, amount);
        });
        return item;
    }

    @Nullable
    public IMaterial getMaterial(@NotNull ItemStack item) {
        return NBT.get(item, nbt -> {
            return placeListByKey().get(nbt.getString(KEY_CURRENT));
        });
    }

    public int getAmount(@NotNull ItemStack item) {
        return NBT.get(item, nbt -> {
            return nbt.getInteger(KEY_AMOUNT);
        });
    }

    public void setAmount(@NotNull ItemStack item, Player player, int amount) {
        NBT.modify(item, nbt -> {
            nbt.setInteger(KEY_AMOUNT, amount);
        });
        ListPair<String, Object> r = new ListPair<>();
        addAmountReplacements(r, amount);
        item().applyItemMeta(item, player, s -> Pair.replace(s, r), l -> Pair.replace(l, r));
    }

    @NotNull
    public static ToolConfig load(SweetBuilderTools plugin, String id, ConfigurationSection config) {
        return new ToolConfig(plugin, id, config);
    }
}
