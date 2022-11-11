package net.geforcemods.securitycraft.screen.components;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraftforge.fml.client.gui.widget.Slider;

public class HoverChecker {
	private int top;
	private int bottom;
	private int left;
	private int right;
	private Widget widget;

	public HoverChecker(int top, int bottom, int left, int right) {
		this.top = top;
		this.bottom = bottom;
		this.left = left;
		this.right = right;
	}

	public HoverChecker(Widget widget) {
		this(widget.x, widget.x + widget.getHeight(), widget.y, widget.y + widget.getWidth());

		this.widget = widget;
	}

	public boolean checkHover(double mouseX, double mouseY, IGuiEventListener currentlyFocused) {
		if (widget != null) {
			if (!widget.visible || (widget instanceof Slider && ((Slider) widget).dragging))
				return false;
			else
				return widget.isHovered() && !(widget.isHovered && widget != currentlyFocused);
		}
		else
			return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
	}
}
