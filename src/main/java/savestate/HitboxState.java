package savestate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.megacrit.cardcrawl.helpers.Hitbox;

// TODO make encoder
public class HitboxState {
    private final float x;
    private final float y;
    private final float cX;
    private final float cY;
    private final float width;
    private final float height;
    private final boolean hovered;
    private final boolean justHovered;
    private final boolean clickStarted;
    private final boolean clicked;

    public HitboxState(Hitbox hitbox) {
        this.x = hitbox.x;
        this.y = hitbox.y;
        this.cX = hitbox.cX;
        this.cY = hitbox.cY;
        this.width = hitbox.width;
        this.hovered = hitbox.hovered;
        this.justHovered = hitbox.justHovered;
        this.height = hitbox.height;
        this.clicked = hitbox.clicked;
        this.clickStarted = hitbox.clickStarted;
    }

    public HitboxState(String jsonString) {
        JsonObject parsed = new JsonParser().parse(jsonString).getAsJsonObject();

        this.x = parsed.get("x").getAsFloat();
        this.y = parsed.get("y").getAsFloat();

        this.cX = parsed.get("c_x").getAsFloat();
        this.cY = parsed.get("c_y").getAsFloat();

        this.width = parsed.get("width").getAsFloat();
        this.height = parsed.get("height").getAsFloat();

        this.hovered = parsed.get("hovered").getAsBoolean();
        this.justHovered = parsed.get("just_hovered").getAsBoolean();

        this.clicked = parsed.get("clicked").getAsBoolean();
        this.clickStarted = parsed.get("click_started").getAsBoolean();
    }

    public Hitbox loadHitbox() {
        Hitbox result = new Hitbox(width, height);
        result.x = x;
        result.y = y;
        result.cX = cX;
        result.cY = cY;
        result.hovered = hovered;
        result.justHovered = justHovered;
        result.clicked = clicked;
        result.clickStarted = clickStarted;

        return result;
    }

    public String encode() {
        JsonObject hitboxStateJson = new JsonObject();

        hitboxStateJson.addProperty("x", x);
        hitboxStateJson.addProperty("y", y);
        hitboxStateJson.addProperty("c_x", cX);
        hitboxStateJson.addProperty("c_y", cY);
        hitboxStateJson.addProperty("width", width);
        hitboxStateJson.addProperty("height", height);
        hitboxStateJson.addProperty("hovered", hovered);
        hitboxStateJson.addProperty("just_hovered", justHovered);
        hitboxStateJson.addProperty("click_started", clickStarted);
        hitboxStateJson.addProperty("clicked", clicked);

        return hitboxStateJson.toString();
    }
}
