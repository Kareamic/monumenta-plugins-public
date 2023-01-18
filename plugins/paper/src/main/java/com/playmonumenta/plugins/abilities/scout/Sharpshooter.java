package com.playmonumenta.plugins.abilities.scout;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityInfo;
import com.playmonumenta.plugins.abilities.AbilityManager;
import com.playmonumenta.plugins.abilities.AbilityWithChargesOrStacks;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.itemstats.abilities.CharmManager;
import com.playmonumenta.plugins.network.ClientModHandler;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.MessagingUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;

public class Sharpshooter extends Ability implements AbilityWithChargesOrStacks {
	private static final double PERCENT_BASE_DAMAGE = 0.25;
	private static final int SHARPSHOOTER_DECAY_TIMER = 20 * 5;
	private static final int MAX_STACKS = 8;
	private static final double PERCENT_DAMAGE_PER_STACK = 0.04;
	private static final double DAMAGE_PER_BLOCK = 0.015;
	private static final double MAX_DISTANCE = 16;
	private static final double ARROW_SAVE_CHANCE = 0.2;

	public static final String CHARM_STACK_DAMAGE = "Sharpshooter Stack Damage";
	public static final String CHARM_STACKS = "Sharpshooter Max Stacks";
	public static final String CHARM_RETRIEVAL = "Sharpshooter Arrow Save Chance";
	public static final String CHARM_DECAY = "Sharpshooter Stack Decay Time";

	public static final AbilityInfo<Sharpshooter> INFO =
		new AbilityInfo<>(Sharpshooter.class, "Sharpshooter", Sharpshooter::new)
			.scoreboardId("Sharpshooter")
			.shorthandName("Ss")
			.descriptions(
				String.format("Your projectiles deal %d%% more damage.", (int) (PERCENT_BASE_DAMAGE * 100)),
				String.format("Each enemy hit with a critical projectile gives you a stack of Sharpshooter, up to %d. Stacks decay after %d seconds of not gaining a stack. Each stack makes your projectiles deal an additional +%d%% damage. Additionally, passively gain a %d%% chance to not consume arrows when shot.",
					MAX_STACKS, SHARPSHOOTER_DECAY_TIMER / 20, (int) (PERCENT_DAMAGE_PER_STACK * 100), (int) (ARROW_SAVE_CHANCE * 100)),
				String.format("Your projectiles deal an extra %s%% damage per block of distance between you and the target, up to %s blocks.", DAMAGE_PER_BLOCK * 100, (int) MAX_DISTANCE))
			.displayItem(new ItemStack(Material.TARGET, 1));

	private final int mMaxStacks;
	private final int mDecayTime;

	public Sharpshooter(Plugin plugin, Player player) {
		super(plugin, player, INFO);
		mMaxStacks = MAX_STACKS + (int) CharmManager.getLevel(mPlayer, CHARM_STACKS);
		mDecayTime = CharmManager.getDuration(mPlayer, CHARM_DECAY, SHARPSHOOTER_DECAY_TIMER);
	}

	private int mStacks = 0;
	private int mTicksToStackDecay = 0;

	@Override
	public boolean onDamage(DamageEvent event, LivingEntity enemy) {
		DamageType type = event.getType();
		boolean huntingCompanion = event.getAbility() == ClassAbility.HUNTING_COMPANION;
		if (huntingCompanion || type == DamageType.PROJECTILE || type == DamageType.PROJECTILE_SKILL) {
			double multiplier = 1 + PERCENT_BASE_DAMAGE;
			if (!huntingCompanion) {
				if (isLevelTwo()) {
					multiplier += mStacks * (PERCENT_DAMAGE_PER_STACK + CharmManager.getLevelPercentDecimal(mPlayer, CHARM_STACK_DAMAGE));
				}
				if (isEnhanced()) {
					multiplier += Math.min(enemy.getLocation().distance(mPlayer.getLocation()), MAX_DISTANCE) * DAMAGE_PER_BLOCK;
				}
			}

			event.setDamage(event.getDamage() * multiplier);

			if (!huntingCompanion && isLevelTwo() && (enemy.getNoDamageTicks() <= enemy.getMaximumNoDamageTicks() / 2f || enemy.getLastDamage() < event.getDamage()) && (type != DamageType.PROJECTILE || (event.getDamager() instanceof Projectile projectile && EntityUtils.isAbilityTriggeringProjectile(projectile, true)))) {
				mTicksToStackDecay = mDecayTime;

				if (mStacks < mMaxStacks) {
					mStacks++;
					MessagingUtils.sendActionBarMessage(mPlayer, "Sharpshooter Stacks: " + mStacks);
					ClientModHandler.updateAbility(mPlayer, this);
				}
			}
		}

		return false; // only changes event damage
	}

	@Override
	public void periodicTrigger(boolean twoHertz, boolean oneSecond, int ticks) {
		if (mStacks > 0) {
			mTicksToStackDecay -= 5;

			if (mTicksToStackDecay <= 0) {
				mTicksToStackDecay = mDecayTime;
				mStacks--;
				MessagingUtils.sendActionBarMessage(mPlayer, "Sharpshooter Stacks: " + mStacks);
				ClientModHandler.updateAbility(mPlayer, this);
			}
		}
	}

	@Override
	public boolean playerConsumeArrowEvent() {
		if (isLevelTwo() && FastUtils.RANDOM.nextDouble() < ARROW_SAVE_CHANCE + CharmManager.getLevelPercentDecimal(mPlayer, CHARM_RETRIEVAL)) {
			mPlayer.playSound(mPlayer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.3f, 1.0f);
			return false;
		}
		return true;
	}

	public static void addStacks(Player player, int stacks) {
		Sharpshooter ss = AbilityManager.getManager().getPlayerAbility(player, Sharpshooter.class);
		if (ss != null) {
			ss.mStacks = Math.min(MAX_STACKS + (int) CharmManager.getLevel(player, CHARM_STACKS), ss.mStacks + stacks);
			MessagingUtils.sendActionBarMessage(player, "Sharpshooter Stacks: " + ss.mStacks);
			ClientModHandler.updateAbility(player, ss);
		}
	}

	public static double getDamageMultiplier(Player player) {
		Sharpshooter ss = AbilityManager.getManager().getPlayerAbility(player, Sharpshooter.class);
		return ss == null ? 1 : (1 + PERCENT_BASE_DAMAGE + ss.mStacks * (PERCENT_DAMAGE_PER_STACK + CharmManager.getLevelPercentDecimal(player, CHARM_STACK_DAMAGE)));
	}

	@Override
	public int getCharges() {
		return mStacks;
	}

	@Override
	public int getMaxCharges() {
		return mMaxStacks;
	}

}
