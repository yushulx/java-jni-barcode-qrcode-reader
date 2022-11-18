#include "NativeBarcodeReader.h"
#include "DynamsoftBarcodeReader.h"

#ifdef __cplusplus
extern "C"
{
#endif

	/*
	* Class:     com_dynamsoft_barcode_NativeBarcodeReader
	* Method:    nativeInitLicense
	* Signature: (JLjava/lang/String;)I
	*/
	JNIEXPORT jint JNICALL Java_com_dynamsoft_barcode_NativeBarcodeReader_nativeInitLicense(JNIEnv *env, jobject, jlong hBarcode, jstring license)
	{
		const char *pszLicense = env->GetStringUTFChars(license, NULL);

		if (hBarcode)
		{
			char errorMsgBuffer[512];
			// Click https://www.dynamsoft.com/customer/license/trialLicense/?product=dbr to get a trial license.
			DBR_InitLicense(pszLicense, errorMsgBuffer, 512);
		}

		env->ReleaseStringUTFChars(license, pszLicense);
		return 0;
	}

	/*
	* Class:     com_dynamsoft_barcode_NativeBarcodeReader
	* Method:    nativeCreateInstance
	* Signature: ()J
	*/
	JNIEXPORT jlong JNICALL Java_com_dynamsoft_barcode_NativeBarcodeReader_nativeCreateInstance(JNIEnv *, jobject)
	{
		return (jlong)DBR_CreateInstance();
	}

	/*
	* Class:     com_dynamsoft_barcode_NativeBarcodeReader
	* Method:    nativeDestroyInstance
	* Signature: (J)V
	*/
	JNIEXPORT void JNICALL Java_com_dynamsoft_barcode_NativeBarcodeReader_nativeDestroyInstance(JNIEnv *, jobject, jlong hBarcode)
	{
		if (hBarcode)
		{
			DBR_DestroyInstance((void *)hBarcode);
		}
	}

	/*
	* Class:     com_dynamsoft_barcode_NativeBarcodeReader
	* Method:    nativeDecodeFile
	* Signature: (JLjava/lang/String;)V
	*/
	JNIEXPORT void JNICALL Java_com_dynamsoft_barcode_NativeBarcodeReader_nativeDecodeFile(JNIEnv *env, jobject, jlong ptr, jstring fileName)
	{
		if (ptr)
		{
			void *hBarcode = (void *)ptr;
			const char *pszFileName = env->GetStringUTFChars(fileName, NULL);

			DBR_DecodeFile(hBarcode, pszFileName, "");

			TextResultArray *paryResult = NULL;
			DBR_GetAllTextResults(hBarcode, &paryResult);

			int count = paryResult->resultsCount;
			for (int index = 0; index < paryResult->resultsCount; index++)
			{
				printf("Barcode %d:\n", index + 1);
				printf("    Type: %s\n", paryResult->results[index]->barcodeFormatString);
				printf("    Text: %s\n", paryResult->results[index]->barcodeText);
			}

			// Release memory
			DBR_FreeTextResults(&paryResult);

			env->ReleaseStringUTFChars(fileName, pszFileName);
		}
	}

	JNIEXPORT jstring JNICALL Java_com_dynamsoft_barcode_NativeBarcodeReader_nativeGetVersion(JNIEnv *env, jobject) 
	{
		const char *version = DBR_GetVersion();
		return env->NewStringUTF(version);
	}

#ifdef __cplusplus
}
#endif