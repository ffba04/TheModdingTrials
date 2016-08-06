package ffba04.blockhologram.renderer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerBlockRenderer extends DefaultBlockRenderer {

	@Override
	public void renderBlock(World world, BlockPos pos, IBlockState state, ItemStack stack, EntityPlayer player,
			float partialTicks) {
		if (state.getRenderType() == EnumBlockRenderType.MODEL) {
			super.renderBlock(world, pos, state, stack, player, partialTicks);
			return;
		}

		TileEntityRendererDispatcher.instance.renderTileEntityAt(world.getTileEntity(pos), 0, 0, 0, partialTicks, -1);
	}

}
