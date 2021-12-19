package net.geforcemods.securitycraft.screen.components;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.client.gui.widget.Slider.ISlider;

public class NamedSlider extends Slider {
	public int id;
	private String blockName;
	private Consumer<NamedSlider> consumer;

	public NamedSlider(Component initialString, Component bN, int id, int xPos, int yPos, int width, int height, Component prefix, String suf, int minVal, int maxVal, int currentVal, boolean showDec, boolean drawStr, @Nullable ISlider par, Consumer<NamedSlider> method) {
		super(xPos, yPos, width, height, prefix, new TextComponent(suf), minVal, maxVal, currentVal, showDec, drawStr, b -> {}, par);

		setMessage(new TextComponent(initialString.getString()));
		blockName = bN.getString();
		this.id = id;
		consumer = method;
	}

	public NamedSlider(Component initialString, Component bN, int id, int xPos, int yPos, int width, int height, Component prefix, String suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, @Nullable ISlider par, Consumer<NamedSlider> method) {
		super(xPos, yPos, width, height, prefix, new TextComponent(suf), minVal, maxVal, currentVal, showDec, drawStr, b -> {}, par);

		setMessage(new TextComponent(initialString.getString()));
		blockName = bN.getString();
		this.id = id;
		consumer = method;
	}

	@Override
	public void renderButton(PoseStack pose, int mouseX, int mouseY, float partial) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		super.renderButton(pose, mouseX, mouseY, partial);
	}

	@Override
	protected void renderBg(PoseStack pose, Minecraft mc, int mouseX, int mouseY) {
		if (visible) {
			int offset = (isHovered() && active ? 2 : 1) * 20;

			if (dragging) {
				sliderValue = (mouseX - (x + 4)) / (float) (width - 8);
				updateSlider();
			}

			RenderSystem._setShaderTexture(0, WIDGETS_LOCATION);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			blit(pose, x + (int) (sliderValue * (width - 8)), y, 0, 46 + offset, 4, 20);
			blit(pose, x + (int) (sliderValue * (width - 8)) + 4, y, 196, 46 + offset, 4, 20);
		}
	}

	@Override
	public void onRelease(double mouseX, double mouseY) {
		super.onRelease(mouseX, mouseY);

		if (consumer != null)
			consumer.accept(this);
	}

	public String getBlockName() {
		return blockName;
	}
}
