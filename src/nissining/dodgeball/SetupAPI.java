package nissining.dodgeball;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.util.HashMap;
import java.util.List;

public class SetupAPI {

    private final static HashMap<String, SetupAPI> setups = new HashMap<>();
    private final static String[] modes = new String[]{
            "等待点",
            "球生成点",
            "队伍出生点",
            "退出并保存设置"
    };

    public static void add(Player p, Config c) {
        setups.put(p.getName(), new SetupAPI(p, c));
    }

    public static boolean remove(Player p) {
        if (get(p) != null) {
            setups.remove(p.getName());
            p.getInventory().clearAll();
            return true;
        }
        return false;
    }

    public static SetupAPI get(Player p) {
        return setups.getOrDefault(p.getName(), null);
    }

    private final Player player;
    private final Config c;
    private final ConfigSection cs;

    public SetupAPI(Player player, Config c) {
        this.player = player;
        this.c = c;
        this.cs = c.getRootSection();
        giveTools();
        player.sendMessage("你正在设置房间！手持对应物品点击方块完成设置！");
    }

    private void giveTools() {
        player.setGamemode(1);
        player.getInventory().clearAll();
        int i = 0;
        for (String s : modes) {
            i++;
            Item item = Item.get(290);
            CompoundTag tag;
            if (item.hasCompoundTag()) {
                tag = item.getNamedTag();
            } else {
                tag = new CompoundTag();
            }
            tag.putInt("setupId", i);
            item.setNamedTag(tag);
            item.setCustomName(s);

            player.getInventory().addItem(item);
        }
    }

    private String getPosToString(Position pos) {
        return pos.x + "/" + pos.y + "/" + pos.z;
    }

    public void touch(Position pos, Item item) {
        if (item.getNamedTagEntry("setupId") == null) {
            return;
        }

        int id = item.getNamedTag().getInt("setupId");
        String sp = getPosToString(pos);
        String t = "";

        switch (id) {
            case 1:
                cs.set("wait_pos", sp);
                t = "已设置等待点";
                break;
            case 2:
                cs.set("ball_pos", sp);
                t = "已设置球出生点";
                break;
            case 3:
                List<String> posList = cs.getStringList("team_pos");
                posList.add(sp);
                cs.set("team_pos", posList);
                t = "已设置" + posList.size() + "个队伍出生点";
                break;
            case 4:
                saveAll();
                if (remove(player))
                    player.sendMessage("已退出并保存设置！");
                else
                    player.sendMessage("退出失败！并没有进入设置状态！");
                break;
        }

        if (!t.isEmpty())
            player.sendMessage(t + " " + sp);
    }

    private void saveAll() {
        c.setAll(cs);
        c.save();
    }

}
