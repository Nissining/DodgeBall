package nissining.dodgeball.tasks;

import cn.nukkit.level.Sound;
import nissining.dodgeball.rooms.Room;
import nissining.dodgeball.utils.Progress;

public class GameTimer extends Thread {

    public Room room;
    private int startT;
    private int overT;
    private int endT;
    public int readyT;

    public GameTimer(Room room) {
        this.room = room;
    }

    private void reset() {
        this.startT = room.getStart();
        this.overT = room.getMainTime();
        this.endT = 10;
        this.readyT = 10;
    }

    public void run() {
        while (room.gameT <= 2) {
            try {
                switch (room.gameT) {
                    case 0:
                        lobby();
                        break;
                    case 1:
                        game();
                        break;
                    case 2:
                        end();
                        break;
                }
                sleep(BaseInGameTask.sec);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void lobby() {
        String stat = "";
        if (room.lobby.size() >= room.getMin()) {
            if (startT > 0) {
                // 达到最大人数，缩短倒计时
                if (room.isFull() && startT > 5) {
                    startT = 6;
                    room.gameMsg("§b房间已满！倒计时缩短至5秒！", 0, null);
                }
                startT--;
                stat = "游戏正在开始:§a" + startT;
                if (startT < 6) {
                    room.gameTitle("" + startT, "", 2, Sound.RANDOM_ORB);
                }
                if (startT <= 0 && room.startGame(false)) {
                    reset();
                }
            }
        } else {
            stat = "等待玩家中...";
            reset();
        }
        room.gameMsg(stat + " §b(" + room.lobby.size() + "/" + room.getMax() + ")", 1, null);
    }

    private boolean ready() {
        String color = "";
        Sound sound = null;
        if (readyT > 0) {
            readyT--;

            // show howtoplay
            if (room.force && readyT > 3) {
                readyT = 3;
            }

            if (readyT < 3) {
                color = Progress.colors[readyT];
                sound = Sound.RANDOM_ORB;
            }
            room.gameTitle(color + readyT + "", "", 1, sound);

            if (readyT < 1) {
                room.readyStartGame();
            }
            return true;
        }
        return false;
    }

    private void game() {
        if (ready())
            return;

        if (overT > 0) {
            overT--;

            // debug
//            if (room.force && overT > 10)
//                overT = 3;

            if (overT <= 10) {
                room.gameTitle(overT + "", "", 1, null);
            }

            if (overT < 1) {
                room.overGame("§c时间耗尽!");
            }
        }

        if (room.gaming.size() < 1 && !room.force) {
            room.overGame("人数不足！自动结束游戏");
            return;
        }

        room.gaming.values().forEach(player -> room.roomInfo(player));
    }

    private void end() {
        if (endT > 0) {
            endT--;
            room.gameMsg("即将开始下一局:§a " + endT, 1, null);
            if (endT < 1)
                room.stopGame();
        }
    }

}
