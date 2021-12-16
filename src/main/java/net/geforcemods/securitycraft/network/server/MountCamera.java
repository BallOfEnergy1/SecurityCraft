package net.geforcemods.securitycraft.network.server;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity;
import net.geforcemods.securitycraft.blocks.SecurityCameraBlock;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class MountCamera
{
	private BlockPos pos;

	public MountCamera() {}

	public MountCamera(BlockPos pos)
	{
		this.pos = pos;
	}

	public static void encode(MountCamera message, FriendlyByteBuf buf)
	{
		buf.writeBlockPos(message.pos);
	}

	public static MountCamera decode(FriendlyByteBuf buf)
	{
		MountCamera message = new MountCamera();

		message.pos = buf.readBlockPos();
		return message;
	}

	public static void onMessage(MountCamera message, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			BlockPos pos = message.pos;
			ServerPlayer player = ctx.get().getSender();
			Level level = player.level;
			BlockState state = level.getBlockState(pos);

			if(level.isLoaded(pos) && state.getBlock() == SCContent.SECURITY_CAMERA.get() && level.getBlockEntity(pos) instanceof SecurityCameraBlockEntity te)
			{
				if(te.getOwner().isOwner(player) || ModuleUtils.isAllowed(te, player))
					((SecurityCameraBlock)state.getBlock()).mountCamera(level, pos, player);
				else
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.CAMERA_MONITOR.get().getDescriptionId()), Utils.localize("messages.securitycraft:notOwned", te.getOwner().getName()), ChatFormatting.RED);
			}
			else
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.CAMERA_MONITOR.get().getDescriptionId()), Utils.localize("messages.securitycraft:cameraMonitor.cameraNotAvailable", pos), ChatFormatting.RED);
		});

		ctx.get().setPacketHandled(true);
	}
}
