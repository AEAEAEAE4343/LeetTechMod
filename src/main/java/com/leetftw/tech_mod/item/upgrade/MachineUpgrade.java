package com.leetftw.tech_mod.item.upgrade;

import com.leetftw.tech_mod.LeetTechMod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.List;
import java.util.Objects;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class MachineUpgrade
{
    public static final ResourceKey<Registry<MachineUpgrade>> MACHINE_UPGRADE_REGISTRY_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "machine_upgrades"));

    public static final Codec<MachineUpgrade> REGISTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(MachineUpgrade::getUpgradeId),
            Codec.FLOAT.fieldOf("speed_multiplier").forGetter(MachineUpgrade::getSpeedMultiplier),
            Codec.FLOAT.fieldOf("efficiency_multiplier").forGetter(MachineUpgrade::getEfficiencyMultiplier),
            Codec.BOOL.fieldOf("machine_specific").forGetter(MachineUpgrade::isMachineSpecific)
    ).apply(instance, MachineUpgrade::new));

    private final float speedMultiplier;
    private final float efficiencyMultiplier;
    private final ResourceLocation upgradeId;
    private final boolean machineSpecific;

    public static final ResourceLocation BLANK_KEY = ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "blank");
    public static final MachineUpgrade BLANK = new MachineUpgrade(BLANK_KEY, 1f, 1f, true);

    public MachineUpgrade(ResourceLocation upgradeId, float speed, float efficiency, boolean machineSpecific)
    {
        this.speedMultiplier = speed;
        this.efficiencyMultiplier = efficiency;
        this.upgradeId = upgradeId;
        this.machineSpecific = machineSpecific;
    }

    public ResourceLocation getUpgradeId()
    {
        return upgradeId;
    }

    public float getSpeedMultiplier()
    {
        return speedMultiplier;
    }

    public float getEfficiencyMultiplier()
    {
        return efficiencyMultiplier;
    }

    public boolean isMachineSpecific()
    {
        return machineSpecific;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof MachineUpgrade that)) return false;
        return hashCode() == that.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(upgradeId, speedMultiplier, efficiencyMultiplier, machineSpecific);
    }

    public static List<MachineUpgrade> getRegisteredUpgrades(HolderLookup.Provider access)
    {
        return access.lookupOrThrow(MACHINE_UPGRADE_REGISTRY_KEY).listElements().map(Holder.Reference::value).toList();
    }

    public static MachineUpgrade fromId(RegistryAccess access, ResourceLocation id)
    {
        return access.lookupOrThrow(MACHINE_UPGRADE_REGISTRY_KEY).getOrThrow(ResourceKey.create(MACHINE_UPGRADE_REGISTRY_KEY, id)).value();
    }

    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                // The registry key.
                MACHINE_UPGRADE_REGISTRY_KEY,
                // The codec of the registry contents.
                MachineUpgrade.REGISTRY_CODEC,
                // The network codec of the registry contents. Often identical to the normal codec.
                // May be a reduced variant of the normal codec that omits data that is not needed on the client.
                // May be null. If null, registry entries will not be synced to the client at all.
                // May be omitted, which is functionally identical to passing null (a method overload
                // with two parameters is called that passes null to the normal three parameter method).
                MachineUpgrade.REGISTRY_CODEC
        );
    }
}
