package pkg;

class TypeWithNewInstance {
    void method() {
        new Runnable() {
            void run() {
            }
        };
    }
}
