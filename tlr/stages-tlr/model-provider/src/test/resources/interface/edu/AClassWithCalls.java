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
        java.util.List<String> list = new java.util.ArrayList<String>();
    }

    private void helper() {
        /* empty for test purposes */
    }
}
