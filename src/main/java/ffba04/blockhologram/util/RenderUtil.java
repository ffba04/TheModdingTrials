package ffba04.blockhologram.util;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class RenderUtil {
	public static void renderGhostModel(IBlockState state, IBakedModel model, World world, int alpha, boolean tint,
			EnumFacing... facings) {
		GlStateManager.bindTexture(Minecraft.getMinecraft().getTextureMapBlocks().getGlTextureId());
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.colorMask(false, false, false, false);

		renderModel(state, model, world, BlockPos.ORIGIN, alpha, tint, facings);
		GlStateManager.colorMask(true, true, true, true);
		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		renderModel(state, model, world, BlockPos.ORIGIN, alpha, tint, facings);

		GlStateManager.disableBlend();
	}

	public static void renderModel(IBlockState state, IBakedModel model, World world, BlockPos blockPos, int alpha,
			boolean tint, EnumFacing... facings) {
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexBuffer = tessellator.getBuffer();
		vertexBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);

		if (facings.length == 0) {
			for (EnumFacing value : EnumFacing.values()) {
				renderQuads(state, vertexBuffer, model.getQuads(state, value, 0), world, blockPos, alpha, tint);
			}

			renderQuads(state, vertexBuffer, model.getQuads(state, null, 0), world, blockPos, alpha, tint);
		} else {
			for (EnumFacing value : facings) {
				renderQuads(state, vertexBuffer, model.getQuads(state, value, 0), world, blockPos, alpha, tint);
			}
		}

		tessellator.draw();
	}

	public static void renderQuads(IBlockState state, VertexBuffer vertexBuffer, List<BakedQuad> quads, World world,
			BlockPos blockPos, int alpha, boolean tint) {
		for (BakedQuad quad : quads) {
			if (state.getBlock() == Blocks.GRASS && quad.hasTintIndex() && !tint && quad.getFace() != EnumFacing.UP) {
				continue;
			}
			int color = quad.getTintIndex() == -1 || !tint ? alpha | 0xFFFFFF
					: alpha | Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, world, blockPos,
							quad.getTintIndex());
			LightUtil.renderQuadColor(vertexBuffer, quad, color);
		}
	}
}
