package net.geforcemods.securitycraft.blocks.mines;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IExplosive;
import net.geforcemods.securitycraft.blocks.BlockOwnable;
import net.geforcemods.securitycraft.util.EntityUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockExplosive extends BlockOwnable implements IExplosive {
	public BlockExplosive(Material material) {
		super(material);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (PlayerUtils.isHoldingItem(player, SCContent.remoteAccessMine, hand) || PlayerUtils.isHoldingItem(player, SCContent.universalOwnerChanger, hand))
			return false;

		if (isActive(world, pos) && isDefusable() && PlayerUtils.isHoldingItem(player, SCContent.wireCutters, hand)) {
			if (defuseMine(world, pos)) {
				if (!player.isCreative())
					player.getHeldItem(hand).damageItem(1, player);

				world.playSound(null, pos, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}

			return true;
		}

		if (!isActive(world, pos) && PlayerUtils.isHoldingItem(player, Items.FLINT_AND_STEEL, hand)) {
			if (activateMine(world, pos)) {
				if (!player.isCreative())
					player.getHeldItem(hand).damageItem(1, player);

				world.playSound(null, pos, SoundEvents.BLOCK_TRIPWIRE_CLICK_ON, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}

			return true;
		}

		if (explodesWhenInteractedWith() && isActive(world, pos) && !EntityUtils.doesPlayerOwn(player, world, pos)) {
			explode(world, pos);
			return true;
		}

		return false;
	}

	/**
	 * @return If the mine should explode when right-clicked?
	 */
	public boolean explodesWhenInteractedWith() {
		return true;
	}

	@Override
	public boolean isDefusable() {
		return true;
	}
}
