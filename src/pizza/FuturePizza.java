package pizza;

import java.util.concurrent.*;
import java.util.function.Function;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/*
 * Makes pizza with CompletableFuture. Pizza is made by
 * assembling rolled risen dough, tomato sauce, and grated
 * cheese on a tray and baking it.
 */
public class FuturePizza {

    private final Executor exec;

    public FuturePizza(Executor exec) {
        this.exec = exec;
    }

    /** Fakes work and traces progress. */
    <T, U> CompletableFuture<U> work(long millis, String task, Function<T, U> action, T arg) {
        return supplyAsync(() -> {
            try {
                Object a = arg instanceof String[] ? String.join(", ", (String[]) arg) : arg;
                System.out.println("Started " + task + ": " + a);
                MILLISECONDS.sleep(millis);
                U result = action.apply(arg);
                System.out.println("Finished " + task + ": " + result);
                return result;
            } catch (InterruptedException ex) {
                throw new RuntimeException("Punting on unexpected interruption");
            }
        }, exec);
    }

    CompletableFuture<String> combine(String... ingredients) {
        return work(120 * ingredients.length, "combining", t -> String.join("+", t), ingredients);
    }

    CompletableFuture<String> letRise(String dough) {
        return work(100, "letting rise", t -> "risen " + t, dough);
    }

    CompletableFuture<String> roll(String dough) {
        return work(50, "rolling", t -> "rolled " + t, dough);
    }

    CompletableFuture<String> grate(String cheese) {
        return work(400, "grating", t -> "grated " + t, cheese);
    }

    CompletableFuture<String> makePizza() {
        CompletableFuture<String> makeDough = combine("Flour", "Water", "Yeast")
            .thenCompose(this::letRise);

        CompletableFuture<String> makeSauce =
            combine("Tomato", "Oil", "Garlic", "Oregano");

        CompletableFuture<String> grateCheese = grate("Cheese");

        return makeDough
            .thenComposeAsync(this::roll, exec)
            .thenCombineAsync(makeSauce, (crust, sauce) -> sauce + " on " + crust, exec)
            .thenCombineAsync(grateCheese, (saucyCrust, cheese) -> cheese + " on " + saucyCrust, exec);
    }

    public static void main(String[] args) {
        int CONCURRENCY = 4;
        ExecutorService exec = Executors.newFixedThreadPool(CONCURRENCY);
        try {
            FuturePizza cp = new FuturePizza(exec);
            CompletableFuture<String> makePizza = cp.makePizza();
            System.out.println("Ready to bake: " + makePizza.join());
        } finally {
            exec.shutdown();
        }
    }
}
