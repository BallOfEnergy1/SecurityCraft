package net.geforcemods.securitycraft.tileentity;

import java.util.EnumMap;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ICustomizable;
import net.geforcemods.securitycraft.api.ILockable;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.INameSetter;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.OptionBoolean;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.blocks.BlockKeypadChest;
import net.geforcemods.securitycraft.entity.ai.EntitySentry;
import net.geforcemods.securitycraft.inventory.InsertOnlyDoubleChestHandler;
import net.geforcemods.securitycraft.items.ItemModule;
import net.geforcemods.securitycraft.misc.EnumModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityKeypadChest extends TileEntityChest implements IPasswordProtected, IOwnable, IModuleInventory, ICustomizable, INameSetter, ILockable {
	private InsertOnlyDoubleChestHandler insertOnlyHandler;
	private String passcode;
	private Owner owner = new Owner();
	private NonNullList<ItemStack> modules = NonNullList.<ItemStack>withSize(getMaxNumberOfModules(), ItemStack.EMPTY);
	private OptionBoolean sendMessage = new OptionBoolean("sendMessage", true);
	private EnumMap<EnumModuleType, Boolean> moduleStates = new EnumMap<>(EnumModuleType.class);

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);

		writeModuleInventory(tag);
		writeModuleStates(tag);
		writeOptions(tag);

		if (passcode != null && !passcode.isEmpty())
			tag.setString("passcode", passcode);

		if (owner != null)
			owner.writeToNBT(tag, false);

		return tag;
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);

		modules = readModuleInventory(tag);
		moduleStates = readModuleStates(tag);
		readOptions(tag);
		passcode = tag.getString("passcode");
		owner.readFromNBT(tag);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 1, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (T) BlockUtils.getProtectedCapability(facing, this, () -> super.getCapability(capability, facing), () -> getInsertOnlyHandler());
		else
			return super.getCapability(capability, facing);
	}

	private InsertOnlyDoubleChestHandler getInsertOnlyHandler() {
		if (insertOnlyHandler == null || insertOnlyHandler.needsRefresh())
			insertOnlyHandler = InsertOnlyDoubleChestHandler.get(this);

		return insertOnlyHandler;
	}

	public IItemHandler getHandlerForSentry(EntitySentry entity) {
		if (entity.getOwner().owns(this))
			return super.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.UP);
		else
			return null;
	}

	@Override
	public boolean enableHack() {
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return slot >= 100 ? getModuleInSlot(slot) : super.getStackInSlot(slot);
	}

	@Override
	public void activate(EntityPlayer player) {
		if (!world.isRemote && !isBlocked())
			((BlockKeypadChest) getBlockType()).activate(world, pos, player);
	}

	@Override
	public void openPasswordGUI(World world, BlockPos pos, EntityPlayer player) {
		if (!world.isRemote && !isBlocked())
			IPasswordProtected.super.openPasswordGUI(world, pos, player);
	}

	@Override
	public void onModuleInserted(ItemStack stack, EnumModuleType module, boolean toggled) {
		IModuleInventory.super.onModuleInserted(stack, module, toggled);

		addOrRemoveModuleFromAttached(stack, false, toggled);
	}

	@Override
	public void onModuleRemoved(ItemStack stack, EnumModuleType module, boolean toggled) {
		IModuleInventory.super.onModuleRemoved(stack, module, toggled);

		addOrRemoveModuleFromAttached(stack, true, toggled);
	}

	@Override
	public boolean isModuleEnabled(EnumModuleType module) {
		return hasModule(module) && moduleStates.get(module) == Boolean.TRUE; //prevent NPE
	}

	@Override
	public void toggleModuleState(EnumModuleType module, boolean shouldBeEnabled) {
		moduleStates.put(module, shouldBeEnabled);

		if (shouldBeEnabled)
			onModuleInserted(getModule(module), module, true);
		else
			onModuleRemoved(getModule(module), module, true);
	}

	@Override
	public void onOptionChanged(Option<?> option) {
		if (option instanceof OptionBoolean) {
			TileEntityKeypadChest offsetTe = findOther();

			if (offsetTe != null)
				offsetTe.setSendsMessages(((OptionBoolean) option).get());
		}
	}

	@Override
	public void dropAllModules() {
		TileEntityKeypadChest offsetTe = findOther();

		for (ItemStack module : getInventory()) {
			if (!(module.getItem() instanceof ItemModule))
				continue;

			if (offsetTe != null)
				offsetTe.removeModule(((ItemModule) module.getItem()).getModuleType(), false);

			Block.spawnAsEntity(world, pos, module);
		}

		getInventory().clear();
	}

	public void addOrRemoveModuleFromAttached(ItemStack module, boolean remove, boolean toggled) {
		if (module.isEmpty() || !(module.getItem() instanceof ItemModule))
			return;

		TileEntityKeypadChest offsetTe = findOther();

		if (offsetTe != null) {
			EnumModuleType moduleType = ((ItemModule) module.getItem()).getModuleType();

			if (toggled && offsetTe.isModuleEnabled(moduleType) != remove)
				return;
			else if (!toggled && offsetTe.hasModule(moduleType) != remove)
				return;

			if (remove)
				offsetTe.removeModule(moduleType, toggled);
			else
				offsetTe.insertModule(module, toggled);
		}
	}

	public TileEntityKeypadChest findOther() {
		IBlockState state = world.getBlockState(pos);

		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			BlockPos offsetPos = pos.offset(facing);
			IBlockState offsetState = world.getBlockState(offsetPos);

			if (state.getBlock() == offsetState.getBlock()) {
				TileEntity offsetTe = world.getTileEntity(offsetPos);

				if (offsetTe instanceof TileEntityKeypadChest)
					return (TileEntityKeypadChest) offsetTe;
			}
		}

		return null;
	}

	@Override
	public void onOwnerChanged(IBlockState state, World level, BlockPos pos, EntityPlayer player) {
		TileEntityKeypadChest otherHalf = findOther();

		if (otherHalf != null)
			otherHalf.setOwner(getOwner().getUUID(), getOwner().getName());

		IOwnable.super.onOwnerChanged(state, level, pos, player);
	}

	@Override
	public String getPassword() {
		return (passcode != null && !passcode.isEmpty()) ? passcode : null;
	}

	@Override
	public void setPassword(String password) {
		passcode = password;
	}

	@Override
	public Owner getOwner() {
		return owner;
	}

	@Override
	public void setOwner(String uuid, String name) {
		owner.set(uuid, name);
	}

	@Override
	protected TileEntityChest getAdjacentChest(EnumFacing side) {
		BlockPos blockpos = pos.offset(side);

		if (isChestAt(blockpos)) {
			TileEntity te = world.getTileEntity(blockpos);

			if (te instanceof TileEntityKeypadChest) {
				TileEntityKeypadChest tileentitychest = (TileEntityKeypadChest) te;

				tileentitychest.setNeighbor(this, side.getOpposite());
				return tileentitychest;
			}
		}

		return null;
	}

	@Override
	public boolean isChestAt(BlockPos pos) {
		return world != null && world.getBlockState(pos).getBlock() instanceof BlockKeypadChest;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		if (!player.isSpectator()) {
			if (numPlayersUsing < 0)
				numPlayersUsing = 0;

			++numPlayersUsing;
			world.addBlockEvent(pos, getBlockType(), 1, numPlayersUsing);
			world.notifyNeighborsOfStateChange(pos, getBlockType(), false);

			if (isModuleEnabled(EnumModuleType.REDSTONE))
				world.notifyNeighborsOfStateChange(pos.down(), getBlockType(), false);
		}
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		if (!player.isSpectator() && getBlockType() instanceof BlockKeypadChest) {
			--numPlayersUsing;
			world.addBlockEvent(pos, getBlockType(), 1, numPlayersUsing);
			world.notifyNeighborsOfStateChange(pos, getBlockType(), false);

			if (isModuleEnabled(EnumModuleType.REDSTONE))
				world.notifyNeighborsOfStateChange(pos.down(), getBlockType(), false);
		}
	}

	public boolean isBlocked() {
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			BlockPos pos = getPos().offset(facing);

			if (world.getBlockState(pos).getBlock() instanceof BlockKeypadChest && BlockKeypadChest.isBlocked(world, pos))
				return true;
		}

		return isSingleBlocked();
	}

	public boolean isSingleBlocked() {
		return BlockKeypadChest.isBlocked(getWorld(), getPos());
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 2, 2));
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return modules;
	}

	@Override
	public EnumModuleType[] acceptedModules() {
		return new EnumModuleType[] {
				EnumModuleType.ALLOWLIST, EnumModuleType.DENYLIST, EnumModuleType.REDSTONE
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				sendMessage
		};
	}

	public boolean sendsMessages() {
		return sendMessage.get();
	}

	public void setSendsMessages(boolean value) {
		IBlockState state = world.getBlockState(pos);

		sendMessage.setValue(value);
		world.notifyBlockUpdate(pos, state, state, 3); //sync option change to client
	}

	@Override
	public ITextComponent getDisplayName() {
		return hasCustomName() ? new TextComponentString(customName) : getDefaultName();
	}

	@Override
	public ITextComponent getDefaultName() {
		return Utils.localize(SCContent.keypadChest.getTranslationKey() + ".name");
	}
}
