package com.leetftw.tech_mod.datagen.texture;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class TextureProvider implements DataProvider
{
    private class Entry
    {
        public List<ResourceLocation> sourceTextures;
        public ResourceLocation destinationTexture;
        public Function<List<BufferedImage>, BufferedImage> textureGenerator;

        public Entry(List<ResourceLocation> sourceTextures, ResourceLocation destinationTexture, Function<List<BufferedImage>, BufferedImage> textureGenerator)
        {
            this.sourceTextures = sourceTextures;
            this.destinationTexture = destinationTexture;
            this.textureGenerator = textureGenerator;
        }
    }

    private final HashMap<ResourceLocation, BufferedImage> imageBuffer = new HashMap<>();
    private final ArrayList<Entry> queue = new ArrayList<>();

    private final String modid;

    private final Path sourceAssetsFolder;
    private final Path destAssetsFolder;

    public TextureProvider(PackOutput packOutput, String modid)
    {
        sourceAssetsFolder = packOutput.getOutputFolder().getParent().getParent().resolve("main/resources/assets");
        destAssetsFolder = packOutput.getOutputFolder().resolve("assets");
        this.modid = modid;
    }

    protected abstract void registerTextureGenerators();

    protected <T> void fromExisting(List<ResourceLocation> sourceTextures, ResourceLocation destinationTexture, BiFunction<List<BufferedImage>, T, BufferedImage> textureGenerator, T additionalData)
    {
        queue.add(new Entry(sourceTextures, destinationTexture, (a) -> textureGenerator.apply(a, additionalData)));
    }

    protected void fromExisting(List<ResourceLocation> sourceTextures, ResourceLocation destinationTexture, Function<List<BufferedImage>, BufferedImage> textureGenerator)
    {
        queue.add(new Entry(sourceTextures, destinationTexture, textureGenerator));
    }

    protected void fromScratch(ResourceLocation destinationTexture, Supplier<BufferedImage> textureGenerator)
    {
        queue.add(new Entry(new ArrayList<>(), destinationTexture, (a) -> textureGenerator.get()));
    }

    private Path getTexturePathFromResourceLoc(Path basePath, ResourceLocation location)
    {
        return basePath.resolve(location.getNamespace() + "/textures/" + location.getPath() + ".png");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput)
    {
        registerTextureGenerators();

        CompletableFuture<?>[] futures = new CompletableFuture[this.queue.size()];
        for (int i = 0; i < queue.size(); i++)
        {
            Entry entry = queue.get(i);
            futures[i] = CompletableFuture.runAsync(() ->
            {
                // Go through all required textures first and load them
                for (int j = 0; j < entry.sourceTextures.size(); j++)
                {
                    ResourceLocation textureLocation = entry.sourceTextures.get(j);
                    if (!imageBuffer.containsKey(textureLocation))
                    {
                        Path textureFile = getTexturePathFromResourceLoc(sourceAssetsFolder, textureLocation);
                        try
                        {
                            BufferedImage image = ImageIO.read(textureFile.toFile());
                            imageBuffer.put(textureLocation, image);
                        }
                        catch (IOException e)
                        {
                            throw new RuntimeException("Failed to open texture for " + textureLocation.toString() + " (full path: " + textureFile + ")", e);
                        }
                    }
                }

                // Now that all images are loaded we can map the resource ids to the images
                List<BufferedImage> images = entry.sourceTextures.stream().map(imageBuffer::get).toList();

                // Now we retrieve the generated image
                BufferedImage generatedImage = entry.textureGenerator.apply(images);

                // Save it
                ResourceLocation textureLocation = entry.destinationTexture;
                Path textureFile = getTexturePathFromResourceLoc(destAssetsFolder, textureLocation);
                try
                {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    HashingOutputStream hashingStream = new HashingOutputStream(Hashing.sha1(), outputStream);
                    ImageIO.write(generatedImage, "png", hashingStream);
                    cachedOutput.writeIfNeeded(textureFile, outputStream.toByteArray(), hashingStream.hash());
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Failed to save texture for " + textureLocation.toString(), e);
                }
            });
        }
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "Textures: " + modid;
    }
}
