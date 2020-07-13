/*
 * 7-Segment Device Driver
 *  Hanback Electronics Co.,ltd
 * File : segment.c
 * Date : April,2009
 */ 

// ����� ������� ����
#include <linux/init.h>
#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/fs.h>
#include <linux/errno.h>
#include <linux/types.h>
#include <asm/fcntl.h>
#include <linux/ioport.h>
#include <linux/delay.h>

#include <asm/ioctl.h>
#include <asm/uaccess.h>
#include <asm/io.h>

#define DRIVER_AUTHOR		"hanback"		// ����� ������
#define DRIVER_DESC		"7-Segment program"	// ��⿡ ���� ����

#define	SEGMENT_MAJOR		242			// ����̽� �ֹ�ȣ
#define	SEGMENT_NAME		"SEGMENT"		// ����̽� �̸�
#define SEGMENT_MODULE_VERSION	"SEGMENT PORT V0.1"	// ����̽� ����

#define SEGMENT_ADDRESS_GRID	0x88000030	// 7-seg�� Digit �� �����ϱ� ���� �������� 
#define SEGMENT_ADDRESS_DATA	0x88000032	// 7-seg�� ���÷��� �ϱ� ���� ��������
#define SEGMENT_ADDRESS_1	0x88000034	// 7-seg�� ���÷��� �ϱ� ���� ��������
#define SEGMENT_ADDRESS_RANGE	0x1000		// I/O ������ ũ��
#define MODE_0_TIMER_FORM	0x0			// ��� ���� ���� ����
#define MODE_1_CLOCK_FORM	0x1

//Global variable
static unsigned int segment_usage = 0;
static unsigned long *segment_data;
static unsigned long *segment_grid;
static int mode_select = 0x0;

// define functions...
// ���� ���α׷����� ����̽��� ó�� ����ϴ� ��츦 ó���ϴ� �Լ�
int segment_open (struct inode *inode, struct file *filp)
{
	// ����̽��� ���� �ִ��� Ȯ��.
	if(segment_usage != 0) return -EBUSY;
	
	// GRID�� DATA�� ���� �ּ� ����
	segment_grid =  ioremap(SEGMENT_ADDRESS_GRID, SEGMENT_ADDRESS_RANGE);
	segment_data =  ioremap(SEGMENT_ADDRESS_DATA, SEGMENT_ADDRESS_RANGE);
	
	// ����� �� �ִ� I/O �������� Ȯ��
	if(!check_mem_region((unsigned long)segment_data,SEGMENT_ADDRESS_RANGE) && !check_mem_region((unsigned long)segment_grid, SEGMENT_ADDRESS_RANGE))
	{
		// I/O �޸� ������ ���
		request_region((unsigned long)segment_grid, SEGMENT_ADDRESS_RANGE, SEGMENT_NAME);
		request_region((unsigned long)segment_data, SEGMENT_ADDRESS_RANGE, SEGMENT_NAME);
	}
	else printk("driver : unable to register this!\n");

	segment_usage = 1;	
	return 0; 
}

// ���� ���α׷����� ����̽��� ���̻� ������� �ʾƼ� �ݱ⸦ �����ϴ� �Լ�
int segment_release (struct inode *inode, struct file *filp)
{
	// ���ε� �����ּҸ� ����
	iounmap(segment_grid);
	iounmap(segment_data);

	// ��ϵ� I/O �޸� ������ ����
	release_region((unsigned long)segment_data, SEGMENT_ADDRESS_RANGE);
	release_region((unsigned long)segment_grid, SEGMENT_ADDRESS_RANGE);

	segment_usage = 0;
	return 0;
}

// x�� ���� LED�� �ڵ�� ��ȯ�Ͽ� ��ȯ
unsigned short Getsegmentcode (short x)
{
	unsigned short code;
	switch (x) {
		case 0x30 : code = 0xfc; break;
		case 0x31 : code = 0x60; break;
		case 0x32 : code = 0xda; break;
		case 0x33 : code = 0xf2; break;
		case 0x34 : code = 0x66; break;
		case 0x35 : code = 0xb6; break;
		case 0x36 : code = 0xbe; break;
		case 0x37 : code = 0xe4; break;
		case 0x38 : code = 0xfe; break;
		case 0x39 : code = 0xf6; break;
		
		case 0x61 : code = 0x80; break;
		case 0x62 : code = 0x0c; break;
		case 0x63 : code = 0x10; break;
		case 0x64 : code = 0x02; break;						
		case 0x65 : code = 0x90; break;
		case 0x66 : code = 0x9c; break;
		case 0x68 : code = 0xf0; break;				
		default : code = 0; break;
	}
	return code;
}						

// ����̽� ����̹��� ���⸦ �����ϴ� �Լ�
ssize_t segment_write(struct file *inode, const char *gdata, size_t length, loff_t *off_what)
{
    unsigned char data[6];
    unsigned char digit[6]={0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
    unsigned int i,ret;

    // ����� �޸� gdata�� Ŀ�� �޸� num���� n��ŭ ���� 
    ret=copy_from_user(data,gdata,6);
	// print
	for(i=0;i<6;i++) {
	    *segment_grid = digit[i];
	    *segment_data = Getsegmentcode(data[i]);
	    mdelay(1);	
	}

    *segment_grid = ~digit[0];
    *segment_data = 0;

    return length;
}

static int segment_ioctl(struct inode *inode, struct file *flip, unsigned int cmd, unsigned long arg)
{
    switch(cmd) {
	case MODE_0_TIMER_FORM:
	    mode_select=0x00;
	    break;
	case MODE_1_CLOCK_FORM:
	    mode_select=0x01;
	    break;
	default:
	    return -EINVAL;
    }

    return 0;
}
							    

// ���� ���۷��̼� ����ü
// ������ ���� open()�� ����Ѵ�. open()�� �ý��� ���� ȣ���Ͽ� Ŀ�� ���η� ����.
// �ش� �ý��� �ݰ� ���õ� ���� ������ ����ü ������ open�� �ش��ϴ� �ʵ尡 ����̹� ������
// segment_open()���� ���ǵǾ� �����Ƿ� segment_open()�� ȣ��ȴ�.
// write�� release�� ���������� �����Ѵ�. ���� ��ϵ��� ���� ���ۿ� ���ؼ��� Ŀ�ο��� ������
// ���� default ������ �ϵ��� �Ǿ� �ִ�.
struct file_operations segment_fops = 
{
	.owner		= THIS_MODULE,
	.open		= segment_open,
	.write		= segment_write,
	.release	= segment_release,
	.ioctl		= segment_ioctl,
};

// ����� Ŀ�� ���η� ����
// ��� ���α׷��� �ٽ����� ������ Ŀ�� ���η� ���� ���񽺸� �����޴� ���̹Ƿ�
// Ŀ�� ���η� ���� init()�� ���� �����Ѵ�.
// ���� ���α׷��� �ҽ� ���ο��� ���ǵ��� ���� ���� �Լ��� ����Ѵ�. �װ��� �ܺ�
// ���̺귯���� ������ �������� ��ũ�Ǿ� ���Ǳ� �����̴�. ��� ���α׷��� Ŀ��
// �����ϰ��� ��ũ�Ǳ� ������ Ŀ�ο��� �����ϰ� ����ϴ� �Լ����� ����� �� �ִ�.
int segment_init(void)
{
	int result;

	// ���� ����̽� ����̹��� ����Ѵ�.
	result = register_chrdev(SEGMENT_MAJOR, SEGMENT_NAME, &segment_fops);
	if (result < 0) {
		printk(KERN_WARNING"Can't get any major\n");
		return result;
	}

	// major ��ȣ�� ����Ѵ�.
	printk(KERN_INFO"Init Module, 7-Segment Major Number : %d\n", SEGMENT_MAJOR);
	return 0;
}

// ����� Ŀ�ο��� ����
void segment_exit(void)
{
	// ���� ����̽� ����̹��� �����Ѵ�.
	unregister_chrdev(SEGMENT_MAJOR,SEGMENT_NAME);

	printk("driver: %s DRIVER EXIT\n", SEGMENT_NAME);
}

module_init(segment_init);	// ��� ���� �� ȣ��Ǵ� �Լ�
module_exit(segment_exit);	// ��� ���� �� ȣ��Ǵ� �Լ�

MODULE_AUTHOR(DRIVER_AUTHOR);	// ����� ������
MODULE_DESCRIPTION(DRIVER_DESC);// ��⿡ ���� ����
MODULE_LICENSE("Dual BSD/GPL");	// ����� ���̼��� ���
