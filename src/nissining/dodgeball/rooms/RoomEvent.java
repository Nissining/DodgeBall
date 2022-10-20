package nissining.dodgeball.rooms;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockRedstone;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.level.ChunkUnloadEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemCompass;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.network.protocol.types.AuthInputAction;
import cn.nukkit.potion.Effect;

import nissining.dodgeball.entities.Ball;
import nissining.dodgeball.utils.Progress;

import java.util.Set;
import java.util.StringJoiner;

public class RoomEvent implements Listener {

    private final Room arena;

    public RoomEvent(Room room) {
        arena = room;
    }

    @EventHandler
    private void onInt(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (arena.isInGame(p)) {
            event.setCancelled();

            if (arena.getPM(p) == 1) {
            }

        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (arena.isInGame(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler
    public void onAnimal(PlayerAnimationEvent event) {
        Player p = event.getPlayer();
        if (arena.isInGame(p)) {
            Item i = p.getInventory().getItemInHand();
            if (i instanceof ItemCompass && event.getAnimationType().equals(AnimatePacket.Action.SWING_ARM)) {
                arena.roomTool.openMenu(p);
            }
        }
    }

    //    @EventHandler
    public void onChat(PlayerChatEvent event) {
        Player p = event.getPlayer();
        if (arena.isInGame(p)) {
            event.setCancelled();
            String[] ss = new String[]{"等待中", "游戏中", "旁观"};
//            arena.gameMsg(
//                    "[" + ss[arena.getPM(p)] + "]" + p.getName() + ": " + event.getMessage(),
//                    0,
//                    null
//            );
        }
    }

    @EventHandler
    private void onBreak(BlockBreakEvent event) {
        if (arena.isInGame(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler
    private void onPlace(BlockPlaceEvent event) {
        if (arena.isInGame(event.getPlayer())) {
            event.setCancelled();
        }
    }

    @EventHandler
    private void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            Player p = (Player) entity;
            if (arena.getPM(p) != 0) {
                event.setCancelled();

                if (arena.getPM(p) == 0 || arena.getPM(p) == 1) {
                    if (event.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                        p.teleport(arena.getPos("wait_pos").add(0.5, 1, 0.5));
                    }
                }

            }

        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities().values()) {
            if (entity instanceof Ball) {
                event.setCancelled();
                break;
            }
        }
    }

}
