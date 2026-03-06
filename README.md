Obfuscates death and kill messages only for entities with Invisibility II or higher. Both default to true but can be edited through gamerule commands:

- /gamerule hideinvismsgs:obfuscate_invis_deaths <True/False>
- /gamerule hideinvismsgs:obfuscate_invis_kills <True/False>

Also adds an admin-only command to send a packet class instance to yourself:

- /sendpacket <packetClassName>

The command looks up `net.minecraft.network.protocol.game.<packetClassName>`, tries to call a no-arg constructor via reflection, and sends the packet to the command executor.
