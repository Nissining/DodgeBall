package nissining.dodgeball.rooms;

import cn.nukkit.Player;
import cn.nukkit.utils.ConfigSection;

public class RoomPlayer {

    public Player player;
    public RoomPlayerData rpd = new RoomPlayerData();

    public String team = "none";

    public RoomPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return player.getName();
    }

    public String getTeam() {
        return team;
    }

    public static class RoomPlayerData {
        // 游戏内的数据
        public ConfigSection gameData = new ConfigSection() {{
            put("win", 0);
            put("fail", 0);
            put("useProp", 0);
            put("hitProp", 0);
            put("finish", 0);
            put("puickUp", 0);
        }};

        public void setData(String s, int i) {
            gameData.set(s, i);
        }

        public int getData(String k) {
            return gameData.getInt(k);
        }

        public void addData(String k, int i) {
            setData(k, getData(k) + i);
        }

        public void reduceData(String k, int i) {
            setData(k, Math.max(0, getData(k) - i));
        }
    }

}
