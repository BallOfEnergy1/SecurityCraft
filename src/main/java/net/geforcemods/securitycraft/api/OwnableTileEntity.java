package net.geforcemods.securitycraft.api;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Used to give this tile entity an owner
 */
public class OwnableTileEntity extends TileEntity implements IOwnable {
	private Owner owner = new Owner();

	public OwnableTileEntity() {
		this(SCContent.teTypeOwnable);
	}

	public OwnableTileEntity(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		super.save(tag);

		if (owner != null)
			owner.write(tag, needsValidation());

		return tag;
	}

	@Override
	public void load(BlockState state, CompoundNBT tag) {
		super.load(state, tag);

		owner.read(tag);
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return save(new CompoundNBT());
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(worldPosition, 1, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
		load(getBlockState(), packet.getTag());
	}

	@Override
	public Owner getOwner() {
		return owner;
	}

	@Override
	public void setOwner(String uuid, String name) {
		owner.set(uuid, name);
	}
}
