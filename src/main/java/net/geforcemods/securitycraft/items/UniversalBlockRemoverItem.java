package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.LinkableBlockEntity;
import net.geforcemods.securitycraft.api.LinkedAction;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.blockentities.InventoryScannerBlockEntity;
import net.geforcemods.securitycraft.blocks.CageTrapBlock;
import net.geforcemods.securitycraft.blocks.DisguisableBlock;
import net.geforcemods.securitycraft.blocks.InventoryScannerBlock;
import net.geforcemods.securitycraft.blocks.LaserBlock;
import net.geforcemods.securitycraft.blocks.OwnableBlock;
import net.geforcemods.securitycraft.blocks.SpecialDoorBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedDoorBlock;
import net.geforcemods.securitycraft.util.IBlockMine;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class UniversalBlockRemoverItem extends Item {
	public UniversalBlockRemoverItem(Properties properties) {
		super(properties);
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext ctx) {
		World world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		TileEntity tileEntity = world.getBlockEntity(pos);
		PlayerEntity player = ctx.getPlayer();

		if (tileEntity != null && isOwnableBlock(block, tileEntity)) {
			if (!((IOwnable) tileEntity).getOwner().isOwner(player)) {
				if (!(block instanceof IBlockMine) && (!(tileEntity.getBlockState().getBlock() instanceof DisguisableBlock) || (((BlockItem) ((DisguisableBlock) tileEntity.getBlockState().getBlock()).getDisguisedStack(world, pos).getItem()).getBlock() instanceof DisguisableBlock)))
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.UNIVERSAL_BLOCK_REMOVER.get().getDescriptionId()), Utils.localize("messages.securitycraft:notOwned", PlayerUtils.getOwnerComponent(((IOwnable) tileEntity).getOwner().getName())), TextFormatting.RED);

				return ActionResultType.FAIL;
			}

			if (tileEntity instanceof IModuleInventory)
				((IModuleInventory) tileEntity).dropAllModules();

			if (block == SCContent.LASER_BLOCK.get()) {
				LinkableBlockEntity te = (LinkableBlockEntity) world.getBlockEntity(pos);

				for (ItemStack module : te.getInventory()) {
					if (!module.isEmpty()) {
						te.createLinkedBlockAction(LinkedAction.MODULE_REMOVED, new Object[] {
								module, ((ModuleItem) module.getItem()).getModuleType()
						}, te);
					}
				}

				if (!world.isClientSide) {
					world.destroyBlock(pos, true);
					LaserBlock.destroyAdjacentLasers(world, pos);
					stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));
				}
			}
			else if (block == SCContent.CAGE_TRAP.get() && world.getBlockState(pos).getValue(CageTrapBlock.DEACTIVATED)) {
				BlockPos originalPos = pos;
				BlockPos middlePos = originalPos.above(4);

				if (!world.isClientSide) {
					new CageTrapBlock.BlockModifier(world, new BlockPos.Mutable().set(originalPos), ((IOwnable) tileEntity).getOwner()).loop((w, p, o) -> {
						TileEntity te = w.getBlockEntity(p);

						if (te instanceof IOwnable && ((IOwnable) te).getOwner().owns((IOwnable) te)) {
							Block b = w.getBlockState(p).getBlock();

							if (b == SCContent.REINFORCED_IRON_BARS.get() || (p.equals(middlePos) && b == SCContent.HORIZONTAL_REINFORCED_IRON_BARS.get()))
								w.destroyBlock(p, false);
						}
					});

					world.destroyBlock(originalPos, true);
					stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));
				}
			}
			else {
				if ((block instanceof ReinforcedDoorBlock || block instanceof SpecialDoorBlock) && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
					pos = pos.below();

				if (block == SCContent.INVENTORY_SCANNER.get()) {
					InventoryScannerBlockEntity te = InventoryScannerBlock.getConnectedInventoryScanner(world, pos);

					if (te != null)
						te.getInventory().clear();
				}

				if (!world.isClientSide) {
					world.destroyBlock(pos, true); //this also removes the BlockEntity
					block.destroy(world, pos, state);
					stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));
				}
			}

			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	private static boolean isOwnableBlock(Block block, TileEntity te) {
		return (te instanceof OwnableBlockEntity || te instanceof IOwnable || block instanceof OwnableBlock);
	}
}
