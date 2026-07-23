# Materia Reborn Update

This update significantly improves JEI integration, Materia Table crafting, administrative commands, and the overall Essence interface. It also introduces a new Liquid Essence recipe and resolves several UI and recipe-display issues.

## Added

### Commands

* Added `/materia getinfo <item>`.

  * Displays the required Materia Table level.
  * Shows the item's Essence values.
  * Shows its analysis requirement.

* Added `/materia essence get <player>`.

  * Displays the current Essence balance of the selected player.

* Expanded the Essence administration commands:

  * `/materia essence give <player> <amount>`
  * `/materia essence remove <player> <amount>`
  * `/materia essence set <player> <amount>`

  Essence can now be managed individually for any selected player.

* Added `/materia unlock <player> <item>`.

  * Permanently unlocks the selected item for a player.

* Added `/materia lock <player> <item>`.

  * Completely resets the selected item's unlock and analysis progress for a player.

## JEI Improvements

Materia Reborn's special recipes have been redesigned as clear, compact 2D instructions to make their requirements and interactions easier to understand.

### Essence Dust

The Essence Dust JEI recipe now displays:

* Both valid main-hand and off-hand item combinations.
* The required right-click interaction.
* The five-second crafting duration.
* The resulting Essence Dust.
* Support for crafting multiple items at once.

### Liquid Essence

Added a new JEI recipe for Liquid Essence:

* 3 Essence
* 2 Essence Crystals
* 1 Water Block

The recipe also explains:

* The four-second creation process.
* The limited lifetime of Liquid Essence.
* How it can be stabilized.
* The effects caused by prolonged exposure.

### Essence Condensation

* Corrected the Essence Condensation recipe to produce **6 Essence**.
* Improved the recipe layout with more compact spacing and cleaner alignment.

### Direct Craft

Added a **Direct Craft** button to JEI crafting recipes.

The button is only available while a Materia Table is open.

Direct Craft:

* Calculates the total Essence cost of all required ingredients.
* Purchases already unlocked ingredients using Essence.
* Places the ingredients into the correct Materia Table crafting slots.
* Displays missing item unlocks.
* Displays insufficient Materia Table levels.
* Displays insufficient Essence.

## Crafting Improvements

### Auto Refill

Added an **Auto Refill** toggle to the Materia Table crafting interface.

After a successful craft, Auto Refill:

* Purchases the ingredients required for another crafting operation.
* Uses the player's Essence balance.
* Places each ingredient back into its previous crafting-grid position.

### Essence Dust Crafting

* Essence Dust now uses a five-second, two-handed rubbing animation.
* Up to 64 Essence Dust can be crafted simultaneously.
* The crafted amount automatically depends on the available ingredients.

## Essence Interface

* The first two positions in the Essence item catalogue now display:

  * The most recently sold item.
  * The most recently purchased item.

* These quick-access entries reset when leaving the current game session.

* Items requiring a higher Materia Table level are now displayed correctly:

  * Inside the input slot.
  * While attached to the mouse cursor.
  * Inside the item information panel.

* The information panel now continues to display:

  * The item icon.
  * Essence values.
  * The required Materia Table level.

## Changed

* The Ritual Builder now requires only **3 Essence per Liquid Essence field**.
* Liquid Essence recipes now consistently use the updated requirement of 3 Essence.
* JEI cauldron recipes were reorganized into a more compact layout.
* Recipe results, ingredient quantities, and interaction descriptions are now centered more consistently.

## Fixed

* Decorative lines and background elements no longer render outside the boundaries of the Materia Table interface.
* The item information panel no longer appears empty for items requiring a higher Materia Table level.
* JEI recipe results and labels are now correctly aligned.
* Ingredient quantities in JEI instructions are now positioned correctly.
* Unnecessary spacing in the Liquid Essence and Essence Condensation instructions has been removed.
