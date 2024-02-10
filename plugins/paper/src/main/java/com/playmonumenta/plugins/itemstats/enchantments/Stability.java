package com.playmonumenta.plugins.itemstats.enchantments;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.itemstats.Enchantment;
import com.playmonumenta.plugins.utils.BossUtils;
import com.playmonumenta.plugins.utils.ItemStatUtils.EnchantmentType;
import org.bukkit.entity.Player;

public class Stability implements Enchantment {

    private static final double SHIELD_DISABLE_REDUCTION_PER_LEVEL = 0.2;

    @Override
    public String getName() {
        return "Stability";
    }

    @Override
    public EnchantmentType getEnchantmentType() {
        return EnchantmentType.STABILITY;
    }
    
    public static double disableReduction(Plugin plugin, Player player) {
        return plugin.mItemStatManager.getEnchantmentLevel(player, EnchantmentType.STABILITY) * SHIELD_DISABLE_REDUCTION_PER_LEVEL;
    }
    
}