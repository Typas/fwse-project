#include <bluetooth/bluetooth.h>
#include <bluetooth/rfcomm.h>
#include <sys/socket.h>
#include <unistd.h> // read(), write(), close()
/* #include <fcntl.h>     // open() */
/* #include <termios.h>   // terminal */
#include "bt_adapter.h"

/* int fd; */
int sock;
int status;                              // 0 means normal
char local_addr[19] = "0021:11:01496B";  // slave, client, hard-coded is bad but no time left
char remote_addr[19] = "0021:11:016E0B"; // master, server, hard-coded is bad but no time left
struct sockaddr_rc remote;
/* struct termios newtio, orgtio; */

int bt_open(void) {
    // allocate a socket
    sock = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
    if (sock < 0) {
        return sock;
    }
    // set the connection parameters
    remote.rc_family = AF_BLUETOOTH;
    remote.rc_channel = (uint8_t)1;
    str2ba(remote_addr, &remote.rc_bdaddr);

    /* int ret; */
    /* open(DEVICE_BT_UART, O_RDWR); */
    /* if (fd < 0) { */
    /*     return fd; */
    /* } */

    /* tcgetattr(fd, &orgtio); */
    /* tcgetattr(fd, &newtio); */
    /* cfsetispeed(&newtio, B38400); */
    /* cfsetospeed(&newtio, B38400); */
    /* newtio.c_cflag |= (CLOCAL | CREAD | CS8 | HUPCL); */
    /* newtio.c_cflag &= ~PARENB; */
    /* newtio.c_cflag &= ~CSTOPB; */
    /* newtio.c_lflag &= ~ECHO; */
    /* if ((ret = tcsetattr(fd, TCSANOW, &newtio)) < 0) { */
    /*     return ret; */
    /* } */

    return sock;
}

int bt_close(void) {
    /* tcsetattr(fd, TCSANOW, &orgtio); */
    /* return close(fd); */
    close(sock);
    return 0;
}

ssize_t bt_read(void *buffer, size_t nbyte) {
    /* return read(fd, &ch, sizeof(int)); */
    /* TODO: limit nbyte? */
    if (status < 0)
        return status;
    status = read(sock, buffer, nbyte);
    return status;
}

ssize_t bt_write(const void *buffer, size_t nbyte) {
    /* return write(fd, buffer, count); */
    if (status < 0)
        return status;
    status = write(sock, buffer, nbyte);
    return status;
}

int bt_join(void) {
    status = connect(sock, (struct sockaddr *)&remote, sizeof(remote));
    return status;
}

int bt_leave(void) {
    /* passively get dc */
    return 0;
}
