![Banner](https://i.imgur.com/ripZdL7.png)

## Description taken from curseforge page (https://www.curseforge.com/minecraft/mc-mods/weaponised-baseball-mod)

Introduces **baseballs** that bounce off blocks and mobs, and that can be hit with **baseball bats**!  
Also allows **throwing** some vanilla items like **slimeballs, bricks, fireballs, ingots**, etc.  
To cope with scattering your balls everywhere, you can craft a **baseball glove** to pick them up.  
Lastly, throwables are now **affected by** the **player’s velocity** at the moment of throwing, and while usually not noticeable in vanilla Minecraft, throwable item **sprites** are now properly **centered** as well.  

Release trailer: https://youtu.be/b708a8nTjWY

## Throwing mechanics

* Pressing a dedicated button (by default the back quote (\`)) switches between tee-up-mode, in which you automatically throw balls up softly, making them easier to hit with a bat.
* Crouching causes you to not pick up balls and to throw them slower.
* Throwables are now affected by the player’s velocity at the moment of throwing.

## Tools

* Catcher’s gloves can be used to pick up throwables you’re looking at.
* You can get combo damage by hitting a ball with a bat and also by bouncing off mobs. This is cancelled when the ball hits a block.
* Stronger bats deal more knockback and launch baseballs further.
* Bats support most sword and bow enchantments.
* Bats are slower and weaker than swords, but deal more knockback.

## Physics

* Throwables will bounce off blocks, entities, even each other, but have a chance of breaking when hitting a mob (or a cactus).
* Mobs receive knockback based on the velocity and mass of the throwables that hit them.
* Mobs will push light throwables out of the way, but will attempt to walk over heavy ones.
* Throwables can activate buttons and trigger blocks, break through glass, slide over ice, etc.
* The bug where item projectiles are slightly off-center compared to their bounding boxes is fixed. (https://bugs.mojang.com/browse/MC-158734). (Sorry for this realisation)
* Some features can be toggled in the client and server configs (mostly for compatibility)

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
  
* Turtle eggs
* Slimeballs
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
 

## Updates to 1.18 and 1.19 coming as soon as possible

## Possible (some day) future features

Give baseballs a 50% chance of ricocheting towards another living being after hitting one, and then a 50% chance of that living entity being the player. So you need to be careful just throwing balls around, since one out of four times you could get a taste of your own medicine, unless you manage to dodge or land a combo hit of course.

Support throwing more different vanilla items with unique behaviors (ink sacs for example).

Add more new throwables (like a bomb for example).

Port to fabric.

If you’d like to thank me or incentivize me to do more modding, I’d like to shamelessly suggest considering pressing the little donate button in the top right :)
