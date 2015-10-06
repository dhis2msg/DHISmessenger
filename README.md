# DHISmessenger
Android Messenger App for DHIS 2

I suggest that the next developer should use these librarys:

```
1. The library (asmack.jar) im using is depricated and not available as a maven dependency any more.
This should be used instead, but needs to update/change the XMPP connection code.
https://github.com/igniterealtime/Smack/wiki/Smack-4.1-Readme-and-Upgrade-Guide
```

```
2. Use Observables with reactiveX for REST calls, or other libraries to search the API and convert JSON to models (such as GSON).
https://github.com/ReactiveX/RxAndroid
```

```
3. Use butterknife more 
http://jakewharton.github.io/butterknife/
```

```
4. Create a better solution for storing XMPP info on device - instead of the solution in "XMPPSessionStorage"
```

Im sorry for the very badly formatted code and struckture. I did not asume that anyone would continue working on this app. Anyone continuing on this app should start formatting the code.

```
Google Play:
https://play.google.com/store/apps/details?id=org.dhis2.messaging
```