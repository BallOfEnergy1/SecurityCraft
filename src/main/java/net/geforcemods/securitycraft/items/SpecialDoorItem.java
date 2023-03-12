package net.geforcemods.securitycraft.items;

import net.geforcemods.securitycraft.api.LinkableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpecialDoorItem extends ItemBlock {
	public SpecialDoorItem(Block block) {
		super(block);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);

		if (facing != EnumFacing.UP)
			return EnumActionResult.FAIL;
		else {
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if (!block.isReplaceable(world, pos))
				pos = pos.offset(facing);

			if (player.canPlayerEdit(pos, facing, stack) && getBlock().canPlaceBlockAt(world, pos)) {
				EnumFacing angleFacing = EnumFacing.fromAngle(player.rotationYaw);
				int offsetX = angleFacing.getXOffset();
				int offsetZ = angleFacing.getZOffset();
				boolean flag = offsetX < 0 && hitZ < 0.5F || offsetX > 0 && hitZ > 0.5F || offsetZ < 0 && hitX > 0.5F || offsetZ > 0 && hitX < 0.5F;
				placeDoor(world, pos, angleFacing, getBlock(), flag);
				SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
				world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				stack.shrink(1);

				TileEntity te = world.getTileEntity(pos);

				if (te != null) {
					LinkableBlockEntity lowerTe = (LinkableBlockEntity) te;
					LinkableBlockEntity upperTe = ((LinkableBlockEntity) world.getTileEntity(pos.up()));

					lowerTe.setOwner(player.getGameProfile().getId().toString(), player.getName());
					upperTe.setOwner(player.getGameProfile().getId().toString(), player.getName());
					LinkableBlockEntity.link(lowerTe, upperTe);
					lowerTe.setCustomName(stack.getDisplayName());
					upperTe.setCustomName(stack.getDisplayName());
				}

				return EnumActionResult.SUCCESS;
			}
			else
				return EnumActionResult.FAIL;
		}
	}

	public static void placeDoor(World world, BlockPos pos, EnumFacing facing, Block door, boolean isRightHinge) //naming might not be entirely correct, but it's giving a rough idea
	{
		BlockPos left = pos.offset(facing.rotateY());
		BlockPos right = pos.offset(facing.rotateYCCW());
		int rightNormalCubeAmount = (world.getBlockState(right).isNormalCube() ? 1 : 0) + (world.getBlockState(right.up()).isNormalCube() ? 1 : 0);
		int leftNormalCubeAmount = (world.getBlockState(left).isNormalCube() ? 1 : 0) + (world.getBlockState(left.up()).isNormalCube() ? 1 : 0);
		boolean isRightDoor = world.getBlockState(right).getBlock() == door || world.getBlockState(right.up()).getBlock() == door;
		boolean isLeftDoor = world.getBlockState(left).getBlock() == door || world.getBlockState(left.up()).getBlock() == door;

		if ((!isRightDoor || isLeftDoor) && leftNormalCubeAmount <= rightNormalCubeAmount) {
			if (isLeftDoor && !isRightDoor || leftNormalCubeAmount < rightNormalCubeAmount)
				isRightHinge = false;
		}
		else
			isRightHinge = true;

		BlockPos blockAbove = pos.up();
		IBlockState state = door.getDefaultState().withProperty(BlockDoor.FACING, facing).withProperty(BlockDoor.HINGE, isRightHinge ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT);
		world.setBlockState(pos, state.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER), 2);
		world.setBlockState(blockAbove, state.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), 2);
		world.notifyNeighborsOfStateChange(pos, door, false);
		world.notifyNeighborsOfStateChange(blockAbove, door, false);
	}
}
