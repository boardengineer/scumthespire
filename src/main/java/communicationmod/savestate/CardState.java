package communicationmod.savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.CardLibrary;

public class CardState {
    private final String cardId;
    private final boolean upgraded;

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
        AbstractCard result = CardLibrary.getCard(cardId);

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
