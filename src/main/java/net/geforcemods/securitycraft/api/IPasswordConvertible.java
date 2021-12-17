package net.geforcemods.securitycraft.api;

import net.geforcemods.securitycraft.blocks.KeypadChestBlock;
import net.geforcemods.securitycraft.blocks.KeypadFurnaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Defines a block that can be converted to a password-protected variant by rightclicking it with a Key Panel. Call
 * 
 * <pre>
 * InterModComms.sendTo("securitycraft", SecurityCraftAPI.IMC_PASSWORD_CONVERTIBLE_MSG, ClassThatImplementsIPasswordConvertible::new);
 * </pre>
 * 
 * during InterModEnqueueEvent to register this with SecurityCraft. <p> - If you are converting a chest into a
 * password-protected chest, you can extend {@link KeypadChestBlock.Convertible}.<br> - If you are converting a furnace into
 * a password-protected furnace, you can extend {@link KeypadFurnaceBlock.Convertible}.
 *
 * @author bl4ckscor3
 */
public interface IPasswordConvertible {
	/**
	 * The block that has to be rightclicked in order to convert it
	 * 
	 * @return Aforementioned block
	 */
	public Block getOriginalBlock();

	/**
	 * Converts the original block to the password-protected one
	 * 
	 * @param player The player who initiated the conversion
	 * @param level The level in which the conversion takes place
	 * @param pos The position the conversaion takes place at
	 * @return true if the conversion was successful, false otherwise
	 */
	public boolean convert(Player player, Level level, BlockPos pos);
}
