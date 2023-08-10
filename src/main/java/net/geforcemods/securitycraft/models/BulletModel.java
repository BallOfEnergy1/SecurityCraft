package net.geforcemods.securitycraft.models;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.geforcemods.securitycraft.entity.sentry.Bullet;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BulletModel extends EntityModel<Bullet> {
	public final ModelRenderer bullet;

	public BulletModel() {
		texWidth = 8;
		texHeight = 4;
		bullet = new ModelRenderer(this, 0, 0);
		bullet.setPos(0.0F, 0.0F, 0.0F);
		bullet.addBox(0.0F, 0.0F, 0.0F, 1, 1, 2);
	}

	@Override
	public void renderToBuffer(MatrixStack matrix, IVertexBuilder builder, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bullet.render(matrix, builder, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public void setupAnim(Bullet entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {}
}
