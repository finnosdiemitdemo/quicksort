package net.pcal.quicksort;

import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.Level;

import java.util.List;
import java.util.Set;

public record QuicksortConfig(
        List<QuicksortChestConfig> chestConfigs,
        Level logLevel
) {

    public record QuicksortChestConfig(
            Identifier baseBlockId,
            int range,
            int cooldownTicks,
            int animationTicks,
            float soundVolume,
            float soundPitch,
            Set<Identifier> nbtMatchEnabledIds,
            Set<Identifier> targetContainerIds,
            Set<Set<SortingGroupItem>> sortingGroups
    ) {
        public interface SortingGroupItem {
            record Tag (TagKey<Item> tagKey) implements SortingGroupItem {}
            record ItemId(Identifier identifier) implements SortingGroupItem {}
            record ItemIdWithWildcard(String namespace, String path) implements SortingGroupItem {}
        }
    }
}

