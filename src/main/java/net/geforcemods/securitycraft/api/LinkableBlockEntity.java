package net.geforcemods.securitycraft.api;

import java.util.ArrayList;

import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.LevelUtils;
import net.geforcemods.securitycraft.util.ModuleUtils;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

public abstract class LinkableBlockEntity extends CustomizableBlockEntity implements ITickableTileEntity {
	public ArrayList<LinkedBlock> linkedBlocks = new ArrayList<>();
	private ListNBT nbtTagStorage = null;

	public LinkableBlockEntity(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void tick() {
		if (hasLevel() && nbtTagStorage != null) {
			readLinkedBlocks(nbtTagStorage);
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
			nbtTagStorage = null;
		}
	}

	@Override
	public void load(CompoundNBT tag) {
		super.load(tag);

		if (tag.contains("linkedBlocks")) {
			if (!hasLevel()) {
				nbtTagStorage = tag.getList("linkedBlocks", Constants.NBT.TAG_COMPOUND);
				return;
			}

			readLinkedBlocks(tag.getList("linkedBlocks", Constants.NBT.TAG_COMPOUND));
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		super.save(tag);

		if (hasLevel() && linkedBlocks.size() > 0) {
			ListNBT tagList = new ListNBT();

			LevelUtils.addScheduledTask(level, () -> {
				for (int i = linkedBlocks.size() - 1; i >= 0; i--) {
					LinkedBlock block = linkedBlocks.get(i);
					CompoundNBT toAppend = new CompoundNBT();

					if (block != null) {
						if (!block.validate(level)) {
							linkedBlocks.remove(i);
							continue;
						}

						toAppend.putString("blockName", block.blockName);
						toAppend.putInt("blockX", block.getX());
						toAppend.putInt("blockY", block.getY());
						toAppend.putInt("blockZ", block.getZ());
					}

					tagList.add(toAppend);
				}

				tag.put("linkedBlocks", tagList);
			});
		}

		return tag;
	}

	private void readLinkedBlocks(ListNBT list) {
		for (int i = 0; i < list.size(); i++) {
			String name = list.getCompound(i).getString("blockName");
			int x = list.getCompound(i).getInt("blockX");
			int y = list.getCompound(i).getInt("blockY");
			int z = list.getCompound(i).getInt("blockZ");

			LinkedBlock block = new LinkedBlock(name, new BlockPos(x, y, z));
			if (hasLevel() && !block.validate(level)) {
				list.remove(i);
				continue;
			}

			if (!linkedBlocks.contains(block))
				link(this, block.asTileEntity(level));
		}
	}

	@Override
	public void setRemoved() {
		for (LinkedBlock block : linkedBlocks) {
			LinkableBlockEntity.unlink(block.asTileEntity(level), this);
		}
	}

	@Override
	public void onModuleInserted(ItemStack stack, ModuleType module) {
		super.onModuleInserted(stack, module);
		ModuleUtils.createLinkedAction(LinkedAction.MODULE_INSERTED, stack, this);
	}

	@Override
	public void onModuleRemoved(ItemStack stack, ModuleType module) {
		super.onModuleRemoved(stack, module);
		ModuleUtils.createLinkedAction(LinkedAction.MODULE_REMOVED, stack, this);
	}

	@Override
	public void onOptionChanged(Option<?> option) {
		createLinkedBlockAction(LinkedAction.OPTION_CHANGED, new Option[] {
				option
		}, this);
	}

	/**
	 * Links two blocks together. Calls onLinkedBlockAction() whenever certain events (found in {@link LinkedAction}) occur.
	 */
	public static void link(LinkableBlockEntity tileEntity1, LinkableBlockEntity tileEntity2) {
		if (isLinkedWith(tileEntity1, tileEntity2))
			return;

		LinkedBlock block1 = new LinkedBlock(tileEntity1);
		LinkedBlock block2 = new LinkedBlock(tileEntity2);

		if (!tileEntity1.linkedBlocks.contains(block2))
			tileEntity1.linkedBlocks.add(block2);

		if (!tileEntity2.linkedBlocks.contains(block1))
			tileEntity2.linkedBlocks.add(block1);
	}

	/**
	 * Unlinks the second tile entity from the first.
	 *
	 * @param tileEntity1 The tile entity to unlink from
	 * @param tileEntity2 The tile entity to unlink
	 */
	public static void unlink(LinkableBlockEntity tileEntity1, LinkableBlockEntity tileEntity2) {
		if (tileEntity1 == null || tileEntity2 == null)
			return;

		LinkedBlock block = new LinkedBlock(tileEntity2);

		if (tileEntity1.linkedBlocks.contains(block))
			tileEntity1.linkedBlocks.remove(block);
	}

	/**
	 * @return Are the two blocks linked together?
	 */
	public static boolean isLinkedWith(LinkableBlockEntity tileEntity1, LinkableBlockEntity tileEntity2) {
		return tileEntity1.linkedBlocks.contains(new LinkedBlock(tileEntity2)) && tileEntity2.linkedBlocks.contains(new LinkedBlock(tileEntity1));
	}

	/**
	 * Calls onLinkedBlockAction() for every block this tile entity is linked to. <p> <b>NOTE:</b> Never use this method in
	 * onLinkedBlockAction(), use createLinkedBlockAction(EnumLinkedAction, Object[], ArrayList[LinkableTileEntity] instead.
	 *
	 * @param action The action that occurred
	 * @param parameters Action-specific parameters, see comments in {@link LinkedAction}
	 * @param excludedTE The LinkableTileEntity which called this method, prevents infinite loops.
	 */
	public void createLinkedBlockAction(LinkedAction action, Object[] parameters, LinkableBlockEntity excludedTE) {
		ArrayList<LinkableBlockEntity> list = new ArrayList<>();

		list.add(excludedTE);
		createLinkedBlockAction(action, parameters, list);
	}

	/**
	 * Calls onLinkedBlockAction() for every block this TileEntity is linked to.
	 *
	 * @param action The action that occurred
	 * @param parameters Action-specific parameters, see comments in {@link LinkedAction}
	 * @param excludedTEs LinkableTileEntities that shouldn't have onLinkedBlockAction() called on them, prevents infinite
	 *            loops. Always add your tile entity to the list whenever using this method
	 */
	public void createLinkedBlockAction(LinkedAction action, Object[] parameters, ArrayList<LinkableBlockEntity> excludedTEs) {
		for (LinkedBlock block : linkedBlocks)
			if (excludedTEs.contains(block.asTileEntity(level)))
				continue;
			else {
				BlockState state = level.getBlockState(block.blockPos);

				block.asTileEntity(level).onLinkedBlockAction(action, parameters, excludedTEs);
				level.sendBlockUpdated(worldPosition, state, state, 3);
			}
	}

	/**
	 * Called whenever certain actions occur in blocks this tile entity is linked to. See {@link LinkedAction} for parameter
	 * descriptions. <p>
	 *
	 * @param action The {@link LinkedAction} that occurred
	 * @param parameters Important variables related to the action
	 * @param excludedTEs LinkableTileEntities that aren't going to have onLinkedBlockAction() called on them, always add
	 *            your tile entity to the list if you're going to call createLinkedBlockAction() in this method to chain-link
	 *            multiple blocks (i.e: like Laser Blocks)
	 */
	protected void onLinkedBlockAction(LinkedAction action, Object[] parameters, ArrayList<LinkableBlockEntity> excludedTEs) {}
}
