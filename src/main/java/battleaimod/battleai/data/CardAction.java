package battleaimod.battleai.data;

import battleaimod.battleai.data.dummycommands.DummyCardCommand;
import battleaimod.battleai.data.dummycommands.DummyCommand;
import battleaimod.utils.FileLogger;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CardAction {
    /*
    A Single CardAction class can represent multiple commands
    E.g. play card into discard = 2 commands
    But since these 2 commands must be played consecutively, they must be evolved together as a single unit
     */

    private final AbstractCard mainCard;

    public int getEnemyIndex() {
        return enemyIndex;
    }

    public void setEnemyIndex(int enemyIndex) {
        this.enemyIndex = enemyIndex;
    }

    private int enemyIndex;


    public CardAction(AbstractCard mainCard, int enemyIndex) {
        this.mainCard = mainCard;
        this.enemyIndex = enemyIndex;
    }

    public CardAction(AbstractCard mainCard) {
        this.mainCard = mainCard;
        this.enemyIndex = -1;
    }

    public AbstractCard getMainCard() {
        return mainCard;
    }


    public List<DummyCommand> getDummyCommands(){
        List<DummyCommand> cmds = new ArrayList<>();

        cmds.add(new DummyCardCommand(mainCard, enemyIndex));
        //TODO: Add HandSelectCommand + GridSelectCommand if needed

        return cmds;
    }

    public static CardAction createCardAction(AbstractCard card) {

        if (card == null || AbstractDungeon.player == null) {
            return null;
        }

        if(card.costForTurn == -2 ||
                (AbstractDungeon.player.hasPower("Entangled") && card.type == AbstractCard.CardType.ATTACK)){
            return null;
        }

        // --- Cards that require enemy target ---
        if (card.target == AbstractCard.CardTarget.ENEMY ||
                card.target == AbstractCard.CardTarget.SELF_AND_ENEMY) {

            List<AbstractMonster> validTargets  = getValidTargets(card);

            if (validTargets.isEmpty()) {
                return null;
            }

            // Pick random valid monster
            int randIndex = AbstractDungeon.cardRandomRng.random(validTargets.size() - 1);
            AbstractMonster target = validTargets.get(randIndex);

            int enemyIndex = AbstractDungeon.getMonsters().monsters.indexOf(target);

            return new CardAction(card, enemyIndex);
        }

        // --- Non-targeted cards ---
        if (card.target == AbstractCard.CardTarget.ALL_ENEMY ||
                card.target == AbstractCard.CardTarget.ALL ||
                card.target == AbstractCard.CardTarget.SELF ||
                card.target == AbstractCard.CardTarget.NONE) {

            if (card.canUse(AbstractDungeon.player, null)) {
                return new CardAction(card);
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return mainCard.toString() + " enemy: " + enemyIndex;
    }

    public static List<AbstractMonster> getValidTargets(AbstractCard card){
        List<AbstractMonster> validTargets = new ArrayList<>();

        if (AbstractDungeon.getMonsters() != null) {
            for (int i = 0; i < AbstractDungeon.getMonsters().monsters.size(); i++) {
                AbstractMonster m = AbstractDungeon.getMonsters().monsters.get(i);

                if (m != null &&
                        !m.isDeadOrEscaped() &&
                        card.canUse(AbstractDungeon.player, m)) {

                    validTargets.add(m);
                }
            }
        }

        return validTargets;
    }
}
