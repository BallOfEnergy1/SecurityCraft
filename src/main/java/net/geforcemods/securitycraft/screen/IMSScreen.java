package net.geforcemods.securitycraft.screen;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

@OnlyIn(Dist.CLIENT)
public class IMSScreen extends ContainerScreen<GenericTEContainer> {
	private static final ResourceLocation TEXTURE = new ResourceLocation("securitycraft:textures/gui/container/blank.png");
	private final String imsName = Utils.localize(SCContent.IMS.get().getTranslationKey()).getFormattedText();
	private final String target = Utils.localize("gui.securitycraft:ims.target").getFormattedText();
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

		addButton(targetButton = new ExtendedButton(width / 2 - 75, height / 2 - 38, 150, 20, "", this::targetButtonClicked));
		updateButtonText();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		font.drawString(imsName, xSize / 2 - font.getStringWidth(imsName) / 2, 6, 4210752);
		font.drawString(target, xSize / 2 - font.getStringWidth(target) / 2, 30, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		renderBackground();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		minecraft.getTextureManager().bindTexture(TEXTURE);
		int startX = (width - xSize) / 2;
		int startY = (height - ySize) / 2;
		this.blit(startX, startY, 0, 0, xSize, ySize);
	}

	protected void targetButtonClicked(Button button) {
		targetMode = IMSTargetingMode.values()[(targetMode.ordinal() + 1) % IMSTargetingMode.values().length]; //next enum value
		tileEntity.setTargetingMode(targetMode);
		SecurityCraft.channel.sendToServer(new SyncIMSTargetingOption(tileEntity.getPos(), tileEntity.getTargetingMode()));
		updateButtonText();
	}

	private void updateButtonText() {
		System.out.println(targetMode);
		targetButton.setMessage(Utils.localize("gui.securitycraft:srat.targets" + (((targetMode.ordinal() + 2) % 3) + 1)).getFormattedText());
	}
}
