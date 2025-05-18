// lib/utils/buffer.ts
export class Buffer<T> {
    private items: T[] = [];
    private maxSize: number;

    constructor(maxSize: number) {
        this.maxSize = maxSize;
    }

    add(item: T) {
        this.items.push(item);
        if (this.items.length > this.maxSize) {
            this.items.shift();
        }
    }

    flush(): T[] {
        const items = [...this.items];
        this.items = [];
        return items;
    }

    clear() {
        this.items = [];
    }
}