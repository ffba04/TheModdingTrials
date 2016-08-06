package ffba04.blockhologram.renderer;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockRenderer {

	Map<Class<? extends Block>, BlockRenderer> TYPES = new HashMap<>();

	void renderBlock(World world, BlockPos pos, IBlockState state, ItemStack stack, EntityPlayer player,
			float partialTicks);

	void renderBlockOverlay(World world, BlockPos pos, IBlockState state, EnumFacing facing, float partialTicks);

}
