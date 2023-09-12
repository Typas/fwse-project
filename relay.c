/*-------------------------------------------------------------------------
 * Filename:      relay.c
 * Author:        chen-hao_liao
 * Description:   gpio driver
 * Created at:     Aug 31 2023
 *-----------------------------------------------------------------------*/

#include "asm-generic/gpio.h"
#include "linux/cred.h"
#include "linux/device.h"
#include "linux/err.h"
#include "linux/kdev_t.h"
#include "linux/printk.h"
#include "linux/stddef.h"
#include <asm/delay.h>
#include <asm/io.h>
#include <asm/uaccess.h>
#include <linux/delay.h>
#include <linux/fs.h>
#include <linux/gpio.h>
#include <linux/init.h>
#include <linux/ioport.h>
#include <linux/kernel.h>
#include <linux/miscdevice.h>
#include <linux/module.h>
#include <linux/poll.h>
#include <linux/sched.h>
#include <linux/string.h>
#include <linux/types.h>
#include <mach/gpio.h>
#include <mach/hardware.h>
#include <mach/regs-gpio.h>
#include <mach/regs-mem.h>

#include <linux/platform_device.h>
#include <mach/gpio.h>
#include <plat/gpio-cfg.h>

#define RELAY_ON 1
#define RELAY_OFF 0

#define RELAY_MAJOR 240
#define relay_name "relay"

/* pin 2 */
#define PIN EXYNOS4_GPX0(0)
#define str(s) _str(s)
#define _str(s) #s

static int state = RELAY_OFF;

static void relay_off(void) {
    printk("relay_off\n");

    state = RELAY_OFF;
    gpio_direction_output(PIN, 0);
}

static void relay_on(void) {
    printk("relay_on\n");

    state = RELAY_ON;
    gpio_direction_output(PIN, 1);
}

static ssize_t relay_read(struct file *filp, char *buf, size_t count,
                          loff_t *l) {
    /* nop */
    return count;
}

static ssize_t relay_write(struct file *filp, char *buf, size_t count,
                           loff_t *f_ops) {
    /* nop */
    return count;
}

static long relay_ioctl(struct file *file, unsigned int cmd,
                        unsigned long arg) {
    switch (cmd) {
    case RELAY_ON:
        relay_on();
        break;
    case RELAY_OFF:
        relay_off();
        break;
    default:
        break;
    }

    return 0;
}

static int relay_open(struct inode *inode, struct file *filp) {
    int ret;
    ret = gpio_request(PIN, relay_name);
    if (ret < 0) {
        printk(KERN_EMERG "open " str(PIN) " fail!\n");
        goto open_fail;
    }

    s3c_gpio_cfgpin(PIN, S3C_GPIO_OUTPUT);

    return 0;

open_fail:
    gpio_free(PIN);
    return ret;
}

static int relay_release(struct inode *inode, struct file *filp) {
    gpio_free(PIN);
    return 0;
}

static struct file_operations relay_fops = {
    owner : THIS_MODULE,
    read : relay_read,
    write : relay_write,
    unlocked_ioctl : relay_ioctl,
    open : relay_open,
    release : relay_release,
};

static struct class *relay_class;

static int __init relay_init(void) {
    int ret;
    void *dev;

    ret = register_chrdev(RELAY_MAJOR, relay_name, &relay_fops);
    if (ret < 0) {
        printk(KERN_WARNING "Can't get major %d\n", RELAY_MAJOR);
        goto reg_dev_fail;
    }

    printk("Relay driver register success!\n");

    relay_class = class_create(THIS_MODULE, relay_name);
    if (IS_ERR(relay_class)) {
        printk(KERN_WARNING "Can't make node %d\n", RELAY_MAJOR);
        ret = PTR_ERR(relay_class);
        goto create_class_fail;
    }

    dev = device_create(relay_class, NULL, MKDEV(RELAY_MAJOR, 0), NULL,
                        relay_name);
    if (IS_ERR(dev)) {
        printk(KERN_WARNING "Can't make device (%d, %d)\n", RELAY_MAJOR, 0);
        class_destroy(relay_class);
        ret = PTR_ERR(dev);
        goto create_dev_fail;
    }
    printk("Relay driver make node success!\n");
    return 0;

create_dev_fail:
    device_destroy(relay_class, MKDEV(RELAY_MAJOR, 0));
create_class_fail:
    class_destroy(relay_class);
reg_dev_fail:
    unregister_chrdev(RELAY_MAJOR, relay_name);
    return ret;
}

static void __exit relay_exit(void) {
    device_destroy(relay_class, MKDEV(RELAY_MAJOR, 0));
    class_destroy(relay_class);
    printk("Relay driver remove node success!\n");

    unregister_chrdev(RELAY_MAJOR, relay_name);
    printk("Relay driver release success!\n");
}

module_init(relay_init);
module_exit(relay_exit);

MODULE_LICENSE("GPL");
MODULE_AUTHOR("Liao");
MODULE_DESCRIPTION("relay driver");
