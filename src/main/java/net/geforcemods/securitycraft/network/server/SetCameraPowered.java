package net.geforcemods.securitycraft.network.server;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.blocks.SecurityCameraBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

public class SetCameraPowered {
	private BlockPos pos;
	private boolean powered;

	public SetCameraPowered() {}

	public SetCameraPowered(BlockPos pos, boolean powered) {
		this.pos = pos;
		this.powered = powered;
	}

	public static void encode(SetCameraPowered message, PacketBuffer buf) {
		buf.writeBlockPos(message.pos);
		buf.writeBoolean(message.powered);
	}

	public static SetCameraPowered decode(PacketBuffer buf) {
		SetCameraPowered message = new SetCameraPowered();

		message.pos = buf.readBlockPos();
		message.powered = buf.readBoolean();
		return message;
	}

	public static void onMessage(SetCameraPowered message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			BlockPos pos = message.pos;
			PlayerEntity player = ctx.get().getSender();
			World world = player.level;
			TileEntity te = world.getBlockEntity(pos);

			if ((te instanceof IOwnable && ((IOwnable) te).isOwnedBy(player)) || (te instanceof IModuleInventory && ((IModuleInventory) te).isAllowed(player))) {
				BlockState state = world.getBlockState(pos);

				world.setBlockAndUpdate(pos, state.setValue(SecurityCameraBlock.POWERED, message.powered));
				world.updateNeighborsAt(pos.relative(state.getValue(SecurityCameraBlock.FACING), -1), state.getBlock());
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
