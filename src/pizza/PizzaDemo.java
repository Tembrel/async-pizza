package pizza;

/*
 * Driver for LatchPizza and FuturePizza.
 */
public class PizzaDemo {
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Demo LatchPizzaBuilder\n");
        LatchPizzaBuilder.main(args);
        
        System.out.println("\n\nDemo FuturePizzaBulder\n");
        FuturePizzaBuilder.main(args);
    }
}
