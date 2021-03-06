package compozitor.processor.core.interfaces;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class LazyLoadProxy<T> {
  private State state;

  private final Supplier<T> supplier;

  private T value;

  public LazyLoadProxy(Supplier<T> supplier) {
    this.state = State.INITIAL;
    this.supplier = supplier;
  }

  public synchronized T execute() {
    if (State.INITIAL.equals(this.state)) {
      this.value = this.supplier.get();
      this.state = State.RAN;
    }

    return this.value;
  }

  enum State {
    INITIAL,
    RAN;
  }
}
