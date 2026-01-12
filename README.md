# Surf

**Surf** is a high-performance server proxy designed for **Hytale**, flexibility, and
server compatibility.

---

## Features

- **High performance**: Capable of managing thousands of concurrent players with minimal overhead.
- **Wide server support**: First-class integration with Hytale server builds. Compatibility with other variants may work
  but is not guaranteed.

---

## Building Surf

Surf uses **Gradle** as its build system. We recommend using the Gradle wrapper included in the project for consistency
across environments.

1. Clone the repository:

```bash
git clone https://github.com/Rishon/Surf.git
cd Surf
```

2. Build the project using the Gradle wrapper:

```bash
./gradlew build
```

3. The compiled JAR file will be located in the `build/libs` directory.