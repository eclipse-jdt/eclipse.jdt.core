package test1;
public record Person(String name, int age) {
    @Override
    public String toString() {
        return name + " (" + age + ")";
    }
}