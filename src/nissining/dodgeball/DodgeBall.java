package nissining.dodgeball;

import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.*;
import nissining.dodgeball.rooms.Room;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class DodgeBall extends PluginBase {

    public static final String gameName = "§bDodgeBall";
    public List<Room> arenas = new ArrayList<>();
    public Config config;

    // 独服模式
    public static boolean onlyServerMode = false;

    public static StringJoiner howtoplay = new StringJoiner("\n", "How To Play " + gameName, "")
            .add("")
            .add("§l§f游戏开始场地生成DodgeBall")
            .add("使用雪球攻击敌人！")
            .add("如团灭敌人将+1分")
            .add("期间需要跑动躲避敌人的攻击！")
            .add("没有命中的雪球将会在同位置生成！");

    @Override
    public void onEnable() {
        if (!getDataFolder().mkdirs()) {
            debug("DodgeBall MiniGames By Nissining");
        }
        this.config = new Config(getDataFolder() + "/config.yml", 2, new ConfigSection() {{
            put("only_server_mode", false);
            put("use_game_cmd", true);
            put("join_fail_cmd", new ArrayList<>() {{
                add("lobby");
                add("hub");
            }});
            put("win_cmd", new ArrayList<>() {{
                add("give %p 1 %win");
            }});
            put("fail_cmd", new ArrayList<>() {{
                add("give %p 1 %win");
            }});
            put("server_logo", "xxxx.xxxx.xxx");
        }});

        onlyServerMode = config.getBoolean("only_server_mode");

        loadRooms();
        getServer().getPluginManager().registerEvents(new PluginEvents(this), this);
    }

    @Override
    public void onDisable() {
        stopAllRooms(false);
    }

    public Config getArenaData(String levelName) {
        return new Config(getDataFolder() + "/rooms/" + levelName + ".yml", Config.YAML,
                new ConfigSection() {{
                    put("min_players", 2);
                    put("max_players", 8);
                    put("start_time", 30);
                    put("main_time", 1500);
                    put("round", 3);
                    put("map_author", "Nissining");
                }}
        );
    }

    public Config getPlayerData(String pn) {
        return new Config(getDataFolder() + "/players/" + pn + ".yml", 2);
    }

    public static void debug(String debug) {
        MainLogger.getLogger().notice(debug);
    }

    private List<String> getRooms() {
        List<String> rooms = new ArrayList<>();
        File f = new File(getDataFolder(), "rooms/");
        File[] fs = f.listFiles();
        if (fs != null) {
            for (File file : fs) {
                String fn = file.getName().split(".yml")[0];
                rooms.add(fn);
            }
        }
        return rooms;
    }

    private void loadRooms() {
        List<String> rooms = getRooms();
        if (rooms.size() > 0) {
            rooms.forEach(this::loadRoom);
        }
    }

    private void loadRoom(String level) {
        arenas.add(new Room(level, this));
    }

    public boolean delRoom(String ln) {
        Room arena = getArena(ln);
        if (arena != null) {
            arena.stopGame();
            arenas.remove(arena);
        }
        File roomConfig = new File(getDataFolder(), "rooms/" + ln + ".yml");
        return roomConfig.delete();
    }

    public void stopAllRooms(boolean reload) {
        arenas.stream().filter(a -> !a.isCloseRoom()).forEach(Room::stopGame);
        if (reload) {
            arenas.clear();
            loadRooms();
        }
    }

    public void startOrStopRoom(String ln, boolean start) {
        List<Room> tempRooms = getAvbArenas(ln);
        if (start) {
            tempRooms.forEach(room -> room.startGame(true));
        } else {
            tempRooms.forEach(Room::stopGame);
        }
    }

    public void disCmd(Player p) {
        for (String cmd : this.config.getStringList("join_fail_cmd")) {
            getServer().dispatchCommand(p, cmd);
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, cn.nukkit.command.Command command, String label, String[] args) {
        return PluginCmd.pitchCommand(sender, command, args, this);
    }

    /*
     *********************************
                GameNPC API
     *********************************
     */

    /**
     * 游戏名称
     *
     * @return 游戏名
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * 获取所有房间玩家数总和
     *
     * @return 总和
     */
    public int getAllGCount() {
        return arenas.stream().mapToInt(a -> a.getAllP().size()).sum();
    }

    /**
     * 获取已设置好的空闲房间列表
     *
     * @return 空闲房间列表
     */
    public List<Room> getAvbArenas(String ln) {
        List<Room> avbRs = arenas.stream()
                .filter(a -> !a.isCloseRoom())
                .collect(Collectors.toList());

        if (ln.isEmpty())
            return avbRs;
        else
            return avbRs.stream()
                    .filter(r -> r.getLevelName().equals(ln))
                    .collect(Collectors.toList());
    }

    /**
     * 快速加入房间
     *
     * @param player 玩家
     * @param ln     目标地图
     */
    public boolean quickJoin(Player player, String ln) {
        if (getArena(player) != null) {
            player.sendMessage("§c你还在游戏房间！");
            return false;
        }
        Room arena;
        List<Room> avb = getAvbArenas(ln);

        // 如果指定的房间为空
        if (avb.isEmpty()) {
            player.sendMessage("§c没有空闲房间！");
            return false;
        }

        //第一次随机进入房间
        if (getAllGCount() < 1) {
            Collections.shuffle(avb);
            arena = avb.stream()
                    .findFirst()
                    .orElse(null);
        } else {
            arena = avb.stream()
                    .min(((o1, o2) -> o2.getLC() - o1.getLC()))
                    .orElse(null);
        }

        if (arena == null) {
            player.sendMessage("§c房间加入失败！");
            return false;
        }

        if (!arena.joinGame(player)) {
            player.sendMessage("§c房间已满或已开始！");
            return false;
        }
        return true;
    }

    /**
     * 获取房间
     *
     * @param o 玩家|游戏地图
     * @return 房间
     */
    public Room getArena(Object o) {
        for (Room arena : arenas) {
            if (o instanceof Player) {
                if (arena.isInGame((Player) o))
                    return arena;
            }
            if (o instanceof String) {
                if (arena.getLevelName().equals(o))
                    return arena;
            }
        }
        return null;
    }

}
