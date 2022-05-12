# SmartSafe

SmartSafe is a personal security system that gives the user agency in a emergent situation such as an attack. Once activated it pairs with nearby/ zoned speakers dedicated to the system and sounds an alarm (in the future, will also set off flashing lights) to serve as a deterrent to the would be attacker. We  believe that this gives the victim the opprotunity to get away or will scare the offender off completely. 

Current capabilites of the app:
We were working with two iHome Speakers that had both had the same device name but different MAC addresses. The code is tailored to find these speakers in particular. In reality, there would be a list of speakers dedicated to the system so we only pair with pre-designated ones.
The application will enter discovery mode once the alarm button is pressed. 

It will look for the pre-designated speakers. The speakers must be in pairing mode in order to be found. Once both speakers are located, the speaker with the strongest signal --> highest RSSI value, is then sent a pairing request. Right now the user must accept the request. (If it is their first time using the app, they will also need to accept the location permissions as well). These permission issues can/will be addressed in future versions of the application, because they cause an unacceptable delay in triggering the system.  

The disarm button currently turns off discovery mode and System(0) exits the app 

All activites happen in the MainActivity Class of the application but can be altered for future use



Specifications:
Need Android Studio to run the application
Need an acutal Android phone to debug and test the Bluetooth functionality (not just an emulator)
Can pair phone and Andriod Studio using USB debugging or WIFI

App looks for two iHome Speakers with certain names. Once found, min RSSI is found
The app has 3 different fragments; Home, History, Profile. They are inoperative but can be turned back on
