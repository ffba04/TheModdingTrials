package ffba04.blockhologram.dummy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSetMultimap;

import ffba04.blockhologram.BlockHologram;
import ffba04.blockhologram.Hologram;
import ffba04.blockhologram.Hologram.Part;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.profiler.Profiler;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DummyWorld extends World {

	private World world;
	private DummyPlayer dummyPlayer;
	private int copyRadius;

	public DummyWorld(World world, int copyRadius) {
		super(null, world.getWorldInfo(), new WorldProviderSurface(), new Profiler(), false);

		this.world = world;
		this.dummyPlayer = new DummyPlayer(this);
		this.copyRadius = copyRadius;
		
		chunkProvider = createChunkProvider();
		provider.registerWorld(this);
		provider.setDimension("dummy".hashCode());
		perWorldStorage = new MapStorage((ISaveHandler) null);
	}

	@SideOnly(Side.CLIENT)
	public void copyWorldToDummy(World world, Hologram hologram) {
		getChunkFromChunkCoords(0, 0).getTileEntityMap().clear();
		BlockPos center = hologram.pos;

		getPositionsAround(center).forEach(pos -> {
			IBlockState state = world.getBlockState(pos);
			setBlockState(pos, state, 0);

			if (state.getBlock().hasTileEntity(state)) {
				TileEntity entity = world.getTileEntity(pos);
				TileEntity dummyEntity = getTileEntity(pos);

				if (entity != null && dummyEntity != null) {
					dummyEntity.readFromNBT(entity.writeToNBT(new NBTTagCompound()));
				}
			}
		});
	}

	public EnumActionResult useBlockItem(EntityPlayer player, EnumHand hand, Hologram hologram) {
		ItemStack stack = player.getHeldItem(hand).copy();
		Item item = stack.getItem();
		
		if (!BlockHologram.enableHologram || BlockHologram.HOLOGRAM_EXLUSIONS.contains(item)) {
			return EnumActionResult.FAIL;
		}

		dummyPlayer.setLocationAndAngles(player.posX, player.posY, player.posZ, player.rotationYaw,
				player.rotationPitch);
		dummyPlayer.setHeldItem(hand, stack);

		EnumActionResult result = EnumActionResult.PASS;
		BlockPos pos = hologram.pos;
		EnumFacing facing = hologram.facing;
		float hitX = hologram.hitX;
		float hitY = hologram.hitY;
		float hitZ = hologram.hitZ;

		result = item.onItemUseFirst(stack, dummyPlayer, this, pos, facing, hitX, hitY, hitZ, hand);

		if (result != EnumActionResult.PASS) {
			return result;
		}

		result = item.onItemUse(stack, dummyPlayer, this, pos, hand, facing, hitX, hitY, hitZ);

		if (result != EnumActionResult.PASS) {
			return result;
		}

		result = item.onItemRightClick(stack, this, dummyPlayer, hand).getType();

		return result;
	}

	@SideOnly(Side.CLIENT)
	public void updateBlockModels(Hologram hologram) {
		final Minecraft mc = Minecraft.getMinecraft();
		BlockPos center = hologram.pos;

		getPositionsAround(center).forEach(pos -> {
			IBlockState current = world.getBlockState(pos).getActualState(world, pos);
			IBlockState dummy = getBlockState(pos).getActualState(this, pos);

			if (current != dummy) {
				IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(dummy);
				TileEntity entity = getTileEntity(pos);
				dummy = getBlockState(pos).getBlock().getExtendedState(dummy, world, pos);

				hologram.parts.add(
						new Part(this, pos, dummy, model, dummy.getBlock() instanceof BlockContainer ? entity : null));
			}
		});
	}

	private Stream<BlockPos> getPositionsAround(BlockPos center) {
		List<BlockPos> positions = new ArrayList<>();

		for (int y = center.getY() - copyRadius; y <= center.getY() + copyRadius; y++) {
			for (int z = center.getZ() - copyRadius; z <= center.getZ() + copyRadius; z++) {
				for (int x = center.getX() - copyRadius; x <= center.getX() + copyRadius; x++) {
					positions.add(new BlockPos(x, y, z));
				}
			}
		}

		return positions.stream();
	}

	@Override
	protected DummyChunk createChunkProvider() {
		return new DummyChunk(this);
	}

	@Override
	public Biome getBiomeGenForCoords(BlockPos pos) {
		return world.getBiomeGenForCoords(pos);
	}

	@Override
	public Biome getBiomeForCoordsBody(final BlockPos pos) {
		return world.getBiomeForCoordsBody(pos);
	}

	@Override
	public BiomeProvider getBiomeProvider() {
		return world.getBiomeProvider();
	}

	@Override
	public Chunk getChunkFromBlockCoords(BlockPos pos) {
		return chunkProvider.getLoadedChunk(0, 0);
	}

	@Override
	public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) {
		return chunkProvider.getLoadedChunk(0, 0);
	}

	@Override
	public boolean canSeeSky(BlockPos pos) {
		return world.canSeeSky(pos);
	}

	@Override
	public boolean canBlockSeeSky(BlockPos pos) {
		return world.canBlockSeeSky(pos);
	}

	@Override
	public int getLight(BlockPos pos) {
		return world.getLight(pos);
	}

	@Override
	public int getLight(BlockPos pos, boolean checkNeighbors) {
		return world.getLight(pos, checkNeighbors);
	}

	@Override
	public BlockPos getHeight(BlockPos pos) {
		return world.getHeight(pos);
	}

	@Override
	public int getLightFor(EnumSkyBlock type, BlockPos pos) {
		return world.getLightFor(type, pos);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
		return world.getLightFromNeighborsFor(type, pos);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		return world.getCombinedLight(pos, lightValue);
	}

	@Override
	public void setLightFor(EnumSkyBlock type, BlockPos pos, int lightValue) {
	}

	@Override
	public float getLightBrightness(BlockPos pos) {
		return world.getLightBrightness(pos);
	}

	@Override
	public boolean isDaytime() {
		return world.isDaytime();
	}

	@Override
	public RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid,
			boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
		return world.rayTraceBlocks(vec31, vec32, stopOnLiquid, ignoreBlockWithoutBoundingBox,
				returnLastUncollidableBlock);
	}

	@Override
	public boolean spawnEntityInWorld(Entity entity) {
		return false;
	}

	@Override
	public void removeEntity(Entity entity) {
	}

	@Override
	public void removeEntityDangerously(Entity entity) {
	}

	@Override
	public void addEventListener(IWorldEventListener listener) {
	}

	@Override
	public boolean isInsideBorder(WorldBorder worldBorder, Entity entity) {
		return world.isInsideBorder(worldBorder, entity);
	}

	@Override
	public List<AxisAlignedBB> getCollisionBoxes(Entity entity, AxisAlignedBB aabb) {
		return world.getCollisionBoxes(entity, aabb);
	}

	@Override
	public List<AxisAlignedBB> getCollisionBoxes(AxisAlignedBB bb) {
		return world.getCollisionBoxes(bb);
	}

	@Override
	public boolean collidesWithAnyBlock(AxisAlignedBB bbox) {
		return world.collidesWithAnyBlock(bbox);
	}

	@Override
	public int calculateSkylightSubtracted(float partialTicks) {
		return world.calculateSkylightSubtracted(partialTicks);
	}

	@Override
	public float getSunBrightnessFactor(float partialTicks) {
		return world.getSunBrightnessFactor(partialTicks);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getSunBrightness(float p_72971_1_) {
		return world.getSunBrightness(p_72971_1_);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getSunBrightnessBody(float p_72971_1_) {
		return world.getSunBrightnessBody(p_72971_1_);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Vec3d getSkyColor(Entity entity, float partialTicks) {
		return world.getSkyColor(entity, partialTicks);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Vec3d getSkyColorBody(Entity entity, float partialTicks) {
		return world.getSkyColorBody(entity, partialTicks);
	}

	@Override
	public float getCelestialAngle(float partialTicks) {
		return world.getCelestialAngle(partialTicks);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getMoonPhase() {
		return world.getMoonPhase();
	}

	@Override
	public float getCurrentMoonPhaseFactor() {
		return world.getCurrentMoonPhaseFactor();
	}

	@Override
	public float getCurrentMoonPhaseFactorBody() {
		return world.getCurrentMoonPhaseFactorBody();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Vec3d getCloudColour(float partialTicks) {
		return world.getCloudColour(partialTicks);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Vec3d getCloudColorBody(float partialTicks) {
		return world.getCloudColorBody(partialTicks);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Vec3d getFogColor(float partialTicks) {
		return world.getFogColor(partialTicks);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getStarBrightness(float partialTicks) {
		return world.getStarBrightness(partialTicks);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getStarBrightnessBody(float partialTicks) {
		return world.getStarBrightnessBody(partialTicks);
	}

	@Override
	public BlockPos getPrecipitationHeight(BlockPos pos) {
		return world.getPrecipitationHeight(pos);
	}

	@Override
	public BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
		return world.getTopSolidOrLiquidBlock(pos);
	}

	@Override
	public void updateEntities() {
	}

	@Override
	public void updateEntityWithOptionalForce(Entity entity, boolean forceUpdate) {
	}

	@Override
	public boolean addTileEntity(TileEntity tile) {
		return false;
	}

	@Override
	public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
	}

	@Override
	public void setTileEntity(BlockPos pos, TileEntity tileEntity) {
		if (tileEntity != null) {
			chunkProvider.getLoadedChunk(0, 0).addTileEntity(pos, tileEntity);
		}
	}

	@Override
	public void removeTileEntity(BlockPos pos) {
		chunkProvider.getLoadedChunk(0, 0).removeTileEntity(pos);
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return chunkProvider.getLoadedChunk(0, 0).getTileEntity(pos, Chunk.EnumCreateEntityType.IMMEDIATE);
	}

	@Override
	public boolean checkNoEntityCollision(AxisAlignedBB bb, Entity entity) {
		return world.checkNoEntityCollision(bb, entity);
	}

	@Override
	public boolean checkBlockCollision(AxisAlignedBB bb) {
		return world.checkBlockCollision(bb);
	}

	@Override
	public boolean containsAnyLiquid(AxisAlignedBB bb) {
		return world.containsAnyLiquid(bb);
	}

	@Override
	public boolean isFlammableWithin(AxisAlignedBB bb) {
		return world.isFlammableWithin(bb);
	}

	@Override
	public boolean handleMaterialAcceleration(AxisAlignedBB bb, Material material, Entity entity) {
		return false;
	}

	@Override
	public boolean isMaterialInBB(AxisAlignedBB bb, Material material) {
		return world.isMaterialInBB(bb, material);
	}

	@Override
	public boolean isAABBInMaterial(AxisAlignedBB bb, Material material) {
		return world.isAABBInMaterial(bb, material);
	}

	@Override
	public Explosion createExplosion(Entity entity, double x, double y, double z, float strength, boolean isSmoking) {
		return null;
	}

	@Override
	public Explosion newExplosion(Entity entity, double x, double y, double z, float strength, boolean isFlaming,
			boolean isSmoking) {
		return null;
	}

	@Override
	public float getBlockDensity(Vec3d vec, AxisAlignedBB bb) {
		return world.getBlockDensity(vec, bb);
	}

	@Override
	public boolean extinguishFire(EntityPlayer player, BlockPos pos, EnumFacing side) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getProviderName() {
		return "dummy";
	}

	@Override
	public boolean isBlockNormalCube(BlockPos pos, boolean _default) {
		return world.isBlockNormalCube(pos, _default);
	}

	@Override
	public void calculateInitialSkylight() {
	}

	@Override
	public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
	}

	@Override
	protected void calculateInitialWeather() {
	}

	@Override
	public void calculateInitialWeatherBody() {
	}

	@Override
	protected void updateWeather() {
	}

	@Override
	public void updateWeatherBody() {
	}

	@SideOnly(Side.CLIENT)
	@Override
	protected void playMoodSoundAndCheckLight(int p_147467_1_, int p_147467_2_, Chunk chunk) {
	}

	@Override
	public void immediateBlockTick(BlockPos pos, IBlockState state, Random random) {
	}

	@Override
	public boolean canBlockFreeze(BlockPos pos, boolean noWaterAdj) {
		return false;
	}

	@Override
	public boolean canBlockFreezeBody(BlockPos pos, boolean noWaterAdj) {
		return false;
	}

	@Override
	public boolean canSnowAt(BlockPos pos, boolean checkLight) {
		return false;
	}

	@Override
	public boolean canSnowAtBody(BlockPos pos, boolean checkLight) {
		return false;
	}

	@Override
	public boolean checkLight(BlockPos pos) {
		return false;
	}

	@Override
	public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos) {
		return false;
	}

	@Override
	public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity entity, AxisAlignedBB bb) {
		return Collections.emptyList();
	}

	@Override
	public List<Entity> getEntitiesInAABBexcluding(Entity entity, AxisAlignedBB boundingBox,
			Predicate<? super Entity> predicate) {
		return Collections.emptyList();
	}

	@Override
	public <T extends Entity> List<T> getEntities(Class<? extends T> entityType, Predicate<? super T> filter) {
		return Collections.emptyList();
	}

	@Override
	public <T extends Entity> List<T> getPlayers(Class<? extends T> playerType, Predicate<? super T> filter) {
		return Collections.emptyList();
	}

	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> classEntity, AxisAlignedBB bb) {
		return Collections.emptyList();
	}

	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb,
			Predicate<? super T> filter) {
		return Collections.emptyList();
	}

	@Override
	public <T extends Entity> T findNearestEntityWithinAABB(Class<? extends T> entityType, AxisAlignedBB aabb,
			T closestTo) {
		return null;
	}

	@Override
	public Entity getEntityByID(int id) {
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public List<Entity> getLoadedEntityList() {
		return Collections.emptyList();
	}

	@Override
	public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
	}

	@Override
	public int countEntities(Class<?> entityType) {
		return 0;
	}

	@Override
	public void loadEntities(Collection<Entity> entityCollection) {
	}

	@Override
	public void unloadEntities(Collection<Entity> entityCollection) {
	}

	@Override
	public boolean canBlockBePlaced(Block block, BlockPos pos, boolean p_175716_3_, EnumFacing side, Entity entity,
			ItemStack itemStack) {
		return world.canBlockBePlaced(block, pos, p_175716_3_, side, entity, itemStack);
	}

	@Override
	public WorldType getWorldType() {
		return world.getWorldType();
	}

	@Override
	public EntityPlayer getClosestPlayerToEntity(Entity entity, double distance) {
		return null;
	}

	@Override
	public EntityPlayer getNearestPlayerNotCreative(Entity entity, double distance) {
		return null;
	}

	@Override
	public EntityPlayer getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator) {
		return null;
	}

	@Override
	public boolean isAnyPlayerWithinRangeAt(double x, double y, double z, double range) {
		return false;
	}

	@Override
	public EntityPlayer getNearestAttackablePlayer(Entity entity, double maxXZDistance, double maxYDistance) {
		return null;
	}

	@Override
	public EntityPlayer getNearestAttackablePlayer(BlockPos pos, double maxXZDistance, double maxYDistance) {
		return null;
	}

	@Override
	public EntityPlayer getNearestAttackablePlayer(double posX, double posY, double posZ, double maxXZDistance,
			double maxYDistance, Function<EntityPlayer, Double> playerToDouble, Predicate<EntityPlayer> p_184150_12_) {
		return null;
	}

	@Override
	public EntityPlayer getPlayerEntityByName(String name) {
		return null;
	}

	@Override
	public EntityPlayer getPlayerEntityByUUID(UUID uuid) {
		return null;
	}

	@Override
	public void checkSessionLock() throws MinecraftException {
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void setTotalWorldTime(long worldTime) {
	}

	@Override
	public long getSeed() {
		return world.getSeed();
	}

	@Override
	public long getTotalWorldTime() {
		return world.getTotalWorldTime();
	}

	@Override
	public long getWorldTime() {
		return world.getWorldTime();
	}

	@Override
	public void setWorldTime(long time) {
	}

	@Override
	public BlockPos getSpawnPoint() {
		return world.getSpawnPoint();
	}

	@Override
	public void setSpawnPoint(BlockPos pos) {
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void joinEntityInSurroundings(Entity entity) {
	}

	@Override
	public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
		return true;
	}

	@Override
	public void addBlockEvent(BlockPos pos, Block block, int eventID, int eventParam) {
	}

	@Override
	public ISaveHandler getSaveHandler() {
		return null;
	}

	@Override
	public WorldInfo getWorldInfo() {
		return world.getWorldInfo();
	}

	@Override
	public GameRules getGameRules() {
		return world.getGameRules();
	}

	@Override
	public float getThunderStrength(float delta) {
		return world.getThunderStrength(delta);
	}

	@Override
	public float getRainStrength(float delta) {
		return world.getRainStrength(delta);
	}

	@Override
	public boolean isRainingAt(BlockPos strikePosition) {
		return world.isRainingAt(strikePosition);
	}

	@Override
	public boolean isThundering() {
		return world.isThundering();
	}

	@Override
	public boolean isRaining() {
		return world.isRaining();
	}

	@Override
	public boolean isBlockinHighHumidity(BlockPos pos) {
		return world.isBlockinHighHumidity(pos);
	}

	@Override
	public MapStorage getMapStorage() {
		return null;
	}

	@Override
	public void setItemData(String dataID, WorldSavedData worldSavedData) {
	}

	@Override
	public WorldSavedData loadItemData(Class<? extends WorldSavedData> clazz, String dataID) {
		return null;
	}

	@Override
	public int getUniqueDataId(String key) {
		return 0;
	}

	@Override
	public int getHeight() {
		return world.getHeight();
	}

	@Override
	public int getActualHeight() {
		return world.getActualHeight();
	}

	@Override
	public Random setRandomSeed(int p_72843_1_, int p_72843_2_, int p_72843_3_) {
		return rand;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public double getHorizon() {
		return world.getHorizon();
	}

	@Override
	public Calendar getCurrentDate() {
		return world.getCurrentDate();
	}

	@Override
	public Scoreboard getScoreboard() {
		return world.getScoreboard();
	}

	@Override
	public void updateComparatorOutputLevel(BlockPos pos, Block block) {
	}

	@Override
	public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
		return world.getDifficultyForLocation(pos);
	}

	@Override
	public EnumDifficulty getDifficulty() {
		return world.getDifficulty();
	}

	@Override
	public int getSkylightSubtracted() {
		return world.getSkylightSubtracted();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getLastLightningBolt() {
		return world.getLastLightningBolt();
	}

	@Override
	public VillageCollection getVillageCollection() {
		return world.getVillageCollection();
	}

	@Override
	public WorldBorder getWorldBorder() {
		return world.getWorldBorder();
	}

	@Override
	public boolean isSpawnChunk(int x, int z) {
		return world.isSpawnChunk(x, z);
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		return world.isSideSolid(pos, side, _default);
	}

	@Override
	public ImmutableSetMultimap<ChunkPos, ForgeChunkManager.Ticket> getPersistentChunks() {
		return ImmutableSetMultimap.<ChunkPos, ForgeChunkManager.Ticket>of();
	}

	@Override
	public int getBlockLightOpacity(BlockPos pos) {
		return world.getBlockLightOpacity(pos);
	}

	@Override
	public int countEntities(net.minecraft.entity.EnumCreatureType type, boolean forSpawnCount) {
		return 0;
	}

	@Override
	public void sendPacketToServer(Packet<?> packet) {
	}

	@Override
	public LootTableManager getLootTableManager() {
		return world.getLootTableManager();
	}

	@Override
	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
		return true;
	}

}
