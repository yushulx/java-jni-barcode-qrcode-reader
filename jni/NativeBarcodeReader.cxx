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
			DBR_InitLicense((void *)hBarcode, pszLicense);
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

			STextResultArray *paryResult = NULL;
			DBR_GetAllTextResults(hBarcode, &paryResult);

			int count = paryResult->nResultsCount;
			int i = 0;
			for (; i < count; i++)
			{
				printf("Index: %d, Type: %s, Value: %s\n", i, paryResult->ppResults[i]->pszBarcodeFormatString, paryResult->ppResults[i]->pszBarcodeText); // Add results to list
			}

			// Release memory
			DBR_FreeTextResults(&paryResult);

			env->ReleaseStringUTFChars(fileName, pszFileName);
		}
	}

#ifdef __cplusplus
}
#endif