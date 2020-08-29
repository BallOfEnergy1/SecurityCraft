package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IIntersectable;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.TileEntitySCTE;
import net.geforcemods.securitycraft.misc.CustomDamageSources;
import net.geforcemods.securitycraft.misc.EnumModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.EntityUtils;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockLaserField extends BlockContainer implements IIntersectable{

	public static final PropertyInteger BOUNDTYPE = PropertyInteger.create("boundtype", 1, 3);
	private static final AxisAlignedBB BOUNDTYPE_1_AABB, BOUNDTYPE_2_AABB, BOUNDTYPE_3_AABB;

	static {
		float px = 1.0F / 16.0F;

		BOUNDTYPE_1_AABB = new AxisAlignedBB(6.75 * px, 0.0F, 6.75 * px, 9.25 * px, 1.0F, 9.25 * px);
		BOUNDTYPE_2_AABB = new AxisAlignedBB(6.75 * px, 6.75 * px, 0.0F, 9.25 * px, 9.25 * px, 1.0F);
		BOUNDTYPE_3_AABB = new AxisAlignedBB(0.0F, 6.75 * px, 6.75 * px, 1.0F, 9.25 * px, 9.25 * px);
	}

	public BlockLaserField(Material material) {
		super(material);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess world, BlockPos pos)
	{
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}

	/**
	 * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
	 * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
	 */
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	/**
	 * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
	 */
	@Override
	public boolean isNormalCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isPassable(IBlockAccess world, BlockPos pos)
	{
		return true;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void onEntityIntersected(World world, BlockPos pos, Entity entity)
	{
		if(!world.isRemote && entity instanceof EntityLivingBase && !EntityUtils.isInvisible((EntityLivingBase)entity))
		{
			for(EnumFacing facing : EnumFacing.VALUES)
			{
				for(int i = 0; i < ConfigHandler.laserBlockRange; i++)
				{
					BlockPos offsetPos = pos.offset(facing, i);
					Block block = world.getBlockState(offsetPos).getBlock();

					if(block == SCContent.laserBlock && !BlockUtils.getBlockProperty(world, offsetPos, BlockLaserBlock.POWERED))
					{
						TileEntity te = world.getTileEntity(offsetPos);

						if(te instanceof IModuleInventory && ((IModuleInventory)te).hasModule(EnumModuleType.WHITELIST) && ModuleUtils.getPlayersFromModule(world, offsetPos, EnumModuleType.WHITELIST).contains(((EntityLivingBase) entity).getName().toLowerCase()))
							return;

						world.setBlockState(offsetPos, world.getBlockState(offsetPos).withProperty(BlockLaserBlock.POWERED, true));
						world.scheduleUpdate(offsetPos, SCContent.laserBlock, 50);

						if(te instanceof IModuleInventory && ((IModuleInventory)te).hasModule(EnumModuleType.HARMING))
						{
							if(!(entity instanceof EntityPlayer && ((IOwnable)te).getOwner().isOwner((EntityPlayer)entity)))
								((EntityLivingBase) entity).attackEntityFrom(CustomDamageSources.LASER, 10F);
						}
					}
				}
			}
		}
	}

	/**
	 * Called right before the block is destroyed by a player.  Args: world, pos, state
	 */
	@Override
	public void onPlayerDestroy(World world, BlockPos pos, IBlockState state)
	{
		if(!world.isRemote)
		{
			EnumFacing[] facingArray = {EnumFacing.byIndex((state.getValue(BlockLaserField.BOUNDTYPE) - 1) * 2), EnumFacing.byIndex((state.getValue(BlockLaserField.BOUNDTYPE) - 1) * 2).getOpposite()};

			for(EnumFacing facing : facingArray)
			{
				for(int i = 0; i < ConfigHandler.laserBlockRange; i++)
				{
					if(BlockUtils.getBlock(world, pos.offset(facing, i)) == SCContent.laserBlock)
					{
						for(int j = 1; j < i; j++)
						{
							world.destroyBlock(pos.offset(facing, j), false);
						}
						break;
					}
				}
			}
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
	{
		if (source.getBlockState(pos).getValue(BOUNDTYPE) == 1)
			return BOUNDTYPE_1_AABB;
		else if (source.getBlockState(pos).getValue(BOUNDTYPE) == 2)
			return BOUNDTYPE_2_AABB;
		else if (source.getBlockState(pos).getValue(BOUNDTYPE) == 3)
			return BOUNDTYPE_3_AABB;
		else return FULL_BLOCK_AABB;
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
	{
		return getDefaultState().withProperty(BOUNDTYPE, 1);
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState().withProperty(BOUNDTYPE, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(BOUNDTYPE);
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, BOUNDTYPE);
	}

	@Override
	@SideOnly(Side.CLIENT)

	/**
	 * only called by clickMiddleMouseButton , and passed to inventory.setCurrentItem (along with isCreative)
	 */
	public ItemStack getItem(World world, BlockPos pos, IBlockState state)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntitySCTE().intersectsEntities();
	}

}
