package nissining.dodgeball.scores;

import cn.nukkit.Player;
import nissining.dodgeball.scores.packet.RemoveObjectivePacket;
import nissining.dodgeball.scores.packet.ScorePacketEntry;
import nissining.dodgeball.scores.packet.SetDisplayObjectivePacket;
import nissining.dodgeball.scores.packet.SetScorePacket;

import java.util.HashMap;
import java.util.List;

public class ScoreboardAPI {

    private static final HashMap<String, String> scoreboards = new HashMap<>();

    public static void add(Player player, String displayName) {
        if (hasPlayer(player)) {
            remove(player);
        }
        String objectiveName = player.getName();
        SetDisplayObjectivePacket pk = new SetDisplayObjectivePacket();
        pk.displaySlot = "sidebar";
        pk.objectiveName = objectiveName;
        pk.displayName = displayName;
        pk.criteriaName = "dummy";
        pk.sortOrder = 0; // 1=大到小 0=小到大
        player.dataPacket(pk);
        scoreboards.put(objectiveName, objectiveName);
    }

    public static void remove(Player player) {
        if (hasPlayer(player)) {
            RemoveObjectivePacket pk = new RemoveObjectivePacket();
            pk.objectiveName = player.getName();
            player.dataPacket(pk);
            scoreboards.remove(player.getName());
        }
    }

    public static void setMessage(Player player, List<String> strings) {
        SetScorePacket pk = new SetScorePacket();
        pk.type = SetScorePacket.TYPE_CHANGE;
        for (int line = 0; line < strings.size(); line++) {
            ScorePacketEntry entry = new ScorePacketEntry();
            entry.objectiveName = player.getName();
            entry.addType = ScorePacketEntry.TYPE_FAKE_PLAYER;
            entry.fakePlayer = strings.get(line);
            entry.score = line;
            entry.scoreboardId = line;
            pk.entries.add(entry);
        }
        player.dataPacket(pk);
    }

    public static boolean hasPlayer(Player player) {
        return scoreboards.containsKey(player.getName());
    }
}
