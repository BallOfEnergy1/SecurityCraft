package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.OptionBoolean;
import net.geforcemods.securitycraft.api.Option.OptionInt;
import net.geforcemods.securitycraft.blocks.BlockKeypad;
import net.geforcemods.securitycraft.gui.GuiHandler;
import net.geforcemods.securitycraft.misc.EnumModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

public class TileEntityKeypad extends TileEntityDisguisable implements IPasswordProtected {

	private String passcode;

	private OptionBoolean isAlwaysActive = new OptionBoolean("isAlwaysActive", false) {
		@Override
		public void toggle() {
			super.toggle();

			BlockUtils.setBlockProperty(world, pos, BlockKeypad.POWERED, getValue());
			world.notifyNeighborsOfStateChange(pos, SCContent.keypad, false);
		}
	};
	private OptionBoolean sendMessage = new OptionBoolean("sendMessage", true);
	private OptionInt signalLength = new OptionInt(this, "signalLength", 60, 5, 400, 5, true); //20 seconds max

	/**
	 * Writes a tile entity to NBT.
	 * @return
	 */
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);

		if(passcode != null && !passcode.isEmpty())
			tag.setString("passcode", passcode);

		return tag;
	}

	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);

		if (tag.hasKey("passcode"))
			if(tag.getInteger("passcode") != 0)
				passcode = String.valueOf(tag.getInteger("passcode"));
			else
				passcode = tag.getString("passcode");
	}

	@Override
	public void activate(EntityPlayer player) {
		if(!world.isRemote && BlockUtils.getBlock(getWorld(), getPos()) instanceof BlockKeypad)
			BlockKeypad.activate(world, pos, signalLength.asInteger());
	}

	@Override
	public void openPasswordGUI(EntityPlayer player) {
		if(getPassword() != null)
			player.openGui(SecurityCraft.instance, GuiHandler.INSERT_PASSWORD_ID, world, pos.getX(), pos.getY(), pos.getZ());
		else
		{
			if(getOwner().isOwner(player))
				player.openGui(SecurityCraft.instance, GuiHandler.SETUP_PASSWORD_ID, world, pos.getX(), pos.getY(), pos.getZ());
			else
				PlayerUtils.sendMessageToPlayer(player, "SecurityCraft", ClientUtils.localize("messages.securitycraft:passwordProtected.notSetUp"), TextFormatting.DARK_RED);
		}
	}

	@Override
	public boolean onCodebreakerUsed(IBlockState blockState, EntityPlayer player, boolean isCodebreakerDisabled) {
		if(isCodebreakerDisabled)
			PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("tile.securitycraft:keypad.name"), ClientUtils.localize("messages.securitycraft:codebreakerDisabled"), TextFormatting.RED);
		else if(!BlockUtils.getBlockProperty(world, pos, BlockKeypad.POWERED)) {
			activate(player);
			return true;
		}

		return false;
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
	public EnumModuleType[] acceptedModules() {
		return new EnumModuleType[]{EnumModuleType.WHITELIST, EnumModuleType.BLACKLIST, EnumModuleType.DISGUISE};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[]{ isAlwaysActive, sendMessage, signalLength };
	}

	public boolean sendsMessages()
	{
		return sendMessage.asBoolean();
	}

	public int getSignalLength()
	{
		return signalLength.asInteger();
	}
}
