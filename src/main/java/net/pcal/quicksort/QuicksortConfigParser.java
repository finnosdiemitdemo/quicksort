package net.pcal.quicksort;

import com.google.gson.Gson;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.pcal.quicksort.QuicksortConfig.QuicksortChestConfig;
import org.apache.logging.log4j.Level;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

class QuicksortConfigParser {

    static QuicksortConfig parse(final InputStream in, QuicksortChestConfig defaultChestConfig) throws IOException {
        final List<QuicksortChestConfig> chests = new ArrayList<>();
        final String rawJson = stripComments(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        final Gson gson = new Gson();

        final QuicksortConfigGson configGson = gson.fromJson(rawJson, QuicksortConfigGson.class);
        for (QuicksortChestConfigGson chestGson : configGson.quicksortChests) {
            chests.add(defaultChestConfig = createWithDefaults(defaultChestConfig,
                chestGson.baseBlockId,
                chestGson.range,
                chestGson.cooldownTicks,
                chestGson.animationTicks,
                chestGson.soundVolume,
                chestGson.soundPitch,
                chestGson.nbtMatchEnabledIds,
                chestGson.targetContainerIds,
                chestGson.sortingGroups != null
                    ? new ArrayList<>(chestGson.sortingGroups)
                    : new ArrayList<>(),
                chestGson.supportItemFrames
            ));
        }
        // adjust logging to configured level
        final String configuredLevel = configGson.logLevel;
        final Level logLevel = Level.getLevel(configuredLevel);
        if (logLevel == null) throw new IllegalArgumentException("Invalid logLevel " + configuredLevel);
        return new QuicksortConfig(Collections.unmodifiableList(chests), logLevel);
    }

    static QuicksortChestConfig createWithDefaults(
        QuicksortChestConfig dflt,
        String baseBlockId,
        Integer range,
        Integer cooldownTicks,
        Integer animationTicks,
        Float soundVolume,
        Float soundPitch,
        Collection<String> nbtMatchEnabledIds,
        Collection<String> targetContainerIds,
        Collection<Collection<String>> sortingGroups,
        boolean supportItemFrames
    ) {
        return new QuicksortChestConfig(
            Identifier.of(requireNonNull(baseBlockId, "baseBlockId is required")),
            requireNonNull(range != null ? range : dflt == null ? null : dflt.range(),
                "range is required"),
            requireNonNull(cooldownTicks != null ? cooldownTicks : dflt == null ? null : dflt.cooldownTicks(),
                "cooldownTicks is required"),
            requireNonNull(animationTicks != null ? animationTicks : dflt == null ? null : dflt.animationTicks(),
                "animationTicks is required"),
            requireNonNull(soundVolume != null ? soundVolume : dflt == null ? null : dflt.soundVolume(),
                "soundVolume is required"),
            requireNonNull(soundPitch != null ? soundPitch : dflt == null ? null : dflt.soundPitch(),
                "soundPitch is required"),
            requireNonNull(nbtMatchEnabledIds != null ? toIdentifierSet(nbtMatchEnabledIds) : dflt == null ? null :
                                                                                              dflt.nbtMatchEnabledIds(),
                "nbtMatchEnabledIds"),
            requireNonNull(targetContainerIds != null ? toIdentifierSet(targetContainerIds) : dflt == null ? null :
                                                                                              dflt.targetContainerIds(),
                "targetContainerIds"),
            sortingGroups == null ? new HashSet<>() :
            sortingGroups.stream()
                .filter(Objects::nonNull)
               .map(QuicksortConfigParser::toTags)
               .collect(Collectors.toSet()),
            supportItemFrames
        );
    }

    private static Set<QuicksortChestConfig.SortingGroupItem> toTags(final Collection<String> s) {
        return s.stream()
                .map(QuicksortConfigParser::toTag)
                .collect(Collectors.toSet());
    }

    private static QuicksortChestConfig.SortingGroupItem toTag(final String s) {
        if (s.contains(":")) {
            final var split = s.split(":");
            if (s.contains("*")) {
                return new QuicksortChestConfig.SortingGroupItem.ItemIdWithWildcard(
                    split[0], split[1]
                );
            }
            return new QuicksortChestConfig.SortingGroupItem.ItemId(
                Identifier.of(split[0], split[1])
            );
        }
        return new QuicksortChestConfig.SortingGroupItem.Tag(
            TagKey.of(RegistryKeys.ITEM, Identifier.of(s))
        );
    }

    private static Set<Identifier> toIdentifierSet(Collection<String> nbtMatchEnabledIds) {
        final Set<Identifier> set = new HashSet<>();
        for (String id : nbtMatchEnabledIds) set.add(Identifier.of(id));
        return set;
    }

    // ===================================================================================
    // Private methods

    private static String stripComments(String json) throws IOException {
        final StringBuilder out = new StringBuilder();
        final BufferedReader br = new BufferedReader(new StringReader(json));
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.strip().startsWith(("//"))) out.append(line).append('\n');
        }
        return out.toString();
    }

    // ===================================================================================
    // Gson object model

    public static class QuicksortConfigGson {
        List<QuicksortChestConfigGson> quicksortChests;
        String logLevel;
    }

    public static class QuicksortChestConfigGson {
        String baseBlockId;
        Integer range;
        Integer cooldownTicks;
        Integer animationTicks;
        Float soundVolume;
        Float soundPitch;
        List<String> nbtMatchEnabledIds;
        List<String> targetContainerIds;
        List<List<String>> sortingGroups;
        boolean supportItemFrames;
    }
}
