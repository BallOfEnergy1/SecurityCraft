package net.geforcemods.securitycraft.items;

import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.entity.Sentry;
import net.geforcemods.securitycraft.network.client.OpenSRATGui;
import net.geforcemods.securitycraft.network.client.UpdateNBTTagOnClient;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;

public class SentryRemoteAccessToolItem extends Item {
	public SentryRemoteAccessToolItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getItemInHand(hand);

		if (!world.isClientSide)
			SecurityCraft.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new OpenSRATGui((player.getServer().getPlayerList().getViewDistance() - 1) * 16));

		return ActionResult.consume(stack);
	}

	@Override
	public ActionResultType useOn(ItemUseContext ctx) {
		return onItemUse(ctx.getPlayer(), ctx.getLevel(), ctx.getClickedPos(), ctx.getItemInHand(), ctx.getClickedFace(), ctx.getClickLocation().x, ctx.getClickLocation().y, ctx.getClickLocation().z);
	}

	public ActionResultType onItemUse(PlayerEntity player, World world, BlockPos pos, ItemStack stack, Direction facing, double hitX, double hitY, double hitZ) {
		List<Sentry> sentries = world.getEntitiesOfClass(Sentry.class, new AxisAlignedBB(pos));

		if (!sentries.isEmpty()) {
			Sentry sentry = sentries.get(0);
			BlockPos pos2 = sentry.blockPosition();

			if (!isSentryAdded(stack, pos2)) {
				int availSlot = getNextAvaliableSlot(stack);

				if (availSlot == 0) {
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.REMOTE_ACCESS_SENTRY.get().getDescriptionId()), Utils.localize("messages.securitycraft:srat.noSlots"), TextFormatting.RED);
					return ActionResultType.FAIL;
				}

				if (!sentry.getOwner().isOwner(player)) {
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.REMOTE_ACCESS_SENTRY.get().getDescriptionId()), Utils.localize("messages.securitycraft:srat.cantBind"), TextFormatting.RED);
					return ActionResultType.FAIL;
				}

				if (stack.getTag() == null)
					stack.setTag(new CompoundNBT());

				stack.getTag().putIntArray(("sentry" + availSlot), BlockUtils.posToIntArray(pos2));

				if (!world.isClientSide)
					SecurityCraft.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new UpdateNBTTagOnClient(stack));

				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.REMOTE_ACCESS_SENTRY.get().getDescriptionId()), Utils.localize("messages.securitycraft:srat.bound", pos2), TextFormatting.GREEN);
			}
			else {
				removeTagFromItemAndUpdate(stack, pos2, player);
				PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.REMOTE_ACCESS_SENTRY.get().getDescriptionId()), Utils.localize("messages.securitycraft:srat.unbound", pos2), TextFormatting.RED);
			}

			return ActionResultType.SUCCESS;
		}
		else if (!world.isClientSide)
			SecurityCraft.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new OpenSRATGui((player.getServer().getPlayerList().getViewDistance() - 1) * 16));

		return ActionResultType.SUCCESS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		if (stack.getTag() == null)
			return;

		for (int i = 1; i <= 12; i++) {
			if (stack.getTag().getIntArray("sentry" + i).length > 0) {
				int[] coords = stack.getTag().getIntArray("sentry" + i);

				if (coords[0] == 0 && coords[1] == 0 && coords[2] == 0) {
					tooltip.add(new StringTextComponent(TextFormatting.GRAY + "---"));
					continue;
				}
				else {
					BlockPos pos = new BlockPos(coords[0], coords[1], coords[2]);
					List<Sentry> sentries = Minecraft.getInstance().player.level.getEntitiesOfClass(Sentry.class, new AxisAlignedBB(pos));
					String nameToShow;

					if (!sentries.isEmpty() && sentries.get(0).hasCustomName())
						nameToShow = sentries.get(0).getCustomName().getString();
					else
						nameToShow = Utils.localize("tooltip.securitycraft:sentry").getString() + " " + i;

					tooltip.add(new StringTextComponent(TextFormatting.GRAY + nameToShow + ": " + Utils.getFormattedCoordinates(pos).getString()));
				}
			}
			else
				tooltip.add(new StringTextComponent(TextFormatting.GRAY + "---"));
		}
	}

	private void removeTagFromItemAndUpdate(ItemStack stack, BlockPos pos, PlayerEntity player) {
		if (stack.getTag() == null)
			return;

		for (int i = 1; i <= 12; i++) {
			if (stack.getTag().getIntArray("sentry" + i).length > 0) {
				int[] coords = stack.getTag().getIntArray("sentry" + i);

				if (coords[0] == pos.getX() && coords[1] == pos.getY() && coords[2] == pos.getZ()) {
					stack.getTag().putIntArray("sentry" + i, new int[] {
							0, 0, 0
					});

					if (!player.level.isClientSide)
						SecurityCraft.channel.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), new UpdateNBTTagOnClient(stack));

					return;
				}
			}
		}

		return;
	}

	private boolean isSentryAdded(ItemStack stack, BlockPos pos) {
		if (stack.getTag() == null)
			return false;

		for (int i = 1; i <= 12; i++) {
			if (stack.getTag().getIntArray("sentry" + i).length > 0) {
				int[] coords = stack.getTag().getIntArray("sentry" + i);

				if (coords[0] == pos.getX() && coords[1] == pos.getY() && coords[2] == pos.getZ())
					return true;
			}
		}

		return false;
	}

	private int getNextAvaliableSlot(ItemStack stack) {
		for (int i = 1; i <= 12; i++) {
			if (stack.getTag() == null)
				return 1;

			int[] pos = stack.getTag().getIntArray("sentry" + i);

			if (pos.length == 0 || (pos[0] == 0 && pos[1] == 0 && pos[2] == 0))
				return i;
		}

		return 0;
	}
}
