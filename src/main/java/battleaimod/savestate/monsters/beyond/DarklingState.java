package battleaimod.savestate.monsters.beyond;

import basemod.ReflectionHacks;
import battleaimod.savestate.monsters.Monster;
import battleaimod.savestate.monsters.MonsterState;
import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.actions.common.SetMoveAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.beyond.Darkling;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import static battleaimod.patches.MonsterPatch.shouldGoFast;

public class DarklingState extends MonsterState {
    private final int chompDmg;
    private final int nipDmg;

    private final boolean firstMove;

    public DarklingState(AbstractMonster monster) {
        super(monster);

        this.chompDmg = ReflectionHacks.getPrivate(monster, Darkling.class, "chompDmg");
        this.nipDmg = ReflectionHacks.getPrivate(monster, Darkling.class, "nipDmg");

        this.firstMove = ReflectionHacks.getPrivate(monster, Darkling.class, "firstMove");

        monsterTypeNumber = Monster.DARKLING.ordinal();
    }

    public DarklingState(String jsonString) {
        super(jsonString);

        // TODO don't parse twice
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.chompDmg = parsed.get("chomp_dmg").getAsInt();
        this.nipDmg = parsed.get("nip_dmg").getAsInt();
        this.firstMove = parsed.get("fist_move").getAsBoolean();

        monsterTypeNumber = Monster.DARKLING.ordinal();
    }

    @Override
    public AbstractMonster loadMonster() {
        Darkling monster = new Darkling(offsetX, offsetY);

        populateSharedFields(monster);

        ReflectionHacks.setPrivate(monster, Darkling.class, "chompDmg", chompDmg);
        ReflectionHacks.setPrivate(monster, Darkling.class, "nipDmg", nipDmg);
        ReflectionHacks.setPrivate(monster, Darkling.class, "firstMove", firstMove);

        return monster;
    }

    @Override
    public String encode() {
        JsonObject monsterStateJson = new JsonParser().parse(super.encode()).getAsJsonObject();

        monsterStateJson.addProperty("chomp_dmg", chompDmg);
        monsterStateJson.addProperty("nip_dmg", nipDmg);
        monsterStateJson.addProperty("fist_move", firstMove);

        return monsterStateJson.toString();
    }

    @SpirePatch(
            clz = Darkling.class,
            paramtypez = {float.class, float.class},
            method = SpirePatch.CONSTRUCTOR
    )
    public static class YetNoAnimationsPatch {
        @SpireInsertPatch(loc = 53)
        public static SpireReturn Insert(Darkling _instance, float x, float y) {
            if (shouldGoFast()) {
                int chompDmg;
                int nipDmg;

                if (AbstractDungeon.ascensionLevel >= 7) {
                    MonsterState.setHp(_instance, 50, 59);
                } else {
                    MonsterState.setHp(_instance, 48, 56);
                }
                if (AbstractDungeon.ascensionLevel >= 2) {
                    chompDmg = 9;
                    nipDmg = AbstractDungeon.monsterHpRng.random(9, 13);
                } else {
                    chompDmg = 8;
                    nipDmg = AbstractDungeon.monsterHpRng.random(7, 11);
                }

                _instance.damage.add(new DamageInfo(_instance, chompDmg));
                _instance.damage.add(new DamageInfo(_instance, nipDmg));

                ReflectionHacks.setPrivate(_instance, Darkling.class, "chompDmg", chompDmg);
                ReflectionHacks.setPrivate(_instance, Darkling.class, "nipDmg", nipDmg);

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }

    @SpirePatch(
            clz = Darkling.class,
            paramtypez = {DamageInfo.class},
            method = "damage"
    )
    public static class SilentDamagePatch {
        @SpireInsertPatch(loc = 199)
        public static SpireReturn Insert(Darkling _instance, DamageInfo info) {
            if (shouldGoFast()) {

                if (_instance.currentHealth <= 0 && !_instance.halfDead) {
                    _instance.halfDead = true;
                    for (AbstractPower p : _instance.powers) {
                        p.onDeath();
                    }
                    for (AbstractRelic r : AbstractDungeon.player.relics) {
                        r.onMonsterDeath(_instance);
                    }
                    _instance.powers.clear();
                    boolean allDead = true;
                    for (AbstractMonster m : (AbstractDungeon.getMonsters()).monsters) {
                        if (m.id.equals("Darkling") && !m.halfDead) {
                            allDead = false;
                        }
                    }
                    if (!allDead) {
                        if (_instance.nextMove != 4) {
                            _instance.setMove((byte) 4, AbstractMonster.Intent.UNKNOWN);
                            _instance.createIntent();
                            AbstractDungeon.actionManager
                                    .addToBottom(new SetMoveAction(_instance, (byte) 4, AbstractMonster.Intent.UNKNOWN));
                        }
                    } else {
                        (AbstractDungeon.getCurrRoom()).cannotLose = false;
                        _instance.halfDead = false;
                        for (AbstractMonster m : (AbstractDungeon.getMonsters()).monsters) {
                            m.die();
                        }
                    }
                }

                return SpireReturn.Return(null);
            }
            return SpireReturn.Continue();
        }
    }
}
