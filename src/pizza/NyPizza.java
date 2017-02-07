package pizza;

import java.util.*;

public class NyPizza extends Pizza {
    public enum Size {SMALL, MEDIUM, LARGE }
    private final Size size;

    public static class Builder extends Pizza.Builder<NyPizza, Builder> {
        private final Size size;

        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }

        public NyPizza build() {
            return new NyPizza(this);
        }
    }

    private NyPizza(Builder builder) {
        super(builder);
        size = builder.size;
    }
}
