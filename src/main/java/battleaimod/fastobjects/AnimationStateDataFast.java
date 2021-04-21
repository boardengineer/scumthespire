package battleaimod.fastobjects;

import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.SkeletonData;

public class AnimationStateDataFast extends AnimationStateData {
    public AnimationStateDataFast() {
        super(new SkeletonData());
    }

    @Override
    public void setMix(Animation from, Animation to, float duration) {
    }

    @Override
    public void setMix(String fromName, String toName, float duration) {
    }
}
