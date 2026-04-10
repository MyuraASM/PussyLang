package pussylang.vm;

class Upvalue {
    private final VM vm;
    final int index;
    boolean isClosed;
    Object closedValue;

    Upvalue(VM vm, int index) {
        this.vm = vm;
        this.index = index;
        this.isClosed = false;
    }

    Object get() {
        if (isClosed) return closedValue;
        return vm.stack[index];
    }

    void set(Object value) {
        if (isClosed) {
            closedValue = value;
        } else {
            vm.stack[index] = value;
        }
    }

    void close() {
        closedValue = vm.stack[index];
        isClosed = true;
    }
}