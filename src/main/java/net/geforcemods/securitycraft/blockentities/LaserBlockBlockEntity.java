package net.geforcemods.securitycraft.blockentities;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.ILinkedAction;
import net.geforcemods.securitycraft.api.LinkableBlockEntity;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.DisabledOption;
import net.geforcemods.securitycraft.api.Option.IgnoreOwnerOption;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.api.Option.SignalLengthOption;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blocks.LaserBlock;
import net.geforcemods.securitycraft.blocks.LaserFieldBlock;
import net.geforcemods.securitycraft.inventory.InsertOnlyInvWrapper;
import net.geforcemods.securitycraft.inventory.LaserBlockMenu;
import net.geforcemods.securitycraft.inventory.LensContainer;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.network.client.UpdateLaserColors;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IInventoryChangedListener;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class LaserBlockBlockEntity extends LinkableBlockEntity implements INamedContainerProvider, IInventoryChangedListener {
	private DisabledOption disabled = new DisabledOption(false) {
		@Override
		public void toggle() {
			setValue(!get());
			setLasersAccordingToDisabledOption();
		}
	};
	private IgnoreOwnerOption ignoreOwner = new IgnoreOwnerOption(true);
	private IntOption signalLength = new SignalLengthOption(this::getBlockPos, 50);
	private Map<Direction, Boolean> sideConfig = Util.make(() -> {
		Map<Direction, Boolean> map = new EnumMap<>(Direction.class);

		for (Direction dir : Direction.values()) {
			map.put(dir, true);
		}

		return map;
	});
	private LazyOptional<IItemHandler> insertOnlyHandler, lensHandler;
	private LensContainer lenses = new LensContainer(6);

	public LaserBlockBlockEntity() {
		super(SCContent.LASER_BLOCK_BLOCK_ENTITY.get());
		lenses.addListener(this);
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		super.save(tag);
		tag.put("sideConfig", saveSideConfig(sideConfig));

		for (int i = 0; i < lenses.getContainerSize(); i++) {
			tag.put("lens" + i, lenses.getItem(i).save(new CompoundNBT()));
		}

		return tag;
	}

	public static CompoundNBT saveSideConfig(Map<Direction, Boolean> sideConfig) {
		CompoundNBT sideConfigTag = new CompoundNBT();

		sideConfig.forEach((dir, enabled) -> sideConfigTag.putBoolean(dir.getName(), enabled));
		return sideConfigTag;
	}

	@Override
	public void load(BlockState state, CompoundNBT tag) {
		super.load(state, tag);
		sideConfig = loadSideConfig(tag.getCompound("sideConfig"));

		for (int i = 0; i < lenses.getContainerSize(); i++) {
			lenses.setItemExclusively(i, ItemStack.of(tag.getCompound("lens" + i)));
		}

		lenses.setChanged();
	}

	public static Map<Direction, Boolean> loadSideConfig(CompoundNBT sideConfigTag) {
		Map<Direction, Boolean> sideConfig = new EnumMap<>(Direction.class);

		for (Direction dir : Direction.values()) {
			if (sideConfigTag.contains(dir.getName(), Constants.NBT.TAG_BYTE))
				sideConfig.put(dir, sideConfigTag.getBoolean(dir.getName()));
			else
				sideConfig.put(dir, true);
		}

		return sideConfig;
	}

	@Override
	protected void onLinkedBlockAction(ILinkedAction action, List<LinkableBlockEntity> excludedBEs) {
		if (action instanceof ILinkedAction.OptionChanged) {
			Option<?> option = ((ILinkedAction.OptionChanged<?>) action).option;

			if (option.getName().equals("disabled")) {
				disabled.copy(option);
				setLasersAccordingToDisabledOption();
			}
			else if (option.getName().equals("ignoreOwner"))
				ignoreOwner.copy(option);
			else if (option.getName().equals("signalLength"))
				signalLength.copy(option);
		}
		else if (action instanceof ILinkedAction.ModuleInserted) {
			ILinkedAction.ModuleInserted moduleInserted = (ILinkedAction.ModuleInserted) action;

			insertModule(moduleInserted.stack, moduleInserted.wasModuleToggled);
		}
		else if (action instanceof ILinkedAction.ModuleRemoved) {
			ILinkedAction.ModuleRemoved moduleRemoved = (ILinkedAction.ModuleRemoved) action;

			removeModule(moduleRemoved.moduleType, moduleRemoved.wasModuleToggled);
		}
		else if (action instanceof ILinkedAction.OwnerChanged) {
			Owner owner = ((ILinkedAction.OwnerChanged) action).newOwner;

			setOwner(owner.getUUID(), owner.getName());
		}
		else if (action instanceof ILinkedAction.StateChanged<?>) {
			BlockState state = getBlockState();

			if (((ILinkedAction.StateChanged<?>) action).property == LaserBlock.POWERED && !state.getValue(LaserBlock.POWERED)) {
				level.setBlockAndUpdate(worldPosition, state.setValue(LaserBlock.POWERED, true));
				BlockUtils.updateIndirectNeighbors(level, worldPosition, SCContent.LASER_BLOCK.get());
				level.getBlockTicks().scheduleTick(worldPosition, SCContent.LASER_BLOCK.get(), signalLength.get());
			}
		}

		excludedBEs.add(this);
		createLinkedBlockAction(action, excludedBEs);
	}

	@Override
	public void onModuleInserted(ItemStack stack, ModuleType module, boolean toggled) {
		super.onModuleInserted(stack, module, toggled);

		if (module == ModuleType.DISGUISE)
			DisguisableBlockEntity.onDisguiseModuleInserted(this, stack, toggled);
		else if (module == ModuleType.SMART)
			applyExistingSideConfig();
	}

	@Override
	public void onModuleRemoved(ItemStack stack, ModuleType module, boolean toggled) {
		super.onModuleRemoved(stack, module, toggled);

		if (module == ModuleType.DISGUISE)
			DisguisableBlockEntity.onDisguiseModuleRemoved(this, stack, toggled);
		else if (module == ModuleType.REDSTONE) {
			if (getBlockState().getValue(LaserBlock.POWERED)) {
				level.setBlockAndUpdate(worldPosition, getBlockState().setValue(LaserBlock.POWERED, false));
				BlockUtils.updateIndirectNeighbors(level, worldPosition, SCContent.LASER_BLOCK.get());
			}
		}
		else if (module == ModuleType.SMART)
			applyExistingSideConfig();
	}

	@Override
	public void containerChanged(IInventory container) {
		if (level == null)
			return;

		for (Direction direction : Direction.values()) {
			int i = 1;
			BlockPos pos = getBlockPos();
			BlockPos modifiedPos = pos.relative(direction, i);
			BlockState stateAtModifiedPos = level.getBlockState(modifiedPos);
			List<BlockPos> positionsToUpdate = new ArrayList<>();

			while (i < ConfigHandler.SERVER.laserBlockRange.get() && stateAtModifiedPos.getBlock() != SCContent.LASER_BLOCK.get()) {
				modifiedPos = pos.relative(direction, ++i);
				stateAtModifiedPos = level.getBlockState(modifiedPos);
				positionsToUpdate.add(modifiedPos);
			}

			TileEntity te = level.getBlockEntity(modifiedPos);

			if (te instanceof LaserBlockBlockEntity) {
				LaserBlockBlockEntity otherLaser = (LaserBlockBlockEntity) te;

				otherLaser.getLensContainer().setItemExclusively(direction.getOpposite().ordinal(), lenses.getItem(direction.ordinal()));

				if (!level.isClientSide)
					SecurityCraft.channel.send(PacketDistributor.DIMENSION.with(() -> level.dimension()), new UpdateLaserColors(positionsToUpdate));

				level.sendBlockUpdated(modifiedPos, stateAtModifiedPos, stateAtModifiedPos, 2);
			}
		}

		setChanged();
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		super.handleUpdateTag(state, tag);
		DisguisableBlockEntity.onHandleUpdateTag(this);
	}

	@Override
	public void readOptions(CompoundNBT tag) {
		if (tag.contains("enabled"))
			tag.putBoolean("disabled", !tag.getBoolean("enabled")); //legacy support

		for (Option<?> option : customOptions()) {
			option.load(tag);
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
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
			insertOnlyHandler = LazyOptional.of(() -> new InsertOnlyInvWrapper(lenses));

		return insertOnlyHandler;
	}

	private LazyOptional<IItemHandler> getNormalHandler() {
		if (lensHandler == null)
			lensHandler = LazyOptional.of(() -> new InvWrapper(lenses));

		return lensHandler;
	}

	@Override
	public Container createMenu(int id, PlayerInventory inventory, PlayerEntity player) {
		return new LaserBlockMenu(id, level, worldPosition, sideConfig, inventory);
	}

	@Override
	public ITextComponent getDisplayName() {
		return super.getDisplayName();
	}

	public LensContainer getLensContainer() {
		return lenses;
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		DisguisableBlockEntity.onSetRemoved(this);
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.HARMING, ModuleType.ALLOWLIST, ModuleType.DISGUISE, ModuleType.REDSTONE, ModuleType.SMART
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				disabled, ignoreOwner, signalLength
		};
	}

	@Override
	public IModelData getModelData() {
		return DisguisableBlockEntity.DEFAULT_MODEL_DATA.get();
	}

	public boolean isEnabled() {
		return !disabled.get();
	}

	public boolean ignoresOwner() {
		return ignoreOwner.get();
	}

	public int getSignalLength() {
		return signalLength.get();
	}

	public void applyNewSideConfig(Map<Direction, Boolean> sideConfig, PlayerEntity player) {
		sideConfig.forEach((direction, enabled) -> setSideEnabled(direction, enabled, player));
	}

	public void applyExistingSideConfig() {
		for (Direction direction : Direction.values()) {
			toggleLaserOnSide(direction, isSideEnabled(direction), null, false);
		}
	}

	public void setSideEnabled(Direction direction, boolean enabled, PlayerEntity player) {
		sideConfig.put(direction, enabled);

		if (isModuleEnabled(ModuleType.SMART))
			toggleLaserOnSide(direction, enabled, player, true);
	}

	public void toggleLaserOnSide(Direction direction, boolean enabled, PlayerEntity player, boolean modifyOtherLaser) {
		int i = 1;
		BlockPos pos = getBlockPos();
		BlockPos modifiedPos = pos.relative(direction, i);
		BlockState stateAtModifiedPos = level.getBlockState(modifiedPos);

		while (i < ConfigHandler.SERVER.laserBlockRange.get() && stateAtModifiedPos.getBlock() != SCContent.LASER_BLOCK.get()) {
			modifiedPos = pos.relative(direction, ++i);
			stateAtModifiedPos = level.getBlockState(modifiedPos);
		}

		if (modifyOtherLaser) {
			TileEntity te = level.getBlockEntity(modifiedPos);

			if (te instanceof LaserBlockBlockEntity)
				((LaserBlockBlockEntity) te).sideConfig.put(direction.getOpposite(), enabled);
		}

		if (enabled) {
			Block block = getBlockState().getBlock();

			if (block instanceof LaserBlock)
				((LaserBlock) block).setLaser(level, pos, direction, player);
		}
		else {
			int boundType = LaserFieldBlock.getBoundType(direction);

			BlockUtils.removeInSequence((directionToCheck, stateToCheck) -> stateToCheck.is(SCContent.LASER_FIELD.get()) && stateToCheck.getValue(LaserFieldBlock.BOUNDTYPE) == boundType, level, worldPosition, direction);
		}
	}

	public Map<Direction, Boolean> getSideConfig() {
		return sideConfig;
	}

	public boolean isSideEnabled(Direction dir) {
		return !isModuleEnabled(ModuleType.SMART) || sideConfig.getOrDefault(dir, true);
	}

	private void setLasersAccordingToDisabledOption() {
		if (isEnabled())
			((LaserBlock) getBlockState().getBlock()).setLaser(level, worldPosition, null);
		else
			LaserBlock.destroyAdjacentLasers(level, worldPosition);
	}

	public ModuleType synchronizeWith(LaserBlockBlockEntity that) {
		if (!LinkableBlockEntity.isLinkedWith(this, that)) {
			Map<ItemStack, Boolean> bothInsertedModules = new Object2BooleanArrayMap<>();
			List<ModuleType> thisInsertedModules = getInsertedModules();
			List<ModuleType> thatInsertedModules = that.getInsertedModules();

			for (ModuleType type : thisInsertedModules) {
				ItemStack thisModule = getModule(type);

				if (thatInsertedModules.contains(type) && !thisModule.areShareTagsEqual(that.getModule(type)))
					return type;

				bothInsertedModules.put(thisModule.copy(), isModuleEnabled(type));
				removeModule(type, false);
			}

			for (ModuleType type : thatInsertedModules) {
				bothInsertedModules.put(that.getModule(type).copy(), that.isModuleEnabled(type));
				that.removeModule(type, false);
				createLinkedBlockAction(new ILinkedAction.ModuleRemoved(type, false), that);
			}

			readOptions(that.writeOptions(new CompoundNBT()));
			LinkableBlockEntity.link(this, that);

			for (Entry<ItemStack, Boolean> entry : bothInsertedModules.entrySet()) {
				ItemStack module = entry.getKey();
				ModuleItem item = (ModuleItem) module.getItem();
				ModuleType type = item.getModuleType();

				insertModule(entry.getKey(), false);
				createLinkedBlockAction(new ILinkedAction.ModuleInserted(module, item, false), this);
				toggleModuleState(type, entry.getValue());
				createLinkedBlockAction(new ILinkedAction.ModuleInserted(module, item, true), this);
			}
		}

		return null;
	}
}
