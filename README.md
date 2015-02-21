# Java-Krist-Miner
A krist currency miner written in Java.

This software allows users to "mine" the cross-Minecraft-server currency, Krist.

The program currently allows users to make use of more than core, if they have more cores available, that is. Currently, only
3/2 of the total cores on one's machine may be used to prevent radically increased CPU usage.

However, it is important to note that your CPU usage will grow greatly when using this program. Be careful. Monitor the
usage and make use of less cores if your usage gets too high.

I HOLD NO RESPONSIBILITY FOR WHAT YOU DO WITH THIS SOFTWARE. IF YOUR COMPUTER MELTS IN ITS CASE, IT'S YOUR FAULT, NOT MINE.

As always, you're welcome to use this software as you please. Simply give credit where credit is due.

# User Instructions
Download the "Krist-Miner.jar" file from the /dist/ folder in the repository. Execute the file (double click it in Windows).
Enter your generated Krist ID. Set the desired sleep time. 0 for maximum speed and greatest CPU usage (WATCH OUT!). ???PROFIT???

# A special warning regarding the "Sleep time (ms)" field
This is the amount of time each mining thread will wait between hashes in milliseconds. If you set this to zero, you will
get the maximum speed possible. However, this will also crank up your CPU usage. Therefore, pick a sensible sleep timer if
you want to use other programs alongside this one. However, picking something like a value of 5 may severely hinder your ability
to mine blocks in competition with other miners. Be smart about this.

# Bugs
This is new software and one of the first programs I've written in Java using Threads and the like. So, there WILL BE BUGS.
Currently the bugs are as follows:
- When the krist server is unreachable or the connection times out, the program will stop. If you notice anything strange, restart the program.
- For a while, the program would not stop mining after "Stop Mining" was clicked. I think this problem is fixed. However, if you get this issue, restart the program.
