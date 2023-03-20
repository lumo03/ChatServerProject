package backend.types;

@FunctionalInterface
public interface OrderTimerFn {
    void run(String s);
}
