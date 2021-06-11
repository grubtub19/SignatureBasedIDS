# SignatureBasedIDS
A command-line signature-based IDS capable of parsing, de-fragmenting, and filtering packets.

![Imgur](https://i.imgur.com/Uh56JhI.png)

## Description

Parses and creates a queryable Packet object which contains any of the following encapsulated protocols:

Ethernet, IPv4, ARP, ICMP, TCP, UDP

Particularly, it is capable of de-fragmenting IPv4 using either the Linux or Windows implementation of fragment overlapping.
Using command line options, it will filter packets from a range of addresses and port, and it will also filter suspicious packets.
It even checks for various exploits that may crash the IDS (e.g., Ping-of-Death and breaking up the TCP header).

Finally, it implements a system to log any packets based on a rule system. Examples are as follows:

alert tcp 192.168.1.0/24 any -> 192.168.1.0/24 111 (content: "|00 01 86 a5|"; msg: "external mountd access";)

alert tcp any any -> 192.168.1.0/16 any (flags: S; msg: "SYN packet";)

alert tcp 129.244.0.0/16 any <> 192.168.1.0/24 23

alert tcp any any -> 192.168.1.0/24 any (msg: "Winnuke attack: urgent pointer"; flags: U+;)

alert icmp any any -> 192.168.1.0/24 any (msg: "DDOS ICMP: fragmented icmp"; fragbits: M;)

## Implementation Description

Packet Parsing:
https://docs.google.com/document/d/1Yf3t2p-VrsLrIwWzd4jsQG_y9aaS9zX9yyjYitMETqM/edit?usp=sharing

De-fragmenting:
https://docs.google.com/document/d/1y4goSCGFFWM6K8cJTGrElf6jBWo0dS2EA1w_BKeLbdU/edit?usp=sharing

Signature-based IDS:
https://docs.google.com/document/d/1hbTWB7EvJmJtwNozZTzeWIs1Ul_ms69fBDkrW4gfZyI/edit?usp=sharing

