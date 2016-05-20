package guthix.model.entity.player;

import java.util.Arrays;

import guthix.fs.EnumDefinition;
import guthix.model.entity.Player;
import guthix.net.message.game.UpdateSkill;

/**
 * Created by Bart Pelle on 8/23/2014.
 */
public class Skills {

    public static final int SKILL_COUNT = 24;
    private static final int[] XP_TABLE = new int[100];

    private double[] xps = new double[SKILL_COUNT];
    private int[] levels = new int[SKILL_COUNT];
    private Player player;
    private int combat;

    public Skills(Player player) {
        this.player = player;

        Arrays.fill(levels, 1);

		/* Hitpoints differs :) */
        xps[3] = levelToXp(10);
        levels[3] = 10;
        recalculateCombat();
    }

    public void update() {
        for (int skill = 0; skill < SKILL_COUNT; skill++) {
            player.write(new UpdateSkill(skill, levels[skill], (int) xps[skill]));
        }
    }

    public int level(int skill) {
        return levels[skill];
    }

    public int xpLevel(int skill) {
        return xpToLevel((int) xps[skill]);
    }

    public int[] levels() {
        return levels;
    }

    public double[] xp() {
        return xps;
    }

    public void setXp(int skill, double amt) {
        int oldLevel = xpToLevel((int) xps[skill]);
        xps[skill] = Math.min(200000000, amt);
        int newLevel = xpToLevel((int) xps[skill]);
        recalculateCombat();
        player.write(new UpdateSkill(skill, levels[skill], (int) xps[skill]));
    }

    public void setLevel(int skill, int level) {
        player.write(new UpdateSkill(skill, level, xpToLevel(level)));
    }

    public void addXp(int skill, double amt) {
        if (skill == ATTACK || skill == STRENGTH || skill == DEFENCE || skill == RANGED || skill == MAGIC || skill == HITPOINTS || skill == PRAYER) {
            amt *= player.world().combatMultiplier();
        } else {
            amt *= player.world().skillingMultiplier();
        }

        int oldLevel = xpToLevel((int) xps[skill]);
        xps[skill] = Math.min(200000000, xps[skill] + amt);
        int newLevel = xpToLevel((int) xps[skill]);

        if (newLevel > oldLevel) {
            if (levels[skill] < newLevel)
                levels[skill] += newLevel - oldLevel;
        }

        recalculateCombat();
        player.write(new UpdateSkill(skill, levels[skill], (int) xps[skill]));
    }

    public void update(int skill) {
        player.write(new UpdateSkill(skill, levels[skill], (int) xps[skill]));
    }

    public int internalIdOf(int skillId) {
        return player.world().definitions().get(EnumDefinition.class, 1482).getInt(skillId);
    }

    public int toSkillId(int internalId) {
        return player.world().definitions().get(EnumDefinition.class, 681).getInt(internalId);
    }

    public String levelUpMessage(int skill, int level) {
        int internal = internalIdOf(skill);
        String first = player.world().definitions().get(EnumDefinition.class, 1477).getString(internal);
        return first + " You have reached level " + level + ".";
    }

    public void alterSkill(int skill, int change, boolean relative) {
        if (relative) {
            levels[skill] += change;
        } else {
            if (change > 0 && levels[skill] < xpLevel(skill) + change) {
                levels[skill] = xpLevel(skill) + change;
            } else if (change < 0 && levels[skill] > xpLevel(skill) + change) {
                levels[skill] = xpLevel(skill) + change;
            }
        }
        update(skill);
    }

    public void replenishStats() {
        for (int i = 0; i < SKILL_COUNT; i++) {
            if (i == HITPOINTS || i == PRAYER) // Hitpoints does not replenish this way
                continue;

            if (levels[i] < xpLevel(i)) {
                levels[i]++;
                update(i);
            } else if (levels[i] > xpLevel(i)) {
                levels[i]--;
                update(i);
            }
        }
    }

    public void resetStats() {
        for (int i = 0; i < SKILL_COUNT; i++) {
            levels[i] = xpLevel(i);
        }
        update();
    }

    public void restorePrayer() {
        levels[PRAYER] = xpLevel(PRAYER);
        player.write(new UpdateSkill(PRAYER, levels[PRAYER], (int) xps[PRAYER]));
    }

    public void recalculateCombat() {
        int old = combat;
        double defence = xpLevel(Skills.DEFENCE);
        double attack = xpLevel(Skills.ATTACK);
        double strength = xpLevel(Skills.STRENGTH);
        double prayer = xpLevel(Skills.PRAYER);
        double ranged = xpLevel(Skills.RANGED);
        double magic = xpLevel(Skills.MAGIC);
        double hp = xpLevel(Skills.HITPOINTS);

        int baseMelee = (int) Math.floor(0.25 * (defence + hp + Math.floor(prayer / 2d)) + 0.325 * (attack + strength));
        int baseRanged = (int) Math.floor(0.25 * (defence + hp + Math.floor(prayer / 2d)) + 0.325 * (Math.floor(ranged / 2) + ranged));
        int baseMage = (int) Math.floor(0.25 * (defence + hp + Math.floor(prayer / 2d)) + 0.325 * (Math.floor(magic / 2) + magic));
        combat = Math.max(Math.max(baseMelee, baseMage), baseRanged);

        // If our combat changed, we need to update our looks as that contains our cb level too.
        if (combat != old && player.looks() != null) {
            player.looks().update();

            // Make the player's attack panel up to date
            player.updateWeaponInterface();
        }
    }

    public int combatLevel() {
        return combat;
    }

    public static int xpToLevel(int xp) {
        int lv = 1;
        for (; lv < 100; lv++) {
            if (xp < XP_TABLE[lv])
                break;
        }
        return lv > 99 ? 99 : lv;
    }

    public static int levelToXp(int level) {
        return XP_TABLE[level - 1];
    }

    static {
        // Calculate XP table
        for (int lv = 1, points = 0; lv < 100; lv++) {
            points += Math.floor(lv + 300 * Math.pow(2, lv / 7D));
            XP_TABLE[lv] = points / 4;
        }
    }

    public static final int ATTACK = 0;
    public static final int DEFENCE = 1;
    public static final int STRENGTH = 2;
    public static final int HITPOINTS = 3;
    public static final int RANGED = 4;
    public static final int PRAYER = 5;
    public static final int MAGIC = 6;
    public static final int COOKING = 7;
    public static final int WOODCUTTING = 8;
    public static final int FLETCHING = 9;
    public static final int FISHING = 10;
    public static final int FIREMAKING = 11;
    public static final int CRAFTING = 12;
    public static final int SMITHING = 13;
    public static final int MINING = 14;
    public static final int HERBLORE = 15;
    public static final int AGILITY = 16;
    public static final int THIEVING = 17;
    public static final int SLAYER = 18;
    public static final int FARMING = 19;
    public static final int RUNECRAFTING = 20;
    public static final int HUNTER = 21;
    public static final int CONSTRUCTION = 22;
    public static final int SUMMONING = 23;

}
