import os
import re

directory = '/app/applet/app/src/main/java/com/example/ui'

for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith('.kt'):
            path = os.path.join(root, file)
            with open(path, 'r') as f:
                content = f.read()
            
            # Remove the malformed parts at the end of the line
            content = re.sub(r'Modifier\.fillMaxWidth\(\, buttonType = com\.example\.ui\.components\.NeoButtonType\.BACK\)', 'Modifier.fillMaxWidth()', content)
            content = re.sub(r', buttonType = com\.example\.ui\.components\.NeoButtonType\.BACK\)', ')', content)
            
            with open(path, 'w') as f:
                f.write(content)
