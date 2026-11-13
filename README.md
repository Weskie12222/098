# House Builder (Fabric, Minecraft 26.2) - v1.2.0

Place a builder block, face the direction you want the building to go, right-click it with an empty hand.
The block you place becomes the **front door / gate**.

| Block | Size | What you get |
|---|---|---|
| Wooden House Builder (Small) | 9x10, 1 floor | living room, kitchen, bedroom |
| Wooden House Builder (Medium) | 11x12, 2 floors | + dining room, hall, master bedroom, bathroom, stairs, porch |
| Wooden House Builder (Large) | 15x14, 2 floors | + study, 3 bedrooms, big living room with fireplace |
| Wooden House Builder (Villa) | 19x16, 2 floors | + library, lounge, swimming pool out front |
| Modern House Builder (S/M/L/Villa) | same sizes | same rooms, glass + concrete, flat roof, rooftop terrace on large/villa |
| Haunted Barracks Builder | 15x13 | blackstone fortress, skull stakes, cobwebs, cells, armory, soul fire |
| Castle Builder (Small) | 17x17 | walls, 4 towers, gate, small keep, well |
| Castle Builder (Medium) | 25x25 | + moat and drawbridge, mid towers, great hall keep, stable, forge |
| Castle Builder (Large) | 35x35 | + bigger keep with turrets, market stalls |
| Castle Builder (Grand) | 47x47 | + huge keep, dungeon cellar with cells |

Rooms are furnished: kitchens (furnace, smoker, sink, island), living rooms (fireplace, sofa, coffee table),
bedrooms (beds, bedside tables, wardrobe, rugs), bathrooms (toilet, sinks, tubs), dining rooms, studies.

All blocks are in the **Building Blocks** creative tab and craftable in survival (8 around 1):

- Wooden: 8 oak planks around stick / oak log / dark oak log / gold ingot (small / medium / large / villa)
- Modern: 8 white concrete around glass pane / glass / iron ingot / diamond
- Haunted barracks: 8 polished blackstone bricks around iron ingot
- Castle: 8 stone bricks around cobblestone / iron ingot / gold ingot / diamond

Leave plenty of clear space: the castles need up to 55x55 blocks of flat ground. Trees and hills in the way are removed.

## Build

Requires JDK 25. `./gradlew build` and use the jar in `build/libs/` that does not end in `-sources`.
Or push to GitHub and let the included workflow build it.

## Code map

- `HouseLayouts.java` - room layouts for the 4 house sizes (edit rooms/doors here)
- `ResidentialBuilder.java` - walls, roofs, stairs, porch, pool
- `Furniture.java` - what goes in each room type
- `CastleBuilder.java` - castles (sizes in the `CFG` table)
- `BarracksBuilder.java` - haunted barracks
- `Ctx.java` - local coordinate helper (u sideways, f forward, y up)
- `HouseStyle.java` / `ModBlocks.java` - the list of blocks
