package net.geforcemods.securitycraft.util;

import java.util.ArrayList;
import java.util.List;

import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.LinkableBlockEntity;
import net.geforcemods.securitycraft.api.LinkedAction;
import net.geforcemods.securitycraft.items.ModuleItem;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ModuleUtils {
	public static List<String> getPlayersFromModule(ItemStack stack) {
		List<String> list = new ArrayList<>();

		if (stack.getItem() instanceof ModuleItem) {
			for (int i = 1; i <= ModuleItem.MAX_PLAYERS; i++) {
				if (stack.getTag() != null && stack.getTag().getString("Player" + i) != null && !stack.getTag().getString("Player" + i).isEmpty())
					list.add(stack.getTag().getString("Player" + i).toLowerCase());
			}
		}

		return list;
	}

	public static boolean isAllowed(IModuleInventory inv, Entity entity) {
		return isAllowed(inv, entity.getName().getString());
	}

	public static boolean isAllowed(IModuleInventory inv, String name) {
		if (!inv.isModuleEnabled(ModuleType.ALLOWLIST))
			return false;

		ItemStack stack = inv.getModule(ModuleType.ALLOWLIST);

		if (stack.hasTag() && stack.getTag().getBoolean("affectEveryone"))
			return true;

		//IModuleInventory#getModule returns ItemStack.EMPTY when the module does not exist, and getPlayersFromModule will then have an empty list
		return getPlayersFromModule(stack).contains(name.toLowerCase());
	}

	public static boolean isDenied(IModuleInventory inv, Entity entity) {
		if (!inv.isModuleEnabled(ModuleType.DENYLIST))
			return false;

		ItemStack stack = inv.getModule(ModuleType.DENYLIST);

		if (stack.hasTag() && stack.getTag().getBoolean("affectEveryone")) {
			if (inv.getBlockEntity() instanceof IOwnable ownable) {
				//only deny players that are not the owner
				if (entity instanceof Player player) {
					//if the player IS the owner, fall back to the default handling (check if the name is on the list)
					if (!ownable.getOwner().isOwner(player))
						return true;
				}
				else
					return true;
			}
			else
				return true;
		}

		//IModuleInventory#getModule returns ItemStack.EMPTY when the module does not exist, and getPlayersFromModule will then have an empty list
		return getPlayersFromModule(stack).contains(entity.getName().getString().toLowerCase());
	}

	public static void createLinkedAction(LinkedAction action, ItemStack stack, LinkableBlockEntity be) {
		if (action == LinkedAction.MODULE_INSERTED) {
			be.createLinkedBlockAction(action, new Object[] {
					stack, (ModuleItem) stack.getItem()
			}, be);
		}
		else if (action == LinkedAction.MODULE_REMOVED) {
			be.createLinkedBlockAction(action, new Object[] {
					stack, ((ModuleItem) stack.getItem()).getModuleType()
			}, be);
		}
		else if (action == LinkedAction.MODULE_ENABLED) {
			be.createLinkedBlockAction(action, new Object[] {
					stack, (ModuleItem) stack.getItem()
			}, be);
		}
		else if (action == LinkedAction.MODULE_DISABLED) {
			be.createLinkedBlockAction(action, new Object[] {
					stack, ((ModuleItem) stack.getItem()).getModuleType()
			}, be);
		}
	}
}