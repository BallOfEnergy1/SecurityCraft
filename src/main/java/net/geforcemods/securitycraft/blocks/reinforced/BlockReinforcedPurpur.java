package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Arrays;
import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blocks.BlockOwnable;
import net.geforcemods.securitycraft.compat.IOverlayDisplay;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockReinforcedPurpur extends BlockOwnable implements IOverlayDisplay, IReinforcedBlock
{
	public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.<EnumType>create("variant", EnumType.class);

	public BlockReinforcedPurpur()
	{
		super(Material.ROCK);
		setDefaultState(blockState.getBaseState().withProperty(VARIANT, EnumType.DEFAULT));
	}

	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
	 * IBlockstate
	 */
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		if (meta == EnumType.LINES_Y.getMetadata())
			switch (facing.getAxis())
			{
				case Z:
					return getDefaultState().withProperty(VARIANT, EnumType.LINES_Z);
				case X:
					return getDefaultState().withProperty(VARIANT, EnumType.LINES_X);
				case Y:
					return getDefaultState().withProperty(VARIANT, EnumType.LINES_Y);
			}

		return getDefaultState().withProperty(VARIANT, EnumType.DEFAULT);
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	@Override
	public int damageDropped(IBlockState state)
	{
		EnumType type = state.getValue(VARIANT);
		return type != EnumType.LINES_X && type != EnumType.LINES_Z ? type.getMetadata() : EnumType.LINES_Y.getMetadata();
	}

	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	@Override
	public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		list.add(new ItemStack(this, 1, EnumType.DEFAULT.getMetadata()));
		list.add(new ItemStack(this, 1, EnumType.LINES_Y.getMetadata()));
	}

	/**
	 * Get the MapColor for this Block and the given BlockState
	 */
	@Override
	public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return MapColor.MAGENTA;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(VARIANT).getMetadata();
	}

	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot)
	{
		switch (rot)
		{
			case COUNTERCLOCKWISE_90:
			case CLOCKWISE_90:

				switch (state.getValue(VARIANT))
				{
					case LINES_X:
						return state.withProperty(VARIANT, EnumType.LINES_Z);
					case LINES_Z:
						return state.withProperty(VARIANT, EnumType.LINES_X);
					default:
						return state;
				}

			default:
				return state;
		}
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, VARIANT);
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis)
	{
		IBlockState state = world.getBlockState(pos);
		EnumType current = state.getValue(VARIANT);
		EnumType next = current == EnumType.LINES_X ? EnumType.LINES_Y :
			current == EnumType.LINES_Y ? EnumType.LINES_Z :
				current == EnumType.LINES_Z ? EnumType.LINES_X : current;
		if (next == current)
			return false;
		world.setBlockState(pos, state.withProperty(VARIANT, next));
		return true;
	}

	@Override
	public ItemStack getDisplayStack(World world, IBlockState state, BlockPos pos)
	{
		return new ItemStack(Item.getItemFromBlock(SCContent.reinforcedPurpur), 1, BlockUtils.getBlockMeta(world, pos) != 0 ? 1 : 0);
	}

	@Override
	public boolean shouldShowSCInfo(World world, IBlockState state, BlockPos pos)
	{
		return true;
	}

	@Override
	public List<Block> getVanillaBlocks()
	{
		return Arrays.asList(Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR);
	}

	@Override
	public int getAmount()
	{
		return 2;
	}

	public static enum EnumType implements IStringSerializable
	{
		DEFAULT(0, "default", "default"),
		LINES_Y(1, "lines_y", "lines"),
		LINES_X(2, "lines_x", "lines"),
		LINES_Z(3, "lines_z", "lines");

		private static final EnumType[] META_LOOKUP = new EnumType[values().length];
		private final int meta;
		private final String serializedName;
		private final String unlocalizedName;

		private EnumType(int meta, String name, String unlocalizedName)
		{
			this.meta = meta;
			serializedName = name;
			this.unlocalizedName = unlocalizedName;
		}

		public int getMetadata()
		{
			return meta;
		}

		@Override
		public String toString()
		{
			return unlocalizedName;
		}

		public static EnumType byMetadata(int meta)
		{
			if (meta < 0 || meta >= META_LOOKUP.length)
				meta = 0;

			return META_LOOKUP[meta];
		}

		@Override
		public String getName()
		{
			return serializedName;
		}

		static
		{
			for (EnumType type : values())
				META_LOOKUP[type.getMetadata()] = type;
		}
	}
}