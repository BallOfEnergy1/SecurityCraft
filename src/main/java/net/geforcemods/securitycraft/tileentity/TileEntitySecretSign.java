package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.api.ICustomizable;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.OptionBoolean;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.misc.EnumModuleType;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.NonNullList;

public class TileEntitySecretSign extends TileEntitySign implements IOwnable, IModuleInventory, ICustomizable {
	private Owner owner = new Owner();
	private OptionBoolean isSecret = new OptionBoolean("isSecret", true);
	private NonNullList<ItemStack> modules = NonNullList.<ItemStack> withSize(getMaxNumberOfModules(), ItemStack.EMPTY);

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);

		writeModuleInventory(tag);
		writeOptions(tag);

		if (owner != null)
			owner.writeToNBT(tag, false);

		return tag;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);

		modules = readModuleInventory(tag);
		readOptions(tag);
		owner.readFromNBT(tag);
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return modules;
	}

	@Override
	public EnumModuleType[] acceptedModules() {
		return new EnumModuleType[] {
				EnumModuleType.ALLOWLIST
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				isSecret
		};
	}

	public boolean isSecret() {
		return isSecret.get();
	}

	public boolean isPlayerAllowedToSeeText(EntityPlayer player) {
		return !isSecret() || getOwner().isOwner(player) || ModuleUtils.isAllowed(this, player);
	}

	@Override
	public void onOptionChanged(Option<?> option) {
		IBlockState state = world.getBlockState(pos);

		world.notifyBlockUpdate(pos, state, state, 2);
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
	public Owner getOwner() {
		return owner;
	}

	@Override
	public void setOwner(String uuid, String name) {
		owner.set(uuid, name);
	}
}
