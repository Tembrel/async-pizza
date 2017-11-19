package pizza;

import java.util.concurrent.*;
import java.util.function.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/*
 * Makes pizza with CountDownLatch. Pizza is made by
 * assembling rolled risen dough, tomato sauce, and grated
 * cheese on a tray and baking it.
 */
public class LatchPizzaBuilder {

    private final Executor exec;
    
    private final CountDownLatch doughCombined = new CountDownLatch(1);    
    private final CountDownLatch doughRisen = new CountDownLatch(1);    
    private final CountDownLatch layerReady = new CountDownLatch(3);

    /* @GuardedBy("doughCombined") */ private String rawDough;
    /* @GuardedBy("doughRisen")    */ private String risenDough;
    /* @GuardedBy("layerReady")    */ private String crust;
    /* @GuardedBy("layerReady")    */ private String sauce;
    /* @GuardedBy("layerReady")    */ private String cheese;

    public LatchPizzaBuilder(Executor exec) {
        this.exec = exec;
    }
    
    RuntimeException unexpectedInterruption() {
        return new RuntimeException("Punting on unexpected interruption");
    }

    /** Fakes work and traces progress. */
    void work(CountDownLatch waitFor, CountDownLatch ready, long millis,
            String task, Supplier<String> supplier, Consumer<String> action) {
        exec.execute(() -> {
            try {
                if (waitFor != null)
                    waitFor.await();
                String in = supplier.get();
                System.out.println("Started " + task + ": " + in);
                MILLISECONDS.sleep(millis);
                action.accept(in);
                System.out.println("Finished " + task + ": " + in);
                ready.countDown();
            } catch (InterruptedException ex) {
                throw unexpectedInterruption();
            }
        });
    }

    void combine(CountDownLatch ready, Consumer<String> action, String... ingredients) {
        work(null, ready, 120 * ingredients.length, "combining",
            () -> String.join("+", ingredients), action);
    }

    void letRise() {
        work(doughCombined, doughRisen, 100, "letting rise",
            () -> rawDough, t -> { risenDough = "risen " + t; });
    }

    void rollOut() {
        work(doughRisen, layerReady, 50, "rolling out",
            () -> risenDough, t -> { crust = "rolled-out " + t; });
    }

    void grateCheese() {
        work(null, layerReady, 400, "grating",
            () -> "cheese", t -> { cheese = "grated " + t; });
    }

    String build() {
        combine(doughCombined, t -> { rawDough = t; }, "Flour", "Water", "Yeast");
        letRise();
        combine(layerReady, t -> { sauce = t; }, "Tomato", "Oil", "Garlic", "Oregano");
        grateCheese();
        rollOut();
        try{
            layerReady.await(); // Wait for all three layers to be ready.
            return cheese + " on " + sauce + " on " + crust;
        } catch (InterruptedException ex) {
            throw unexpectedInterruption();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int CONCURRENCY = 4;
        ExecutorService exec = Executors.newFixedThreadPool(CONCURRENCY);
        try {
            LatchPizzaBuilder builder = new LatchPizzaBuilder(exec);
            String pizza = builder.build();
            System.out.println("Ready to bake: " + pizza);
        } finally {
            exec.shutdown();
        }
    }
}
