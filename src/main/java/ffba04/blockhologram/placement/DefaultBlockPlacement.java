package ffba04.blockhologram.placement;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DefaultBlockPlacement implements BlockPlacement {

	@Override
	public Block getBlock(ItemStack stack) {
		ItemBlock item = (ItemBlock) stack.getItem();
		return item.getBlock();
	}

	@Override
	public BlockPos getPosition(World world, BlockPos pos, IBlockState state, ItemStack stack, EnumFacing facing,
			EntityPlayer player, float hitX, float hitY, float hitZ) {
		if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
			pos = pos.offset(facing);
		}

		if (!player.canPlayerEdit(pos, facing, stack)
				|| !world.canBlockBePlaced(state.getBlock(), pos, false, facing, null, stack)) {
			return null;
		}
		return pos;
	}

	@Override
	public IBlockState placeBlockInWorld(World world, BlockPos pos, EntityPlayer player, IBlockState state,
			IBlockState current, ItemStack stack, EnumFacing facing, int metadata, float hitX, float hitY, float hitZ) {
		world.setBlockState(pos, state.getBlock().onBlockPlaced(world, pos, facing, hitX, hitY, hitZ, metadata, player),
				0);
		state.getBlock().onBlockPlacedBy(world, pos, state, player, stack);
		state = world.getBlockState(pos).getActualState(world, pos);

		return state;
	}

	@Override
	public int getMetadata(ItemStack stack) {
		return 0;
	}

}
