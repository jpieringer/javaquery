package pkg;

public class SingleType {
    public Runnable method() {
        return new Runnable() {
            @Override
            public void run() {
            }
        };
    }
}
