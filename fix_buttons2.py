import os
import re

directory = '/app/applet/app/src/main/java/com/example/ui'

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            # Use regex to find NeoButton calls for "Back to..."
            # Let's just find NeoButton(text = "Back to X"
            # and replace it with NeoButton(text = "Back to X", buttonType = NeoButtonType.BACK
            
            # Since my previous sed injected buttonType = NeoButtonType.BACK, let's see if it's there
            content = re.sub(r'NeoButton\(\s*text\s*=\s*"Back to [^"]*"(?:,\s*buttonType\s*=\s*NeoButtonType\.BACK)?', 
                             lambda m: m.group(0) if 'buttonType' in m.group(0) else m.group(0) + ', buttonType = NeoButtonType.BACK', content)
            
            with open(path, 'w') as f:
                f.write(content)
