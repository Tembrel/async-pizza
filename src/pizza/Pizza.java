package pizza;

import java.util.*;

// Builder pattern for class hierarchies
public abstract class Pizza {
    public enum Topping { HAM, MUSHROOM, ONION, PEPPER, SAUSAGE }
    final Set<Topping> tops;

    abstract static class Builder<T extends Pizza, B extends Builder<T, ? extends B>> {
        EnumSet<Topping> tops = EnumSet.noneOf(Topping.class);
        public B addTopping(Topping topping) {
            tops.add(Objects.requireNonNull(topping));
            return uncheckedCast(this);
        }
        abstract Pizza build();
    }

    Pizza(Builder<?, ?> builder) {
        tops = builder.tops.clone(); // Defensive copy (Item 39)
    }

    @SuppressWarnings("unchecked") // See Item 24
    static <S> S uncheckedCast(Object o) { return (S) o; }
}
