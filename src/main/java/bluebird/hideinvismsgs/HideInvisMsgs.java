package bluebird.hideinvismsgs;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HideInvisMsgs implements ModInitializer {

    public static Logger LOGGER = LoggerFactory.getLogger("hideinvismsgs");

    private static final Identifier GAMERULE_IDENTIFIER = Identifier.fromNamespaceAndPath("hideinvismsgs","obfuscate_invis_deaths");
    private static final Identifier GAMERULE_IDENTIFIER_2 = Identifier.fromNamespaceAndPath("hideinvismsgs","obfuscate_invis_kills");
    public static final GameRule<Boolean> OBFUSCATED_INVIS_DEATHS = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(GAMERULE_IDENTIFIER);
    public static final GameRule<Boolean> OBFUSCATED_INVIS_KILLS = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(GAMERULE_IDENTIFIER_2);

    public void onInitialize() {
        LOGGER.info("HideInvisDeaths initialed");
    }
}