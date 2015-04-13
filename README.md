# AndroidGroupMessenger

 group messenger that can send message to multiple AVDs and store them in a permanent key-value storage

1. This app contains a provider which is used to store all messages, but the abstraction it provided a general key-value table.
2. This app multicast every user-entered message to all app instances (including the one that is sending the message)
3. contains a testing script to test whether all the functionalities are working correctly or not.<br/>
4. This app also provides a faciltiy for reading/writing a message (key/value) to the app using Content Provider.
