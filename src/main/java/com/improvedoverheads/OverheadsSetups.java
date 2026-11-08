package com.improvedoverheads;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ConfigProfile;

@Singleton
final class OverheadsSetups
{
    static final String STORAGE_KEY = "savedSetups";
    static final String THEMES_KEY = "savedHitsplatThemes";
    static final int MAX_JSON = 524288;
    private static final int MAX_SAVED = 12;
    private static final int MAX_STORAGE = 4194304;
    private final ConfigManager manager;
    private final Gson gson;
    final OverheadsSetupSchema schema;
    private final java.util.concurrent.atomic.AtomicLong generation = new java.util.concurrent.atomic.AtomicLong();

    static final class Session
    {
        private final Long profile;
        private final long generation;

        private Session(Long profile, long generation)
        {
            this.profile = profile;
            this.generation = generation;
        }
    }

    static final class Change
    {
        final String key;
        final String label;
        final JsonElement before;
        final JsonElement after;
        private final Object value;

        private Change(String key, String label, JsonElement before, JsonElement after, Object value)
        {
            this.key = key;
            this.label = label;
            this.before = before;
            this.after = after;
            this.value = value;
        }
    }

    static final class Preview
    {
        final String name;
        final List<Change> changes;
        final int ignored;
        private final Session session;

        private Preview(String name, List<Change> changes, int ignored, Session session)
        {
            this.name = name;
            this.changes = Collections.unmodifiableList(changes);
            this.ignored = ignored;
            this.session = session;
        }
    }

    @Inject
    OverheadsSetups(OverheadsConfig config, ConfigManager manager, Gson gson)
    {
        this.manager = manager;
        this.gson = gson;
        schema = new OverheadsSetupSchema(config);
    }

    private OverheadsProfiles profiles;
    private OverheadsProfiles.Scope themeScope = OverheadsProfiles.Scope.SELF;
    void setThemeScope(OverheadsProfiles.Scope scope) { themeScope = scope; }
    String themeActor() { return themeScope.label; }
    @Inject void configureProfiles(OverheadsProfiles profiles)
    {
        this.profiles = profiles;
        schema.addProfiles(profiles);
    }

    Session session()
    {
        return new Session(profileId(), generation.get());
    }

    private Long profileId()
    {
        ConfigProfile profile = manager.getProfile();
        return profile == null ? null : profile.getId();
    }

    void invalidate()
    {
        generation.incrementAndGet();
    }

    void check(Session session)
    {
        if (session == null || session.generation != generation.get() || !Objects.equals(session.profile, profileId()))
        {
            throw new IllegalStateException("The profile changed. Reopen Setups.");
        }
    }

    String exportCurrent(String name, Session session)
    {
        return exportCurrent(name, session, false);
    }

    String exportCurrent(String name, Session session, boolean themes)
    {
        check(session);
        JsonObject values = schema.snapshot();
        if (themes && profiles != null)
        {
            values = new OverheadsSetupSchema(profiles.config(themeScope)).snapshot();
        }
        String result = gson.toJson(document(validName(name), themes ? OverheadsHitsplatThemes.select(values) : values));
        check(session);
        return result;
    }

    String themePreset(OverheadsHitsplatStyle style, OverheadsHitsplatMotion motion, Session session)
    {
        check(session);
        JsonObject values = OverheadsHitsplatThemes.select(new OverheadsSetupSchema(new OverheadsConfig() {}).snapshot());
        values.addProperty("hitsplatStyle", Objects.requireNonNull(style).name());
        values.addProperty("hitsplatMotion", Objects.requireNonNull(motion).name());
        if (motion != OverheadsHitsplatMotion.FIXED)
        {
            values.addProperty("fadeInDuration", 40);
            values.addProperty("fullOpacityDuration", 1100);
            values.addProperty("fadeOutDuration", 400);
        }
        return gson.toJson(document(style.toString() + " theme", values));
    }

    private static JsonObject document(String name, JsonObject settings)
    {
        JsonObject root = new JsonObject();
        root.addProperty("format", "improved-overheads");
        root.addProperty("version", 2);
        root.addProperty("name", name);
        root.add("settings", settings);
        return root;
    }

    Preview preview(String json, Session session)
    {
        return preview(json, session, false);
    }

    Preview preview(String json, Session session, boolean themes)
    {
        check(session);
        JsonObject root = readObject(json, MAX_JSON);
        if (!"improved-overheads".equals(OverheadsSetupSchema.stringValue(root.get("format"), 64)))
        {
            throw new IllegalArgumentException("This is not an Improved Overheads setup.");
        }
        int version = OverheadsSetupSchema.integerValue(root.get("version"), 1, Integer.MAX_VALUE);
        if (version > 2)
        {
            throw new IllegalArgumentException("This setup version is not supported.");
        }
        String name = validName(OverheadsSetupSchema.stringValue(root.get("name"), 64));
        JsonElement settingsValue = root.get("settings");
        if (settingsValue == null || !settingsValue.isJsonObject())
        {
            throw new IllegalArgumentException("The setup has no settings object.");
        }
        if (version == 1 && profiles != null && !themes)
        {
            JsonObject legacy = settingsValue.getAsJsonObject().deepCopy();
            String[][] aliases = {{"prayerRestorePreview", "restorePreview"}, {"prayerRestorePreviewColor", "restorePreviewColor"}, {"prayerLowColor", "lowResourceColor"}};
            for (String[] pair : aliases)
            {
                if (!legacy.has(pair[0]) && legacy.has(pair[1]))
                {
                    legacy.add(pair[0], legacy.get(pair[1]));
                    settingsValue.getAsJsonObject().add(pair[0], legacy.get(pair[1]));
                }
            }
            for (OverheadsProfiles.Scope scope : OverheadsProfiles.Scope.values())
            {
                if (scope == OverheadsProfiles.Scope.SELF) { continue; }
                legacy.entrySet().forEach(entry ->
                {
                    String key = OverheadsSetupSchema.prefix(scope) + entry.getKey();
                    if (schema.entries.containsKey(key) && !settingsValue.getAsJsonObject().has(key))
                    { settingsValue.getAsJsonObject().add(key, entry.getValue()); }
                });
            }
        }
        if (version == 2 && profiles != null && !themes
            && settingsValue.getAsJsonObject().keySet().stream().noneMatch(key -> key.startsWith("party.")))
        {
            JsonObject existing = settingsValue.getAsJsonObject().deepCopy();
            existing.entrySet().forEach(entry ->
            {
                if (entry.getKey().startsWith("players."))
                {
                    String key = "party." + entry.getKey().substring("players.".length());
                    if (schema.entries.containsKey(key)) settingsValue.getAsJsonObject().add(key, entry.getValue());
                }
            });
        }
        List<Change> changes = new ArrayList<>();
        int ignored = 0;
        for (Map.Entry<String, JsonElement> setting : settingsValue.getAsJsonObject().entrySet())
        {
            String key = themes && profiles != null && setting.getKey().indexOf('.') < 0
                ? OverheadsSetupSchema.prefix(themeScope) + setting.getKey() : setting.getKey();
            OverheadsSetupSchema.Entry<?> entry = schema.entries.get(key);
            if (entry == null || setting.getKey().equals("partyDisplay") || themes && !OverheadsHitsplatThemes.includes(setting.getKey()))
            {
                ignored++;
                continue;
            }
            Change change = decode(entry, setting.getValue());
            if (!change.before.equals(change.after))
            {
                changes.add(change);
            }
        }
        check(session);
        return new Preview(name, changes, ignored, session);
    }

    private static <T> Change decode(OverheadsSetupSchema.Entry<T> entry, JsonElement value)
    {
        try
        {
            T decoded = entry.decode.apply(value);
            return new Change(entry.key, entry.label, entry.current(), entry.encode.apply(decoded), decoded);
        }
        catch (RuntimeException ex)
        {
            throw new IllegalArgumentException(entry.label + ": " + ex.getMessage(), ex);
        }
    }

    Preview reverse(Preview applied)
    {
        if (applied == null) { throw new IllegalStateException("Apply a setup first."); }
        check(applied.session);
        List<Change> reversed = new ArrayList<>();
        for (Change change : applied.changes)
        {
            OverheadsSetupSchema.Entry<?> entry = schema.entries.get(change.key);
            reversed.add(new Change(change.key, change.label, change.after, change.before, entry.decode.apply(change.before)));
        }
        return new Preview("Undo " + applied.name, reversed, 0, applied.session);
    }

    int apply(Preview preview)
    {
        if (preview == null)
        {
            throw new IllegalStateException("Preview a setup first.");
        }
        check(preview.session);
        for (Change change : preview.changes)
        {
            if (!schema.entries.get(change.key).current().equals(change.before))
            {
                throw new IllegalStateException("Settings changed since the preview. Preview again before applying.");
            }
        }
        int applied = 0;
        for (Change change : preview.changes)
        {
            check(preview.session);
            OverheadsSetupSchema.Entry<?> entry = schema.entries.get(change.key);
            manager.setConfiguration(entry.group, entry.storageKey, change.value);
            applied++;
        }
        return applied;
    }

    String copyGroup(int source, int destination, Session session)
    {
        check(session);
        if (source < 0 || source > (profiles == null ? 2 : 3) || destination < 0 || destination > (profiles == null ? 2 : 3) || source == destination)
        {
            throw new IllegalArgumentException("Choose two different actor groups.");
        }
        if (profiles != null)
        {
            OverheadsProfiles.Scope from = OverheadsProfiles.Scope.values()[source];
            OverheadsProfiles.Scope to = OverheadsProfiles.Scope.values()[destination];
            JsonObject copied = new JsonObject();
            OverheadsSetupSchema fromSchema = new OverheadsSetupSchema(profiles.config(from));
            for (OverheadsSetupSchema.Entry<?> entry : fromSchema.entries.values())
            {
                if (entry.key.equals("partyDisplay")) { continue; }
                String sourceKey = OverheadsProfiles.actorKey(entry.key, from);
                copied.add(OverheadsSetupSchema.prefix(to) + entry.key, fromSchema.entries.get(sourceKey).current());
            }
            return gson.toJson(document("Copied actor settings", copied));
        }
        String[][] keys = {
            {"selfHealthDisplay", "otherHealthDisplay", "npcHealthDisplay"},
            {"selfNameVisibility", "playerNameVisibility", "npcNameVisibility"},
            {"disableMyHitsplats", "disableAllyHitsplats", "disableEnemyHitsplats"}
        };
        JsonObject values = new JsonObject();
        for (String[] group : keys)
        {
            values.add(group[destination], schema.entries.get(group[source]).current());
        }
        if (source != 0 && destination != 0)
        {
            values.add(destination == 1 ? "otherHealthVisibility" : "npcHealthVisibility",
                schema.entries.get(source == 1 ? "otherHealthVisibility" : "npcHealthVisibility").current());
        }
        return gson.toJson(document("Copied group visibility", values));
    }

    Map<String, String> saved(Session session)
    {
        return saved(session, false);
    }

    Map<String, String> saved(Session session, boolean themes)
    {
        check(session);
        String stored = manager.getConfiguration(OverheadsConfig.GROUP, themes ? THEMES_KEY : STORAGE_KEY);
        Map<String, String> result = new LinkedHashMap<>();
        if (stored == null || stored.isEmpty())
        {
            return result;
        }
        JsonObject root = readObject(stored, MAX_STORAGE);
        if (root.size() > MAX_SAVED)
        {
            throw new IllegalArgumentException("Too many saved setups.");
        }
        for (Map.Entry<String, JsonElement> entry : root.entrySet())
        {
            String name = validName(entry.getKey());
            String json = gson.toJson(entry.getValue());
            if (!entry.getValue().isJsonObject() || json.length() > MAX_JSON)
            {
                throw new IllegalArgumentException("Invalid saved setup: " + name);
            }
            result.put(name, json);
        }
        return result;
    }

    void saveCurrent(String name, boolean replace, Session session)
    {
        saveCurrent(name, replace, session, false);
    }

    void saveCurrent(String name, boolean replace, Session session, boolean themes)
    {
        name = validName(name);
        Map<String, String> saved = saved(session, themes);
        if (saved.containsKey(name) && !replace)
        {
            throw new IllegalArgumentException("That name exists. Select Replace saved to overwrite it.");
        }
        if (!saved.containsKey(name) && saved.size() >= MAX_SAVED)
        {
            throw new IllegalArgumentException("You can save up to " + MAX_SAVED + (themes ? " themes per profile." : " setups per profile."));
        }
        saved.put(name, exportCurrent(name, session, themes));
        writeSaved(saved, session, themes);
    }

    void delete(String name, Session session)
    {
        delete(name, session, false);
    }

    void delete(String name, Session session, boolean themes)
    {
        Map<String, String> saved = saved(session, themes);
        saved.remove(name);
        writeSaved(saved, session, themes);
    }

    private void writeSaved(Map<String, String> saved, Session session, boolean themes)
    {
        JsonObject root = new JsonObject();
        saved.forEach((name, json) -> root.add(name, readObject(json, MAX_JSON)));
        String value = gson.toJson(root);
        if (value.length() > MAX_STORAGE)
        {
            throw new IllegalArgumentException("Saved setups exceed the storage limit.");
        }
        check(session);
        manager.setConfiguration(OverheadsConfig.GROUP, themes ? THEMES_KEY : STORAGE_KEY, value);
    }

    private static String validName(String name)
    {
        if (name == null || name.trim().isEmpty() || name.trim().length() > 64
            || name.chars().anyMatch(Character::isISOControl))
        {
            throw new IllegalArgumentException("Enter a setup name between 1 and 64 characters.");
        }
        return name.trim();
    }

    static JsonObject readObject(String json, int maximum)
    {
        if (json == null || json.isEmpty() || json.length() > maximum)
        {
            throw new IllegalArgumentException("JSON is empty or exceeds " + maximum + " characters.");
        }
        try (JsonReader reader = new JsonReader(new StringReader(json)))
        {
            reader.setLenient(false);
            JsonElement value = readValue(reader, 0, new int[]{0});
            if (!value.isJsonObject() || reader.peek() != JsonToken.END_DOCUMENT)
            {
                throw new IllegalArgumentException("Expected one JSON object.");
            }
            return value.getAsJsonObject();
        }
        catch (IOException | NumberFormatException | IllegalStateException ex)
        {
            throw new IllegalArgumentException("Invalid setup JSON.", ex);
        }
    }

    private static JsonElement readValue(JsonReader reader, int depth, int[] count) throws IOException
    {
        if (depth > 12 || ++count[0] > 20000)
        {
            throw new IllegalArgumentException("Setup JSON is too complex.");
        }
        switch (reader.peek())
        {
            case BEGIN_OBJECT:
                JsonObject object = new JsonObject();
                reader.beginObject();
                while (reader.hasNext())
                {
                    String key = reader.nextName();
                    if (key.length() > 128 || object.has(key) || object.size() >= 2048)
                    {
                        throw new IllegalArgumentException("Invalid or duplicate setup key.");
                    }
                    object.add(key, readValue(reader, depth + 1, count));
                }
                reader.endObject();
                return object;
            case BEGIN_ARRAY:
                JsonArray array = new JsonArray();
                reader.beginArray();
                while (reader.hasNext())
                {
                    if (array.size() >= 512)
                    {
                        throw new IllegalArgumentException("Setup array is too large.");
                    }
                    array.add(readValue(reader, depth + 1, count));
                }
                reader.endArray();
                return array;
            case STRING:
                String text = reader.nextString();
                if (text.length() > 8192)
                {
                    throw new IllegalArgumentException("Setup text is too long.");
                }
                return new JsonPrimitive(text);
            case NUMBER:
                String number = reader.nextString();
                if (number.length() > 64)
                {
                    throw new IllegalArgumentException("Setup number is too long.");
                }
                return new JsonPrimitive(new BigDecimal(number));
            case BOOLEAN:
                return new JsonPrimitive(reader.nextBoolean());
            case NULL:
                reader.nextNull();
                return JsonNull.INSTANCE;
            default:
                throw new IllegalArgumentException("Invalid setup JSON.");
        }
    }

    static String display(JsonElement value)
    {
        if (value.isJsonObject() && value.getAsJsonObject().has("family"))
        {
            JsonObject font = value.getAsJsonObject();
            return font.get("family").getAsString() + " " + font.get("size").getAsInt()
                + (font.get("bold").getAsBoolean() ? " bold" : "") + (font.get("italic").getAsBoolean() ? " italic" : "");
        }
        String text = value.isJsonPrimitive() ? value.getAsString() : value.toString();
        return text.length() <= 160 ? text : text.substring(0, 157) + "...";
    }
}
