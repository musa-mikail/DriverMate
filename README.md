# Driver Mate
A simple Android App that provides feedback to drivers on their safe Driving Behaviours.
The application which was written in Java use the accelerometer sensor in an Android device to track the rate of braking and acceleration per second and report a hard braking or harsh acceleration event if the acceleration in X, y or z direction is above +-20m/S2 within 1 seconds.
current features:
- Geolocation services integration via Google maps API.
- Location services listeners and event managers v3 for precision GPS tracking.
- User authentication and authorization using RBAC.
- Local data persistence using sqlite database management engine.
- Integration of latest text to speech (TTS) engine.

planned improvements in v2.0:
- interface with external camera to track facial contours and implement a machine learning model to track micro sleep.
- backend integration to a remote mongodb server for reporting and date persistence.
- migration to react native platform for cross platform integration in iPhone devices.