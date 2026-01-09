📱 WhatsApp Deleted Message Listener
-----------
Android application that listens to WhatsApp notifications and heuristically detects messages deleted by the sender (individual and group chats).

📌 Description
-----------
WhatsApp Deleted Message Listener is an Android app that monitors WhatsApp notifications and applies a heuristic algorithm to identify messages that were deleted by the remote sender (“Delete for everyone”).

The app is designed as a first-version experimental solution, favoring high detection probability even if some extra messages are recorded.

✨ Features
-----------
🔔 Listens to WhatsApp notifications using NotificationListenerService

🔔 Add notifier to allow continues APP running and notify about new deleted messages

🧠 Heuristic algorithm for deleted message detection

👤 Supports one-to-one chats

👥 Supports group chats

🗃️ Local message storage

🔍 Marks messages deleted by sender

🔐 Fully offline & local processing

🧠 Algorithm Overview
-----------
Intercepts WhatsApp notifications

Stores incoming messages with metadata:

Sender

Chat / Group ID

Timestamp

Content

Detects deletion notifications

Matches deletion events against stored messages using:

Temporal proximity

Sender consistency

Chat context

Marks the most probable message as deleted

⚠️ This is a heuristic approach — accuracy is probabilistic, not guaranteed.

It uses public Android APIs only and operates strictly within Android’s permission system.
Use responsibly and in compliance with local laws and platform policies.

⚠️ Disclaimer
---
The APP heuristic require deletion notification as new message income.
Not all whatsApp deletion do it. To simplified the logic, all income messages been store.
That way the user may have false positive deletion messages, but wouldn't miss true one.
The user get UI to clear store message to avoid irrelevant memory allocation. 

⚠️ Known Limitations
-----------
Heuristic-based (may produce false positives)

Depends on WhatsApp notification format

Accuracy may vary by device & Android version

Cannot recover messages deleted before notification arrival

Media messages may have limited support

🚀 Roadmap
-----------
Improve heuristic precision

Reduce false positives

Media deletion detection

Better group message correlation

Encrypted local storage

UI for browsing deleted messages


🔐 Permissions
----
The app requires the following permission:

Notification Access

Used to read WhatsApp notifications only

No root required

No private APIs used

▶️ Getting Started
--
1. Clone the Repository
git clone https://github.com/eyalra5/RestoreDelMessages.git
2. Build the project
3. Install using ADB or
4. Build APK, copy using BlueTooth and install (required manual confirmation)


APP icon
-----
<img width="205" height="196" alt="image" src="https://github.com/user-attachments/assets/cd89032e-ae7d-4ac4-9fee-fe5b85ac44ed" />


screen shot example
------
<img width="383" height="454" alt="image" src="https://github.com/user-attachments/assets/ebc47ad1-673f-48c9-a13d-7bb6f78f349b" />



