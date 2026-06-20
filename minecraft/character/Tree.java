package com.mojang.minecraft.character;

import com.mojang.minecraft.Entity;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.renderer.Tesselator;
import com.mojang.minecraft.renderer.Textures;
import org.lwjgl.opengl.GL11;

public class Tree extends Entity {
    private final Textures textures;

    public Tree(Level level, Textures textures, float x, float y, float z) {
        super(level);
        this.textures = textures;
        this.setPos(x, y, z);
        this.setSize(1.0F, 3.0F);
    }

    public void resetPos() {
        super.resetPos();
        this.setSize(0.01F, 3.0F);
        do {
            this.move(0.0F, -1.0F, 0.0F);
        } while(!this.onGround);
        this.setSize(1.0F, 3.0F);
    }

    public void render(float a) {
        Tesselator t = Tesselator.instance;

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.textures.loadTexture("/tree.png", GL11.GL_LINEAR));
        GL11.glPushMatrix();

        float x = this.xo + (this.x - this.xo) * a;
        float y = this.yo + (this.y - this.yo) * a;
        float z = this.zo + (this.z - this.zo) * a;
        GL11.glTranslatef(x, y, z);
        GL11.glRotatef(-this.level.player.yRot, 0.0F, 1.0F, 0.0F);

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

        GL11.glPopMatrix();
    }
}
