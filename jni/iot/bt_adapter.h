#ifndef _BT_ADAPTER_H
#define _BT_ADAPTER_H

#include <sys/types.h>
#define DEVICE_BT_UART "/dev/ttySAC3"

/* @return */
/* the file descriptor number */
/* negative number if error happens */
int bt_open(void);
/* @return */
/* 0 if close without error */
/* negative number if error happens */
int bt_close(void);
/* @param */
/* ch - the buffer character address */
/* @return */
/* total size get */
/* -1 if error happens */
ssize_t bt_read(int* ch);
/* @param */
/* buffer - the buffer that stores the data */
/* count - the number of bytes needs to be sent */
/* @return */
/* total size sent */
/* -1 if error happens */
ssize_t bt_write(const void *buffer, size_t count);
/* nop */
int bt_join(void);
/* nop */
int bt_leave(void);

#endif // _BT_ADAPTER_H
