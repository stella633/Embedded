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
		// 디바이스를 열지 못하면 로그를 출력하고 프로그램 종료
		__android_log_print(ANDROID_LOG_ERROR, "SegmentActivity", "Device Open ERROR!\n");

		exit(1);
	}
	return data; 
}


