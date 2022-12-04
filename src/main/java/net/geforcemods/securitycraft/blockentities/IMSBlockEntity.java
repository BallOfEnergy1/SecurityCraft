package net.geforcemods.securitycraft.blockentities;

import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.blocks.mines.IMSBlock;
import net.geforcemods.securitycraft.entity.IMSBomb;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.EntityUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;

public class IMSBlockEntity extends CustomizableBlockEntity implements ITickableTileEntity {
	private IntOption range = new IntOption(this::getBlockPos, "range", 15, 1, 30, 1, true);
	private DisabledOption disabled = new DisabledOption(false);
	/** Number of bombs remaining in storage. **/
	private int bombsRemaining = 4;
	/**
	 * The targeting option currently selected for this IMS. PLAYERS = players, PLAYERS_AND_MOBS = hostile mobs & players, MOBS =
	 * hostile mobs.
	 **/
	private IMSTargetingMode targetingMode = IMSTargetingMode.PLAYERS_AND_MOBS;
	private boolean updateBombCount = false;
	private int attackTime = getAttackInterval();

	public IMSBlockEntity() {
		super(SCContent.IMS_BLOCK_ENTITY.get());
	}

	@Override
	public void tick() {
		if (!level.isClientSide && updateBombCount) {
			int mineCount = getBlockState().getValue(IMSBlock.MINES);

			if (!(mineCount - 1 < 0 || mineCount > 4))
				level.setBlockAndUpdate(worldPosition, getBlockState().setValue(IMSBlock.MINES, mineCount - 1));

			updateBombCount = false;
		}

		if (!isDisabled() && attackTime-- == 0) {
			attackTime = getAttackInterval();
			launchMine();
		}
	}

	/**
	 * Create a bounding box around the IMS, and fire a mine if a mob or player is found.
	 */
	private void launchMine() {
		if (bombsRemaining > 0) {
			AxisAlignedBB area = new AxisAlignedBB(worldPosition).inflate(range.get());
			LivingEntity target = null;

			if (targetingMode == IMSTargetingMode.MOBS || targetingMode == IMSTargetingMode.PLAYERS_AND_MOBS) {
				List<MonsterEntity> mobs = level.<MonsterEntity>getEntitiesOfClass(MonsterEntity.class, area, e -> !EntityUtils.isInvisible(e) && canAttackEntity(e));

				if (!mobs.isEmpty())
					target = mobs.get(0);
			}

			if (target == null && (targetingMode == IMSTargetingMode.PLAYERS || targetingMode == IMSTargetingMode.PLAYERS_AND_MOBS)) {
				List<PlayerEntity> players = level.<PlayerEntity>getEntitiesOfClass(PlayerEntity.class, area, e -> !EntityUtils.isInvisible(e) && canAttackEntity(e));

				if (!players.isEmpty())
					target = players.get(0);
			}

			if (target != null) {
				double addToX = bombsRemaining == 4 || bombsRemaining == 3 ? 0.84375D : 0.0D; //0.84375 is the offset towards the bomb's position in the model
				double addToZ = bombsRemaining == 4 || bombsRemaining == 2 ? 0.84375D : 0.0D;
				int launchHeight = getLaunchHeight();
				double accelerationX = target.getX() - worldPosition.getX();
				double accelerationY = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - worldPosition.getY() - launchHeight;
				double accelerationZ = target.getZ() - worldPosition.getZ();

				level.addFreshEntity(new IMSBomb(level, worldPosition.getX() + addToX, worldPosition.getY(), worldPosition.getZ() + addToZ, accelerationX, accelerationY, accelerationZ, launchHeight, this));

				if (!level.isClientSide)
					level.playSound(null, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundEvents.ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F);

				bombsRemaining--;
				updateBombCount = true;
			}
		}
	}

	public boolean canAttackEntity(LivingEntity entity) {
		return entity != null && (!(entity instanceof PlayerEntity) || !getOwner().isOwner((PlayerEntity) entity) && !((PlayerEntity) entity).isCreative() && !((PlayerEntity) entity).isSpectator()) //PlayerEntity checks
				&& !(isAllowed(entity)); //checks for all entities
	}

	/**
	 * Returns the amount of blocks the {@link IMSBomb} should move up before firing at an entity.
	 */
	private int getLaunchHeight() {
		int height;

		for (height = 1; height <= 9; height++) {
			BlockState state = getLevel().getBlockState(getBlockPos().above(height));

			if (state == null || state.isAir(getLevel(), getBlockPos()))
				continue;
			else
				break;
		}

		return height;
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		super.save(tag);

		tag.putInt("bombsRemaining", bombsRemaining);
		tag.putInt("targetingOption", targetingMode.ordinal());
		tag.putBoolean("updateBombCount", updateBombCount);
		return tag;
	}

	@Override
	public void load(BlockState state, CompoundNBT tag) {
		super.load(state, tag);

		bombsRemaining = tag.getInt("bombsRemaining");
		targetingMode = IMSTargetingMode.values()[tag.getInt("targetingOption")];
		updateBombCount = tag.getBoolean("updateBombCount");
	}

	public void setBombsRemaining(int bombsRemaining) {
		this.bombsRemaining = bombsRemaining;
	}

	public IMSTargetingMode getTargetingMode() {
		return targetingMode;
	}

	public void setTargetingMode(IMSTargetingMode targetingOption) {
		this.targetingMode = targetingOption;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.SPEED
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				range, disabled
		};
	}

	public int getAttackInterval() {
		return isModuleEnabled(ModuleType.SPEED) ? 40 : 80;
	}

	public boolean isDisabled() {
		return disabled.get();
	}

	public static enum IMSTargetingMode {
		PLAYERS,
		PLAYERS_AND_MOBS,
		MOBS;
	}
}
