package ffba04.blockhologram.placement;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPurpurSlab;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// This class is a mess, and can probably be done a lot better. Feel free to submit a PR, because I don't want to open this class ever again.
public class SlabBlockPlacement extends MetadataBlockPlacement {

	@SuppressWarnings("deprecation")
	@Override
	public BlockPos getPosition(World world, BlockPos pos, IBlockState state, ItemStack stack, EnumFacing facing,
			EntityPlayer player, float hitX, float hitY, float hitZ) {
		EnumFacing offset = null;

		if (!(state.getBlock() instanceof BlockSlab) || ((BlockSlab) state.getBlock()).isDouble()) {
			pos = pos.offset(facing);
			state = world.getBlockState(pos);
			offset = facing;

			if (!(state.getBlock() instanceof BlockSlab)) {
				return super.getPosition(world, pos, state, stack, facing, player, hitX, hitY, hitZ);
			}
		}

		BlockSlab slab = (BlockSlab) state.getBlock();
		EnumBlockHalf half = state.getValue(BlockSlab.HALF);

		if (slab instanceof BlockStoneSlab) {
			Block stackBlock = getBlock(stack);

			if (!(stackBlock instanceof BlockStoneSlab)) {
				if (offset == null) {
					return super.getPosition(world, pos, state, stack, facing, player, hitX, hitY, hitZ);
				}

				return null;
			}

			BlockStoneSlab.EnumType blockVariant = state.getValue(BlockStoneSlab.VARIANT);
			BlockStoneSlab.EnumType stackVariant = stackBlock.getStateFromMeta(getMetadata(stack))
					.getValue(BlockStoneSlab.VARIANT);

			if (blockVariant != stackVariant) {
				if (offset == null) {
					return super.getPosition(world, pos, state, stack, facing, player, hitX, hitY, hitZ);
				}

				return null;
			}
		} else if (slab instanceof BlockPurpurSlab) {
			Block stackBlock = getBlock(stack);

			if (!(stackBlock instanceof BlockPurpurSlab)) {
				if (offset == null) {
					return super.getPosition(world, pos, state, stack, facing, player, hitX, hitY, hitZ);
				}

				return null;
			}

			BlockPurpurSlab.Variant blockVariant = state.getValue(BlockPurpurSlab.VARIANT);
			BlockPurpurSlab.Variant stackVariant = stackBlock.getStateFromMeta(getMetadata(stack))
					.getValue(BlockPurpurSlab.VARIANT);

			if (blockVariant != stackVariant) {
				if (offset == null) {
					return super.getPosition(world, pos, state, stack, facing, player, hitX, hitY, hitZ);
				}

				return null;
			}
		}

		if (half == EnumBlockHalf.BOTTOM) {
			if (facing == EnumFacing.UP && offset == null) {
				return pos;
			} else if (facing == EnumFacing.DOWN && offset == EnumFacing.DOWN) {
				return pos;
			} else if (hitY >= 0.5F) {
				return pos;
			}
		} else if (half == EnumBlockHalf.TOP) {
			if (facing == EnumFacing.DOWN && offset == null) {
				return pos;
			} else if (facing == EnumFacing.UP && offset == EnumFacing.UP) {
				return pos;
			} else if (hitY <= 0.5F) {
				return pos;
			}
		}

		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState placeBlockInWorld(World world, BlockPos pos, EntityPlayer player, IBlockState state,
			IBlockState current, ItemStack stack, EnumFacing facing, int metadata, float hitX, float hitY, float hitZ) {
		if (!(current.getBlock() instanceof BlockSlab)) {
			return super.placeBlockInWorld(world, pos, player, state, current, stack, facing, metadata, hitX, hitY,
					hitZ);
		}

		Block slab = current.getBlock();
		int slabID = Block.getIdFromBlock(slab);
		IBlockState doubleSlab = Block.getBlockById(slabID - 1).getDefaultState();

		if (slab instanceof BlockStoneSlab) {
			BlockStoneSlab.EnumType variant = slab.getStateFromMeta(getMetadata(stack))
					.getValue(BlockStoneSlab.VARIANT);
			doubleSlab = doubleSlab.withProperty(BlockStoneSlab.VARIANT, variant);
		} else if (slab instanceof BlockPurpurSlab) {
			BlockPurpurSlab.Variant variant = slab.getStateFromMeta(getMetadata(stack))
					.getValue(BlockPurpurSlab.VARIANT);
			doubleSlab = doubleSlab.withProperty(BlockPurpurSlab.VARIANT, variant);
		}

		return doubleSlab;
	}

}
