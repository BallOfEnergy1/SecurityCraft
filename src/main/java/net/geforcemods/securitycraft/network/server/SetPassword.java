package net.geforcemods.securitycraft.network.server;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.blockentities.KeypadChestBlockEntity;
import net.geforcemods.securitycraft.blocks.KeypadChestBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class SetPassword {

	private String password;
	private int x, y, z;

	public SetPassword(){

	}

	public SetPassword(int x, int y, int z, String code){
		this.x = x;
		this.y = y;
		this.z = z;
		password = code;
	}

	public static void encode(SetPassword message, FriendlyByteBuf buf)
	{
		buf.writeInt(message.x);
		buf.writeInt(message.y);
		buf.writeInt(message.z);
		buf.writeUtf(message.password);
	}

	public static SetPassword decode(FriendlyByteBuf buf)
	{
		SetPassword message = new SetPassword();

		message.x = buf.readInt();
		message.y = buf.readInt();
		message.z = buf.readInt();
		message.password = buf.readUtf(Integer.MAX_VALUE / 4);
		return message;
	}

	public static void onMessage(SetPassword message, Supplier<NetworkEvent.Context> ctx)
	{
		ctx.get().enqueueWork(() -> {
			BlockPos pos = new BlockPos(message.x, message.y, message.z);
			String password = message.password;
			Player player = ctx.get().getSender();
			Level level = player.level;

			if(level.getBlockEntity(pos) instanceof IPasswordProtected be && (!(be instanceof IOwnable ownable) || ownable.getOwner().isOwner(player))){
				be.setPassword(password);

				if(be instanceof KeypadChestBlockEntity chestTe)
					checkAndUpdateAdjacentChest(chestTe, level, pos, password, player);
			}
		});

		ctx.get().setPacketHandled(true);
	}

	private static void checkAndUpdateAdjacentChest(KeypadChestBlockEntity te, Level world, BlockPos pos, String codeToSet, Player player) {
		if(te.getBlockState().getValue(KeypadChestBlock.TYPE) != ChestType.SINGLE)
		{
			BlockPos offsetPos = pos.relative(KeypadChestBlock.getConnectedDirection(te.getBlockState()));
			BlockEntity otherTe = world.getBlockEntity(offsetPos);

			if(otherTe instanceof KeypadChestBlockEntity chestTe && te.getOwner().owns(chestTe))
			{
				chestTe.setPassword(codeToSet);
				world.sendBlockUpdated(offsetPos, otherTe.getBlockState(), otherTe.getBlockState(), 2);
			}
		}
	}
}
