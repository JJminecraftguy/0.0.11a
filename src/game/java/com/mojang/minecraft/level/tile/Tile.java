package com.mojang.minecraft.level.tile;

import java.util.Random;

import com.mojang.minecraft.Player;
import com.mojang.minecraft.level.Directions;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.renderer.Tesselator;
import com.mojang.minecraft.particle.Particle;
import com.mojang.minecraft.particle.ParticleEngine;
import com.mojang.minecraft.phys.AABB;

public class Tile {
	public static final Tile[] tiles = new Tile[256];
	public static final Tile empty = null;
	public static final Tile rock = new TerrainTile(1, 1);
	public static final Tile grass = new GrassTile(2);
	public static final Tile dirt = new DirtTile(3, 2);
	public static final Tile stoneBrick = new Tile(4, 16);
	public static final Tile wood = new Tile(5, 4);
	public static final Tile bush = new Bush(6);
	public static final Tile tree = new TreeTile(7);
	public int tex;
	public final int id;

	protected Tile(int id) {
		tiles[id] = this;
		this.id = id;
	}

	protected Tile(int id, int tex) {
		this(id);
		this.tex = tex;
	}

    public void render(Tesselator t, Level level, int layer, int x, int y, int z) {
        int color = this.getColor(level, x, y, z);
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        if (layer == 1) {
            r *= 0.6F;
            g *= 0.6F;
            b *= 0.6F;
        }

        byte shape = level.getShape(x, y, z);

        float c1 = 1.0F;
        float c2 = 0.8F;
        float c3 = 0.6F;
        if(this.shouldRenderFace(level, x, y - 1, z, layer, 0)) {
            this.renderFace(t, shape, x, y, z, 0, r * c1, g * c1, b * c1);
        }

        if(this.shouldRenderFace(level, x, y + 1, z, layer, 1)) {
            this.renderFace(t, shape, x, y, z, 1, r * c1, g * c1, b * c1);
        }

        if(this.shouldRenderFace(level, x, y, z - 1, layer, 2)) {
            this.renderFace(t, shape, x, y, z, 2, r * c2, g * c2, b * c2);
        }

        if(this.shouldRenderFace(level, x, y, z + 1, layer, 3)) {
            this.renderFace(t, shape, x, y, z, 3, r * c2, g * c2, b * c2);
        }

        if(this.shouldRenderFace(level, x - 1, y, z, layer, 4)) {
            this.renderFace(t, shape, x, y, z, 4, r * c3, g * c3, b * c3);
        }

        if(this.shouldRenderFace(level, x + 1, y, z, layer, 5)) {
            this.renderFace(t, shape, x, y, z, 5, r * c3, g * c3, b * c3);
        }

    }

	public void render(Tesselator t, Level level, Player player, int layer, int x, int y, int z) {
		this.render(t, level, layer, x, y, z);
	}

	protected int getColor(Level level, int x, int y, int z) {
		return 0xFFFFFF;
	}

	public boolean isDynamicallyRendered() {
		return false;
	}

	public String getDynamicTexture() {
		return null;
	}

	private boolean shouldRenderFace(Level level, int x, int y, int z, int layer, int face) {
		if (level.isSolidTile(x, y, z)) {
			if(face == 0) {
				return false;
			}

			// 3: +x +z
			// 2: +x -z
			// 1: -x +z
			// 0: -x -z
			int shape = level.getShape(x, y, z);
			int myShape = level.getShape(x - Directions.OFFSET_X[face], y - Directions.OFFSET_Y[face], z - Directions.OFFSET_Z[face]);
			switch(face) {
				case 1: // face +y
					if(shape != 0b1110
						&& shape != 0b1101
						&& shape != 0b1011
						&& shape != 0b0111) return false;
					break;
				case 2: // face -z
					if((shape & 0b1010) == (myShape & 0b0101)) return false;
					break;
				case 3: // face +z
					if((shape & 0b0101) == (myShape & 0b1010)) return false;
					break;
				case 4: // face -x
					if((shape & 0b1100) == (myShape & 0b0011)) return false;
					break;
				case 5: // face +x
					if((shape & 0b0011) == (myShape & 0b1100)) return false;
					break;
			}
		}

		return level.isLit(x, y, z) ^ layer == 1;
	}

	protected int getTexture(int face) {
		return this.tex;
	}

	public void renderFace(Tesselator t, byte shape, int x, int y, int z, int face, float r, float g, float b) {
		t.color(r, g, b);

		int tex = this.getTexture(face);
		float u0 = (float)(tex % 16) / 16.0F + 0.5F / 256.0F;
		float u1 = u0 + 0.999F / 16.0F - 1.0F / 256.0F;
		float v0 = (float)(tex / 16) / 16.0F + 0.5F / 256.0F;
		float v1 = v0 + 0.999F / 16.0F - 1.0F / 256.0F;
		float x0 = (float)x + 0.0F;
		float x1 = (float)x + 1.0F;
		float y0 = (float)y + 0.0F;
		float y1 = (float)y + 1.0F;
		float z0 = (float)z + 0.0F;
		float z1 = (float)z + 1.0F;

		if(face == 0) {
			t.vertexUV(x0, y0, z1, u0, v1);
			t.vertexUV(x0, y0, z0, u0, v0);
			t.vertexUV(x1, y0, z0, u1, v0);
			t.vertexUV(x1, y0, z1, u1, v1);
		}

		if(face == 1) {
			boolean ti00 = (shape & 0b0001) != 0;
			boolean ti01 = (shape & 0b0010) != 0;
			boolean ti10 = (shape & 0b0100) != 0;
			boolean ti11 = (shape & 0b1000) != 0;

			r *= 0.8F;
			g *= 0.8F;
			b *= 0.8F;

			switch(shape) {
				case 0b0001: // -x -z inner corner
					drawTopTriangle(t, x0, x1, y1, y1, z0, z1, u0, u1, v0, v1, 0);
					t.color(r, g, b);
					drawTopTriangle(t, x0, x1, y1, y0, z0, z1, u0, u1, v0, v1, 3);
					return;
				case 0b0010: // -x +z inner corner
					drawTopTriangle(t, x0, x1, y1, y1, z0, z1, u0, u1, v0, v1, 1);
					t.color(r, g, b);
					drawTopTriangle(t, x0, x1, y1, y0, z0, z1, u0, u1, v0, v1, 2);
					return;
				case 0b0100: // +x -z inner corner
					drawTopTriangle(t, x0, x1, y1, y1, z0, z1, u0, u1, v0, v1, 2);
					t.color(r, g, b);
					drawTopTriangle(t, x0, x1, y1, y0, z0, z1, u0, u1, v0, v1, 1);
					return;
				case 0b1000: // +x +z inner corner
					drawTopTriangle(t, x0, x1, y1, y1, z0, z1, u0, u1, v0, v1, 3);
					t.color(r, g, b);
					drawTopTriangle(t, x0, x1, y1, y0, z0, z1, u0, u1, v0, v1, 0);
					return;
			}

			if((shape & 0b1111) != 0) {
				t.color(r, g, b);
			}

			switch(shape) {
				case 0b1110: // -x -z outer corner
					drawTopTriangle(t, x0, x1, y0, y1, z0, z1, u0, u1, v0, v1, 3);
					return;
				case 0b1101: // -x +z outer corner
					drawTopTriangle(t, x0, x1, y0, y1, z0, z1, u0, u1, v0, v1, 2);
					return;
				case 0b1011: // +x -z outer corner
					drawTopTriangle(t, x0, x1, y0, y1, z0, z1, u0, u1, v0, v1, 1);
					return;
				case 0b0111: // +x +z outer corner
					drawTopTriangle(t, x0, x1, y0, y1, z0, z1, u0, u1, v0, v1, 0);
					return;
			}

			boolean altTessellation = ti00 || ti11;

			if(!altTessellation) t.vertexUV(x1, ti11 ? y0 : y1, z1, u1, v1);
			t.vertexUV(x1, ti10 ? y0 : y1, z0, u1, v0);
			t.vertexUV(x0, ti00 ? y0 : y1, z0, u0, v0);
			t.vertexUV(x0, ti01 ? y0 : y1, z1, u0, v1);
			if(altTessellation) t.vertexUV(x1, ti11 ? y0 : y1, z1, u1, v1);
		}

		if(face == 2) {
			boolean ti00 = (shape & 0b0001) != 0;
			boolean ti10 = (shape & 0b0100) != 0;
			if(ti00 && ti10) return;

			t.vertexUV(x0, ti00 ? y0 : y1, z0, u1, ti00 ? v1 : v0);
			t.vertexUV(x1, ti10 ? y0 : y1, z0, u0, ti10 ? v1 : v0);
			t.vertexUV(x1, y0, z0, u0, v1);
			t.vertexUV(x0, y0, z0, u1, v1);
		}

		if(face == 3) {
			boolean ti01 = (shape & 0b0010) != 0;
			boolean ti11 = (shape & 0b1000) != 0;
			if(ti01 && ti11) return;

			t.vertexUV(x0, ti01 ? y0 : y1, z1, u0, ti01 ? v1 : v0);
			t.vertexUV(x0, y0, z1, u0, v1);
			t.vertexUV(x1, y0, z1, u1, v1);
			t.vertexUV(x1, ti11 ? y0 : y1, z1, u1, ti11 ? v1 : v0);
		}

		if(face == 4) {
			boolean ti00 = (shape & 0b0001) != 0;
			boolean ti01 = (shape & 0b0010) != 0;
			if(ti00 && ti01) return;

			t.vertexUV(x0, ti01 ? y0 : y1, z1, u1, ti01 ? v1 : v0);
			t.vertexUV(x0, ti00 ? y0 : y1, z0, u0, ti00 ? v1 : v0);
			t.vertexUV(x0, y0, z0, u0, v1);
			t.vertexUV(x0, y0, z1, u1, v1);
		}

		if(face == 5) {
			boolean ti10 = (shape & 0b0100) != 0;
			boolean ti11 = (shape & 0b1000) != 0;
			if(ti10 && ti11) return;

			t.vertexUV(x1, y0, z1, u0, v1);
			t.vertexUV(x1, y0, z0, u1, v1);
			t.vertexUV(x1, ti10 ? y0 : y1, z0, u1, ti10 ? v1 : v0);
			t.vertexUV(x1, ti11 ? y0 : y1, z1, u0, ti11 ? v1 : v0);
		}

	}

	private static void drawTopTriangle(Tesselator t, float x0, float x1, float y0, float y1, float z0, float z1, float u0, float u1, float v0, float v1, int type) {
		switch(type) {
			case 3:
				t.vertexUV(x1, y0, z0, u1, v0);
				t.vertexUV(x0, y1, z0, u0, v0);
				t.vertexUV(x0, y0, z1, u0, v1);
				t.vertexUV(x0, y0, z1, u0, v1);
				break;
			case 2:
				t.vertexUV(x1, y0, z1, u1, v1);
				t.vertexUV(x0, y0, z0, u0, v0);
				t.vertexUV(x0, y1, z1, u0, v1);
				t.vertexUV(x0, y1, z1, u0, v1);
				break;
			case 1:
				t.vertexUV(x1, y0, z1, u1, v1);
				t.vertexUV(x1, y1, z0, u1, v0);
				t.vertexUV(x0, y0, z0, u0, v0);
				t.vertexUV(x0, y0, z0, u0, v0);
				break;
			case 0:
				t.vertexUV(x1, y1, z1, u1, v1);
				t.vertexUV(x1, y0, z0, u1, v0);
				t.vertexUV(x0, y0, z1, u0, v1);
				t.vertexUV(x0, y0, z1, u0, v1);
				break;
		}
	}

	public void renderFaceNoTexture(Tesselator t, byte shape, int x, int y, int z, int face) {
		// eaglercraft note: x/y/z bounds are expanded by 0.001 (rather than the
		// vanilla exact-bounds box) to avoid z-fighting on the highlight outline
		float x0 = (float)x - 0.001F;
		float x1 = (float)x + 1.001F;
		float y0 = (float)y - 0.001F;
		float y1 = (float)y + 1.001F;
		float z0 = (float)z - 0.001F;
		float z1 = (float)z + 1.001F;

		if(face == 0) {
			t.vertex(x0, y0, z1);
			t.vertex(x0, y0, z0);
			t.vertex(x1, y0, z0);
			t.vertex(x1, y0, z1);
		}

		if(face == 1) {
			boolean ti00 = (shape & 0b0001) != 0;
			boolean ti01 = (shape & 0b0010) != 0;
			boolean ti10 = (shape & 0b0100) != 0;
			boolean ti11 = (shape & 0b1000) != 0;

			switch(shape) {
				case 0b1110: // -x -z outer corner
					t.vertex(x1, y0, z0);
					t.vertex(x0, y1, z0);
					t.vertex(x0, y0, z1);
					t.vertex(x0, y0, z1);
					return;
				case 0b1101: // -x +z outer corner
					t.vertex(x1, y0, z1);
					t.vertex(x0, y0, z0);
					t.vertex(x0, y1, z1);
					t.vertex(x0, y1, z1);
					return;
				case 0b1011: // +x -z outer corner
					t.vertex(x1, y0, z1);
					t.vertex(x1, y1, z0);
					t.vertex(x0, y0, z0);
					t.vertex(x0, y0, z0);
					return;
				case 0b0111: // +x +z outer corner
					t.vertex(x1, y1, z1);
					t.vertex(x1, y0, z0);
					t.vertex(x0, y0, z1);
					t.vertex(x0, y0, z1);
					return;
			}

			boolean altTessellation = ti00 || ti11;

			if(!altTessellation) t.vertex(x1, ti11 ? y0 : y1, z1);
			t.vertex(x1, ti10 ? y0 : y1, z0);
			t.vertex(x0, ti00 ? y0 : y1, z0);
			t.vertex(x0, ti01 ? y0 : y1, z1);
			if(altTessellation) t.vertex(x1, ti11 ? y0 : y1, z1);
		}

		if(face == 2) {
			boolean ti00 = (shape & 0b0001) != 0;
			boolean ti10 = (shape & 0b0100) != 0;

			t.vertex(x0, ti00 ? y0 : y1, z0);
			t.vertex(x1, ti10 ? y0 : y1, z0);
			t.vertex(x1, y0, z0);
			t.vertex(x0, y0, z0);
		}

		if(face == 3) {
			boolean ti01 = (shape & 0b0010) != 0;
			boolean ti11 = (shape & 0b1000) != 0;

			t.vertex(x0, ti01 ? y0 : y1, z1);
			t.vertex(x0, y0, z1);
			t.vertex(x1, y0, z1);
			t.vertex(x1, ti11 ? y0 : y1, z1);
		}

		if(face == 4) {
			boolean ti00 = (shape & 0b0001) != 0;
			boolean ti01 = (shape & 0b0010) != 0;

			t.vertex(x0, ti01 ? y0 : y1, z1);
			t.vertex(x0, ti00 ? y0 : y1, z0);
			t.vertex(x0, y0, z0);
			t.vertex(x0, y0, z1);
		}

		if(face == 5) {
			boolean ti10 = (shape & 0b0100) != 0;
			boolean ti11 = (shape & 0b1000) != 0;

			t.vertex(x1, y0, z1);
			t.vertex(x1, y0, z0);
			t.vertex(x1, ti10 ? y0 : y1, z0);
			t.vertex(x1, ti11 ? y0 : y1, z1);
		}

	}

	private static void drawTopTriangle(Tesselator t, float x0, float x1, float y0, float y1, float z0, float z1, int type) {
		switch(type) {
			case 3:
				t.vertex(x1, y0, z0);
				t.vertex(x0, y1, z0);
				t.vertex(x0, y0, z1);
				t.vertex(x0, y0, z1);
				break;
			case 2:
				t.vertex(x1, y0, z1);
				t.vertex(x0, y0, z0);
				t.vertex(x0, y1, z1);
				t.vertex(x0, y1, z1);
				break;
			case 1:
				t.vertex(x1, y0, z1);
				t.vertex(x1, y1, z0);
				t.vertex(x0, y0, z0);
				t.vertex(x0, y0, z0);
				break;
			case 0:
				t.vertex(x1, y1, z1);
				t.vertex(x1, y0, z0);
				t.vertex(x0, y0, z1);
				t.vertex(x0, y0, z1);
				break;
		}
	}

	public final AABB getTileAABB(int x, int y, int z) {
		return new AABB((float)x, (float)y, (float)z, (float)(x + 1), (float)(y + 1), (float)(z + 1));
	}

	public AABB getAABB(Level level, int x, int y, int z) {
		return this.getTileAABB(x, y, z);
	}

	public boolean blocksLight() {
		return true;
	}

	public boolean isSolid() {
		return true;
	}

	public byte queryShape(Level level, int x, int y, int z) {
		return 0;
	}

	public void tick(Level level, int x, int y, int z, Random random) {
	}

	public void destroy(Level level, int x, int y, int z, ParticleEngine particleEngine) {
		byte SD = 4;

		for(int xx = 0; xx < SD; ++xx) {
			for(int yy = 0; yy < SD; ++yy) {
				for(int zz = 0; zz < SD; ++zz) {
					float xp = (float)x + ((float)xx + 0.5F) / (float)SD;
					float yp = (float)y + ((float)yy + 0.5F) / (float)SD;
					float zp = (float)z + ((float)zz + 0.5F) / (float)SD;
					particleEngine.add(new Particle(level, xp, yp, zp, xp - (float)x - 0.5F, yp - (float)y - 0.5F, zp - (float)z - 0.5F, this.tex));
				}
			}
		}

	}

	public static boolean isShapeOuterCorner(byte shape) {
		return shape == 0b1110
			|| shape == 0b1101
			|| shape == 0b1011
			|| shape == 0b0111;
	}

	public static boolean isShapeInnerCorner(byte shape) {
		return shape == 0b0001
			|| shape == 0b0010
			|| shape == 0b0100
			|| shape == 0b1000;
	}

	protected static int visualHash(int x, int y, int z, long salt) {
		int h = (int)((long)x * 18397L + (long)y * 20483L + (long)z * 29303L);
		h ^= (int)salt;
		h ^= h >>> 13;
		h *= 0x45d9f3b;
		h ^= h >>> 16;
		return h;
	}

	protected static float visualHashFloat(int x, int y, int z, long salt) {
		return (visualHash(x, y, z, salt) & 0xFFFFFF) / (float)0x1000000;
	}
}
