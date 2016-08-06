package ffba04.blockhologram.placement;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LilyPadPlacement extends DefaultBlockPlacement {

	@Override
	public BlockPos getPosition(World world, BlockPos pos, IBlockState state, ItemStack stack, EnumFacing facing,
			EntityPlayer player, float hitX, float hitY, float hitZ) {
		pos = pos.offset(facing);

		if (state.getMaterial() == Material.WATER && state.getValue(BlockLiquid.LEVEL).intValue() == 0
				&& world.isAirBlock(pos.up())) {
			return pos.up();
		}
		return null;
	}

}
