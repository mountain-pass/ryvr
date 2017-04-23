from net.grinder.script.Grinder import grinder
from net.grinder.script import Test

def load_class(class_name):
    m = __import__(class_name)
    for comp in class_name.split('.')[1:]:
        m = getattr(m, comp)
    return m
 
test_runner = load_class(grinder.getProperties().getProperty('grinder.java_test_runner'))

test1 = Test(1, "Test")
test1.record(test_runner.call)
 
class TestRunner:
 
    def __init__(self):
        self.runner = test_runner(grinder)
 
    def __call__(self):
        self.runner.call(grinder)
        
    def __del__(self):
        self.runner.tearDown(grinder)