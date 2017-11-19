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
public class FuturePizzaBuilder {

    private final Executor exec;

    public FuturePizzaBuilder(Executor exec) {
        this.exec = exec;
    }

    /** Fakes work and traces progress. */
    <T, U> CompletableFuture<U> work(long millis, String task, Function<T, U> action, T in) {
        return supplyAsync(() -> {
            try {
                System.out.println("Started " + task + ": " + in);
                MILLISECONDS.sleep(millis);
                U out = action.apply(in);
                System.out.println("Finished " + task + ": " + out);
                return out;
            } catch (InterruptedException ex) {
                throw new RuntimeException("Punting on unexpected interruption");
            }
        }, exec);
    }

    CompletableFuture<String> combine(String... ingredients) {
        return work(120 * ingredients.length, "combining", t -> t, String.join("+", ingredients));
    }

    CompletableFuture<String> letRise(String dough) {
        return work(100, "letting rise", t -> "risen " + t, dough);
    }

    CompletableFuture<String> rollOut(String dough) {
        return work(50, "rolling out", t -> "rolled-out " + t, dough);
    }

    CompletableFuture<String> grate(String cheese) {
        return work(400, "grating", t -> "grated " + t, cheese);
    }

    CompletableFuture<String> makeLayers() {
        CompletableFuture<String> makeDough = combine("Flour", "Water", "Yeast")
            .thenCompose(this::letRise);

        CompletableFuture<String> makeSauce =
            combine("Tomato", "Oil", "Garlic", "Oregano");

        CompletableFuture<String> grateCheese = grate("Cheese");

        return makeDough
            .thenComposeAsync(this::rollOut, exec)
            .thenCombineAsync(makeSauce, (crust, sauce) -> sauce + " on " + crust, exec)
            .thenCombineAsync(grateCheese, (saucyCrust, cheese) -> cheese + " on " + saucyCrust, exec);
    }

    public static void main(String[] args) {
        int CONCURRENCY = 4;
        ExecutorService exec = Executors.newFixedThreadPool(CONCURRENCY);
        try {
            FuturePizzaBuilder builder = new FuturePizzaBuilder(exec);
            CompletableFuture<String> makeLayers = builder.makeLayers();
            System.out.println("Ready to bake: " + makeLayers.join());
        } finally {
            exec.shutdown();
        }
    }
}
