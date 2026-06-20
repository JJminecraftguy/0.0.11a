package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.Player;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.renderer.Tesselator;

public final class DynamicTileInfo {
    public final Tile tile;
    public final int x, y, z;

    public DynamicTileInfo(Tile tile, int x, int y, int z) {
        this.tile = tile;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void render(Tesselator t, Level level, Player player, int layer) {
        this.tile.render(t, level, player, layer, this.x, this.y, this.z);
    }
}
