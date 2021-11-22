package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.LinkableTileEntity;
import net.geforcemods.securitycraft.api.LinkedAction;
import net.geforcemods.securitycraft.api.OwnableTileEntity;
import net.geforcemods.securitycraft.blocks.CageTrapBlock;
import net.geforcemods.securitycraft.blocks.DisguisableBlock;
import net.geforcemods.securitycraft.blocks.InventoryScannerBlock;
import net.geforcemods.securitycraft.blocks.LaserBlock;
import net.geforcemods.securitycraft.blocks.OwnableBlock;
import net.geforcemods.securitycraft.blocks.SpecialDoorBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedDoorBlock;
import net.geforcemods.securitycraft.tileentity.InventoryScannerTileEntity;
import net.geforcemods.securitycraft.tileentity.KeypadChestTileEntity;
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

public class UniversalBlockRemoverItem extends Item
{
	public UniversalBlockRemoverItem(Properties properties)
	{
		super(properties);
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext ctx)
	{
		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		BlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		TileEntity tileEntity = world.getTileEntity(pos);
		PlayerEntity player = ctx.getPlayer();

		if(tileEntity != null && isOwnableBlock(block, tileEntity))
		{
			if(!((IOwnable) tileEntity).getOwner().isOwner(player))
			{
				if(!(block instanceof IBlockMine) && (!(tileEntity.getBlockState().getBlock() instanceof DisguisableBlock) || (((BlockItem)((DisguisableBlock)tileEntity.getBlockState().getBlock()).getDisguisedStack(world, pos).getItem()).getBlock() instanceof DisguisableBlock)))
					PlayerUtils.sendMessageToPlayer(player, Utils.localize(SCContent.UNIVERSAL_BLOCK_REMOVER.get().getTranslationKey()), Utils.localize("messages.securitycraft:notOwned", PlayerUtils.getOwnerComponent(((IOwnable) tileEntity).getOwner().getName())), TextFormatting.RED);

				return ActionResultType.FAIL;
			}

			if(tileEntity instanceof IModuleInventory)
			{
				boolean isChest = tileEntity instanceof KeypadChestTileEntity;

				for(ItemStack module : ((IModuleInventory)tileEntity).getInventory())
				{
					if(isChest)
						((KeypadChestTileEntity)tileEntity).addOrRemoveModuleFromAttached(module, true);

					Block.spawnAsEntity(world, pos, module);
				}
			}

			if(block == SCContent.LASER_BLOCK.get())
			{
				LinkableTileEntity te = (LinkableTileEntity)world.getTileEntity(pos);

				for(ItemStack module : te.getInventory())
				{
					if(!module.isEmpty())
						te.createLinkedBlockAction(LinkedAction.MODULE_REMOVED, new Object[] {module, ((ModuleItem)module.getItem()).getModuleType()}, te);
				}

				if (!world.isRemote) {
					world.destroyBlock(pos, true);
					LaserBlock.destroyAdjacentLasers(world, pos);
					stack.damageItem(1, player, p -> p.sendBreakAnimation(ctx.getHand()));
				}
			}
			else if(block == SCContent.CAGE_TRAP.get() && world.getBlockState(pos).get(CageTrapBlock.DEACTIVATED))
			{
				BlockPos originalPos = pos;
				BlockPos middlePos = originalPos.up(4);

				if (!world.isRemote) {
					new CageTrapBlock.BlockModifier(world, new BlockPos.Mutable(originalPos), ((IOwnable)tileEntity).getOwner()).loop((w, p, o) -> {
						TileEntity te = w.getTileEntity(p);

						if(te instanceof IOwnable && ((IOwnable)te).getOwner().owns((IOwnable)te))
						{
							Block b = w.getBlockState(p).getBlock();

							if(b == SCContent.REINFORCED_IRON_BARS.get() || (p.equals(middlePos) && b == SCContent.HORIZONTAL_REINFORCED_IRON_BARS.get()))
								w.destroyBlock(p, false);
						}
					});

					world.destroyBlock(originalPos, false);
					stack.damageItem(1, player, p -> p.sendBreakAnimation(ctx.getHand()));
				}
			}
			else
			{
				if((block instanceof ReinforcedDoorBlock || block instanceof SpecialDoorBlock) && state.get(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
					pos = pos.down();

				if(block == SCContent.INVENTORY_SCANNER.get())
				{
					InventoryScannerTileEntity te = InventoryScannerBlock.getConnectedInventoryScanner(world, pos);

					if(te != null)
						te.getInventory().clear();
				}

				if (!world.isRemote) {
					world.destroyBlock(pos, true);
					world.removeTileEntity(pos);
					stack.damageItem(1, player, p -> p.sendBreakAnimation(ctx.getHand()));
				}
			}

			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	private static boolean isOwnableBlock(Block block, TileEntity te)
	{
		return (te instanceof OwnableTileEntity || te instanceof IOwnable || block instanceof OwnableBlock);
	}
}
