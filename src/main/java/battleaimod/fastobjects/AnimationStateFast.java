package battleaimod.fastobjects;

import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.AnimationState;

public class AnimationStateFast extends AnimationState {
    @Override
    public TrackEntry setAnimation(int trackIndex, String animationName, boolean loop) {
        return new TrackEntry();
    }

    @Override
    public TrackEntry addAnimation(int trackIndex, Animation animation, boolean loop, float delay) {
        return null;
    }

    @Override
    public TrackEntry addAnimation(int trackIndex, String animationName, boolean loop, float delay) {
        return null;
    }

    @Override
    public TrackEntry getCurrent(int trackIndex) {
        return new TrackEntry();
    }
}
