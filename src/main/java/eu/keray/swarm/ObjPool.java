package eu.keray.swarm;

import java.lang.reflect.Array;

public class ObjPool<T> {
    Class<T> clazz;
    T[] stack;
    private int size;

    @SuppressWarnings("unchecked")
    public ObjPool(Class<T> clazz) {
        this.clazz = clazz;
        this.stack = (T[]) Array.newInstance(clazz, 128);
    }

    public T take() {
        try {
            if (this.size <= 0)
                return this.clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this.stack[--this.size];
    }

    @SuppressWarnings("unchecked")
    private void ensureSize(int size) {
        if (this.stack.length < size) {
            int newsize = Math.max((int)(this.stack.length * 1.4F), this.stack.length + 10);
            T[] newstack = (T[]) Array.newInstance(this.clazz, newsize);
            // FIX: was incorrectly passing this.clazz instead of this.stack
            System.arraycopy(this.stack, 0, newstack, 0, this.size);
            this.stack = newstack;
        }
    }

    public void give(T obj) {
        ensureSize(this.size + 1);
        this.stack[this.size++] = obj;
    }
}
