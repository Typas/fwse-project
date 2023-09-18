#include <unistd.h>    // read(), write(), close()
#include <fcntl.h>     // open()
#include <termios.h>   // terminal
#include "bt_adapter.h"

int fd;
struct termios newtio, orgtio;

int bt_open(void) {
    int ret;

    open(DEVICE_BT_UART, O_RDWR);
    if (fd < 0) {
        return fd;
    }

    tcgetattr(fd, &orgtio);
    tcgetattr(fd, &newtio);
    cfsetispeed(&newtio, B38400);
    cfsetospeed(&newtio, B38400);
    newtio.c_cflag |= (CLOCAL | CREAD | CS8 | HUPCL);
    newtio.c_cflag &= ~PARENB;
    newtio.c_cflag &= ~CSTOPB;
    newtio.c_lflag &= ~ECHO;
    if ((ret = tcsetattr(fd, TCSANOW, &newtio)) < 0) {
        return ret;
    }

    return fd;
}

int bt_close(void) {
    tcsetattr(fd, TCSANOW, &orgtio);
    return close(fd);
}

ssize_t bt_read(int* ch) {
    return read(fd, &ch, sizeof(int));
}

ssize_t bt_write(const void *buffer, size_t count) {
    return write(fd, buffer, count);
}

int bt_join(void) {
    /* TODO: send join signal */
    return 0;
}

int bt_leave(void) {
    /* TODO: send leave signal */
    return 0;
}
