package net.geforcemods.securitycraft.screen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IExplosive;
import net.geforcemods.securitycraft.network.server.RemoteControlMine;
import net.geforcemods.securitycraft.network.server.UpdateNBTTagOnServer;
import net.geforcemods.securitycraft.screen.components.PictureButton;
import net.geforcemods.securitycraft.screen.components.TextHoverChecker;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

@OnlyIn(Dist.CLIENT)
public class MineRemoteAccessToolScreen extends Screen {
	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/mrat.png");
	private static final ResourceLocation INFO_BOOK_ICONS = new ResourceLocation("securitycraft:textures/gui/info_book_icons.png"); //for the explosion icon
	private final TranslationTextComponent mratName = Utils.localize(SCContent.REMOTE_ACCESS_MINE.get().getTranslationKey());
	private ItemStack mrat;
	private Button[][] guiButtons = new Button[6][4]; //6 mines, 4 actions (defuse, prime, detonate, unbind)
	private static final int DEFUSE = 0, ACTIVATE = 1, DETONATE = 2, UNBIND = 3;
	private int xSize = 256, ySize = 184;
	private List<TextHoverChecker> hoverCheckers = new ArrayList<>();
	private final TranslationTextComponent notBound = Utils.localize("gui.securitycraft:mrat.notBound");
	private final TranslationTextComponent[] lines = new TranslationTextComponent[6];
	private final int[] lengths = new int[6];

	public MineRemoteAccessToolScreen(ItemStack item) {
		super(new TranslationTextComponent(item.getTranslationKey()));

		mrat = item;
	}

	@Override
	public void init() {
		super.init();

		int padding = 25;
		int y = padding;
		int[] coords = null;
		int id = 0;

		hoverCheckers.clear();

		for (int i = 0; i < 6; i++) {
			y += 30;
			coords = getMineCoordinates(i);
			int startX = (width - xSize) / 2;
			int startY = (height - ySize) / 2;

			// initialize buttons
			for (int j = 0; j < 4; j++) {
				int btnX = startX + j * padding + 154;
				int btnY = startY + y - 48;
				int mine = id / 4;
				int action = id % 4;

				id++;

				switch (j) {
					case DEFUSE:
						guiButtons[i][j] = new PictureButton(btnX, btnY, 20, 20, itemRenderer, new ItemStack(SCContent.WIRE_CUTTERS.get()), b -> buttonClicked(mine, action));
						guiButtons[i][j].active = false;
						break;
					case ACTIVATE:
						guiButtons[i][j] = new PictureButton(btnX, btnY, 20, 20, itemRenderer, new ItemStack(Items.FLINT_AND_STEEL), b -> buttonClicked(mine, action));
						guiButtons[i][j].active = false;
						break;
					case DETONATE:
						guiButtons[i][j] = new PictureButton(btnX, btnY, 20, 20, INFO_BOOK_ICONS, 54, 1, 0, 1, 18, 18, 256, 256, b -> buttonClicked(mine, action));
						guiButtons[i][j].active = false;
						break;
					case UNBIND:
						guiButtons[i][j] = new ExtendedButton(btnX, btnY, 20, 20, new StringTextComponent("X"), b -> buttonClicked(mine, action));
						guiButtons[i][j].active = false;
						break;
				}

				addButton(guiButtons[i][j]);
			}

			BlockPos minePos = new BlockPos(coords[0], coords[1], coords[2]);

			if (!(coords[0] == 0 && coords[1] == 0 && coords[2] == 0)) {
				guiButtons[i][UNBIND].active = true;

				if (Minecraft.getInstance().player.world.isBlockPresent(minePos)) {
					Block block = minecraft.world.getBlockState(minePos).getBlock();

					if (block instanceof IExplosive) {
						boolean active = ((IExplosive) block).isActive(minecraft.world, minePos);
						boolean defusable = ((IExplosive) block).isDefusable();

						guiButtons[i][DEFUSE].active = active && defusable;
						guiButtons[i][ACTIVATE].active = !active && defusable;
						guiButtons[i][DETONATE].active = active;
						hoverCheckers.add(new TextHoverChecker(guiButtons[i][DEFUSE], Utils.localize("gui.securitycraft:mrat.defuse")));
						hoverCheckers.add(new TextHoverChecker(guiButtons[i][ACTIVATE], Utils.localize("gui.securitycraft:mrat.activate")));
						hoverCheckers.add(new TextHoverChecker(guiButtons[i][DETONATE], Utils.localize("gui.securitycraft:mrat.detonate")));
						hoverCheckers.add(new TextHoverChecker(guiButtons[i][UNBIND], Utils.localize("gui.securitycraft:mrat.unbind")));
					}
					else {
						removeTagFromToolAndUpdate(mrat, coords[0], coords[1], coords[2]);

						for (int j = 0; j < 4; j++) {
							guiButtons[i][j].active = false;
						}
					}
				}
				else {
					for (int j = 0; j < 3; j++) {
						hoverCheckers.add(new TextHoverChecker(guiButtons[i][j], Utils.localize("gui.securitycraft:mrat.outOfRange")));
					}

					hoverCheckers.add(new TextHoverChecker(guiButtons[i][UNBIND], Utils.localize("gui.securitycraft:mrat.unbind")));
				}
			}

			if (coords[0] == 0 && coords[1] == 0 && coords[2] == 0)
				lines[i] = notBound;
			else
				lines[i] = Utils.localize("gui.securitycraft:mrat.mineLocations", new BlockPos(coords[0], coords[1], coords[2]));

			lengths[i] = font.getStringPropertyWidth(lines[i]);
		}
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		renderBackground(matrix);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(TEXTURE);
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;
		this.blit(matrix, startX, startY, 0, 0, xSize, ySize);
		super.render(matrix, mouseX, mouseY, partialTicks);
		font.drawText(matrix, mratName, startX + xSize / 2 - font.getStringPropertyWidth(mratName), startY + -25 + 13, 0xFF0000);

		for (int i = 0; i < 6; i++) {
			font.drawText(matrix, lines[i], startX + xSize / 2 - lengths[i] + 25, startY + i * 30 + 13, 4210752);
		}

		for (TextHoverChecker chc : hoverCheckers) {
			if (chc != null && chc.checkHover(mouseX, mouseY) && chc.getName() != null)
				func_243308_b(matrix, chc.getLines(), mouseX, mouseY);
		}
	}

	private void buttonClicked(int mine, int action) {
		int[] coords = getMineCoordinates(mine);

		switch (action) {
			case DEFUSE:
				((IExplosive) Minecraft.getInstance().player.world.getBlockState(new BlockPos(coords[0], coords[1], coords[2])).getBlock()).defuseMine(Minecraft.getInstance().player.world, new BlockPos(coords[0], coords[1], coords[2]));
				SecurityCraft.channel.sendToServer(new RemoteControlMine(coords[0], coords[1], coords[2], "defuse"));
				guiButtons[mine][DEFUSE].active = false;
				guiButtons[mine][ACTIVATE].active = true;
				guiButtons[mine][DETONATE].active = false;
				break;
			case ACTIVATE:
				((IExplosive) Minecraft.getInstance().player.world.getBlockState(new BlockPos(coords[0], coords[1], coords[2])).getBlock()).activateMine(Minecraft.getInstance().player.world, new BlockPos(coords[0], coords[1], coords[2]));
				SecurityCraft.channel.sendToServer(new RemoteControlMine(coords[0], coords[1], coords[2], "activate"));
				guiButtons[mine][DEFUSE].active = true;
				guiButtons[mine][ACTIVATE].active = false;
				guiButtons[mine][DETONATE].active = true;
				break;
			case DETONATE:
				SecurityCraft.channel.sendToServer(new RemoteControlMine(coords[0], coords[1], coords[2], "detonate"));
				removeTagFromToolAndUpdate(mrat, coords[0], coords[1], coords[2]);

				for (int i = 0; i < 4; i++) {
					guiButtons[mine][i].active = false;
				}

				break;
			case UNBIND:
				removeTagFromToolAndUpdate(mrat, coords[0], coords[1], coords[2]);

				for (int i = 0; i < 4; i++) {
					guiButtons[mine][i].active = false;
				}
		}
	}

	/**
	 * @param mine 0 based
	 */
	private int[] getMineCoordinates(int mine) {
		mine++; //mines are stored starting by mine1 up to mine6

		if (mrat.getItem() != null && mrat.getItem() == SCContent.REMOTE_ACCESS_MINE.get() && mrat.getTag() != null && mrat.getTag().getIntArray("mine" + mine) != null && mrat.getTag().getIntArray("mine" + mine).length > 0)
			return mrat.getTag().getIntArray("mine" + mine);

		return new int[] {
				0, 0, 0
		};
	}

	private void removeTagFromToolAndUpdate(ItemStack stack, int x, int y, int z) {
		if (stack.getTag() == null)
			return;

		for (int i = 1; i <= 6; i++) {
			if (stack.getTag().getIntArray("mine" + i).length > 0) {
				int[] coords = stack.getTag().getIntArray("mine" + i);

				if (coords[0] == x && coords[1] == y && coords[2] == z && !(coords[0] == 0 && coords[1] == 0 && coords[2] == 0)) {
					stack.getTag().putIntArray("mine" + i, new int[] {
							0, 0, 0
					});
					SecurityCraft.channel.sendToServer(new UpdateNBTTagOnServer(stack));
					return;
				}
			}
		}
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (minecraft.gameSettings.keyBindInventory.isActiveAndMatches(InputMappings.getInputByCode(p_keyPressed_1_, p_keyPressed_2_))) {
			this.onClose();
			return true;
		}

		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}
}
