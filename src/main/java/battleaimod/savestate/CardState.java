package battleaimod.savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.CardLibrary;

import java.util.UUID;

public class CardState {
    private final String cardId;
    private final boolean upgraded;
    private final int baseDamage;
    private final int cost;
    private final int costForTurn;
    private final int magicNumber;
    private final int block;
    private final boolean freeToPlayOnce;
    private final String name;

    private final boolean inBottleTornado;
    private final boolean inBottleLightning;
    private final boolean inBottleFlame;
    private final boolean isCostModifiedForTurn;
    private final boolean isCostModified;

    // Everything works without these, there is just s wonky 'draw' animation that can be avoided
    // by setting all the physical properies right away
    private final float current_x;
    private final float current_y;
    private final float target_x;
    private final float target_y;
    private final float angle;
    private final float targetAngle;
    private final float drawScale;
    private final float targetDrawScale;


    private final UUID uuid;


    // private final HitboxState hb;
    public CardState(AbstractCard card) {
        this.cardId = card.cardID;
        this.block = card.block;
        this.upgraded = card.upgraded;
        this.baseDamage = card.baseDamage;
        this.cost = card.cost;
        this.costForTurn = card.costForTurn;

        this.inBottleFlame = card.inBottleFlame;
        this.inBottleTornado = card.inBottleTornado;
        this.inBottleLightning = card.inBottleLightning;
        this.freeToPlayOnce = card.freeToPlayOnce;

        this.current_x = card.current_x;
        this.current_y = card.current_y;

        this.target_x = card.target_x;
        this.target_y = card.target_y;

        this.angle = card.angle;
        this.targetAngle = card.targetAngle;

        this.drawScale = card.drawScale;
        this.targetDrawScale = card.targetDrawScale;
        this.name = card.name;
        this.uuid = card.uuid;
        this.isCostModifiedForTurn = card.isCostModifiedForTurn;
        this.isCostModified = card.isCostModified;
        this.magicNumber = card.magicNumber;
    }

    public CardState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.cardId = parsed.get("card_id").getAsString();
        this.upgraded = parsed.get("upgraded").getAsBoolean();
        this.baseDamage = parsed.get("base_damage").getAsInt();
        this.cost = parsed.get("cost").getAsInt();
        this.costForTurn = parsed.get("cost_for_turn").getAsInt();

        this.inBottleLightning = parsed.get("in_bottle_lightning").getAsBoolean();
        this.inBottleTornado = parsed.get("in_bottle_tornado").getAsBoolean();
        this.inBottleFlame = parsed.get("in_bottle_flame").getAsBoolean();

        this.name = parsed.get("name").getAsString();
        this.uuid = UUID.fromString(parsed.get("uuid").getAsString());
        this.freeToPlayOnce = parsed.get("free_to_play_once").getAsBoolean();
        this.isCostModifiedForTurn = parsed.get("is_cost_modified_for_turn").getAsBoolean();
        this.isCostModified = parsed.get("is_cost_modified").getAsBoolean();
        this.magicNumber = parsed.get("magic_number").getAsInt();
        this.block = parsed.get("block").getAsInt();

        // TODO
        this.current_x = 0;
        this.current_y = 0;

        this.target_x = 0;
        this.target_y = 0;

        this.angle = 0;
        this.targetAngle = 0;

        this.drawScale = 1.0F;
        this.targetDrawScale = 1.0F;
    }

    public AbstractCard loadCard() {
        AbstractCard result = CardLibrary.getCard(cardId).makeCopy();

        if (upgraded) {
            result.upgrade();
        }

        result.current_x = current_x;
        result.current_y = current_y;

        result.target_x = target_x;
        result.target_y = target_y;

        result.angle = angle;
        result.targetAngle = targetAngle;

        result.drawScale = drawScale;
        result.targetDrawScale = targetDrawScale;

        result.baseDamage = baseDamage;
        result.cost = cost;
        result.costForTurn = costForTurn;

        result.inBottleLightning = inBottleLightning;
        result.inBottleFlame = inBottleFlame;
        result.inBottleTornado = inBottleTornado;
        result.name = name;

        result.uuid = uuid;
        result.freeToPlayOnce = freeToPlayOnce;
        result.isCostModifiedForTurn = isCostModifiedForTurn;
        result.isCostModified = isCostModified;
        result.magicNumber = magicNumber;
        result.block = block;

        return result;
    }

    public String getName() {
        return cardId;
    }

    public String encode() {
        JsonObject cardStateJson = new JsonObject();

        cardStateJson.addProperty("card_id", cardId);
        cardStateJson.addProperty("upgraded", upgraded);
        cardStateJson.addProperty("base_damage", baseDamage);
        cardStateJson.addProperty("cost", cost);
        cardStateJson.addProperty("cost_for_turn", costForTurn);
        cardStateJson.addProperty("in_bottle_lightning", inBottleLightning);
        cardStateJson.addProperty("in_bottle_flame", inBottleFlame);
        cardStateJson.addProperty("in_bottle_tornado", inBottleTornado);
        cardStateJson.addProperty("name", name);
        cardStateJson.addProperty("free_to_play_once", freeToPlayOnce);
        cardStateJson.addProperty("uuid", uuid.toString());
        cardStateJson.addProperty("is_cost_modified_for_turn", isCostModifiedForTurn);
        cardStateJson.addProperty("is_cost_modified", isCostModified);
        cardStateJson.addProperty("magic_number", magicNumber);
        cardStateJson.addProperty("block", block);

        return cardStateJson.toString();
    }
}
