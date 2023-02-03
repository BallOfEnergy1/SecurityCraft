package net.geforcemods.securitycraft.network.server;

import java.util.List;

import io.netty.buffer.ByteBuf;
import net.geforcemods.securitycraft.entity.sentry.Sentry;
import net.geforcemods.securitycraft.util.LevelUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetSentryMode implements IMessage {
	public BlockPos pos;
	public int mode;

	public SetSentryMode() {}

	public SetSentryMode(BlockPos sentryPos, int mode) {
		pos = sentryPos;
		this.mode = mode;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong(buf.readLong());
		mode = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos.toLong());
		buf.writeInt(mode);
	}

	public static class Handler implements IMessageHandler<SetSentryMode, IMessage> {
		@Override
		public IMessage onMessage(SetSentryMode message, MessageContext context) {
			LevelUtils.addScheduledTask(context.getServerHandler().player.world, () -> {
				EntityPlayer player = context.getServerHandler().player;
				List<Sentry> sentries = player.world.<Sentry>getEntitiesWithinAABB(Sentry.class, new AxisAlignedBB(message.pos));

				if (!sentries.isEmpty() && sentries.get(0).isOwnedBy(player))
					sentries.get(0).toggleMode(player, message.mode, false);
			});

			return null;
		}
	}
}
