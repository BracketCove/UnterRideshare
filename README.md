## Project Overview

This project is a functioning ride sharing application which I have built during the month of February. 


### Features
* Messaging between drivers and passengers, powered by Stream Chat SDK
* Google Maps with Directions API integration
* Google Places for autocomplete destination searching
* Coroutines and Flows for concurrency
* Navigation, DI, and Scoping for ViewModels via Simple-Stack
* FirebaseAuth with Stream cloud integrations for User creation and session management
* Stream Chat SDK for User and Ride data management
* Stream Chat SDK for passenger to driver messages
* Interoperability between Compose and XML in the same project
* Basic user profile management
* Reactive Ui based on cross-client server driven updates

### Architecture
My current approach to MVVM, where each distinct screen as a specific ViewModel that contains data
necessary to draw that screen appropriately. This is in contrast with a different variation of
MVVM where ViewModels just expose raw data and no details of how to present it. This variation
reduces the complexity of the Views, and works quite well as a general purpose architecture on
Android. 

## Resources
[Google Cloud Setup](https://cloud.google.com/run/docs/setup)

[Stream Android Docs](https://getstream.io/chat/sdk/android/)

[Firebase Setup](https://firebase.google.com/docs/android/setup)

[Simple-Stack](https://github.com/Zhuinden/simple-stack)

[My YT Channel](https://www.youtube.com/channel/UCSwuCetC3YlO1Y7bqVW5GHg)

[Follow me on twitter](https://twitter.com/wiseAss301)



## Acknowledgements
Cyber Shark - Font choice

Josh (CMYUI) - BE consulting

Gabor Varadi (Zhuinden) - Navigation, DI, and Services with Simple-Stack

Jaewoong Eum (Skydoves) - Stream Chat SDK and Flow integration