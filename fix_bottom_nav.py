import re

with open('/app/applet/app/src/main/java/com/example/ui/components/NeoBottomNavigation.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'audioManager.playSound(com.example.audio.AppAudioManager.SoundType.CLICK, view)',
    'audioManager.playSound(com.example.audio.AppAudioManager.SoundType.CLICK)'
)

with open('/app/applet/app/src/main/java/com/example/ui/components/NeoBottomNavigation.kt', 'w') as f:
    f.write(content)
print("Fixed NeoBottomNavigation.kt")
