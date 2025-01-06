package com.leetftw.tech_mod.client.render.block.model;

import com.leetftw.tech_mod.LeetTechMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;

public abstract class LeetUnbakedModel
{
    protected class VertexData
    {
        public float x;
        public float y;
        public float z;

        public float u;
        public float v;

        public float xn;
        public float yn;
        public float zn;

        public int color;

        public VertexData()
        {
            this(0, 0, 0, 0, 0, 0, 0, 0, 0);
        }

        public VertexData(float x, float y, float z,
                                float u, float v,
                                float xn, float yn, float zn, int color)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
            this.xn = xn;
            this.yn = yn;
            this.zn = zn;
            this.color = color;
        }

        public VertexData setPosition(Vector3f position) { return setPosition(position.x, position.y, position.z); }
        public VertexData setPosition(float x, float y, float z)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public VertexData setUV(Vector2f uv) { return setUV(uv.x, uv.y); }
        public VertexData setUV(float u, float v)
        {
            this.u = u;
            this.v = v;
            return this;
        }

        public VertexData setNormal(Vector3f normal) { return setNormal(normal.x, normal.y, normal.z); }
        public VertexData setNormal(float xn, float yn, float zn)
        {
            this.xn = xn;
            this.yn = yn;
            this.zn = zn;
            return this;
        }

        public VertexData setColor(int color)
        {
            this.color = color;
            return this;
        }
        public VertexData setColor(int r, int g, int b, int a)
        {
            this.color = new Color(r, g, b, a).getRGB();
            return this;
        }
        public VertexData setColor(float r, float g, float b, float a)
        {
            this.color = new Color(r, g, b, a).getRGB();
            return this;
        }

        public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay)
        {
            RenderType renderType = getRenderType();

            Matrix4f pose = poseStack.last().pose();
            Vector3f newPos = pose.transformPosition(x, y, z, new Vector3f());

            consumer.addVertex(newPos);
            if (renderType.format.hasUV(0))
                consumer.setUv(u, v);
            if (renderType.format.hasUV(1))
                consumer.setOverlay(packedOverlay);
            if (renderType.format.hasUV(2))
                consumer.setLight(packedLight);
            if (renderType.format.hasNormal())
                consumer.setNormal(xn, yn, zn);
            if (renderType.format.hasColor())
                consumer.setColor(color);
        }
    }

    private final ArrayList<VertexData> vertices = new ArrayList<>();
    private boolean baked = false;

    protected void addExistingVertexData(VertexData vertexData)
    {
        vertices.add(vertexData);
    }

    protected VertexData createVertex()
    {
        VertexData data = new VertexData();
        addExistingVertexData(data);
        return data;
    }

    private void resetVertexData()
    {
        vertices.clear();
    }

    public void bake()
    {
        resetVertexData();
        generateVertexData();
        if (vertices.isEmpty()) LeetTechMod.LOGGER.warn("Model '" + getClass().getSimpleName() + "' baked, but has no vertices!");
        baked = true;
    }

    public void reset()
    {
        resetVertexData();
        baked = false;
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {
        if (!baked) return;

        VertexConsumer consumer = bufferSource.getBuffer(getRenderType());

        for (VertexData vertexData : vertices)
            vertexData.render(poseStack, consumer, packedLight, packedOverlay);
    }

    protected abstract RenderType getRenderType();
    protected abstract void generateVertexData();
}
