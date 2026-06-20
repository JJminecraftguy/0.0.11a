package com.mojang.minecraft.level;

import org.lwjgl.opengl.GL11;

import com.mojang.minecraft.Player;
import com.mojang.minecraft.level.tile.DynamicTileInfo;
import com.mojang.minecraft.level.tile.Tile;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.renderer.Tesselator;
import com.mojang.minecraft.renderer.Textures;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Chunk {
	public AABB aabb;
	public final Level level;
	public final int x0;
	public final int y0;
	public final int z0;
	public final int x1;
	public final int y1;
	public final int z1;
	public final float x;
	public final float y;
	public final float z;
	private boolean dirty = true;
	private int lists = -1;
	public long dirtiedTime = 0L;
	private final List<DynamicTileInfo> dynamicTiles = new ArrayList<>();
	private static Tesselator t = Tesselator.instance;
	public static int updates = 0;
	private static long totalTime = 0L;
	private static int totalUpdates = 0;

	public Chunk(Level level, int x0, int y0, int z0, int x1, int y1, int z1) {
		this.level = level;
		this.x0 = x0;
		this.y0 = y0;
		this.z0 = z0;
		this.x1 = x1;
		this.y1 = y1;
		this.z1 = z1;
		this.x = (float)(x0 + x1) / 2.0F;
		this.y = (float)(y0 + y1) / 2.0F;
		this.z = (float)(z0 + z1) / 2.0F;
		this.aabb = new AABB((float)x0, (float)y0, (float)z0, (float)x1, (float)y1, (float)z1);
		this.lists = GL11.glGenLists(2);
	}

	private void rebuild(int layer) {
		if(layer == 0) {
			this.dynamicTiles.clear();
		}

		this.dirty = false;
		++updates;
		long before = System.nanoTime();
		GL11.glNewList(this.lists + layer, GL11.GL_COMPILE);
		t.init();
		int tiles = 0;

		for(int x = this.x0; x < this.x1; ++x) {
			for(int y = this.y0; y < this.y1; ++y) {
				for(int z = this.z0; z < this.z1; ++z) {
					int tileId = this.level.getTile(x, y, z);
					if(tileId > 0) {
						Tile tile = Tile.tiles[tileId];
						if(tile.isDynamicallyRendered()) {
							if(layer == 0) {
								this.dynamicTiles.add(new DynamicTileInfo(tile, x, y, z));
							}
						} else {
							tile.render(t, this.level, layer, x, y, z);
							++tiles;
						}
					}
				}
			}
		}

		t.flush();
		GL11.glEndList();
		long after = System.nanoTime();
		if(tiles > 0) {
			totalTime += after - before;
			++totalUpdates;
		}

	}

	public void rebuild() {
		this.rebuild(0);
		this.rebuild(1);
	}

	public void render(int layer, Player player, Textures textures) {
	    if (this.dirty) {
	        this.rebuild();
	    }
	    
	    if (layer == 1) {
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	        GL11.glEnable(GL11.GL_ALPHA_TEST);
	        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
	    }

		GL11.glCallList(this.lists + layer);

		if(!this.dynamicTiles.isEmpty()) {
			t.init();
			String prevTexture = null;

			for(DynamicTileInfo dynamicTile : this.dynamicTiles) {
				String texture = dynamicTile.tile.getDynamicTexture();
				if(!Objects.equals(texture, prevTexture)) {
					int id = textures.loadTexture(texture == null ? "/terrain.png" : texture, GL11.GL_LINEAR);
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
					prevTexture = texture;
				}

				dynamicTile.render(t, this.level, player, layer);
			}

			t.flush();
			if(prevTexture != null) {
				int id = textures.loadTexture("/terrain.png", GL11.GL_LINEAR);
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
			}
		}
	}

	public void setDirty() {
		if(!this.dirty) {
			this.dirtiedTime = System.currentTimeMillis();
		}

		this.dirty = true;
	}

	public boolean isDirty() {
		return this.dirty;
	}

	public float distanceToSqr(Player player) {
		float xd = player.x - this.x;
		float yd = player.y - this.y;
		float zd = player.z - this.z;
		return xd * xd + yd * yd + zd * zd;
	}
}
