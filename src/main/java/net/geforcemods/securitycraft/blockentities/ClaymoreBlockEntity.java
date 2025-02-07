package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.IgnoreOwnerOption;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.blocks.mines.ClaymoreBlock;
import net.geforcemods.securitycraft.inventory.ClaymoreMenu;
import net.geforcemods.securitycraft.inventory.InsertOnlyInvWrapper;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.EntityUtils;
import net.geforcemods.securitycraft.util.ITickingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ClaymoreBlockEntity extends CustomizableBlockEntity implements ITickingBlockEntity, MenuProvider {
	private IntOption range = new IntOption("range", 5, 1, 10, 1, true);
	private IgnoreOwnerOption ignoreOwner = new IgnoreOwnerOption(true);
	private LazyOptional<IItemHandler> insertOnlyHandler, lensHandler;
	private SimpleContainer lens = new SimpleContainer(1);
	private int cooldown = -1;

	public ClaymoreBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.CLAYMORE_BLOCK_ENTITY.get(), pos, state);
	}

	@Override
	public void tick(Level level, BlockPos pos, BlockState state) { //server only as per ClaymoreBlock
		if (state.getValue(ClaymoreBlock.DEACTIVATED))
			return;

		if (cooldown > 0) {
			cooldown--;
			return;
		}

		if (cooldown == 0) {
			((ClaymoreBlock) state.getBlock()).explode(level, pos);
			return;
		}

		Direction dir = state.getValue(ClaymoreBlock.FACING);
		AABB area = switch (dir) {
			case NORTH -> new AABB(pos).contract(0, 0, range.get());
			case SOUTH -> new AABB(pos).contract(0, 0, -range.get());
			case EAST -> new AABB(pos).contract(-range.get(), 0, 0);
			case WEST -> new AABB(pos).contract(range.get(), 0, 0);
			default -> new AABB(pos);
		};

		level.getEntitiesOfClass(LivingEntity.class, area, e -> !EntityUtils.isInvisible(e) && !(e instanceof Player player && player.isCreative()) && !(e instanceof OwnableEntity ownableEntity && allowsOwnableEntity(ownableEntity)) && e.canBeSeenByAnyone() && !(EntityUtils.doesEntityOwn(e, level, pos) && ignoresOwner())).stream().findFirst().ifPresent(entity -> {
			cooldown = 20;
			level.playSound(null, BlockPos.containing(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, 0.6F);
		});
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		writeOptions(tag);
		tag.putInt("cooldown", cooldown);
		tag.put("lens", lens.createTag());
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		readOptions(tag);
		cooldown = tag.getInt("cooldown");
		lens.fromTag(tag.getList("lens", Tag.TAG_COMPOUND));
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == ForgeCapabilities.ITEM_HANDLER)
			return BlockUtils.getProtectedCapability(side, this, this::getNormalHandler, this::getInsertOnlyHandler).cast();
		else
			return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		if (insertOnlyHandler != null)
			insertOnlyHandler.invalidate();

		if (lensHandler != null)
			lensHandler.invalidate();

		super.invalidateCaps();
	}

	@Override
	public void reviveCaps() {
		insertOnlyHandler = null;
		lensHandler = null;
		super.reviveCaps();
	}

	private LazyOptional<IItemHandler> getInsertOnlyHandler() {
		if (insertOnlyHandler == null)
			insertOnlyHandler = LazyOptional.of(() -> new InsertOnlyInvWrapper(lens));

		return insertOnlyHandler;
	}

	private LazyOptional<IItemHandler> getNormalHandler() {
		if (lensHandler == null)
			lensHandler = LazyOptional.of(() -> new InvWrapper(lens));

		return lensHandler;
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
		return new ClaymoreMenu(id, level, worldPosition, inventory);
	}

	@Override
	public Component getDisplayName() {
		return super.getDisplayName();
	}

	public Container getLensContainer() {
		return lens;
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				range, ignoreOwner
		};
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[0];
	}

	public boolean ignoresOwner() {
		return ignoreOwner.get();
	}

	@Override
	public AABB getRenderBoundingBox() {
		return new AABB(worldPosition);
	}
}
