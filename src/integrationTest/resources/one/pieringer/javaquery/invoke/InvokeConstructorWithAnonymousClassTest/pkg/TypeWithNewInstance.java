package pkg;

class TypeWithNewInstance {
    void method() {
        new Runnable() {
            public void run() {
            }
        };
    }
}
