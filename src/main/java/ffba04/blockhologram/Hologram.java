package ffba04.blockhologram;

import java.util.ArrayList;
import java.util.List;

import ffba04.blockhologram.util.RenderUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Hologram {

	public final BlockPos pos;
	public final float hitX;
	public final float hitY;
	public final float hitZ;
	public final EnumFacing facing;
	public final float yaw;
	public final float pitch;
	public final List<Part> parts = new ArrayList<>();

	public Hologram(BlockPos pos, Vec3d hitVec, EnumFacing facing, float yaw, float pitch) {
		this.pos = pos;
		hitX = (float) (hitVec.xCoord - pos.getX());
		hitY = (float) (hitVec.yCoord - pos.getY());
		hitZ = (float) (hitVec.zCoord - pos.getZ());
		this.facing = facing;
		this.yaw = yaw;
		this.pitch = pitch;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}

		if (!(obj instanceof Hologram)) {
			return false;
		}

		Hologram hologram = (Hologram) obj;
		boolean flag = false;

		flag = pos.equals(hologram.pos);
		flag &= hitX == hologram.hitX;
		flag &= hitY == hologram.hitY;
		flag &= hitZ == hologram.hitZ;
		flag &= facing == hologram.facing;
		flag &= yaw == hologram.yaw;
		flag &= pitch == hologram.pitch;

		return flag;
	}

	public static class Part {
		public final World world;
		public final BlockPos pos;
		public final IBlockState state;
		public final IBakedModel model;
		public final TileEntity entity;

		public Part(World world, BlockPos pos, IBlockState state, IBakedModel model, TileEntity entity) {
			this.world = world;
			this.pos = pos;
			this.state = state;
			this.model = model;
			this.entity = entity;
		}

		public void renderPart(float partialTicks) {
			GlStateManager.scale(1.008F, 1.024F, 1.008F);
			GlStateManager.translate(-0.004F, -0.004F, -0.004F);
			
			if (state.getRenderType() == EnumBlockRenderType.MODEL) {
				RenderUtil.renderGhostModel(state, model, world, 0xAA000000, true);
			}

			if (entity != null) {
				TileEntityRendererDispatcher.instance.renderTileEntityAt(entity, 0, 0, 0, partialTicks);
			}
		}
	}

}
