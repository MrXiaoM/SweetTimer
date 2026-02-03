package top.mrxiaom.sweet.timer;

import top.mrxiaom.pluginbase.func.language.Language;
import top.mrxiaom.pluginbase.func.language.Message;

import static top.mrxiaom.pluginbase.func.language.LanguageFieldAutoHolder.field;

@Language(prefix="messages.")
public class Messages {
    @Language(prefix="messages.command.")
    public static class Command {
        public static final Message no_permission = field("&c你没有执行该命令的权限");
        public static final Message no_timer = field("&c找不到指定的定时器 %timer%");
        public static final Message reload__success = field("&a配置文件已重载");
    }
}
