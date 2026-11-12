class TestClass:
    def method(self):
        pass

def caller():
    helper()
    obj = TestClass()
    obj.method()


def helper():
    pass
