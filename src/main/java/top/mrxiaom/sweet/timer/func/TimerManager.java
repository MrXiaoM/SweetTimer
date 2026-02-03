package top.mrxiaom.sweet.timer.func;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.pluginbase.actions.ActionProviders;
import top.mrxiaom.pluginbase.api.IAction;
import top.mrxiaom.pluginbase.api.IRunTask;
import top.mrxiaom.pluginbase.func.AutoRegister;
import top.mrxiaom.pluginbase.utils.ConfigUtils;
import top.mrxiaom.pluginbase.utils.Util;
import top.mrxiaom.sweet.timer.SweetTimer;
import top.mrxiaom.sweet.timer.config.TimerConfig;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@AutoRegister
public class TimerManager extends AbstractModule {
    private final File dataFile;
    private boolean debug = false;
    private final Map<String, TimerConfig> configList = new HashMap<>();
    private IRunTask timerTask = null;
    public TimerManager(SweetTimer plugin) {
        super(plugin);
        this.dataFile = plugin.resolve("./data.yml");
    }

    public Set<String> keys() {
        return configList.keySet();
    }

    public TimerConfig get(String id) {
        return configList.get(id);
    }

    @Override
    public void reloadConfig(MemoryConfiguration pluginConfig) {
        debug = pluginConfig.getBoolean("debug", false);
        File folder = plugin.resolve(pluginConfig.getString("timer-folder", "./timer"));
        if (!folder.exists()) {
            Util.mkdirs(folder);
            plugin.saveResource("timer/example.yml", new File(folder, "example.yml"));
        }
        YamlConfiguration dataConfig = ConfigUtils.load(dataFile);
        configList.clear();
        Util.reloadFolder(folder, false, (idRaw, file) -> {
            String id = idRaw.replace("\\", "/");
            YamlConfiguration config = ConfigUtils.load(file);
            try {
                TimerConfig loaded = new TimerConfig(plugin, id, config, dataConfig);
                configList.put(id, loaded);
            } catch (Throwable t) {
                warn("加载定时器配置 " + id + " 时出现异常", t);
            }
        });
        info("[timer] 共加载了 " + configList.size() + " 个定时器配置");
        timerTask = plugin.getScheduler().runTaskTimerAsync(this::doSchedulerCheck, 1L, pluginConfig.getLong("check-period", 20));
    }

    public void doSchedulerCheck() {
        boolean save = false;
        List<TimerConfig> listExecute = new ArrayList<>();
        List<TimerConfig> listDeny = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (TimerConfig timer : configList.values()) {
            // 不在指定时间内的定时器不运行
            if (timer.isNotInTime(now)) {
                if (debug) info("[调试] 定时器配置 " + timer.getId() + " 不在允许的运行时间以内");
                continue;
            }
            TimerConfig.Data data = timer.getData();
            // 判定成功次数已到达指定次数的定时器不运行
            if (timer.getSuccessRunRound() >= 0 && data.getSuccessRoundCount() >= timer.getSuccessRunRound()) {
                if (debug) info("[调试] 定时器配置 " + timer.getId() + " 已到达允许运行次数上限");
                continue;
            }
            long currentPassRound = timer.getCurrentPassRound(now);
            long lastRound = data.getLastRound();
            if (currentPassRound > lastRound) {
                save = true;
                data.setLastRound(currentPassRound);
                if (currentPassRound - lastRound != 1) {
                    if (debug) info("[调试] 定时器配置 " + timer.getId() + " 上一轮次(" + lastRound + ")与当前轮次(" + currentPassRound + ")相差大于1，服务器可能经过重启或修改配置，不执行自动上架操作");
                    continue;
                }
                long currentRoundSeconds = timer.getCurrentRoundSeconds(now);
                if (currentRoundSeconds <= timer.getGivingUpGap().getTotalSeconds()) {
                    // 运行定时器检查
                    if (!timer.doConditionCheck(now)) {
                        if (debug) info("[调试] 定时器配置 " + timer.getId() + " 的条件检查不通过");
                        listDeny.add(timer);
                        continue;
                    }
                    if (debug) info("[调试] 定时器配置 " + timer.getId() + " 计划运行上架任务");

                    // 计划运行定时器任务
                    listExecute.add(timer);
                }
            }
        }
        if (!listDeny.isEmpty()) {
            for (TimerConfig timer : listDeny) {
                runActions(timer.conditionDenyActions);
            }
        }
        if (!listExecute.isEmpty()) {
            for (TimerConfig timer : listExecute) {
                if (debug) info("[调试] 正在执行定时器 " + timer.getId() + " 的任务内容");
                runActions(timer.executorRunActions);
                runActions(timer.getExecutorRandomActions());
            }
        }
        if (save) saveData();
    }

    private void runActions(List<IAction> actions) {
        if (actions == null || actions.isEmpty()) return;
        plugin.getScheduler().runTask(() -> ActionProviders.run(plugin, null, actions));
    }

    /**
     * 保存自动上架的定时器数据
     */
    public void saveData() {
        YamlConfiguration data = new YamlConfiguration();
        for (TimerConfig property : configList.values()) {
            property.getData().save(data);
        }
        try {
            ConfigUtils.save(data, dataFile);
        } catch (IOException e) {
            warn(e);
        }
    }

    @Override
    public void onDisable() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    public static TimerManager inst() {
        return instanceOf(TimerManager.class);
    }
}
