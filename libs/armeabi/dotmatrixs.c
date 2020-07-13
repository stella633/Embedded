#include <string.h>
#include <jni.h>
#include <termios.h>
#include <sys/mman.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <android/log.h>

jint
Java_ac_kr_kgu_esproject_ArrayAdderActivity_DotMatrixPrint(
		JNIEnv* env, jobject thiz, jstring data)
{
	jboolean iscopy;
	char* buf; 
	int dev,ret, len;
	char str[100];

	// 입력 String 읽어오는 함수
	buf = (*env)->GetStringUTFChars(env, data, &iscopy);
	len = (*env)->GetStringLength(env, data);
	
	dev = open("/dev/dotmatrix", O_RDWR | O_SYNC);

	if(dev != -1) {
		ret = write(dev, buf, len);
		close(dev);
	} else {
		// 디바이스를 열지 못하면 로그를 출력하고 프로그램 종료
		__android_log_print(ANDROID_LOG_ERROR, "DotMatrixActivity", "Device Open ERROR!\n");
		exit(1);
	}
	return 0;
}
