package FilterTheSpire.ui.screens;

import FilterTheSpire.FilterTheSpire;
import FilterTheSpire.utils.ExtraColors;
import FilterTheSpire.utils.ExtraFonts;
import basemod.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;
import com.megacrit.cardcrawl.ui.DialogWord;
import com.megacrit.cardcrawl.vfx.SpeechTextEffect;

import java.util.ArrayList;

public class AlternateConfigMenu extends ModPanel {
    private static Texture TEX_BG = new Texture("FilterTheSpire/images/config_screen_bg.png");
    private ModLabeledToggleButton neowBonusToggle;

    private ArrayList<ModLabeledButton> filterButtons = new ArrayList<>();
    private ArrayList<FilterScreen> filterScreens = new ArrayList<>();
    private ModLabeledButton clearButton;
    SpeechTextEffect clearMessage;

    private boolean visible = false;

    public AlternateConfigMenu() {
        super();

        final float xPosition = 400.0F;
        float yPosition = FilterScreen.INFO_TOP_MAIN;

        BossSwapFilterScreen bossRelicScreen = new BossSwapFilterScreen();
        filterButtons.add(createFilterScreenButton("Choose Boss Relics", xPosition, yPosition, bossRelicScreen));

        yPosition -= 90.0F;
        ShopRelicFilterScreen shopRelicScreen = new ShopRelicFilterScreen();
        filterButtons.add(createFilterScreenButton("Choose Shop Relics", xPosition, yPosition, shopRelicScreen));

        yPosition -= 90.0F;
        NeowBonusFilterScreen neowBonusScreen = new NeowBonusFilterScreen();
        filterButtons.add(createFilterScreenButton("Choose Neow Bonuses", xPosition, yPosition, neowBonusScreen));

        yPosition -= 90.0F;
        NthRelicFilterScreen nthRelicFilterScreen = new NthRelicFilterScreen();
        filterButtons.add(createFilterScreenButton("Choose Relic Filter", xPosition, yPosition, nthRelicFilterScreen));

        filterScreens.add(bossRelicScreen);
        filterScreens.add(shopRelicScreen);
        filterScreens.add(neowBonusScreen);
        filterScreens.add(nthRelicFilterScreen);
        clearButton = new ModLabeledButton("Clear All Filters", FilterScreen.INFO_LEFT + 50.0F, 805.0f,
                Settings.CREAM_COLOR, Color.RED, FontHelper.tipHeaderFont, this,
                (self) -> {
                    FilterTheSpire.config.clearFilters();
                    for (FilterScreen screen : filterScreens) {
                        screen.clearFilter();
                    }
                    clearMessage = new SpeechTextEffect((clearButton.getX() - 125.0F) * Settings.xScale, 850.0f * Settings.yScale, 2.0F, "Filters Cleared", DialogWord.AppearEffect.BUMP_IN);
                });

        neowBonusToggle = new ModLabeledToggleButton("Enable all Neow Bonuses",
                FilterScreen.INFO_LEFT,         // NOTE: no scaling! (ModLabeledToggleButton scales later)
                FilterScreen.INFO_BOTTOM_CHECK, // same as above
                Settings.CREAM_COLOR,
                FontHelper.charDescFont,
                FilterTheSpire.config.getBooleanKeyOrSetDefault("allNeowBonuses", true),
                null,
                (modLabel) -> {
                },
                (button) -> {
                    FilterTheSpire.config.setBooleanKey("allNeowBonuses", button.enabled);
                }) {
            // Override the update of the toggle button to add an informational tool tip when hovered
            @Override
            public void update() {
                super.update();

                // Unfortunately, the hb is private so we have to use reflection here
                Hitbox hb = ReflectionHacks.getPrivate(toggle, ModToggleButton.class, "hb");

                if (hb != null && hb.hovered) {
                    TipHelper.renderGenericTip(
                            FilterScreen.INFO_LEFT * Settings.xScale,
                            (FilterScreen.INFO_BOTTOM_CHECK - 40.0f) * Settings.yScale,
                            "Info",
                            "If checked, you will be guaranteed to see all four Neow options regardless of " +
                                    "whether or not the previous run made it to the act one boss. NL NL Disabling this " +
                                    "patch makes the experience more like the base game, but you may not have access " +
                                    "to the boss swap option.");
                }
            }
        };
    }

    @Override
    public void renderBg(SpriteBatch sb) {
        // Dim to diminish the rest of the config menu
        sb.setColor(ExtraColors.SCREEN_DIM);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0, 0, Settings.WIDTH, Settings.HEIGHT);

        // Draw our screen texture in the center
        sb.setColor(Color.WHITE);
        sb.draw(TEX_BG,
                Math.round((Settings.WIDTH - (TEX_BG.getWidth() * Settings.xScale)) * 0.5f),
                Math.round((Settings.HEIGHT - (TEX_BG.getHeight() * Settings.yScale)) * 0.5f),
                Math.round(TEX_BG.getWidth() * Settings.xScale),
                Math.round(TEX_BG.getHeight() * Settings.yScale)
        );

        float titleLeft = 386.0f;
        float titleBottom = 819.0f;
        FontHelper.renderFontLeftDownAligned(sb, ExtraFonts.configTitleFont(), "Filter Menu", titleLeft * Settings.xScale, titleBottom * Settings.yScale, Settings.GOLD_COLOR);

        FontHelper.renderSmartText(sb,
                FontHelper.tipBodyFont,
                "This mod assumes that all cards and relics have been unlocked by leveling each character. If you have not unlocked everything, the filters will not work consistently.",
                FilterScreen.INFO_LEFT * Settings.xScale,
                FilterScreen.INFO_TOP_MAIN * Settings.yScale,
                FilterScreen.INFO_WIDTH * Settings.xScale,
                30.0f * Settings.yScale,
                Settings.CREAM_COLOR);
    }

    @Override
    public void render(SpriteBatch sb) {
        renderBg(sb);
        for (ModLabeledButton button : filterButtons) {
            button.render(sb);
        }

        boolean isShowingFilterScreen = false;
        for (FilterScreen screen : filterScreens) {
            if (screen.isShowing) {
                screen.render(sb);
                isShowingFilterScreen = true;
                break;
            }
        }
        if (!isShowingFilterScreen) {
            neowBonusToggle.render(sb);
            clearButton.render(sb);
            if (clearMessage != null && !clearMessage.isDone) {
                clearMessage.render(sb);
            }
        }
    }

    @Override
    public void update() {
        boolean isShowingFilterScreen = false;
        for (FilterScreen screen : filterScreens) {
            if (screen.isShowing) {
                screen.update();
                isShowingFilterScreen = true;
                break;
            }
        }
        if (!isShowingFilterScreen) {
            neowBonusToggle.update();
            clearButton.update();
            for (ModLabeledButton button : filterButtons) {
                button.update();
            }
            if (clearMessage != null && !clearMessage.isDone) {
                clearMessage.update();
            }
        }

        if (InputHelper.pressedEscape) {
            BaseMod.modSettingsUp = false;
            InputHelper.pressedEscape = false;
        }

        if (!BaseMod.modSettingsUp) {
            this.waitingOnEvent = false;
            Gdx.input.setInputProcessor(this.oldInputProcessor);
            CardCrawlGame.mainMenuScreen.lighten();
            CardCrawlGame.mainMenuScreen.screen = MainMenuScreen.CurScreen.MAIN_MENU;
            CardCrawlGame.cancelButton.hideInstantly();
            this.isUp = false;
        }

        // Enable and disable hitboxes
        if (isUp && !visible) {
            visible = true;
        } else if (!isUp && visible) {
            visible = false;
        }
    }

    private ModLabeledButton createFilterScreenButton(String buttonText, float xPosition, float yPosition, FilterScreen screen) {
        return new ModLabeledButton(buttonText, xPosition, yPosition,
                Settings.CREAM_COLOR, Color.GOLD, FontHelper.tipHeaderFont, this,
                (self) -> {
                    screen.isShowing = true;
                });
    }
}
