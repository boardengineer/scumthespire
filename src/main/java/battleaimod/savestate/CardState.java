package battleaimod.savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.CardLibrary;

public class CardState {
    private final String cardId;
    private final boolean upgraded;
//
//    private final float current_x;
//    private final float current_y;
//    private final float target_x;
//    private final float target_y;
//
//    private final float drawScale;
//    private final float targetDrawScale;
//
//    private final HitboxState hb;

    public CardState(AbstractCard card) {
        this.cardId = card.cardID;
        this.upgraded = card.upgraded;
    }

    public CardState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.cardId = parsed.get("card_id").getAsString();
        this.upgraded = parsed.get("upgraded").getAsBoolean();
    }

    public AbstractCard loadCard() {
        AbstractCard result = CardLibrary.getCard(cardId).makeCopy();

        if (upgraded) {
            result.upgrade();
        }

        return result;
    }

    public String getName() {
        return cardId;
    }

    public String encode() {
        JsonObject cardStateJson = new JsonObject();


        cardStateJson.addProperty("card_id", cardId);
        cardStateJson.addProperty("upgraded", upgraded);

        return cardStateJson.toString();
    }
}
