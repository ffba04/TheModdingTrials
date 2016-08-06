package ffba04.blockhologram.dummy;

import java.util.Arrays;
import java.util.List;
import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DummyChunk extends Chunk {
	
	private World world;
	private IBlockState[] blockStorage = new IBlockState[4096];

	public DummyChunk(World world) {
		super(world, 0, 0);
		this.world = world;
		setChunkLoaded(true);
		Arrays.fill(blockStorage, 0, blockStorage.length, Blocks.AIR.getDefaultState());
	}

	@Override
	public IBlockState getBlockState(int x, int y, int z) {
		return blockStorage[((y & 0xF) << 8) + ((z & 0xF) << 4) + (x & 0xF)];
	}

	@Override
	public IBlockState setBlockState(BlockPos pos, IBlockState stateNew) {
		int x = pos.getX() & 0xF;
		int y = pos.getY() & 0xF;
		int z = pos.getZ() & 0xF;

		IBlockState stateOld = getBlockState(pos);
		Block blockNew = stateNew.getBlock();
		Block blockOld = stateOld.getBlock();

		blockStorage[(y << 8) + (z << 4) + x] = stateNew;

		if (blockOld.hasTileEntity(stateOld)) {
			removeTileEntity(pos);
		}

		if (world.isRemote == false && blockOld != blockNew
				&& (world.captureBlockSnapshots == false || blockNew.hasTileEntity(stateNew))) {
			blockNew.onBlockAdded(world, pos, stateNew);
		}

		if (blockNew.hasTileEntity(stateNew)) {
			TileEntity entity = blockNew.createTileEntity(world, stateNew);
			world.setTileEntity(pos, entity);

			if (entity != null) {
				entity.updateContainingBlockInfo();
			}
		}

		return stateOld;
	}

	@Override
	public int getHeight(BlockPos pos) {
		return 16;
	}

	@Override
	public int getHeightValue(int x, int z) {
		return 16;
	}

	@Override
	public int getTopFilledSegment() {
		return 0;
	}

	@SideOnly(Side.CLIENT)
	@Override
	protected void generateHeightMap() {
	}

	@Override
	public void generateSkylightMap() {
	}

	@Override
	public int getLightFor(EnumSkyBlock skyBlock, BlockPos pos) {
		return 15;
	}

	@Override
	public void setLightFor(EnumSkyBlock skyBlock, BlockPos pos, int value) {
	}

	@Override
	public int getLightSubtracted(BlockPos pos, int amount) {
		return 15;
	}

	@Override
	public void addEntity(Entity entity) {
	}

	@Override
	public void removeEntity(Entity entity) {
	}

	@Override
	public void removeEntityAtIndex(Entity entity, int index) {
	}

	@Override
	public boolean canSeeSky(BlockPos pos) {
		return true;
	}

	@Override
	public void onChunkLoad() {
	}

	@Override
	public void onChunkUnload() {
	}

	@Override
	public void getEntitiesWithinAABBForEntity(Entity entity, AxisAlignedBB aabb, List<Entity> listToFill,
			Predicate<? super Entity> predicate) {
	}

	@Override
	public <T extends Entity> void getEntitiesOfTypeWithinAAAB(Class<? extends T> entityClass, AxisAlignedBB aabb,
			List<T> listToFill, Predicate<? super T> predicate) {
	}

	@Override
	public boolean needsSaving(boolean auto) {
		return false;
	}

	@Override
	public void populateChunk(IChunkProvider chunkProvider, IChunkGenerator chunkGenrator) {
	}

	@Override
	protected void populateChunk(IChunkGenerator generator) {
	}

	@Override
	public BlockPos getPrecipitationHeight(BlockPos pos) {
		return BlockPos.ORIGIN;
	}

	@Override
	public void onTick(boolean p_150804_1_) {
	}

	@Override
	public boolean isPopulated() {
		return true;
	}

	@Override
	public boolean isChunkTicked() {
		return true;
	}

	@Override
	public boolean getAreLevelsEmpty(int startY, int endY) {
		return false;
	}

	@Override
	public void setStorageArrays(ExtendedBlockStorage[] newStorageArrays) {
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void fillChunk(PacketBuffer buf, int p_186033_2_, boolean p_186033_3_) {
	}

	@Override
	public Biome getBiome(BlockPos pos, BiomeProvider provider) {
		return Biomes.PLAINS;
	}

	@Override
	public void enqueueRelightChecks() {
	}

	@Override
	public void checkLight() {
	}

	@Override
	public boolean isLoaded() {
		return true;
	}

	@Override
	public void setHeightMap(int[] newHeightMap) {
	}

	@Override
	public boolean isTerrainPopulated() {
		return true;
	}

	@Override
	public boolean isLightPopulated() {
		return true;
	}

	@Override
	public int getLowestHeight() {
		return 16;
	}
	
}
