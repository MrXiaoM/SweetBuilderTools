package top.mrxiaom.sweet.buildertools;

import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.Message;

import static top.mrxiaom.pluginbase.func.language.LanguageFieldAutoHolder.field;

@Language(prefix = "messages.")
public class Messages {

    @Language(prefix = "messages.item.")
    public static class Item {
        public static final Message infinite = field("无限");
        public static final Message unknown_material = field("未指定方块");
        public static final Message amount__no_need_to_recover = field("&e这个工具无需填充可用方块数量");
        public static final Message amount__recover_success = field("&a已为该工具填充 %recover_amount% 个方块");
    }

    @Language(prefix = "messages.command.")
    public static class Command {
        public static final Message player__only = field("该命令只能由玩家执行");
        public static final Message player__not_online = field("&e玩家不在线 (或不存在)");

        public static final Message give__not_found = field("&e找不到工具&b %id%");
        public static final Message give__success = field("&a已给予玩家&e %player% &b工具&e %id%");

        public static final Message reload__success = field("&a配置文件已重载");
    }
}
