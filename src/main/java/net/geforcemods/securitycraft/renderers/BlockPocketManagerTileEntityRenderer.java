package net.geforcemods.securitycraft.renderers;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.geforcemods.securitycraft.blockentity.BlockPocketManagerBlockEntity;
import net.geforcemods.securitycraft.blocks.BlockPocketManagerBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockPocketManagerTileEntityRenderer extends TileEntityRenderer<BlockPocketManagerBlockEntity> {
	public BlockPocketManagerTileEntityRenderer(TileEntityRendererDispatcher terd) {
		super(terd);
	}

	@Override
	public void render(BlockPocketManagerBlockEntity te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
		// The code below draws the outline border of a block pocket.

		if (!te.showOutline)
			return;

		Matrix4f positionMatrix = matrix.last().pose();
		Direction facing = te.getBlockState().getValue(BlockPocketManagerBlock.FACING);
		IVertexBuilder builder = buffer.getBuffer(RenderType.lines());
		int offset = facing == Direction.NORTH || facing == Direction.EAST ? -te.autoBuildOffset : te.autoBuildOffset; //keep negative values moving the offset to the left consistent
		int size = te.size;
		int half = (size - 1) / 2;
		int leftX = -half + offset;
		int rightX = half + 1 + offset;
		int frontZ = facing == Direction.NORTH || facing == Direction.WEST ? 0 : 1;
		int backZ = facing == Direction.NORTH || facing == Direction.WEST ? size : 1 - size;

		//x- and z-values get switched when the manager's direction is west or east
		if (facing == Direction.EAST || facing == Direction.WEST) {
			leftX = frontZ;
			rightX = backZ;
			frontZ = -half + offset;
			backZ = half + 1 + offset;
		}

		//bottom lines
		builder.vertex(positionMatrix, leftX, 0.0F, frontZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, rightX, 0.0F, frontZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, leftX, 0.0F, backZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, rightX, 0.0F, backZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, leftX, 0.0F, frontZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, leftX, 0.0F, backZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, rightX, 0.0F, frontZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, rightX, 0.0F, backZ).color(0, 0, 255, 255).endVertex();
		//top lines
		builder.vertex(positionMatrix, leftX, size, frontZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, rightX, size, frontZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, leftX, size, backZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, rightX, size, backZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, leftX, size, frontZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, leftX, size, backZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, rightX, size, frontZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, rightX, size, backZ).color(0, 0, 255, 255).endVertex();
		//corner edge lines
		builder.vertex(positionMatrix, leftX, 0.0F, frontZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, leftX, size, frontZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, leftX, 0.0F, backZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, leftX, size, backZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, rightX, 0.0F, backZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, rightX, size, backZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, rightX, 0.0F, frontZ).color(0, 0, 255, 255).endVertex();
		builder.vertex(positionMatrix, rightX, size, frontZ).color(0, 0, 255, 255).endVertex();
	}

	@Override
	public boolean shouldRenderOffScreen(BlockPocketManagerBlockEntity te) {
		return te.showOutline;
	}
}
