package ffba04.blockhologram.placement;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockPlacement {

	Map<Class<? extends Item>, BlockPlacement> TYPES = new HashMap<>();

	Block getBlock(ItemStack stack);

	BlockPos getPosition(World world, BlockPos pos, IBlockState state, ItemStack stack, EnumFacing facing,
			EntityPlayer player, float hitX, float hitY, float hitZ);

	IBlockState placeBlockInWorld(World world, BlockPos pos, EntityPlayer player, IBlockState state,
			IBlockState current, ItemStack stack, EnumFacing facing, int metadata, float hitX, float hitY, float hitZ);

	int getMetadata(ItemStack stack);

}
