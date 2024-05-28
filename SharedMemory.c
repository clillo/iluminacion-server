#include <jni.h>
#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>

JNIEXPORT jstring JNICALL Java_SharedMemoryReader_readFromSharedMemory(JNIEnv *env, jobject thisObj) {
    const char* name = "/my_shared_memory";
    const int SIZE = 4096;

    int shm_fd;
    void* ptr;

    shm_fd = shm_open(name, O_RDONLY, 0666);
    ptr = mmap(0, SIZE, PROT_READ, MAP_SHARED, shm_fd, 0);

    jstring result = (*env)->NewStringUTF(env, (char*)ptr);
    munmap(ptr, SIZE);
    close(shm_fd);

    return result;
}