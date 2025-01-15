package com.leetftw.tech_mod.client.render.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;

// A Minecraft baked model is not compatible with this shape.
// BakedModel only supports normals defined as the Direction enum
// Since this model uses more complex shapes than squares, a custom
// class is used here to pre-generate vertex data
// Performance improvement is minimal vs generating vertices each
// frame but I expect that with multiple energy rings in the world this
// can really help.
public class EnergyRingUnbakedModel extends LeetUnbakedModel
{
    TextureAtlasSprite sprite;
    float d; float d_prime;
    float delta_alpha; float delta_beta;

    public EnergyRingUnbakedModel(TextureAtlasSprite sprite, float d, float d_prime, int delta_alpha, int delta_beta)
    {
        this.sprite = sprite;
        this.d = d;
        this.d_prime = d_prime;
        this.delta_alpha = delta_alpha;
        this.delta_beta = delta_beta;
    }

    @Override
    protected RenderType getRenderType() {
        return RenderType.SOLID;
    }

    @Override
    protected void generateVertexData() {
        // Calculate all vertices first
        int numRings = (int) (360f / delta_alpha);
        int numSegments = (int) (360f / delta_beta);

        Vector2f[][] textureCoords = new Vector2f[numRings + 1][numSegments + 1];
        Vector3f[][] vertices = new Vector3f[numRings + 1][numSegments + 1];

        // I'm going to be honest here, I had some trouble getting this working
        // with just the raw math. I understand the math behind drawing a torus,
        // but I just couldn't get it to work in Minecraft. Everything I did in
        // desmos did not seem to translate well to Java.

        // But ClaudeAI could. At least partially, it was able to generate proper
        // vertices, but there were some issues with mutating functions.

        // Generate vertices
        for (int i = 0; i <= numRings; i++)
        {
            float alpha = (float) (i * 2 * Math.PI / numRings); // Convert to radians directly

            for (int j = 0; j <= numSegments; j++)
            {
                float beta = (float) (j * 2 * Math.PI / numSegments); // Convert to radians directly

                // Parametric equations for torus
                float x = (float) ((d + d_prime * Math.cos(beta)) * Math.cos(alpha));
                float y = (float) (d_prime * Math.sin(beta));
                float z = (float) ((d + d_prime * Math.cos(beta)) * Math.sin(alpha));

                vertices[i][j] = new Vector3f(x, y, z);

                // Map U based on angle around major radius (i)
                // Map V based on angle around tube (j)
                float u = ((float) i / numRings);
                float v = (float) j / numSegments;

                // Map to sprite coordinates
                float textureU = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * u;
                float textureV = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * v;
                textureCoords[i][j] = new Vector2f(textureU, textureV);
            }
        }

        // Generate quads
        for (int i = 0; i < numRings; i++)
        {
            for (int j = 0; j < numSegments; j++)
            {
                // Took me two full days to figure out Vector3f.add mutates the object
                // instead of creating a new modified copy.

                // Calculate the center of the quad
                Vector3f center = new Vector3f(vertices[i][j]) // Start with a copy of the first vertex
                        .add(vertices[i + 1][j])
                        .add(vertices[i + 1][j + 1])
                        .add(vertices[i][j + 1])
                        .mul(0.25f); // Average the positions to find the center

                // Calculate the vector pointing to the center of the torus ring
                Vector3f toCenter = new Vector3f(
                        (float) (center.x - d * Math.cos(i * 2 * Math.PI / numRings)), // X
                        center.y, // Y (remains the same for a torus because the shape's center is always at y=0)
                        (float) (center.z - d * Math.sin(i * 2 * Math.PI / numRings))  // Z
                );

                // Normalize the vector to get the normal of the quad
                Vector3f norm = toCenter.normalize();

                int packedLight = 0x00F000F0;
                int packedOverlay = 0;
                int color = Color.YELLOW.getRGB();

                // Create quad
                int k = i, l = j;
                createVertex().setPosition(vertices[k][l]).setUV(textureCoords[k][l])
                        .setNormal(norm).setColor(color);
                l = j + 1;
                createVertex().setPosition(vertices[k][l]).setUV(textureCoords[k][l])
                        .setNormal(norm).setColor(color);
                k = i + 1;
                createVertex().setPosition(vertices[k][l]).setUV(textureCoords[k][l])
                        .setNormal(norm).setColor(color);
                l = j;
                createVertex().setPosition(vertices[k][l]).setUV(textureCoords[k][l])
                        .setNormal(norm).setColor(color);
            }
        }
    }
}
