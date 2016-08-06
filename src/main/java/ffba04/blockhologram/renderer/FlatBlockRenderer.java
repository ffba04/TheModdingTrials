package ffba04.blockhologram.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FlatBlockRenderer extends DefaultBlockRenderer {

	@Override
	public void renderBlockOverlay(World world, BlockPos pos, IBlockState state, EnumFacing facing,
			float partialTicks) {
		super.renderBlockOverlay(world, pos, Blocks.REDSTONE_WIRE.getDefaultState(), facing, partialTicks);
	}

}
