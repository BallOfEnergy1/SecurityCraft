package net.geforcemods.securitycraft.network.server;

import java.util.function.Supplier;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.items.CameraMonitorItem;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class RemoveCameraTag {
	private ItemStack heldItem;
	private int camID;

	public RemoveCameraTag() {}

	public RemoveCameraTag(ItemStack stack, int cid) {
		heldItem = stack;
		camID = cid;
	}

	public static void encode(RemoveCameraTag message, FriendlyByteBuf buf) {
		buf.writeItem(message.heldItem);
		buf.writeInt(message.camID);
	}

	public static RemoveCameraTag decode(FriendlyByteBuf buf) {
		RemoveCameraTag message = new RemoveCameraTag();

		message.heldItem = buf.readItem();
		message.camID = buf.readInt();
		return message;
	}

	public static void onMessage(RemoveCameraTag message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ItemStack monitor = PlayerUtils.getSelectedItemStack(ctx.get().getSender().getInventory(), SCContent.CAMERA_MONITOR.get());

			if (!monitor.isEmpty())
				monitor.getTag().remove(CameraMonitorItem.getTagNameFromPosition(monitor.getTag(), ((CameraMonitorItem) monitor.getItem()).getCameraPositions(monitor.getTag()).get(message.camID - 1)));
		});

		ctx.get().setPacketHandled(true);
	}
}
