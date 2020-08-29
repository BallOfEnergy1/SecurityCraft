package net.geforcemods.securitycraft.tileentity;

import java.util.ArrayList;

import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.EnumLinkedAction;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.OptionBoolean;
import net.geforcemods.securitycraft.misc.EnumModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.EntityUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

public class TileEntityScannerDoor extends CustomizableSCTE
{
	private OptionBoolean sendMessage = new OptionBoolean("sendMessage", true);

	@Override
	public void entityViewed(EntityLivingBase entity)
	{
		IBlockState upperState = world.getBlockState(pos);
		IBlockState lowerState = world.getBlockState(pos.down());

		if(!world.isRemote && upperState.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER && !EntityUtils.isInvisible(entity))
		{
			if(!(entity instanceof EntityPlayer))
				return;

			EntityPlayer player = (EntityPlayer)entity;

			if(PlayerUtils.isPlayerMountedOnCamera(player))
				return;

			if(!getOwner().isOwner(player))
			{
				PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("item.securitycraft:scannerDoorItem.name"), ClientUtils.localize("messages.securitycraft:retinalScanner.notOwner").replace("#", getOwner().getName()), TextFormatting.RED);
				return;
			}

			boolean open = !BlockUtils.getBlockProperty(world, pos.down(), BlockDoor.OPEN);

			world.setBlockState(pos, upperState.withProperty(BlockDoor.OPEN, !upperState.getValue(BlockDoor.OPEN)), 3);
			world.setBlockState(pos.down(), lowerState.withProperty(BlockDoor.OPEN, !lowerState.getValue(BlockDoor.OPEN)), 3);
			world.markBlockRangeForRenderUpdate(pos.down(), pos);
			world.playEvent(null, open ? 1005 : 1011, pos, 0);

			if(open && sendMessage.get())
				PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize("item.securitycraft:scannerDoorItem.name"), ClientUtils.localize("messages.securitycraft:retinalScanner.hello").replace("#", player.getName()), TextFormatting.GREEN);
		}
	}

	@Override
	protected void onLinkedBlockAction(EnumLinkedAction action, Object[] parameters, ArrayList<CustomizableSCTE> excludedTEs)
	{
		if(action == EnumLinkedAction.OPTION_CHANGED)
			sendMessage.copy((Option<?>)parameters[0]);
	}

	@Override
	public int getViewCooldown()
	{
		return 30;
	}

	@Override
	public EnumModuleType[] acceptedModules()
	{
		return new EnumModuleType[]{};
	}

	@Override
	public Option<?>[] customOptions()
	{
		return new Option[]{ sendMessage };
	}
}
