package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class BlockReinforcedOldLog extends BlockReinforcedLog implements IReinforcedBlock
{
	public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class, type -> type.getMetadata() < 4);

	public BlockReinforcedOldLog()
	{
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, EnumType.OAK).withProperty(LOG_AXIS, BlockLog.EnumAxis.Y));
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		list.add(new ItemStack(this, 1, EnumType.OAK.getMetadata()));
		list.add(new ItemStack(this, 1, EnumType.SPRUCE.getMetadata()));
		list.add(new ItemStack(this, 1, EnumType.BIRCH.getMetadata()));
		list.add(new ItemStack(this, 1, EnumType.JUNGLE.getMetadata()));
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		IBlockState state = getDefaultState().withProperty(VARIANT, EnumType.byMetadata((meta & 3) % 4));

		switch (meta & 12)
		{
			case 0:
				state = state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Y);
				break;
			case 4:
				state = state.withProperty(LOG_AXIS, BlockLog.EnumAxis.X);
				break;
			case 8:
				state = state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Z);
				break;
			default:
				state = state.withProperty(LOG_AXIS, BlockLog.EnumAxis.NONE);
		}

		return state;
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state)
	{
		byte b0 = 0;
		int meta = b0 | state.getValue(VARIANT).getMetadata();

		switch (BlockReinforcedOldLog.SwitchEnumAxis.AXIS_LOOKUP[state.getValue(LOG_AXIS).ordinal()])
		{
			case 1:
				meta |= 4;
				break;
			case 2:
				meta |= 8;
				break;
			case 3:
				meta |= 12;
		}

		return meta;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, VARIANT, LOG_AXIS);
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	@Override
	public int damageDropped(IBlockState state)
	{
		return state.getValue(VARIANT).getMetadata();
	}

	@Override
	public List<Block> getVanillaBlocks()
	{
		return Arrays.asList(Blocks.LOG);
	}

	@Override
	public int getAmount()
	{
		return 12;
	}

	static final class SwitchEnumAxis
	{
		static final int[] AXIS_LOOKUP = new int[BlockLog.EnumAxis.values().length];

		static
		{
			try
			{
				AXIS_LOOKUP[BlockLog.EnumAxis.X.ordinal()] = 1;
			}
			catch (NoSuchFieldError e)
			{

			}

			try
			{
				AXIS_LOOKUP[BlockLog.EnumAxis.Z.ordinal()] = 2;
			}
			catch (NoSuchFieldError e)
			{

			}

			try
			{
				AXIS_LOOKUP[BlockLog.EnumAxis.NONE.ordinal()] = 3;
			}
			catch (NoSuchFieldError e)
			{

			}
		}
	}
}