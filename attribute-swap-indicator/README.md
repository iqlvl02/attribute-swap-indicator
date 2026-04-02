# Attribute Swap Indicator Mod
### Minecraft 1.21.1 | Fabric

Shows an animated HUD banner whenever an **attribute swap** is detected on your player — for example when one stat goes down and another goes up (like a trade/swap mechanic).

---

## ✨ Features

- **Animated popup banner** slides in from the top of the screen
- Shows **FROM** attribute (red) and **TO** attribute (green)
- Displays old and new attribute values
- Smooth **fade in / hold / fade out** animation
- Auto-detects attribute swaps each tick (no server mod needed)
- **`/swaptest`** command to preview the indicator any time
- **`/swaptest <from> <to>`** to preview with custom attribute names
- Client-side only — works on any server

---

## 🛠 Building

### Requirements
- Java 21+
- Gradle (wrapper included)

### Steps

```bash
# Clone / unzip the mod source
cd attribute-swap-indicator

# Build the mod JAR
./gradlew build

# Output JAR will be at:
# build/libs/attribute-swap-indicator-1.0.0.jar
```

> **Windows users:** use `gradlew.bat build` instead.

---

## 📦 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for **Minecraft 1.21.1**
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) (required)
3. Drop `attribute-swap-indicator-1.0.0.jar` into your `.minecraft/mods/` folder
4. Launch Minecraft!

---

## 🎮 Usage

### Automatic Detection
The mod tracks your player attributes every tick. When it detects that one attribute decreased and its swap-partner increased in the same tick window, it shows the indicator automatically.

**Tracked swap pairs (auto-detected):**
| Attribute A | Attribute B |
|---|---|
| Max Health | Armor |
| Movement Speed | Attack Speed |
| Attack Damage | Knockback Resistance |
| Armor | Armor Toughness |
| Luck | Attack Damage |

### Test Command
In-game, run:
```
/swaptest
```
This triggers a sample indicator (Attack Damage → Armor).

Custom test:
```
/swaptest Strength Agility
```

---

## 📁 File Structure

```
src/main/java/com/attributeswap/indicator/
├── client/
│   ├── AttributeSwapIndicatorClient.java   ← Main entrypoint, registers events
│   ├── SwapIndicatorRenderer.java          ← HUD drawing logic + animation
│   └── AttributeSwapManager.java           ← Tick-based attribute change detection
└── mixin/
    └── ClientPlayerInteractionManagerMixin.java  ← Mixin placeholder
```

---

## 🔧 Customization

Edit `SwapIndicatorRenderer.java` to tweak:
- `HOLD_TICKS` — how long the banner stays visible (default: 40 ticks = 2 sec)
- `FADE_IN_TICKS` / `FADE_OUT_TICKS` — animation speed
- Border color: change `0x44FF88` to any hex color
- Panel size / position

Edit `AttributeSwapManager.java` to add more swap pairs in the `SWAP_PAIRS` array.

---

## 📜 License
MIT — free to use, modify, and redistribute.
