package net.geforcemods.securitycraft.network.server;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.api.CustomizableTileEntity;
import net.geforcemods.securitycraft.api.ICustomizable;
import net.geforcemods.securitycraft.api.IOwnable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class ToggleOption {
	private int x, y, z, id;

	public ToggleOption() {}

	public ToggleOption(int x, int y, int z, int id) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.id = id;
	}

	public static void encode(ToggleOption message, PacketBuffer buf) {
		buf.writeInt(message.x);
		buf.writeInt(message.y);
		buf.writeInt(message.z);
		buf.writeInt(message.id);
	}

	public static ToggleOption decode(PacketBuffer buf) {
		ToggleOption message = new ToggleOption();

		message.x = buf.readInt();
		message.y = buf.readInt();
		message.z = buf.readInt();
		message.id = buf.readInt();
		return message;
	}

	public static void onMessage(ToggleOption message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			BlockPos pos = new BlockPos(message.x, message.y, message.z);
			int id = message.id;
			PlayerEntity player = ctx.get().getSender();
			TileEntity te = player.world.getTileEntity(pos);

			if (te instanceof ICustomizable && (!(te instanceof IOwnable) || ((IOwnable) te).getOwner().isOwner(player))) {
				((ICustomizable) te).customOptions()[id].toggle();
				((ICustomizable) te).onOptionChanged(((ICustomizable) te).customOptions()[id]);

				if (te instanceof CustomizableTileEntity)
					player.world.notifyBlockUpdate(pos, te.getBlockState(), te.getBlockState(), 3);
			}
		});

		ctx.get().setPacketHandled(true);
	}
}
