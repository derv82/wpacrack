wpacrack

Written in Java.

Open-source distributed Wifi-Protected Access (WPA) cracker.

--------------------

SYNOPSIS

This project is a proof of concept: that open-source distributed computing can be used to crack WPA-encrypted passwords using sequential password lists.

The project functions as a proof-of-concept project is intended to function.  The system still requires an actual GUI/view for both the Server and Client programs.

--------------------

WHY?

The WPA encryption for wireless networks (now the most-secure method of authenticating with wireless networks) can be broken by brute-force cracking.  Cracking the WPA hash requires "guessing" the correct password and calculating the hash.  Calculating the hash for a WPA password takes computing power; most modern CPU's can attempt to crack at a rate of ~500 tries/second. For a single machine to try every possible 8-character numeric password, it would take roughly 2 days.

By distributing the workload across many machines, the time required to try the same passwords drops as expected.

There is an issue with distributing pre-computed wordlists to other machines.  The issue is that the wordlists have to be sent over the wire, which costs bandwidth and memory.  This is inefficient.  By using sequential passwords, the server only needs to send *a range* of passwords (i.e. 00000000-00010530).  In this way, adding new machines and keeping track of which passwords are being tried is much simpler.

--------------------

DETAILS

The "server" program for this project was not designed to be run by a high-end, dedicated server with a fast connection.  It was designed to be run by any machine with JVM and decent network speed.

Most of the bandwidth is handled by the "server" uploading necessary files and information to a webserver via FTP. The clients (bots) then check with the web server for new files/information every so often.

In this way, the "server" does not need to maintain constant connections with the clients.

--------------------

(c) 2011 derv merkler