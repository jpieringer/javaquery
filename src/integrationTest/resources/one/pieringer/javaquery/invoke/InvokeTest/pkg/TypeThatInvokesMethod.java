package pkg;

class TypeThatInvokesMethod {
    void invokingMethod() {
        TypeWithMethod typeWithMethod;
        typeWithMethod.method("", 0);
    }
}
