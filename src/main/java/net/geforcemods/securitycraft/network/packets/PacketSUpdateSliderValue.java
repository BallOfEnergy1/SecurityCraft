package net.geforcemods.securitycraft.network.packets;

import io.netty.buffer.ByteBuf;
import net.geforcemods.securitycraft.api.CustomizableSCTE;
import net.geforcemods.securitycraft.api.ICustomizable;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.OptionDouble;
import net.geforcemods.securitycraft.api.Option.OptionInt;
import net.geforcemods.securitycraft.util.WorldUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSUpdateSliderValue implements IMessage{

	private BlockPos pos;
	private int id;
	private double value;

	public PacketSUpdateSliderValue(){ }

	public PacketSUpdateSliderValue(BlockPos pos, int id, double v){
		this.pos = pos;
		this.id = id;
		value = v;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos.toLong());
		buf.writeInt(id);
		buf.writeDouble(value);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong(buf.readLong());
		id = buf.readInt();
		value = buf.readDouble();
	}

	public static class Handler extends PacketHelper implements IMessageHandler<PacketSUpdateSliderValue, IMessage> {

		@Override
		public IMessage onMessage(PacketSUpdateSliderValue message, MessageContext context) {
			WorldUtils.addScheduledTask(getWorld(context.getServerHandler().player), () -> {
				BlockPos pos = message.pos;
				int id = message.id;
				double value = message.value;
				EntityPlayer player = context.getServerHandler().player;
				TileEntity te = getWorld(player).getTileEntity(pos);

				if(te instanceof ICustomizable) {
					Option<?> o = ((ICustomizable)te).customOptions()[id];

					if(o instanceof OptionDouble)
						((OptionDouble)o).setValue(value);
					else if(o instanceof OptionInt)
						((OptionInt)o).setValue((int)value);

					((ICustomizable)te).onOptionChanged(((ICustomizable)te).customOptions()[id]);

					if(te instanceof CustomizableSCTE)
						((CustomizableSCTE)te).sync();
				}
			});

			return null;
		}
	}

}
