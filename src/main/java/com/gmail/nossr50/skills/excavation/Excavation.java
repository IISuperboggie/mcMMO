package com.gmail.nossr50.skills.excavation;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.config.Config;
import com.gmail.nossr50.config.TreasuresConfig;
import com.gmail.nossr50.datatypes.PlayerProfile;
import com.gmail.nossr50.datatypes.treasure.ExcavationTreasure;
import com.gmail.nossr50.events.fake.FakePlayerAnimationEvent;
import com.gmail.nossr50.mods.ModChecks;
import com.gmail.nossr50.skills.SkillType;
import com.gmail.nossr50.skills.SkillTools;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.Users;

public class Excavation {
    public static boolean requiresTool = Config.getInstance().getExcavationRequiresTool();

    /**
     * Check to see if treasures were found.
     *
     * @param block The block to check
     * @param player The player who broke the block
     */
    public static void excavationProcCheck(Block block, Player player) {
        Material type = block.getType();
        Location location = block.getLocation();

        PlayerProfile profile = Users.getProfile(player);
        int skillLevel = profile.getSkillLevel(SkillType.EXCAVATION);
        ArrayList<ItemStack> is = new ArrayList<ItemStack>();

        List<ExcavationTreasure> treasures = new ArrayList<ExcavationTreasure>();

        int xp;

        switch (type) {
        case CLAY:
            xp = Config.getInstance().getExcavationClayXP();
            break;

        case DIRT:
            xp = Config.getInstance().getExcavationDirtXP();
            break;

        case GRASS:
            xp = Config.getInstance().getExcavationGrassXP();
            break;

        case GRAVEL:
            xp = Config.getInstance().getExcavationGravelXP();
            break;

        case MYCEL:
            xp = Config.getInstance().getExcavationMycelXP();
            break;

        case SAND:
            xp = Config.getInstance().getExcavationSandXP();
            break;

        case SOUL_SAND:
            xp = Config.getInstance().getExcavationSoulSandXP();
            break;

        default:
            xp = ModChecks.getCustomBlock(block).getXpGain();;
            break;
        }

        if (Permissions.excavationTreasures(player)) {
            switch (type) {
            case DIRT:
                treasures = TreasuresConfig.getInstance().excavationFromDirt;
                break;

            case GRASS:
                treasures = TreasuresConfig.getInstance().excavationFromGrass;
                break;

            case SAND:
                treasures = TreasuresConfig.getInstance().excavationFromSand;
                break;

            case GRAVEL:
                treasures = TreasuresConfig.getInstance().excavationFromGravel;
                break;

            case CLAY:
                treasures = TreasuresConfig.getInstance().excavationFromClay;
                break;

            case MYCEL:
                treasures = TreasuresConfig.getInstance().excavationFromMycel;
                break;

            case SOUL_SAND:
                treasures = TreasuresConfig.getInstance().excavationFromSoulSand;
                break;

            default:
                break;
            }

            for (ExcavationTreasure treasure : treasures) {
                if (skillLevel >= treasure.getDropLevel()) {
                    int activationChance = Misc.calculateActivationChance(Permissions.luckyExcavation(player));

                    if (Misc.getRandom().nextDouble() * activationChance <= treasure.getDropChance()) {
                        xp += treasure.getXp();
                        is.add(treasure.getDrop());
                    }
                }
            }

            //Drop items
            for (ItemStack x : is) {
                if (x != null) {
                    Misc.dropItem(location, x);
                }
            }
        }

        SkillTools.xpProcessing(player, profile, SkillType.EXCAVATION, xp);
    }

    /**
     * Handle triple drops from Giga Drill Breaker.
     *
     * @param player The player using the ability
     * @param block The block to check
     */
    public static void gigaDrillBreaker(Player player, Block block) {
        SkillTools.abilityDurabilityLoss(player.getItemInHand(), Misc.toolDurabilityLoss);

        if (!mcMMO.placeStore.isTrue(block) && Misc.blockBreakSimulate(block, player, true)) {
            FakePlayerAnimationEvent armswing = new FakePlayerAnimationEvent(player);
            mcMMO.p.getServer().getPluginManager().callEvent(armswing);

            Excavation.excavationProcCheck(block, player);
            Excavation.excavationProcCheck(block, player);
        }

        player.playSound(block.getLocation(), Sound.ITEM_PICKUP, Misc.POP_VOLUME, Misc.POP_PITCH);
    }
}