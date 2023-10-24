#ifndef _RELAY_ADAPTER_H
#define _RELAY_ADAPTER_H

int relay_open(void);
int relay_close(void);
int relay_set_on(int relay_num);
int relay_set_off(int relay_num);

#endif // _RELAY_ADAPTER_H
