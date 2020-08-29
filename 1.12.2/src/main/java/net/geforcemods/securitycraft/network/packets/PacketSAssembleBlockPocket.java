package net.geforcemods.securitycraft.network.packets;

import io.netty.buffer.ByteBuf;
import net.geforcemods.securitycraft.tileentity.TileEntityBlockPocketManager;
import net.geforcemods.securitycraft.util.WorldUtils;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSAssembleBlockPocket implements IMessage
{
	private BlockPos pos;
	private int dimension, size;

	public PacketSAssembleBlockPocket() {}

	public PacketSAssembleBlockPocket(TileEntityBlockPocketManager te, int size)
	{
		pos = te.getPos();
		dimension = te.getWorld().provider.getDimension();
		this.size = size;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		pos = BlockPos.fromLong(buf.readLong());
		dimension = buf.readInt();
		size = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeLong(pos.toLong());
		buf.writeInt(dimension);
		buf.writeInt(size);
	}

	public static class Handler implements IMessageHandler<PacketSAssembleBlockPocket, IMessage>
	{
		@Override
		public IMessage onMessage(PacketSAssembleBlockPocket message, MessageContext ctx)
		{
			WorldUtils.addScheduledTask(ctx.getServerHandler().player.world, () -> {
				TileEntity te = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(message.dimension).getTileEntity(message.pos);

				if(te instanceof TileEntityBlockPocketManager)
				{
					((TileEntityBlockPocketManager)te).size = message.size;
					((TileEntityBlockPocketManager)te).autoAssembleMultiblock(ctx.getServerHandler().player);
				}
			});

			return null;
		}
	}
}
