package edu;

public class TestClass {
    public void method() {
    }
}


public class AClassWithCalls {
    public void caller() {
        helper();
        TestClass other = new TestClass();
        other.method();
    }

    private void helper() {
    }
}
