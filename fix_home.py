import re

with open('/app/applet/app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

# We want to keep everything up to the end of the "PDF LAB TUTOR" bento tile, and then close the LazyColumn and Column.

match = re.search(r'(// Bento Tile 6: PDF Lab Tutor \(Hinglish\).*?item \{.*?NeoCard\(.*?onClick.*?\{ onNavigate\("learning_material"\) \}.*?\{.*?Row\(.*?\{.*?Column.*?\{.*?Text\(.*?Text\(.*?\).*?\}).*?\}', content, re.DOTALL)

if match:
    pass

# A simpler way: we know it ends with "PDF Lab Tutor"
# Let's just truncate manually in Python using split
parts = content.split('// Bento Tile 6: PDF Lab Tutor (Hinglish)')
if len(parts) > 1:
    before = parts[0]
    after = parts[1]
    
    # We find the end of the item { ... }
    item_end = after.find('      }\n    }')
    if item_end != -1:
        fixed = before + '// Bento Tile 6: PDF Lab Tutor (Hinglish)' + after[:item_end + 13] + '  }\n}\n'
        with open('/app/applet/app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
            f.write(fixed)
        print("Fixed HomeScreen.kt")
