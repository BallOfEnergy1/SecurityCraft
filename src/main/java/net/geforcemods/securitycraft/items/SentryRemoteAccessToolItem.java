package net.geforcemods.securitycraft.items;

import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.entity.sentry.Sentry;
import net.geforcemods.securitycraft.network.client.OpenScreen;
import net.geforcemods.securitycraft.network.client.OpenScreen.DataType;
import net.geforcemods.securitycraft.network.client.UpdateNBTTagOnClient;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.PacketDistributor;

public class SentryRemoteAccessToolItem extends Item {
	public SentryRemoteAccessToolItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!level.isClientSide)
			SecurityCraft.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new OpenScreen(DataType.SENTRY_REMOTE_ACCESS_TOOL));

		return InteractionResultHolder.consume(stack);
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		Player player = ctx.getPlayer();
		List<Sentry> sentries = level.getEntitiesOfClass(Sentry.class, new AABB(pos));

		if (!sentries.isEmpty()) {
			ItemStack stack = ctx.getItemInHand();
			Sentry sentry = sentries.get(0);
			BlockPos sentryPos = sentry.blockPosition();

			if (!isSentryAdded(stack, sentryPos)) {
				int availSlot = getNextAvailableSlot(stack);

				if (availSlot == 0) {
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.SENTRY_REMOTE_ACCESS_TOOL.get().getDescriptionId()), Utils.localize("messages.securitycraft:srat.noSlots"), ChatFormatting.RED);
					return InteractionResult.FAIL;
				}

				if (!sentry.isOwnedBy(player)) {
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.SENTRY_REMOTE_ACCESS_TOOL.get().getDescriptionId()), Utils.localize("messages.securitycraft:srat.cantBind"), ChatFormatting.RED);
					return InteractionResult.FAIL;
				}

				if (stack.getTag() == null)
					stack.setTag(new CompoundTag());

				stack.getTag().putIntArray(("sentry" + availSlot), BlockUtils.posToIntArray(sentryPos));

				if (!level.isClientSide && !stack.isEmpty())
					SecurityCraft.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new UpdateNBTTagOnClient(stack));

				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.SENTRY_REMOTE_ACCESS_TOOL.get().getDescriptionId()), Utils.localize("messages.securitycraft:srat.bound", sentryPos), ChatFormatting.GREEN);
			}
			else {
				removeTagFromItemAndUpdate(stack, sentryPos, player);
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.SENTRY_REMOTE_ACCESS_TOOL.get().getDescriptionId()), Utils.localize("messages.securitycraft:srat.unbound", sentryPos), ChatFormatting.RED);
			}

			return InteractionResult.SUCCESS;
		}
		else if (!level.isClientSide)
			SecurityCraft.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new OpenScreen(DataType.SENTRY_REMOTE_ACCESS_TOOL));

		return InteractionResult.SUCCESS;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		if (stack.getTag() == null)
			return;

		for (int i = 1; i <= 12; i++) {
			int[] coords = stack.getTag().getIntArray("sentry" + i);

			if (coords.length != 3)
				tooltip.add(Component.literal(ChatFormatting.GRAY + "---"));
			else {
				BlockPos pos = new BlockPos(coords[0], coords[1], coords[2]);
				List<Sentry> sentries = Minecraft.getInstance().player.level().getEntitiesOfClass(Sentry.class, new AABB(pos));
				String nameToShow;

				if (!sentries.isEmpty() && sentries.get(0).hasCustomName())
					nameToShow = sentries.get(0).getCustomName().getString();
				else
					nameToShow = Utils.localize("tooltip.securitycraft:sentry").getString() + " " + i;

				tooltip.add(Component.literal(ChatFormatting.GRAY + nameToShow + ": " + Utils.getFormattedCoordinates(pos).getString()));
			}
		}
	}

	private void removeTagFromItemAndUpdate(ItemStack stack, BlockPos pos, Player player) {
		if (stack.getTag() == null)
			return;

		for (int i = 1; i <= 12; i++) {
			int[] coords = stack.getTag().getIntArray("sentry" + i);

			if (coords.length == 3 && coords[0] == pos.getX() && coords[1] == pos.getY() && coords[2] == pos.getZ()) {
				stack.getTag().remove("sentry" + i);

				if (!player.level().isClientSide && !stack.isEmpty())
					SecurityCraft.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), new UpdateNBTTagOnClient(stack));

				return;
			}
		}
	}

	public static boolean hasSentryAdded(CompoundTag tag) {
		if (tag == null)
			return false;

		for (int i = 1; i <= 12; i++) {
			if (tag.contains("sentry" + i))
				return true;
		}

		return false;
	}

	public static boolean isSentryAdded(ItemStack stack, BlockPos pos) {
		if (stack.getTag() == null)
			return false;

		for (int i = 1; i <= 12; i++) {
			int[] coords = stack.getTag().getIntArray("sentry" + i);

			if (coords.length == 3 && coords[0] == pos.getX() && coords[1] == pos.getY() && coords[2] == pos.getZ())
				return true;
		}

		return false;
	}

	public static int getNextAvailableSlot(ItemStack stack) {
		if (stack.getTag() == null)
			return 1;

		for (int i = 1; i <= 12; i++) {
			if (stack.getTag().getIntArray("sentry" + i).length != 3)
				return i;
		}

		return 0;
	}
}
