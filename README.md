Obfuscates death and kill messages only for entities with Invisibility II or higher. Both default to true but can be edited through gamerule commands:

- /gamerule hideinvismsgs:obfuscate_invis_deaths <True/False>
- /gamerule hideinvismsgs:obfuscate_invis_kills <True/False>

Also adds an admin-only packet command:

- /sendpacket <className> <id> <value>

The command resolves `net.minecraft.network.protocol.game.<className>` and tries to instantiate either:
- `ClientboundGameEventPacket(Type, float)` (special-case for game events), or
- `(int, float)`, and then fallback to a no-arg constructor.

The created packet is sent to the command executor.
