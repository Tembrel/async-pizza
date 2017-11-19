package pizza;

import java.util.concurrent.*;
import java.util.function.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/*
 * Makes pizza with CountDownLatch. Pizza is made by
 * assembling rolled risen dough, tomato sauce, and grated
 * cheese on a tray and baking it.
 */
public class LatchPizza {

    private final ExecutorService exec;
    
    private final CountDownLatch doughCombined = new CountDownLatch(1);
    private volatile String rawDough;
    
    private final CountDownLatch doughRisen = new CountDownLatch(1);
    private volatile String risenDough;
    
    private final CountDownLatch layersReady = new CountDownLatch(3);
    private volatile String crust;
    private volatile String sauce;
    private volatile String cheese;

    public LatchPizza(ExecutorService exec) {
        this.exec = exec;
    }

    /** Fakes work and traces progress. */
    void work(CountDownLatch waitFor, CountDownLatch ready,
            long millis, String task, Supplier<String> supplier,
            Consumer<String> action) {
        exec.submit(() -> {
            try {
                if (waitFor != null) {
                    waitFor.await();
                }
                String a = supplier.get();
                System.out.println("Started " + task + ": " + a);
                MILLISECONDS.sleep(millis);
                action.accept(a);
                System.out.println("Finished " + task + ": " + a);
                ready.countDown();
            } catch (InterruptedException ex) {
                throw new RuntimeException("Punting on unexpected interruption");
            }
        });
    }

    void combine(CountDownLatch ready, Consumer<String> action, String... ingredients) {
        work(null, ready, 120 * ingredients.length,
            "combining", () -> String.join("+", ingredients), action);
    }

    void letRise() {
        work(doughCombined, doughRisen, 100, "letting rise", () -> rawDough, t -> { risenDough = "risen " + t; });
    }

    void rollDough() {
        work(doughRisen, layersReady, 50, "rolling", () -> risenDough, t -> { crust = "rolled " + t; });
    }

    void grateCheese() {
        work(null, layersReady, 400, "grating", () -> "cheese", t -> { cheese = "grated " + t; });
    }

    String makePizza() throws InterruptedException {
        combine(doughCombined, t -> { rawDough = t; }, "Flour", "Water", "Yeast");
        letRise();
        combine(layersReady, t -> { sauce = t; }, "Tomato", "Oil", "Garlic", "Oregano");
        grateCheese();
        rollDough();

        layersReady.await();
        
        return cheese + " on " + sauce + " on " + crust;
    }

    public static void main(String[] args) throws InterruptedException {
        int CONCURRENCY = 4;
        ExecutorService exec = Executors.newFixedThreadPool(CONCURRENCY);
        try {
            LatchPizza cp = new LatchPizza(exec);
            String pizza = cp.makePizza();
            System.out.println("Ready to bake: " + pizza);
        } finally {
            exec.shutdown();
        }
    }
}
