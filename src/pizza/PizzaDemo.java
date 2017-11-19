package pizza;

/*
 * Driver for LatchPizza and FuturePizza.
 */
public class PizzaDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Demo LatchPizza\n");
        LatchPizzaBuilder.main(args);
        
        System.out.println("\n\nDemo FuturePizza\n");
        FuturePizzaBuilder.main(args);
    }
}
