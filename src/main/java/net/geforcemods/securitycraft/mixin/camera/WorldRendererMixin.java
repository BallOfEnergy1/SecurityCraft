package net.geforcemods.securitycraft.mixin.camera;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.WorldRenderer;

/**
 * This mixin fixes camera chunks disappearing when the player entity moves while viewing a camera (e.g. while being in a
 * minecart or falling)
 */
@Mixin(value = WorldRenderer.class, priority = 1100)
public class WorldRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Redirect(method = "setupRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ViewFrustum;repositionCamera(DD)V"))
	public void onRepositionCamera(ViewFrustum viewFrustum, double x, double z) {
		if (!PlayerUtils.isPlayerMountedOnCamera(minecraft.player))
			viewFrustum.repositionCamera(x, z);
	}
}
