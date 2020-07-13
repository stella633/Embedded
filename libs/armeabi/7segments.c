#include <string.h>
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <termios.h>
#include <sys/mman.h>
#include <android/log.h>

jstring
Java_ac_kr_kgu_esproject_ArrayAdderActivity_SegmentPrint (JNIEnv* env,
													jobject thiz, jstring data )
{	
	int dev, ret ;
	dev = open("/dev/segment",O_RDWR | O_SYNC);
	
	const char *temp = (*env)->GetStringUTFChars(env,data, 0);
	if(dev != -1) {
		ret = write(dev,temp,6);
		close(dev);
	} else {
		// ����̽��� ���� ���ϸ� �α׸� ����ϰ� ���α׷� ����
		__android_log_print(ANDROID_LOG_ERROR, "SegmentActivity", "Device Open ERROR!\n");

		exit(1);
	}
	return data; 
}


