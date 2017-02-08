package pizza;

import java.util.*;

// Builder pattern for class hierarchies
public abstract class Pizza {
    public enum Topping { HAM, MUSHROOM, ONION, PEPPER, SAUSAGE }
    final Set<Topping> tops;

    abstract static class Builder<T extends Pizza, B extends Builder<T, B>> {
        EnumSet<Topping> tops = EnumSet.noneOf(Topping.class);
        public B addTopping(Topping topping) {
            tops.add(Objects.requireNonNull(topping));
            return self();
        }

        abstract Pizza build();

        protected abstract B self();
    }

    Pizza(Builder<?, ?> builder) {
        tops = builder.tops.clone(); // Defensive copy (Item 39)
    }
}
