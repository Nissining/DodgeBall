package nissining.dodgeball.rooms;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// 游戏结算API
public class RoomSettlement {

    private final Room r;
    private final List<Player> winners = new ArrayList<>();
    private final List<Player> notWinners = new ArrayList<>();

    // 已排序好的排名
    public List<RoomPlayer> isSortRanks = new ArrayList<>();

    public RoomSettlement(Room r) {
        this.r = r;
    }

    public int addWinnerAndGet(Player winner) {
        if (!winners.contains(winner))
            winners.add(winner);
        return getWinnerCount();
    }

    public int getWinnerCount() {
        return winners.size();
    }

    public Player getWinner() {
        return winners.stream().findFirst().orElse(null);
    }

    public void addNotWinner(Player winner) {
        if (notWinners.contains(winner))
            return;
        notWinners.add(winner);
    }


    public void clearAll() {
        this.winners.clear();
        this.notWinners.clear();
    }

    // 先进行排序 (优化)
    public void reloadSettlementInfo() {
        isSortRanks = r.gp.values()
                .stream()
                .sorted((Comparator.comparingInt(RoomPlayer::getFinishTime)))
                .limit(5)
                .collect(Collectors.toList());
    }

    public static String[] colors() {
        return new String[]{"§6", "§e", "§b"};
    }

    // 游戏结算排行
    public void sendSettlementInfo(Player p) {
        StringBuilder sb = new StringBuilder("{n}[Game Over - 本轮最速排名]{n}");
        String color;

        for (int i = 0; i < 5; i++) {
            if (i <= 2) {
                color = colors()[i];
            } else {
                color = TextFormat.GRAY.toString();
            }
            sb.append(color).append(i + 1).append(".");
            if (i >= isSortRanks.size()) {
                sb.append("none{n}");
            } else {
                RoomPlayer rp = isSortRanks.get(i);
                sb.append(rp.getPlayedTimeInfo(rp.getName().equals(p.getName()))).append("{n}");
            }
        }

        p.sendMessage(sb.toString().replace("{n}", "\n"));
    }

    public void winSettlement() {
        Player winner = getWinner();
        if (winner != null) {


            //对接奖励
//            PluginMain.instance.rewardProgress1(winner);

            //执行胜利指令
//            dispatchCommand(winner, "wim_cmd");
        }

        for (int i = 1; i < winners.size(); i++) {
            Player target = winners.get(i);
            if (target == null)
                continue;

            //对接奖励 获胜奖
//            PluginMain.instance.rewardProgress2(target);

            //执行失败指令
//            dispatchCommand(target, "fail_cmd");
        }
    }

//    private void dispatchCommand(Player target, String k) {
//        PlayerDataAPI pda = PlayerDataAPI.getPlayerData(target);
//        if (pda == null)
//            return;
//
//        for (String cmd : r.data.getStringList(k)) {
//            String replace = cmd
//                    .replace("%win", pda.getDataInt("win") + "")
//                    //防止带有空格名称
//                    .replace("%p", "\"" + target.getName() + "\"");
//            r.server.dispatchCommand(
//                    r.server.getConsoleSender(),
//                    replace
//            );
//        }
//
//    }


}
