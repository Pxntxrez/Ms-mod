package eu.keray.swarm;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class Config {
    public static SwarmConfigEnum SWARM_OVERWORLD = SwarmConfigEnum.FULLMOON;
    public static final boolean ENABLE_SOUNDS = true;
    public static SwarmConfigEnum SWARM_UNDERGROUND = SwarmConfigEnum.FULLMOON;
    public static boolean SWARM_NETHER = true;
    public static boolean SWARM_DIMENSIONS = true;
    public static boolean KILL_MOBS_DAYTIME = true;
    public static boolean ATTACK_ANIMALS = true;
    public static boolean MODIFY_RESISTANCE = true;
    public static int AGGRO_RANGE = 120;
    public static int MAX_RESISTANCE = 90;
    public static float MAX_RES = (MAX_RESISTANCE / 5);
    public static float RESISTANCE_MULTIPLIER = 0.8F;

    static Configuration config;
    public static boolean DEBUG = false;

    // Mob mod ids that Monster Swarm must completely ignore (no swarming, no digging,
    // no target hijacking). Lets overhaul mods like Scape and Run: Parasites keep their own AI.
    public static final Set<String> EXCLUDED_MODS = new HashSet<>();

    public static void preInit(File conf) {
        config = new Configuration(conf);
        reload();
        config.save();
    }

    public static void reload() {
        config.load();

        config.getCategory("general").setComment("");

        Property prop = config.get("general", "Swarm Overworld Surface", "FULLMOON");
        prop.setComment("Allow mobs to swarm and destroy blocks on the surface in overworld. Valid settings: ALWAYS, NEVER, FULLMOON");
        SWARM_OVERWORLD = SwarmConfigEnum.valueOf(prop.getString());

        prop = config.get("general", "Swarm Overworld Underground", "ALWAYS");
        prop.setComment("Allow mobs to swarm and destroy blocks underground in overworld. Valid settings: ALWAYS, NEVER, FULLMOON");
        SWARM_UNDERGROUND = SwarmConfigEnum.valueOf(prop.getString());

        prop = config.get("general", "Swarm Nether", true);
        prop.setComment("If set to True, monsters will swarm in the Nether.");
        SWARM_NETHER = prop.getBoolean();

        prop = config.get("general", "Swarm Other Dimensions", true);
        prop.setComment("If set to True, monsters will swarm in other dimensions. (from mods, etc)");
        SWARM_DIMENSIONS = prop.getBoolean();

        prop = config.get("general", "All Mobs Burn In Daylight", true);
        prop.setComment("If set to True, all swarming mobs will burn in daylight. (useful to get rid of creepers after a night)");
        KILL_MOBS_DAYTIME = prop.getBoolean();

        prop = config.get("general", "Attack Animals", true);
        prop.setComment("If set to True, mobs will target animals.");
        ATTACK_ANIMALS = prop.getBoolean();

        prop = config.get("general", "Modify Block Resistance", true);
        prop.setComment("Allow modified block resistance.");
        MODIFY_RESISTANCE = prop.getBoolean();

        prop = config.get("general", "Excluded Mod IDs", new String[] { "srparasites" });
        prop.setComment("Mobs from these mod ids are completely ignored by Monster Swarm "
                + "(no swarming, no digging, no target hijacking), so the mod's own AI stays intact. "
                + "Use the mod id / namespace, e.g. srparasites");
        EXCLUDED_MODS.clear();
        for (String s : prop.getStringList()) {
            String id = s.trim().toLowerCase(Locale.ROOT);
            if (!id.isEmpty())
                EXCLUDED_MODS.add(id);
        }
    }
}
