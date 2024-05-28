public class SharedMemoryReader {
    static {
        System.loadLibrary("shared_memory");
    }

    private native String readFromSharedMemory();

    public static void main(String[] args) {
        SharedMemoryReader reader = new SharedMemoryReader();
        String message = reader.readFromSharedMemory();
        System.out.println("Message read from shared memory: " + message);
    }
}