package net.geforcemods.securitycraft.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LinkedBlock {
	public String blockName;
	public BlockPos blockPos = null;

	public LinkedBlock(String name, BlockPos pos) {
		blockName = name;
		blockPos = pos;
	}

	public LinkedBlock(LinkableTileEntity tileEntity) {
		blockName = tileEntity.getBlockState().getBlock().getTranslationKey();
		blockPos = tileEntity.getPos();
	}

	public boolean validate(World world) {
		return !(world == null || (world.isAirBlock(blockPos) || !world.getBlockState(blockPos).getBlock().getTranslationKey().equals(blockName)));
	}

	public LinkableTileEntity asTileEntity(World world) {
		if (!validate(world))
			return null;

		return (LinkableTileEntity) world.getTileEntity(blockPos);
	}

	public String getBlockName() {
		return blockName;
	}

	public void setName(String blockName) {
		this.blockName = blockName;
	}

	public void setPos(BlockPos pos) {
		blockPos = pos;
	}

	public BlockPos getPos() {
		return blockPos;
	}

	public int getX() {
		return blockPos.getX();
	}

	public int getY() {
		return blockPos.getY();
	}

	public int getZ() {
		return blockPos.getZ();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof LinkedBlock) {
			LinkedBlock block = (LinkedBlock) o;
			return (block.getPos().getX() == blockPos.getX() && block.getPos().getY() == blockPos.getY() && block.getPos().getZ() == blockPos.getZ());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return blockPos.getX() + blockPos.getY() + blockPos.getZ();
	}

	@Override
	public String toString() {
		return (blockName + " | " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ());
	}
}
