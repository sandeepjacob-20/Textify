ABOUT TEXTIFY

Textify is a text recognition and transcription application developed using Huawei HMS Kit. It can save the recognized text as a .docx file so that 
it can be transferedm edited or stored easily, making the process of creating text documents from images easy. It can also transcribe speech made in english.

The second update has introduced the feature of translation. Both the recognized text as well as the transcribed text can be translated into 6 different languages. The currently
supportted languages are English, Hindi, French, Chinese, Russian and Spanish. 

Currently that app can be used only if the android device has HMS core installed. On Non-Huawei devices it can be installed via the Huawei AppGallery. The app is also available on the
Huawei AppGallery (Except in parts of China and India due to certain government restriction). 

Link for the App : https://appgallery.huawei.com/app/C106477127

DEVELOPMENT

Textify uses the ML kit provided by HMS Core to recognize text from images. It uses an offline mdoel to detect the text. The app captures an image or loads an image from the 
device and sends the image as an input to the model which returns the detected text as a string. This is then displayed in a TextView. This text can then be translated into 
6 different languages. The translation is also done using the HMS ML Kit. A save button is provided for the user to save the text as a doc file. The model is downloaded when the app
is used for the first time and then it can be used as and when required even if the device is not connected to the internet. Translation can also be done offline.

Transcription also uses the HMS ML Kit. The transcription service uses real-time transcription and so it requires an active internet connection. The result of the transcription
is also a string which is displayed and saved using the same TextView. 

In addition to improve sscurity, the app also features a login page that uses the silent signin service provided by HMS Account Kit. On opening the user is greeted with a custom
greeting as per the time of the day.

For more information check out these development resources,
HMS core : https://consumer.huawei.com/in/support/content/en-gb15841718/
HMS ML Kit : https://developer.huawei.com/consumer/en/hms/huawei-mlkit/
Text Recognition using HMS ML kit : https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/text-recognition-0000001050040053
Transcription using HMS ML kit : https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/real-time-transcription-0000001054964200
HMS Account Kit : https://developer.huawei.com/consumer/en/hms/huawei-accountkit/
