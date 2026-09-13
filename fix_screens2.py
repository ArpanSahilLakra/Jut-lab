import os
import re

screens = [
    '/app/applet/app/src/main/java/com/example/ui/screens/StudentDashboardScreen.kt',
    '/app/applet/app/src/main/java/com/example/ui/screens/TeacherWorkspaceScreen.kt',
    '/app/applet/app/src/main/java/com/example/ui/screens/SettingsScreen.kt'
]

for screen in screens:
    with open(screen, 'r') as f:
        content = f.read()
    
    # We want to remove the item { NeoButton(text = "Back", ...) } block at the end
    # Just replace it anywhere
    new_content = re.sub(r'item\s*\{\s*NeoButton\(text = "Back", buttonType = NeoButtonType\.BACK, onClick = onBack, modifier = Modifier\.fillMaxWidth\(\)\)\s*\}', '', content)
    
    with open(screen, 'w') as f:
        f.write(new_content)
    print(f"Fixed {os.path.basename(screen)}")
