package net.geforcemods.securitycraft.entity.ai;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.entity.EntitySentry;
import net.geforcemods.securitycraft.entity.EntitySentry.EnumSentryMode;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.MathHelper;

public class EntityAIAttackRangedIfEnabled extends EntityAIBase {
	private EntitySentry sentry;
	private EntityLivingBase attackTarget;
	private int rangedAttackTime;
	private final Supplier<Integer> maxAttackTime;
	private final float attackRadius;

	public EntityAIAttackRangedIfEnabled(IRangedAttackMob attacker, Supplier<Integer> maxAttackTime, float maxAttackDistance) {
		sentry = (EntitySentry) attacker;
		rangedAttackTime = -1;
		this.maxAttackTime = maxAttackTime;
		attackRadius = maxAttackDistance;
		setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		EntityLivingBase potentialTarget = sentry.getAttackTarget();

		if (potentialTarget == null)
			return false;
		else {
			attackTarget = potentialTarget;
			return sentry.getMode() != EnumSentryMode.IDLE;
		}
	}

	@Override
	public void resetTask() {
		attackTarget = null;
		rangedAttackTime = -3;
	}

	@Override
	public void updateTask() //copied from vanilla to remove pathfinding code
	{
		double targetDistance = sentry.getDistanceSq(attackTarget.posX, attackTarget.getEntityBoundingBox().minY, attackTarget.posZ);

		sentry.getLookHelper().setLookPositionWithEntity(attackTarget, 30.0F, 30.0F);

		if (--rangedAttackTime == 0) {
			if (!sentry.getEntitySenses().canSee(attackTarget))
				return;

			float f = MathHelper.sqrt(targetDistance) / attackRadius;
			float distanceFactor = MathHelper.clamp(f, 0.1F, 1.0F);

			sentry.attackEntityWithRangedAttack(attackTarget, distanceFactor);
			rangedAttackTime = MathHelper.floor(maxAttackTime.get());
		}
		else if (rangedAttackTime < 0)
			rangedAttackTime = MathHelper.floor(maxAttackTime.get());
	}
}
