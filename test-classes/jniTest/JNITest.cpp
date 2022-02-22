#include <iostream>
#include "JNITest.h"

using namespace std;

JNIEXPORT void JNICALL Java_JNITest_sayHello
  (JNIEnv* env, jobject thisObject) {
    std::cout << "Hello from C++ !!" << std::endl;
}

JNIEXPORT jobject JNICALL Java_JNITest_getData
  (JNIEnv* env, jobject) {
  
  jclass aClass = env->FindClass("A");
  jclass fClass = env->FindClass("F");
  
  jobject ret = env->AllocObject(aClass);
  jobject fObj = env->AllocObject(fClass);
  
  jfieldID fField = env->GetFieldID(aClass, "f", "F");
  
  env->SetObjectField(ret, fField, fObj);
  
  return ret;
}
