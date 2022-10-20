package nissining.dodgeball.rooms;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import nissining.dodgeball.DodgeBall;
import nissining.dodgeball.scores.ScoreboardAPI;
import nissining.dodgeball.tasks.*;
import nissining.dodgeball.utils.Progress;
import nissining.dodgeball.utils.TeamAPI;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Room {

    public Config data;
    private final String levelName;
    public Server server;
    public DodgeBall m;
    public int gameT = 0;
    public boolean force = false;

    public HashMap<String, Player> lobby = new HashMap<>();
    public HashMap<String, Player> gaming = new HashMap<>();
    public HashMap<String, Player> spec = new HashMap<>();
    public HashMap<String, RoomPlayer> gp = new HashMap<>();

    public RoomSettlement roomSettlement;
    public RoomTool roomTool;
    public TeamAPI teamAPI;

    public ExecutorService service = Executors.newWorkStealingPool();

    public Room(String ln, DodgeBall dodgeBall) {
        data = dodgeBall.getArenaData(ln);
        m = dodgeBall;
        levelName = ln;
        server = dodgeBall.getServer();

        stopGame();
        init();

        DodgeBall.debug("Room: " + levelName + " loaded!");
    }

    public void init() {
        roomTool = new RoomTool(this);
        roomSettlement = new RoomSettlement(this);
        teamAPI = new TeamAPI(this);

        service.execute(() -> new GameTimer(this).start());
        server.getPluginManager().registerEvents(new RoomEvent(this), m);
    }

    public String getLevelName() {
        return levelName;
    }

    public Level getArenaLevel() {
        return server.getLevelByName(getLevelName());
    }

    public int getMin() {
        return force ? 1 : data.getInt("min_players", 2);
    }

    public int getMax() {
        return force ? 1 : data.getInt("max_players", 8);
    }

    public int getStart() {
        return data.getInt("start_time", 10);
    }

    public int getMainTime() {
        return data.getInt("main_time", 1500);
    }

    public int getLC() {
        return lobby.size();
    }

    public int getMaxRound() {
        return force ? 1 : data.getInt("round", 3);
    }

    public String getMapAuthor() {
        return data.getString("map_author", "Nissining");
    }

    public boolean isFull() {
        return lobby.size() >= getMax();
    }

    public boolean isCloseRoom() {
        return gameT != 0 || isFull() || !server.isLevelLoaded(levelName);
    }

    public boolean isInGame(Player p) {
        return getPM(p) != -1;
    }

    public int getPM(Player player) {
        return getPM(player.getName());
    }

    public int getPM(String n) {
        if (lobby.containsKey(n))
            return 0;
        if (gaming.containsKey(n))
            return 1;
        if (spec.containsKey(n))
            return 2;
        return -1;
    }

    public HashMap<String, Player> getAllP() {
        return new HashMap<>(lobby) {{
            putAll(gaming);
            putAll(spec);
        }};
    }

    public void gameMsg(String msg, int type, Sound sound) {
        sendMsgAndGet(new String[]{(type == 0 ? "msg" : "tip"), msg}, sound);
    }

    public void gameTitle(String msg, String text, int time, Sound sound) {
        sendMsgAndGet(new String[]{"title", msg, text, time + ""}, sound);
    }

    public HashMap<String, Player> sendMsgAndGet(String[] data, Sound sound) {
        if (data.length == 0) {
            return new HashMap<>();
        }

        HashMap<String, Player> ps = getAllP();

        switch (data[0]) {
            case "msg":
                if (data.length == 2) {
                    ps.values().forEach(player -> player.sendMessage(data[1]));
                } else {
                    return ps;
                }
                break;
            case "tip":
                if (data.length == 2) {
                    ps.values().forEach(player -> player.sendTip(data[1]));
                } else {
                    return ps;
                }
                break;
            case "title":
                if (data.length == 4) {
                    ps.values().forEach(player -> player.sendTitle(
                            data[1],
                            data[2],
                            5,
                            Integer.parseInt(data[3]) * 20,
                            5
                    ));
                } else {
                    return ps;
                }
                break;
        }

        if (sound != null) {
            ps.values().forEach(player -> player.level.addSound(player, sound));
        }

        return ps;
    }


    private void resetPlayer(Player player, int gm) {
        player.getInventory().clearAll();
        player.setExperience(0, 0);
        player.setHealth(player.getMaxHealth());
        player.getFoodData().reset();
        player.removeAllEffects();
        player.setGamemode(gm);
        player.setScale(1.0f);
        player.setDataFlag(0, 16, false);
        ScoreboardAPI.remove(player);
    }

    public RoomPlayer getGP(Player p) {
        return getGP(p.getName());
    }

    public RoomPlayer getGP(String n) {
        return gp.getOrDefault(n, null);
    }

    public Position getPos(String k) {
        return getBasicPos(data.getString(k));
    }

    public Position getBasicPos(String s) {
        String[] ss = s.split("/");
        return new Position(
                Double.parseDouble(ss[0]),
                Double.parseDouble(ss[1]),
                Double.parseDouble(ss[2]),
                getArenaLevel()
        );
    }

    public boolean joinGame(Player p) {
        if (getPM(p) != -1 || isCloseRoom()) {
            return false;
        }

        resetPlayer(p, 2);
        lobby.put(p.getName(), p);
        gp.put(p.getName(), new RoomPlayer(p));

        roomTool.giveTool(p);

        p.sendMessage(DodgeBall.howtoplay.toString());
        p.teleport(getPos("wait_pos").add(0.5, 1, 0.5));

        sendMsgAndGet(new String[]{"msg", "§a[+]§7" + p.getName()}, null);
        return true;
    }

    public void quitGame(Player p, boolean nextGame) {
        resetPlayer(p, 0);

        lobby.remove(p.getName());
        gaming.remove(p.getName());
        spec.remove(p.getName());

        // 保存玩家数据到players文件夹中
        // TODO: support mysql


        gp.remove(p.getName());

        if (nextGame)
            nextGame = m.quickJoin(p, "");
        if (!nextGame)
            m.disCmd(p);

        sendMsgAndGet(
                new String[]{"msg", "§c[-]§7" + p.getName()},
                null
        );
    }

    public void specGame(Player p) {
        resetPlayer(p, 3);
        gaming.remove(p.getName());
        spec.put(p.getName(), p);
        roomTool.giveTool(p);
        p.teleport(getPos("wait_pos").floor().add(0.5, 1, 0.5));
        p.sendMessage("§7你现在是一名观战者！");
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public boolean startGame(boolean forceStart) {
        setForce(forceStart);
        if (lobby.size() < getMin() && !force) {
            sendMsgAndGet(
                    new String[]{"msg", "§c人数不足，无法开始！"},
                    null
            );
            return false;
        }
        gameT = 1;

        HashMap<String, Player> ps = sendMsgAndGet(
                new String[]{"title", DodgeBall.gameName, "Map By " + getMapAuthor(), "60"},
                null
        );

        ps.values().forEach(player -> {
            resetPlayer(player, 0);
            lobby.remove(player.getName());
            gaming.put(player.getName(), player);
            // 没有选择队伍将随机分配
            teamAPI.joinToTeam(player);
            // 传送至队伍出生点
            teamAPI.tpToTeamPos(player);

            player.setDataFlag(0, 16, true);
        });

        return true;
    }

    // 游戏正式开始
    public void readyStartGame() {
        service.execute(() -> {
        });

        sendMsgAndGet(
                new String[]{"title", "", "GO!", "1"},
                null
        );

    }

    public void overGame(String reason) {
        gameT = 2;
        clearEntities();

        HashMap<String, Player> ps = sendMsgAndGet(
                new String[]{"title", "", reason, "3"},
                null
        );

//        ps.values().forEach(player -> overGameInfo(player, rps));
    }

    private void overGameInfo(Player player, List<RoomPlayer> sortedList) {
        StringJoiner sj = new StringJoiner("\n", "[Game Over - 本轮排名]", "");
        sj.add("");
        sj.add("最快完成:");

        String color;
        for (int i = 0; i < 5; i++) {
            if (i > 2) {
                color = TextFormat.GRAY.toString();
            } else {
                color = Progress.colors[i];
            }

            if (i > sortedList.size() - 1) {
                sj.add(color + i + ".none");
            } else {
                RoomPlayer rp = sortedList.get(i);
                boolean im = rp.getName().equalsIgnoreCase(player.getName());
//                sj.add(color + i + "." + rp.getPlayedTimeInfo(im));
            }
        }

        player.sendMessage(sj.toString());
    }

    public void stopGame() {
        this.getAllP().values().forEach(p -> quitGame(p, true));
        this.gameT = 0;
        this.force = false;
        if (this.roomSettlement != null) {
            this.roomSettlement.clearAll();
        }
        loadGameLevel();
    }

    public void loadGameLevel() {
        if (!server.isLevelLoaded(levelName)) {
            server.loadLevel(levelName);
        }
    }

    public void clearEntities() {
        for (Entity en : getArenaLevel().getEntities()) {
            if (!(en instanceof Player)) {
                en.kill();
                en.close();
            }
        }
    }

    public void roomInfo(Player p) {
        List<String> info = new ArrayList<>();

        ScoreboardAPI.add(p, DodgeBall.gameName);
        ScoreboardAPI.setMessage(p, info);
    }

    @Override
    public String toString() {
        return "Room{" +
                "levelName='" + levelName + '\'' +
                ", gameT=" + gameT +
                ", force=" + force +
                ", gaming=" + gp.size() +
                '}';
    }

}
