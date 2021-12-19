package net.geforcemods.securitycraft.screen.components;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.GuiUtils;

@OnlyIn(Dist.CLIENT)
public class PictureButton extends IdButton {
	private final ItemRenderer itemRenderer;
	private ItemStack blockToRender = ItemStack.EMPTY;
	private ItemStack itemToRender = ItemStack.EMPTY;
	private ResourceLocation textureLocation;
	private int u;
	private int v;
	private int drawOffsetX;
	private int drawOffsetY;
	private int drawWidth;
	private int drawHeight;
	private int textureWidth;
	private int textureHeight;

	public PictureButton(int id, int xPos, int yPos, int width, int height, ItemRenderer par7, ItemStack itemToRender) {
		this(id, xPos, yPos, width, height, par7, itemToRender, null);
	}

	public PictureButton(int id, int xPos, int yPos, int width, int height, ItemRenderer par7, ItemStack itemToRender, Consumer<IdButton> onClick) {
		super(id, xPos, yPos, width, height, "", onClick);
		itemRenderer = par7;

		if (!itemToRender.isEmpty() && itemToRender.getItem() instanceof BlockItem)
			blockToRender = new ItemStack(Block.byItem(itemToRender.getItem()));
		else
			this.itemToRender = new ItemStack(itemToRender.getItem());
	}

	public PictureButton(int id, int xPos, int yPos, int width, int height, ResourceLocation texture, int textureX, int textureY, int drawOffsetX, int drawOffsetY, int drawWidth, int drawHeight, int textureWidth, int textureHeight, Consumer<IdButton> onClick) {
		super(id, xPos, yPos, width, height, "", onClick);

		itemRenderer = null;
		textureLocation = texture;
		u = textureX;
		v = textureY;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.drawOffsetX = drawOffsetX;
		this.drawOffsetY = drawOffsetY;
		this.drawWidth = drawWidth;
		this.drawHeight = drawHeight;
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			Minecraft mc = Minecraft.getInstance();
			Font font = mc.font;

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			GuiUtils.drawContinuousTexturedBox(pose, WIDGETS_LOCATION, x, y, 0, 46 + getYImage(isHoveredOrFocused()) * 20, width, height, 200, 20, 2, 3, 2, 2, getBlitOffset());

			if (!blockToRender.isEmpty()) {
				itemRenderer.renderAndDecorateItem(blockToRender, x + 2, y + 3);
				itemRenderer.renderGuiItemDecorations(font, blockToRender, x + 2, y + 3, "");
			}
			else if (!itemToRender.isEmpty()) {
				itemRenderer.renderAndDecorateItem(itemToRender, x + 2, y + 2);
				itemRenderer.renderGuiItemDecorations(font, itemToRender, x + 2, y + 2, "");
			}
			else if (getTextureLocation() != null) {
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem._setShaderTexture(0, getTextureLocation());
				blit(pose, x + drawOffsetX, y + drawOffsetY, drawWidth, drawHeight, u, v, drawWidth, drawHeight, textureWidth, textureHeight);
			}
		}
	}

	public ResourceLocation getTextureLocation() {
		return textureLocation;
	}

	public ItemStack getItemStack() {
		return !blockToRender.isEmpty() ? blockToRender : itemToRender;
	}
}
