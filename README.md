# 🌍 KRUD World
### ✨ ម៉ាស៊ីនបង្កើតពិភពលោក Minecraft ដែលមានប្រសិទ្ធភាពខ្ពស់ ✨

[![License](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Spigot-green.svg)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)

**KRUD World** គឺជាម៉ាស៊ីនបង្កើតពិភពលោកដែលមានមុខងារសម្បូរបែប និងខ្លាំងក្លាសម្រាប់ម៉ាស៊ីនមេ (Server) Minecraft ដែលត្រូវបានរចនាឡើងយ៉ាងពិសេសសម្រាប់ **KRUD SMP**។ ដោយផ្អែកលើម៉ាស៊ីន **Iris World Generator** ដ៏រឹងមាំ វាផ្តល់នូវប្រសិទ្ធភាពដែលមិនអាចប្រៀបផ្ទឹមបាន និងសោភ័ណភាពដ៏អស្ចារ្យសម្រាប់ផ្ទៃដីនៃម៉ាស៊ីនមេរបស់អ្នក។

---

## 🚀 លក្ខណៈពិសេសសំខាន់ៗ

*   🏙️ **វិមាត្រតាមបំណង (Custom Dimensions):** រុករក **ព្រះរាជាណាចក្រខ្មែរ (Khmer Kingdom)** ដ៏រុងរឿង ដែលជាវិមាត្រតែមួយគត់ដែលបានបង្កើតឡើងសម្រាប់ KRUD SMP។
*   ⚡ **អាទិភាពលើប្រសិទ្ធភាព:** បង្កើតឡើងនៅលើម៉ាស៊ីន Iris ដែលផ្តល់នូវល្បឿនបង្កើតពិភពលោកលឿនបំផុតក្នុងឧស្សាហកម្មនេះ។
*   📦 **គាំទ្រច្រើនជំនាន់:** អាចប្រើជាមួយ Minecraft កំណែ **1.20.1** រហូតដល់ **1.21.1**។
*   🔔 **ការជូនដំណឹងតាមរចនាប័ទ្ម DonutSMP:** ប្រព័ន្ធផ្ញើសារដែលមានស្លាកសញ្ញា (Branded) សម្រាប់អារម្មណ៍ម៉ាស៊ីនមេទំនើប។
*   🎨 **ការកំណត់តាមបំណងពេញលេញ:** ការកំណត់រចនាសម្ព័ន្ធ KRUD និងពាក្យបញ្ជាពិសេស។
*   🌏 **ការគាំទ្រជាសកល:** គាំទ្រតំបន់ម៉ោង `Asia/Phnom_Penh` ដោយផ្ទាល់។

---

## 🗺️ វិមាត្រដែលរួមបញ្ចូល

រុករកពិភពលោកប្លែកៗជាច្រើនដែលមានស្រាប់៖
*   🏞️ **Overworld**
*   ⚪ **Vanilla**
*   📏 **Flat**
*   🔴 **Redstone**
*   ☄️ **Mars**
*   ✨ **New Horizons**
*   🌑 **The End**

---

## 🎮 ពាក្យបញ្ជា និងការអនុញ្ញាត (Commands & Permissions)

### ពាក្យបញ្ជាមូលដ្ឋាន៖ `/kworld` (ឈ្មោះហៅក្រៅ៖ `/kw`, `/iris`)
គឺជាពាក្យបញ្ជាចម្បងសម្រាប់គ្រប់គ្រងពិភពលោករបស់អ្នក។

| ពាក្យបញ្ជា | ការពិពណ៌នា | ការអនុញ្ញាត |
|:--- |:--- |:--- |
| `/kworld create <ឈ្មោះ> [វិមាត្រ] [Seed] [Main]` | បង្កើតពិភពលោកថ្មីដោយប្រើវិមាត្រជាក់លាក់។ | `kworld.admin` |
| `/kworld tp <ពិភពលោក> [អ្នកលេង]` | បញ្ជូនខ្លួនអ្នក ឬអ្នកលេងផ្សេងទៀតទៅកាន់ពិភពលោកណាមួយ។ | `kworld.admin` |
| `/kworld list` | បង្ហាញបញ្ជីវិមាត្រ KrudWorld ដែលមានទាំងអស់។ | `kworld.admin` |
| `/kworld pregen <កាំ/Radius> [ពិភពលោក] [កណ្តាល] [GUI]` | ចាប់ផ្តើមការបង្កើតពិភពលោកទុកជាមុន (Pregen) ដើម្បីប្រសិទ្ធភាពកាន់តែប្រសើរ។ | `kworld.admin` |
| `/kworld pregen stop` | បញ្ឈប់កិច្ចការ pregeneration ដែលកំពុងសកម្ម។ | `kworld.admin` |
| `/kworld pregen pause` | ផ្អាក ឬបន្តកិច្ចការ pregeneration ដែលកំពុងសកម្ម។ | `kworld.admin` |
| `/kworld studio` | បើកកម្មវិធីកែសម្រួល KrudWorld Studio។ | `kworld.studio` |
| `/kworld version` | បង្ហាញកំណែបច្ចុប្បន្នរបស់ Plugin។ | `kworld.admin` |

### សេចក្តីសង្ខេបនៃការអនុញ្ញាត (Permissions)
*   🔑 **`kworld.admin`**: ការចូលប្រើជាអ្នកគ្រប់គ្រងពេញលេញ (តាមលំនាំដើមគឺ OP)។
*   🛠️ **`kworld.studio`**: ការចូលប្រើកម្មវិធីកែសម្រួលពិភពលោក Studio (តាមលំនាំដើមគឺ OP)។
*   🚀 **`kworld.bypass`**: រំលងការរឹតបន្តឹងលើការបង្កើតពិភពលោកជាក់លាក់។

---

## 🛠️ ការបង្កើត និងការដំឡើង (Build & Installation)

### តម្រូវការ
*   **Java 21** ឬថ្មីជាងនេះ។
*   **Gradle** (រួមបញ្ចូលតាមរយៈ `gradlew`)។

### ការបង្កើតចេញពីប្រភពកូដ (Source)
```bash
# ចម្លងឃ្លាំង (Repository)
git clone https://github.com/KrudStudio/KRUD-World.git

# ចូលទៅក្នុងថត
cd KRUD-World

# បង្កើតគម្រោង
./gradlew build
```
ឯកសារ `.jar` ដែលបានចងក្រងរួច នឹងស្ថិតនៅក្នុង `build/libs/`។

---

## 📜 គុណលក្ខណៈ និងអាជ្ញាប័ណ្ណ (Attribution & Licensing)

គម្រោងនេះគឺជាការបំបែក (Fork) ចេញពី **Iris World Generator** ដោយ **Volmit Software** (Arcane Arts)។

*   **គម្រោងដើម:** [Iris on GitHub](https://github.com/VolmitSoftware/Iris)
*   **អាជ្ញាប័ណ្ណដើម:** GPL-3.0
*   **កែសម្រួលដោយ:** Krud Studio

គម្រោងនេះមានអាជ្ញាប័ណ្ណក្រោម **GNU General Public License v3.0**។ ក្រោមលក្ខខណ្ឌនៃអាជ្ញាប័ណ្ណនេះ គម្រោងដែលបំបែកនេះ និងដេរីវេណាមួយ **ត្រូវតែរក្សាទុកជាប្រភពបើកចំហ (Open Source)**។

---

## 🙌 ឥណទាន (Credits)
*   🛡️ **Krud Studio** — ការកែសម្រួល KRUD, ការដាក់ស្លាកសញ្ញា និងវិមាត្រ Khmer Kingdom។
*   🔮 **Volmit Software** — ម៉ាស៊ីន Iris ដើមដ៏អស្ចារ្យ។

---
<p align="center">បង្កើតឡើងដោយ ❤️ សម្រាប់សហគមន៍ KRUD SMP</p>
