package nissining.dodgeball.utils;

import cn.nukkit.Player;
import cn.nukkit.level.Position;
import cn.nukkit.utils.DyeColor;
import cn.nukkit.utils.Utils;
import nissining.dodgeball.rooms.Room;
import nissining.dodgeball.rooms.RoomPlayer;

import java.util.*;
import java.util.stream.Collectors;

public class TeamAPI {

    private final Room room;

    /*
    各队伍的颜色
     */
    public static List<Team> Teams() {
        return new ArrayList<>() {{
            add(new Team("红队", "§c", 14, DyeColor.RED, 0));
            add(new Team("黄队", "§e", 4, DyeColor.YELLOW, 1));
            add(new Team("蓝队", "§b", 11, DyeColor.BLUE, 2));
            add(new Team("绿队", "§a", 5, DyeColor.GREEN, 3));

            add(new Team("紫队", "§d", 10, DyeColor.PURPLE, 4));
            add(new Team("橙队", "§6", 1, DyeColor.ORANGE, 5));
            add(new Team("白队", "§f", 0, DyeColor.WHITE, 6));
            add(new Team("灰队", "§7", 7, DyeColor.GRAY, 7));
        }};
    }

    public TeamAPI(Room room) {
        this.room = room;
    }

    public int getMaxTeam() {
        return this.room.data.getStringList("team_pos").size();
    }

    public int getTeamMaxCount() {
        if (this.room.getMax() < getMaxTeam()) {
            throw new RuntimeException("队伍数大于房间最大人数！分配不正常！");
        }

        int i = this.room.getMin() / getMaxTeam();
        return Math.max(i, 1);
    }

    // 获取玩家所在队伍
    public String getTeamByPlayer(Player player) {
        return room.getGP(player).team;
    }

    // 是相同队伍的
    public boolean isSameTeam(Player p1, Player p2) {
        return isSameTeam(getTeamByPlayer(p1), getTeamByPlayer(p2));
    }

    public boolean isSameTeam(String p1, String p2) {
        return p1.equals(p2);
    }

    public String getTeamNameAndColor(String tn) {
        String tc = "";
        for (Team team : Teams()) {
            if (team.teamName.equalsIgnoreCase(tn)) {
                tc = team.teamColor;
                break;
            }
        }
        return tc + tn;
    }

    // 获取配置文件中已设置的队伍名称
    public List<String> ArenaTeams() {
        int max = room.data.getStringList("team_pos").size();

        return Teams().stream()
                .limit(max)
                .map(team -> team.teamName)
                .collect(Collectors.toList());
    }

    // 获取队伍人数
    public int getTeamCount(String team) {
        return (int) room.gp.values().stream()
                .filter(rp -> isSameTeam(rp.team, team))
                .count();
    }

    // 获取队伍存活人数
    public int getTeamCountByAlive(String team) {
        return (int) room.gp.values().stream()
                .filter(rp -> isSameTeam(rp.team, team))
                .filter(rp -> !rp.getPlayer().isSpectator())
                .count();
    }

    // 队伍分配
    public String getRandomFreeTeam() {
        String team = ArenaTeams().get(Utils.rand(0, ArenaTeams().size()));
        int max = getTeamCount(team);
        if (max >= getTeamMaxCount()) {
            return getRandomFreeTeam();
        }
        return team;
    }

    // 获取队伍玩家列表
    public List<RoomPlayer> getTeamPlayers(String tn) {
        return room.gp.values().stream()
                .filter(rp -> isSameTeam(rp.team, tn))
                .collect(Collectors.toList());
    }

    public void setToTeam(Player player, String tn) {
        room.getGP(player).team = tn;
        player.sendMessage("你已加入队伍: " + getTeamNameAndColor(tn));
    }

    //加入队伍
    public void joinToTeam(Player player) {
        // 可以自由选择队伍
        RoomPlayer rp = room.getGP(player);
        if (rp == null) {
            return;
        }

        if (!rp.getTeam().equalsIgnoreCase("none")) {
            return;
        }

        String tn = getRandomFreeTeam();
        setToTeam(player, tn);
    }

    //退出队伍
    public void quitToTeam(Player player) {
        if (room.getPM(player) == 1) {
            room.getGP(player).team = "none";
        }
    }

    public Team getTeam(String tn) {
        return Teams().stream()
                .filter(team -> team.teamName.equalsIgnoreCase(tn))
                .findFirst().orElse(null);
    }

    public void tpToTeamPos(Player player) {
        Team team = getTeam(getTeamByPlayer(player));
        if (team == null) {
            player.sendMessage("传送失败！原因： 不存在的队伍数据");
            return;
        }

        int tid = team.teamPosId;
        String strPos = room.data.getStringList("team_pos").get(tid);
        Position position = room.getBasicPos(strPos);
        player.teleport(position.floor().add(0.5, 1, 0.5));
    }

    // 获取存活的队伍
    public List<String> getAliveTeams() {
        return ArenaTeams().stream()
                .filter(s -> getTeamCount(s) > 0)
                .collect(Collectors.toList());
    }

    public static class Team {

        public String teamName;
        public String teamColor;
        public DyeColor teamDc;
        public int teamDamage;
        public int teamPosId;

        public Team(String teamName, String teamColor, int teamDamage, DyeColor teamDc, int teamPosId) {
            this.teamName = teamName;
            this.teamColor = teamColor;
            this.teamDamage = teamDamage;
            this.teamDc = teamDc;
            this.teamPosId = teamPosId;
        }


    }

}
