package net.geforcemods.securitycraft.models;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SonicSecuritySystemModel extends EntityModel<Entity> {
	public final ModelRenderer radar;
	public final ModelRenderer bb_main;

	public SonicSecuritySystemModel() {
		texWidth = 32;
		texHeight = 32;

		radar = new ModelRenderer(this);
		radar.setPos(0.0F, 10.5F, 0.0F);
		radar.texOffs(15, 0).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 2.0F, 0.0F, false);
		radar.texOffs(15, 3).addBox(-1.5F, -1.5F, 1.0F, 3.0F, 1.0F, 1.0F, 0.0F, false);
		radar.texOffs(15, 5).addBox(0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		radar.texOffs(15, 7).addBox(-1.5F, -0.5F, 1.0F, 1.0F, 1.0F, 1.0F, 0.0F, false);
		radar.texOffs(15, 9).addBox(-1.5F, 0.5F, 1.0F, 3.0F, 1.0F, 1.0F, 0.0F, false);
		radar.texOffs(0, 0).addBox(-2.5F, -2.5F, 1.5F, 5.0F, 1.0F, 1.0F, 0.0F, false);
		radar.texOffs(0, 2).addBox(1.5F, -1.5F, 1.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		radar.texOffs(0, 6).addBox(-2.5F, -1.5F, 1.5F, 1.0F, 3.0F, 1.0F, 0.0F, false);
		radar.texOffs(0, 10).addBox(-2.5F, 1.5F, 1.5F, 5.0F, 1.0F, 1.0F, 0.0F, false);

		bb_main = new ModelRenderer(this);
		bb_main.setPos(0.0F, 24.0F, 0.0F);
		bb_main.texOffs(0, 28).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 1.0F, 3.0F, 0.0F, false);
		bb_main.texOffs(0, 25).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);
		bb_main.texOffs(12, 20).addBox(-0.5F, -13.0F, -0.5F, 1.0F, 11.0F, 1.0F, 0.0F, false);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		radar.render(matrixStack, buffer, packedLight, packedOverlay);
		bb_main.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRadarRotation(float rotation) {
		radar.yRot = rotation;
	}
}