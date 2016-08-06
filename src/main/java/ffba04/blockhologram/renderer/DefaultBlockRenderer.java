package ffba04.blockhologram.renderer;

import ffba04.blockhologram.util.RenderUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DefaultBlockRenderer implements BlockRenderer {

	@Override
	public void renderBlock(World world, BlockPos pos, IBlockState state, ItemStack stack, EntityPlayer player,
			float partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
		IBakedModel model = dispatcher.getBlockModelShapes().getModelForState(state);

		RenderUtil.renderGhostModel(state, model, world, 0x66000000, true);
	}

	@Override
	public void renderBlockOverlay(World world, BlockPos pos, IBlockState state, EnumFacing facing,
			float partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
		IBakedModel model = dispatcher.getBlockModelShapes().getModelForState(state);

		GlStateManager.scale(1.004F, 1.012F, 1.004F);
		GlStateManager.translate(-0.002F, -0.002F, -0.002F);
		GlStateManager.disableTexture2D();
		RenderUtil.renderGhostModel(state, model, world, 0x55000000, false);
		GlStateManager.enableTexture2D();
	}

}
