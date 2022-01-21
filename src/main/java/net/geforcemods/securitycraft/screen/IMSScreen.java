package net.geforcemods.securitycraft.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.containers.GenericTEContainer;
import net.geforcemods.securitycraft.network.server.SyncIMSTargetingOption;
import net.geforcemods.securitycraft.tileentity.IMSTileEntity;
import net.geforcemods.securitycraft.tileentity.IMSTileEntity.IMSTargetingMode;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

@OnlyIn(Dist.CLIENT)
public class IMSScreen extends ContainerScreen<GenericTEContainer> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/blank.png");
	private final TranslationTextComponent imsName = Utils.localize(SCContent.IMS.get().getTranslationKey());
	private final TranslationTextComponent target = Utils.localize("gui.securitycraft:ims.target");
	private IMSTileEntity tileEntity;
	private Button targetButton;
	private IMSTargetingMode targetMode;

	public IMSScreen(GenericTEContainer container, PlayerInventory inv, ITextComponent name) {
		super(container, inv, name);
		tileEntity = (IMSTileEntity) container.te;
		targetMode = tileEntity.getTargetingMode();
	}

	@Override
	public void init() {
		super.init();

		addButton(targetButton = new ExtendedButton(width / 2 - 75, height / 2 - 38, 150, 20, StringTextComponent.EMPTY, this::targetButtonClicked));
		updateButtonText();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrix, int mouseX, int mouseY) {
		font.drawText(matrix, imsName, xSize / 2 - font.getStringPropertyWidth(imsName) / 2, 6, 4210752);
		font.drawText(matrix, target, xSize / 2 - font.getStringPropertyWidth(target) / 2, 30, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int mouseX, int mouseY) {
		renderBackground(matrix);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(TEXTURE);
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;
		this.blit(matrix, startX, startY, 0, 0, xSize, ySize);
	}

	protected void targetButtonClicked(Button button) {
		targetMode = IMSTargetingMode.values()[(targetMode.ordinal() + 1) % IMSTargetingMode.values().length]; //next enum value
		tileEntity.setTargetingMode(targetMode);
		SecurityCraft.channel.sendToServer(new SyncIMSTargetingOption(tileEntity.getPos(), tileEntity.getTargetingMode()));
		updateButtonText();
	}

	private void updateButtonText() {
		targetButton.setMessage(Utils.localize("gui.securitycraft:srat.targets" + (((targetMode.ordinal() + 2) % 3) + 1)));
	}
}
