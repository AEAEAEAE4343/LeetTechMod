package com.leetftw.tech_mod.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.joml.Vector3f;

import java.util.Arrays;

// Big shoutout to team CoFH!
public class RetexturedBakedQuad extends BakedQuad
{
    private final TextureAtlasSprite texture;

    public RetexturedBakedQuad(BakedQuad quad, TextureAtlasSprite newTexture)
    {
        super(Arrays.copyOf(quad.getVertices(), quad.getVertices().length), quad.getTintIndex(), calculateFacing(quad.getVertices()), quad.getSprite(), quad.isShade(), quad.getLightEmission(), quad.hasAmbientOcclusion());
        this.texture = newTexture;
        this.remapQuad();
    }

    private void remapQuad()
    {
        for (int i = 0; i < 4; ++i)
        {
            int j = DefaultVertexFormat.BLOCK.getVertexSize() * i;
            int uvIndex = 4;
            this.vertices[j + uvIndex] = Float.floatToRawIntBits(this.texture.getU(getUnInterpolatedU(this.sprite, Float.intBitsToFloat(this.vertices[j + uvIndex]))));
            this.vertices[j + uvIndex + 1] = Float.floatToRawIntBits(this.texture.getV(getUnInterpolatedV(this.sprite, Float.intBitsToFloat(this.vertices[j + uvIndex + 1]))));
        }
    }

    private static Direction calculateFacing(int[] faceData)
    {
        Vector3f vector3f = new Vector3f(Float.intBitsToFloat(faceData[0]), Float.intBitsToFloat(faceData[1]), Float.intBitsToFloat(faceData[2]));
        Vector3f vector3f1 = new Vector3f(Float.intBitsToFloat(faceData[8]), Float.intBitsToFloat(faceData[9]), Float.intBitsToFloat(faceData[10]));
        Vector3f vector3f2 = new Vector3f(Float.intBitsToFloat(faceData[16]), Float.intBitsToFloat(faceData[17]), Float.intBitsToFloat(faceData[18]));
        Vector3f vector3f3 = (new Vector3f(vector3f)).sub(vector3f1);
        Vector3f vector3f4 = (new Vector3f(vector3f2)).sub(vector3f1);
        Vector3f vector3f5 = (new Vector3f(vector3f4)).cross(vector3f3).normalize();
        if (!vector3f5.isFinite()) {
            return Direction.UP;
        } else {
            Direction direction = null;
            float f = 0.0F;

            for(Direction direction1 : Direction.values()) {
                Vec3i vec3i = direction1.getUnitVec3i();
                Vector3f vector3f6 = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
                float f1 = vector3f5.dot(vector3f6);
                if (f1 >= 0.0F && f1 > f) {
                    f = f1;
                    direction = direction1;
                }
            }

            return direction == null ? Direction.UP : direction;
        }
    }

    @Override
    public TextureAtlasSprite getSprite()
    {
        return texture;
    }

    private static float getUnInterpolatedU(TextureAtlasSprite sprite, float u)
    {
        float f = sprite.getU1() - sprite.getU0();
        return (u - sprite.getU0()) / f;
    }

    private static float getUnInterpolatedV(TextureAtlasSprite sprite, float v)
    {
        float f = sprite.getV1() - sprite.getV0();
        return (v - sprite.getV0()) / f;
    }
}