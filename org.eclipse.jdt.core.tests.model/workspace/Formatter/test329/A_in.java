int bogus(int a) {
    if (a > 1)
        try {a = 1;} finally {
    } else
        a = 0;
    return a;
}