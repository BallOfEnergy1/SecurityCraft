/*
 * Minecraft Forge Copyright (c) 2016. This library is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software Foundation version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public License along with this library; if not, write
 * to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */
package net.geforcemods.securitycraft.gui.components;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.client.config.GuiButtonExt;

/**
 * This class is blatantly stolen from iChunUtils with permission. and blatantly edited by bl4ckscor3 to fit SC's needs
 *
 * @author iChun
 */
public class GuiSlider extends GuiButtonExt {
	/** The value of this slider control. */
	public double sliderValue;
	/** Is this slider control being dragged. */
	public boolean dragging = false;
	public boolean showDecimal = true;
	public double minValue = 0.0D;
	public double maxValue = 5.0D;
	public int precision = 1;
	@Nullable
	public ISlider parent = null;
	public boolean drawString = true;
	private String blockName;
	public String prefix;

	public GuiSlider(String initialString, String bN, int id, int xPos, int yPos, int width, int height, String prefix, int minVal, int maxVal, int currentVal, boolean showDec, boolean drawStr, @Nullable ISlider par) {
		this(initialString, bN, id, xPos, yPos, width, height, prefix, (double) minVal, (double) maxVal, (double) currentVal, showDec, drawStr, par);
	}

	public GuiSlider(String initialString, String bN, int id, int xPos, int yPos, int width, int height, String prefix, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, @Nullable ISlider par) {
		super(id, xPos, yPos, width, height, prefix);
		minValue = minVal;
		maxValue = maxVal;
		parent = par;
		showDecimal = showDec;
		blockName = bN;
		String val;
		sliderValue = (currentVal - minVal) / (maxVal - minVal);
		this.prefix = prefix;

		if (showDecimal) {
			val = Double.toString(getValue());
			precision = Math.min(val.substring(val.indexOf('.') + 1).length(), 4);
		}
		else {
			val = Integer.toString(getValueInt());
			precision = 0;
		}

		displayString = initialString;
		drawString = drawStr;

		if (!drawString)
			displayString = "";
	}

	@Override
	public int getHoverState(boolean par1) {
		return 0;
	}

	@Override
	protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
		if (visible) {
			if (dragging) {
				sliderValue = (mouseX - (x + 4)) / (double) (width - 8);
				updateSlider();
			}

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			drawTexturedModalRect(x + (int) (sliderValue * (width - 8)), y, 0, 66, 4, 20);
			drawTexturedModalRect(x + (int) (sliderValue * (width - 8)) + 4, y, 196, 66, 4, 20);
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (super.mousePressed(mc, mouseX, mouseY)) {
			sliderValue = (double) (mouseX - (x + 4)) / (double) (width - 8);
			updateSlider();
			dragging = true;
			return true;
		}
		else
			return false;
	}

	public void updateSlider() {
		if (sliderValue < 0.0F)
			sliderValue = 0.0F;

		if (sliderValue > 1.0F)
			sliderValue = 1.0F;

		String val;

		if (showDecimal) {
			val = Double.toString(getValue());

			if (val.substring(val.indexOf('.') + 1).length() > precision) {
				val = val.substring(0, val.indexOf('.') + precision + 1);

				if (val.endsWith("."))
					val = val.substring(0, val.indexOf('.') + precision);
			}
			else {
				while (val.substring(val.indexOf('.') + 1).length() < precision) {
					val = val + "0";
				}
			}
		}
		else {
			val = Integer.toString(getValueInt());
		}

		if (parent != null)
			parent.onChangeSliderValue(this, blockName, id);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		dragging = false;

		if (parent != null)
			parent.onMouseRelease(id);
	}

	public int getValueInt() {
		return (int) Math.round(getValue());
	}

	public double getValue() {
		return sliderValue * (maxValue - minValue) + minValue;
	}

	public void setValue(double newValue) {
		sliderValue = (newValue - minValue) / (maxValue - minValue);
	}

	public static interface ISlider {
		void onChangeSliderValue(GuiSlider slider, String blockName, int id);

		void onMouseRelease(int id);
	}
}
