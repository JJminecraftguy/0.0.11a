package com.mojang.minecraft.level.tile;

import com.mojang.minecraft.Player;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.renderer.Tesselator;
import org.lwjgl.opengl.GL11;

public class TreeTile extends Bush {
    private int renderList;

    protected TreeTile(int id) {
        super(id);
    }

    public boolean isDynamicallyRendered() {
        return true;
    }

    public String getDynamicTexture() {
        return "/tree.png";
    }

    public void render(Tesselator t, Level level, Player player, int layer, int x, int y, int z) {
        if(level.isLit(x, y, z) ^ layer != 1) {
            return;
        }

        if(this.renderList == 0) {
            this.renderList = GL11.glGenLists(1);
            GL11.glNewList(this.renderList, GL11.GL_COMPILE);
            t.init();
            t.color(1.0F, 1.0F, 1.0F);
            t.vertexUV(-1.0F, 0.0F, 0.0F, 1.0F, 1.0F);
            t.vertexUV(+1.0F, 0.0F, 0.0F, 0.0F, 1.0F);
            t.vertexUV(+1.0F, 4.0F, 0.0F, 0.0F, 0.0F);
            t.vertexUV(-1.0F, 4.0F, 0.0F, 1.0F, 0.0F);
            t.vertexUV(+1.0F, 0.0F, 0.0F, 0.0F, 1.0F);
            t.vertexUV(-1.0F, 0.0F, 0.0F, 1.0F, 1.0F);
            t.vertexUV(-1.0F, 4.0F, 0.0F, 1.0F, 0.0F);
            t.vertexUV(+1.0F, 4.0F, 0.0F, 0.0F, 0.0F);
            t.flush();
            GL11.glEndList();
        }

        float xf = x + 0.5F;
        float zf = z + 0.5F;

        setVisualRandSeed(x, y, z, "Tree".hashCode());
        xf += visualRand.nextFloat() * 0.5F - 0.25F;
        zf += visualRand.nextFloat() * 0.5F - 0.25F;

        GL11.glPushMatrix();
        GL11.glTranslatef(xf, y, zf);
        GL11.glRotatef(-player.yRot, 0.0F, 1.0F, 0.0F);
        GL11.glCallList(this.renderList);
        GL11.glPopMatrix();
    }
}
