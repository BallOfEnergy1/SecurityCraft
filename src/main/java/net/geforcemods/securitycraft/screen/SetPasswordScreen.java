package net.geforcemods.securitycraft.screen;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.inventory.GenericMenu;
import net.geforcemods.securitycraft.network.server.SetPassword;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SetPasswordScreen extends GuiContainer implements GuiResponder {
	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/blank.png");
	private TileEntity tileEntity;
	private char[] allowedChars = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '\u0008', '\u001B'
	}; //0-9, backspace and escape
	private String blockName;
	private GuiTextField keycodeTextbox;
	private GuiButton saveAndContinueButton;

	public SetPasswordScreen(InventoryPlayer inventoryPlayer, TileEntity tileEntity) {
		super(new GenericMenu(inventoryPlayer, tileEntity));
		this.tileEntity = tileEntity;
		blockName = Utils.localize(tileEntity.getBlockType().getTranslationKey() + ".name").getFormattedText();
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		buttonList.add(saveAndContinueButton = new GuiButton(0, width / 2 - 48, height / 2 + 30 + 10, 100, 20, Utils.localize("gui.securitycraft:password.save").getFormattedText()));
		saveAndContinueButton.enabled = false;

		keycodeTextbox = new GuiTextField(1, fontRenderer, width / 2 - 37, height / 2 - 47, 77, 12);
		keycodeTextbox.setTextColor(-1);
		keycodeTextbox.setDisabledTextColour(-1);
		keycodeTextbox.setEnableBackgroundDrawing(true);
		keycodeTextbox.setMaxStringLength(11);
		keycodeTextbox.setFocused(true);
		keycodeTextbox.setGuiResponder(this);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.disableLighting();
		keycodeTextbox.drawTextBox();
		drawString(fontRenderer, "CODE:", width / 2 - 67, height / 2 - 47 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String setup = Utils.localize("gui.securitycraft:password.setup").getFormattedText();
		String combined = blockName + " " + setup;

		if (fontRenderer.getStringWidth(combined) < xSize - 10)
			fontRenderer.drawString(combined, xSize / 2 - fontRenderer.getStringWidth(combined) / 2, 6, 4210752);
		else {
			fontRenderer.drawString(blockName, xSize / 2 - fontRenderer.getStringWidth(blockName) / 2, 6, 4210752);
			fontRenderer.drawString(setup, xSize / 2 - fontRenderer.getStringWidth(setup) / 2, 16, 4210752);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;

		drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(TEXTURE);
		drawTexturedModalRect(startX, startY, 0, 0, xSize, ySize);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode != Keyboard.KEY_ESCAPE && keycodeTextbox.isFocused() && isValidChar(typedChar))
			keycodeTextbox.textboxKeyTyped(typedChar, keyCode);
		else
			super.keyTyped(typedChar, keyCode);
	}

	private boolean isValidChar(char c) {
		for (int i = 0; i < allowedChars.length; i++) {
			if (c == allowedChars[i])
				return true;
		}

		return false;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		keycodeTextbox.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == saveAndContinueButton.id) {
			((IPasswordProtected) tileEntity).setPassword(keycodeTextbox.getText());
			SecurityCraft.network.sendToServer(new SetPassword(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ(), keycodeTextbox.getText()));
			Minecraft.getMinecraft().player.closeScreen();
		}
	}

	@Override
	public void setEntryValue(int id, String text) {
		saveAndContinueButton.enabled = !text.isEmpty();
	}

	@Override
	public void setEntryValue(int id, boolean value) {}

	@Override
	public void setEntryValue(int id, float value) {}
}
