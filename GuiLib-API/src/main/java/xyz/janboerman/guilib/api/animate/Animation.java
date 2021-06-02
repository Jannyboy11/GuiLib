package xyz.janboerman.guilib.api.animate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * An Animation is a container of {@link Frame}s.
 * Animations can be executed using an {@link AnimationRunner}.
 */
public interface Animation {

    /**
     * Resets the animation to its initial state.
     */
    public void reset();

    /**
     * Get the next frame of the animation.
     * @return the next frame
     */
    public Frame nextFrame();

    /**
     * Tests whether this animation has another frame.
     * @return true if this animation has at least one more frame, otherwise false
     */
    public boolean hasNextFrame();

    /**
     * Create an Animation from an array of frames.
     * @param frames the frames
     * @return a new Animation
     */
    public static Animation ofFrames(Frame... frames) {
        return new SimpleAnimation(List.of(frames));
    }

    /**
     * Create an animation from a list of frames.
     * @param frames the frames
     * @return a new Animation
     */
    public static Animation ofFrames(List<? extends Frame> frames) {
        return new SimpleAnimation(frames);
    }

    /**
     * Create an animation that lazily generates an infinite number of frames.
     * @param seed the first frame
     * @param nextFrame the function that knows how to compute the next frame
     * @param <F> the type of Frames
     * @return a new Animation
     */
    public static <F extends Frame> Animation infinite(F seed, UnaryOperator<F> nextFrame) {
        return new InfiniteAnimation(seed, nextFrame);
    }

    /**
     * Turn the animation into an auto-resetting animation, so that it automatically starts over when this animation is at its end.
     * @return an animation that loops this animation
     */
    public default Animation continuously() {
        return new ContinuousAnimation(this);
    }

    /**
     * Append another animation to this animation.
     * @param next the appended animation
     * @return an animation that first steps through the current animation, and then through the next animation
     */
    public default Animation andThen(Animation next) {
        return new ConcatAnimation(this, next);
    }
}

class ConcatAnimation implements Animation {

    private final Animation one, two;

    ConcatAnimation(Animation one, Animation two) {
        this.one = Objects.requireNonNull(one, "one cannot be null");
        this.two = Objects.requireNonNull(two, "two cannot be null");
    }

    @Override
    public void reset() {
        one.reset();
        two.reset();
    }

    @Override
    public Frame nextFrame() {
        if (one.hasNextFrame()) return one.nextFrame();
        return two.nextFrame();
    }

    @Override
    public boolean hasNextFrame() {
        return one.hasNextFrame() || two.hasNextFrame();
    }

    @Override
    public int hashCode() {
        return Objects.hash(one, two);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ConcatAnimation)) return false;

        ConcatAnimation that = (ConcatAnimation) obj;
        return Objects.equals(this.one, that.one)
                && Objects.equals(this.two, that.two);
    }

    @Override
    public String toString() {
        return "ConcatAnimation(one=" + one + ",two=" + two + ")";
    }
}

class ContinuousAnimation implements Animation {

    private final Animation wrapped;

    ContinuousAnimation(Animation wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "wrapped cannot be null");
    }

    @Override
    public void reset() {
        wrapped.reset();
    }

    @Override
    public Frame nextFrame() {
        if (!wrapped.hasNextFrame()) wrapped.reset();
        return wrapped.nextFrame();
    }

    @Override
    public boolean hasNextFrame() {
        return true;
    }

    @Override
    public Animation continuously() {
        return this;
    }

    @Override
    public Animation andThen(Animation next) {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(wrapped);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ContinuousAnimation)) return false;

        ContinuousAnimation that = (ContinuousAnimation) obj;
        return Objects.equals(this.wrapped, that.wrapped);
    }

    @Override
    public String toString() {
        return "ContinuousAnimation(wrapped=" + wrapped + ")";
    }
}

class InfiniteAnimation<F extends Frame> implements Animation {

    private final F startingFrame;
    private final UnaryOperator<F> nextFrame;

    private F state;

    InfiniteAnimation(F startingFrame, UnaryOperator<F> nextFrame) {
        this.startingFrame = Objects.requireNonNull(startingFrame, "startingFrame cannot be null");
        this.nextFrame = Objects.requireNonNull(nextFrame, "nextFrame cannot be null");

        state = startingFrame;
    }

    @Override
    public void reset() {
        state = startingFrame;
    }

    @Override
    public F nextFrame() {
        F value = state;
        state = nextFrame.apply(state);
        return value;
    }

    @Override
    public boolean hasNextFrame() {
        return true;
    }

    @Override
    public Animation continuously() {
        return this;
    }

    @Override
    public Animation andThen(Animation next) {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startingFrame, nextFrame, state);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof InfiniteAnimation)) return false;

        InfiniteAnimation that = (InfiniteAnimation) obj;
        return Objects.equals(this.startingFrame, that.startingFrame)
                && Objects.equals(this.nextFrame, that.nextFrame)
                && Objects.equals(this.state, that.state);
    }

    @Override
    public String toString() {
        return "InfiniteAnimation(startingFrame=" + startingFrame + ",nextFrame=" + nextFrame + ",state=" + state + ")";
    }
}

class SimpleAnimation implements Animation {

    private int currentIndex;
    private final ArrayList<? extends Frame> frames;

    private SimpleAnimation(int index, ArrayList<? extends Frame> frames) {
        this.currentIndex = index;
        this.frames = frames;
    }

    SimpleAnimation(List<? extends Frame> frames) {
        Objects.requireNonNull(frames, "frames cannot be null");
        if (frames.isEmpty()) throw new IllegalArgumentException("frames cannot be empty");

        this.frames = new ArrayList<>(frames);
    }

    @Override
    public void reset() {
        this.currentIndex = 0;
    }

    @Override
    public Frame nextFrame() {
        return frames.get(currentIndex++);
    }

    @Override
    public boolean hasNextFrame() {
        return currentIndex < frames.size();
    }

    @Override
    public Animation andThen(Animation next) {
        if (next instanceof SimpleAnimation) {
            SimpleAnimation that = (SimpleAnimation) next;

            ArrayList<Frame> newFrames = new ArrayList<>(this.frames.size() + that.frames.size());
            newFrames.addAll(this.frames);
            newFrames.addAll(that.frames);
            return new SimpleAnimation(currentIndex, newFrames);
        } else {
            return Animation.super.andThen(next);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentIndex, frames);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof SimpleAnimation)) return false;

        SimpleAnimation that = (SimpleAnimation) obj;
        return this.currentIndex == that.currentIndex
                && Objects.equals(this.frames, that.frames);
    }

    @Override
    public String toString() {
        return "SimpleAnimation(currentIndex=" + currentIndex + ",frames=" + frames + ")";
    }
}

