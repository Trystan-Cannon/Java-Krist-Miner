# Java-Krist-Miner
A krist currency miner written in Java.

This software allows users to "mine" the cross-Minecraft-server currency, Krist.

The program currently allows users to make use of more than core, if they have more cores available, that is. Currently, the
default is 1 core.

However, it is important to note that your CPU usage will grow greatly when using this program. Be careful. Monitor the
usage and make use of less cores if your usage gets too high.

I HOLD NO RESPONSIBILITY FOR WHAT YOU DO WITH THIS SOFTWARE. IF YOUR COMPUTER MELTS IN ITS CASE, IT'S YOUR FAULT, NOT MINE.

Monitor the temperature of your chip. Make sure that it is within a safe range. If you don't know the safe range, use Google. I'm serious. You can cause some harm to your hardware if you let it run too quickly without proper cooling.

As always, you're welcome to use this software as you please. Simply give credit where credit is due.

# User Instructions
Download the "Krist-Miner.jar" file from the /dist/ folder in the repository. In the same folder, create another folder called "lib." Place the "guava-18.0" jar file into this new folder.

Windows: Execute the Krist_Miner jar file (double click it in Windows).Enter your generated Krist ID. Set the desired sleep time. 0 for maximum speed and greatest CPU usage (WATCH OUT!). ???PROFIT???

Ubuntu (Linux): Open your terminal and navigate to the directory in which you placed the miner jar. Type: "java -jar Krist_Miner.jar" or whatever you named the jar. This will create the config.txt. You can edit it from then on and execute the jar by just double clicking it. If you have any issues, let me know; I don't use Linux.

# Setting the core limit
When you start the program, it reads from a file to see if you've set your own core limit. The program supports up to 6 and no less than 1.
The config file is located in the same directory as the miner jar. It is named "config.txt." To change the core limit, open this file and change the field 'coreLimit' from 1 to whatever number you want, after reading the comments, of course.

# Bugs
This is new software and one of the first programs I've written in Java using Threads and the like. So, there WILL BE BUGS.
Currently the bugs are as follows:
- When the krist server is unreachable or the connection times out, the program will stop. If you notice anything strange, restart the program.
- For a while, the program would not stop mining after "Stop Mining" was clicked. I think this problem is fixed. However, if you get this issue, restart the program.
