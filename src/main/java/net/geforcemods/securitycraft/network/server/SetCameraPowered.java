package net.geforcemods.securitycraft.network.server;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.blocks.SecurityCameraBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class SetCameraPowered {
	private BlockPos pos;
	private boolean powered;

	public SetCameraPowered() {}

	public SetCameraPowered(BlockPos pos, boolean powered) {
		this.pos = pos;
		this.powered = powered;
	}

	public static void encode(SetCameraPowered message, FriendlyByteBuf buf) {
		buf.writeBlockPos(message.pos);
		buf.writeBoolean(message.powered);
	}

	public static SetCameraPowered decode(FriendlyByteBuf buf) {
		SetCameraPowered message = new SetCameraPowered();

		message.pos = buf.readBlockPos();
		message.powered = buf.readBoolean();
		return message;
	}

	public static void onMessage(SetCameraPowered message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			BlockPos pos = message.pos;
			Player player = ctx.get().getSender();
			Level level = player.level;

			if (level.getBlockEntity(pos) instanceof IOwnable be && be.getOwner().isOwner(player)) {
				level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(SecurityCameraBlock.POWERED, message.powered));
				level.updateNeighborsAt(pos.relative(level.getBlockState(pos).getValue(SecurityCameraBlock.FACING), -1), level.getBlockState(pos).getBlock());
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
