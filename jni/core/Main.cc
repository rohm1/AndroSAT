/*****************************************************************************************[Main.cc]
@author rohm1
@create 10.2012
**************************************************************************************************/

#include <jni.h>
#include "core/Launch.h"

extern "C" {
    JNIEXPORT jstring JNICALL Java_org_rohm1_androsat_AndroSAT_minisatJNI( JNIEnv* env, jobject thiz, jstring jin, jint debug);
};

JNIEXPORT jstring JNICALL Java_org_rohm1_androsat_AndroSAT_minisatJNI( JNIEnv* env, jobject thiz, jstring jin, jint debug)
{
    return env->NewStringUTF(
		minisat( env->GetStringUTFChars(jin, 0), debug != 0 )
		);
}
