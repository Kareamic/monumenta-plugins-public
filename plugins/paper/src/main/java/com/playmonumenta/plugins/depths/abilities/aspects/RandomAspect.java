package com.playmonumenta.plugins.depths.abilities.aspects;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.depths.abilities.WeaponAspectDepthsAbility;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class RandomAspect extends WeaponAspectDepthsAbility {

	public static final String ABILITY_NAME = "Mystery Box";

	public RandomAspect(Plugin plugin, Player player) {
		super(plugin, player, ABILITY_NAME);
		mDisplayMaterial = Material.BARREL;
	}

	@Override
	public String getDescription(int rarity) {
		return "Obtain a random ability. Transforms into a random other aspect after defeating floor 3.";
	}
}

