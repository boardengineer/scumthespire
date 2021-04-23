package battleaimod.battleai;

import battleaimod.BattleAiMod;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.actions.utility.WaitAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class CardCommand implements Command {
    private final int cardIndex;
    private final int monsterIndex;
    public final String displayString;

    public CardCommand(int cardIndex, int monsterIndex, String displayString) {
        this.cardIndex = cardIndex;
        this.monsterIndex = monsterIndex;
        this.displayString = displayString;
    }

    public CardCommand(int cardIndex, String displayString) {
        this.cardIndex = cardIndex;
        this.monsterIndex = -1;
        this.displayString = displayString;
    }

    public CardCommand(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.cardIndex = parsed.get("card_index").getAsInt();
        this.monsterIndex = parsed.get("monster_index").getAsInt();
        this.displayString = parsed.get("display_string").getAsString();
    }

    @Override
    public void execute() {
//        System.err.println("Executing " + displayString);

        AbstractDungeon.player.hand.refreshHandLayout();
        AbstractCard card = AbstractDungeon.player.hand.group.get(cardIndex);
        AbstractMonster monster = null;

        if (monsterIndex != -1) {
            monster = AbstractDungeon.getMonsters().monsters.get(monsterIndex);
        }

        AbstractDungeon.actionManager.cardQueue.add(new CardQueueItem(card, monster));

//        if (!shouldGoFast()) {
            AbstractDungeon.actionManager.addToBottom(new WaitAction(.2F));
//        }

        BattleAiMod.readyForUpdate = true;
    }

    @Override
    public String toString() {
        return displayString + monsterIndex;
    }

    @Override
    public String encode() {
        JsonObject cardCommandJson = new JsonObject();

        cardCommandJson.addProperty("type", "CARD");

        cardCommandJson.addProperty("card_index", cardIndex);
        cardCommandJson.addProperty("monster_index", monsterIndex);
        cardCommandJson.addProperty("display_string", displayString);

        return cardCommandJson.toString();
    }
}
