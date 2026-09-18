# Random Quote (Android)

An Android application ported from the original Python Random Quote project (`HeptaHeaven/python-random-quote`), built with modern Kotlin and Jetpack Compose.

## Features
- **Core Principles**: Incorporates the original 14 engineering and software design principles from `quotes.txt`.
- **Random Quote Engine**: Generates random quotes on demand, tracking exploration count and avoiding consecutive duplicates.
- **Quote Navigation**: History back/forward navigation to revisit previously generated quotes.
- **Favorites & Persistence**: Mark quotes as favorites with local persistence across sessions.
- **Copy & Share**: Quick actions to copy principles to the clipboard or share via Android's native share sheet.
- **Browse Collection**: Bottom sheet interface to browse all principles categorized by focus area (Performance, Simplicity, Communication, Craftsmanship, Architecture, Philosophy).
- **Design & Typography**: Crafted with Material Design 3, custom display typography (Playfair Display & Plus Jakarta Sans), and an adaptive launcher icon.

## Tech Stack
- Kotlin 2.1.0 & Jetpack Compose
- Gradle 9.3.1 with Kotlin DSL
- Material 3 Design System
- StateFlow & ViewModel MVVM architecture
