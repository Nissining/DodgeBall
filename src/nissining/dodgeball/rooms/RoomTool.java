package nissining.dodgeball.rooms;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.element.ElementButtonImageData;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.ItemCompass;
import nissining.dodgeball.utils.FormAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomTool {

    public static List<ElementButton> buttons = new ArrayList<>() {{
        add(new RoomToolButton("退出房间", "items/door_wood"));
        add(new RoomToolButton("传送玩家", "items/ender_eye"));
        add(new RoomToolButton("再玩一次", "items/wood_sword"));
        add(new RoomToolButton("强制开始", "items/wood_sword"));
        add(new RoomToolButton("随机传送", "items/ender_eye"));
    }};

    private final Room a;

    public RoomTool(Room a) {
        this.a = a;
    }

    public void giveTool(Player p) {
        if (p != null) {
            p.getInventory().clearAll();
            p.getInventory().setItem(8, new ItemCompass().setCustomName("§r§a菜单 - 点击/长按"));
        }
    }

    public void openMenu(Player p) {
        if (a.getPM(p) == -1)
            return;

        FormWindowSimple f = new FormWindowSimple("Game Menu", "");

        for (int i = 0; i < buttons.size(); i++) {
            if (i > 0 && a.getPM(p) != 2) {
                continue;
            }
            if (i == 3 && !p.isOp())
                continue;
            f.addButton(buttons.get(i));
        }

        FormAPI fapi = new FormAPI(p, f) {
            @Override
            public void call() {
                if (!wasClosed()) {
                    switch (getButtonText()) {
                        case "退出房间":
                        case "再玩一次":
                            a.quitGame(p, getButtonText().equals("再玩一次"));
                            break;
                        case "传送玩家":
                            tpMenu(p);
                            break;
                        case "强制开始":
                            a.startGame(true);
                            break;
                    }
                }
            }
        };
        fapi.sendToPlayer(p);
    }

    private void tpMenu(Player p) {
        FormWindowSimple f = new FormWindowSimple("Teleport To Player", "");

        f.addButton(buttons.get(buttons.size() - 1));
        a.gaming.values().forEach(p1 -> f.addButton(new ElementButton(p1.getName())));

        FormAPI f1 = new FormAPI(p, f) {
            @Override
            public void call() {
                if (!wasClosed()) {

                    Player target;
                    List<Player> tempPs = new ArrayList<>(a.gaming.values());

                    if ("随机传送".equals(getButtonText())) {
                        Collections.shuffle(tempPs);
                        target = tempPs.stream().findFirst().orElse(null);
                    } else {
                        target = tempPs.stream().filter(p1 -> p1.getName().equals(getButtonText())).findFirst().orElse(null);
                    }

                    if (target == null) {
                        p.sendMessage("目标丢失！");
                        return;
                    }

                    p.teleport(target.floor().add(0.5, 1, 0.5));
                }
            }
        };
        f1.sendToPlayer(p);
    }

    public static class RoomToolButton extends ElementButton {

        public RoomToolButton(String s, String image) {
            super(s);
            this.addImage(new ElementButtonImageData("path", "textures/" + image));
        }
    }


}
