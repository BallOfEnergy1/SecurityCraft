package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.entity.SentryEntity;
import net.geforcemods.securitycraft.entity.SentryEntity.SentryMode;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class SentryItem extends Item
{
	public SentryItem(Item.Properties properties)
	{
		super(properties);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext ctx)
	{
		return onItemUse(ctx.getPlayer(), ctx.getWorld(), ctx.getPos(), ctx.getItem(), ctx.getFace(), ctx.getHitVec().x, ctx.getHitVec().y, ctx.getHitVec().z);
	}

	public ActionResultType onItemUse(PlayerEntity player, World world, BlockPos pos, ItemStack stack, Direction facing, double hitX, double hitY, double hitZ)
	{
		if(!world.isRemote)
		{
			pos = pos.offset(facing); //get sentry position

			if(!world.isAirBlock(pos))
				return ActionResultType.PASS;
			else if(world.isAirBlock(pos.down()))
			{
				PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize(SCContent.SENTRY.get().getTranslationKey()), ClientUtils.localize("messages.securitycraft:sentry.needsBlockBelow"), TextFormatting.DARK_RED);
				return ActionResultType.FAIL;
			}

			SentryEntity entity = SCContent.eTypeSentry.create(world);

			entity.setupSentry(player);
			entity.setPosition(pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F);
			world.addEntity(entity);
			PlayerUtils.sendMessageToPlayer(player, ClientUtils.localize(SCContent.SENTRY.get().getTranslationKey()), ClientUtils.localize("messages.securitycraft:sentry.mode" + (SentryMode.CAMOUFLAGE_HP.ordinal() % 2)).append(ClientUtils.localize("messages.securitycraft:sentry.descriptionMode" + SentryMode.CAMOUFLAGE_HP.ordinal())), TextFormatting.DARK_RED);

			if(!player.isCreative())
				stack.shrink(1);
		}

		return ActionResultType.SUCCESS;
	}
}
