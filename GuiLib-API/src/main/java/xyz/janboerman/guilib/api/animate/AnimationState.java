package xyz.janboerman.guilib.api.animate;

/**
 * A status for a {@link AnimationRunner}.
 */
public enum AnimationState {

    /**
     * Indicates that the animation runner has not started playing the animation yet.
     */
    NOT_STARTED,
    /**
     * Indicates that the animation runner is currently busy running the animation.
     */
    RUNNING,
    /**
     * Indicates that the animation has been interrupted using a call to {@link AnimationRunner#stop()}.
     */
    PAUSED,
    /**
     * Indicates that the animation is done playing. This can happen when the animation is out of frames.
     */
    FINISHED;

}
