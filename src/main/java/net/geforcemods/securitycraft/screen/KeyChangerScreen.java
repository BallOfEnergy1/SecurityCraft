package net.geforcemods.securitycraft.screen;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.inventory.GenericMenu;
import net.geforcemods.securitycraft.network.server.SetPasscode;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class KeyChangerScreen extends GuiContainer {
	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/blank.png");
	private char[] allowedChars = {
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '\u0008', '\u0009', '\u001B'
	}; //0-9, backspace, tab, and escape
	private GuiTextField textboxNewPasscode;
	private GuiTextField textboxConfirmPasscode;
	private GuiButton confirmButton;
	private TileEntity tileEntity;

	public KeyChangerScreen(TileEntity tileEntity) {
		super(new GenericMenu(null));
		this.tileEntity = tileEntity;
	}

	@Override
	public void initGui() {
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		confirmButton = addButton(new GuiButton(0, width / 2 - 52, height / 2 + 52, 100, 20, Utils.localize("gui.securitycraft:universalKeyChanger.confirm").getFormattedText()));
		confirmButton.enabled = false;

		textboxNewPasscode = new GuiTextField(0, fontRenderer, width / 2 - 57, height / 2 - 47, 110, 12);

		textboxNewPasscode.setTextColor(-1);
		textboxNewPasscode.setDisabledTextColour(-1);
		textboxNewPasscode.setEnableBackgroundDrawing(true);
		textboxNewPasscode.setMaxStringLength(20);
		textboxNewPasscode.setFocused(true);

		textboxConfirmPasscode = new GuiTextField(1, fontRenderer, width / 2 - 57, height / 2 - 7, 110, 12);

		textboxConfirmPasscode.setTextColor(-1);
		textboxConfirmPasscode.setDisabledTextColour(-1);
		textboxConfirmPasscode.setEnableBackgroundDrawing(true);
		textboxConfirmPasscode.setMaxStringLength(20);
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
		textboxNewPasscode.drawTextBox();
		textboxConfirmPasscode.drawTextBox();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(Utils.localize("item.securitycraft:universalKeyChanger.name").getFormattedText(), xSize / 2 - fontRenderer.getStringWidth(Utils.localize("item.securitycraft:universalKeyChanger.name").getFormattedText()) / 2, 6, 4210752);
		fontRenderer.drawString(Utils.localize("gui.securitycraft:universalKeyChanger.enterNewPasscode").getFormattedText(), xSize / 2 - fontRenderer.getStringWidth(Utils.localize("gui.securitycraft:universalKeyChanger.enterNewPasscode").getFormattedText()) / 2, 25, 4210752);
		fontRenderer.drawString(Utils.localize("gui.securitycraft:universalKeyChanger.confirmNewPasscode").getFormattedText(), xSize / 2 - fontRenderer.getStringWidth(Utils.localize("gui.securitycraft:universalKeyChanger.confirmNewPasscode").getFormattedText()) / 2, 65, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(TEXTURE);
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;
		this.drawTexturedModalRect(startX, startY, 0, 0, xSize, ySize);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_TAB) {
			boolean isFirstTextboxFocused = textboxNewPasscode.isFocused();

			textboxNewPasscode.setFocused(!isFirstTextboxFocused);
			textboxConfirmPasscode.setFocused(isFirstTextboxFocused);
			return;
		}
		else if (keyCode == Keyboard.KEY_ESCAPE || !isValidChar(typedChar)) {
			super.keyTyped(typedChar, keyCode);
			return;
		}

		if (textboxNewPasscode.isFocused())
			textboxNewPasscode.textboxKeyTyped(typedChar, keyCode);
		else if (textboxConfirmPasscode.isFocused())
			textboxConfirmPasscode.textboxKeyTyped(typedChar, keyCode);
		else
			super.keyTyped(typedChar, keyCode);

		setConfirmButtonState();
	}

	private boolean isValidChar(char c) {
		for (int x = 1; x <= allowedChars.length; x++) {
			if (c == allowedChars[x - 1])
				return true;
		}

		return false;
	}

	private void setConfirmButtonState() {
		String newPasscode = textboxNewPasscode.getText();

		confirmButton.enabled = newPasscode != null && newPasscode.equals(textboxConfirmPasscode.getText());
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		textboxNewPasscode.mouseClicked(mouseX, mouseY, mouseButton);
		textboxConfirmPasscode.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 0) {
			SecurityCraft.network.sendToServer(new SetPasscode(tileEntity.getPos().getX(), tileEntity.getPos().getY(), tileEntity.getPos().getZ(), textboxNewPasscode.getText()));
			Minecraft.getMinecraft().player.closeScreen();
			PlayerUtils.sendMessageToPlayer(Minecraft.getMinecraft().player, Utils.localize("item.securitycraft:universalKeyChanger.name"), Utils.localize("messages.securitycraft:universalKeyChanger.passcodeChanged"), TextFormatting.GREEN, true);
		}
	}
}
