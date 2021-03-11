package net.geforcemods.securitycraft.tileentity;

import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableTileEntity;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.blocks.mines.IMSBlock;
import net.geforcemods.securitycraft.containers.GenericTEContainer;
import net.geforcemods.securitycraft.entity.IMSBombEntity;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.EntityUtils;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class IMSTileEntity extends CustomizableTileEntity implements INamedContainerProvider {

	private IntOption range = new IntOption(this::getPos, "range", 12, 1, 30, 1, true);
	/** Number of bombs remaining in storage. **/
	private int bombsRemaining = 4;
	/** The targeting option currently selected for this IMS. PLAYERS = players, PLAYERS_AND_MOBS = hostile mobs & players, MOBS = hostile mobs.**/
	private IMSTargetingMode targetingMode = IMSTargetingMode.PLAYERS_AND_MOBS;
	private boolean updateBombCount = false;

	public IMSTileEntity()
	{
		super(SCContent.teTypeIms);
	}

	@Override
	public void tick(){
		super.tick();

		if(!world.isRemote && updateBombCount){
			int mineCount = getBlockState().get(IMSBlock.MINES);

			if(!(mineCount - 1 < 0 || mineCount > 4))
				world.setBlockState(pos, getBlockState().with(IMSBlock.MINES, mineCount - 1));

			updateBombCount = false;
		}

		if(world.getGameTime() % 80L == 0L)
			launchMine();
	}

	/**
	 * Create a bounding box around the IMS, and fire a mine if a mob or player is found.
	 */
	private void launchMine() {
		if(bombsRemaining > 0){
			AxisAlignedBB area = new AxisAlignedBB(pos).grow(range.get());
			LivingEntity target = null;

			if(targetingMode == IMSTargetingMode.MOBS || targetingMode == IMSTargetingMode.PLAYERS_AND_MOBS)
			{
				List<MonsterEntity> mobs = world.getEntitiesWithinAABB(MonsterEntity.class, area, e -> !EntityUtils.isInvisible(e) && canAttackEntity(e));

				if(!mobs.isEmpty())
					target = mobs.get(0);
			}

			if(target == null && (targetingMode == IMSTargetingMode.PLAYERS  || targetingMode == IMSTargetingMode.PLAYERS_AND_MOBS))
			{
				List<PlayerEntity> players = world.getEntitiesWithinAABB(PlayerEntity.class, area, e -> !EntityUtils.isInvisible(e) && canAttackEntity(e));

				if(!players.isEmpty())
					target = players.get(0);
			}

			if (target != null) {
				double addToX = bombsRemaining == 4 || bombsRemaining == 3 ? 1.2D : 0.55D;
				double addToZ = bombsRemaining == 4 || bombsRemaining == 2 ? 1.2D : 0.6D;
				int launchHeight = getLaunchHeight();
				double accelerationX = target.posX - pos.getX();
				double accelerationY = target.getBoundingBox().minY + target.getHeight() / 2.0F - pos.getY() - launchHeight;
				double accelerationZ = target.posZ - pos.getZ();

				world.addEntity(new IMSBombEntity(world, pos.getX() + addToX, pos.getY(), pos.getZ() + addToZ, accelerationX, accelerationY, accelerationZ, launchHeight));

				if (!world.isRemote)
					world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F);

				bombsRemaining--;
				updateBombCount = true;
			}
		}
	}

	public boolean canAttackEntity(LivingEntity entity)
	{
		return entity != null
				&& (!(entity instanceof PlayerEntity) || !getOwner().isOwner((PlayerEntity)entity) && !PlayerUtils.isPlayerMountedOnCamera(entity) && !((PlayerEntity)entity).isCreative() && !((PlayerEntity)entity).isSpectator()) //PlayerEntity checks
				&& !(hasModule(ModuleType.WHITELIST) && ModuleUtils.getPlayersFromModule(world, pos, ModuleType.WHITELIST).contains(entity.getName().getString().toLowerCase())); //checks for all entities
	}

	/**
	 * Returns the amount of blocks the {@link IMSBombEntity} should move up before firing at an entity.
	 */
	private int getLaunchHeight() {
		int height;

		for(height = 1; height <= 9; height++)
		{
			BlockState state = getWorld().getBlockState(getPos().up(height));

			if(state == null || state.isAir(getWorld(), getPos()))
				continue;
			else
				break;
		}

		return height;
	}

	/**
	 * Writes a tile entity to NBT.
	 * @return
	 */
	@Override
	public CompoundNBT write(CompoundNBT tag){
		super.write(tag);

		tag.putInt("bombsRemaining", bombsRemaining);
		tag.putInt("targetingOption", targetingMode.ordinal());
		tag.putBoolean("updateBombCount", updateBombCount);
		return tag;
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void read(CompoundNBT tag){
		super.read(tag);

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

	public void setTargetingMode(IMSTargetingMode targetingMode) {
		this.targetingMode = targetingMode;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[]{ModuleType.WHITELIST};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[]{range};
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player)
	{
		return new GenericTEContainer(SCContent.cTypeIMS, windowId, world, pos);
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent(SCContent.IMS.get().getTranslationKey());
	}

	public static enum IMSTargetingMode {
		PLAYERS, PLAYERS_AND_MOBS, MOBS;
	}
}
