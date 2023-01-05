package net.geforcemods.securitycraft.blocks.mines;

import java.util.Random;

import net.geforcemods.securitycraft.api.TileEntityOwnable;
import net.geforcemods.securitycraft.entity.EntityBouncingBetty;
import net.geforcemods.securitycraft.util.EntityUtils;
import net.geforcemods.securitycraft.util.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockBouncingBetty extends BlockExplosive {
	public static final PropertyBool DEACTIVATED = PropertyBool.create("deactivated");

	public BlockBouncingBetty(Material material) {
		super(material);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		return new AxisAlignedBB(0.200F, 0.000F, 0.200F, 0.800F, 0.200F, 0.800F);
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		if (world.getBlockState(pos.down()).getMaterial() != Material.AIR)
			return;
		else if (world.getBlockState(pos).getValue(DEACTIVATED))
			world.destroyBlock(pos, true);
		else
			explode(world, pos);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		return world.isSideSolid(pos.down(), EnumFacing.UP);
	}

	@Override
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
		if (!state.getBoundingBox(world, pos).offset(pos).grow(0.01D).intersects(entity.getEntityBoundingBox()))
			return;
		else if (!EntityUtils.doesEntityOwn(entity, world, pos) && !(entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative()))
			explode(world, pos);
	}

	@Override
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (!player.capabilities.isCreativeMode && !EntityUtils.doesPlayerOwn(player, world, pos))
			explode(world, pos);
	}

	@Override
	public boolean activateMine(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);

		if (state.getValue(DEACTIVATED)) {
			world.setBlockState(pos, state.withProperty(DEACTIVATED, false));
			return true;
		}

		return false;
	}

	@Override
	public boolean defuseMine(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);

		if (!state.getValue(DEACTIVATED)) {
			world.setBlockState(pos, state.withProperty(DEACTIVATED, true));
			return true;
		}

		return false;
	}

	@Override
	public void explode(World world, BlockPos pos) {
		if (world.isRemote || world.getBlockState(pos).getValue(DEACTIVATED))
			return;

		world.setBlockToAir(pos);
		EntityBouncingBetty entitytntprimed = new EntityBouncingBetty(world, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
		entitytntprimed.fuse = 15;
		entitytntprimed.motionY = 0.5D;
		WorldUtils.addScheduledTask(world, () -> world.spawnEntity(entitytntprimed));
		entitytntprimed.playSound(SoundEvent.REGISTRY.getObject(new ResourceLocation("entity.tnt.primed")), 1.0F, 1.0F);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random random, int fortune) {
		return Item.getItemFromBlock(this);
	}

	@Override
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(Item.getItemFromBlock(this));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(DEACTIVATED, meta == 1 ? true : false);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(DEACTIVATED) ? 1 : 0;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, DEACTIVATED);
	}

	@Override
	public boolean isActive(World world, BlockPos pos) {
		return !world.getBlockState(pos).getValue(DEACTIVATED);
	}

	@Override
	public boolean isDefusable() {
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityOwnable();
	}
}
