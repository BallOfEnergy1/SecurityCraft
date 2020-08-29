package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Arrays;
import java.util.List;

import net.geforcemods.securitycraft.misc.EnumModuleType;
import net.geforcemods.securitycraft.tileentity.TileEntityWhitelistOnly;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockButton;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockReinforcedButton extends BlockButton implements IReinforcedBlock
{
	private final boolean isWooden;

	public BlockReinforcedButton(boolean isWooden)
	{
		super(isWooden);

		this.isWooden = isWooden;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if(isAllowedToPress(world, pos, (TileEntityWhitelistOnly)world.getTileEntity(pos), player))
			return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
		return false;
	}

	public boolean isAllowedToPress(World world, BlockPos pos, TileEntityWhitelistOnly te, EntityPlayer entity)
	{
		return te.getOwner().isOwner(entity) || ModuleUtils.getPlayersFromModule(world, pos, EnumModuleType.WHITELIST).contains(entity.getName().toLowerCase());
	}

	@Override
	protected void playClickSound(EntityPlayer player, World world, BlockPos pos)
	{
		world.playSound(player, pos, isWooden ? SoundEvents.BLOCK_WOOD_BUTTON_CLICK_ON : SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
	}

	@Override
	protected void playReleaseSound(World world, BlockPos pos)
	{
		world.playSound(null, pos, isWooden ? SoundEvents.BLOCK_WOOD_BUTTON_CLICK_OFF : SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileEntityWhitelistOnly();
	}

	@Override
	public int getAmount()
	{
		return 1;
	}

	@Override
	public List<Block> getVanillaBlocks()
	{
		return Arrays.asList(isWooden ? Blocks.WOODEN_BUTTON : Blocks.STONE_BUTTON);
	}
}
