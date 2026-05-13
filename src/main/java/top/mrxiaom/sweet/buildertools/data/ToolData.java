package top.mrxiaom.sweet.buildertools.data;

import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteItemNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadableItemNBT;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnusedReturnValue")
public class ToolData {
    public static final String KEY_ID = "SWEET_BUILDER_TOOLS_ID";
    public static final String KEY_UNIQUE = "SWEET_BUILDER_TOOLS_UNIQUE";
    public static final String KEY_PLAYER = "SWEET_BUILDER_TOOLS_PLAYER";
    public static final String KEY_AMOUNT = "SWEET_BUILDER_TOOLS_AMOUNT";
    public static final String KEY_CURRENT = "SWEET_BUILDER_TOOLS_CURRENT";
    public static final String BLOCK_ID = "SBT_ID";

    private String id;
    private String unique;
    private String player;
    private String current;
    private int amount;
    private ToolData() {}

    public String id() {
        return id;
    }

    public ToolData id(String id) {
        this.id = id;
        return this;
    }

    public String unique() {
        return unique;
    }

    public ToolData unique(String unique) {
        this.unique = unique;
        return this;
    }

    public String player() {
        return player;
    }

    public ToolData player(String player) {
        this.player = player;
        return this;
    }

    public String current() {
        return current;
    }

    public ToolData current(String current) {
        this.current = current;
        return this;
    }

    public int amount() {
        return amount;
    }

    public ToolData amount(int amount) {
        this.amount = amount;
        return this;
    }

    public boolean isValid() {
        return id != null && unique != null && player != null && current != null;
    }

    public void saveTo(@Nullable ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR) || item.getAmount() == 0) return;
        NBT.modify(item, nbt -> {
            saveTo(nbt);
        });
    }

    public void saveTo(@NotNull ReadWriteItemNBT nbt) {
        if (id != null) {
            nbt.setString(KEY_ID, id);
        } else if (nbt.hasTag(KEY_ID)) {
            nbt.removeKey(KEY_ID);
        }
        if (unique != null) {
            nbt.setString(KEY_UNIQUE, unique);
        } else if (nbt.hasTag(KEY_UNIQUE)) {
            nbt.removeKey(KEY_UNIQUE);
        }
        if (player != null) {
            nbt.setString(KEY_PLAYER, player);
        } else if (nbt.hasTag(KEY_PLAYER)) {
            nbt.removeKey(KEY_PLAYER);
        }
        if (current != null) {
            nbt.setString(KEY_CURRENT, current);
        } else if (nbt.hasTag(KEY_CURRENT)) {
            nbt.removeKey(KEY_CURRENT);
        }
        nbt.setInteger(KEY_AMOUNT, amount);
    }

    @NotNull
    public static ToolData readFrom(@Nullable ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR) || item.getAmount() == 0) return create();
        return NBT.get(item, nbt -> {
            return readFrom(nbt);
        });
    }

    @NotNull
    public static ToolData readFrom(@NotNull ReadableItemNBT nbt) {
        ToolData data = create();
        if (nbt.hasTag(KEY_ID)) data.id(nbt.getString(KEY_ID));
        if (nbt.hasTag(KEY_UNIQUE)) data.unique(nbt.getString(KEY_UNIQUE));
        if (nbt.hasTag(KEY_PLAYER)) data.player(nbt.getString(KEY_PLAYER));
        if (nbt.hasTag(KEY_CURRENT)) data.current(nbt.getString(KEY_CURRENT));
        if (nbt.hasTag(KEY_AMOUNT)) data.amount(nbt.getInteger(KEY_AMOUNT));
        return data;
    }

    @NotNull
    public static ToolData create() {
        return new ToolData();
    }
}
