package pizza;

import static pizza.Pizza.Topping.*;

public class PizzaMain {
    public static void main(String[] args) {
        NyPizza pizza = new NyPizza.Builder(NyPizza.Size.SMALL)
            .addTopping(SAUSAGE).addTopping(ONION).build();
        Calzone calzone = new Calzone.Builder()
            .addTopping(HAM).sauceInside().build();
        System.out.println("pizza class is " + pizza.getClass() + ", calzone class is " + calzone.getClass());
    }
}