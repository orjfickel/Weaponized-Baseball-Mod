![Banner](https://i.imgur.com/ripZdL7.png)

## Description taken from curseforge page (https://www.curseforge.com/minecraft/mc-mods/weaponised-baseball-mod)

Introduces **baseballs** that bounce off blocks and mobs, and that can be hit with **baseball bats**!  
Also allows **throwing** some vanilla items like **slimeballs, bricks, fireballs, ingots**, etc.  
To cope with scattering your balls everywhere, you can craft a **baseball glove** to pick them up.  
Lastly, throwables are now **affected by** the **player’s velocity** at the moment of throwing, and while usually not noticeable in vanilla Minecraft, throwable item **sprites** are now properly **centered** as well.  

Release trailer: https://youtu.be/b708a8nTjWY

## Throwing mechanics

* Pressing a dedicated button while holding a bat (by default the back quote (`)) switches between tee-up-mode, in which you automatically throw balls up softly, making them easier to hit with a bat. Note that this only works by holding a bat in your main hand and a throwable in your off-hand
* Crouching causes you to not pick up balls and to throw them slower.
* Throwables are now affected by the player’s velocity at the moment of throwing.
* You can set a custom key as the throw key, so that you reduce the risk of accidentally throwing away your diamonds

## Tools

* Catcher’s gloves can be used to pick up throwables you’re looking at.
* You can get combo damage by hitting a ball with a bat and also by bouncing off mobs. This is cancelled when the ball hits a block.
* Stronger bats deal more knockback and launch baseballs further.
* Bats support most sword and bow enchantments.
* Bats are slower and weaker than swords, but deal more knockback.
* Dispensers will shoot baseballs and other throwables.

## Physics

* Throwables will bounce off blocks, entities, even each other, but have a chance of breaking when hitting a mob (or a cactus).
* Mobs receive knockback based on the velocity and mass of the throwables that hit them.
* Mobs will push light throwables out of the way, but will attempt to walk over heavy ones.
* Throwables can activate buttons and trigger blocks, break through glass, slide over ice, etc.
* The bug where item projectiles are slightly off-center compared to their bounding boxes is fixed. (https://bugs.mojang.com/browse/MC-158734). (Sorry for this realisation)
* With the commands "throwable bounciness x" or "throwable friction x" you can play around with setting the bounciness and friction to whatever you like

## Config

Some features can be toggled in the client and server configs (see also the configured mod):
* How many ticks a throwable can be idle before despawning. Default value is 1200 (2 min).
* Enable throwables dropping themselves after the idle time is over.
* Disable overriding the vanilla throwables behaviour to make them hittable with bats.
* Turn on lite mode which disables slow collisions and throwable on throwable collisions.
* Enable overriding the throwable item sprite renderer to fix the sprite centering bug. (Note that you cannot turn this off in 1.19 since configs are no longer loaded before the renderers are registered)
    
## What to know for survival

Baseballs in relation to arrows are slightly less accurate, do a little more damage when hit with an iron bat, and have slightly more range. 

The new throwables have a certain chance of breaking upon hitting a mob. For base/dirt/stone/cork/slime/fire-balls this chance is 5%.

While I am confident it should make for a fun survival experience, any feedback in regards to the balancing would be appreciated, as I am not an expert in this kind of thing.

### Crafting

Dirt, stone, and cork balls can be crafted by placing their respective materials in a circle in a crafting table.

To craft baseballs,one must first obtain oak wood (made from 4 logs), and place it in a 4x4 grid to craft cork. Surrounding cork with a layer of slimeballs, and then a layer of wool crafts baseball cores. This in turn can be combined with string and leather to finally yield some powerful baseballs.

Catcher’s gloves can be crafted simply with a bunch of leather, and one piece of string to bind the fingers.

<details>
  <summary>Crafting screenshots</summary>
  
<img src="https://i.imgur.com/RC0Sz6A.png" alt="crafting cork" width="400"/>
<img src="https://i.imgur.com/u8YVItB.png" alt="crafting baseball core" width="400"/>
<img src="https://i.imgur.com/QRhImgT.png" alt="crafting baseballs" width="400"/>
<img src="https://i.imgur.com/3myFoX7.png" alt="crafting catcher's glove" width="400"/>

</details>

## Full list of supported throwables:
<details>
  <summary>Expand</summary>
  
* Any eggs
* Any slimeballs
* Fire charges
* Baseballs
* Dirtballs
* Stoneballs
* Corkballs
* Any coals
* Any gems
* Any nuggets
* Any ingots
* Any of the vanilla throwables like snowballs and eggs
  
</details>
 

## Feel free to use in any modpacks, please link to the CurseForge download page and do not rehost the files.

## Possible (some day) future features

* Give baseballs a 50% chance of ricocheting towards another living being after hitting one, and then a 50% chance of that living entity being the player. So you need to be careful just throwing balls around, since one out of four times you could get a taste of your own medicine, unless you manage to dodge or land a combo hit of course.
* Support throwing more different vanilla items with unique behaviors (for example ink sacs, or maybe even blocks that get placed when they hit something).
* Add more new throwables (like a bomb for example).
* Add an Illager baseball player.
* Handle hitting block edges properly, rather than treating them like flat sides
* Port to fabric.
