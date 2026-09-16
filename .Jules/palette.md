## 2025-05-18 - Dynamic Content Description for Password Visibility Toggle Icon Buttons
**Learning:** In Jetpack Compose `OutlinedTextField` trailing icons, state toggling buttons like password visibility controls require dynamic `contentDescription` strings (e.g. `if (passwordVisible) "Hide password" else "Show password"`) rather than `null`, enabling screen readers to inform visually-impaired users of both the icon's purpose and state change.
**Action:** When creating toggle buttons or icon buttons with state in Compose screens, always provide context-aware dynamic content descriptions.
