package net.geforcemods.securitycraft.network.server;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.blockentities.BlockPocketManagerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class AssembleBlockPocket {
	private BlockPos pos;
	private int size;

	public AssembleBlockPocket() {}

	public AssembleBlockPocket(BlockPocketManagerBlockEntity te, int size) {
		pos = te.getBlockPos();
		this.size = size;
	}

	public static void encode(AssembleBlockPocket message, FriendlyByteBuf buf) {
		buf.writeLong(message.pos.asLong());
		buf.writeInt(message.size);
	}

	public static AssembleBlockPocket decode(FriendlyByteBuf buf) {
		AssembleBlockPocket message = new AssembleBlockPocket();

		message.pos = BlockPos.of(buf.readLong());
		message.size = buf.readInt();
		return message;
	}

	public static void onMessage(AssembleBlockPocket message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (ctx.get().getSender().level.getBlockEntity(message.pos) instanceof BlockPocketManagerBlockEntity be && be.isOwnedBy(ctx.get().getSender())) {
				be.size = message.size;
				be.autoAssembleMultiblock();
				be.setChanged();
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
