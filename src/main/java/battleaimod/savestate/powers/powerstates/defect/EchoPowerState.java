package battleaimod.savestate.powers.powerstates.defect;

import basemod.ReflectionHacks;
import battleaimod.savestate.powers.PowerState;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.cards.blue.EchoForm;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.powers.EchoPower;

public class EchoPowerState extends PowerState
{
    public final int cardsDoubledThisTurn;
    
    public EchoPowerState(AbstractPower power) {
        super(power);
        cardsDoubledThisTurn = ReflectionHacks.getPrivate(power, EchoPower.class, "cardsDoubledThisTurn");
    }
    
    public EchoPowerState(String jsonString) {
        super(jsonString);
        this.cardsDoubledThisTurn = jObject.get("cards_doubled_this_turn").getAsInt();
    }

    @Override
    public AbstractPower loadPower(AbstractCreature targetAndSource) {
        EchoPower retVal = new EchoPower(targetAndSource, amount);
        ReflectionHacks.setPrivate(retVal, EchoPower.class, "cardsDoubledThisTurn", cardsDoubledThisTurn);
        return retVal;
    }
    
    @Override
    public String encode() {
        if(jObject == null)
        {
            jObject = new JsonParser().parse(super.encode()).getAsJsonObject();
        }
    
        jObject.addProperty("cards_doubled_this_turn", cardsDoubledThisTurn);
        
        return jObject.toString();
    }
}