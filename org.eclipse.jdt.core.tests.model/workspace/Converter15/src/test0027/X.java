package test0027;

public enum X {
    PENNY(1), NICKEL(5), DIME(10), QUARTER(25);

    X(int value) { this.value = value; }

    private final int value;

    public int value() { return value; }
}