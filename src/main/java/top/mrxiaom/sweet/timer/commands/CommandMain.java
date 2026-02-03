package top.mrxiaom.sweet.timer.commands;

import com.google.common.collect.Lists;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.Pair;
import top.mrxiaom.sweet.timer.Messages;
import top.mrxiaom.sweet.timer.SweetTimer;
import top.mrxiaom.sweet.timer.config.TimerConfig;
import top.mrxiaom.sweet.timer.func.AbstractModule;
import top.mrxiaom.sweet.timer.func.TimerManager;

import java.time.LocalDateTime;
import java.util.*;

@AutoRegister
public class CommandMain extends AbstractModule implements CommandExecutor, TabCompleter, Listener {
    public CommandMain(SweetTimer plugin) {
        super(plugin);
        registerCommand("sweettimer", this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (sender.isOp()) {
            if (args.length == 2 && "print".equalsIgnoreCase(args[0])) {
                String timerName = args[1];
                TimerManager manager = TimerManager.inst();
                TimerConfig timer = manager.get(timerName);
                if (timer == null) {
                    return Messages.Command.no_timer.tm(sender, Pair.of("%timer%", timerName));
                }
                timer.print(sender);
                return true;
            }
            if (args.length == 2 && "condition".equalsIgnoreCase(args[0])) {
                String timerName = args[1];
                TimerManager manager = TimerManager.inst();
                TimerConfig timer = manager.get(timerName);
                if (timer == null) {
                    return Messages.Command.no_timer.tm(sender, Pair.of("%timer%", timerName));
                }
                if (timer.getSuccessRunRound() >= 0 && timer.getData().getSuccessRoundCount() >= timer.getSuccessRunRound()) {
                    return t(sender, "&e自动上架配置 &b" + timer.getId() + " &e已到达允许运行次数上限");
                }
                int reason = timer.doConditionCheckWithReason(LocalDateTime.now());
                switch (reason) {
                    case 0:
                        return t(sender, "&a自动上架配置 &e" + timer.getId() + "&a 条件测试通过");
                    case 1:
                        return t(sender, "&e自动上架配置 &b" + timer.getId() + "&e 表达式条件测试不通过");
                    case 2:
                        return t(sender, "&e自动上架配置 &b" + timer.getId() + "&e 概率条件测试不通过");
                    case 3:
                        return t(sender, "&e自动上架配置 &b" + timer.getId() + "&e 星期条件测试不通过");
                    case 4:
                        return t(sender, "&e自动上架配置 &b" + timer.getId() + "&e 月份条件测试不通过");
                }
                return t(sender, "&e自动上架配置 &b" + timer.getId() + "&e 条件测试不通过，未知的测试结果代码 &b" + reason);
            }
            if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
                plugin.reloadConfig();
                return Messages.Command.reload__success.tm(sender);
            }
            return true;
        }
        return Messages.Command.no_permission.tm(sender);
    }

    private static final List<String> listArg0 = Lists.newArrayList();
    private static final List<String> listOpArg0 = Lists.newArrayList("print", "condition", "reload");
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return startsWith(sender.isOp() ? listOpArg0 : listArg0, args[0]);
        }
        if (args.length == 2) {
            if (sender.isOp()) {
                if ("print".equalsIgnoreCase(args[0]) || "condition".equalsIgnoreCase(args[0])) {
                    return startsWith(TimerManager.inst().keys(), args[1]);
                }
            }
        }
        return Collections.emptyList();
    }

    public List<String> startsWith(Collection<String> list, String s) {
        String s1 = s.toLowerCase();
        List<String> stringList = new ArrayList<>(list);
        stringList.removeIf(it -> !it.toLowerCase().startsWith(s1));
        return stringList;
    }
}
