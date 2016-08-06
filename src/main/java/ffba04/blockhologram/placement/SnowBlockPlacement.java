package ffba04.blockhologram.placement;

import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SnowBlockPlacement extends DefaultBlockPlacement {

	@Override
	public BlockPos getPosition(World world, BlockPos pos, IBlockState state, ItemStack stack, EnumFacing facing,
			EntityPlayer player, float hitX, float hitY, float hitZ) {
		if (state.getBlock() != Blocks.SNOW_LAYER || facing != EnumFacing.UP) {
			return super.getPosition(world, pos, state, stack, facing, player, hitX, hitY, hitZ);
		}

		int layer = state.getValue(BlockSnow.LAYERS);

		if (layer == 8) {
			return super.getPosition(world, pos, state, stack, facing, player, hitX, hitY, hitZ);
		}

		return pos;
	}

	@Override
	public IBlockState placeBlockInWorld(World world, BlockPos pos, EntityPlayer player, IBlockState state,
			IBlockState current, ItemStack stack, EnumFacing facing, int metadata, float hitX, float hitY, float hitZ) {
		if (current.getBlock() != Blocks.SNOW_LAYER) {
			return super.placeBlockInWorld(world, pos, player, state, current, stack, facing, metadata, hitX, hitY,
					hitZ);
		}

		int layer = current.getValue(BlockSnow.LAYERS);

		if (layer == 8) {
			return super.placeBlockInWorld(world, pos, player, state, current, stack, facing, metadata, hitX, hitY,
					hitZ);
		}

		return state.withProperty(BlockSnow.LAYERS, layer + 1);
	}

}
