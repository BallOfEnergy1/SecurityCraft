package net.geforcemods.securitycraft.gui;

import java.util.ArrayList;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.containers.ContainerGeneric;
import net.geforcemods.securitycraft.gui.components.HoverChecker;
import net.geforcemods.securitycraft.items.ItemCameraMonitor;
import net.geforcemods.securitycraft.misc.CameraView;
import net.geforcemods.securitycraft.network.server.MountCamera;
import net.geforcemods.securitycraft.network.server.RemoveCameraTag;
import net.geforcemods.securitycraft.tileentity.TileEntitySecurityCamera;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GuiCameraMonitor extends GuiContainer {
	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/blank.png");
	private InventoryPlayer playerInventory;
	private ItemCameraMonitor cameraMonitor;
	private NBTTagCompound nbtTag;
	private GuiButton prevPageButton;
	private GuiButton nextPageButton;
	private GuiButton[] cameraButtons = new GuiButton[10];
	private GuiButton[] unbindButtons = new GuiButton[10];
	private HoverChecker[] hoverCheckers = new HoverChecker[10];
	private TileEntitySecurityCamera[] cameraTEs = new TileEntitySecurityCamera[10];
	private int[] cameraViewDim = new int[10];
	private int page = 1;

	public GuiCameraMonitor(InventoryPlayer inventory, ItemCameraMonitor item, NBTTagCompound itemNBTTag) {
		super(new ContainerGeneric(inventory, null));
		playerInventory = inventory;
		cameraMonitor = item;
		nbtTag = itemNBTTag;
	}

	public GuiCameraMonitor(InventoryPlayer inventory, ItemCameraMonitor item, NBTTagCompound itemNBTTag, int page) {
		this(inventory, item, itemNBTTag);
		this.page = page;
	}

	@Override
	public void initGui() {
		super.initGui();

		prevPageButton = new GuiButton(-1, width / 2 - 68, height / 2 + 40, 20, 20, "<");
		nextPageButton = new GuiButton(0, width / 2 + 52, height / 2 + 40, 20, 20, ">");
		buttonList.add(prevPageButton);
		buttonList.add(nextPageButton);

		cameraButtons[0] = new GuiButton(1, width / 2 - 38, height / 2 - 60 + 10, 20, 20, "#");
		cameraButtons[1] = new GuiButton(2, width / 2 - 8, height / 2 - 60 + 10, 20, 20, "#");
		cameraButtons[2] = new GuiButton(3, width / 2 + 22, height / 2 - 60 + 10, 20, 20, "#");
		cameraButtons[3] = new GuiButton(4, width / 2 - 38, height / 2 - 30 + 10, 20, 20, "#");
		cameraButtons[4] = new GuiButton(5, width / 2 - 8, height / 2 - 30 + 10, 20, 20, "#");
		cameraButtons[5] = new GuiButton(6, width / 2 + 22, height / 2 - 30 + 10, 20, 20, "#");
		cameraButtons[6] = new GuiButton(7, width / 2 - 38, height / 2 + 10, 20, 20, "#");
		cameraButtons[7] = new GuiButton(8, width / 2 - 8, height / 2 + 10, 20, 20, "#");
		cameraButtons[8] = new GuiButton(9, width / 2 + 22, height / 2 + 10, 20, 20, "#");
		cameraButtons[9] = new GuiButton(10, width / 2 - 38, height / 2 + 40, 80, 20, "#");

		unbindButtons[0] = new GuiButton(11, width / 2 - 19, height / 2 - 68 + 10, 8, 8, "x");
		unbindButtons[1] = new GuiButton(12, width / 2 + 11, height / 2 - 68 + 10, 8, 8, "x");
		unbindButtons[2] = new GuiButton(13, width / 2 + 41, height / 2 - 68 + 10, 8, 8, "x");
		unbindButtons[3] = new GuiButton(14, width / 2 - 19, height / 2 - 38 + 10, 8, 8, "x");
		unbindButtons[4] = new GuiButton(15, width / 2 + 11, height / 2 - 38 + 10, 8, 8, "x");
		unbindButtons[5] = new GuiButton(16, width / 2 + 41, height / 2 - 38 + 10, 8, 8, "x");
		unbindButtons[6] = new GuiButton(17, width / 2 - 19, height / 2 + 2, 8, 8, "x");
		unbindButtons[7] = new GuiButton(18, width / 2 + 11, height / 2 + 2, 8, 8, "x");
		unbindButtons[8] = new GuiButton(19, width / 2 + 41, height / 2 + 2, 8, 8, "x");
		unbindButtons[9] = new GuiButton(20, width / 2 + 41, height / 2 + 32, 8, 8, "x");

		for (int i = 0; i < 10; i++) {
			GuiButton button = cameraButtons[i];
			int camID = (button.id + ((page - 1) * 10));
			ArrayList<CameraView> views = cameraMonitor.getCameraPositions(nbtTag);
			CameraView view = views.get(camID - 1);

			button.displayString += camID;
			buttonList.add(button);

			if (view != null) {
				if (view.dimension != Minecraft.getMinecraft().player.dimension) {
					hoverCheckers[button.id - 1] = new HoverChecker(button);
					cameraViewDim[button.id - 1] = view.dimension;
				}

				World world = Minecraft.getMinecraft().world;
				TileEntity te = world.getTileEntity(view.getLocation());

				cameraTEs[button.id - 1] = te instanceof TileEntitySecurityCamera ? (TileEntitySecurityCamera) te : null;
				hoverCheckers[button.id - 1] = new HoverChecker(button);

				if (cameraTEs[button.id - 1] != null && cameraTEs[button.id - 1].isDisabled())
					button.enabled = false;
			}
			else {
				button.enabled = false;
				unbindButtons[button.id - 1].enabled = false;
				cameraTEs[button.id - 1] = null;
				continue;
			}
		}

		for (int i = 0; i < 10; i++) {
			buttonList.add(unbindButtons[i]);
		}

		if (page == 1)
			prevPageButton.enabled = false;

		if (page == 3 || cameraMonitor.getCameraPositions(nbtTag).size() < (page * 10) + 1)
			nextPageButton.enabled = false;

		for (int i = cameraMonitor.getCameraPositions(nbtTag).size() + 1; i <= (page * 10); i++) {
			cameraButtons[(i - 1) - ((page - 1) * 10)].enabled = false;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		for (int i = 0; i < hoverCheckers.length; i++) {
			if (hoverCheckers[i] != null && hoverCheckers[i].checkHover(mouseX, mouseY)) {
				if (cameraTEs[i] != null) {
					if (cameraTEs[i].isDisabled())
						drawHoveringText(Utils.localize("gui.securitycraft:scManual.disabled").getFormattedText(), mouseX, mouseY);
					else if (cameraTEs[i].hasCustomName())
						drawHoveringText(mc.fontRenderer.listFormattedStringToWidth(Utils.localize("gui.securitycraft:monitor.cameraName").getFormattedText().replace("#", cameraTEs[i].getName()), 150), mouseX, mouseY, mc.fontRenderer);
				}
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == -1)
			mc.displayGuiScreen(new GuiCameraMonitor(playerInventory, cameraMonitor, nbtTag, page - 1));
		else if (button.id == 0)
			mc.displayGuiScreen(new GuiCameraMonitor(playerInventory, cameraMonitor, nbtTag, page + 1));
		else if (button.id < 11) {
			int camID = button.id + ((page - 1) * 10);
			BlockPos cameraPos = cameraMonitor.getCameraPositions(nbtTag).get(camID - 1).getLocation();
			TileEntity te = mc.world.getTileEntity(cameraPos);

			if (te instanceof TileEntitySecurityCamera && ((TileEntitySecurityCamera) te).isDisabled()) {
				button.enabled = false;
				return;
			}

			SecurityCraft.network.sendToServer(new MountCamera(cameraPos));
			Minecraft.getMinecraft().player.closeScreen();
		}
		else {
			int camID = (button.id - 10) + ((page - 1) * 10);

			SecurityCraft.network.sendToServer(new RemoveCameraTag(PlayerUtils.getSelectedItemStack(playerInventory, SCContent.cameraMonitor), camID));
			nbtTag.removeTag(ItemCameraMonitor.getTagNameFromPosition(nbtTag, cameraMonitor.getCameraPositions(nbtTag).get(camID - 1)));
			button.enabled = false;
			cameraButtons[(camID - 1) % 10].enabled = false;
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRenderer.drawString(Utils.localize("gui.securitycraft:monitor.selectCameras").getFormattedText(), xSize / 2 - fontRenderer.getStringWidth(Utils.localize("gui.securitycraft:monitor.selectCameras").getFormattedText()) / 2, 6, 4210752);
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
	public boolean doesGuiPauseGame() {
		return false;
	}
}