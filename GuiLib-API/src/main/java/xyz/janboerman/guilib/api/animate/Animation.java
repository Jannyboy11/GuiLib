package xyz.janboerman.guilib.api.animate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

public interface Animation {

    public void reset();

    public Frame nextFrame();

    public boolean hasNextFrame();

    public static Animation ofFrames(Frame... frames) {
        return new SimpleAnimation(List.of(frames));
    }

    public static Animation ofFrames(List<? extends Frame> frames) {
        return new SimpleAnimation(frames);
    }

    public static <F extends Frame> Animation infinite(F seed, UnaryOperator<F> nextFrame) {
        return new InfiniteAnimation(seed, nextFrame);
    }

    public default Animation continuously() {
        return new ContinuousAnimation(this);
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

