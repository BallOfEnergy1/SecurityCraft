package net.geforcemods.securitycraft.util;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.network.server.SyncTENBTTag;
import net.geforcemods.securitycraft.network.server.UpdateNBTTagOnServer;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class ClientUtils {
	/**
	 * Returns the current Minecraft in-game time, in a 12-hour AM/PM format.
	 */
	public static String getFormattedMinecraftTime() {
		Long time = Minecraft.getMinecraft().world.provider.getWorldTime();

		int hours24 = (int) ((float) time.longValue() / 1000L + 6L) % 24;
		int hours = hours24 % 12;
		int minutes = (int) (time.longValue() / 16.666666F % 60.0F);

		return String.format("%02d:%02d %s", Integer.valueOf(hours < 1 ? 12 : hours), Integer.valueOf(minutes), hours24 < 12 ? "AM" : "PM");
	}

	/**
	 * Sends the client-side NBTTagCompound of a block's TileEntity to the server.
	 */
	public static void syncTileEntity(TileEntity tileEntity) {
		NBTTagCompound tag = new NBTTagCompound();
		tileEntity.writeToNBT(tag);
		SecurityCraft.network.sendToServer(new SyncTENBTTag(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ(), tag));
	}

	/**
	 * Sends the client-side NBTTagCompound of a player's held item to the server.
	 */
	public static void syncItemNBT(ItemStack item) {
		SecurityCraft.network.sendToServer(new UpdateNBTTagOnServer(item));
	}

	public static Quaternion fromXYZDegrees(Vector3f degreesVector) {
		return fromXYZ((float) Math.toRadians(degreesVector.getX()), (float) Math.toRadians(degreesVector.getY()), (float) Math.toRadians(degreesVector.getZ()));
	}

	public static Quaternion fromXYZ(float x, float y, float z) {
		Quaternion quaternion = new Quaternion();

		Quaternion.mul(new Quaternion((float) Math.sin(x / 2.0F), 0.0F, 0.0F, (float) Math.cos(x / 2.0F)), quaternion, quaternion);
		Quaternion.mul(new Quaternion(0.0F, (float) Math.sin(y / 2.0F), 0.0F, (float) Math.cos(y / 2.0F)), quaternion, quaternion);
		Quaternion.mul(new Quaternion(0.0F, 0.0F, (float) Math.sin(z / 2.0F), (float) Math.cos(z / 2.0F)), quaternion, quaternion);
		return quaternion;
	}
}