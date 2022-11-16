package net.geforcemods.securitycraft.entity.ai;

import java.util.Collections;
import java.util.List;

import net.geforcemods.securitycraft.api.SecurityCraftAPI;
import net.geforcemods.securitycraft.entity.EntitySentry;
import net.geforcemods.securitycraft.entity.EntitySentry.EnumSentryMode;
import net.geforcemods.securitycraft.util.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityFlying;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Attacks any player who is not the owner, or any mob
 */
public class EntityAITargetNearestPlayerOrMob extends EntityAINearestAttackableTarget<EntityLivingBase> {
	private EntitySentry sentry;

	public EntityAITargetNearestPlayerOrMob(EntitySentry sentry) {
		super(sentry, EntityLivingBase.class, true);

		this.sentry = sentry;
	}

	@Override
	public boolean shouldExecute() {
		List<EntityLivingBase> list = taskOwner.world.<EntityLivingBase>getEntitiesWithinAABB(targetClass, getTargetableArea(getTargetDistance()), e -> sentry.getEntitySenses().canSee(e) && !EntityUtils.isInvisible(e));

		if (list.isEmpty() || sentry.isShutDown())
			return false;
		else {
			EnumSentryMode mode = sentry.getMode();
			int i;

			Collections.sort(list, sorter);

			//get the nearest target that is either a mob or a player
			for (i = 0; i < list.size(); i++) {
				EntityLivingBase potentialTarget = list.get(i);

				if (potentialTarget.getIsInvulnerable())
					continue;

				if (mode.attacksPlayers()) {
					//@formatter:off
					if(potentialTarget instanceof EntityPlayer
							&& !((EntityPlayer)potentialTarget).isSpectator()
							&& !((EntityPlayer)potentialTarget).isCreative()
							&& !((EntitySentry)taskOwner).getOwner().isOwner(((EntityPlayer)potentialTarget))
							&& !sentry.isTargetingAllowedPlayer(potentialTarget)
							&& !EntityUtils.isInvisible(potentialTarget)) {
						break;
					}
					//@formatter:on
				}

				if (mode.attacksHostile() && isSupportedTarget(potentialTarget))
					break;
			}

			if (i < list.size()) {
				if (isCloseEnough(list.get(i))) {
					targetEntity = list.get(i);
					taskOwner.setAttackTarget(targetEntity);
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public boolean shouldContinueExecuting() {
		return (isSupportedTarget(targetEntity) || targetEntity instanceof EntityPlayer) && isCloseEnough(targetEntity) && shouldExecute() && !sentry.isTargetingAllowedPlayer(targetEntity) && super.shouldContinueExecuting();
	}

	public boolean isCloseEnough(Entity entity) {
		return entity != null && taskOwner.getDistanceSq(entity) <= getTargetDistance() * getTargetDistance();
	}

	public boolean isSupportedTarget(EntityLivingBase potentialTarget) {
		//@formatter:off
		return potentialTarget.deathTime == 0 &&
				(potentialTarget instanceof EntityMob ||
						potentialTarget instanceof EntityFlying ||
						potentialTarget instanceof EntitySlime ||
						potentialTarget instanceof EntityShulker ||
						potentialTarget instanceof EntityDragon ||
						SecurityCraftAPI.getRegisteredSentryAttackTargetChecks().stream().anyMatch(check -> check.canAttack(potentialTarget)));
		//@formatter:on
	}

	@Override
	protected double getTargetDistance() {
		return EntitySentry.MAX_TARGET_DISTANCE;
	}
}
