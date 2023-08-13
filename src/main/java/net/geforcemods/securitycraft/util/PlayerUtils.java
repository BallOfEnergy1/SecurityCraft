package net.geforcemods.securitycraft.util;

import java.util.Iterator;
import java.util.List;

import com.mojang.authlib.GameProfile;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.Owner;
import net.geforcemods.securitycraft.compat.ftbutilities.FTBUtilitiesCompat;
import net.geforcemods.securitycraft.compat.ftbutilities.TeamRepresentation;
import net.geforcemods.securitycraft.entity.camera.SecurityCamera;
import net.geforcemods.securitycraft.network.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.Side;

public class PlayerUtils {
	private PlayerUtils() {}

	/**
	 * Gets the EntityPlayer instance of a player (if they're online) using their name. <p> Args: playerName.
	 */
	public static EntityPlayer getPlayerFromName(String name) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			List<?> players = Minecraft.getMinecraft().world.playerEntities;
			Iterator<?> iterator = players.iterator();

			while (iterator.hasNext()) {
				EntityPlayer tempPlayer = (EntityPlayer) iterator.next();

				if (tempPlayer.getName().equals(name))
					return tempPlayer;
			}

			return null;
		}
		else {
			List<?> players = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers();
			Iterator<?> iterator = players.iterator();

			while (iterator.hasNext()) {
				EntityPlayer tempPlayer = (EntityPlayer) iterator.next();

				if (tempPlayer.getName().equals(name))
					return tempPlayer;
			}

			return null;
		}
	}

	/**
	 * Returns true if a player with the given name is in the world. Args: playerName.
	 */
	public static boolean isPlayerOnline(String name) {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			for (int i = 0; i < Minecraft.getMinecraft().world.playerEntities.size(); i++) {
				EntityPlayer player = Minecraft.getMinecraft().world.playerEntities.get(i);

				if (player != null && player.getName().equals(name))
					return true;
			}

			return false;
		}
		else
			return (FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(name) != null);
	}

	public static void sendMessageToPlayer(EntityPlayer player, ITextComponent prefix, ITextComponent text, TextFormatting color) {
		sendMessageToPlayer(player, prefix, text, color, false);
	}

	public static void sendMessageToPlayer(EntityPlayer player, ITextComponent prefix, ITextComponent text, TextFormatting color, boolean shouldSendFromClient) {
		if (player.world.isRemote == shouldSendFromClient) {
			//@formatter:off
			player.sendMessage(new TextComponentString("[")
					.appendSibling(prefix.setStyle(new Style().setColor(color)))
					.appendSibling(new TextComponentString(TextFormatting.WHITE + "] "))
					.appendSibling(text));
			//@formatter:on
		}
	}

	/**
	 * Sends the given {@link ICommandSender} a chat message, followed by a link prefixed with a colon. <p> Args: sender, prefix,
	 * text, link, color.
	 */
	public static void sendMessageEndingWithLink(ICommandSender sender, ITextComponent prefix, ITextComponent text, String link, TextFormatting color) {
		//@formatter:off
		sender.sendMessage(new TextComponentString("[")
				.appendSibling(prefix.setStyle(new Style().setColor(color)))
				.appendSibling(new TextComponentString(TextFormatting.WHITE + "] "))
				.appendSibling(text)
				.appendSibling(new TextComponentString(": "))
				.appendSibling(ForgeHooks.newChatWithLinks(link)));
		//@formatter:on
	}

	/**
	 * Returns the ItemStack of the given item the player if they are currently holding it (both hands are checked).
	 *
	 * @param player The player to check
	 * @param item The item type that should be searched for
	 * @return The ItemStack whose item matches the given item, {@link ItemStack#EMPTY} if the player is not holding the item
	 */
	public static ItemStack getItemStackFromAnyHand(EntityPlayer player, Item item) {
		if (player.inventory.getCurrentItem().getItem() == item)
			return player.inventory.getCurrentItem();

		if (player.inventory.offHandInventory.get(0).getItem() == item)
			return player.inventory.offHandInventory.get(0);

		return ItemStack.EMPTY;
	}

	/**
	 * Is the entity mounted on to a security camera? Args: entity.
	 */
	public static boolean isPlayerMountedOnCamera(EntityLivingBase entity) {
		if (!(entity instanceof EntityPlayer))
			return false;

		EntityPlayer player = (EntityPlayer) entity;

		if (player.world.isRemote)
			return ClientProxy.isPlayerMountedOnCamera();
		else
			return ((EntityPlayerMP) player).getSpectatingEntity() instanceof SecurityCamera;
	}

	/**
	 * Checks if two given players are on the same scoreboard/FTB Teams team
	 *
	 * @param owner1 The first owner object representing a player
	 * @param owner2 The second owner object representing a player
	 * @return true if both players are on the same team, false otherwise
	 */
	public static boolean areOnSameTeam(Owner owner1, Owner owner2) {
		if (owner1.equals(owner2))
			return true;

		if (Loader.isModLoaded("ftbutilities"))
			return FTBUtilitiesCompat.areOnSameTeam(owner1, owner2);

		ScorePlayerTeam team = getPlayersVanillaTeam(owner1.getName());

		return team != null && team.getMembershipCollection().contains(owner2.getName());
	}

	/**
	 * Gets the scoreboard team the given player is on
	 *
	 * @param playerName The player whose team to get
	 * @return The team the given player is on. null if the player is not part of a team
	 */
	public static ScorePlayerTeam getPlayersVanillaTeam(String playerName) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

		if (server != null)
			return server.getEntityWorld().getScoreboard().getPlayersTeam(playerName);
		else
			return SecurityCraft.proxy.getClientPlayer().world.getScoreboard().getPlayersTeam(playerName);
	}

	/**
	 * Gets the component to use for displaying a block's owner. If team ownership is enabled and the given player is on a team,
	 * this will return the colored team name.
	 *
	 * @param owner The player who owns the block
	 * @return The component to display
	 */
	public static ITextComponent getOwnerComponent(Owner owner) {
		if (ConfigHandler.enableTeamOwnership) {
			TeamRepresentation teamRepresentation = TeamRepresentation.get(owner);

			if (teamRepresentation != null)
				return Utils.localize("messages.securitycraft:teamOwner", new TextComponentString(teamRepresentation.name()).setStyle(new Style().setColor(teamRepresentation.color()))).setStyle(new Style().setColor(TextFormatting.GRAY));
		}

		return new TextComponentString(owner.getName());
	}

	/**
	 * Retrieves the name of the player head the given player may be wearing
	 *
	 * @param player The player to check
	 * @return The name of the skull owner, null if the player is not wearing a player head or the skull owner is faulty
	 */
	public static Owner getSkullOwner(EntityPlayer player) {
		ItemStack stack = player.getItemStackFromSlot(EntityEquipmentSlot.HEAD);

		if (stack.getItem() == Items.SKULL && stack.hasTagCompound()) {
			NBTTagCompound stackTag = stack.getTagCompound();

			if (stackTag.hasKey("SkullOwner", Constants.NBT.TAG_STRING))
				return new Owner(stackTag.getString("SkullOwner"), "ownerUUID");
			else if (stackTag.hasKey("SkullOwner", Constants.NBT.TAG_COMPOUND)) {
				GameProfile profile = NBTUtil.readGameProfileFromNBT(stackTag.getCompoundTag("SkullOwner"));
				String name = "ownerName";
				String uuid = "ownerUUID";

				if (profile.getName() != null)
					name = profile.getName();

				if (profile.getId() != null)
					uuid = profile.getId().toString();

				return new Owner(name, uuid);
			}
		}

		return null;
	}
}
