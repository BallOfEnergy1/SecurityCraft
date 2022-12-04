package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.blocks.DisguisableBlock;
import net.geforcemods.securitycraft.inventory.CustomizeBlockMenu;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class UniversalBlockModifierItem extends Item {
	public UniversalBlockModifierItem(Item.Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext ctx) {
		World world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		TileEntity te = world.getBlockEntity(pos);
		PlayerEntity player = ctx.getPlayer();

		if (te instanceof IModuleInventory) {
			if (te instanceof IOwnable && !((IOwnable) te).isOwnedBy(player)) {
				if (!(te.getBlockState().getBlock() instanceof DisguisableBlock) || (((BlockItem) ((DisguisableBlock) te.getBlockState().getBlock()).getDisguisedStack(world, pos).getItem()).getBlock() instanceof DisguisableBlock))
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.UNIVERSAL_BLOCK_MODIFIER.get().getDescriptionId()), Utils.localize("messages.securitycraft:notOwned", PlayerUtils.getOwnerComponent(((IOwnable) te).getOwner().getName())), TextFormatting.RED);

				return ActionResultType.FAIL;
			}
			else if (!ctx.getLevel().isClientSide) {
				NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
					@Override
					public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
						return new CustomizeBlockMenu(windowId, world, pos, inv);
					}

					@Override
					public ITextComponent getDisplayName() {
						return new TranslationTextComponent(te.getBlockState().getBlock().getDescriptionId());
					}
				}, pos);
			}

			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}
}
