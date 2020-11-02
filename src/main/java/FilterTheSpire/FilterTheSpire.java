package FilterTheSpire;

import FilterTheSpire.utils.ExtraColors;
import FilterTheSpire.utils.ExtraFonts;
import FilterTheSpire.utils.SeedTesting;
import basemod.BaseMod;
import basemod.interfaces.PostDungeonInitializeSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import basemod.interfaces.RenderSubscriber;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BooleanSupplier;

@SpireInitializer
public class FilterTheSpire implements PostInitializeSubscriber, PostDungeonInitializeSubscriber, RenderSubscriber {
    public static void initialize() { new FilterTheSpire(); }

    private int timesStartedOver = 0;
    private final int MAX_START_OVER = 200;

    //private boolean isResetting = false;
    public static boolean SEARCHING_FOR_SEEDS;

    // TODO: localization
    private static final String info = "Searching for a suitable seed";
    private static final String extra_info = "Seeds searched: ";

    //private static Texture BG = new Texture("FilterTheSpire/images/fts_background.png");
    private static Texture BG;

    private ArrayList<BooleanSupplier> validators = new ArrayList<>();

    public FilterTheSpire() {
        BaseMod.subscribe(this);

        // Example usage - adding filters is as easy as passing a function that returns a bool
        //   all predicates in the validators list are logical AND together at the end
        //   logical OR will be more complicated to make it generic, but the idea is the same
        //   (pass a function that returns the result of the OR condition)
        validators.add(() -> bossSwapIs("Pandora's Box"));
        //validators.add(() -> bossSwapIs("Snecko Eye"));
    }

    // DEBUG
    private void printRelicPool() {
        if (CardCrawlGame.isInARun() && CardCrawlGame.chosenCharacter != null) {
            ArrayList<String> bossRelics = new ArrayList(AbstractDungeon.bossRelicPool);
            bossRelics.sort(String::compareTo);

            System.out.println("\n---------------------------------------");
            System.out.println("\nBoss relics (" + CardCrawlGame.chosenCharacter.name() + "):");
            bossRelics.forEach(System.out::println);
            System.out.println("---------------------------------------\n");
        }
    }

    // TODO: other filters
//    private boolean bossSwapIs(String targetRelic) {
//        if (CardCrawlGame.isInARun()) {
//            ArrayList<String> bossRelics = AbstractDungeon.bossRelicPool;
//
//            // DEBUG
//            System.out.println("Checking seed to see if boss swap is " + targetRelic);
//            System.out.println("testing my own stuff now");
//            SeedTesting.testing();
//
//            // TODO: remove / debug
////            printRelicPool();
//
//            if (!bossRelics.isEmpty()) {
//                String relic = bossRelics.get(0);
//                return targetRelic == relic;
//            }
//        }
//
//        return false;
//    }

    private boolean validateSeed() {
        return validators.stream().allMatch(BooleanSupplier::getAsBoolean);
    }

    private boolean bossSwapIs(String targetRelic) {
        Random relicRng = new Random(Settings.seed);

        // Skip past all these
        relicRng.randomLong(); // common
        relicRng.randomLong(); // uncommon
        relicRng.randomLong(); // rare
        relicRng.randomLong(); // shop
        //relicRng.randomLong(); // boss <- this is the one (we perform it below)

        ArrayList<String> bossRelicPool = new ArrayList<>();
        RelicLibrary.populateRelicPool(bossRelicPool, AbstractRelic.RelicTier.BOSS, AbstractDungeon.player.chosenClass);
        Collections.shuffle(bossRelicPool, new java.util.Random(relicRng.randomLong()));

        return !bossRelicPool.isEmpty() && bossRelicPool.get(0) == targetRelic;
    }


    private void playNeowSound() {
        int roll = MathUtils.random(3);
        if (roll == 0) {
            CardCrawlGame.sound.play("VO_NEOW_1A");
        } else if (roll == 1) {
            CardCrawlGame.sound.play("VO_NEOW_1B");
        } else if (roll == 2) {
            CardCrawlGame.sound.play("VO_NEOW_2A");
        } else {
            CardCrawlGame.sound.play("VO_NEOW_2B");
        }
    }

//    @Override
//    public void receivePostDungeonInitialize() {
//        // Mute when first starting the search
//        if (timesStartedOver == 0) {
//            SEARCHING_FOR_SEEDS = true;
//        }
//
//        if (validateSeed()) {
//            timesStartedOver = 0;
//            isResetting = false;
//
//            SEARCHING_FOR_SEEDS = false;
//            if (AbstractDungeon.scene != null) {
//                // Play the Neow sound we originally patched out
//                playNeowSound();
//            }
//        }
//        else {
//            // Haven't reached the reset limit yet, so can reset and try again
//            if (timesStartedOver < MAX_START_OVER) {
//                isResetting = true;
//                RestartHelper.restartRun();
//                timesStartedOver++;
//            }
//            else {
//                isResetting = false;
//                System.out.println("ERROR: ran out of resets"); // TODO: show a warning message on neow
//            }
//        }
//
//    }

    @Override
    public void receivePostDungeonInitialize() {
        while (!validateSeed()) {
            //isResetting = true;
            SEARCHING_FOR_SEEDS = true;
            RestartHelper.randomSeed();
            timesStartedOver++;
        }

        if (timesStartedOver > 0)
            RestartHelper.makeReal();

        System.out.println("Found a valid start in " + timesStartedOver + " attempts.");

        // Reset
        timesStartedOver = 0;
        //isResetting = false;
        SEARCHING_FOR_SEEDS = false;

//        // Mute when first starting the search
//        if (timesStartedOver == 0) {
//            SEARCHING_FOR_SEEDS = true;
//        }
//
//        if (validateSeed()) {
//            if (timesStartedOver != 0)
//                RestartHelper.makeReal();
//
//            timesStartedOver = 0;
//            isResetting = false;
//
//            SEARCHING_FOR_SEEDS = false;
//            if (AbstractDungeon.scene != null) {
//                // Play the Neow sound we originally patched out
//                playNeowSound();
//            }
//        }
//        else {
//            // Haven't reached the reset limit yet, so can reset and try again
//            if (timesStartedOver < MAX_START_OVER) {
//                isResetting = true;
//                //RestartHelper.restartRun();
//                RestartHelper.randomSeed();
//                timesStartedOver++;
//            }
//            else {
//                isResetting = false;
//                System.out.println("ERROR: ran out of resets"); // TODO: show a warning message on neow
//            }
//        }

    }


    @Override
    public void receiveRender(SpriteBatch sb) {
        //if (true) {
        //if (isResetting) {
        if (SEARCHING_FOR_SEEDS) {
//            sb.setColor(Color.BLACK);
//            sb.draw(ImageMaster.WHITE_SQUARE_IMG,
//                    0,
//                    0,
//                    Settings.WIDTH,
//                    Settings.HEIGHT);

            if (BG != null) {
                sb.setColor(Color.WHITE);
                sb.draw(BG, 0, 0, Settings.WIDTH, Settings.HEIGHT);
            }
            else {
                System.out.println("OJB WARNING: BG texture not initialized properly");
            }

            FontHelper.renderFontCentered(sb,
                    FontHelper.menuBannerFont,
                    "Searching for the perfect seed...",
                    (Settings.WIDTH * 0.5f),
                    (Settings.HEIGHT * 0.5f) + (224.0f * Settings.scale),
                    Settings.CREAM_COLOR
                    );

            FontHelper.renderFontCentered(sb,
                    //FontHelper.tipBodyFont,
                    ExtraFonts.largeNumberFont(),
                    "" + timesStartedOver,
                    (Settings.WIDTH * 0.5f),
                    (Settings.HEIGHT * 0.5f),
                    ExtraColors.PINK_COLOR
            );

            FontHelper.renderFontCentered(sb,
                    FontHelper.menuBannerFont,
                    "Seeds Explored",
                    (Settings.WIDTH * 0.5f),
                    321 * Settings.scale,
                    Color.GRAY
            );

            FontHelper.renderFontRightTopAligned(sb,
                    FontHelper.menuBannerFont,
                    "Filter the Spire",
                    Settings.WIDTH - (85.0f * Settings.scale),
                    945 * Settings.scale,
                    Color.GRAY
            );

            FontHelper.renderFontRightTopAligned(sb,
                    FontHelper.menuBannerFont,
                    "v0.1.0",
                    Settings.WIDTH - (85.0f * Settings.scale),
                    890 * Settings.scale,
                    Color.GRAY
            );

            // Render the loading text
//            FontHelper.renderFontCentered(sb,
//                    FontHelper.dungeonTitleFont,
//                    info,
//                    Settings.WIDTH / 2.0f,
//                    Settings.HEIGHT / 2.0f);
//
//            FontHelper.renderFontLeftDownAligned(sb,
//                    FontHelper.eventBodyText,
//                    extra_info + timesStartedOver,
//                    100 * Settings.scale,
//                    100 * Settings.scale,
//                    Settings.CREAM_COLOR);
        }
    }

    @Override
    public void receivePostInitialize() {
        BG = new Texture("FilterTheSpire/images/fts_background.png");
    }
}
