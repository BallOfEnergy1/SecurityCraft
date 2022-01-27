package net.geforcemods.securitycraft.screen;

import java.util.Arrays;

import com.mojang.blaze3d.systems.RenderSystem;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.containers.BlockPocketManagerContainer;
import net.geforcemods.securitycraft.network.server.SyncBlockPocketManager;
import net.geforcemods.securitycraft.screen.components.NamedSlider;
import net.geforcemods.securitycraft.screen.components.StackHoverChecker;
import net.geforcemods.securitycraft.screen.components.StringHoverChecker;
import net.geforcemods.securitycraft.tileentity.BlockPocketManagerTileEntity;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
import net.minecraftforge.fml.client.gui.widget.Slider;
import net.minecraftforge.fml.network.PacketDistributor;

public class BlockPocketManagerScreen extends ContainerScreen<BlockPocketManagerContainer> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/block_pocket_manager.png");
	private static final ResourceLocation TEXTURE_STORAGE = new ResourceLocation("securitycraft:textures/gui/container/block_pocket_manager_storage.png");
	private static final ItemStack BLOCK_POCKET_WALL = new ItemStack(SCContent.BLOCK_POCKET_WALL.get());
	private static final ItemStack REINFORCED_CHISELED_CRYSTAL_QUARTZ = new ItemStack(SCContent.REINFORCED_CHISELED_CRYSTAL_QUARTZ.get());
	private static final ItemStack REINFORCED_CRYSTAL_QUARTZ_PILLAR = new ItemStack(SCContent.REINFORCED_CRYSTAL_QUARTZ_PILLAR.get());
	private final String blockPocketManager = Utils.localize(SCContent.BLOCK_POCKET_MANAGER.get().getDescriptionId()).getColoredString();
	private final String youNeed = Utils.localize("gui.securitycraft:blockPocketManager.youNeed").getColoredString();
	private final boolean storage;
	private final boolean isOwner;
	private final int[] materialCounts = new int[3];
	public BlockPocketManagerTileEntity te;
	private int size = 5;
	private Button toggleButton;
	private Button sizeButton;
	private Button assembleButton;
	private Button outlineButton;
	private Slider offsetSlider;
	private StackHoverChecker[] hoverCheckers = new StackHoverChecker[3];
	private StringHoverChecker assembleHoverChecker;
	private int wallsNeededOverall = (size - 2) * (size - 2) * 6;
	private int pillarsNeededOverall = (size - 2) * 12 - 1;
	private final int chiseledNeededOverall = 8;
	private int wallsStillNeeded;
	private int pillarsStillNeeded;
	private int chiseledStillNeeded;

	public BlockPocketManagerScreen(BlockPocketManagerContainer container, PlayerInventory inv, ITextComponent name) {
		super(container, inv, name);

		te = container.te;
		size = te.size;
		isOwner = container.isOwner;
		storage = container.storage;

		if (storage)
			imageWidth = 256;

		imageHeight = !storage ? 194 : 240;
	}

	@Override
	public void init() {
		super.init();

		int width = storage ? 123 : imageWidth;
		int widgetWidth = storage ? 110 : 120;
		int widgetOffset = widgetWidth / 2;
		//@formatter:off
		int[] yOffset = storage ? new int[] {-76, -100, -52, -28, -4} : new int[] {-40, -70, 23, 47, 71};
		//@formatter:on

		addButton(toggleButton = new ExtendedButton(leftPos + width / 2 - widgetOffset, topPos + imageHeight / 2 + yOffset[0], widgetWidth, 20, Utils.localize("gui.securitycraft:blockPocketManager." + (!te.enabled ? "activate" : "deactivate")).getColoredString(), this::toggleButtonClicked));
		addButton(sizeButton = new ExtendedButton(leftPos + width / 2 - widgetOffset, topPos + imageHeight / 2 + yOffset[1], widgetWidth, 20, Utils.localize("gui.securitycraft:blockPocketManager.size", size, size, size).getColoredString(), this::sizeButtonClicked));
		addButton(assembleButton = new ExtendedButton(leftPos + width / 2 - widgetOffset, topPos + imageHeight / 2 + yOffset[2], widgetWidth, 20, Utils.localize("gui.securitycraft:blockPocketManager.assemble").getColoredString(), this::assembleButtonClicked));
		addButton(outlineButton = new ExtendedButton(leftPos + width / 2 - widgetOffset, topPos + imageHeight / 2 + yOffset[3], widgetWidth, 20, Utils.localize("gui.securitycraft:blockPocketManager.outline." + (!te.showOutline ? "show" : "hide")).getColoredString(), this::outlineButtonClicked));
		addButton(offsetSlider = new NamedSlider(Utils.localize("gui.securitycraft:projector.offset", te.autoBuildOffset).getColoredString(), "", leftPos + width / 2 - widgetOffset, topPos + imageHeight / 2 + yOffset[4], widgetWidth, 20, Utils.localize("gui.securitycraft:projector.offset", "").getColoredString(), "", (-size + 2) / 2, (size - 2) / 2, te.autoBuildOffset, false, true, null, this::offsetSliderReleased));
		offsetSlider.updateSlider();

		if (!te.getOwner().isOwner(Minecraft.getInstance().player))
			sizeButton.active = toggleButton.active = assembleButton.active = outlineButton.active = offsetSlider.active = false;
		else {
			updateMaterialInformation(true);
			sizeButton.active = offsetSlider.active = !te.enabled;
		}

		if (!storage) {
			hoverCheckers[0] = new StackHoverChecker(BLOCK_POCKET_WALL, topPos + 93, topPos + 113, leftPos + 23, leftPos + 43);
			hoverCheckers[1] = new StackHoverChecker(REINFORCED_CRYSTAL_QUARTZ_PILLAR, topPos + 93, topPos + 113, leftPos + 75, leftPos + 95);
			hoverCheckers[2] = new StackHoverChecker(REINFORCED_CHISELED_CRYSTAL_QUARTZ, topPos + 93, topPos + 113, leftPos + 128, leftPos + 148);
		}
		else {
			hoverCheckers[0] = new StackHoverChecker(BLOCK_POCKET_WALL, topPos + imageHeight - 73, topPos + imageHeight - 54, leftPos + 174, leftPos + 191);
			hoverCheckers[1] = new StackHoverChecker(REINFORCED_CRYSTAL_QUARTZ_PILLAR, topPos + imageHeight - 50, topPos + imageHeight - 31, leftPos + 174, leftPos + 191);
			hoverCheckers[2] = new StackHoverChecker(REINFORCED_CHISELED_CRYSTAL_QUARTZ, topPos + imageHeight - 27, topPos + imageHeight - 9, leftPos + 174, leftPos + 191);
		}

		assembleHoverChecker = new StringHoverChecker(assembleButton, Arrays.asList(Utils.localize("gui.securitycraft:blockPocketManager.needStorageModule").getColoredString(), Utils.localize("messages.securitycraft:blockpocket.notEnoughItems").getColoredString()));
	}

	@Override
	protected void renderLabels(int mouseX, int mouseY) {
		font.draw(blockPocketManager, (storage ? 123 : imageWidth) / 2 - font.width(blockPocketManager) / 2, 6, 4210752);

		if (!te.enabled && isOwner) {
			if (!storage) {
				font.draw(youNeed, imageWidth / 2 - font.width(youNeed) / 2, 83, 4210752);

				font.draw(wallsNeededOverall + "", 42, 100, 4210752);
				minecraft.getItemRenderer().renderAndDecorateItem(BLOCK_POCKET_WALL, 25, 96);

				font.draw(pillarsNeededOverall + "", 94, 100, 4210752);
				minecraft.getItemRenderer().renderAndDecorateItem(REINFORCED_CRYSTAL_QUARTZ_PILLAR, 77, 96);

				font.draw(chiseledNeededOverall + "", 147, 100, 4210752);
				minecraft.getItemRenderer().renderAndDecorateItem(REINFORCED_CHISELED_CRYSTAL_QUARTZ, 130, 96);
			}
			else {
				font.draw(youNeed, 169 + 87 / 2 - font.width(youNeed) / 2, imageHeight - 83, 4210752);

				font.draw(Math.max(0, wallsStillNeeded) + "", 192, imageHeight - 66, 4210752);
				minecraft.getItemRenderer().renderAndDecorateItem(BLOCK_POCKET_WALL, 175, imageHeight - 70);

				font.draw(Math.max(0, pillarsStillNeeded) + "", 192, imageHeight - 44, 4210752);
				minecraft.getItemRenderer().renderAndDecorateItem(REINFORCED_CRYSTAL_QUARTZ_PILLAR, 175, imageHeight - 48);

				font.draw(Math.max(0, chiseledStillNeeded) + "", 192, imageHeight - 22, 4210752);
				minecraft.getItemRenderer().renderAndDecorateItem(REINFORCED_CHISELED_CRYSTAL_QUARTZ, 175, imageHeight - 26);
			}
		}

		if (storage) {
			font.draw(inventory.getDisplayName().getColoredString(), 8, imageHeight - 94, 4210752);
			renderTooltip(mouseX - leftPos, mouseY - topPos);
		}
	}

	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		super.render(mouseX, mouseY, partialTicks);

		if (!te.enabled && isOwner) {
			for (StackHoverChecker shc : hoverCheckers) {
				if (shc.checkHover(mouseX, mouseY)) {
					renderTooltip(shc.getStack(), mouseX, mouseY);
					return;
				}
			}
		}

		if (!te.enabled && isOwner && !assembleButton.active && assembleHoverChecker.checkHover(mouseX, mouseY)) {
			if (!storage)
				GuiUtils.drawHoveringText(assembleHoverChecker.getLines().subList(0, 1), mouseX, mouseY, width, height, -1, font);
			else
				GuiUtils.drawHoveringText(assembleHoverChecker.getLines().subList(1, 2), mouseX, mouseY, width, height, -1, font);
		}
	}

	@Override
	protected void renderBg(float partialTicks, int mouseX, int mouseY) {
		renderBackground();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bind(storage ? TEXTURE_STORAGE : TEXTURE);
		blit(leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}

	@Override
	protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
		//the super call needs to be before calculating the stored materials, as it is responsible for putting the stack inside the slot
		super.slotClicked(slot, slotId, mouseButton, type);
		//every time items are added/removed, the mouse is clicking a slot and these values are recomputed
		//not the best place, as this code will run when an empty slot is clicked while not holding any item, but it's good enough
		updateMaterialInformation(true);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (offsetSlider.dragging)
			offsetSlider.mouseReleased(mouseX, mouseY, button);

		return super.mouseReleased(mouseX, mouseY, button);
	}

	private void updateMaterialInformation(boolean recalculateStoredStacks) {
		if (recalculateStoredStacks) {
			materialCounts[0] = materialCounts[1] = materialCounts[2] = 0;

			te.getStorageHandler().ifPresent(handler -> {
				for (int i = 0; i < handler.getSlots(); i++) {
					ItemStack stack = handler.getStackInSlot(i);

					if (stack.getItem() instanceof BlockItem) {
						Block block = ((BlockItem) stack.getItem()).getBlock();

						if (block == SCContent.BLOCK_POCKET_WALL.get())
							materialCounts[0] += stack.getCount();
						else if (block == SCContent.REINFORCED_CRYSTAL_QUARTZ_PILLAR.get())
							materialCounts[1] += stack.getCount();
						else if (block == SCContent.REINFORCED_CHISELED_CRYSTAL_QUARTZ.get())
							materialCounts[2] += stack.getCount();
					}
				}
			});
		}

		wallsNeededOverall = (size - 2) * (size - 2) * 6;
		pillarsNeededOverall = (size - 2) * 12 - 1;
		wallsStillNeeded = wallsNeededOverall - materialCounts[0];
		pillarsStillNeeded = pillarsNeededOverall - materialCounts[1];
		chiseledStillNeeded = chiseledNeededOverall - materialCounts[2];
		//the assemble button should always be active when the player is in creative mode
		assembleButton.active = isOwner && (minecraft.player.isCreative() || (!te.enabled && storage && wallsStillNeeded <= 0 && pillarsStillNeeded <= 0 && chiseledStillNeeded <= 0));
	}

	public void toggleButtonClicked(Button button) {
		if (te.enabled)
			te.disableMultiblock();
		else {
			TranslationTextComponent feedback;

			te.size = size;
			feedback = te.enableMultiblock();

			if (feedback != null)
				PlayerUtils.sendMessageToPlayer(Minecraft.getInstance().player, Utils.localize(SCContent.BLOCK_POCKET_MANAGER.get().getDescriptionId()), feedback, TextFormatting.DARK_AQUA, true);
		}

		Minecraft.getInstance().player.closeContainer();
	}

	public void sizeButtonClicked(Button button) {
		int newOffset;
		int newMin;
		int newMax;

		size += 4;

		if (size > 25)
			size = 5;

		newMin = (-size + 2) / 2;
		newMax = (size - 2) / 2;

		if (te.autoBuildOffset > 0)
			newOffset = Math.min(te.autoBuildOffset, newMax);
		else
			newOffset = Math.max(te.autoBuildOffset, newMin);

		updateMaterialInformation(false);
		te.size = size;
		offsetSlider.minValue = newMin;
		offsetSlider.maxValue = newMax;
		te.autoBuildOffset = newOffset;
		offsetSlider.setValue(newOffset);
		offsetSlider.updateSlider();
		button.setMessage(Utils.localize("gui.securitycraft:blockPocketManager.size", size, size, size).getColoredString());
		sync();
	}

	public void assembleButtonClicked(Button button) {
		ITextComponent feedback;

		te.size = size;
		feedback = te.autoAssembleMultiblock();

		if (feedback != null)
			PlayerUtils.sendMessageToPlayer(Minecraft.getInstance().player, Utils.localize(SCContent.BLOCK_POCKET_MANAGER.get().getDescriptionId()), feedback, TextFormatting.DARK_AQUA, true);

		Minecraft.getInstance().player.closeContainer();
	}

	public void outlineButtonClicked(Button button) {
		te.toggleOutline();
		outlineButton.setMessage(Utils.localize("gui.securitycraft:blockPocketManager.outline." + (!te.showOutline ? "show" : "hide")).getColoredString());
		sync();
	}

	public void offsetSliderReleased(Slider slider) {
		te.autoBuildOffset = slider.getValueInt();
		sync();
	}

	private void sync() {
		SecurityCraft.channel.send(PacketDistributor.SERVER.noArg(), new SyncBlockPocketManager(te.getBlockPos(), te.size, te.showOutline, te.autoBuildOffset));
	}
}
