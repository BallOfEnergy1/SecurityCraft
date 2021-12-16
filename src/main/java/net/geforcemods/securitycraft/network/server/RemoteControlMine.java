package net.geforcemods.securitycraft.network.server;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.api.IExplosive;
import net.geforcemods.securitycraft.api.IOwnable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class RemoteControlMine{

	private int x, y, z;
	private String state;

	public RemoteControlMine(){

	}

	public RemoteControlMine(int x, int y, int z, String state){
		this.x = x;
		this.y = y;
		this.z = z;
		this.state = state;
	}

	public static void encode(RemoteControlMine message, FriendlyByteBuf buf)
	{
		buf.writeInt(message.x);
		buf.writeInt(message.y);
		buf.writeInt(message.z);
		buf.writeUtf(message.state);
	}

	public static RemoteControlMine decode(FriendlyByteBuf buf)
	{
		RemoteControlMine message = new RemoteControlMine();

		message.x = buf.readInt();
		message.y = buf.readInt();
		message.z = buf.readInt();
		message.state = buf.readUtf(Integer.MAX_VALUE / 4);
		return message;
	}

	public static void onMessage(RemoteControlMine message, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			Level level = player.level;
			BlockPos pos = new BlockPos(message.x, message.y, message.z);
			BlockState state = level.getBlockState(pos);

			if(state.getBlock() instanceof IExplosive explosive)
			{
				if(level.getBlockEntity(pos) instanceof IOwnable be && be.getOwner().isOwner(player))
				{
					if(message.state.equalsIgnoreCase("activate"))
						explosive.activateMine(level,pos);
					else if(message.state.equalsIgnoreCase("defuse"))
						explosive.defuseMine(level, pos);
					else if(message.state.equalsIgnoreCase("detonate"))
						explosive.explode(level, pos);
				}
			}
		});

		ctx.get().setPacketHandled(true);

	}

}
