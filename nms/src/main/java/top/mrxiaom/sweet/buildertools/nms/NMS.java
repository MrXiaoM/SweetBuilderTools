package top.mrxiaom.sweet.buildertools.nms;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;

import java.util.logging.Logger;

public class NMS {
    private static IPlaceBlock placeBlock;

    public static IPlaceBlock getPlaceBlock() {
        return placeBlock;
    }

    public static void init(Logger logger) {
        MinecraftVersion ver = MinecraftVersion.getVersion();
        switch (ver) {
            case UNKNOWN:
            case MC26_2:
                placeBlock = new top.mrxiaom.sweet.buildertools.nms.mojmap_26_2.PlaceBlock();
                break;
            case MC26_1:
                placeBlock = new top.mrxiaom.sweet.buildertools.nms.mojmap_26_1.PlaceBlock();
                break;
            case MC1_21_R7:
                try {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.mojmap_1_21_11.PlaceBlock();
                } catch (LinkageError ignored) {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_21_R7.PlaceBlock();
                }
                break;
            case MC1_21_R6:
                try {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.mojmap_1_21_9.PlaceBlock();
                } catch (LinkageError ignored) {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_21_R6.PlaceBlock();
                }
                break;
            case MC1_21_R5:
                try {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.mojmap_1_21_8.PlaceBlock();
                } catch (LinkageError ignored) {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_21_R5.PlaceBlock();
                }
                break;
            case MC1_21_R4:
                try {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.mojmap_1_21_5.PlaceBlock();
                } catch (LinkageError ignored) {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_21_R4.PlaceBlock();
                }
                break;
            case MC1_21_R3:
                try {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.mojmap_1_21_4.PlaceBlock();
                } catch (LinkageError ignored) {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_21_R3.PlaceBlock();
                }
                break;
            case MC1_21_R2:
                try {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.mojmap_1_21_3.PlaceBlock();
                } catch (LinkageError ignored) {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_21_R2.PlaceBlock();
                }
                break;
            case MC1_21_R1:
                try {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.mojmap_1_21.PlaceBlock();
                } catch (LinkageError ignored) {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_21_R1.PlaceBlock();
                }
                break;
            case MC1_20_R4:
                try {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.mojmap_1_20_6.PlaceBlock();
                } catch (LinkageError ignored) {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_20_R4.PlaceBlock();
                }
                break;
            case MC1_20_R3:
                placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_20_R3.PlaceBlock();
                break;
            case MC1_20_R2:
                placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_20_R2.PlaceBlock();
                break;
            case MC1_20_R1:
                placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_20_R1.PlaceBlock();
                break;
            case MC1_19_R3:
                placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_19_R3.PlaceBlock();
                break;
            case MC1_18_R2:
                placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_18_R2.PlaceBlock();
                break;
            case MC1_17_R1:
                placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_17_R1.PlaceBlock();
                break;
            case MC1_16_R3:
                placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_16_R3.PlaceBlock();
                break;
            case MC1_15_R1:
                try {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_15_R1.PlaceBlock();
                    break;
                } catch (ReflectiveOperationException ignored) {
                }
            case MC1_14_R1:
                try {
                    placeBlock = new top.mrxiaom.sweet.buildertools.nms.v1_14_R1.PlaceBlock();
                    break;
                } catch (ReflectiveOperationException ignored) {
                }
            default:
                logger.warning("插件不适配当前服务端版本");
                break;
        }
    }
}
