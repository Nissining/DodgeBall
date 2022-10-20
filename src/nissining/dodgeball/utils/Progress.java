package nissining.dodgeball.utils;

import cn.nukkit.entity.Entity;
import cn.nukkit.potion.Effect;
import cn.nukkit.utils.TextFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;

// 字符串进度条API
public class Progress {

/*
    public static String addSubtractPar(double now, double max, boolean show) {
        BigDecimal rate = BigDecimal.valueOf(now / (max / 20)).setScale(1, RoundingMode.HALF_EVEN);
        BigDecimal result = BigDecimal.valueOf(20d).setScale(1, RoundingMode.HALF_EVEN).subtract(rate);

        StringBuilder sb = new StringBuilder();

        BigDecimal i = BigDecimal.valueOf(1d).setScale(1, RoundingMode.HALF_EVEN);
        while (i.doubleValue() <= result.doubleValue()) {
            i = i.add(BigDecimal.valueOf(1d).setScale(1, RoundingMode.HALF_EVEN));

            if (i.doubleValue() < rate.doubleValue()) {
                sb.append()
            }
        }
        for (double i = 1d; i <= result; i += 1.0d) {

        }
    }
*/

    /**
     * 进度条
     * <p>
     * 1---------------
     * -1--------------
     * --1-------------
     * ---1------------
     *
     * @param now  当前值
     * @param max  最大值
     * @param show 显示数值
     * @return 进度条
     */
    public static String addPar(double now, double max, boolean show) {
        BigDecimal rate = BigDecimal.valueOf(now / max * 20);

        StringBuilder s = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            if (i < rate.intValue()) {
                s.append("§a|");
            } else if (i == rate.intValue()) {
                s.append("§e§l|");
            } else {
                s.append("§r§7|");
            }
        }

        if (show) {
            s.append(" ");
            s.append(BigDecimal.valueOf(now).setScale(1, RoundingMode.HALF_UP));
        }

        return s.toString();
    }

    public static String[] colors = new String[]{
            TextFormat.GOLD.toString(), TextFormat.YELLOW.toString(), TextFormat.RED.toString()
    };

    public static void giveEffect(Entity entity, int eid, int level, int time) {
        entity.addEffect(Effect.getEffect(eid)
                .setAmplifier(level)
                .setDuration((time == -1 ? 9 * 99999 : time * 20))
                .setVisible(false));
    }

}
