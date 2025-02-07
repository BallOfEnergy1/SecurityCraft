package net.geforcemods.securitycraft.screen;

import com.mojang.blaze3d.platform.InputConstants;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.network.server.SetPasscode;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SetPasscodeScreen extends Screen {
	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/blank.png");
	private int imageWidth = 176;
	private int imageHeight = 166;
	private int leftPos;
	private int topPos;
	private BlockEntity be;
	private Component setup;
	private MutableComponent combined;
	private EditBox keycodeTextbox;

	public SetPasscodeScreen(BlockEntity be, Component title) {
		super(title);
		this.be = be;
		setup = Utils.localize("gui.securitycraft:passcode.setup");
		combined = title.plainCopy().append(Component.literal(" ")).append(setup);
	}

	@Override
	public void init() {
		super.init();

		leftPos = (width - imageWidth) / 2;
		topPos = (height - imageHeight) / 2;

		Button saveAndContinueButton = addRenderableWidget(new Button(width / 2 - 48, height / 2 + 30 + 10, 100, 20, Utils.localize("gui.securitycraft:passcode.save"), this::saveAndContinueButtonClicked, Button.DEFAULT_NARRATION));

		saveAndContinueButton.active = false;
		keycodeTextbox = addRenderableWidget(new EditBox(font, width / 2 - 37, height / 2 - 47, 77, 12, Component.empty()));
		keycodeTextbox.setMaxLength(Integer.MAX_VALUE);
		keycodeTextbox.setFilter(s -> s.matches("\\d*"));
		keycodeTextbox.setResponder(text -> saveAndContinueButton.active = !text.isEmpty());
		setInitialFocus(keycodeTextbox);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics);
		guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawString(font, "CODE:", width / 2 - 67, height / 2 - 47 + 2, 4210752);

		if (font.width(combined) < imageWidth - 10)
			guiGraphics.drawString(font, combined, width / 2 - font.width(combined) / 2, topPos + 6, 4210752, false);
		else {
			guiGraphics.drawString(font, title, width / 2 - font.width(title) / 2, topPos + 6, 4210752, false);
			guiGraphics.drawString(font, setup, width / 2 - font.width(setup) / 2, topPos + 16, 4210752, false);
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
			onClose();
			return true;
		}

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void saveAndContinueButtonClicked(Button button) {
		SecurityCraft.CHANNEL.sendToServer(new SetPasscode(be.getBlockPos().getX(), be.getBlockPos().getY(), be.getBlockPos().getZ(), keycodeTextbox.getValue()));
		Minecraft.getInstance().player.closeContainer();
	}
}
