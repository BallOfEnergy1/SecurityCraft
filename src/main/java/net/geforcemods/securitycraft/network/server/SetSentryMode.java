package net.geforcemods.securitycraft.network.server;

import java.util.List;
import java.util.function.Supplier;

import net.geforcemods.securitycraft.entity.Sentry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class SetSentryMode {
	public BlockPos pos;
	public int mode;

	public SetSentryMode() {}

	public SetSentryMode(BlockPos sentryPos, int mode) {
		pos = sentryPos;
		this.mode = mode;
	}

	public static void encode(SetSentryMode message, PacketBuffer buf) {
		buf.writeBlockPos(message.pos);
		buf.writeInt(message.mode);
	}

	public static SetSentryMode decode(PacketBuffer buf) {
		SetSentryMode message = new SetSentryMode();

		message.pos = buf.readBlockPos();
		message.mode = buf.readInt();
		return message;
	}

	public static void onMessage(SetSentryMode message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			PlayerEntity player = ctx.get().getSender();

			List<Sentry> sentries = player.level.<Sentry>getEntitiesOfClass(Sentry.class, new AxisAlignedBB(message.pos));

			if (!sentries.isEmpty() && sentries.get(0).isOwnedBy(player))
				sentries.get(0).toggleMode(player, message.mode, false);
		});

		ctx.get().setPacketHandled(true);
	}
}
