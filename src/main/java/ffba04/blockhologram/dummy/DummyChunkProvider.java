package ffba04.blockhologram.dummy;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

public class DummyChunkProvider implements IChunkProvider {
	
	private DummyWorld world;
	
	public DummyChunkProvider(DummyWorld world) {
		this.world = world;
	}

	@Override
	public Chunk getLoadedChunk(int x, int z) {
		return world.getChunkFromChunkCoords(0, 0);
	}

	@Override
	public Chunk provideChunk(int x, int z) {
		return world.getChunkFromChunkCoords(0, 0);
	}

	@Override
	public boolean unloadQueuedChunks() {
		return true;
	}

	@Override
	public String makeString() {
		return "dummy";
	}

}
