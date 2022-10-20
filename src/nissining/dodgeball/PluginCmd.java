package nissining.dodgeball;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;

import java.util.StringJoiner;

public class PluginCmd {

    private static final String label = "/db";
    private static final String[] opCmd = new String[]{
            "cre", "del", "start", "stop", "status"
    };

    private static boolean isOpCmd(String cmd) {
        for (String s : opCmd) {
            if (s.equalsIgnoreCase(cmd)) return true;
        }
        return false;
    }

    public static boolean pitchCommand(CommandSender sender, Command command, String[] args, DodgeBall m) {
        Server server = m.getServer();
        if (command.getLabel().equalsIgnoreCase("hub") && m.config.getBoolean("use_game_cmd")) {
            if (sender instanceof Player)
                ((Player) sender).teleport(server.getDefaultLevel().getSafeSpawn());
            return true;
        }

        if (args.length < 1) {
            if (command.getLabel().equalsIgnoreCase("db"))
                sendCommandHelps(sender);
            return false;
        }

        if (isOpCmd(args[0]) && !sender.isOp()) {
            sender.sendMessage("需要OP权限！");
            return false;
        }

        switch (args[0]) {
            case "cre":
                if (sender instanceof Player) {
                    if (!server.isLevelLoaded(args[1])) {
                        sender.sendMessage("该地图未被加载");
                        return false;
                    }
                    if (args[1].equals(server.getDefaultLevel().getFolderName())) {
                        sender.sendMessage("地图不能是主世界！");
                        return false;
                    }
                    SetupAPI.add((Player) sender, m.getArenaData(args[1]));
                    ((Player) sender).teleport(server.getLevelByName(args[1]).getSafeSpawn());
                } else {
                    sender.sendMessage("请在游戏中输入！");
                }
                break;
            case "del":
                if (m.delRoom(args[1]))
                    sender.sendMessage("已删除房间" + args[1]);
                else
                    sender.sendMessage("删除房间失败！");
                break;
            case "join":
                if (sender instanceof Player) {
                    if (!m.quickJoin((Player) sender, (args.length == 2 ? args[1] : "")))
                        m.disCmd((Player) sender);
                }
                break;
            case "start": // <start|stop> <levelName>
            case "stop":
                if (sender instanceof Player) {
                    String level = args.length == 2 ? args[1] : ((Player) sender).level.getFolderName();
                    boolean isStart = args.length == 1 && args[0].equalsIgnoreCase("start");
                    m.startOrStopRoom(level, isStart);
                }
                break;
            case "reload":
                m.stopAllRooms(true);
                sender.sendMessage("已重新加载房间！");
                break;
            case "status":
                if (args.length == 1) {
                    m.getAvbArenas("").stream()
                            .sorted((a1, a2) -> a2.getAllP().size() - a1.getAllP().size())
                            .forEach(a -> sender.sendMessage(a.toString()));
                } else if (args.length == 2) {
                    sender.sendMessage("[房间状态查看器] 目标：" + args[1]);
                    m.getAvbArenas(args[1]).stream()
                            .sorted((a1, a2) -> a2.getAllP().size() - a1.getAllP().size())
                            .forEach(a -> sender.sendMessage(a.toString()));
                }
                break;
        }
        return true;
    }

    private static void sendCommandHelps(CommandSender p) {
        StringJoiner sj = new StringJoiner("\n- ", DodgeBall.gameName + TextFormat.WHITE + " Help List", "");

        if (p.isOp()) {
            sj.add("")
                    .add(label + " <args> - 主要指令")
                    .add("")
                    .add("cre|del <levelname> - 创建|删除房间")
                    .add("start|stop|status <levelname> - 强制开始|停止|查看房间")
                    .add("reload- 重载房间")
                    .add("");
        } else {
            sj.add("join <levelname> - 快速加入房间");
        }

        sj.add("")
                .add("Plugin By Nissining! Have Fun!");

        p.sendMessage(sj.toString());
    }

}
