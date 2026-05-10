package top.mrxiaom.sweet.buildertools.data;

import org.bukkit.block.*;
import top.mrxiaom.pluginbase.utils.Util;

import java.util.function.Predicate;

@SuppressWarnings("deprecation")
public enum EnumBlockState {
    Banner(state -> state instanceof Banner, false),
    Barrel(state -> state instanceof Barrel, true),
    Beacon(state -> state instanceof Beacon, true),
    Bed(state -> state instanceof Bed, true),
    Beehive(state -> state instanceof Beehive, false),
    Bell(state -> state instanceof Bell, false),
    BlastFurnace(state -> state instanceof BlastFurnace, true),
    BrewingStand(state -> state instanceof BrewingStand, true),
    BrushableBlock(state -> state instanceof BrushableBlock, false),
    CalibratedSculkSensor(state -> state instanceof CalibratedSculkSensor, false),
    Campfire(state -> state instanceof Campfire, false),
    Chest(state -> state instanceof Chest, true),
    ChiseledBookshelf(state -> state instanceof ChiseledBookshelf, false),
    CommandBlock(state -> state instanceof CommandBlock, true),
    Comparator(state -> state instanceof Comparator, true),
    Conduit(state -> state instanceof Conduit, true),
    Container(state -> state instanceof Container, true),
    CreatureSpawner(state -> state instanceof CreatureSpawner, false),
    DaylightDetector(state -> state instanceof DaylightDetector, true),
    DecoratedPot(state -> state instanceof DecoratedPot, false),
    Dispenser(state -> state instanceof Dispenser, true),
    Dropper(state -> state instanceof Dropper, true),
    EnchantingTable(state -> state instanceof EnchantingTable, true),
    EnderChest(state -> state instanceof EnderChest, true),
    EndGateway(state -> state instanceof EndGateway, true),
    EntityBlockStorage(state -> state instanceof EntityBlockStorage, true),
    Furnace(state -> state instanceof Furnace, true),
    HangingSign(state -> state instanceof HangingSign, true),
    Hopper(state -> state instanceof Hopper, true),
    Jigsaw(state -> state instanceof Jigsaw, true),
    Jukebox(state -> state instanceof Jukebox, false),
    Lectern(state -> state instanceof Lectern, true),
    Lidded(state -> state instanceof Lidded, true),
    Lockable(state -> state instanceof Lockable, true),
    SculkCatalyst(state -> state instanceof SculkCatalyst, false),
    SculkSensor(state -> state instanceof SculkSensor, false),
    SculkShrieker(state -> state instanceof SculkShrieker, false),
    ShulkerBox(state -> state instanceof ShulkerBox, true),
    Sign(state -> state instanceof Sign, true),
    Skull(state -> state instanceof Skull, false),
    Smoker(state -> state instanceof Smoker, true),
    Structure(state -> state instanceof Structure, true),
    SuspiciousSand(state -> state instanceof SuspiciousSand, false),

    ;
    private final String className;
    private final boolean available;
    private final Predicate<BlockState> typeChecker;
    private final boolean hasInteractFunc;
    EnumBlockState(Predicate<BlockState> typeChecker, boolean hasInteractFunc) {
        this.className = "org.bukkit.block." + name();
        this.available = Util.isPresent(className);
        this.typeChecker = typeChecker;
        this.hasInteractFunc = hasInteractFunc;
    }

    public String className() {
        return className;
    }

    public boolean available() {
        return available;
    }

    public boolean hasInteractFunc() {
        return hasInteractFunc;
    }

    public boolean isInstance(BlockState state) {
        return available && typeChecker.test(state);
    }

    public static boolean hasInteractFuncWithBlock(BlockState state) {
        for (EnumBlockState value : values()) {
            if (value.hasInteractFunc() && value.isInstance(state)) {
                return true;
            }
        }
        return false;
    }
}
