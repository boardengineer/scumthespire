package communicationmod;

import com.megacrit.cardcrawl.helpers.Hitbox;

public class HitboxLoader {
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

    public HitboxLoader(Hitbox hitbox) {
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
}
