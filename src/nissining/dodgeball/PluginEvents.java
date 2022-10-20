package nissining.dodgeball;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.event.player.*;
import nissining.dodgeball.rooms.Room;

public class PluginEvents implements Listener {

    private final DodgeBall m;

    public PluginEvents(DodgeBall m) {
        this.m = m;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        SetupAPI.remove(e.getPlayer());
        if (m.config.getBoolean("only_server_mode") && !m.quickJoin(e.getPlayer(), "")) {
            m.disCmd(e.getPlayer());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        Room room = m.getArena(p);
        if (room != null)
            room.quitGame(p, false);
    }

    @EventHandler
    public void onQuit1(EntityLevelChangeEvent e) {
        Entity en = e.getEntity();
        if (en instanceof Player) {
            Room room = m.getArena(en);
            if (room != null && !e.getTarget().equals(room.getArenaLevel()))
                room.quitGame((Player) en, false);
        }
    }

    @EventHandler
    public void onTouch(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        SetupAPI setupAPI = SetupAPI.get(p);
        if (setupAPI != null && event.getAction().equals(PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled();
            setupAPI.touch(event.getBlock(), event.getItem());
        }
    }

}
