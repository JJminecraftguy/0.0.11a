package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.level.Level;

import java.util.Random;

public class TerrainTile extends Tile {
    private static final Random colorRand = new Random();

    protected TerrainTile(int id, int tex) {
        super(id, tex);
    }

    public byte queryShape(Level level, int x, int y, int z) {
        if(level.isSolidTile(x, y + 1, z)) {
            if(level.isSolidTile(x, y + 2, z)) {
                return 0;
            }

            Tile tile = Tile.tiles[level.getTile(x, y, z)];
            int aboveShape = tile.queryShape(level, x, y + 1, z);

            byte shape;
            int xo, zo;
            switch(aboveShape) {
                case 0b1110:
                    shape = 0b1000;
                    xo = 0;
                    zo = 0;
                    break;
                case 0b1101:
                    shape = 0b0100;
                    xo = 0;
                    zo = -1;
                    break;
                case 0b1011:
                    shape = 0b0010;
                    xo = -1;
                    zo = 0;
                    break;
                case 0b0111:
                    shape = 0b0001;
                    xo = -1;
                    zo = -1;
                    break;
                default:
                    return 0;
            }

            int solids = 0;
            for(int i = 0; i < 4; i++) {
                int xx = x + xo + (i & 1);
                int zz = z + zo + (i >> 1);
                if(level.isSolidTile(xx, y, zz)) {
                    solids++;
                }
            }

            if(solids == 4) {
                return 0;
            }
            return shape;
        }

        if(!level.isSolidTile(x, y - 1, z)) {
            return 0;
        }

        byte shape = 0;
        int topIndentCount = 0;

        boolean[] solidNeighbors = new boolean[9];
        for(int i = 0; i < 9; i++) {
            solidNeighbors[i] = level.isSolidTile(x - 1 + (i % 3), y, z - 1 + (i / 3));
        }

        for(int i = 0; i < 4; i++) {
            int xo = i >> 1;
            int zo = i & 1;

            for(int j = 0; j < 4; j++) {
                boolean solid = solidNeighbors[((j & 1) + xo) + ((j >> 1) + zo) * 3];
                if(!solid) {
                    shape |= (byte)(1 << i);
                    topIndentCount++;
                    break;
                }
            }
        }

        if(topIndentCount == 4) {
            shape = -128;
        }
        return shape;
    }

    protected int getColor(Level level, int x, int y, int z) {
        setVisualRandSeed(x, y, z, 0);
        int r = 192 + visualRand.nextInt(64);
        int g = 192 + visualRand.nextInt(64);
        int b = 192 + visualRand.nextInt(64);
        return r << 16 | g << 8 | b;
    }
}
