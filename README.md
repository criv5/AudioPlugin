roadmap

main:
  onenable: register stuff, get players and add their current region to map and spawn their packet entity, add player and data object to hashmap, syncing runnables increment++;
  ondisable: unregister stuff, get players, remove current regon and despawn packet entity. remove player and data object from hashmap
events: scan movement, teleport, respawn, and other movement based events for teleporting packet entity as needed
transition: if bool transitioning = true, do not play the song on fade out maybe make an enum for FADEOUT, SWITCH, FADEIN, NONE
methods: getTrackNumber(String track) {} based on ints stored in config for maximum track, packet methods go here too
commands: region sound mappings: /rsm list, /rsm add, /rsm remove
