import java.util.concurrent.*;
import java.util.function.Function;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
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
    <T, U> CompletableFuture<U> work(long millis, String action, T in, Function<T, U> task) {
        return supplyAsync(() -> {
            try {
                System.out.println("Started " + action + " " + in);
                MILLISECONDS.sleep(millis);
                U out = task.apply(in);
                System.out.println("Finished " + action + " " + in);
                return out;
            } catch (InterruptedException ex) {
                throw new RuntimeException("Punting on unexpected interruption");
            }
        }, exec);
    }

    CompletableFuture<String> combine(String... ingredients) {
        return work(120 * ingredients.length, "combining",
            String.join(", ", ingredients), t -> "{"+t+"}");
    }

    CompletableFuture<String> letRise(String dough) {
        return work(100, "letting rise", dough, t -> "risen " + t);
    }

    CompletableFuture<String> rollOut(String dough) {
        return work(50, "rolling out", dough, t -> "rolled-out " + t);
    }

    CompletableFuture<String> grate(String cheese) {
        return work(400, "grating", cheese, t -> "grated " + t);
    }

    CompletableFuture<String> buildPizza() {
        CompletableFuture<String> makeDough = combine("flour", "water", "yeast")
            .thenComposeAsync(this::letRise, exec);

        CompletableFuture<String> makeSauce =
            combine("tomato", "oil", "garlic", "oregano");

        CompletableFuture<String> grateCheese = grate("cheese");

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
            String pizza = builder.buildPizza().join();
            System.out.println("Ready to bake " + pizza);
        } finally {
            exec.shutdown();
        }
    }
}
