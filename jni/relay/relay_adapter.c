#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include "relay_adapter.h"
#define DEVICE_RELAY "/dev/relay"
#define RELAY_ON 1
#define RELAY_OFF 0

int fd;

int relay_open(void) {
    fd = open(DEVICE_RELAY, O_RDONLY);
    return fd;
}

int relay_close(void) {
    fd = close(fd);
    return fd;
}

int relay_set_on(int relay_num) {
    int ret = ioctl(fd, RELAY_ON, &relay_num);
    return fd;
}

int relay_set_off(int relay_num) {
    int ret = ioctl(fd, RELAY_OFF, &relay_num);
    return fd;
}
