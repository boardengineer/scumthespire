package battleaimod.savestate.monsters.exordium;

import basemod.ReflectionHacks;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.actions.unique.BurnIncreaseAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.status.Burn;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.Hexaghost;
import com.megacrit.cardcrawl.monsters.exordium.HexaghostBody;
import com.megacrit.cardcrawl.monsters.exordium.HexaghostOrb;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToDiscardEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class HexaghostState extends MonsterState {
    private final boolean activated;
    private final boolean burnUpgraded;
    private final int orbActiveCount;
    private final List<Boolean> activeOrbs;

    public HexaghostState(AbstractMonster monster) {
        super(monster);

        activated = ReflectionHacks
                .getPrivate(monster, Hexaghost.class, "activated");
        burnUpgraded = ReflectionHacks
                .getPrivate(monster, Hexaghost.class, "burnUpgraded");
        orbActiveCount = ReflectionHacks
                .getPrivate(monster, Hexaghost.class, "orbActiveCount");
        ArrayList<HexaghostOrb> orbs = ReflectionHacks
                .getPrivate(monster, Hexaghost.class, "orbs");
        activeOrbs = orbs.stream().map(orb -> orb.activated)
                         .collect(Collectors.toList());

        monsterTypeNumber = Monster.HEXAGHOST.ordinal();
    }

    public HexaghostState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.activated = parsed.get("activated").getAsBoolean();
        this.burnUpgraded = parsed.get("burn_upgraded").getAsBoolean();
        this.orbActiveCount = parsed.get("orb_active_count").getAsInt();
        ArrayList<Boolean> orbs = new ArrayList<>();
        parsed.get("active_orbs").getAsJsonArray()
              .forEach(active -> orbs.add(active.getAsBoolean()));
        this.activeOrbs = orbs;

        monsterTypeNumber = Monster.HEXAGHOST.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Hexaghost monster = new Hexaghost();
        populateSharedFields(monster);

        ReflectionHacks
                .setPrivate(monster, Hexaghost.class, "activated", activated);
        ReflectionHacks
                .setPrivate(monster, Hexaghost.class, "burnUpgraded", burnUpgraded);
        ReflectionHacks
                .setPrivate(monster, Hexaghost.class, "orbActiveCount", orbActiveCount);
        ArrayList<HexaghostOrb> orbs = ReflectionHacks
                .getPrivate(monster, Hexaghost.class, "orbs");
        for (int i = 0; i < activeOrbs.size(); i++) {
            if (activeOrbs.get(i)) {
                orbs.get(i).activate(0, 0);
            }
        }

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("activated", activated);
        monsterStateJson.addProperty("burn_upgraded", burnUpgraded);
        monsterStateJson.addProperty("orb_active_count", orbActiveCount);
        JsonArray orbArray = new JsonArray();
        activeOrbs.forEach(isActive -> orbArray.add(isActive));
        monsterStateJson.add("active_orbs", orbArray);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = HexaghostBody.class,
            paramtypez = {AbstractMonster.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoBodyCreationAnimationsPatch {
        public static SpireReturn Prefix(HexaghostBody _instance, AbstractMonster monster) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = ImageMaster.class,
            paramtypez = {String.class},
            method = "loadImage"
    )
    public static class NoHexaghostImagesPatch {
        public static SpireReturn Prefix(String imgUrl) {
            if (shouldGoFast()) {
                if (imgUrl.equals(Hexaghost.IMAGE)) {
                    return SpireReturn.Return(null);
                }
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = Hexaghost.class,
            paramtypez = {},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class NoDisposeBodyCreationAnimationsPatch {
        public static void Postfix(Hexaghost _instance) {
            if (shouldGoFast()) {
                Texture img = ReflectionHacks.getPrivate(_instance, AbstractMonster.class, "img");
                ReflectionHacks.setPrivate(_instance, AbstractMonster.class, "img", null);
                if (img != null) {
                    img.dispose();
                }
                List<Disposable> disposables = ReflectionHacks
                        .getPrivate(_instance, AbstractMonster.class, "disposables");
                disposables.clear();
            }
        }
    }

    @SpirePatch(
            clz = HexaghostBody.class,
            paramtypez = {SpriteBatch.class},
            method = "render"
    )
    public static class NoRenderBodyPatch {
        public static SpireReturn Prefix(HexaghostBody _instance, SpriteBatch sb) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = HexaghostBody.class,
            paramtypez = {},
            method = "update"
    )
    public static class NoUpdateBodyPatch {
        public static SpireReturn Prefix(HexaghostBody _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = BurnIncreaseAction.class,
            paramtypez = {},
            method = "update"
    )
    public static class BurnIncreasePatch {
        public static SpireReturn Prefix(BurnIncreaseAction _instance) {
            if (shouldGoFast()) {
                for(AbstractCard card: AbstractDungeon.player.discardPile.group) {
                    if(card instanceof Burn) {
                        card.upgrade();
                    }
                }

                for(AbstractCard card: AbstractDungeon.player.drawPile.group) {
                    if(card instanceof Burn) {
                        card.upgrade();
                    }
                }

                Burn b = new Burn();
                b.upgrade();
                AbstractDungeon.effectList.add(new ShowCardAndAddToDiscardEffect(b));
                Burn c = new Burn();
                c.upgrade();
                AbstractDungeon.effectList.add(new ShowCardAndAddToDiscardEffect(c));
                Burn d = new Burn();
                d.upgrade();
                AbstractDungeon.effectList.add(new ShowCardAndAddToDiscardEffect(d));

                _instance.isDone = true;
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = HexaghostBody.class,
            paramtypez = {},
            method = "dispose"
    )
    public static class NoDisposeBodyPatch {
        public static SpireReturn Prefix(HexaghostBody _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = Hexaghost.class,
            paramtypez = {},
            method = "createOrbs"
    )
    public static class NoOrbPatch {
        public static SpireReturn Prefix(Hexaghost _instance) {
            if (shouldGoFast()) {
                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
