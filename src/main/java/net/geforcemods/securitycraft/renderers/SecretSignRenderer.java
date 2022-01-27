package net.geforcemods.securitycraft.renderers;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.geforcemods.securitycraft.blockentities.SecretSignBlockEntity;
import net.geforcemods.securitycraft.blocks.SecretStandingSignBlock;
import net.geforcemods.securitycraft.blocks.SecretWallSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.SignTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.SignTileEntityRenderer.SignModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SecretSignRenderer extends TileEntityRenderer<SecretSignBlockEntity> {
	private final SignModel model = new SignModel();

	public SecretSignRenderer(TileEntityRendererDispatcher terd) {
		super(terd);
	}

	@Override
	public void render(SecretSignBlockEntity te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
		BlockState state = te.getBlockState();
		RenderMaterial material = SignTileEntityRenderer.getMaterial(state.getBlock());
		FontRenderer font = renderer.getFont();
		IVertexBuilder builder;

		matrix.pushPose();

		if (state.getBlock() instanceof SecretStandingSignBlock) {
			matrix.translate(0.5D, 0.5D, 0.5D);
			matrix.mulPose(Vector3f.YP.rotationDegrees(-(state.getValue(SecretStandingSignBlock.ROTATION) * 360 / 16.0F)));
			model.stick.visible = true;
		}
		else {
			matrix.translate(0.5D, 0.5D, 0.5D);
			matrix.mulPose(Vector3f.YP.rotationDegrees(-state.getValue(SecretWallSignBlock.FACING).toYRot()));
			matrix.translate(0.0D, -0.3125D, -0.4375D);
			model.stick.visible = false;
		}

		matrix.pushPose();
		matrix.scale(0.6666667F, -0.6666667F, -0.6666667F);
		builder = material.buffer(buffer, model::renderType);
		model.sign.render(matrix, builder, combinedLight, combinedOverlay);
		model.stick.render(matrix, builder, combinedLight, combinedOverlay);
		matrix.popPose();
		matrix.translate(0.0D, 0.33333334F, 0.046666667F);
		matrix.scale(0.010416667F, -0.010416667F, 0.010416667F);

		if (te.isPlayerAllowedToSeeText(Minecraft.getInstance().player)) {
			int textColor = te.getColor().getTextColor();
			int r = (int) (NativeImage.getR(textColor) * 0.4D);
			int g = (int) (NativeImage.getG(textColor) * 0.4D);
			int b = (int) (NativeImage.getB(textColor) * 0.4D);
			int argb = NativeImage.combine(0, b, g, r);

			for (int line = 0; line < 4; ++line) {
				IReorderingProcessor rp = te.getRenderMessage(line, (p_243502_1_) -> {
					List<IReorderingProcessor> list = font.split(p_243502_1_, 90);
					return list.isEmpty() ? IReorderingProcessor.EMPTY : list.get(0);
				});

				if (rp != null)
					font.drawInBatch(rp, -font.width(rp) / 2, line * 10 - 20, argb, false, matrix.last().pose(), buffer, false, 0, combinedLight);
			}
		}

		matrix.popPose();
	}
}