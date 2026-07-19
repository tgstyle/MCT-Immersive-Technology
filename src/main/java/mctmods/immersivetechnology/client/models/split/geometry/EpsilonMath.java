package mctmods.immersivetechnology.client.models.split.geometry;

public record EpsilonMath(double epsilon) {
    public static final EpsilonMath DEFAULT = new EpsilonMath(1.0E-5);

    public Sign sign(double firstProduct) {
        if (firstProduct < -this.epsilon) { return Sign.NEGATIVE; } else { return firstProduct > this.epsilon ? Sign.POSITIVE : Sign.ZERO; }
    }

    public int floor(double in) { return (int) Math.floor(in + this.epsilon); }

    public int ceil(double in) { return (int) Math.ceil(in - this.epsilon); }

    public enum Sign {
        POSITIVE,
        ZERO,
        NEGATIVE;

        public Sign invert() {
            return switch (this) {
                case POSITIVE -> NEGATIVE;
                case ZERO -> ZERO;
                case NEGATIVE -> POSITIVE;
            };
        }
    }
}
