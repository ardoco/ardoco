package edu;

public class TestClass {
    public void method() {
        /* empty for test purposes */
    }
}


public class AClassWithCalls {
    public void caller() {
        helper();
        TestClass other = new TestClass();
        other.method();
    }

    private void helper() {
        /* empty for test purposes */
    }
}
