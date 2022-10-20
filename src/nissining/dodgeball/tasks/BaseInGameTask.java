package nissining.dodgeball.tasks;

import nissining.dodgeball.rooms.Room;

public class BaseInGameTask extends Thread {

    public final Room room;

    public static long sec = 1000L;
    public static long mill = 100L;
    public static long nano = 10L;

    public BaseInGameTask(Room room) {
        this.room = room;
    }

    @Override
    public void run() {
    }

}
