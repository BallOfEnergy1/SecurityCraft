package net.geforcemods.securitycraft.network.client;

import io.netty.buffer.ByteBuf;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.misc.EnumModuleType;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RefreshDiguisedModel implements IMessage {
	private BlockPos pos;
	private boolean insert;
	private ItemStack stack;
	private boolean toggled;

	public RefreshDiguisedModel() {}

	public RefreshDiguisedModel(BlockPos pos, boolean insert, ItemStack stack, boolean toggled) {
		this.pos = pos;
		this.insert = insert;
		this.stack = stack;
		this.toggled = toggled;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos.toLong());
		buf.writeBoolean(insert);
		ByteBufUtils.writeItemStack(buf, stack);
		buf.writeBoolean(toggled);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = BlockPos.fromLong(buf.readLong());
		insert = buf.readBoolean();
		stack = ByteBufUtils.readItemStack(buf);
		toggled = buf.readBoolean();
	}

	public static class Handler implements IMessageHandler<RefreshDiguisedModel, IMessage> {
		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(RefreshDiguisedModel message, MessageContext context) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				IModuleInventory te = (IModuleInventory) Minecraft.getMinecraft().world.getTileEntity(message.pos);

				if (te != null) {
					if (message.insert)
						te.insertModule(message.stack, message.toggled);
					else
						te.removeModule(EnumModuleType.DISGUISE, message.toggled);

					Minecraft.getMinecraft().world.markBlockRangeForRenderUpdate(message.pos, message.pos);
				}
			});
			return null;
		}
	}
}
