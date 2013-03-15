class Base {
}
class Foo<U extends Base, V extends Bar<U, @Marker Foo<U, V>>> {
}
class Bar<E extends Base, F extends Foo<E, @Marker Bar<E, F>>> {
}
