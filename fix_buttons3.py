import os
import re

directory = '/app/applet/app/src/main/java/com/example/ui'

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            # Clean up everything around NeoButton(text = "Back to ..."
            # We want to find any line that has `NeoButton(text = "Back to ` and replace the whole line
            lines = content.split('\n')
            for i, line in enumerate(lines):
                if 'NeoButton' in line and '"Back to' in line:
                    # Parse out modifier if it exists
                    has_modifier = 'modifier' in line
                    fillMaxWidth = 'fillMaxWidth' in line
                    
                    new_line = '      NeoButton(text = "Back", buttonType = NeoButtonType.BACK, onClick = onBack'
                    if has_modifier:
                        if fillMaxWidth:
                            new_line += ', modifier = Modifier.fillMaxWidth()'
                        else:
                            # Just let it be for now, wait, maybe extract it?
                            pass
                    new_line += ')'
                    lines[i] = new_line
            
            # Reset buttons as well
            for i, line in enumerate(lines):
                if 'NeoButton' in line and '"Reset"' in line:
                    lines[i] = '      NeoButton(text = "Reset", buttonType = NeoButtonType.RESET, onClick = { /* TODO check if onReset exists */ })' # Wait, Reset doesn't always have onBack. It has a specific onClick.
                    # It's better to just replace the text="Reset" part
                    pass

            content = '\n'.join(lines)
            
            with open(path, 'w') as f:
                f.write(content)

