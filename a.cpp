#include <fcntl.h>
#include <sys/mman.h>
#include <unistd.h>
#include <cstring>
#include <iostream>

int main() {
    const char* name = "/my_shared_memory";
    const int SIZE = 4096;
    const char* message = "Hello from C++!";

    int shm_fd;
    void* ptr;

    shm_fd = shm_open(name, O_CREAT | O_RDWR, 0666);
    ftruncate(shm_fd, SIZE);
    ptr = mmap(0, SIZE, PROT_WRITE, MAP_SHARED, shm_fd, 0);

    sprintf((char*)ptr, "%s", message);
    std::cout << "Message written to shared memory: " << message << std::endl;

    return 0;
}
